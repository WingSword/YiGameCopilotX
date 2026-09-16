package org.walks.rooms;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Single-node authoritative room service. Public responses never serialize stored rooms directly. */
public final class RoomServer implements AutoCloseable {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TTL = 12 * 60 * 60 * 1000L;
    private static final int MAX_ROOMS = 500;
    private final HttpServer http;
    private final ExecutorService workers = Executors.newFixedThreadPool(16);
    private final ScheduledExecutorService cleanup = Executors.newSingleThreadScheduledExecutor();
    private final Path dataFile;
    private final FileChannel lockChannel;
    private final FileLock lock;
    private final String adminToken;
    private final Set<String> allowedOrigins;
    private final String publicWebUrl;
    private Map<String,Object> state;
    private final Map<String,long[]> limits = new HashMap<>();
    private final Map<String,Long> presence = new HashMap<>();
    private final String[][] words = {
        {"咖啡","奶茶"}, {"月亮","太阳"}, {"火锅","烧烤"}, {"钢琴","吉他"},
        {"饺子","馄饨"}, {"海豚","鲸鱼"}, {"雪糕","冰淇淋"}, {"篮球","排球"},
        {"地铁","高铁"}, {"香水","花露水"}, {"围巾","手套"}, {"蛋糕","面包"},
        {"电影","电视剧"}, {"雨伞","雨衣"}, {"沙发","躺椅"}, {"台灯","手电筒"}
    };

    public RoomServer(String host, int port, Path directory, String adminToken) throws IOException {
        this(host,port,directory,adminToken,Set.of());
    }
    public RoomServer(String host, int port, Path directory, String adminToken,Set<String> allowedOrigins) throws IOException {
        this(host,port,directory,adminToken,allowedOrigins,"");
    }
    public RoomServer(String host, int port, Path directory, String adminToken,Set<String> allowedOrigins,String publicWebUrl) throws IOException {
        this.publicWebUrl=publicWebUrl.isBlank()?"":RoomInvitation.address(publicWebUrl,false);
        Files.createDirectories(directory);
        protect(directory, "rwx------");
        dataFile = directory.resolve("rooms.json");
        lockChannel = FileChannel.open(directory.resolve("service.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        lock = lockChannel.tryLock();
        if (lock == null) throw new IOException("Data directory already in use");
        this.adminToken = adminToken;
        this.allowedOrigins = Set.copyOf(allowedOrigins);
        // A corrupt snapshot is an operator error; never silently discard rooms or statistics.
        state = Files.exists(dataFile) ? Json.object(Json.parse(Files.readString(dataFile))) :
            map("rooms", new LinkedHashMap<>(), "entries", new LinkedHashMap<>(), "history", new ArrayList<>(), "createdRooms", 0L, "startedGames", 0L, "completedGames", 0L);
        http = HttpServer.create(new InetSocketAddress(host, port), 64);
        http.createContext("/", this::handle);
        http.setExecutor(workers);
    }
    public void start() { http.start(); cleanup.scheduleAtFixedRate(this::expire, 60, 60, TimeUnit.SECONDS); cleanup.scheduleAtFixedRate(this::advanceTimedGames,1,1,TimeUnit.SECONDS); }
    public int port() { return http.getAddress().getPort(); }
    public static void main(String[] args) throws Exception {
        String admin = System.getenv().getOrDefault("ADMIN_TOKEN", "");
        if (admin.length() < 32) throw new IllegalArgumentException("Set ADMIN_TOKEN to at least 32 random characters");
        RoomServer server = new RoomServer(System.getenv().getOrDefault("HOST", "0.0.0.0"),
            Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")), Path.of(System.getenv().getOrDefault("DATA_DIR", "data")), admin,
            new HashSet<>(Arrays.stream(System.getenv().getOrDefault("WEB_ORIGINS", "").split(",")).map(String::trim).filter(s->!s.isEmpty()).toList()),
            System.getenv().getOrDefault("PUBLIC_WEB_URL",""));
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        server.start();
        System.out.println("Room service ready on port " + server.port());
    }
    private void handle(HttpExchange e) throws IOException {
        int status = 200; Object response;
        try {
            String path = e.getRequestURI().getPath();
            String method = e.getRequestMethod();
            String origin=e.getRequestHeaders().getFirst("Origin");
            if(origin!=null && !path.startsWith("/api/v1/admin/") && allowedOrigins.contains(origin)) {
                e.getResponseHeaders().set("Access-Control-Allow-Origin",origin);
                e.getResponseHeaders().set("Vary","Origin");
                e.getResponseHeaders().set("Access-Control-Expose-Headers","Retry-After");
            }
            if(method.equals("OPTIONS")) {
                if(origin==null || !allowedOrigins.contains(origin) || path.startsWith("/api/v1/admin/")) throw fail(403,"ORIGIN_DENIED","此网页地址未获服务端允许");
                e.getResponseHeaders().set("Access-Control-Allow-Methods","GET, POST, DELETE, OPTIONS");
                e.getResponseHeaders().set("Access-Control-Allow-Headers","Authorization, Content-Type");
                e.getResponseHeaders().set("Access-Control-Max-Age","600");
                e.sendResponseHeaders(204,-1); e.close(); return;
            }
            if (!List.of("GET", "POST", "DELETE").contains(method)) throw fail(405,"METHOD_NOT_ALLOWED","不支持的请求方法");
            String ip = e.getRemoteAddress().getAddress().getHostAddress();
            // Never trust client-supplied proxy headers. Edge proxy applies its own per-client limits.
            throttle(ip, path, method);
            Map<String,Object> body = new LinkedHashMap<>();
            if (method.equals("POST")) {
                if (!Optional.ofNullable(e.getRequestHeaders().getFirst("Content-Type")).orElse("").toLowerCase(Locale.ROOT).startsWith("application/json")) throw fail(415,"CONTENT_TYPE","需要 JSON 请求");
                byte[] bytes = e.getRequestBody().readNBytes(8193);
                if (bytes.length > 8192) throw fail(413,"TOO_LARGE","请求内容过长");
                body = Json.object(Json.parse(new String(bytes, StandardCharsets.UTF_8)));
            }
            String auth = Optional.ofNullable(e.getRequestHeaders().getFirst("Authorization")).orElse("");
            String token = auth.startsWith("Bearer ") ? auth.substring(7) : "";
            synchronized (this) {
                // Roll back in-memory mutations if validation or durable persistence fails.
                String before = method.equals("GET") ? null : Json.write(state);
                try {
                    response = route(method, path, token, body);
                    if (before != null && !before.equals(Json.write(state))) save();
                } catch (Exception error) {
                    if (before != null) state = Json.object(Json.parse(before));
                    throw error;
                }
            }
        } catch (ApiError error) {
            status = error.status; response = map("code",error.code,"message",error.getMessage());
        } catch (IllegalArgumentException | NullPointerException error) {
            status = 400; response = map("code","INVALID_REQUEST","message","请求格式或参数不正确");
        } catch (Exception error) {
            status = 503; response = map("code","SERVICE_UNAVAILABLE","message","服务暂不可用，请稍后重试");
            System.err.println("Request failed: " + error.getClass().getSimpleName());
        }
        byte[] bytes = Json.write(response).getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
        e.getResponseHeaders().set("Cache-Control","no-store");
        e.getResponseHeaders().set("X-Content-Type-Options","nosniff");
        if (status == 429) e.getResponseHeaders().set("Retry-After","60");
        e.sendResponseHeaders(status,bytes.length);
        try (OutputStream out = e.getResponseBody()) { out.write(bytes); } finally { e.close(); }
    }
    private synchronized void throttle(String ip, String path, String method) {
        long now = System.currentTimeMillis();
        boolean entry = method.equals("POST") && (path.equals("/api/v1/rooms") || path.endsWith("/join"));
        String bucket = ip + (entry ? ":entry" : ":all");
        limits.entrySet().removeIf(x -> now - x.getValue()[0] > 60_000);
        if (limits.size() > 10000) throw fail(429,"RATE_LIMIT","请求过多，请稍后重试");
        long[] count = limits.computeIfAbsent(bucket, k -> new long[]{now,0});
        if (++count[1] > (entry ? 30 : 3000)) throw fail(429,"RATE_LIMIT","请求过多，请稍后重试");
    }
    private Object route(String method, String path, String token, Map<String,Object> body) {
        if (method.equals("GET") && path.equals("/health")) return map("status","ok","protocol",1);
        if (path.startsWith("/api/v1/admin/")) {
            if (token.isEmpty() || !constant(token,adminToken)) throw fail(401,"UNAUTHORIZED","管理凭据无效");
            if (method.equals("GET") && path.equals("/api/v1/admin/stats")) return map("activeRooms",rooms().size(),"createdRooms",state.get("createdRooms"),"startedGames",state.get("startedGames"),"completedGames",state.get("completedGames"),"recentGames",state.get("history"));
            if (method.equals("GET") && path.equals("/api/v1/admin/rooms")) return map("rooms", rooms().values().stream().map(v -> {
                Map<String,Object> r = Json.object(v);
                return map("roomId",r.get("roomId"),"status",r.get("status"),"players",players(r).size(),"createdAt",r.get("createdAt"),"updatedAt",r.get("updatedAt"));
            }).toList());
            if (method.equals("DELETE") && path.matches("/api/v1/admin/rooms/[0-9]{6}")) { closeRoom(path.substring(path.lastIndexOf('/')+1)); return map("ok",true); }
            throw fail(404,"NOT_FOUND","接口不存在");
        }
        if (method.equals("POST") && path.equals("/api/v1/rooms")) return enter(body, null);
        if (!path.matches("/api/v1/rooms/[0-9]{6}(/[a-z]+)?")) throw fail(404,"NOT_FOUND","接口不存在");
        String[] parts = path.split("/");
        String code = parts[4];
        Map<String,Object> room = room(code);
        String action = parts.length > 5 ? parts[5] : "";
        if (method.equals("POST") && action.equals("join")) return enter(body, room);
        Map<String,Object> self = players(room).stream().filter(p -> constant(str(p,"token"),token) && !token.isEmpty()).findFirst()
            .orElseThrow(() -> fail(401,"SESSION_EXPIRED","房间会话已失效，请重新加入"));
        presence.put(str(self,"id"),System.currentTimeMillis());
        if (method.equals("GET") && action.isEmpty()) return view(room,self);
        if (method.equals("DELETE") && action.isEmpty()) { host(room,self); closeRoom(code); return map("ok",true); }
        if (!method.equals("POST")) throw fail(405,"METHOD_NOT_ALLOWED","不支持的请求方法");
        switch(action) {
            case "hostview" -> {
                host(room,self);
                if(!str(room,"roundId").equals(str(body,"roundId"))) throw fail(409,"STALE_ROUND","本局已更新，请刷新后重试");
                String mode=text(body,"hostView",1,10);
                if(!List.of("player","admin").contains(mode)) throw fail(400,"VALIDATION","房主模式无效");
                if(!mode.equals(hostView(room))) {
                    if(number(body,"revision",-1)!=number(room,"hostViewRevision",0)) throw fail(409,"STALE_VIEW","房主模式已更新，请重试");
                    setHostView(room,mode,true);
                }
            }
            case "invite" -> {
                host(room,self); waiting(room);
                String web=publicWebUrl.isEmpty()?str(body,"webUrl"):publicWebUrl;
                if(web.isBlank()) throw fail(409,"WEB_NOT_CONFIGURED","请填写已经部署的网页版地址，或由服务端配置 PUBLIC_WEB_URL");
                if(str(room,"invitation").isEmpty()) room.put("invitation",token(24));
                // A deployed web origin proxies /api; native clients may still use the default HTTP endpoint.
                try { return RoomInvitation.view(web,publicWebUrl.isEmpty()?text(body,"serverUrl",1,256):RoomInvitation.origin(web),code,str(room,"invitation")); }
                catch(IllegalArgumentException error) { throw fail(400,"VALIDATION",error.getMessage()); }
            }
            case "ready" -> {
                waiting(room);
                if (!(body.get("ready") instanceof Boolean)) throw fail(400,"VALIDATION","准备状态无效");
                self.put("ready",body.get("ready"));
            }
            case "start" -> {
                host(room,self); waiting(room);
                if (number(body,"roundNumber",0) != number(room,"roundNumber",0)) throw fail(409,"STALE_ROUND","房间轮次已更新");
                List<Map<String,Object>> ps = players(room);
                int spies = number(room,"spyCount",1), blanks = number(room,"blankCount",0);
                boolean oneNight = gameType(room).equals("werewolf");
                boolean ledger = gameType(room).equals("ledger");
                boolean hunt = gameType(room).equals("hunt");
                boolean avalon=gameType(room).equals("avalon"), drawing=gameType(room).equals("drawing");
                if (ledger ? ps.size() < 2 : oneNight || hunt || avalon || drawing ? ps.size() != number(room,"maxPlayers",5) : ps.size() < 4 || spies + blanks >= ps.size() / 2.0) throw fail(409,"PLAYER_COUNT",ledger ? "至少 2 人一起记账" : oneNight || hunt || avalon || drawing ? "请等待配置人数全部加入" : "至少 4 人，卧底与白板合计需少于一半");
                if (ps.stream().anyMatch(p -> !Boolean.TRUE.equals(p.get("ready")))) throw fail(409,"NOT_READY","还有玩家未准备");
                if (ps.stream().anyMatch(p -> !connected(p))) throw fail(409,"PLAYER_OFFLINE","请等待离线玩家重连，或移出该玩家");
                if (ledger) {
                    room.put("ledger",LedgerGame.start(ps,Json.object(room.get("ledgerConfig"))));
                } else if(drawing) {
                    room.put("game",DrawingGame.start(ps,RANDOM));
                } else if(avalon) {
                    Map<String,Object> game=AvalonGame.start(ps,AvalonGame.deck(room.get("roles"),ps.size()),RANDOM);
                    room.put("game",game);
                    @SuppressWarnings("unchecked") List<Map<String,Object>> seats=(List<Map<String,Object>>)game.get("seats");
                    for(int i=0;i<ps.size();i++) ps.get(i).put("identity",map("role",AvalonGame.name((String)seats.get(i).get("role")),"word","身份与初始信息仅自己可见"));
                } else if (hunt) {
                    Map<String,Object> game = HuntGame.start(ps,number(room,"witchCount",1),RANDOM);
                    room.put("game",game);
                    @SuppressWarnings("unchecked") List<Map<String,Object>> seats = (List<Map<String,Object>>)game.get("seats");
                    for(int i=0;i<ps.size();i++) ps.get(i).put("identity",map("role",HuntGame.name((String)seats.get(i).get("role")),"word","单身份简化玩法；按下方提示行动"));
                } else if (oneNight) {
                    Map<String,Object> game = OneNightGame.start(ps,OneNightGame.deck(room.get("roles"),ps.size()),RANDOM);
                    room.put("game",game);
                    @SuppressWarnings("unchecked") List<Map<String,Object>> seats = (List<Map<String,Object>>)game.get("seats");
                    for(int i=0;i<ps.size();i++) ps.get(i).put("identity",map("role",OneNightGame.name((String)seats.get(i).get("initial")),"word","初始身份；请根据下方提示完成本局"));
                } else {
                List<Map<String,Object>> shuffled = new ArrayList<>(ps); Collections.shuffle(shuffled,RANDOM);
                String[] pair = words[RANDOM.nextInt(words.length)];
                for (int i=0;i<shuffled.size();i++) {
                    Map<String,Object> p = shuffled.get(i);
                    p.put("identity",map("role",i < spies ? "卧底" : i < spies+blanks ? "白板" : "平民", "word",i < spies ? pair[1] : i < spies+blanks ? "你没有词条" : pair[0]));
                }
                }
                room.put("status","PLAYING"); room.put("roundId",UUID.randomUUID().toString());
                room.put("roundNumber",number(room,"roundNumber",0)+1); room.put("startedAt",System.currentTimeMillis());
                room.put("roundPlayers",ps.size()); room.put("winner","");
                setHostView(room,"player",true);
                increment("startedGames");
            }
            case "finish" -> {
                host(room,self); sameRound(room,body);
                if(List.of("werewolf","hunt","avalon","drawing").contains(gameType(room))) throw fail(409,"AUTOMATIC_RESULT","本玩法由云端自动结算胜负");
                String winner = gameType(room).equals("ledger") ? "记账结束" : text(body,"winner",1,8);
                if (!gameType(room).equals("ledger") && !List.of("平民","卧底","白板","未判定").contains(winner)) throw fail(400,"VALIDATION","请选择获胜阵营");
                if (str(room,"status").equals("FINISHED")) {
                    if (!winner.equals(str(room,"winner"))) throw fail(409,"ALREADY_FINISHED","本局已经结算");
                    return view(room,self);
                }
                if (!str(room,"status").equals("PLAYING")) throw fail(409,"NOT_PLAYING","本局尚未开始");
                complete(room,winner);
            }
            case "ledger" -> {
                sameRound(room,body);
                if(!gameType(room).equals("ledger") || !str(room,"status").equals("PLAYING")) throw fail(409,"NOT_PLAYING","记账尚未开始或已经结束");
                try { LedgerGame.act(Json.object(room.get("ledger")),str(self,"id"),str(self,"id").equals(str(room,"hostId")),body); }
                catch(IllegalArgumentException error) { throw fail(409,"INVALID_ACTION",error.getMessage()); }
            }
            case "draw" -> {
                sameRound(room,body);
                if(!gameType(room).equals("drawing") || room.get("game")==null) throw fail(409,"NOT_PLAYING","绘画尚未开始");
                Map<String,Object> game=Json.object(room.get("game"));
                try { DrawingGame.act(game,str(self,"id"),str(self,"id").equals(str(room,"hostId")),body,System.currentTimeMillis()); }
                catch(IllegalArgumentException error) { throw fail(409,"INVALID_ACTION",error.getMessage()); }
                if(str(game,"phase").equals("RESULT") && !str(room,"status").equals("FINISHED")) complete(room,str(game,"winner"));
            }
            case "game" -> {
                sameRound(room,body);
                if(!List.of("werewolf","hunt","avalon").contains(gameType(room)) || room.get("game")==null) throw fail(409,"NOT_PLAYING","本局尚未开始");
                Map<String,Object> game=Json.object(room.get("game"));
                try {
                    if(gameType(room).equals("avalon")) AvalonGame.act(game,str(self,"id"),text(body,"stepId",1,64),text(body,"choice",1,100),body.get("targets"));
                    else if(gameType(room).equals("hunt")) HuntGame.act(game,str(self,"id"),str(self,"id").equals(str(room,"hostId")),text(body,"stepId",1,64),text(body,"choice",1,100));
                    else OneNightGame.act(game,str(self,"id"),str(self,"id").equals(str(room,"hostId")),text(body,"stepId",1,64),text(body,"choice",1,100));
                }
                catch(IllegalArgumentException error) { throw fail(409,"INVALID_ACTION",error.getMessage()); }
                if(str(game,"phase").equals("RESULT") && !str(room,"status").equals("FINISHED")) complete(room,str(game,"winner"));
            }
            case "next" -> {
                host(room,self); sameRound(room,body);
                if (!str(room,"status").equals("FINISHED")) throw fail(409,"NOT_FINISHED","请先结束本局");
                room.put("status","WAITING"); room.put("winner",""); room.remove("game"); room.remove("ledger");
                setHostView(room,"admin",true);
                for (Map<String,Object> p : players(room)) { p.remove("identity"); p.put("ready",str(p,"id").equals(str(room,"hostId"))); }
            }
            case "kick" -> {
                host(room,self); waiting(room);
                String target = text(body,"playerId",1,64);
                if (target.equals(str(self,"id"))) throw fail(400,"VALIDATION","房主请使用离开房间");
                removePlayer(room,target);
            }
            case "leave" -> {
                if(gameType(room).equals("ledger") && str(room,"status").equals("PLAYING")) complete(room,"记账结束：玩家离开");
                if(List.of("werewolf","hunt","avalon","drawing").contains(gameType(room)) && str(room,"status").equals("PLAYING")) {
                    Json.object(room.get("game")).put("phase","ABORTED");
                    complete(room,"中止：玩家离开");
                }
                removePlayer(room,str(self,"id"));
                if (players(room).isEmpty()) closeRoom(code);
                else if (str(room,"hostId").equals(str(self,"id"))) {
                    room.put("hostId",players(room).get(0).get("id")); players(room).get(0).put("ready",true);
                    setHostView(room,str(room,"status").equals("WAITING")?"admin":"player",true);
                }
                room.put("updatedAt",System.currentTimeMillis()); return map("ok",true);
            }
            default -> throw fail(404,"NOT_FOUND","接口不存在");
        }
        room.put("updatedAt",System.currentTimeMillis());
        return view(room,self);
    }
    private Object enter(Map<String,Object> body, Map<String,Object> room) {
        String nickname = text(body,"nickname",1,20);
        String requestId = text(body,"requestId",32,100);
        String signature = digest(Json.write(body));
        Map<String,Object> cached = entries().get(requestId) instanceof Map<?,?> m ? Json.object(m) : null;
        if (cached != null) {
            String expectedRoom = room == null ? "" : str(room,"roomId");
            if (!constant(signature,str(cached,"signature")) || !expectedRoom.equals(str(cached,"joinRoom"))) throw fail(409,"REQUEST_CONFLICT","请重新尝试进入房间");
            Map<String,Object> live = room(str(cached,"roomId"));
            Map<String,Object> p = players(live).stream().filter(x -> str(x,"id").equals(str(cached,"playerId"))).findFirst().orElseThrow(() -> fail(401,"SESSION_EXPIRED","会话已失效"));
            presence.put(str(p,"id"),System.currentTimeMillis());
            return entryView(live,p,str(cached,"roomKey"));
        }
        String key = str(body,"roomKey").trim();
        String joinRoom = room == null ? "" : str(room,"roomId");
        if (room == null) {
            if (!List.of("spy","werewolf","ledger","hunt","avalon","drawing").contains(str(body,"gameType"))) throw fail(400,"UNSUPPORTED_GAME","请选择支持的网络游戏");
            if (rooms().size() >= MAX_ROOMS) throw fail(503,"ROOM_LIMIT","房间已满，请稍后再试");
            if (key.isEmpty()) key = token(6);
            if (key.length()<6 || key.length()>32) throw fail(400,"VALIDATION","密钥需要 6–32 个字符");
            int max = number(body,"maxPlayers",12), spies = number(body,"spyCount",1), blanks = number(body,"blankCount",0);
            boolean oneNight = str(body,"gameType").equals("werewolf");
            boolean ledger = str(body,"gameType").equals("ledger");
            boolean hunt = str(body,"gameType").equals("hunt");
            boolean avalon=str(body,"gameType").equals("avalon"), drawing=str(body,"gameType").equals("drawing");
            int witches = number(body,"witchCount",1);
            if(hunt) HuntGame.config(max,witches);
            if(drawing) DrawingGame.config(max);
            List<String> roles = oneNight ? OneNightGame.deck(body.get("roles"),max) : avalon ? AvalonGame.deck(body.get("roles"),max) : List.of();
            if (ledger ? max<2 || max>20 : !oneNight && !hunt && !avalon && !drawing && (max<4 || max>16 || spies<1 || spies>3 || blanks<0 || blanks>1 || spies+blanks>=max/2.0)) throw fail(400,"VALIDATION","人数或身份配置无效");
            String code;
            do { code = String.format("%06d",RANDOM.nextInt(1_000_000)); } while(rooms().containsKey(code));
            String salt = token(16);
            room = map("roomId",code,"status","WAITING","players",new ArrayList<>(),"roomSalt",salt,"roomHash",hashKey(key,salt),"maxPlayers",max,"spyCount",spies,"blankCount",blanks,
                "createdAt",System.currentTimeMillis(),"updatedAt",System.currentTimeMillis(),"roundNumber",0,"roundId","","winner","");
            room.put("gameType",str(body,"gameType")); room.put("roles",roles);
            if(ledger) room.put("ledgerConfig",LedgerGame.config(body));
            if(hunt) room.put("witchCount",witches);
            rooms().put(code,room); increment("createdRooms");
        } else {
            String invitation=str(body,"inviteToken");
            if(!invitation.isEmpty()) {
                if(invitation.length()!=32 || str(room,"invitation").isEmpty() || !constant(invitation,str(room,"invitation"))) throw fail(403,"INVALID_INVITATION","邀请已失效，请让房主重新分享");
            } else if (key.length()<6 || key.length()>32 || !constant(hashKey(key,str(room,"roomSalt")),str(room,"roomHash"))) throw fail(403,"WRONG_KEY","房间密钥不正确");
            waiting(room);
            if (players(room).size() >= number(room,"maxPlayers",12)) throw fail(409,"ROOM_FULL","房间人数已满");
        }
        if (players(room).stream().anyMatch(p -> nickname.equals(str(p,"nickname")))) throw fail(409,"NAME_TAKEN","房间内已有同名玩家");
        boolean owner = players(room).isEmpty();
        Map<String,Object> player = map("id",UUID.randomUUID().toString(),"nickname",nickname,"token",token(32),"ready",owner);
        players(room).add(player);
        if (owner) { room.put("hostId",player.get("id")); setHostView(room,"admin",false); }
        presence.put(str(player,"id"),System.currentTimeMillis());
        room.put("updatedAt",System.currentTimeMillis());
        entries().put(requestId,map("signature",signature,"joinRoom",joinRoom,"roomId",room.get("roomId"),"playerId",player.get("id"),"roomKey",owner ? key : "", "createdAt",System.currentTimeMillis()));
        return entryView(room,player,owner ? key : "");
    }
    private Object entryView(Map<String,Object> room, Map<String,Object> player, String key) { return map("token",player.get("token"),"roomKey",key,"room",view(room,player)); }
    private Object view(Map<String,Object> r, Map<String,Object> self) {
        return map("roomId",r.get("roomId"),"gameType",gameType(r),"status",r.get("status"),"hostId",r.get("hostId"),"selfId",self.get("id"),
            "maxPlayers",r.get("maxPlayers"),"spyCount",r.get("spyCount"),"blankCount",r.get("blankCount"),"roundId",r.get("roundId"),"roundNumber",r.get("roundNumber"),"winner",r.get("winner"),
            "hostView",hostView(r),"hostViewRevision",r.getOrDefault("hostViewRevision",0),"hostViewChanges",r.getOrDefault("hostViewChanges",List.of()),
            "players",players(r).stream().map(p -> map("id",p.get("id"),"nickname",p.get("nickname"),"ready",p.get("ready"),"connected",connected(p))).toList(),
            "identity",self.get("identity"),"roles",r.getOrDefault("roles",List.of()),"witchCount",r.getOrDefault("witchCount",1),
            "ledgerConfig",r.get("ledgerConfig"),"ledger",r.get("ledger")==null ? null : LedgerGame.view(Json.object(r.get("ledger")),str(self,"id")),
            "drawing",r.get("game")!=null && gameType(r).equals("drawing") ? DrawingGame.view(Json.object(r.get("game")),str(self,"id"),System.currentTimeMillis()) : null,
            "game",gameView(r,self));
    }
    private Object gameView(Map<String,Object> room,Map<String,Object> self) {
        if(room.get("game")==null) return null;
        Map<String,Object> game=Json.object(room.get("game"));
        String id=str(self,"id"); boolean host=id.equals(str(room,"hostId"));
        return switch(gameType(room)) {
            case "hunt" -> HuntGame.view(game,id,host);
            case "avalon" -> AvalonGame.view(game,id);
            case "werewolf" -> OneNightGame.view(game,id,host);
            default -> null;
        };
    }
    private synchronized void advanceTimedGames() {
        long now=System.currentTimeMillis();
        List<Map<String,Object>> due=rooms().values().stream().map(Json::object).filter(room -> {
            if(!gameType(room).equals("drawing") || !str(room,"status").equals("PLAYING")) return false;
            Map<String,Object> game=Json.object(room.get("game"));
            return str(game,"phase").equals("DRAWING") && ((Number)game.get("deadline")).longValue()<=now;
        }).toList();
        if(due.isEmpty()) return;
        String before=Json.write(state);
        try {
            for(Map<String,Object> room:due) { DrawingGame.timeout(Json.object(room.get("game")),now); room.put("updatedAt",now); }
            save();
        } catch(Exception error) { state=Json.object(Json.parse(before)); System.err.println("Timer persistence failed: "+error.getClass().getSimpleName()); }
    }
    private static String gameType(Map<String,Object> r) { return (String)r.getOrDefault("gameType","spy"); }
    private static String hostView(Map<String,Object> room) { return (String)room.getOrDefault("hostView",str(room,"status").equals("WAITING")?"admin":"player"); }
    @SuppressWarnings("unchecked") private static void setHostView(Map<String,Object> room,String mode,boolean notify) {
        room.put("hostView",mode);
        if(!notify) return;
        int revision=number(room,"hostViewRevision",0)+1; room.put("hostViewRevision",revision);
        List<Object> events=(List<Object>)room.computeIfAbsent("hostViewChanges",k->new ArrayList<>());
        events.add(map("revision",revision,"mode",mode,"hostId",room.get("hostId")));
        while(events.size()>20) events.remove(0);
    }
    private void complete(Map<String,Object> room,String winner) {
        room.put("status","FINISHED"); room.put("winner",winner); increment("completedGames");
        history().add(map("roomId",room.get("roomId"),"roundId",room.get("roundId"),"gameType",gameType(room), "playerCount",room.get("roundPlayers"),"winner",winner,"finishedAt",System.currentTimeMillis(),"durationMillis",System.currentTimeMillis()-((Number)room.get("startedAt")).longValue()));
        while(history().size()>1000) history().remove(0);
    }
    private boolean connected(Map<String,Object> p) { return System.currentTimeMillis() - presence.getOrDefault(str(p,"id"),0L) < 20_000; }
    private Map<String,Object> room(String code) {
        Object value = rooms().get(code);
        if (value == null) throw fail(404,"ROOM_NOT_FOUND","房间不存在或已解散");
        Map<String,Object> r = Json.object(value);
        if (System.currentTimeMillis()-((Number)r.get("updatedAt")).longValue()>TTL) throw fail(410,"ROOM_EXPIRED","房间已过期，请创建新房间");
        return r;
    }
    private void host(Map<String,Object> r, Map<String,Object> self) { if (!str(r,"hostId").equals(str(self,"id"))) throw fail(403,"HOST_ONLY","仅房主可以操作"); }
    private void waiting(Map<String,Object> r) { if (!str(r,"status").equals("WAITING")) throw fail(409,"ROUND_ACTIVE","请等待本局结束后再操作"); }
    private void sameRound(Map<String,Object> r,Map<String,Object> b) { if (str(r,"roundId").isEmpty() || !str(r,"roundId").equals(str(b,"roundId"))) throw fail(409,"STALE_ROUND","本局已更新，请刷新后重试"); }
    private void removePlayer(Map<String,Object> r,String id) { players(r).removeIf(p -> str(p,"id").equals(id)); entries().values().removeIf(v -> str(Json.object(v),"playerId").equals(id)); presence.remove(id); }
    private void closeRoom(String id) { Object r = rooms().remove(id); if(r != null) players(Json.object(r)).forEach(p -> presence.remove(str(p,"id"))); entries().values().removeIf(v -> str(Json.object(v),"roomId").equals(id)); }
    private synchronized void expire() {
        String before = Json.write(state);
        try {
            List<String> expired = rooms().entrySet().stream().filter(e -> System.currentTimeMillis()-((Number)Json.object(e.getValue()).get("updatedAt")).longValue()>TTL).map(Map.Entry::getKey).toList();
            expired.forEach(this::closeRoom);
            if (!expired.isEmpty()) save();
        } catch(Exception error) { state = Json.object(Json.parse(before)); System.err.println("Snapshot cleanup failed"); }
    }
    private void save() throws IOException {
        Path tmp = dataFile.resolveSibling("rooms.tmp");
        byte[] data = Json.write(state).getBytes(StandardCharsets.UTF_8);
        try (FileChannel channel = FileChannel.open(tmp,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE)) {
            protect(tmp,"rw-------"); java.nio.ByteBuffer bytes = java.nio.ByteBuffer.wrap(data);
            while(bytes.hasRemaining()) channel.write(bytes); channel.force(true);
        }
        Files.move(tmp,dataFile,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
    }
    private static void protect(Path p,String mode) throws IOException { try { Files.setPosixFilePermissions(p,PosixFilePermissions.fromString(mode)); } catch(UnsupportedOperationException ignored) {} }
    private void increment(String key) { state.put(key,((Number)state.get(key)).longValue()+1); }
    private Map<String,Object> rooms() { return Json.object(state.get("rooms")); }
    private Map<String,Object> entries() { return Json.object(state.get("entries")); }
    @SuppressWarnings("unchecked") private List<Object> history() { return (List<Object>)state.get("history"); }
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> players(Map<String,Object> r) { return (List<Map<String,Object>>)r.get("players"); }
    private static int number(Map<String,Object> m,String key,int fallback) { Object v=m.get(key); if(v==null)return fallback; if(!(v instanceof Number n) || n.doubleValue()!=n.intValue())throw fail(400,"VALIDATION","请输入整数"); return ((Number)v).intValue(); }
    private static String str(Map<String,Object> m,String key) { Object v=m.get(key); return v instanceof String s ? s : ""; }
    private static String text(Map<String,Object> m,String key,int min,int max) { String s=str(m,key).trim(); if(s.length()<min || s.length()>max || s.chars().anyMatch(c -> c<32))throw fail(400,"VALIDATION","请检查昵称、密钥及房间参数"); return s; }
    static Map<String,Object> map(Object... pairs) { Map<String,Object> m=new LinkedHashMap<>(); for(int i=0;i<pairs.length;i+=2)m.put((String)pairs[i],pairs[i+1]); return m; }
    private static String token(int bytes) { byte[] b=new byte[bytes]; RANDOM.nextBytes(b); return Base64.getUrlEncoder().withoutPadding().encodeToString(b); }
    private static String digest(String value) { try { return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch(GeneralSecurityException e) { throw new IllegalStateException(e); } }
    private static String hashKey(String value,String salt) {
        PBEKeySpec spec=new PBEKeySpec(value.toCharArray(),salt.getBytes(StandardCharsets.UTF_8),120_000,256);
        try { return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded()); }
        catch(GeneralSecurityException e) { throw new IllegalStateException(e); } finally { spec.clearPassword(); }
    }
    private static boolean constant(String a,String b) { return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),b.getBytes(StandardCharsets.UTF_8)); }
    private static ApiError fail(int status,String code,String message) { return new ApiError(status,code,message); }
    private static final class ApiError extends RuntimeException { final int status; final String code; ApiError(int status,String code,String message){super(message);this.status=status;this.code=code;} }
    public void close() { http.stop(1); cleanup.shutdownNow(); workers.shutdownNow(); try {lock.release();lockChannel.close();} catch(IOException ignored) {} }
}

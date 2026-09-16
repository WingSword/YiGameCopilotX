package org.walks.rooms;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.walks.rooms.RoomServer.map;

/** Real HTTP concurrency/restart contract and deterministic rule coverage for the new modes. */
public final class CloudExpansionTest {
    private static int checks;
    private static String base;
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private record Response(int status, Map<String,Object> body) {}
    private static void check(boolean valid,String message) { checks++; if(!valid) throw new AssertionError(message); }
    private static Response call(String path,String token,Object body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + path)).header("Content-Type","application/json");
        if(!token.isEmpty()) request.header("Authorization","Bearer " + token);
        if(body != null) request.POST(HttpRequest.BodyPublishers.ofString(Json.write(body)));
        HttpResponse<String> response = HTTP.send(request.build(),HttpResponse.BodyHandlers.ofString());
        return new Response(response.statusCode(),Json.object(Json.parse(response.body())));
    }
    private static Map<String,Object> entry(String type,String name,int count) { return map("nickname",name,"roomKey","Secret88","requestId",UUID.randomUUID().toString(),"gameType",type,"maxPlayers",count,"ledgerPreset","chips","initialBalance",1000L,"witchCount",1); }
    private static Map<String,Object> command(long revision,String op,Object... fields) {
        Map<String,Object> body = map("requestId",UUID.randomUUID().toString(),"revision",revision,"operation",op,"memo",""); body.putAll(map(fields)); return body;
    }
    private static Map<String,Object> ledger(Map<String,Object> room) { return Json.object(room.get("ledger")); }
    private static long balance(Map<String,Object> state,String id) { return ((Number)rows(state,"accounts").stream().filter(a->id.equals(a.get("id"))).findFirst().orElseThrow().get("balance")).longValue(); }
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> rows(Map<String,Object> state,String key) { return (List<Map<String,Object>>)state.get(key); }
    private static void rejected(Runnable action,Map<String,Object> state,String description) {
        String before = Json.write(state);
        try { action.run(); throw new AssertionError("accepted " + description); } catch(IllegalArgumentException expected) { checks++; }
        check(before.equals(Json.write(state)),"invalid action changed state: " + description);
    }
    private static void huntBoundaries() {
        List<Map<String,Object>> players = new ArrayList<>();
        for(int i=0;i<5;i++) players.add(map("id","p"+i,"nickname","玩家"+i));
        Map<String,Object> game = HuntGame.start(players,2,new Random(18));
        List<Map<String,Object>> seats = rows(game,"seats");
        List<String> witches = seats.stream().filter(p->p.get("role").equals("WITCH")).map(p->(String)p.get("id")).toList();
        List<String> villagers = seats.stream().filter(p->p.get("role").equals("VILLAGER")).map(p->(String)p.get("id")).toList();
        String sheriff = (String)seats.stream().filter(p->p.get("role").equals("SHERIFF")).findFirst().orElseThrow().get("id");
        for(Map<String,Object> p:players) huntAct(game,(String)p.get("id"),sheriff,"confirm");
        for(Map<String,Object> p:players) {
            String self=(String)p.get("id");
            huntAct(game,self,sheriff,self.equals(witches.get(0)) ? "player:"+sheriff : self.equals(witches.get(1)) ? "player:"+villagers.get(0) : "skip");
        }
        check(game.get("phase").equals("DAY") && seats.stream().allMatch(p->Boolean.TRUE.equals(p.get("alive"))),"tied witch targets kill nobody");
        huntAct(game,sheriff,sheriff,"vote");
        for(int i=0;i<players.size();i++) huntAct(game,"p"+i,sheriff,"player:p"+((i+1)%players.size()));
        check(game.get("phase").equals("NIGHT") && seats.stream().allMatch(p->Boolean.TRUE.equals(p.get("alive"))),"tied exile vote eliminates nobody");
        for(Map<String,Object> p:players) {
            String self=(String)p.get("id");
            huntAct(game,self,sheriff,witches.contains(self) || self.equals(sheriff) ? "player:"+sheriff : "skip");
        }
        check(seats.stream().allMatch(p->Boolean.TRUE.equals(p.get("alive"))),"simplified sheriff can successfully self protect");
        huntAct(game,sheriff,sheriff,"vote");
        rejected(()->huntAct(game,sheriff,sheriff,"player:"+sheriff),game,"self voting");
        for(Map<String,Object> p:players) {
            String self=(String)p.get("id"); huntAct(game,self,sheriff,self.equals(sheriff) ? "skip" : "player:"+sheriff);
        }
        check(rows(HuntGame.view(game,sheriff,true),"options").isEmpty(),"dead sheriff has no night action");
        rejected(()->huntAct(game,sheriff,sheriff,"player:"+villagers.get(0)),game,"dead sheriff guarding");
        for(String victim:villagers) {
            for(Map<String,Object> p:seats) if(Boolean.TRUE.equals(p.get("alive"))) {
                String self=(String)p.get("id"); huntAct(game,self,sheriff,witches.contains(self) ? "player:"+victim : "skip");
            }
            if(game.get("phase").equals("RESULT")) break;
            check(game.get("phase").equals("DAY"),"dead sheriff does not block night completion");
            huntAct(game,sheriff,sheriff,"vote");
            check(rows(HuntGame.view(game,sheriff,true),"options").isEmpty(),"dead host starts vote but cannot cast a ballot");
            for(Map<String,Object> p:seats) if(Boolean.TRUE.equals(p.get("alive"))) huntAct(game,(String)p.get("id"),sheriff,"skip");
        }
        check(game.get("winner").equals("女巫阵营胜利"),"witches win immediately after the final nonwitch dies at night");
    }
    private static void huntAct(Map<String,Object> game,String self,String host,String choice) {
        String step=(String)HuntGame.view(game,self,self.equals(host)).get("stepId");
        HuntGame.act(game,self,self.equals(host),step,choice);
    }
    private static void rules() {
        List<Map<String,Object>> players = List.of(map("id","a","nickname","甲"),map("id","b","nickname","乙"),map("id","c","nickname","丙"));
        for(String preset:List.of("electronic","property","score","chips")) {
            Map<String,Object> g = LedgerGame.start(players,LedgerGame.config(map("ledgerPreset",preset,"initialBalance",1000L)));
            Map<String,Object> transfer = command(0,"transfer","from","a","to","b","amount",100L);
            LedgerGame.act(g,"a",false,transfer); LedgerGame.act(g,"a",false,transfer);
            check(balance(g,"a")==900 && balance(g,"b")==1100,"idempotent transfer " + preset);
            rejected(()->LedgerGame.act(g,"a",false,command(1,"transfer","from","b","to","a","amount",1L)),g,"other account payment");
            rejected(()->LedgerGame.act(g,"a",false,command(1,"transfer","from","a","to","a","amount",1L)),g,"same account");
            rejected(()->LedgerGame.act(g,"a",false,command(1,"transfer","from","a","to","b","amount",1.5)),g,"fractional amount");
            rejected(()->LedgerGame.act(g,"a",false,command(0,"transfer","from","a","to","b","amount",1L)),g,"stale revision");
            LedgerGame.act(g,"a",true,command(1,"collect","amount",100L,"targets",List.of("a","b","c")));
            check(balance(g,"pot")==300,"public pool collected atomically");
            LedgerGame.act(g,"a",true,command(2,"split","amount",100L,"targets",List.of("c","b","a")));
            check(balance(g,"a")==834 && balance(g,"b")==1033 && balance(g,"c")==933 && balance(g,"pot")==200,"split remainders follow seating order");
            String undo = (String)LedgerGame.view(g,"a").get("undoId");
            LedgerGame.act(g,"a",true,command(3,"undo","entryId",undo));
            check(balance(g,"pot")==300 && rows(g,"history").size()==4 && Boolean.TRUE.equals(rows(g,"history").get(2).get("reversed")),"audited batch undo");
            rejected(()->LedgerGame.act(g,"a",true,command(4,"split","amount",301L,"targets",List.of("a","b"))),g,"insufficient pool");
            rejected(()->LedgerGame.act(g,"a",true,command(4,"grant","amount",1L,"targets",List.of("a","a"))),g,"duplicate targets");
            Map<String,Object> request = command(4,"request","from","b","to","a","amount",50L);
            LedgerGame.act(g,"a",false,request);
            check(rows(LedgerGame.view(g,"c"),"pending").isEmpty(),"private payment requests");
            String requestId = (String)rows(g,"pending").get(0).get("id");
            rejected(()->LedgerGame.act(g,"a",true,command(5,"approve","entryId",requestId)),g,"host cannot approve for payer");
            LedgerGame.act(g,"b",false,command(5,"approve","entryId",requestId));
            check(balance(g,"a")==850 && balance(g,"b")==950 && rows(g,"pending").isEmpty(),"payer approved once");
            if(preset.equals("chips")) rejected(()->LedgerGame.act(g,"a",true,command(6,"collect","amount",951L,"targets",List.of("b","c"))),g,"all or nothing chip collection");
            rejected(()->LedgerGame.act(g,"a",true,command(6,"grant","amount",LedgerGame.MAX,"targets",List.of("a","b"))),g,"balance overflow");
            String before = Json.write(g);
            LedgerGame.act(g,"a",false,transfer);
            check(before.equals(Json.write(g)),"old retry after later changes never mutates balances");
        }
        for(int count=4;count<=12;count++) for(int witches=1;witches<=count-2;witches++) for(int seed=0;seed<8;seed++) {
            List<Map<String,Object>> ps = new ArrayList<>(); for(int i=0;i<count;i++)ps.add(map("id","p"+i,"nickname","玩家"+i));
            Map<String,Object> g = HuntGame.start(ps,witches,new Random(seed));
            int steps=0;
            while(!g.get("phase").equals("RESULT") && steps++<200) {
                for(Map<String,Object> p:ps) {
                    Map<String,Object> view = HuntGame.view(g,(String)p.get("id"),p==ps.get(0));
                    for(Map<String,Object> publicSeat:rows(view,"roster")) if(Boolean.TRUE.equals(publicSeat.get("alive")) && !g.get("phase").equals("RESULT")) check(publicSeat.get("role").equals(""),"living role hidden from public roster");
                    List<Map<String,Object>> options=rows(view,"options"); if(options.isEmpty())continue;
                    String choice=(String)options.get(0).get("id");
                    if(g.get("phase").equals("NIGHT")) choice="skip";
                    if(g.get("phase").equals("VOTE")) {
                        Map<String,Object> witch=rows(g,"seats").stream().filter(s->Boolean.TRUE.equals(s.get("alive")) && s.get("role").equals("WITCH")).findFirst().orElseThrow();
                        String target="player:"+witch.get("id"); choice=options.stream().anyMatch(o->o.get("id").equals(target)) ? target : "skip";
                    }
                    String step=(String)view.get("stepId"), self=(String)p.get("id");
                    HuntGame.act(g,self,p==ps.get(0),step,choice);
                    String snapshot=Json.write(g); HuntGame.act(g,self,p==ps.get(0),step,choice);
                    check(snapshot.equals(Json.write(g)),"hunt retry is idempotent");
                    g = Json.object(Json.parse(Json.write(g))); // persisted round trip after every actor
                }
            }
            check(g.get("winner").equals("村民阵营胜利"),"hunt village win at every supported count/configuration");
        }
    }
    public static void main(String[] args) throws Exception {
        rules();
        huntBoundaries();
        Path dir = Files.createTempDirectory(Path.of("build"),"cloud-expansion-");
        RoomServer server = new RoomServer("127.0.0.1",0,dir,"test-only-token-01234567890123456789");
        try {
            server.start(); base="http://127.0.0.1:"+server.port()+"/api/v1/rooms";
            Response host = call("","",entry("ledger","房主",20)); check(host.status==200,"ledger create");
            String token=(String)host.body.get("token"), id=(String)Json.object(host.body.get("room")).get("selfId"), path="/"+Json.object(host.body.get("room")).get("roomId");
            Response guest=call(path+"/join","",entry("ledger","来宾",20)); String otherToken=(String)guest.body.get("token"), other=(String)Json.object(guest.body.get("room")).get("selfId");
            check(call(path+"/start",token,map("roundNumber",0)).status==409,"unready ledger cannot start");
            call(path+"/ready",otherToken,map("ready",true));
            Map<String,Object> room=call(path+"/start",token,map("roundNumber",0)).body;
            check(ledger(room).get("history") instanceof List && room.get("identity")==null,"ledger has no secret role");
            String round=(String)room.get("roundId");
            Map<String,Object> command=command(0,"transfer","from",other,"to",id,"amount",100L,"roundId",round);
            Response pay=call(path+"/ledger",otherToken,command); check(pay.status==200,"guest self payment");
            check(balance(ledger(pay.body),id)==1100,"payment visible to clients");
            server.close(); server=new RoomServer("127.0.0.1",0,dir,"test-only-token-01234567890123456789"); server.start(); base="http://127.0.0.1:"+server.port()+"/api/v1/rooms";
            check(call(path+"/ledger",otherToken,command).status==200,"retry after server restart accepted");
            check(balance(ledger(call(path,token,null).body),id)==1100,"restart never duplicates debit");
            Map<String,Object> a=command(1,"transfer","from",other,"to",id,"amount",1L,"roundId",round);
            Map<String,Object> b=command(1,"transfer","from",id,"to",other,"amount",1L,"roundId",round);
            CompletableFuture<Response> first=CompletableFuture.supplyAsync(()-> {try{return call(path+"/ledger",otherToken,a);}catch(Exception e){throw new CompletionException(e);}});
            Response second=call(path+"/ledger",token,b);
            check((first.get().status==200 ? 1 : 0)+(second.status==200 ? 1 : 0)==1,"concurrent revisions commit exactly once");
            check(call(path+"/ledger",otherToken,command(2,"transfer","from",id,"to",other,"amount",1L,"roundId",round)).status==409,"HTTP guest cannot debit host");
            check(call(path+"/finish",otherToken,map("roundId",round)).status==403,"guest cannot end ledger");
            check(call(path+"/finish",token,map("roundId",round)).status==200,"host closes ledger");
            check(call(path+"/ledger",token,command(2,"transfer","from",id,"to",other,"amount",1L,"roundId",round)).status==409,"finished ledger locked");
            check(call(path+"/next",token,map("roundId",round)).body.get("ledger")==null,"new round clears old ledger");
            Response hunt=call("","",entry("hunt","主持",4)); check(hunt.status==200,"hunt create");
            String huntPath="/"+Json.object(hunt.body.get("room")).get("roomId"), huntHost=(String)hunt.body.get("token");
            List<String> tokens=new ArrayList<>(List.of(huntHost));
            for(int i=1;i<4;i++) tokens.add((String)call(huntPath+"/join","",entry("hunt","猎巫"+i,4)).body.get("token"));
            for(String t:tokens)call(huntPath+"/ready",t,map("ready",true));
            room=call(huntPath+"/start",huntHost,map("roundNumber",0)).body; String huntRound=(String)room.get("roundId");
            for(String t:tokens) {
                room=call(huntPath,t,null).body;
                check(room.get("identity")!=null && !Json.write(room.get("players")).contains("role"),"hunt deals only own identity");
                Map<String,Object> game=Json.object(room.get("game"));
                check(call(huntPath+"/game",t,map("roundId",huntRound,"stepId",game.get("stepId"),"choice","confirm")).status==200,"hunt private confirmation");
            }
            check(call(huntPath+"/finish",huntHost,map("roundId",huntRound,"winner","平民")).status==409,"hunt result is authoritative");
            call(huntPath+"/leave",tokens.get(1),map());
            room=call(huntPath,huntHost,null).body;
            check(room.get("status").equals("FINISHED") && Json.object(room.get("game")).get("phase").equals("ABORTED"),"hunt leave cannot deadlock round");
            System.out.println("PASS: " + checks + " cloud expansion checks; ledger permissions, atomic batches, approvals, concurrent HTTP, restart, and 432 complete hunt games");
        } finally { server.close(); }
    }
}

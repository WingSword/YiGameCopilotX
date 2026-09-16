package org.walks.rooms;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import static org.walks.rooms.RoomServer.map;

/** Secret ballots, stage boundaries, reconnects and browser transport use production engines. */
public final class CloudSixTest {
    private static int checks, games;
    private static final Set<String> EVIL=Set.of("ASSASSIN","MORGANA","MORDRED","OBERON","MINION");
    private static void check(boolean value,String reason) { checks++; if(!value) throw new AssertionError(reason); }
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> rows(Map<String,Object> g,String key) { return (List<Map<String,Object>>)g.get(key); }
    private static String str(Map<String,Object> g,String key) { return (String)g.get(key); }
    private static int num(Map<String,Object> g,String key) { return ((Number)g.get(key)).intValue(); }
    private static Map<String,Object> copy(Map<String,Object> g) { return Json.object(Json.parse(Json.write(g))); }
    private static List<Map<String,Object>> players(int n) { List<Map<String,Object>> p=new ArrayList<>(); for(int i=0;i<n;i++) p.add(map("id","p"+i,"nickname","玩家"+i)); return p; }
    private static void rejected(Map<String,Object> g,Runnable action,String reason) {
        String before=Json.write(g);
        try { action.run(); throw new AssertionError("Accepted: "+reason); } catch(IllegalArgumentException expected) { checks++; }
        check(before.equals(Json.write(g)),"Invalid command mutated state: "+reason);
    }
    private static void avalonAct(Map<String,Object> g,String id,String choice,Object targets) {
        String step=str(AvalonGame.view(g,id),"stepId");
        AvalonGame.act(g,id,step,choice,targets);
        String after=Json.write(g);
        AvalonGame.act(g,id,step,choice,targets);
        check(after.equals(Json.write(g)),"Avalon duplicate action after stage transition");
        rejected(g,()->AvalonGame.act(g,id,step,"tampered",null),"altered submitted ballot");
    }
    private static void avalon() {
        for(int n=5;n<=10;n++) for(int seed=0;seed<8;seed++) for(int scenario=0;scenario<6;scenario++) {
            Map<String,Object> g=AvalonGame.start(players(n),List.of(),new Random(seed));
            check(rows(g,"seats").stream().filter(p->EVIL.contains(p.get("role"))).count()==(n<=6?2:n<=9?3:4),"standard good/evil ratio");
            for(Map<String,Object> p:rows(g,"seats")) {
                String self=str(p,"id"), role=str(p,"role");
                String notes=Json.write(AvalonGame.view(g,self).get("notes"));
                for(Map<String,Object> other:rows(g,"seats")) {
                    boolean visible=role.equals("MERLIN") ? EVIL.contains(other.get("role")) && !other.get("role").equals("MORDRED") :
                        role.equals("PERCIVAL") ? List.of("MERLIN","MORGANA").contains(other.get("role")) : EVIL.contains(role) && !role.equals("OBERON") && other!=p && EVIL.contains(other.get("role")) && !other.get("role").equals("OBERON");
                    check(notes.contains(str(other,"nickname"))==visible,"role-specific initial knowledge "+role);
                }
            }
            for(int i=0;i<n;i++) avalonAct(g,"p"+i,"confirm",null);
            int loops=0;
            while(!str(g,"phase").equals("RESULT")) {
                check(++loops<40,"Avalon always finishes");
                g=copy(g); // Every phase must survive JSON persistence.
                final Map<String,Object> current=g;
                String phase=str(g,"phase");
                Map<String,Object> a=Json.object(AvalonGame.view(g,"p0").get("avalon"));
                check(rows(AvalonGame.view(g,"p0"),"revealed").isEmpty(),"identities remain hidden before assassination");
                int quest=num(g,"quest"),fails=(scenario==2 ? 2 : scenario>=4 ? quest==1||quest==3||quest==5 ? 1 : quest==4 ? scenario==4?1:2 : 0 : 0);
                if(phase.equals("TEAM")) {
                    int size=num(a,"teamSize");
                    check(size==new int[][]{{2,3,2,3,3},{2,3,4,3,4},{2,3,3,4,4},{3,4,4,5,5}}[Math.min(n-5,3)][quest-1],"standard team size");
                    check(num(a,"failsNeeded")== (n>=7&&quest==4?2:1),"only fourth quest has two-fail threshold");
                    List<String> team=new ArrayList<>();
                    rows(g,"seats").stream().filter(p->EVIL.contains(p.get("role"))).limit(Math.max(1,fails)).forEach(p->team.add(str(p,"id")));
                    rows(g,"seats").stream().filter(p->!team.contains(p.get("id"))).limit(size-team.size()).forEach(p->team.add(str(p,"id")));
                    String leader=str(a,"leaderId"), step=str(AvalonGame.view(g,leader),"stepId");
                    rejected(g,()->AvalonGame.act(current,leader,step,"team",List.of(leader,leader)),"duplicate/incorrect team size");
                    avalonAct(g,leader,"team",team);
                } else if(phase.equals("TEAM_VOTE")) {
                    String log=Json.write(g.get("log")); int leader=num(g,"leader");
                    for(int i=0;i<n;i++) {
                        avalonAct(g,"p"+i,scenario==3 ? "reject" : "approve",null);
                        if(i<n-1) check(log.equals(Json.write(AvalonGame.view(g,"p"+(n-1)).get("publicLog"))),"ballots remain secret until everyone votes");
                    }
                    if(scenario==3 && !str(g,"phase").equals("RESULT")) check(num(g,"leader")== (leader+1)%n,"leader rotates clockwise after rejection");
                } else if(phase.equals("QUEST")) {
                    @SuppressWarnings("unchecked") List<String> team=(List<String>)g.get("team");
                    String outsider=rows(g,"seats").stream().map(p->str(p,"id")).filter(id->!team.contains(id)).findFirst().orElseThrow();
                    rejected(g,()->AvalonGame.act(current,outsider,str(AvalonGame.view(current,outsider),"stepId"),"success",null),"non-team quest vote");
                    int left=fails,leader=num(g,"leader"), oldMissions=rows(g,"missions").size();
                    for(String id:List.copyOf(team)) {
                        boolean evil=EVIL.contains(rows(g,"seats").stream().filter(p->id.equals(p.get("id"))).findFirst().orElseThrow().get("role"));
                        if(!evil) rejected(g,()->AvalonGame.act(current,id,str(AvalonGame.view(current,id),"stepId"),"fail",null),"good cannot sabotage");
                        boolean fail=evil && left>0; if(fail) left--;
                        avalonAct(g,id,fail?"fail":"success",null);
                    }
                    check(rows(g,"missions").size()==oldMissions+1,"one mission per completed ballot");
                    Map<String,Object> result=rows(g,"missions").get(oldMissions);
                    check(Boolean.TRUE.equals(result.get("success"))==(fails<(n>=7&&quest==4?2:1)),"quest failure threshold result");
                    check(!Json.write(AvalonGame.view(g,"p0")).contains("\"choice\""),"individual task cards never serialized");
                    if(str(g,"phase").equals("TEAM")) check(num(g,"leader")== (leader+1)%n,"leader rotates after quest");
                } else if(phase.equals("ASSASSINATE")) {
                    String assassin=str(rows(g,"seats").stream().filter(p->p.get("role").equals("ASSASSIN")).findFirst().orElseThrow(),"id");
                    boolean hit=scenario==1;
                    String target=str(rows(g,"seats").stream().filter(p->hit ? p.get("role").equals("MERLIN") : p.get("role").equals("LOYAL")).findFirst().orElseThrow(),"id");
                    check(rows(AvalonGame.view(g,assassin),"options").size()==n-1,"assassin targets do not reveal alignment");
                    avalonAct(g,assassin,"player:"+target,null);
                } else throw new AssertionError(phase);
            }
            check(rows(AvalonGame.view(g,"p0"),"revealed").size()==n,"final identities revealed");
            check(str(g,"winner").contains(scenario==0 ? "善良" : "邪恶"),"expected winner scenario "+scenario);
            games++;
        }
        // Even-player tie is a rejection, not a majority.
        Map<String,Object> tie=AvalonGame.start(players(6),List.of(),new Random(1));
        for(int i=0;i<6;i++) avalonAct(tie,"p"+i,"confirm",null);
        String leader=str(Json.object(AvalonGame.view(tie,"p0").get("avalon")),"leaderId");
        avalonAct(tie,leader,"team",List.of("p0","p1"));
        for(int i=0;i<6;i++) avalonAct(tie,"p"+i,i<3 ? "approve" : "reject",null);
        check(num(tie,"rejections")==1 && str(tie,"phase").equals("TEAM"),"3:3 ballot rejected");
    }
    private static Map<String,Object> drawCommand(Map<String,Object> g,String op,Object... values) {
        Map<String,Object> c=map("requestId",UUID.randomUUID().toString(),"stepId","draw:"+g.get("step"),"revision",g.get("revision"),"operation",op); c.putAll(map(values)); return c;
    }
    private static void drawAct(Map<String,Object> g,String self,boolean host,Map<String,Object> c,long now) {
        DrawingGame.act(g,self,host,c,now); String after=Json.write(g);
        DrawingGame.act(g,self,host,c,now);
        check(after.equals(Json.write(g)),"drawing idempotence including stage changes");
        Map<String,Object> changed=new LinkedHashMap<>(c); changed.put("guess","tampered");
        rejected(g,()->DrawingGame.act(g,self,host,changed,now),"altered drawing receipt");
    }
    private static void drawing() {
        for(int n=3;n<=10;n++) for(int seed=0;seed<5;seed++) {
            Map<String,Object> g=DrawingGame.start(players(n),new Random(seed)); long now=100_000;
            for(int turn=0;turn<n;turn++) {
                g=copy(g); final Map<String,Object> current=g;
                String painter="p"+turn,guessing="p"+((turn+1)%n);
                String word=str(DrawingGame.view(g,painter,now),"word");
                check(!word.isEmpty() && str(DrawingGame.view(g,guessing,now),"word").isEmpty(),"only painter sees word");
                rejected(g,()->DrawingGame.act(current,guessing,false,drawCommand(current,"start"),100_000),"only painter can start");
                drawAct(g,painter,turn==0,drawCommand(g,"start"),now);
                Map<String,Object> stroke=drawCommand(g,"stroke","color","#111111","width",8,"points",List.of(List.of(0,0),List.of(1000,1000)));
                drawAct(g,painter,turn==0,stroke,now);
                check(rows(DrawingGame.view(g,guessing,now),"strokes").size()==1,"shared board available to guessers");
                rejected(g,()->DrawingGame.act(current,guessing,false,drawCommand(current,"clear"),100_000),"guesser cannot clear board");
                rejected(g,()->DrawingGame.act(current,painter,true,drawCommand(current,"stroke","color","#111111","width",8,"points",List.of(List.of(-1,100))),100_000),"out of range coordinates");
                drawAct(g,painter,turn==0,drawCommand(g,"undo"),now);
                check(rows(g,"strokes").isEmpty(),"undo removes exactly one stroke");
                drawAct(g,painter,turn==0,drawCommand(g,"stroke","color","#E53935","width",18,"points",List.of(List.of(500,500))),now);
                drawAct(g,painter,turn==0,drawCommand(g,"clear"),now);
                check(rows(g,"strokes").isEmpty(),"clear synchronized");
                if(turn%3==0) {
                    for(int i=0;i<n;i++) if(i!=turn) {
                        now+=700; drawAct(g,"p"+i,i==0,drawCommand(g,"guess","guess",word),now);
                        check(str(DrawingGame.view(g,"p"+i,now),"feedback").contains("10"),"correct score feedback");
                    }
                    check(str(g,"phase").equals("TURN_RESULT"),"all guesses immediately end turn");
                } else if(turn%3==1) {
                    drawAct(g,guessing,false,drawCommand(g,"guess","guess","错误答案"),now+700);
                    check(!Boolean.TRUE.equals(rows(g,"seats").get((turn+1)%n).get("guessed")),"wrong guess awards no points");
                    check(!DrawingGame.timeout(g,now+89_999),"timeout not early");
                    check(DrawingGame.timeout(g,now+90_000),"deadline transitions without clients");
                    check(!DrawingGame.timeout(g,now+90_001),"timeout transition only once");
                } else drawAct(g,"p0",true,drawCommand(g,"end"),now);
                check(str(DrawingGame.view(g,guessing,now),"word").equals(word),"answer revealed after turn");
                drawAct(g,"p0",true,drawCommand(g,"next"),now);
                now+=100_000;
            }
            check(str(g,"phase").equals("RESULT") && !str(g,"winner").isEmpty(),"complete rotation final result"); games++;
        }
    }
    private static final HttpClient HTTP=HttpClient.newHttpClient();
    private static String base;
    private static HttpResponse<String> request(String method,String path,String token,Object body,String origin) throws Exception {
        HttpRequest.Builder b=HttpRequest.newBuilder(URI.create(base+path));
        if(!token.isEmpty()) b.header("Authorization","Bearer "+token);
        if(origin!=null) b.header("Origin",origin);
        if(body!=null) b.header("Content-Type","application/json");
        b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(Json.write(body)));
        return HTTP.send(b.build(),HttpResponse.BodyHandlers.ofString());
    }
    private static Map<String,Object> ok(String path,String token,Object body) throws Exception {
        HttpResponse<String> r=request(body==null?"GET":"POST",path,token,body,"http://localhost:8090");
        check(r.statusCode()==200,"HTTP "+path+" "+r.body());
        check(r.headers().firstValue("Access-Control-Allow-Origin").orElse("").equals("http://localhost:8090"),"CORS actual response");
        return Json.object(Json.parse(r.body()));
    }
    private static void http() throws Exception {
        for(String type:List.of("avalon","drawing")) {
            Path dir=Files.createTempDirectory("six-cloud-"); int n=type.equals("avalon")?5:3;
            RoomServer server=new RoomServer("127.0.0.1",0,dir,"a".repeat(40),Set.of("http://localhost:8090")); server.start(); base="http://127.0.0.1:"+server.port();
            try {
                HttpResponse<String> pre=request("OPTIONS","/api/v1/rooms","",null,"http://localhost:8090");
                check(pre.statusCode()==204 && pre.headers().firstValue("Access-Control-Allow-Headers").orElse("").contains("Authorization"),"browser authorization preflight");
                check(request("OPTIONS","/api/v1/rooms","",null,"https://untrusted.test").statusCode()==403,"unknown origin blocked");
                check(request("OPTIONS","/api/v1/admin/stats","",null,"http://localhost:8090").statusCode()==403,"admin never exposed via CORS");
                List<String> tokens=new ArrayList<>();
                Map<String,Object> entry=ok("/api/v1/rooms","",map("nickname","浏览器","roomKey","Secret88","requestId",UUID.randomUUID().toString(),"gameType",type,"maxPlayers",n));
                tokens.add(str(entry,"token")); Map<String,Object> room=Json.object(entry.get("room")); String path="/api/v1/rooms/"+room.get("roomId");
                for(int i=1;i<n;i++) tokens.add(str(ok(path+"/join","",map("nickname","手机"+i,"roomKey","Secret88","requestId",UUID.randomUUID().toString())),"token"));
                for(String token:tokens) ok(path+"/ready",token,map("ready",true));
                room=ok(path+"/start",tokens.get(0),map("roundNumber",0)); String round=str(room,"roundId"), self=str(room,"selfId");
                check(request("GET",path,"invalid",null,null).statusCode()==401,"untrusted browser identifier is not authentication");
                check(request("POST",path+"/finish",tokens.get(0),map("roundId",round,"winner","造假"),null).statusCode()==409,"host cannot invent game result");
                if(type.equals("drawing")) {
                    Map<String,Object> d=Json.object(room.get("drawing"));
                    ok(path+"/draw",tokens.get(0),map("roundId",round,"stepId",d.get("stepId"),"requestId",UUID.randomUUID().toString(),"operation","start"));
                } else {
                    for(String token:tokens) {
                        Map<String,Object> r=ok(path,token,null),g=Json.object(r.get("game"));
                        ok(path+"/game",token,map("roundId",round,"stepId",g.get("stepId"),"choice","confirm"));
                    }
                }
                server.close();
                if(type.equals("drawing")) {
                    Path file=dir.resolve("rooms.json"); Map<String,Object> state=Json.object(Json.parse(Files.readString(file)));
                    Map<String,Object> saved=Json.object(Json.object(state.get("rooms")).values().iterator().next());
                    Json.object(saved.get("game")).put("deadline",System.currentTimeMillis()-1); Files.writeString(file,Json.write(state));
                }
                server=new RoomServer("127.0.0.1",0,dir,"a".repeat(40),Set.of("http://localhost:8090")); server.start(); base="http://127.0.0.1:"+server.port();
                if(type.equals("drawing")) Thread.sleep(1200);
                room=ok(path,tokens.get(0),null);
                check(str(room,"selfId").equals(self),"same membership after service restart/browser reload");
                check(str(Json.object(room.get(type.equals("drawing")?"drawing":"game")),"phase").equals(type.equals("drawing")?"TURN_RESULT":"TEAM"),"restart resumes authoritative phase");
                ok(path+"/leave",tokens.get(1),map()); room=ok(path,tokens.get(0),null);
                check(str(room,"status").equals("FINISHED"),"leaving cannot strand game");
                check(str(Json.object(room.get(type.equals("drawing")?"drawing":"game")),"phase").equals("ABORTED"),"leave aborts incomplete game");
                room=ok(path+"/next",tokens.get(0),map("roundId",round));
                check(str(room,"status").equals("WAITING") && room.get("game")==null && room.get("drawing")==null,"next game clears private state and board");
            } finally { server.close(); }
        }
    }
    public static void main(String[] args) throws Exception {
        avalon(); drawing(); http();
        System.out.println("PASS: "+checks+" six-game checks; "+games+" complete Avalon/drawing games, role knowledge, ballots, retries, HTTP, CORS and restart");
    }
}

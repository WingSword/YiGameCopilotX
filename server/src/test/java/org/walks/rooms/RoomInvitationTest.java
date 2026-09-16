package org.walks.rooms;

import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import static org.walks.rooms.RoomServer.map;

/** Real HTTP checks for join-only invitations and the participating host's public view mode. */
public final class RoomInvitationTest {
    private static final HttpClient HTTP=HttpClient.newHttpClient();
    private static String base;
    private static int checks;
    private static void check(boolean ok,String reason) { checks++; if(!ok) throw new AssertionError(reason); }
    private static Map<String,Object> request(String path,String token,Map<String,Object> body,int status) throws Exception {
        HttpRequest.Builder b=HttpRequest.newBuilder(URI.create(base+"/api/v1/rooms"+path)).header("Content-Type","application/json");
        if(!token.isEmpty()) b.header("Authorization","Bearer "+token);
        if(body==null) b.GET(); else b.POST(HttpRequest.BodyPublishers.ofString(Json.write(body)));
        HttpResponse<String> result=HTTP.send(b.build(),HttpResponse.BodyHandlers.ofString());
        check(result.statusCode()==status,path+" expected "+status+" got "+result.statusCode()+": "+result.body());
        return Json.object(Json.parse(result.body()));
    }
    private static Map<String,Object> room(Map<String,Object> entry) { return Json.object(entry.get("room")); }
    private static String str(Map<String,Object> m,String key) { return Objects.toString(m.get(key),""); }
    private static Map<String,Object> create(String kind,int n) throws Exception {
        return request("","",map("nickname","房主","roomKey","Secret88","requestId",UUID.randomUUID().toString(),"gameType",kind,"maxPlayers",n),200);
    }
    private static Map<String,Object> mode(String path,String token,Map<String,Object> r,String mode,int status) throws Exception {
        return request(path+"/hostview",token,map("roundId",r.get("roundId"),"revision",r.get("hostViewRevision"),"hostView",mode),status);
    }
    private static Map<String,String> fragment(String url) {
        Map<String,String> parts=new LinkedHashMap<>();
        for(String p:URI.create(url).getRawFragment().split("&")) { String[] kv=p.split("=",2); parts.put(kv[0],URLDecoder.decode(kv[1],StandardCharsets.UTF_8)); }
        return parts;
    }
    private static void invalidAddress(String value,boolean origin) {
        try { RoomInvitation.address(value,origin); throw new AssertionError("Accepted "+value); } catch(IllegalArgumentException expected) { checks++; }
    }
    public static void main(String[] args) throws Exception {
        for(String url:List.of("javascript:alert(1)","https://user:pass@example.com/","https://example.com/#secret","https://example.com/?key=x","https://exa mple.com","ftp://example.com","http://")) invalidAddress(url,false);
        invalidAddress("https://example.com/api",true);
        check(RoomInvitation.origin("https://example.com:8443/play/").equals("https://example.com:8443"),"configured web origin proxies API");
        try { RoomInvitation.view("https://example.com","http://example.com","123456","a".repeat(32)); throw new AssertionError("mixed content accepted"); } catch(IllegalArgumentException expected) { checks++; }
        Path dir=Files.createTempDirectory("room-invitations-");
        try {
            RoomServer server=new RoomServer("127.0.0.1",0,dir,"a".repeat(40)); server.start(); base="http://127.0.0.1:"+server.port();
            try {
                Map<String,Object> owner=create("ledger",2), r=room(owner); String token=str(owner,"token"), path="/"+r.get("roomId");
                check(r.get("hostView").equals("admin"),"new host opens management view");
                request(path+"/invite",token,map("serverUrl",base),409);
                Map<String,Object> qr=request(path+"/invite",token,map("webUrl","http://localhost:8090/play/","serverUrl",base),200);
                String url=str(qr,"url"); Map<String,String> invite=fragment(url);
                check(invite.keySet().equals(Set.of("room","invite","server")),"only join fields in URL");
                check(!url.contains(token) && !url.contains("Secret88"),"no identity/member/room-key credential in QR");
                check(invite.get("invite").matches("[A-Za-z0-9_-]{32}"),"join token entropy/encoding");
                List<?> modules=(List<?>)qr.get("modules");
                check(modules.size()>=21 && modules.size()%4==1 && modules.stream().allMatch(row->row.toString().matches("[01]{"+modules.size()+"}")),"complete QR matrix");
                check(request(path+"/invite",token,map("webUrl","http://localhost:8090/play/","serverUrl",base),200).equals(qr),"stable repeated invitation");
                check(!Json.write(request(path,token,null,200)).contains(invite.get("invite")),"invite absent from public room snapshot");
                request(path+"/join","",map("nickname","伪造","roomKey","Secret88","inviteToken","x".repeat(32),"requestId",UUID.randomUUID().toString()),403);
                Map<String,Object> join=map("nickname","扫码玩家","roomKey","","inviteToken",invite.get("invite"),"requestId",UUID.randomUUID().toString());
                Map<String,Object> guest=request(path+"/join","",join,200); String guestToken=str(guest,"token");
                check(str(guest,"roomKey").isEmpty(),"join-only client has no room password");
                check(request(path+"/join","",join,200).get("token").equals(guestToken),"retry full-room join returns same membership");
                request(path+"/invite",guestToken,map("webUrl","http://localhost:8090","serverUrl",base),403);
                mode(path,guestToken,r,"player",403);
                Map<String,Object> player=mode(path,token,r,"player",200);
                check(((List<?>)player.get("players")).size()==2,"switch leaves guest and host seats intact");
                Map<String,Object> admin=mode(path,token,player,"admin",200);
                check(((List<?>)admin.get("hostViewChanges")).size()==2,"both rapid switches available between polls");
                check(mode(path,token,r,"admin",200).get("hostViewRevision").equals(admin.get("hostViewRevision")),"duplicate target mode is idempotent");
                mode(path,token,r,"player",409);
                check(request(path,guestToken,null,200).get("hostViewChanges").equals(admin.get("hostViewChanges")),"all members see same reminders");
                server.close(); server=new RoomServer("127.0.0.1",0,dir,"a".repeat(40)); server.start(); base="http://127.0.0.1:"+server.port();
                Map<String,Object> restored=request(path,token,null,200);
                check(restored.get("hostViewChanges").equals(admin.get("hostViewChanges")),"mode and reminders survive restart");
                check(fragment(str(request(path+"/invite",token,map("webUrl","http://localhost:8090/play/","serverUrl",base),200),"url")).get("invite").equals(invite.get("invite")),"invitation survives restart");
                request(path+"/ready",guestToken,map("ready",true),200);
                r=request(path+"/start",token,map("roundNumber",0),200);
                check(r.get("hostView").equals("player"),"start opens participant interface by default");
                mode(path,token,restored,"admin",409);
                request(path+"/invite",token,map("webUrl","http://localhost:8090","serverUrl",base),409);
                request(path+"/join","",map("nickname","开局加入","inviteToken",invite.get("invite"),"requestId",UUID.randomUUID().toString()),409);
                for(int i=0;i<24;i++) r=mode(path,token,r,i%2==0?"admin":"player",200);
                check(((List<?>)r.get("hostViewChanges")).size()==20,"bounded reminder history");
                String ledger=Json.write(r.get("ledger"));
                r=mode(path,token,r,"admin",200); check(ledger.equals(Json.write(r.get("ledger"))),"switch never changes ledger revision/balances");
                request(path+"/leave",token,map(),200);
                r=request(path,guestToken,null,200); check(r.get("hostId").equals(r.get("selfId")),"host transfer retains participating member");
                check(r.get("hostView").equals("player"),"transferred finished host defaults to player");
            } finally { server.close(); }
            try(RoomServer deployed=new RoomServer("127.0.0.1",0,dir.resolve("configured"),"a".repeat(40),Set.of(),"https://play.example.com/game/")) {
                deployed.start(); base="http://127.0.0.1:"+deployed.port();
                for(String kind:List.of("spy","werewolf","hunt","ledger","avalon","drawing")) {
                    int n=kind.equals("avalon")?5:kind.equals("werewolf")||kind.equals("drawing")?3:4;
                    Map<String,Object> e=create(kind,n), r=room(e); String token=str(e,"token"), path="/"+r.get("roomId");
                    Map<String,Object> qr=request(path+"/invite",token,map("webUrl","http://wrong.example","serverUrl","http://8.133.216.39:8080"),200);
                    String url=str(qr,"url");
                    check(url.startsWith("https://play.example.com/game/#"),"deployed web overrides caller URL");
                    check(fragment(url).get("server").equals("https://play.example.com"),"native default produces HTTPS API invitation");
                    List<String> participants=new ArrayList<>(); participants.add(token);
                    for(int i=1;i<n;i++) {
                        Map<String,Object> g=request(path+"/join","",map("nickname","成员"+i,"inviteToken",fragment(url).get("invite"),"requestId",UUID.randomUUID().toString()),200);
                        participants.add(str(g,"token")); request(path+"/ready",str(g,"token"),map("ready",true),200);
                    }
                    r=request(path+"/start",token,map("roundNumber",0),200);
                    Object identity=r.get("identity"), game=r.get("game"), drawing=r.get("drawing"), players=r.get("players");
                    r=mode(path,token,r,"admin",200); r=mode(path,token,r,"player",200);
                    check(Objects.equals(identity,r.get("identity")) && Objects.equals(game,r.get("game")) && Objects.equals(drawing,r.get("drawing")) && Objects.equals(players,r.get("players")),"mode switch preserves "+kind+" identity/actions/seats");
                    for(String p:participants) check(request(path,p,null,200).get("hostViewChanges").equals(r.get("hostViewChanges")),"mode history broadcast "+kind);
                }
            }
            System.out.println("PASS: "+checks+" room invitation checks; six games, QR matrix, join-only tokens, mode reminders, stale/duplicate requests, host transfer, restart and HTTPS native defaults");
        } finally {
            try(var files=Files.walk(dir)) { for(Path p:files.sorted(Comparator.reverseOrder()).toList()) Files.delete(p); }
        }
    }
}

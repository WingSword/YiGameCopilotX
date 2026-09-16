package org.walks.rooms;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** End-to-end HTTP tests against the actual service, including disk restart and concurrent writes. */
public final class RoomServerTest {
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String ADMIN = "test-only-admin-token-01234567890123456789";
    private static String base;
    private static int checks;
    private record Result(int status, Map<String,Object> body) {}
    private static Result call(String method, String path, String token, Object body) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base+path)).timeout(java.time.Duration.ofSeconds(15));
        if (!token.isEmpty()) b.header("Authorization","Bearer "+token);
        b.header("Content-Type","application/json");
        b.method(method,body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(Json.write(body)));
        HttpResponse<String> r=HTTP.send(b.build(),HttpResponse.BodyHandlers.ofString());
        check("no-store".equals(r.headers().firstValue("Cache-Control").orElse("")),"private response must not be cached");
        return new Result(r.statusCode(),Json.object(Json.parse(r.body())));
    }
    private static Map<String,Object> body(String nickname) { return RoomServer.map("nickname",nickname,"roomKey","Secret88","requestId",UUID.randomUUID().toString(),"gameType","spy","maxPlayers",4,"spyCount",1,"blankCount",0); }
    private static void check(boolean yes,String message) { checks++; if(!yes)throw new AssertionError(message); }
    private static Map<String,Object> room(Result r) { return Json.object(r.body.get("room")); }
    private static String text(Map<String,Object> m,String key) { return (String)m.get(key); }
    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("build"));
        Path dir=Files.createTempDirectory(Path.of("build"),"rooms-test-");
        String code, hostToken, guestToken, guestId, roundId;
        try(RoomServer server=new RoomServer("127.0.0.1",0,dir,ADMIN)) {
            server.start(); base="http://127.0.0.1:"+server.port();
            check(call("GET","/health","",null).status==200,"health");
            Map<String,Object> create=body("Host");
            Result entry=call("POST","/api/v1/rooms","",create);
            check(entry.status==200,"create"); code=text(room(entry),"roomId"); hostToken=text(entry.body,"token");
            check(code.matches("[0-9]{6}"),"server-generated six digit room code");
            Result retry=call("POST","/api/v1/rooms","",create);
            check(hostToken.equals(text(retry.body,"token")),"create retry restores same membership");
            check(room(retry).get("identity")==null,"no identity before start");
            String path="/api/v1/rooms/"+code;
            Map<String,Object> wrong=body("Intruder"); wrong.put("roomKey","Wrong888");
            check(call("POST",path+"/join","",wrong).status==403,"wrong key rejected");
            check(call("GET",path,"",null).status==401,"public room list requires membership");
            Map<String,Object> join=body("Guest1");
            Result guest=call("POST",path+"/join","",join);
            guestToken=text(guest.body,"token"); guestId=text(room(guest),"selfId");
            check(call("POST",path+"/join","",join).body.get("token").equals(guestToken),"join retry never duplicates player");
            check(call("POST",path+"/start",guestToken,RoomServer.map("roundNumber",0,"playerId",text(room(entry),"selfId"))).status==403,"spoofed host id does not grant host rights");
            check(call("DELETE",path,guestToken,null).status==403,"guest cannot dissolve room");
            check(call("POST",path+"/join","",body("Guest1")).status==409,"duplicate nickname rejected");
            List<String> tokens=new ArrayList<>(List.of(hostToken,guestToken));
            for(int i=2;i<=3;i++) tokens.add(text(call("POST",path+"/join","",body("Guest"+i)).body,"token"));
            check(call("POST",path+"/join","",body("Full")).status==409,"full room rejects join");
            check(call("POST",path+"/start",hostToken,RoomServer.map("roundNumber",0)).status==409,"unready players block start");
            for(String token:tokens) check(call("POST",path+"/ready",token,RoomServer.map("ready",true)).status==200,"ready update");
            List<CompletableFuture<Integer>> starts=new ArrayList<>();
            for(int i=0;i<3;i++) starts.add(CompletableFuture.supplyAsync(() -> {
                try{return call("POST",path+"/start",tokens.get(0),RoomServer.map("roundNumber",0)).status;}catch(Exception e){throw new CompletionException(e);}
            }));
            int wins=0; for(var start:starts) if(start.get()==200)wins++;
            check(wins==1,"concurrent start only deals once");
            int spies=0; Set<String> words=new HashSet<>();
            roundId="";
            for(String token:tokens) {
                Result own=call("GET",path,token,null);
                roundId=text(own.body,"roundId");
                Map<String,Object> identity=Json.object(own.body.get("identity"));
                if("卧底".equals(identity.get("role")))spies++;
                words.add(text(identity,"word"));
                check(!Json.write(own.body.get("players")).contains("identity"),"roster cannot leak any identity");
                check(!Json.write(own.body).contains("token") && !Json.write(own.body).contains("roomKey") && !Json.write(own.body).contains("roomHash"),"room view excludes secrets");
            }
            check(spies==1 && words.size()==2,"correct private role and word allocation");
            check(call("POST",path+"/join","",body("Late")).status==409,"no late join after deal");
            check(call("POST",path+"/ready",guestToken,RoomServer.map("ready",true)).status==409,"cannot change readiness mid-round");
            check(call("GET","/api/v1/admin/stats","",null).status==401,"admin auth required");
            check(call("GET","/api/v1/admin/stats",guestToken,null).status==401,"player token is not admin");
            check(call("GET","/api/v1/admin/stats",ADMIN,null).body.get("startedGames").equals(1L),"one started game");
        }
        try(RoomServer server=new RoomServer("127.0.0.1",0,dir,ADMIN)) {
            server.start(); base="http://127.0.0.1:"+server.port(); String path="/api/v1/rooms/"+code;
            Result recovered=call("GET",path,guestToken,null);
            check(roundId.equals(recovered.body.get("roundId")) && recovered.body.get("identity")!=null,"restart restores same private identity and token");
            check(call("POST",path+"/finish",guestToken,RoomServer.map("roundId",roundId,"winner","卧底")).status==403,"only host can finish");
            check(call("POST",path+"/finish",hostToken,RoomServer.map("roundId","old","winner","平民")).status==409,"stale settlement rejected");
            Object finish=RoomServer.map("roundId",roundId,"winner","平民");
            check(call("POST",path+"/finish",hostToken,finish).status==200,"finish");
            check(call("POST",path+"/finish",hostToken,finish).status==200,"settlement retry idempotent");
            check(call("GET","/api/v1/admin/stats",ADMIN,null).body.get("completedGames").equals(1L),"one completed game");
            check(call("POST",path+"/next",hostToken,RoomServer.map("roundId",roundId)).status==200,"next round");
            Result waiting=call("GET",path,guestToken,null);
            check(waiting.body.get("identity")==null && "WAITING".equals(waiting.body.get("status")),"next round clears identity");
            check(call("POST",path+"/start",hostToken,RoomServer.map("roundNumber",0)).status==409,"old start request cannot trigger new round");
            check(call("POST",path+"/leave",hostToken,RoomServer.map()).status==200,"host leaves");
            Result transferred=call("GET",path,guestToken,null);
            check(guestId.equals(transferred.body.get("hostId")),"host ownership transferred");
            check(call("GET",path,hostToken,null).status==401,"departed token revoked");
            // Force a persistence failure: successful responses must imply durable state.
            Files.createDirectory(dir.resolve("rooms.tmp"));
            check(call("POST",path+"/ready",guestToken,RoomServer.map("ready",false)).status==503,"disk failure reported");
            Files.delete(dir.resolve("rooms.tmp"));
            Result rolledBack=call("GET",path,guestToken,null);
            @SuppressWarnings("unchecked") List<Map<String,Object>> roster=(List<Map<String,Object>>)rolledBack.body.get("players");
            check(Boolean.TRUE.equals(roster.get(0).get("ready")),"disk failure rolls back in-memory readiness");
            check(call("DELETE",path,guestToken,null).status==200,"new host can close");
            check(call("GET",path,guestToken,null).status==404,"closed room unavailable");
            check(call("GET","/api/v1/admin/stats",ADMIN,null).body.get("activeRooms").equals(0L),"admin count reflects closure");
            check(!Files.readString(dir.resolve("rooms.json")).contains("Secret88"),"removed room secrets no longer persisted");
            check(call("POST","/api/v1/rooms","",RoomServer.map("nickname","x","requestId",UUID.randomUUID().toString(),"gameType","spy","maxPlayers",-1)).status==400,"invalid limits rejected");
            boolean limited=false;
            for(int i=0;i<35;i++) if(call("POST","/api/v1/rooms","",RoomServer.map()).status==429) {limited=true;break;}
            check(limited,"entry rate limit");
        }
        for(String invalid:List.of("{\"x\":1,\"x\":2}","[1,]","01","true false","{", "1e1000")) {
            boolean rejected=false;try{Json.parse(invalid);}catch(IllegalArgumentException e){rejected=true;}
            check(rejected,"invalid JSON rejected");
        }
        check(Json.parse(Json.write("中文\n🃏")).equals("中文\n🃏"),"Unicode JSON roundtrip");
        System.out.println("PASS: " + checks + " checks; real HTTP, authorization, private roles, concurrency, restart, rollback, rate limits and JSON");
    }
}

package org.walks.rooms;

import java.util.*;
import static org.walks.rooms.RoomServer.map;

/** Single-role house rules used by the local app, not the Salem 1692 card game. */
final class HuntGame {
    static final String RULES = "单身份简化玩法：无审判牌、手牌或阴谋传染。女巫秘密选人，警长守护（可自守）；白天讨论后秘密投票。女巫目标或放逐票并列时无人出局，弃权不计票。女巫全部出局则村民胜，非女巫全部出局则女巫胜。";
    static void config(int count,int witches) {
        if(count<4 || count>12 || witches<1 || witches>count-2) throw new IllegalArgumentException("简化猎巫镇需 4–12 人、至少 1 名女巫、1 名警长和 1 名村民");
    }
    static String name(String role) { return switch(role) { case "WITCH" -> "女巫"; case "SHERIFF" -> "警长"; default -> "村民"; }; }
    static Map<String,Object> start(List<Map<String,Object>> players,int witches,Random random) {
        config(players.size(),witches);
        List<String> roles = new ArrayList<>();
        for(int i=0;i<players.size();i++) roles.add(i<witches ? "WITCH" : i==witches ? "SHERIFF" : "VILLAGER");
        Collections.shuffle(roles,random);
        List<Map<String,Object>> seats = new ArrayList<>();
        for(int i=0;i<players.size();i++) seats.add(map("id",players.get(i).get("id"),"nickname",players.get(i).get("nickname"),"role",roles.get(i),"alive",true,"choice",""));
        return map("phase","DEAL","step",0L,"day",1L,"winner","","seats",seats,"log",new ArrayList<String>(),"receipts",new LinkedHashMap<>());
    }
    static Map<String,Object> view(Map<String,Object> game,String self,boolean host) {
        Map<String,Object> player = seat(game,self);
        String phase = str(game,"phase"), prompt = "", role = str(player,"role");
        List<Object> options = new ArrayList<>();
        boolean awaiting = alive(player) && str(player,"choice").isEmpty();
        switch(phase) {
            case "DEAL" -> { prompt = "查看自己的身份并确认，全部确认后进入夜晚。"; if(awaiting) option(options,"confirm","我记住了，进入夜晚"); }
            case "NIGHT" -> {
                prompt = !alive(player) ? "你已出局，请等待本夜结束。" : !awaiting ? "本夜已提交，等待其余存活玩家。" : role.equals("WITCH") ? "选择本夜目标；女巫票数最多的目标生效，并列则无人被杀。" : role.equals("SHERIFF") ? "选择守护一名存活玩家，本简化玩法允许自守。" : "本夜没有技能，请确认等待。";
                if(awaiting) {
                    if(!role.equals("VILLAGER")) for(Map<String,Object> target:seats(game)) if(alive(target)) option(options,"player:"+str(target,"id"),str(target,"nickname"));
                    option(options,"skip",role.equals("VILLAGER") ? "结束夜间等待" : "不使用技能");
                }
            }
            case "DAY" -> { prompt = "第 " + game.get("day") + " 天，讨论后由房主开启秘密投票。并列或全员弃权则无人出局。"; if(host) option(options,"vote","开始秘密投票"); }
            case "VOTE" -> {
                prompt = !alive(player) ? "你已出局，不能参与投票。" : !awaiting ? "已投票，请等待其余存活玩家。" : "选择要放逐的玩家，不可投给自己；最高票并列则无人出局。";
                if(awaiting) { for(Map<String,Object> target:seats(game)) if(alive(target) && target != player) option(options,"player:"+str(target,"id"),str(target,"nickname")); option(options,"skip","弃权"); }
            }
            case "RESULT" -> prompt = str(game,"winner");
            case "ABORTED" -> prompt = "有玩家离开，本局已中止。";
            default -> throw new IllegalArgumentException("无效的游戏阶段");
        }
        List<String> notes = new ArrayList<>();
        notes.add(RULES);
        if(role.equals("WITCH")) notes.add("女巫同伴：" + String.join("、",seats(game).stream().filter(p->p!=player && str(p,"role").equals("WITCH")).map(p->str(p,"nickname")+(alive(p)?"":"（已出局）")).toList()));
        List<Object> roster = new ArrayList<>();
        boolean ended = phase.equals("RESULT") || phase.equals("ABORTED");
        for(Map<String,Object> p:seats(game)) roster.add(map("id",p.get("id"),"nickname",p.get("nickname"),"alive",p.get("alive"),"role",ended || !alive(p) ? name(str(p,"role")) : ""));
        return map("phase",phase,"stepId",stepId(game),"prompt",prompt,"options",options,"notes",notes,"revealed",List.of(),"center",List.of(),
            "confirmedCount",seats(game).stream().filter(p->!str(p,"choice").isEmpty()).count(),"votedCount",seats(game).stream().filter(p->alive(p) && !str(p,"choice").isEmpty()).count(),
            "livingCount",seats(game).stream().filter(HuntGame::alive).count(),"day",game.get("day"),"roster",roster,"publicLog",game.get("log"));
    }
    static void act(Map<String,Object> game,String self,boolean host,String step,String choice) {
        Map<String,Object> receipts = Json.object(game.get("receipts"));
        String receipt = self+":"+step;
        if(receipts.containsKey(receipt)) { if(!receipts.get(receipt).equals(choice)) throw new IllegalArgumentException("本步骤已提交，不能修改"); return; }
        if(!step.equals(stepId(game))) throw new IllegalArgumentException("阶段已更新，请查看最新提示");
        @SuppressWarnings("unchecked") List<Map<String,Object>> options = (List<Map<String,Object>>)view(game,self,host).get("options");
        if(options.stream().noneMatch(o->choice.equals(o.get("id")))) throw new IllegalArgumentException("当前不能执行该操作");
        Map<String,Object> player = seat(game,self);
        String phase = str(game,"phase");
        if(phase.equals("DAY")) advance(game,"VOTE");
        else {
            player.put("choice",choice);
            if(seats(game).stream().filter(HuntGame::alive).allMatch(p->!str(p,"choice").isEmpty())) {
                if(phase.equals("DEAL")) advance(game,"NIGHT");
                if(phase.equals("NIGHT")) {
                    String target = top(seats(game).stream().filter(p->alive(p) && str(p,"role").equals("WITCH")).toList());
                    String protectedId = seats(game).stream().filter(p->alive(p) && str(p,"role").equals("SHERIFF")).map(p->str(p,"choice")).findFirst().orElse("");
                    if(!target.isEmpty() && !target.equals(protectedId)) eliminate(game,target,"夜晚");
                    else log(game,"第 " + game.get("day") + " 夜：无人出局");
                    if(!finish(game)) advance(game,"DAY");
                }
                if(phase.equals("VOTE")) {
                    String target = top(seats(game).stream().filter(HuntGame::alive).toList());
                    if(!target.isEmpty()) eliminate(game,target,"放逐"); else log(game,"第 " + game.get("day") + " 天：无人被放逐");
                    if(!finish(game)) { game.put("day",((Number)game.get("day")).longValue()+1); advance(game,"NIGHT"); }
                }
            }
        }
        receipts.put(receipt,choice);
        while(receipts.size()>240) receipts.remove(receipts.keySet().iterator().next());
    }
    private static String top(List<Map<String,Object>> voters) {
        Map<String,Integer> counts = new LinkedHashMap<>();
        for(Map<String,Object> p:voters) if(str(p,"choice").startsWith("player:")) counts.merge(str(p,"choice"),1,Integer::sum);
        int max = counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<String> leaders = counts.keySet().stream().filter(k->counts.get(k)==max).toList();
        return leaders.size()==1 ? leaders.get(0) : "";
    }
    private static void eliminate(Map<String,Object> game,String choice,String reason) {
        Map<String,Object> target = seat(game,choice.substring(7)); target.put("alive",false);
        log(game,"第 " + game.get("day") + " 天 · " + reason + "：" + str(target,"nickname") + " 出局（" + name(str(target,"role")) + "）");
    }
    private static boolean finish(Map<String,Object> game) {
        long witches = seats(game).stream().filter(p->alive(p) && str(p,"role").equals("WITCH")).count();
        long others = seats(game).stream().filter(p->alive(p) && !str(p,"role").equals("WITCH")).count();
        if(witches>0 && others>0) return false;
        game.put("winner",witches==0 ? "村民阵营胜利" : "女巫阵营胜利"); advance(game,"RESULT"); return true;
    }
    private static void advance(Map<String,Object> g,String phase) { g.put("phase",phase); g.put("step",((Number)g.get("step")).longValue()+1); seats(g).forEach(p->p.put("choice","")); }
    @SuppressWarnings("unchecked") private static void log(Map<String,Object> g,String message) { List<String> logs=(List<String>)g.get("log"); logs.add(message); if(logs.size()>60)logs.remove(0); }
    private static void option(List<Object> options,String id,String label) { options.add(map("id",id,"label",label)); }
    private static String stepId(Map<String,Object> g) { return "hunt:"+g.get("step"); }
    private static String str(Map<String,Object> g,String key) { return (String)g.get(key); }
    private static boolean alive(Map<String,Object> p) { return Boolean.TRUE.equals(p.get("alive")); }
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> seats(Map<String,Object> g) { return (List<Map<String,Object>>)g.get("seats"); }
    private static Map<String,Object> seat(Map<String,Object> game,String self) { return seats(game).stream().filter(p->self.equals(p.get("id"))).findFirst().orElseThrow(()->new IllegalArgumentException("本局成员不存在")); }
}

package org.walks.rooms;

import java.util.*;
import static org.walks.rooms.RoomServer.map;

/** Base Avalon: clockwise leaders, public team ballots, secret quests and assassination. */
final class AvalonGame {
    private static final Set<String> EVIL = Set.of("ASSASSIN", "MORGANA", "MORDRED", "OBERON", "MINION");
    private static final Set<String> ROLES = Set.of("MERLIN", "PERCIVAL", "LOYAL", "ASSASSIN", "MORGANA", "MORDRED", "OBERON", "MINION");
    private static final int[][] TEAMS = {{2,3,2,3,3}, {2,3,4,3,4}, {2,3,3,4,4}, {3,4,4,5,5}};
    static int evilCount(int count) {
        if(count < 5 || count > 10) throw new IllegalArgumentException("阿瓦隆需要 5–10 人");
        return count <= 6 ? 2 : count <= 9 ? 3 : 4;
    }
    static List<String> deck(Object raw,int count) {
        int evil = evilCount(count);
        List<String> roles = new ArrayList<>();
        if(raw instanceof List<?> input && !input.isEmpty()) {
            for(Object role:input) {
                if(!(role instanceof String s) || !ROLES.contains(s)) throw new IllegalArgumentException("不支持的阿瓦隆角色");
                roles.add(s);
            }
        } else {
            roles.addAll(List.of("MERLIN","PERCIVAL","ASSASSIN","MORGANA"));
            if(evil >= 3) roles.add("MORDRED");
            if(evil == 4) roles.add("OBERON");
            while(roles.size() < count) roles.add("LOYAL");
        }
        if(roles.size()!=count || roles.stream().filter(EVIL::contains).count()!=evil || Collections.frequency(roles,"MERLIN")!=1 || Collections.frequency(roles,"ASSASSIN")!=1)
            throw new IllegalArgumentException("牌组人数、善恶比例或梅林/刺客配置不正确");
        for(String role:ROLES) if(!role.equals("LOYAL") && !role.equals("MINION") && Collections.frequency(roles,role)>1) throw new IllegalArgumentException("特殊角色不能重复");
        return roles;
    }
    static String name(String role) { return switch(role) {
        case "MERLIN" -> "梅林"; case "PERCIVAL" -> "派西维尔"; case "ASSASSIN" -> "刺客";
        case "MORGANA" -> "莫甘娜"; case "MORDRED" -> "莫德雷德"; case "OBERON" -> "奥伯伦";
        case "MINION" -> "爪牙"; default -> "忠臣";
    }; }
    static Map<String,Object> start(List<Map<String,Object>> players,List<String> deck,Random random) {
        List<String> roles = new ArrayList<>(deck(deck,players.size())); Collections.shuffle(roles,random);
        List<Map<String,Object>> seats = new ArrayList<>();
        for(int i=0;i<players.size();i++) seats.add(map("id",players.get(i).get("id"),"nickname",players.get(i).get("nickname"),"role",roles.get(i),"choice",""));
        return map("phase","DEAL","step",0L,"quest",1L,"leader",random.nextInt(players.size()),"rejections",0L,
            "team",new ArrayList<>(),"missions",new ArrayList<>(),"log",new ArrayList<>(),"winner","","seats",seats,"receipts",new LinkedHashMap<>());
    }
    static Map<String,Object> view(Map<String,Object> game,String self) {
        Map<String,Object> player = seat(game,self);
        String phase=str(game,"phase"),role=str(player,"role"),prompt="";
        List<Object> options=new ArrayList<>();
        boolean awaiting=str(player,"choice").isEmpty(), ended=List.of("RESULT","ABORTED").contains(phase);
        switch(phase) {
            case "DEAL" -> { prompt="私下查看身份和初始信息，全部确认后开始组队。"; if(awaiting) option(options,"confirm","我记住了"); }
            case "TEAM" -> { prompt="由队长选择 " + teamSize(game) + " 名玩家出征，队长可选自己。"; if(self.equals(leader(game))) option(options,"team","提交队伍"); }
            case "TEAM_VOTE" -> { prompt="全员秘密表决，超过半数同意才通过；平票为反对，连续 5 次否决则邪恶胜。";
                if(awaiting) { option(options,"approve","同意队伍"); option(options,"reject","反对队伍"); } }
            case "QUEST" -> { prompt="仅出征成员提交任务牌；全部提交后只公布失败牌数量。" + (failsNeeded(game)==2 ? "本轮至少两张失败牌才失败。" : "本轮一张失败牌即失败。");
                if(team(game).contains(self) && awaiting) { option(options,"success","任务成功"); if(EVIL.contains(role)) option(options,"fail","任务失败"); } }
            case "ASSASSINATE" -> { prompt="善良已完成三次任务。邪恶讨论后，由刺客指认梅林；其他身份仍保密。";
                if(role.equals("ASSASSIN")) for(Map<String,Object> target:seats(game)) if(!target.get("id").equals(self)) option(options,"player:"+target.get("id"),str(target,"nickname")); }
            case "RESULT" -> prompt=str(game,"winner");
            case "ABORTED" -> prompt="有玩家离开，本局中止。";
            default -> throw new IllegalArgumentException("无效游戏阶段");
        }
        List<String> notes=new ArrayList<>();
        notes.add(EVIL.contains(role) ? "你的阵营：邪恶。任务可选择成功或失败。" : "你的阵营：善良。任务只能选择成功。");
        if(role.equals("MERLIN")) notes.add("你看到的邪恶玩家（不含莫德雷德）："+names(game,p->EVIL.contains(str(p,"role")) && !str(p,"role").equals("MORDRED")));
        if(role.equals("PERCIVAL")) notes.add("梅林候选（梅林与莫甘娜，无法区分）："+names(game,p->List.of("MERLIN","MORGANA").contains(str(p,"role"))));
        if(EVIL.contains(role) && !role.equals("OBERON")) notes.add("邪恶同伴（不含奥伯伦）："+names(game,p->p!=player && EVIL.contains(str(p,"role")) && !str(p,"role").equals("OBERON")));
        if(role.equals("OBERON")) notes.add("奥伯伦属于邪恶，但不与其他邪恶玩家互认。梅林能看见你。");
        List<Object> revealed=new ArrayList<>();
        if(phase.equals("RESULT")) for(Map<String,Object> p:seats(game)) revealed.add(map("nickname",p.get("nickname"),"initialRole",name(str(p,"role")),"finalRole",name(str(p,"role")),"voteTarget","","eliminated",false));
        return map("phase",phase,"stepId",stepId(game),"prompt",prompt,"options",options,"notes",notes,"revealed",revealed,"center",List.of(),
            "confirmedCount",seats(game).stream().filter(p->!str(p,"choice").isEmpty()).count(),"publicLog",game.get("log"),
            "avalon",map("leaderId",leader(game),"quest",game.get("quest"),"teamSize",ended ? 0 : teamSize(game),"failsNeeded",ended ? 0 : failsNeeded(game),
                "rejections",game.get("rejections"),"team",game.get("team"),"missions",game.get("missions"),"submitted",seats(game).stream().filter(p->!str(p,"choice").isEmpty()).count()));
    }
    static void act(Map<String,Object> game,String self,String step,String choice,Object rawTargets) {
        List<String> targets=new ArrayList<>();
        if(choice.equals("team")) {
            if(!(rawTargets instanceof List<?> list)) throw new IllegalArgumentException("请选择出征成员");
            for(Object value:list) { if(!(value instanceof String id)) throw new IllegalArgumentException("成员无效"); targets.add(id); }
        }
        List<String> canonical=new ArrayList<>(targets); Collections.sort(canonical);
        String signature=choice+Json.write(canonical),key=self+":"+step;
        Map<String,Object> receipts=Json.object(game.get("receipts"));
        if(receipts.containsKey(key)) { if(!receipts.get(key).equals(signature)) throw new IllegalArgumentException("本步骤已提交，不能修改"); return; }
        if(!stepId(game).equals(step)) throw new IllegalArgumentException("阶段已更新，请刷新后操作");
        if(rows(view(game,self),"options").stream().noneMatch(o->choice.equals(o.get("id")))) throw new IllegalArgumentException("当前不能执行此操作");
        String phase=str(game,"phase");
        if(phase.equals("TEAM")) {
            if(targets.size()!=teamSize(game) || new HashSet<>(targets).size()!=targets.size()) throw new IllegalArgumentException("出征人数不正确或有重复成员");
            targets.forEach(id->seat(game,id)); game.put("team",canonical); advance(game,"TEAM_VOTE");
        } else if(phase.equals("ASSASSINATE")) {
            Map<String,Object> target=seat(game,choice.substring(7));
            log(game,"刺客指认："+str(target,"nickname")); finish(game,str(target,"role").equals("MERLIN") ? "邪恶阵营胜利：刺中梅林" : "善良阵营胜利：梅林存活");
        } else {
            seat(game,self).put("choice",choice);
            List<Map<String,Object>> actors=seats(game).stream().filter(p->!phase.equals("QUEST") || team(game).contains(str(p,"id"))).toList();
            if(actors.stream().allMatch(p->!str(p,"choice").isEmpty())) {
                if(phase.equals("DEAL")) advance(game,"TEAM");
                if(phase.equals("TEAM_VOTE")) {
                    long approvals=actors.stream().filter(p->str(p,"choice").equals("approve")).count();
                    log(game,"第 "+game.get("quest")+" 轮组队："+String.join("、",team(game).stream().map(id->str(seat(game,id),"nickname")).toList()));
                    log(game,"表决："+String.join("；",actors.stream().map(p->str(p,"nickname")+(str(p,"choice").equals("approve") ? " 同意" : " 反对")).toList()));
                    if(approvals>actors.size()/2.0) { game.put("rejections",0L); advance(game,"QUEST"); }
                    else {
                        game.put("rejections",number(game,"rejections")+1);
                        if(number(game,"rejections")==5) finish(game,"邪恶阵营胜利：连续五次否决");
                        else { nextLeader(game); game.put("team",new ArrayList<>()); advance(game,"TEAM"); }
                    }
                }
                if(phase.equals("QUEST")) {
                    long fails=actors.stream().filter(p->str(p,"choice").equals("fail")).count();
                    boolean success=fails<failsNeeded(game);
                    rows(game,"missions").add(map("quest",game.get("quest"),"fails",fails,"success",success));
                    log(game,"第 "+game.get("quest")+" 次任务："+(success ? "成功" : "失败")+"，失败牌 "+fails+" 张");
                    long successes=rows(game,"missions").stream().filter(m->Boolean.TRUE.equals(m.get("success"))).count();
                    if(successes==3) advance(game,"ASSASSINATE");
                    else if(rows(game,"missions").size()-successes==3) finish(game,"邪恶阵营胜利：三次任务失败");
                    else { game.put("quest",number(game,"quest")+1); nextLeader(game); game.put("team",new ArrayList<>()); advance(game,"TEAM"); }
                }
            }
        }
        receipts.put(key,signature);
    }
    private static int teamSize(Map<String,Object> game) { return TEAMS[Math.min(seats(game).size()-5,3)][(int)number(game,"quest")-1]; }
    private static int failsNeeded(Map<String,Object> game) { return seats(game).size()>=7 && number(game,"quest")==4 ? 2 : 1; }
    private static String leader(Map<String,Object> game) { return str(seats(game).get((int)number(game,"leader")),"id"); }
    private static void nextLeader(Map<String,Object> g) { g.put("leader",(number(g,"leader")+1)%seats(g).size()); }
    private static void finish(Map<String,Object> g,String winner) { g.put("winner",winner); advance(g,"RESULT"); }
    private static void advance(Map<String,Object> g,String phase) { g.put("phase",phase); g.put("step",number(g,"step")+1); seats(g).forEach(p->p.put("choice","")); }
    private static String names(Map<String,Object> g,java.util.function.Predicate<Map<String,Object>> filter) { return String.join("、",seats(g).stream().filter(filter).map(p->str(p,"nickname")).toList()); }
    @SuppressWarnings("unchecked") private static void log(Map<String,Object> g,String message) { ((List<String>)g.get("log")).add(message); }
    private static void option(List<Object> options,String id,String label) { options.add(map("id",id,"label",label)); }
    private static String stepId(Map<String,Object> g) { return "avalon:"+g.get("step"); }
    private static long number(Map<String,Object> g,String key) { return ((Number)g.get(key)).longValue(); }
    private static String str(Map<String,Object> g,String key) { return (String)g.get(key); }
    @SuppressWarnings("unchecked") private static List<String> team(Map<String,Object> g) { return (List<String>)g.get("team"); }
    private static List<Map<String,Object>> seats(Map<String,Object> g) { return rows(g,"seats"); }
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> rows(Map<String,Object> g,String key) { return (List<Map<String,Object>>)g.get(key); }
    private static Map<String,Object> seat(Map<String,Object> g,String id) { return seats(g).stream().filter(p->id.equals(p.get("id"))).findFirst().orElseThrow(()->new IllegalArgumentException("成员不存在")); }
}

package org.walks.rooms;

import java.util.*;
import static org.walks.rooms.RoomServer.map;

/** Pure, persisted one-night state machine. Only view() may cross the player API boundary. */
final class OneNightGame {
    private static final List<String> ROLES = List.of("WEREWOLF", "MINION", "DOPPELGANGER", "SEER", "ROBBER", "TROUBLEMAKER", "DRUNK", "INSOMNIAC", "HUNTER", "VILLAGER", "MASON_A", "MASON_B", "TANNER");
    private static final List<String> NAMES = List.of("狼人", "爪牙", "化身幽灵", "预言家", "强盗", "捣蛋鬼", "酒鬼", "失眠者", "猎人", "村民", "守夜人", "守夜人", "皮匠");
    static String name(String role) { return NAMES.get(ROLES.indexOf(role)); }
    static List<String> preset(int n) {
        if (n < 3 || n > 10) throw new IllegalArgumentException("玩家人数必须为 3–10 人");
        List<String> result = new ArrayList<>(List.of("WEREWOLF", "SEER", "ROBBER", "VILLAGER", "VILLAGER", "VILLAGER"));
        if (n >= 4) result.add("WEREWOLF");
        if (n >= 5) result.add("TROUBLEMAKER");
        if (n >= 6) result.add("DRUNK");
        if (n >= 7) result.add("INSOMNIAC");
        if (n >= 8) result.add("MINION");
        if (n >= 9) result.add("HUNTER");
        if (n >= 10) result.add("TANNER");
        return result;
    }
    static List<String> deck(Object value, int n) {
        if (value == null) return preset(n);
        if (!(value instanceof List<?> list) || list.size() != n + 3 || n < 3 || n > 10) throw new IllegalArgumentException("角色牌数必须为玩家数加三");
        List<String> roles = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof String r) || !ROLES.contains(r)) throw new IllegalArgumentException("未知角色");
            roles.add(r);
        }
        for (String role : ROLES) {
            int limit = role.equals("WEREWOLF") ? 2 : role.equals("VILLAGER") ? 3 : 1;
            if (Collections.frequency(roles, role) > limit) throw new IllegalArgumentException("重复角色超出牌组数量");
        }
        if (!roles.contains("WEREWOLF") || roles.contains("MASON_A") != roles.contains("MASON_B")) throw new IllegalArgumentException("需要狼人，守夜人必须成对加入");
        return roles;
    }
    static Map<String,Object> start(List<Map<String,Object>> players, List<String> deck, Random random) {
        List<String> cards = new ArrayList<>(deck); Collections.shuffle(cards, random);
        return dealt(players, cards);
    }
    // Package-private deterministic seam for production-rule tests; never exposed as an HTTP input.
    static Map<String,Object> dealt(List<Map<String,Object>> players, List<String> cards) {
        int n = players.size();
        List<Object> seats = new ArrayList<>();
        for (int i = 0; i < n; i++) seats.add(map("id", players.get(i).get("id"), "nickname", players.get(i).get("nickname"), "initial", cards.get(i), "card", cards.get(i), "seen", false, "vote", -1, "notes", new ArrayList<String>()));
        List<Object> queue = new ArrayList<>();
        for (int order = 1; order <= 9; order++) for (int i = 0; i < n; i++) if (order(cards.get(i)) == order) queue.add(map("seat", i, "role", cards.get(i), "copy", false));
        return map("phase", "DEAL", "seats", seats, "center", new ArrayList<>(cards.subList(n, n + 3)), "queue", queue, "step", 0, "copied", "", "receipts", new LinkedHashMap<>(), "winner", "", "eliminated", new ArrayList<Integer>());
    }
    private static int order(String r) { return switch(r) { case "DOPPELGANGER" -> 1; case "WEREWOLF" -> 2; case "MINION" -> 3; case "MASON_A", "MASON_B" -> 4; case "SEER" -> 5; case "ROBBER" -> 6; case "TROUBLEMAKER" -> 7; case "DRUNK" -> 8; case "INSOMNIAC" -> 9; default -> 0; }; }
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> seats(Map<String,Object> g) { return (List<Map<String,Object>>) g.get("seats"); }
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> queue(Map<String,Object> g) { return (List<Map<String,Object>>) g.get("queue"); }
    @SuppressWarnings("unchecked") private static List<String> center(Map<String,Object> g) { return (List<String>) g.get("center"); }
    private static String str(Map<String,Object> m, String k) { return (String) m.get(k); }
    private static int num(Map<String,Object> m, String k) { return ((Number) m.get(k)).intValue(); }
    private static String effective(Map<String,Object> g, String card) { return card.equals("DOPPELGANGER") && !str(g,"copied").isEmpty() ? str(g,"copied") : card; }
    private static String initial(Map<String,Object> g, int i) { return effective(g, str(seats(g).get(i),"initial")); }
    private static String card(Map<String,Object> g, int i) { return str(seats(g).get(i),"card"); }
    private static Map<String,Object> turn(Map<String,Object> g) { int s = num(g,"step"); return s < queue(g).size() ? queue(g).get(s) : null; }
    private static List<Integer> teammates(Map<String,Object> g, int self, boolean wolves) {
        List<Integer> ids = new ArrayList<>();
        for (int i=0; i<seats(g).size(); i++) if (i != self && (wolves ? initial(g,i).equals("WEREWOLF") : initial(g,i).startsWith("MASON_"))) ids.add(i);
        return ids;
    }
    private static String names(Map<String,Object> g, List<Integer> ids) { return String.join("、", ids.stream().map(i -> str(seats(g).get(i),"nickname")).toList()); }
    private static void option(List<Object> options, String id, String label) { options.add(map("id",id,"label",label)); }
    private static String stepId(Map<String,Object> g) { return str(g,"phase").equals("NIGHT") ? "night:" + num(g,"step") : str(g,"phase").toLowerCase(Locale.ROOT); }
    static Map<String,Object> view(Map<String,Object> g, String selfId, boolean host) {
        int self = -1; for (int i=0;i<seats(g).size();i++) if (selfId.equals(seats(g).get(i).get("id"))) self=i;
        if (self < 0) throw new IllegalArgumentException("本局成员不存在");
        Map<String,Object> p = seats(g).get(self);
        String phase = str(g,"phase"), prompt = "", sid = stepId(g);
        List<Object> options = new ArrayList<>();
        switch (phase) {
            case "DEAL" -> { prompt = "查看并记住初始身份，确认后等待所有玩家。"; if (!Boolean.TRUE.equals(p.get("seen"))) option(options,"confirm","我记住了，进入夜晚"); }
            case "NIGHT" -> {
                prompt = "夜间行动进行中，请等待自己的提示。";
                Map<String,Object> t = turn(g);
                if (t != null && num(t,"seat") == self) {
                    String role = str(t,"role"); prompt = "请执行" + name(role) + "的行动";
                    if (List.of("DOPPELGANGER","SEER","ROBBER").contains(role)) for (int i=0;i<seats(g).size();i++) if(i!=self) option(options,"player:"+i,str(seats(g).get(i),"nickname"));
                    if (role.equals("TROUBLEMAKER")) for(int i=0;i<seats(g).size();i++) for(int j=i+1;j<seats(g).size();j++) if(i!=self && j!=self) option(options,"swap:"+i+":"+j,"交换 " + names(g,List.of(i,j)));
                    if (role.equals("SEER")) for(int i=0;i<3;i++) for(int j=i+1;j<3;j++) option(options,"centers:"+i+":"+j,"查看底牌 " +(i+1)+"、"+(j+1));
                    if (role.equals("DRUNK") || (role.equals("WEREWOLF") && teammates(g,self,true).isEmpty())) for(int i=0;i<3;i++) option(options,"center:"+i,(role.equals("DRUNK") ? "交换底牌 " : "查看底牌 ")+(i+1));
                    if (List.of("MINION","MASON_A","MASON_B","INSOMNIAC").contains(role) || role.equals("WEREWOLF") && !teammates(g,self,true).isEmpty()) option(options,"confirm","查看我的夜间信息");
                    if (List.of("WEREWOLF","SEER","ROBBER","TROUBLEMAKER").contains(role)) option(options,"skip","不使用技能");
                }
            }
            case "DAY" -> { prompt = "天亮了。根据初始身份和夜间信息讨论；交换后的身份将在结算时揭晓。"; if(host) option(options,"vote","开始秘密投票"); }
            case "VOTE" -> { prompt = "每人投给另一位玩家，提交后不可更改。所有票提交后统一揭晓。"; if(num(p,"vote") < 0) for(int i=0;i<seats(g).size();i++) if(i!=self) option(options,"player:"+i,"投给 " + str(seats(g).get(i),"nickname")); }
            case "RESULT" -> prompt = "本局结束 · " + str(g,"winner");
            case "ABORTED" -> prompt = "有玩家离开，本局已中止，房主可以开始下一局。";
        }
        List<Object> reveal = new ArrayList<>();
        if (phase.equals("RESULT")) {
            for (Map<String,Object> seat : seats(g)) reveal.add(map("nickname",seat.get("nickname"),"initialRole",name(str(seat,"initial")),"finalRole",name(effective(g,str(seat,"card"))),"voteTarget",str(seats(g).get(num(seat,"vote")),"nickname"),"eliminated", ((List<?>)g.get("eliminated")).stream().anyMatch(i -> ((Number)i).intValue() == seats(g).indexOf(seat))));
        }
        return map("phase",phase,"stepId",sid,"prompt",prompt,"options",options,"notes",p.get("notes"),"revealed",reveal,
            "center",phase.equals("RESULT") ? center(g).stream().map(OneNightGame::name).toList() : List.of(),
            "confirmedCount",seats(g).stream().filter(s -> Boolean.TRUE.equals(s.get("seen"))).count(), "votedCount",seats(g).stream().filter(s -> num(s,"vote")>=0).count());
    }
    static boolean act(Map<String,Object> g, String selfId, boolean host, String step, String choice) {
        Map<String,Object> receipts = Json.object(g.get("receipts"));
        String key = selfId+":"+step;
        if (receipts.containsKey(key)) {
            if (!choice.equals(receipts.get(key))) throw new IllegalArgumentException("该操作已提交，不可更改");
            return false;
        }
        Map<String,Object> v = view(g,selfId,host);
        if(!step.equals(v.get("stepId"))) throw new IllegalArgumentException("行动步骤已更新，请刷新");
        if (((List<?>)v.get("options")).stream().noneMatch(o -> choice.equals(Json.object(o).get("id")))) throw new IllegalArgumentException("当前不可执行该行动");
        int self=0; while(!selfId.equals(seats(g).get(self).get("id"))) self++;
        Map<String,Object> p = seats(g).get(self);
        switch(str(g,"phase")) {
            case "DEAL" -> { p.put("seen",true); if(seats(g).stream().allMatch(s -> Boolean.TRUE.equals(s.get("seen")))) { g.put("phase","NIGHT"); advance(g); } }
            case "NIGHT" -> { perform(g,self,choice); g.put("step",num(g,"step")+1); advance(g); }
            case "DAY" -> g.put("phase","VOTE");
            case "VOTE" -> { p.put("vote",Integer.parseInt(choice.split(":")[1])); if(seats(g).stream().allMatch(s -> num(s,"vote")>=0)) resolve(g); }
            default -> throw new IllegalArgumentException("本局已结束");
        }
        receipts.put(key,choice);
        return true;
    }
    private static void advance(Map<String,Object> g) { if(turn(g)==null) g.put("phase","DAY"); }
    @SuppressWarnings("unchecked") private static void note(Map<String,Object> g,int self,String text) { ((List<String>)seats(g).get(self).get("notes")).add(text); }
    private static void swap(Map<String,Object> g,int a,int b) { String old=card(g,a); seats(g).get(a).put("card",card(g,b)); seats(g).get(b).put("card",old); }
    private static void perform(Map<String,Object> g,int self,String choice) {
        Map<String,Object> t=turn(g); String role=str(t,"role");
        if(choice.equals("skip")) { note(g,self,name(role)+"：未使用技能"); return; }
        String[] args=choice.split(":"); int a=args.length>1?Integer.parseInt(args[1]):-1, b=args.length>2?Integer.parseInt(args[2]):-1;
        switch(role) {
            case "DOPPELGANGER" -> {
                String copied=card(g,a); g.put("copied",copied); note(g,self,"复制身份："+name(copied));
                if(order(copied)>0) {
                    int at=num(g,"step")+1;
                    if(List.of("WEREWOLF","MASON_A","MASON_B","INSOMNIAC").contains(copied)) while(at<queue(g).size() && order(str(queue(g).get(at),"role"))<=order(copied)) at++;
                    queue(g).add(at,map("seat",self,"role",copied,"copy",true));
                }
            }
            case "WEREWOLF" -> { List<Integer> mates=teammates(g,self,true); note(g,self,mates.isEmpty()?"独狼查看底牌"+(a+1)+"："+name(center(g).get(a)):"狼人同伴："+names(g,mates)); }
            case "MINION" -> { List<Integer> wolves=teammates(g,self,true); note(g,self,wolves.isEmpty()?"场上没有狼人":"狼人："+names(g,wolves)); }
            case "MASON_A", "MASON_B" -> { List<Integer> mates=teammates(g,self,false); note(g,self,mates.isEmpty()?"场上没有其他守夜人":"守夜人同伴："+names(g,mates)); }
            case "SEER" -> note(g,self,args[0].equals("player")?str(seats(g).get(a),"nickname")+"的牌："+name(card(g,a)):"底牌"+(a+1)+"："+name(center(g).get(a))+"；底牌"+(b+1)+"："+name(center(g).get(b)));
            case "ROBBER" -> { swap(g,self,a); note(g,self,"交换后看到的牌："+name(card(g,self))); }
            case "TROUBLEMAKER" -> { swap(g,a,b); note(g,self,"已交换："+names(g,List.of(a,b))+"（未查看身份）"); }
            case "DRUNK" -> { String old=card(g,self); seats(g).get(self).put("card",center(g).get(a)); center(g).set(a,old); note(g,self,"已交换底牌"+(a+1)+"，不能查看新身份"); }
            case "INSOMNIAC" -> note(g,self,"最后看到的牌："+name(card(g,self)));
            default -> throw new IllegalArgumentException("未知夜间角色");
        }
    }
    static void resolve(Map<String,Object> g) {
        int n=seats(g).size(); int[] counts=new int[n]; for(Map<String,Object> p:seats(g)) counts[num(p,"vote")]++;
        int max=Arrays.stream(counts).max().orElse(0); Set<Integer> dead=new LinkedHashSet<>();
        if(max>=2) for(int i=0;i<n;i++) if(counts[i]==max) dead.add(i);
        boolean changed; do { changed=false; for(int i:new ArrayList<>(dead)) if(effective(g,card(g,i)).equals("HUNTER")) changed|=dead.add(num(seats(g).get(i),"vote")); } while(changed);
        boolean wolf=false,wolfDead=false,tannerDead=false,minion=false,minionDead=false;
        for(int i=0;i<n;i++) switch(effective(g,card(g,i))) {
            case "WEREWOLF" -> { wolf=true; wolfDead|=dead.contains(i); }
            case "MINION" -> { minion=true; minionDead|=dead.contains(i); }
            case "TANNER" -> tannerDead|=dead.contains(i);
        }
        boolean village=wolf?wolfDead:minion?minionDead:dead.isEmpty();
        boolean evil=wolf?!wolfDead:minion&&!minionDead&&!dead.isEmpty();
        List<String> winners=new ArrayList<>();
        if(tannerDead) { winners.add("皮匠"); if(wolfDead) winners.add("村民阵营"); }
        else { if(village) winners.add("村民阵营"); if(evil) winners.add("狼人阵营"); }
        g.put("winner",winners.isEmpty()?"无人获胜":String.join("、",winners)+"胜利");
        g.put("eliminated",new ArrayList<>(dead)); g.put("phase","RESULT");
    }
}

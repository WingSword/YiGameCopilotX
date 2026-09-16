package org.walks.rooms;

import java.text.Normalizer;
import java.util.*;
import static org.walks.rooms.RoomServer.map;

/** One drawing turn per member, 90 seconds; correct guess +10 and painter +5. */
final class DrawingGame {
    private static final List<String> WORDS=List.of("雨伞","自行车","西瓜","机器人","长颈鹿","火车","雪人","蛋糕","熊猫","风筝","飞机","热气球","吉他","海豚","城堡","太阳","眼镜","帐篷","鲸鱼","足球","冰淇淋","滑梯","书包","蝴蝶","章鱼","汉堡","台灯","闹钟","火山","树叶","毛毛虫","彩虹","轮船","南瓜","雪花","蘑菇","灯塔","月亮","游泳池","花瓶");
    private static final Set<String> COLORS=Set.of("#111111","#E53935","#1E88E5","#43A047","#FDD835","#8E24AA","#FFFFFF");
    static void config(int count) { if(count<3 || count>10) throw new IllegalArgumentException("你画我猜需要 3–10 人"); }
    static Map<String,Object> start(List<Map<String,Object>> players,Random random) {
        config(players.size()); List<String> words=new ArrayList<>(WORDS); Collections.shuffle(words,random);
        List<Map<String,Object>> seats=new ArrayList<>();
        for(Map<String,Object> p:players) seats.add(map("id",p.get("id"),"nickname",p.get("nickname"),"score",0L,"guessed",false,"feedback","","lastGuess",0L,"guesses",0L));
        return map("phase","DRAW_READY","turn",0L,"step",0L,"deadline",0L,"revision",0L,"words",new ArrayList<>(words.subList(0,players.size())),
            "seats",seats,"strokes",new ArrayList<>(),"receipts",new LinkedHashMap<>(),"log",new ArrayList<>(),"winner","");
    }
    static Map<String,Object> view(Map<String,Object> game,String self,long now) {
        String phase=str(game,"phase"); Map<String,Object> player=seat(game,self);
        boolean painter=painter(game).equals(self), result=List.of("TURN_RESULT","RESULT").contains(phase);
        return map("phase",phase,"stepId",stepId(game),"turn",number(game,"turn")+1,"totalTurns",seats(game).size(),"painterId",painter(game),
            "word",painter && !phase.equals("ABORTED") || result ? word(game) : "","wordLength",word(game).codePointCount(0,word(game).length()),
            "remainingSeconds",Math.max(0,(number(game,"deadline")-now+999)/1000),"deadline",game.get("deadline"),"revision",game.get("revision"),
            "canDraw",painter && phase.equals("DRAWING") && now<number(game,"deadline"),"canGuess",!painter && !Boolean.TRUE.equals(player.get("guessed")) && phase.equals("DRAWING") && now<number(game,"deadline"),
            "feedback",player.get("feedback"),"strokes",game.get("strokes"),"publicLog",game.get("log"),
            "scores",seats(game).stream().map(p->map("id",p.get("id"),"nickname",p.get("nickname"),"score",p.get("score"),"guessed",p.get("guessed"))).toList());
    }
    static void act(Map<String,Object> game,String self,boolean host,Map<String,Object> body,long now) {
        String request=text(body,"requestId",32,100),step=text(body,"stepId",1,64),op=text(body,"operation",1,16);
        String key=self+":"+request, signature=CommandFingerprint.of(body);
        Map<String,Object> receipts=Json.object(game.get("receipts"));
        if(receipts.containsKey(key)) { if(!receipts.get(key).equals(signature)) throw invalid("同一请求不能修改内容"); return; }
        if(!step.equals(stepId(game))) throw invalid("画图轮次已更新，请查看最新画面");
        String phase=str(game,"phase");
        if(List.of("RESULT","ABORTED").contains(phase)) throw invalid("本局已经结束");
        if(receipts.size()>=1200 && !List.of("next","end","tick").contains(op)) throw invalid("本轮操作已达上限，请由房主结束本轮");
        boolean painter=painter(game).equals(self);
        switch(op) {
            case "start" -> {
                if(!painter || !phase.equals("DRAW_READY")) throw invalid("请等待画者开始");
                game.put("phase","DRAWING"); game.put("deadline",now+90_000); game.put("step",number(game,"step")+1);
            }
            case "stroke", "undo", "clear" -> {
                if(!painter || !phase.equals("DRAWING") || now>=number(game,"deadline")) throw invalid("现在不能修改画板");
                if(LedgerGame.integer(body.get("revision"),0,Long.MAX_VALUE)!=number(game,"revision")) throw invalid("画板已更新，请重试");
                List<Map<String,Object>> strokes=rows(game,"strokes");
                if(op.equals("stroke")) {
                    if(strokes.size()>=120) throw invalid("本轮最多 120 笔，请撤销或清空后继续");
                    String color=text(body,"color",7,7); if(!COLORS.contains(color)) throw invalid("画笔颜色无效");
                    long width=LedgerGame.integer(body.get("width"),2,30);
                    if(!(body.get("points") instanceof List<?> points) || points.isEmpty() || points.size()>96) throw invalid("每笔需要 1–96 个点");
                    List<Object> normalized=new ArrayList<>();
                    for(Object raw:points) {
                        if(!(raw instanceof List<?> point) || point.size()!=2) throw invalid("画笔坐标无效");
                        normalized.add(List.of(LedgerGame.integer(point.get(0),0,1000),LedgerGame.integer(point.get(1),0,1000)));
                    }
                    strokes.add(map("id",request,"color",color,"width",width,"points",normalized));
                } else if(op.equals("undo")) {
                    if(strokes.isEmpty()) throw invalid("没有可撤销的笔画"); strokes.remove(strokes.size()-1);
                } else strokes.clear();
                game.put("revision",number(game,"revision")+1);
            }
            case "guess" -> {
                Map<String,Object> p=seat(game,self);
                if(painter || !phase.equals("DRAWING") || now>=number(game,"deadline") || Boolean.TRUE.equals(p.get("guessed"))) throw invalid("现在不能猜词");
                if(now-number(p,"lastGuess")<600 || number(p,"guesses")>=100) throw invalid("猜词过于频繁，请稍后再试");
                String guess=text(body,"guess",1,40);
                p.put("lastGuess",now); p.put("guesses",number(p,"guesses")+1);
                if(normalize(guess).equals(normalize(word(game)))) {
                    p.put("guessed",true); p.put("score",number(p,"score")+10); p.put("feedback","猜中了，获得 10 分");
                    Map<String,Object> artist=seat(game,painter(game)); artist.put("score",number(artist,"score")+5);
                    log(game,str(p,"nickname")+" 猜中了");
                    if(seats(game).stream().filter(s->!s.get("id").equals(painter(game))).allMatch(s->Boolean.TRUE.equals(s.get("guessed")))) endTurn(game,"全部猜中");
                } else p.put("feedback","未猜中，再试一次");
            }
            case "end" -> { if(!host || !List.of("DRAW_READY","DRAWING").contains(phase)) throw invalid("仅房主可提前结束本轮"); endTurn(game,"房主结束本轮"); }
            case "tick" -> { if(!phase.equals("DRAWING") || now<number(game,"deadline")) throw invalid("本轮尚未超时"); endTurn(game,"时间到"); }
            case "next" -> {
                if(!host || !phase.equals("TURN_RESULT")) throw invalid("仅房主可开始下一轮");
                if(number(game,"turn")+1==seats(game).size()) {
                    long max=seats(game).stream().mapToLong(p->number(p,"score")).max().orElse(0);
                    game.put("winner","积分最高："+String.join("、",seats(game).stream().filter(p->number(p,"score")==max).map(p->str(p,"nickname")).toList())+"（"+max+" 分）");
                    game.put("phase","RESULT");
                } else {
                    game.put("turn",number(game,"turn")+1); game.put("phase","DRAW_READY"); game.put("deadline",0L);
                    game.put("revision",0L); rows(game,"strokes").clear(); receipts.clear();
                    for(Map<String,Object> p:seats(game)) { p.put("guessed",false); p.put("feedback",""); p.put("lastGuess",0L); p.put("guesses",0L); }
                }
                game.put("step",number(game,"step")+1);
            }
            default -> throw invalid("不支持的绘画操作");
        }
        receipts.put(key,signature);
    }
    static boolean timeout(Map<String,Object> g,long now) {
        if(!str(g,"phase").equals("DRAWING") || now<number(g,"deadline")) return false;
        endTurn(g,"时间到"); return true;
    }
    private static void endTurn(Map<String,Object> g,String reason) {
        log(g,"第 "+(number(g,"turn")+1)+" 轮："+reason+"，答案「"+word(g)+"」");
        g.put("phase","TURN_RESULT"); g.put("step",number(g,"step")+1);
    }
    private static String normalize(String s) { return Normalizer.normalize(s,Normalizer.Form.NFKC).replaceAll("\\s+","").toLowerCase(Locale.ROOT); }
    private static String text(Map<String,Object> m,String key,int min,int max) {
        if(!(m.get(key) instanceof String s) || s.trim().length()<min || s.trim().length()>max || s.chars().anyMatch(c->c<32 || c>=127 && c<=159)) throw invalid("输入无效："+key);
        return s.trim();
    }
    private static String stepId(Map<String,Object> g) { return "draw:"+g.get("step"); }
    private static String painter(Map<String,Object> g) { return str(seats(g).get((int)number(g,"turn")),"id"); }
    private static String word(Map<String,Object> g) { return (String)((List<?>)g.get("words")).get((int)number(g,"turn")); }
    @SuppressWarnings("unchecked") private static void log(Map<String,Object> g,String message) { List<String> log=(List<String>)g.get("log"); log.add(message); if(log.size()>100) log.remove(0); }
    private static String str(Map<String,Object> g,String key) { return (String)g.get(key); }
    private static long number(Map<String,Object> g,String key) { return ((Number)g.get(key)).longValue(); }
    private static List<Map<String,Object>> seats(Map<String,Object> g) { return rows(g,"seats"); }
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> rows(Map<String,Object> g,String key) { return (List<Map<String,Object>>)g.get(key); }
    private static Map<String,Object> seat(Map<String,Object> g,String id) { return seats(g).stream().filter(p->id.equals(p.get("id"))).findFirst().orElseThrow(()->invalid("成员不存在")); }
    private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}

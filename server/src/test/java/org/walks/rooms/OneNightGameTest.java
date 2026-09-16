package org.walks.rooms;

import java.util.*;
import static org.walks.rooms.RoomServer.map;

public final class OneNightGameTest {
    private static int checks;
    private static void check(boolean ok,String message) { checks++; if(!ok) throw new AssertionError(message); }
    private static List<Map<String,Object>> players(int n) { List<Map<String,Object>> ps=new ArrayList<>(); for(int i=0;i<n;i++) ps.add(map("id","p"+i,"nickname","玩家"+i)); return ps; }
    private static Map<String,Object> game(String... cards) { Map<String,Object> g=OneNightGame.dealt(players(cards.length-3),List.of(cards)); for(int i=0;i<cards.length-3;i++) OneNightGame.act(g,"p"+i,i==0,"deal","confirm"); return g; }
    private static void action(Map<String,Object> g,int player,String choice) { OneNightGame.act(g,"p"+player,player==0,(String)OneNightGame.view(g,"p"+player,player==0).get("stepId"),choice); }
    private static String notes(Map<String,Object> g,int p) { return Json.write(OneNightGame.view(g,"p"+p,p==0).get("notes")); }
    private static Map<String,Object> copy(Map<String,Object> g) { return Json.object(Json.parse(Json.write(g))); }
    private static void vote(Map<String,Object> g,int... targets) { g.put("phase","DAY"); action(g,0,"vote"); for(int i=0;i<targets.length;i++) action(g,i,"player:"+targets[i]); }
    public static void main(String[] args) {
        for(int n=3;n<=10;n++) for(int seed=0;seed<80;seed++) {
            Map<String,Object> g=OneNightGame.start(players(n),OneNightGame.preset(n),new Random(seed));
            int actions=0;
            while(!g.get("phase").equals("RESULT")) {
                boolean progressed=false;
                for(int p=0;p<n;p++) {
                    Map<String,Object> view=OneNightGame.view(g,"p"+p,p==0);
                    check(((List<?>)view.get("center")).isEmpty(),"center remains private before result");
                    check(((List<?>)view.get("revealed")).isEmpty(),"roster remains private before result");
                    List<?> options=(List<?>)view.get("options");
                    if(options.isEmpty()) continue;
                    String step=(String)view.get("stepId"), choice=(String)Json.object(options.get(seed%options.size())).get("id");
                    OneNightGame.act(g,"p"+p,p==0,step,choice);
                    String before=Json.write(g);
                    check(!OneNightGame.act(g,"p"+p,p==0,step,choice),"duplicate accepted without replay");
                    check(before.equals(Json.write(g)),"duplicate has no mutation");
                    g=copy(g); // Every action survives a JSON persistence roundtrip.
                    actions++; progressed=true; break;
                }
                check(progressed && actions<60,"all presets reach a result without deadlocks");
            }
            check(((List<?>)OneNightGame.view(g,"p0",true).get("revealed")).size()==n,"every identity revealed only at result");
        }
        Map<String,Object> g=game("INSOMNIAC","ROBBER","TROUBLEMAKER","DRUNK","WEREWOLF","VILLAGER","SEER");
        action(g,1,"player:0"); action(g,2,"swap:0:3"); action(g,3,"center:0"); action(g,0,"confirm");
        check(notes(g,0).contains("酒鬼"),"insomniac observes all prior swaps");
        check(!notes(g,3).contains("狼人"),"drunk never sees replacement");
        g=game("DOPPELGANGER","ROBBER","INSOMNIAC","WEREWOLF","VILLAGER","SEER");
        action(g,0,"player:1"); action(g,0,"player:2"); action(g,1,"player:2"); action(g,2,"confirm");
        check(notes(g,0).contains("失眠者") && notes(g,1).contains("化身幽灵"),"copied robber acts immediately and preserves physical doppelganger card");
        g=game("DOPPELGANGER","WEREWOLF","MINION","VILLAGER","VILLAGER","SEER");
        action(g,0,"player:1"); action(g,1,"confirm"); action(g,0,"confirm"); action(g,2,"confirm");
        check(notes(g,1).contains("玩家0") && notes(g,2).contains("玩家0"),"wolf and minion recognize copied wolf");
        g=game("HUNTER","WEREWOLF","VILLAGER","SEER","ROBBER","VILLAGER"); vote(g,1,0,0);
        check(g.get("winner").equals("村民阵营胜利"),"hunter takes voted target automatically");
        check(Json.write(g.get("eliminated")).equals("[0,1]"),"hunter elimination included");
        g=game("TANNER","WEREWOLF","VILLAGER","VILLAGER","SEER","ROBBER","DRUNK"); vote(g,1,0,0,1);
        check(g.get("winner").equals("皮匠、村民阵营胜利"),"tanner and village may share victory");
        g=game("MINION","VILLAGER","VILLAGER","WEREWOLF","SEER","ROBBER"); vote(g,1,2,1);
        check(g.get("winner").equals("狼人阵营胜利"),"minion without wolves wins when another dies");
        g=game("SEER","VILLAGER","VILLAGER","WEREWOLF","ROBBER","DRUNK"); vote(g,1,2,1);
        check(g.get("winner").equals("无人获胜"),"no wolf and a dead villager is not wolf victory");
        g=game("SEER","VILLAGER","VILLAGER","WEREWOLF","ROBBER","DRUNK"); vote(g,1,2,0);
        check(g.get("winner").equals("村民阵营胜利"),"one vote each means nobody dies");
        try { OneNightGame.deck(List.of("WEREWOLF"),3); throw new AssertionError("invalid deck accepted"); } catch(IllegalArgumentException expected) { checks++; }
        System.out.println("PASS: "+checks+" one-night checks; 640 complete games, private views, retries, persistence and special roles");
    }
}

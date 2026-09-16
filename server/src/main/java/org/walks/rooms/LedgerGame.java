package org.walks.rooms;

import java.util.*;

/** Virtual tabletop accounts. Commands are atomic under RoomServer's durable mutation lock. */
final class LedgerGame {
    static final long MAX = 999_999_999_999L;
    static final String BANK = "bank", POT = "pot";
    private LedgerGame() {}

    static Map<String,Object> config(Map<String,Object> body) {
        String preset = (String)body.getOrDefault("ledgerPreset", "electronic");
        long initial = switch(preset) {
            case "electronic" -> 1_500_000; case "property" -> 1500; case "score" -> 0; case "chips" -> 1000;
            default -> throw invalid("请选择有效的记账场景");
        };
        if(body.containsKey("initialBalance")) initial = integer(body.get("initialBalance"),0,MAX);
        return map("preset",preset,"initialBalance",initial,"unit",preset.equals("score") ? "分" : preset.equals("chips") ? "筹码" : "游戏币");
    }

    static Map<String,Object> start(List<Map<String,Object>> players, Map<String,Object> config) {
        List<Map<String,Object>> accounts = new ArrayList<>();
        for(Map<String,Object> player : players) accounts.add(map("id",player.get("id"),"name",player.get("nickname"),"balance",config.get("initialBalance")));
        accounts.add(map("id",POT,"name","公共池","balance",0L));
        return map("config",config,"revision",0L,"accounts",accounts,"history",new ArrayList<>(),"pending",new ArrayList<>(),"receipts",new LinkedHashMap<>());
    }

    static void act(Map<String,Object> state, String self, boolean host, Map<String,Object> command) {
        String request = text(command,"requestId",32,100);
        Map<String,Object> receipts = Json.object(state.get("receipts"));
        String key = self + ":" + request, signature = CommandFingerprint.of(command);
        if(receipts.containsKey(key)) {
            if(!receipts.get(key).equals(signature)) throw invalid("同一请求不能修改内容，请重新确认");
            return;
        }
        if(receipts.size() >= 2000) throw invalid("本局操作已达上限，请结束后开启新账本");
        if(integer(command.get("revision"),0,Long.MAX_VALUE) != integer(state.get("revision"),0,Long.MAX_VALUE)) throw invalid("账本已更新，请查看最新余额后重新确认");
        String operation = text(command,"operation",1,20);
        switch(operation) {
            case "transfer", "request" -> {
                String from = text(command,"from",1,64), to = text(command,"to",1,64);
                long amount = integer(command.get("amount"),1,MAX);
                String memo = text(command,"memo",0,60);
                checkAccount(state,from); checkAccount(state,to);
                if(from.equals(to)) throw invalid("请选择不同的付款方与收款方");
                if(operation.equals("request")) {
                    if(!to.equals(self) || from.equals(BANK) || from.equals(POT)) throw invalid("只能向其他玩家发起收款请求");
                    if(rows(state,"pending").size() >= 30) throw invalid("待确认收款过多，请先处理");
                    rows(state,"pending").add(map("id",UUID.randomUUID().toString(),"from",from,"to",to,"amount",amount,"memo",memo));
                } else {
                    if(!host && !from.equals(self)) throw invalid("只能支付自己的余额，银行和代记请交给房主");
                    post(state,self,List.of(leg(from,to,amount)),memo.isBlank() ? "转账" : memo,"");
                }
            }
            case "approve", "reject", "cancel" -> {
                String id = text(command,"entryId",1,100);
                Map<String,Object> pending = rows(state,"pending").stream().filter(p -> id.equals(p.get("id"))).findFirst().orElseThrow(() -> invalid("收款请求已处理"));
                if(operation.equals("cancel") ? !self.equals(pending.get("to")) : !self.equals(pending.get("from"))) throw invalid("仅该请求的付款人可确认或拒绝，收款人可取消");
                if(operation.equals("approve")) post(state,self,List.of(leg(str(pending,"from"),str(pending,"to"),(Long)pending.get("amount"))),str(pending,"memo").isBlank() ? "确认收款" : str(pending,"memo"),"");
                rows(state,"pending").remove(pending);
            }
            case "grant", "collect", "split" -> {
                if(!host) throw invalid("批量操作仅限房主");
                long amount = integer(command.get("amount"),1,MAX);
                Object raw = command.get("targets");
                if(!(raw instanceof List<?> targets) || targets.isEmpty() || targets.size()>20) throw invalid("请至少选择一名玩家");
                Set<String> unique = new HashSet<>();
                List<Map<String,Object>> legs = new ArrayList<>();
                int index = 0;
                // Use seating order for remainder allocation, regardless of the submitted selection order.
                for(Object target : targets) {
                    if(!(target instanceof String id) || !unique.add(id) || id.equals(BANK) || id.equals(POT)) throw invalid("玩家选择无效或重复");
                    checkAccount(state,id);
                }
                if(operation.equals("split") && amount < unique.size()) throw invalid("均分总额不能少于人数，每人至少 1 个单位");
                for(Map<String,Object> account : rows(state,"accounts")) if(unique.contains(str(account,"id"))) {
                    long share = operation.equals("split") ? amount / unique.size() + (index++ < amount % unique.size() ? 1 : 0) : amount;
                    legs.add(operation.equals("collect") ? leg(str(account,"id"),POT,share) : leg(operation.equals("split") ? POT : BANK,str(account,"id"),share));
                }
                String memo = text(command,"memo",0,60);
                post(state,self,legs,memo.isBlank() ? switch(operation) { case "grant" -> "批量发放"; case "collect" -> "收至公共池"; default -> "均分公共池"; } : memo,"");
            }
            case "undo" -> {
                if(!host) throw invalid("撤销仅限房主");
                String id = text(command,"entryId",1,100);
                Map<String,Object> latest = rows(state,"history").stream().filter(e -> str(e,"reverses").isEmpty() && !Boolean.TRUE.equals(e.get("reversed"))).reduce((a,b)->b).orElseThrow(() -> invalid("没有可撤销的交易"));
                if(!id.equals(latest.get("id"))) throw invalid("只能撤销最近一笔有效交易，请刷新记录");
                List<Map<String,Object>> reversed = new ArrayList<>();
                for(Map<String,Object> leg : rows(latest,"legs")) reversed.add(leg(str(leg,"to"),str(leg,"from"),(Long)leg.get("amount")));
                post(state,self,reversed,"撤销：" + str(latest,"memo"),id);
                latest.put("reversed",true);
            }
            default -> throw invalid("不支持的记账操作");
        }
        state.put("revision",((Number)state.get("revision")).longValue()+1);
        receipts.put(key,signature);
    }

    private static void post(Map<String,Object> state,String actor,List<Map<String,Object>> legs,String memo,String reverses) {
        Map<String,Long> deltas = new LinkedHashMap<>();
        for(Map<String,Object> leg : legs) {
            long amount = (Long)leg.get("amount");
            deltas.merge(str(leg,"from"),-amount,Long::sum); deltas.merge(str(leg,"to"),amount,Long::sum);
        }
        Map<String,Long> balances = new LinkedHashMap<>();
        for(Map<String,Object> account : rows(state,"accounts")) {
            String id = str(account,"id");
            long next = ((Number)account.get("balance")).longValue()+deltas.getOrDefault(id,0L);
            if(next < -MAX || next > MAX) throw invalid("交易后余额超出允许范围");
            if(next < 0 && (id.equals(POT) || str(Json.object(state.get("config")),"preset").equals("chips"))) throw invalid(id.equals(POT) ? "公共池余额不足" : "筹码不足，请调整金额");
            balances.put(id,next);
        }
        for(Map<String,Object> account : rows(state,"accounts")) account.put("balance",balances.get(str(account,"id")));
        rows(state,"history").add(map("id",UUID.randomUUID().toString(),"actor",actor,"memo",memo,"timestamp",System.currentTimeMillis(),"legs",legs,"reverses",reverses,"reversed",false));
    }

    static Map<String,Object> view(Map<String,Object> state,String self) {
        Map<String,Object> config = Json.object(state.get("config"));
        List<Map<String,Object>> history = rows(state,"history");
        String undoId = history.stream().filter(e -> str(e,"reverses").isEmpty() && !Boolean.TRUE.equals(e.get("reversed"))).reduce((a,b)->b).map(e->str(e,"id")).orElse("");
        return map("revision",state.get("revision"),"preset",config.get("preset"),"unit",config.get("unit"),"initialBalance",config.get("initialBalance"),
            "accounts",state.get("accounts"),"history",history,"undoId",undoId,
            "pending",rows(state,"pending").stream().filter(p->self.equals(p.get("from")) || self.equals(p.get("to"))).toList());
    }
    private static void checkAccount(Map<String,Object> state,String id) {
        if(!id.equals(BANK) && rows(state,"accounts").stream().noneMatch(a -> id.equals(a.get("id")))) throw invalid("账户已失效，请重新选择");
    }
    private static Map<String,Object> leg(String from,String to,long amount) { return map("from",from,"to",to,"amount",amount); }
    static long integer(Object raw,long min,long max) {
        if(!(raw instanceof Byte || raw instanceof Short || raw instanceof Integer || raw instanceof Long)) throw invalid("金额和版本必须为整数");
        long value = ((Number)raw).longValue();
        if(value<min || value>max) throw invalid("数值超出允许范围");
        return value;
    }
    private static String text(Map<String,Object> b,String key,int min,int max) {
        if(!(b.getOrDefault(key,"") instanceof String s) || s.trim().length()<min || s.trim().length()>max || s.chars().anyMatch(c->c<32 || c>=127 && c<=159)) throw invalid("输入内容无效：" + key);
        return s.trim();
    }
    private static String str(Map<String,Object> m,String key) { return (String)m.getOrDefault(key,""); }
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> rows(Map<String,Object> m,String key) { return (List<Map<String,Object>>)m.get(key); }
    private static Map<String,Object> map(Object... values) { Map<String,Object> m=new LinkedHashMap<>(); for(int i=0;i<values.length;i+=2)m.put((String)values[i],values[i+1]); return m; }
    private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}

package dev.bennett.codexmeter;

import java.util.*;
import org.json.*;

/** Immutable local content and a bounded presentation-only selector. No growth/network dependency. */
public final class CoachCatalog {
    public final int schemaVersion,contentVersion;
    public final List<Quote> quotes;
    public final Map<String,Tip> tips;
    public final Map<Integer,String> legacyIds;
    public static final class Quote {
        public final String id,group,tone,text,tipId,paidText;
        public final int selectionPriority;
        Quote(JSONObject q)throws JSONException{id=q.getString("id");group=q.getString("situation");tone=q.getString("tone");text=q.getString("text");tipId=q.optString("tipId");paidText=q.optString("paidText");selectionPriority=q.optInt("selectionPriority",0);if(selectionPriority<0||selectionPriority>1)throw new IllegalArgumentException("Quote priority");}
    }
    public static final class Tip {
        public final String id,field,interest,tool,title,inputs,prompt,check,fallback;
        Tip(JSONObject t)throws JSONException{id=t.getString("id");field=t.getString("field");interest=t.getString("interest");tool=t.getString("tool");title=t.getString("title");inputs=t.getString("inputs");prompt=t.getString("prompt");check=t.getString("check");fallback=t.getString("fallback");}
    }
    public CoachCatalog(JSONObject json)throws JSONException {
        schemaVersion=json.getInt("schemaVersion");contentVersion=json.getInt("contentVersion");if(schemaVersion!=2)throw new IllegalArgumentException("Coach schema");
        Map<String,Tip> ts=new LinkedHashMap<>();JSONArray list=json.getJSONArray("tips");for(int i=0;i<list.length();i++){Tip t=new Tip(list.getJSONObject(i));if(ts.put(t.id,t)!=null||t.prompt.isEmpty())throw new IllegalArgumentException("Tip ID/prompt");}tips=Collections.unmodifiableMap(ts);
        List<Quote> qs=new ArrayList<>();Set<String> ids=new HashSet<>();list=json.getJSONArray("quotes");for(int i=0;i<list.length();i++){Quote q=new Quote(list.getJSONObject(i));if(!ids.add(q.id)||!q.tipId.isEmpty()&&!ts.containsKey(q.tipId))throw new IllegalArgumentException("Quote ID/tip");qs.add(q);}quotes=Collections.unmodifiableList(qs);
        Map<Integer,String> old=new HashMap<>();JSONObject mapping=json.getJSONObject("legacyIds");Iterator<String> it=mapping.keys();while(it.hasNext()){String k=it.next();String v=mapping.getString(k);if(!ids.contains(v))throw new IllegalArgumentException("Legacy quote mapping");old.put(Integer.parseInt(k),v);}legacyIds=Collections.unmodifiableMap(old);
    }
    public Quote find(String id){for(Quote q:quotes)if(q.id.equals(id))return q;return null;}
    static final String WEEKLY_PRACTICE = "weekly_practice";
    public static String group(UsageSnapshot snapshot,boolean fresh,boolean error,boolean bonus,double daily,long now){
        return group(snapshot, fresh, error, bonus, daily, now, false);
    }
    static String group(UsageSnapshot snapshot, boolean fresh, boolean error, boolean bonus,
            double daily, long now, boolean verifiedPro) {
        if(error)return "network";if(snapshot==null||snapshot.longWindow()==null)return "missing";if(!fresh||snapshot.fetchedAtMillis>now||now-snapshot.fetchedAtMillis>900000L||snapshot.longWindow().effectiveResetAtMillis(snapshot.fetchedAtMillis)<=now)return "stale";
        if (snapshot.weekly != null && snapshot.fiveHour == null && snapshot.allowed && !snapshot.limitReached) {
            if (!verifiedPro || snapshot.monthly != null || !snapshot.additionalLimits.isEmpty()) return "missing";
            double used = snapshot.weekly.preciseUsedPercent;
            if (used >= 100) return "missing"; // A still-allowed account may have an unobserved credit allowance.
            return used >= 90 ? "critical" : WEEKLY_PRACTICE;
        }
        if(expired(snapshot.fiveHour,snapshot.fetchedAtMillis,now)||expired(snapshot.monthly,snapshot.fetchedAtMillis,now))return "stale";
        // Authoritative blocked flags and relevant short-window constraint precede bonus/burst.
        if(snapshot.limitReached||!snapshot.allowed||snapshot.fiveHour!=null&&snapshot.fiveHour.preciseUsedPercent>=90&&snapshot.fiveHour.preciseUsedPercent<100)return "critical";
        UsageWindow w=snapshot.longWindow();if(w.preciseUsedPercent>=90&&w.preciseUsedPercent<100)return "critical";
        if((w.preciseUsedPercent>=100||snapshot.fiveHour!=null&&snapshot.fiveHour.preciseUsedPercent>=100)&&snapshot.allowed&&!snapshot.limitReached)return bonus?"bonus":"heavy";
        // Rounded zero and a zero long window never erase positive activity in another window.
        double used=w.preciseUsedPercent;
        if(snapshot.fiveHour!=null)used=Math.max(used,snapshot.fiveHour.preciseUsedPercent);
        if(snapshot.monthly!=null)used=Math.max(used,snapshot.monthly.preciseUsedPercent);
        String result=SpicyRotation.group(used,daily,true,bonus,w.effectiveResetAtMillis(snapshot.fetchedAtMillis)-now,w.windowSeconds,false);
        if(!result.equals("zero"))return result;
        if(snapshot.fiveHour==null&&!snapshot.planType.trim().equalsIgnoreCase("free"))return "missing";
        for(UsageLimit limit:snapshot.additionalLimits){
            if(limit==null||limit.primary==null&&limit.secondary==null||!limit.allowed||limit.limitReached)return "missing";
            for(UsageWindow extra:new UsageWindow[]{limit.primary,limit.secondary})if(extra!=null){
                if(expired(extra,snapshot.fetchedAtMillis,now))return "stale";
                if(extra.preciseUsedPercent>0)return "missing"; // Independently metered: do not invent a combined percentage.
            }
        }
        return Double.isFinite(daily)&&daily>0?"tiny":result;
    }
    private static boolean expired(UsageWindow w,long at,long now){return w!=null&&w.effectiveResetAtMillis(at)>0&&w.effectiveResetAtMillis(at)<=now;}
    /** Optional paid-plan joke has a neutral catalog fallback; price/payment is never inferred. */
    public static String displayText(Quote q,UsageSnapshot snapshot,boolean fresh,boolean error,boolean bonus,double daily,long now){
        if(q.paidText.isEmpty()||snapshot==null||!Arrays.asList("zero","tiny","low").contains(q.group)
                ||!q.group.equals(group(snapshot,fresh,error,bonus,daily,now)))return q.text;
        String plan=snapshot.planType.trim().toLowerCase(Locale.ROOT).replaceAll("[ _-]","");String label;
        switch(plan){case "plus":label="플러스";break;case "pro":case "pro20x":label="프로";break;case "pro5x":case "prolite":label="프로 라이트";break;default:return q.text;}
        return q.paidText.replace("{plan}",label);
    }
    public static boolean constrained(String group){return Arrays.asList("critical","missing","stale","network").contains(group);}
    public JSONObject migrate(JSONObject old)throws JSONException {
        if(old.optInt("schemaVersion")==2)return new JSONObject(old.toString());
        JSONObject out=new JSONObject().put("schemaVersion",2).put("recent",new JSONArray()).put("tips",new JSONArray()).put("hidden",new JSONArray());
        String current=legacyIds.get(old.optInt("id",-1));if(current!=null)out.put("current",current);
        for(String field:new String[]{"recent","hidden"}){JSONArray a=old.optJSONArray(field),result=new JSONArray();if(a!=null)for(int i=0;i<a.length();i++){String id=legacyIds.get(a.optInt(i,-1));if(id!=null&&!contains(result,id))result.put(id);}out.put(field,result);}
        return out;
    }
    private static boolean contains(JSONArray a,String id){for(int i=0;i<a.length();i++)if(id.equals(a.optString(i)))return true;return false;}
    private static int recentIndex(JSONArray a,String id){for(int i=a.length()-1;i>=0;i--)if(id.equals(a.optString(i)))return i;return -1;}
    private static void remember(JSONObject state,String key,String id,int limit)throws JSONException{JSONArray a=state.optJSONArray(key);if(a==null)a=new JSONArray();JSONArray b=new JSONArray();for(int i=Math.max(0,a.length()-limit+1);i<a.length();i++)if(!id.equals(a.optString(i)))b.put(a.optString(i));b.put(id);state.put(key,b);}
    public Quote select(JSONObject state,String group,String tone,String interest,String stamp,boolean rotate,Random random)throws JSONException {
        if(!tone.equals("spicy"))return null;
        JSONArray hidden=state.optJSONArray("hidden");if(hidden==null)hidden=new JSONArray();JSONArray recent=state.optJSONArray("recent"),recentTips=state.optJSONArray("tips");if(recent==null)recent=new JSONArray();if(recentTips==null)recentTips=new JSONArray();
        List<Quote> candidates = new ArrayList<>();
        for (Quote q : quotes) {
            // The practical tip series contains no allowance, billing or zero-activity claims.
            boolean eligible = WEEKLY_PRACTICE.equals(group)
                    ? q.id.startsWith("ko.practice.") && !q.tipId.isEmpty() : q.group.equals(group);
            if (eligible && q.tone.equals(tone) && !contains(hidden, q.id)) candidates.add(q);
        }
        Quote current=find(state.optString("current"));if(!rotate&&stamp.equals(state.optString("stamp"))&&candidates.contains(current))return current;
        Quote chosen=null;int best=Integer.MIN_VALUE,totalWeight=0;for(Quote q:candidates){Tip t=tips.get(q.tipId);int score=0;
            // No tip calls during constrained/unknown states. Display only offline preparation copy.
            if(constrained(group)&&t!=null)continue;
            int rec=recentIndex(recent,q.id),tr=t==null?-1:recentIndex(recentTips,t.id);score-=rec<0?0:1000+rec;score-=tr<0?0:100+tr;
            if(t!=null&&interest.equals(t.interest))score+=10;
            // Curated banter is more likely only among equally eligible rows. Keep every row
            // reachable and preserve interest, cooldowns and urgent guidance ahead of delivery.
            int weight=group.equals("critical")?1:1+2*q.selectionPriority;
            if(score>best){best=score;chosen=q;totalWeight=weight;}
            else if(score==best){totalWeight+=weight;if(random.nextInt(totalWeight)<weight)chosen=q;}
        }
        if(chosen==null){state.remove("current");return null;}
        state.put("current",chosen.id).put("stamp",stamp);remember(state,"recent",chosen.id,20);if(!chosen.tipId.isEmpty())remember(state,"tips",chosen.tipId,5);return chosen;
    }
}

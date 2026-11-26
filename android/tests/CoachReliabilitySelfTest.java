package dev.bennett.codexmeter;

import java.nio.file.*;
import java.util.*;
import org.json.*;

/** Focused reset-boundary/presentation regressions. Synthetic JVM values, no Android/account I/O. */
public final class CoachReliabilitySelfTest {
    static int checks;
    static final long RESET=1800000001000L, OBSERVED=RESET-1000;
    static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    static UsageWindow window(double used,long seconds,long reset){return new UsageWindow(used,seconds,0,reset/1000);}
    static UsageSnapshot sample(double shortUsed,double weeklyUsed,boolean allowed,boolean reached){
        return new UsageSnapshot("plus",allowed,reached,window(shortUsed,18000,RESET),window(weeklyUsed,604800,RESET+3*86400000L),OBSERVED);
    }
    static String group(UsageSnapshot s,long now,boolean error,boolean bonus){return CoachCatalog.group(s,true,error,bonus,0,now);}
    static void expect(String expected,UsageSnapshot s,long now,boolean error,boolean bonus,String label){String actual=group(s,now,error,bonus);check(expected.equals(actual),label+" expected="+expected+" actual="+actual);}
    public static void main(String[] args)throws Exception {
        if(args.length>1&&args[1].equals("probe")){
            expect("critical",sample(95,25,true,false),RESET-1,false,false,"short quota before reset");
            expect("stale",sample(95,25,true,false),RESET,false,false,"SHORT_RESET_MUST_PRECEDE_OLD_CRITICAL");
            System.out.println("Coach boundary probe: "+checks+" checks passed.");return;
        }
        TimeZone original=TimeZone.getDefault();try{
            for(String zone:new String[]{"Asia/Seoul","UTC","America/Los_Angeles","Pacific/Auckland"}){
                TimeZone.setDefault(TimeZone.getTimeZone(zone));
                for(double used:new double[]{0,0.1,25,89.9,90,95,99.9,100}){
                    UsageSnapshot s=sample(used,25,true,false);
                    check(!group(s,RESET-1,false,false).equals("stale"),"Fresh short quota before exact reset "+zone+"/"+used);
                    expect("stale",s,RESET,false,false,"Exact short reset "+zone+"/"+used);
                    expect("stale",s,RESET+1,false,true,"Past short reset before bonus "+zone+"/"+used);
                    expect("network",s,RESET,true,true,"Lookup error still precedes stale "+zone+"/"+used);
                }
                for(boolean allowed:new boolean[]{false,true})for(boolean reached:new boolean[]{false,true}){
                    UsageSnapshot s=sample(95,95,allowed,reached);
                    expect("critical",s,RESET-1,false,true,"Current authoritative constraint retained");
                    expect("stale",s,RESET,false,true,"Elapsed short reset precedes captured blocked flags");
                }
                UsageWindow monthly=window(0,2592000,RESET);
                UsageSnapshot additionalMonthly=new UsageSnapshot("plus",true,false,window(95,18000,RESET+7200000),window(25,604800,RESET+3*86400000L),monthly,Collections.emptyList(),null,-1,OBSERVED);
                expect("critical",additionalMonthly,RESET-1,false,false,"Relevant monthly still current");
                expect("stale",additionalMonthly,RESET,false,false,"Expired monthly precedes short critical");
                UsageSnapshot longExpired=new UsageSnapshot("plus",true,false,window(0,18000,RESET+7200000),window(0,604800,RESET),OBSERVED);
                expect("stale",longExpired,RESET,false,false,"Weekly reset remains guarded");
                UsageSnapshot recent=new UsageSnapshot("plus",true,false,window(0,18000,RESET+7200000),window(0,604800,RESET+3*86400000L),OBSERVED);
                expect("zero",recent,RESET,false,false,"Verified current relevant zeros remain zero");
                expect("stale",recent,OBSERVED+900001,false,false,"Fifteen-minute expiry never zero");
                expect("missing",null,RESET,false,false,"Missing observation never zero");
            }
        }finally{TimeZone.setDefault(original);}
        CoachCatalog catalog=new CoachCatalog(new JSONObject(Files.readString(Path.of(args[0]))));Random random=new Random(9913);JSONObject state=catalog.migrate(new JSONObject());
        UsageWindow week = window(27,604800,RESET);
        UsageSnapshot pro = new UsageSnapshot("prolite",true,false,null,week,OBSERVED);
        String practice = CoachCatalog.group(pro,true,false,false,0,OBSERVED,true);
        check(CoachCatalog.WEEKLY_PRACTICE.equals(practice),"Confirmed weekly-only Pro has usable evidence");
        check("missing".equals(CoachCatalog.group(pro,true,false,false,0,OBSERVED,false)),"Unconfirmed plan stays cautious");
        check("network".equals(CoachCatalog.group(pro,true,true,false,0,OBSERVED,true)),"Failed Pro observation");
        check("stale".equals(CoachCatalog.group(pro,true,false,false,0,RESET,true)),"Expired Pro observation");
        check("critical".equals(CoachCatalog.group(new UsageSnapshot("prolite",false,true,null,week,OBSERVED),true,false,false,0,OBSERVED,true)),"Blocked Pro");
        check("critical".equals(CoachCatalog.group(new UsageSnapshot("prolite",true,false,null,window(99.51,604800,RESET),OBSERVED),true,false,false,0,OBSERVED,true)),"Low weekly quota never invites a prompt");
        check("missing".equals(CoachCatalog.group(new UsageSnapshot("prolite",true,false,null,window(100,604800,RESET),OBSERVED),true,false,false,0,OBSERVED,true)),"Exhausted counter with unknown credits");
        JSONObject practiceState=catalog.migrate(new JSONObject());
        JSONArray hiddenPractice=new JSONArray();
        for(int i=0;i<40;i++){
            CoachCatalog.Quote practical=catalog.select(practiceState,practice,"spicy","","w"+i,false,random);
            check(practical!=null&&practical.id.startsWith("ko.practice.")&&catalog.tips.containsKey(practical.tipId),"Weekly-only offers audited tips");
            hiddenPractice.put(practical.id);practiceState.put("hidden",hiddenPractice);
        }
        check(catalog.select(practiceState,practice,"spicy","","hidden-all",false,random)==null,"Hidden practical tips stay hidden");
        check(catalog.select(new JSONObject(),practice,"off","","off",false,random)==null,"Weekly-only respects OFF");
        CoachCatalog.Quote first=catalog.select(state,"low","spicy","","same-foreground:low",false,random);String unchanged=state.toString();
        for(int i=0;i<100;i++){check(catalog.select(state,"low","spicy","","same-foreground:low",false,random).id.equals(first.id),"Same-state read/refresh keeps selected ID");check(unchanged.equals(state.toString()),"Same-state read does not grow recent history");}
        ArrayDeque<String> recent=new ArrayDeque<>();
        for(int i=0;i<50;i++){
            CoachCatalog.Quote next=catalog.select(state,"low","spicy","","new-foreground:"+i,false,random);
            check(!recent.contains(next.id),"New selection avoids previous20 quotes");recent.addLast(next.id);if(recent.size()>20)recent.removeFirst();
            check(state.getJSONArray("recent").length()<=20&&state.getJSONArray("tips").length()<=5,"Bounded presentation history");
        }
        state.put("hidden",new JSONArray().put(first.id));
        for(int i=0;i<20;i++)check(!catalog.select(state,"low","spicy","","hidden:"+i,false,random).id.equals(first.id),"Hidden stable ID remains excluded");
        check(catalog.select(state,"low","off","","off",false,random)==null,"OFF unchanged");
        for(String guard:new String[]{"network","missing","stale","critical"}){
            CoachCatalog.Quote q=catalog.select(state,guard,"spicy","","guard:"+guard,false,random);check(q!=null&&q.tipId.isEmpty(),"Unknown/limited states never propose another prompt call");
        }
        System.out.println("Coach reliability: "+checks+" checks passed; reset +/-1ms, four timezones, error/zero/blocked/bonus, stable reading/new-selection cooldown/OFF/hidden. JVM synthetic only.");
    }
}

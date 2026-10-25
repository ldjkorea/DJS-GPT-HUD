package dev.bennett.codexmeter;

import java.time.*;
import java.util.*;
import org.json.*;

public final class Stable315UiSelfTest {
    static int checks;
    static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        check(!HudUiModel.calendarGrid(335.9f,1),"Every date needs 48dp width");
        check(HudUiModel.calendarGrid(336,1.3f),"Exact 7x48dp boundary");
        check(!HudUiModel.calendarGrid(1000,1.31f),"Large text uses week lists");
        for(int y=2024;y<=2028;y++)for(int m=1;m<=12;m++){
            YearMonth month=YearMonth.of(y,m);Set<LocalDate> seen=new HashSet<>();
            for(int day=1;day<=month.lengthOfMonth();day++){
                LocalDate date=month.atDay(day);List<LocalDate> week=HudUiModel.week(month,date);
                check(week.size()>=1&&week.size()<=7&&week.contains(date),"Selected date always available");
                for(LocalDate d:week){check(YearMonth.from(d).equals(month),"No dates from another displayed month");seen.add(d);}
                check(HudUiModel.week(month,date.minusYears(5)).contains(month.atDay(1)),"Past anchors clamp to current month");
                check(HudUiModel.week(month,date.plusYears(5)).contains(month.atEndOfMonth()),"Future anchors clamp to current month");
            }
            check(seen.size()==month.lengthOfMonth(),"All dates remain reachable, including leap days");
        }
        JSONObject owned=new JSONObject().put("medal.title1",new JSONObject().put("first",1));String before=owned.toString();
        String[] ids={"medal.title1","medal.title3"};
        check(HudUiModel.visibleMedals(owned,ids,0)==2,"All medal titles");
        check(HudUiModel.visibleMedals(owned,ids,1)==1,"Medal-only owned filter is not empty");
        check(HudUiModel.visibleMedals(owned,ids,2)==1,"Locked filter includes missing titles");
        check(HudUiModel.visibleMedals(null,ids,1)==0,"No owned data stays empty");
        check(owned.toString().equals(before),"Presentation never changes acquisition facts");
        check(HudUiModel.remaining(10,14)==4&&HudUiModel.remaining(20,14)==0,"Remaining goal stays nonnegative");
        System.out.println("Stable315 UI: "+checks+" checks passed; calendar reachability / medal filters");
    }
}

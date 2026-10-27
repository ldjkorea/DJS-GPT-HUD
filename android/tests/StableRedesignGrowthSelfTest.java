package dev.bennett.codexmeter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Continuous user navigation, including clipped weeks, rather than isolated date membership. */
public final class StableRedesignGrowthSelfTest {
    static int checks;
    static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        check(GrowthCalendarNavigation.adjacentAnchor(YearMonth.of(2026,10),LocalDate.of(2026,10,31),1).equals(LocalDate.of(2026,11,1)),"October end must not skip November 1");
        check(GrowthCalendarNavigation.adjacentAnchor(YearMonth.of(2026,11),LocalDate.of(2026,11,1),-1).equals(LocalDate.of(2026,10,31)),"November beginning must retain the October final week");
        for(LocalDate day=LocalDate.of(2024,1,1);day.isBefore(LocalDate.of(2029,1,1));day=day.plusDays(1)){
            YearMonth month=YearMonth.from(day);List<LocalDate> current=HudUiModel.week(month,day);
            for(int direction:new int[]{-1,1}){
                LocalDate adjacent=GrowthCalendarNavigation.adjacentAnchor(month,day,direction);
                List<LocalDate> next=HudUiModel.week(YearMonth.from(adjacent),adjacent);
                check(direction<0?next.get(next.size()-1).plusDays(1).equals(current.get(0)):current.get(current.size()-1).plusDays(1).equals(next.get(0)),"Adjacent displayed ranges have no gaps or repeated dates: "+day);
                LocalDate back=GrowthCalendarNavigation.adjacentAnchor(YearMonth.from(adjacent),adjacent,-direction);
                check(HudUiModel.week(YearMonth.from(back),back).equals(current),"Previous/next is reversible at "+day);
            }
            check(GrowthCalendarNavigation.adjacentAnchor(month,day,0).equals(day),"Zero movement preserves selected date");
        }
        System.out.println("Stable redesign growth: "+checks+" checks passed; continuous month-boundary navigation");
    }
}

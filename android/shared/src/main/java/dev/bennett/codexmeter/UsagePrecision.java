package dev.bennett.codexmeter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Presentation only: at most three decimals, with no invented trailing precision. */
public final class UsagePrecision {
    private UsagePrecision() {}
    public static String number(double value) {
        if (!Double.isFinite(value)) return "—";
        BigDecimal rounded = BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        format.setRoundingMode(RoundingMode.HALF_UP);
        format.setGroupingUsed(false);
        return format.format(rounded);
    }
    public static String used(UsageWindow window) { return number(window.preciseUsedPercent) + "%"; }
    public static String remaining(UsageWindow window) { return number(100.0 - window.preciseUsedPercent) + "%"; }
    /** Selected display precision only; raw observations and scoring are unchanged. */
    public static String number(double value, int digits) {
        if (!Double.isFinite(value)) return "—";
        return number(BigDecimal.valueOf(value),digits);
    }
    private static String number(BigDecimal value,int digits) {
        if(digits==0){DecimalFormat whole=new DecimalFormat("0",DecimalFormatSymbols.getInstance(Locale.getDefault()));whole.setRoundingMode(RoundingMode.HALF_UP);return whole.format(value.setScale(0,RoundingMode.HALF_UP));}
        digits = digits == 2 ? 2 : 3;
        DecimalFormat format = new DecimalFormat(digits == 2 ? "0.00" : "0.000",
                DecimalFormatSymbols.getInstance(Locale.getDefault()));
        format.setRoundingMode(RoundingMode.HALF_UP);
        format.setGroupingUsed(false);
        return format.format(value.setScale(digits, RoundingMode.HALF_UP));
    }
    public static String percent(UsageWindow window,boolean used,int digits,boolean symbol) {
        if(window==null)return "—";
        if (digits == 0) {
            double raw = window.preciseUsedPercent;
            boolean nearEmpty = raw > 0 && raw < 1;
            boolean nearFull = raw > 99 && raw < 100;
            if (nearEmpty || nearFull) {
                boolean belowOne = used ? nearEmpty : nearFull;
                // Preserve the widget option to hide the percent symbol.
                if (!symbol) return belowOne ? "<1" : ">99";
                if ("ko".equals(Locale.getDefault().getLanguage())) {
                    return belowOne ? "1% 미만" : "99% 초과";
                }
                return belowOne ? "<1%" : ">99%";
            }
            int value = used ? window.usedPercent : window.remainingPercent();
            return Integer.toString(value) + (symbol ? "%" : "");
        }
        BigDecimal value=BigDecimal.valueOf(window.preciseUsedPercent);
        if(!used)value=BigDecimal.valueOf(100).subtract(value);
        return number(value,digits)+(symbol?"%":"");
    }
    public static String used(UsageWindow window, int digits) { return percent(window,true,digits,true); }
    public static String remaining(UsageWindow window, int digits) { return percent(window,false,digits,true); }
}

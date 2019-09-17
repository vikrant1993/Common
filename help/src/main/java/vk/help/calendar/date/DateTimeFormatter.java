package vk.help.calendar.date;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DateTimeFormatter {

    DateTimeFormatter(String aFormat) {
        fFormat = aFormat;
        fLocale = null;
        fCustomLocalization = null;
        validateState();
    }

    DateTimeFormatter(String aFormat, Locale aLocale) {
        fFormat = aFormat;
        fLocale = aLocale;
        fCustomLocalization = null;
        validateState();
    }

    DateTimeFormatter(String aFormat, List<String> aMonths, List<String> aWeekdays, List<String> aAmPmIndicators) {
        fFormat = aFormat;
        fLocale = null;
        fCustomLocalization = new CustomLocalization(aMonths, aWeekdays, aAmPmIndicators);
        validateState();
    }

    String format(DateTime aDateTime) {
        fEscapedRanges = new ArrayList<>();
        fInterpretedRanges = new ArrayList<>();
        findEscapedRanges();
        interpretInput(aDateTime);
        return produceFinalOutput();
    }

    private final String fFormat;
    private final Locale fLocale;
    private Collection<InterpretedRange> fInterpretedRanges;
    private Collection<EscapedRange> fEscapedRanges;

    private final Map<Locale, List<String>> fMonths = new LinkedHashMap<>();

    private final Map<Locale, List<String>> fWeekdays = new LinkedHashMap<>();

    private final Map<Locale, List<String>> fAmPm = new LinkedHashMap<>();

    private final CustomLocalization fCustomLocalization;

    private final class CustomLocalization {
        CustomLocalization(List<String> aMonths, List<String> aWeekdays, List<String> aAmPm) {
            if (aMonths.size() != 12) {
                throw new IllegalArgumentException("Your List of custom months must have size 12, but its size is " + aMonths.size());
            }
            if (aWeekdays.size() != 7) {
                throw new IllegalArgumentException("Your List of custom weekdays must have size 7, but its size is " + aWeekdays.size());
            }
            if (aAmPm.size() != 2) {
                throw new IllegalArgumentException("Your List of custom a.m./p.m. indicators must have size 2, but its size is " + aAmPm.size());
            }
            Months = aMonths;
            Weekdays = aWeekdays;
            AmPmIndicators = aAmPm;
        }

        List<String> Months;
        List<String> Weekdays;
        List<String> AmPmIndicators;
    }

    private static final class InterpretedRange {
        int Start;
        int End;
        String Text;

        @Override
        public String toString() {
            return "Start:" + Start + " End:" + End + " '" + Text + "'";
        }
    }

    private static final class EscapedRange {
        int Start;
        int End;
    }

    private static final String ESCAPE_CHAR = "|";
    private static final Pattern ESCAPED_RANGE = Pattern.compile("\\|[^\\|]*\\|");

    private static final String YYYY = "YYYY";
    private static final String YY = "YY";
    private static final String M = "M";
    private static final String MM = "MM";
    private static final String MMM = "MMM";
    private static final String MMMM = "MMMM";
    private static final String D = "D";
    private static final String DD = "DD";
    private static final String WWW = "WWW";
    private static final String WWWW = "WWWW";

    private static final String hh = "hh";
    private static final String h = "h";
    private static final String m = "m";
    private static final String mm = "mm";
    private static final String s = "s";
    private static final String ss = "ss";

    private static final String h12 = "h12";

    private static final String hh12 = "hh12";

    private static final int AM = 0;
    private static final int PM = 1;

    private static final String a = "a";

    private static final Pattern FRACTIONALS = Pattern.compile("f{1,9}");

    private static final String EMPTY_STRING = "";

    private static final List<String> TOKENS = new ArrayList<>();

    static {
        TOKENS.add(YYYY);
        TOKENS.add(YY);
        TOKENS.add(MMMM);
        TOKENS.add(MMM);
        TOKENS.add(MM);
        TOKENS.add(M);
        TOKENS.add(DD);
        TOKENS.add(D);
        TOKENS.add(WWWW);
        TOKENS.add(WWW);
        TOKENS.add(hh12);
        TOKENS.add(h12);
        TOKENS.add(hh);
        TOKENS.add(h);
        TOKENS.add(mm);
        TOKENS.add(m);
        TOKENS.add(ss);
        TOKENS.add(s);
        TOKENS.add(a);
        TOKENS.add("fffffffff");
        TOKENS.add("ffffffff");
        TOKENS.add("fffffff");
        TOKENS.add("ffffff");
        TOKENS.add("fffff");
        TOKENS.add("ffff");
        TOKENS.add("fff");
        TOKENS.add("ff");
        TOKENS.add("f");
    }

    private void findEscapedRanges() {
        Matcher matcher = ESCAPED_RANGE.matcher(fFormat);
        while (matcher.find()) {
            EscapedRange escapedRange = new EscapedRange();
            escapedRange.Start = matcher.start(); //first pipe
            escapedRange.End = matcher.end() - 1; //second pipe
            fEscapedRanges.add(escapedRange);
        }
    }

    private boolean isInEscapedRange(InterpretedRange aInterpretedRange) {
        boolean result = false; //innocent till shown guilty
        for (EscapedRange escapedRange : fEscapedRanges) {
            //checking only the start is sufficient, because the tokens never contain the escape char
            if (escapedRange.Start <= aInterpretedRange.Start && aInterpretedRange.Start <= escapedRange.End) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void interpretInput(DateTime aDateTime) {
        String format = fFormat;
        for (String token : TOKENS) {
            Pattern pattern = Pattern.compile(token);
            Matcher matcher = pattern.matcher(format);
            while (matcher.find()) {
                InterpretedRange interpretedRange = new InterpretedRange();
                interpretedRange.Start = matcher.start();
                interpretedRange.End = matcher.end() - 1;
                if (!isInEscapedRange(interpretedRange)) {
                    interpretedRange.Text = interpretThe(matcher.group(), aDateTime);
                    fInterpretedRanges.add(interpretedRange);
                }
            }
            format = format.replace(token, withCharDenotingAlreadyInterpreted(token));
        }
    }

    private String withCharDenotingAlreadyInterpreted(String aToken) {
        StringBuilder result = new StringBuilder();
        for (int idx = 1; idx <= aToken.length(); ++idx) {
            result.append("@");
        }
        return result.toString();
    }

    private String produceFinalOutput() {
        StringBuilder result = new StringBuilder();
        int idx = 0;
        while (idx < fFormat.length()) {
            String letter = nextLetter(idx);
            InterpretedRange interpretation = getInterpretation(idx);
            if (interpretation != null) {
                result.append(interpretation.Text);
                idx = interpretation.End;
            } else {
                if (!ESCAPE_CHAR.equals(letter)) {
                    result.append(letter);
                }
            }
            ++idx;
        }
        return result.toString();
    }

    private InterpretedRange getInterpretation(int aIdx) {
        InterpretedRange result = null;
        for (InterpretedRange interpretedRange : fInterpretedRanges) {
            if (interpretedRange.Start == aIdx) {
                result = interpretedRange;
            }
        }
        return result;
    }

    private String nextLetter(int aIdx) {
        return fFormat.substring(aIdx, aIdx + 1);
    }

    private String interpretThe(String aCurrentToken, DateTime aDateTime) {
        String result;
        if (YYYY.equals(aCurrentToken)) {
            result = valueStr(aDateTime.getYear());
        } else if (YY.equals(aCurrentToken)) {
            result = noCentury(valueStr(aDateTime.getYear()));
        } else if (MMMM.equals(aCurrentToken)) {
            int month = aDateTime.getMonth();
            result = fullMonth(month);
        } else if (MMM.equals(aCurrentToken)) {
            int month = aDateTime.getMonth();
            result = firstThreeChars(fullMonth(month));
        } else if (MM.equals(aCurrentToken)) {
            result = addLeadingZero(valueStr(aDateTime.getMonth()));
        } else if (M.equals(aCurrentToken)) {
            result = valueStr(aDateTime.getMonth());
        } else if (DD.equals(aCurrentToken)) {
            result = addLeadingZero(valueStr(aDateTime.getDay()));
        } else if (D.equals(aCurrentToken)) {
            result = valueStr(aDateTime.getDay());
        } else if (WWWW.equals(aCurrentToken)) {
            int weekday = aDateTime.getWeekDay();
            result = fullWeekday(weekday);
        } else if (WWW.equals(aCurrentToken)) {
            int weekday = aDateTime.getWeekDay();
            result = firstThreeChars(fullWeekday(weekday));
        } else if (hh.equals(aCurrentToken)) {
            result = addLeadingZero(valueStr(aDateTime.getHour()));
        } else if (h.equals(aCurrentToken)) {
            result = valueStr(aDateTime.getHour());
        } else if (h12.equals(aCurrentToken)) {
            result = valueStr(twelveHourStyle(aDateTime.getHour()));
        } else if (hh12.equals(aCurrentToken)) {
            result = addLeadingZero(valueStr(twelveHourStyle(aDateTime.getHour())));
        } else if (a.equals(aCurrentToken)) {
            int hour = aDateTime.getHour();
            result = amPmIndicator(hour);
        } else if (mm.equals(aCurrentToken)) {
            result = addLeadingZero(valueStr(aDateTime.getMinute()));
        } else if (m.equals(aCurrentToken)) {
            result = valueStr(aDateTime.getMinute());
        } else if (ss.equals(aCurrentToken)) {
            result = addLeadingZero(valueStr(aDateTime.getSecond()));
        } else if (s.equals(aCurrentToken)) {
            result = valueStr(aDateTime.getSecond());
        } else if (aCurrentToken.startsWith("f")) {
            Matcher matcher = FRACTIONALS.matcher(aCurrentToken);
            if (matcher.matches()) {
                String nanos = nanosWithLeadingZeroes(aDateTime.getNanoseconds());
                int numDecimalsToShow = aCurrentToken.length();
                result = firstNChars(nanos, numDecimalsToShow);
            } else {
                throw new IllegalArgumentException("Unknown token in date formatting pattern: " + aCurrentToken);
            }
        } else {
            throw new IllegalArgumentException("Unknown token in date formatting pattern: " + aCurrentToken);
        }

        return result;
    }

    private String valueStr(Object aItem) {
        String result = EMPTY_STRING;
        if (aItem != null) {
            result = String.valueOf(aItem);
        }
        return result;
    }

    private String noCentury(String aItem) {
        String result = EMPTY_STRING;
        if (Util.textHasContent(aItem)) {
            result = aItem.substring(2);
        }
        return result;
    }

    private String nanosWithLeadingZeroes(Integer aNanos) {
        StringBuilder result = new StringBuilder(valueStr(aNanos));
        while (result.length() < 9) {
            result.insert(0, "0");
        }
        return result.toString();
    }

    private String addLeadingZero(String aTimePart) {
        String result = aTimePart;
        if (Util.textHasContent(aTimePart) && aTimePart.length() == 1) {
            result = "0" + result;
        }
        return result;
    }

    private String firstThreeChars(String aText) {
        String result = aText;
        if (Util.textHasContent(aText) && aText.length() >= 3) {
            result = aText.substring(0, 3);
        }
        return result;
    }

    private String fullMonth(Integer aMonth) {
        String result = "";
        if (aMonth != null) {
            if (fCustomLocalization != null) {
                result = lookupCustomMonthFor(aMonth);
            } else if (fLocale != null) {
                result = lookupMonthFor(aMonth);
            } else {
                throw new IllegalArgumentException("Your date pattern requires either a Locale, or your own custom localizations for text:" + Util.quote(fFormat));
            }
        }
        return result;
    }

    private String lookupCustomMonthFor(Integer aMonth) {
        return fCustomLocalization.Months.get(aMonth - 1);
    }

    private String lookupMonthFor(Integer aMonth) {
        if (!fMonths.containsKey(fLocale)) {
            List<String> months = new ArrayList<>();
            SimpleDateFormat format = new SimpleDateFormat("MMMM", fLocale);
            for (int idx = Calendar.JANUARY; idx <= Calendar.DECEMBER; ++idx) {
                Calendar firstDayOfMonth = new GregorianCalendar();
                firstDayOfMonth.set(Calendar.YEAR, 2000);
                firstDayOfMonth.set(Calendar.MONTH, idx);
                firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 15);
                String monthText = format.format(firstDayOfMonth.getTime());
                months.add(monthText);
            }
            fMonths.put(fLocale, months);
        }
        return fMonths.get(fLocale).get(aMonth - 1); //list is 0-based
    }

    private String fullWeekday(Integer aWeekday) {
        String result = "";
        if (aWeekday != null) {
            if (fCustomLocalization != null) {
                result = lookupCustomWeekdayFor(aWeekday);
            } else if (fLocale != null) {
                result = lookupWeekdayFor(aWeekday);
            } else {
                throw new IllegalArgumentException("Your date pattern requires either a Locale, or your own custom localizations for text:" + Util.quote(fFormat));
            }
        }
        return result;
    }

    private String lookupCustomWeekdayFor(Integer aWeekday) {
        return fCustomLocalization.Weekdays.get(aWeekday - 1);
    }

    private String lookupWeekdayFor(Integer aWeekday) {
        if (!fWeekdays.containsKey(fLocale)) {
            List<String> weekdays = new ArrayList<>();
            SimpleDateFormat format = new SimpleDateFormat("EEEE", fLocale);
            for (int idx = 8; idx <= 14; ++idx) {
                Calendar firstDayOfWeek = new GregorianCalendar();
                firstDayOfWeek.set(Calendar.YEAR, 2009);
                firstDayOfWeek.set(Calendar.MONTH, 1); //month is 0-based
                firstDayOfWeek.set(Calendar.DAY_OF_MONTH, idx);
                String weekdayText = format.format(firstDayOfWeek.getTime());
                weekdays.add(weekdayText);
            }
            fWeekdays.put(fLocale, weekdays);
        }
        return fWeekdays.get(fLocale).get(aWeekday - 1); //list is 0-based
    }

    private String firstNChars(String aText, int aN) {
        return Util.textHasContent(aText) && aText.length() >= aN ? aText.substring(0, aN) : aText;
    }

    private Integer twelveHourStyle(Integer aHour) {
        Integer result = aHour;
        if (aHour != null) {
            if (aHour == 0) {
                result = 12;
            } else if (aHour > 12) {
                result = aHour - 12;
            }
        }
        return result;
    }

    private String amPmIndicator(Integer aHour) {
        String result = "";
        if (aHour != null) {
            if (fCustomLocalization != null) {
                result = lookupCustomAmPmFor(aHour);
            } else if (fLocale != null) {
                result = lookupAmPmFor(aHour);
            } else {
                throw new IllegalArgumentException("Your date pattern requires either a Locale, or your own custom localizations for text:" + Util.quote(fFormat));
            }
        }
        return result;
    }

    private String lookupCustomAmPmFor(Integer aHour) {
        return aHour < 12 ? fCustomLocalization.AmPmIndicators.get(AM) : fCustomLocalization.AmPmIndicators.get(PM);
    }

    private String lookupAmPmFor(Integer aHour) {
        if (!fAmPm.containsKey(fLocale)) {
            List<String> indicators = new ArrayList<>();
            indicators.add(getAmPmTextFor(6));
            indicators.add(getAmPmTextFor(18));
            fAmPm.put(fLocale, indicators);
        }
        return aHour < 12 ? fAmPm.get(fLocale).get(AM) : fAmPm.get(fLocale).get(PM);
    }

    private String getAmPmTextFor(Integer aHour) {
        SimpleDateFormat format = new SimpleDateFormat("a", fLocale);
        Calendar someDay = new GregorianCalendar();
        someDay.set(Calendar.YEAR, 2000);
        someDay.set(Calendar.MONTH, 6);
        someDay.set(Calendar.DAY_OF_MONTH, 15);
        someDay.set(Calendar.HOUR_OF_DAY, aHour);
        return format.format(someDay.getTime());
    }

    private void validateState() {
        if (!Util.textHasContent(fFormat)) {
            throw new IllegalArgumentException("DateTime format has no content.");
        }
    }
}
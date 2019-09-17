package vk.help.calendar.date;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTime implements Comparable<DateTime>, Serializable {

    public enum Unit {
        YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, NANOSECONDS;
    }

    public enum DayOverflow {
        LastDay,
        FirstDay,
        Spillover,
        Abort
    }

    public DateTime(String aDateTime) {
        fIsAlreadyParsed = false;
        if (aDateTime == null) {
            throw new IllegalArgumentException("String passed to DateTime constructor is null. You can use an empty string, but not a null reference.");
        }
        fDateTime = aDateTime;
    }

    public static boolean isParseable(String aCandidateDateTime) {
        boolean result = true;
        try {
            DateTime dt = new DateTime(aCandidateDateTime);
            dt.ensureParsed();
        } catch (RuntimeException ex) {
            result = false;
        }
        return result;
    }

    public DateTime(Integer aYear, Integer aMonth, Integer aDay, Integer aHour, Integer aMinute, Integer aSecond, Integer aNanoseconds) {
        fIsAlreadyParsed = true;
        fYear = aYear;
        fMonth = aMonth;
        fDay = aDay;
        fHour = aHour;
        fMinute = aMinute;
        fSecond = aSecond;
        fNanosecond = aNanoseconds;
        validateState();
    }

    public static DateTime forDateOnly(Integer aYear, Integer aMonth, Integer aDay) {
        return new DateTime(aYear, aMonth, aDay, null, null, null, null);
    }

    public static DateTime forTimeOnly(Integer aHour, Integer aMinute, Integer aSecond, Integer aNanoseconds) {
        return new DateTime(null, null, null, aHour, aMinute, aSecond, aNanoseconds);
    }

    public static DateTime forInstant(long aMilliseconds, TimeZone aTimeZone) {
        Calendar calendar = new GregorianCalendar(aTimeZone);
        calendar.setTimeInMillis(aMilliseconds);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 0..23
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int milliseconds = calendar.get(Calendar.MILLISECOND);
        int nanoseconds = milliseconds * 1000 * 1000;
        return new DateTime(year, month, day, hour, minute, second, nanoseconds);
    }

    public long getMilliseconds(TimeZone aTimeZone) {
        Integer year = getYear();
        Integer month = getMonth();
        Integer day = getDay();
        //coerce missing times to 0:
        int hour = getHour() == null ? 0 : getHour();
        int minute = getMinute() == null ? 0 : getMinute();
        int second = getSecond() == null ? 0 : getSecond();
        int nanos = getNanoseconds() == null ? 0 : getNanoseconds();

        Calendar calendar = new GregorianCalendar(aTimeZone);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // 0-based
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour); // 0..23
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, nanos / 1000000);

        return calendar.getTimeInMillis();
    }

    public static DateTime forInstantNanos(long aNanoseconds, TimeZone aTimeZone) {
        long millis = aNanoseconds / MILLION; //integer division truncates towards 0, doesn't round
        long nanosRemaining = aNanoseconds % MILLION; //size 0..999,999
        //when negative: go to the previous millis, and take the complement of nanosRemaining
        if (aNanoseconds < 0) {
            millis = millis - 1;
            nanosRemaining = MILLION + nanosRemaining; //-1 remaining coerced to 999,999
        }

        //base calculation in millis
        Calendar calendar = new GregorianCalendar(aTimeZone);
        calendar.setTimeInMillis(millis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 0..23
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int milliseconds = calendar.get(Calendar.MILLISECOND);

        DateTime withoutNanos = new DateTime(year, month, day, hour, minute, second, milliseconds * MILLION);
        return withoutNanos.plus(0, 0, 0, 0, 0, 0, (int) nanosRemaining, DayOverflow.Spillover);
    }

    public long getNanosecondsInstant(TimeZone aTimeZone) {
        Integer year = getYear();
        Integer month = getMonth();
        Integer day = getDay();
        int hour = getHour() == null ? 0 : getHour();
        int minute = getMinute() == null ? 0 : getMinute();
        int second = getSecond() == null ? 0 : getSecond();
        int nanos = getNanoseconds() == null ? 0 : getNanoseconds();

        int millis = nanos / MILLION; //integer division truncates, doesn't round
        int nanosRemaining = nanos % MILLION; //0..999,999 - always positive

        Calendar calendar = new GregorianCalendar(aTimeZone);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // 0-based
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour); // 0..23
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millis);

        long baseResult = calendar.getTimeInMillis() * MILLION; // either sign
        return baseResult + nanosRemaining;
    }

    public String getRawDateString() {
        return fDateTime;
    }

    public Integer getYear() {
        ensureParsed();
        return fYear;
    }

    public Integer getMonth() {
        ensureParsed();
        return fMonth;
    }

    public Integer getDay() {
        ensureParsed();
        return fDay;
    }

    public Integer getHour() {
        ensureParsed();
        return fHour;
    }

    public Integer getMinute() {
        ensureParsed();
        return fMinute;
    }

    public Integer getSecond() {
        ensureParsed();
        return fSecond;
    }

    public Integer getNanoseconds() {
        ensureParsed();
        return fNanosecond;
    }

    public Integer getModifiedJulianDayNumber() {
        ensureHasYearMonthDay();
        return calculateJulianDayNumberAtNoon() - 1 - EPOCH_MODIFIED_JD;
    }

    public Integer getWeekDay() {
        ensureHasYearMonthDay();
        int dayNumber = calculateJulianDayNumberAtNoon() + 1;
        int index = dayNumber % 7;
        return index + 1;
    }

    public Integer getDayOfYear() {
        ensureHasYearMonthDay();
        int k = isLeapYear() ? 1 : 2;
        return ((275 * fMonth) / 9) - k * ((fMonth + 9) / 12) + fDay - 30;
    }

    public Boolean isLeapYear() {
        ensureParsed();
        Boolean result = null;
        if (isPresent(fYear)) {
            result = isLeapYear(fYear);
        } else {
            throw new MissingItem("Year is absent. Cannot determine if leap year.");
        }
        return result;
    }

    public int getNumDaysInMonth() {
        ensureHasYearMonthDay();
        return getNumDaysInMonth(fYear, fMonth);
    }

    public Integer getWeekIndex(DateTime aStartingFromDate) {
        ensureHasYearMonthDay();
        aStartingFromDate.ensureHasYearMonthDay();
        int diff = getModifiedJulianDayNumber() - aStartingFromDate.getModifiedJulianDayNumber();
        return (diff / 7) + 1; // integer division
    }

    public Integer getWeekIndex() {
        DateTime start = DateTime.forDateOnly(2000, 1, 2);
        return getWeekIndex(start);
    }

    public boolean isSameDayAs(DateTime aThat) {
        ensureHasYearMonthDay();
        aThat.ensureHasYearMonthDay();
        return (fYear.equals(aThat.fYear) && fMonth.equals(aThat.fMonth) && fDay.equals(aThat.fDay));
    }

    public boolean lt(DateTime aThat) {
        return compareTo(aThat) < EQUAL;
    }

    public boolean lteq(DateTime aThat) {
        return compareTo(aThat) < EQUAL || equals(aThat);
    }

    public boolean gt(DateTime aThat) {
        return compareTo(aThat) > EQUAL;
    }

    public boolean gteq(DateTime aThat) {
        return compareTo(aThat) > EQUAL || equals(aThat);
    }

    public Unit getPrecision() {
        ensureParsed();
        Unit result = null;
        if (isPresent(fNanosecond)) {
            result = Unit.NANOSECONDS;
        } else if (isPresent(fSecond)) {
            result = Unit.SECOND;
        } else if (isPresent(fMinute)) {
            result = Unit.MINUTE;
        } else if (isPresent(fHour)) {
            result = Unit.HOUR;
        } else if (isPresent(fDay)) {
            result = Unit.DAY;
        } else if (isPresent(fMonth)) {
            result = Unit.MONTH;
        } else if (isPresent(fYear)) {
            result = Unit.YEAR;
        }
        return result;
    }

    public DateTime truncate(Unit aPrecision) {
        ensureParsed();
        DateTime result = null;
        if (Unit.NANOSECONDS == aPrecision) {
            throw new IllegalArgumentException("It makes no sense to truncate to nanosecond precision, since that's the highest precision available.");
        } else if (Unit.SECOND == aPrecision) {
            result = new DateTime(fYear, fMonth, fDay, fHour, fMinute, fSecond, null);
        } else if (Unit.MINUTE == aPrecision) {
            result = new DateTime(fYear, fMonth, fDay, fHour, fMinute, null, null);
        } else if (Unit.HOUR == aPrecision) {
            result = new DateTime(fYear, fMonth, fDay, fHour, null, null, null);
        } else if (Unit.DAY == aPrecision) {
            result = new DateTime(fYear, fMonth, fDay, null, null, null, null);
        } else if (Unit.MONTH == aPrecision) {
            result = new DateTime(fYear, fMonth, null, null, null, null, null);
        } else if (Unit.YEAR == aPrecision) {
            result = new DateTime(fYear, null, null, null, null, null, null);
        }
        return result;
    }

    public boolean unitsAllPresent(Unit... aUnits) {
        boolean result = true;
        ensureParsed();
        for (Unit unit : aUnits) {
            if (Unit.NANOSECONDS == unit) {
                result = result && fNanosecond != null;
            } else if (Unit.SECOND == unit) {
                result = result && fSecond != null;
            } else if (Unit.MINUTE == unit) {
                result = result && fMinute != null;
            } else if (Unit.HOUR == unit) {
                result = result && fHour != null;
            } else if (Unit.DAY == unit) {
                result = result && fDay != null;
            } else if (Unit.MONTH == unit) {
                result = result && fMonth != null;
            } else if (Unit.YEAR == unit) {
                result = result && fYear != null;
            }
        }
        return result;
    }

    public boolean hasYearMonthDay() {
        return unitsAllPresent(Unit.YEAR, Unit.MONTH, Unit.DAY);
    }

    public boolean hasHourMinuteSecond() {
        return unitsAllPresent(Unit.HOUR, Unit.MINUTE, Unit.SECOND);
    }

    public boolean unitsAllAbsent(Unit... aUnits) {
        boolean result = true;
        ensureParsed();
        for (Unit unit : aUnits) {
            if (Unit.NANOSECONDS == unit) {
                result = result && fNanosecond == null;
            } else if (Unit.SECOND == unit) {
                result = result && fSecond == null;
            } else if (Unit.MINUTE == unit) {
                result = result && fMinute == null;
            } else if (Unit.HOUR == unit) {
                result = result && fHour == null;
            } else if (Unit.DAY == unit) {
                result = result && fDay == null;
            } else if (Unit.MONTH == unit) {
                result = result && fMonth == null;
            } else if (Unit.YEAR == unit) {
                result = result && fYear == null;
            }
        }
        return result;
    }

    public DateTime getStartOfDay() {
        ensureHasYearMonthDay();
        return getStartEndDateTime(fDay, 0, 0, 0, 0);
    }

    public DateTime getEndOfDay() {
        ensureHasYearMonthDay();
        return getStartEndDateTime(fDay, 23, 59, 59, 999999999);
    }

    public DateTime getStartOfMonth() {
        ensureHasYearMonthDay();
        return getStartEndDateTime(1, 0, 0, 0, 0);
    }

    public DateTime getEndOfMonth() {
        ensureHasYearMonthDay();
        return getStartEndDateTime(getNumDaysInMonth(), 23, 59, 59, 999999999);
    }

    public DateTime plus(Integer aNumYears, Integer aNumMonths, Integer aNumDays, Integer aNumHours, Integer aNumMinutes, Integer aNumSeconds, Integer aNumNanoseconds, DayOverflow aDayOverflow) {
        DateTimeInterval interval = new DateTimeInterval(this, aDayOverflow);
        return interval.plus(aNumYears, aNumMonths, aNumDays, aNumHours, aNumMinutes, aNumSeconds, aNumNanoseconds);
    }

    public DateTime minus(Integer aNumYears, Integer aNumMonths, Integer aNumDays, Integer aNumHours, Integer aNumMinutes, Integer aNumSeconds, Integer aNumNanoseconds, DayOverflow aDayOverflow) {
        return new DateTimeInterval(this, aDayOverflow).minus(aNumYears, aNumMonths, aNumDays, aNumHours, aNumMinutes, aNumSeconds, aNumNanoseconds);
    }

    public DateTime plusDays(Integer aNumDays) {
        ensureHasYearMonthDay();
        int thisJDAtNoon = getModifiedJulianDayNumber() + 1 + EPOCH_MODIFIED_JD;
        int resultJD = thisJDAtNoon + aNumDays;
        DateTime datePortion = fromJulianDayNumberAtNoon(resultJD);
        return new DateTime(datePortion.getYear(), datePortion.getMonth(), datePortion.getDay(), fHour, fMinute, fSecond, fNanosecond);
    }

    public DateTime minusDays(Integer aNumDays) {
        return plusDays(-1 * aNumDays);
    }

    public int numDaysFrom(DateTime aThat) {
        return aThat.getModifiedJulianDayNumber() - this.getModifiedJulianDayNumber();
    }

    public long numSecondsFrom(DateTime aThat) {
        long result = 0;
        aThat.ensureParsed();
        if (hasYearMonthDay() && aThat.hasYearMonthDay()) {
            result = numDaysFrom(aThat) * 86400;
        }
        return (result - this.numSecondsInTimePortion() + aThat.numSecondsInTimePortion());
    }

    public String format(String aFormat) {
        return new DateTimeFormatter(aFormat).format(this);
    }

    public String format(String aFormat, Locale aLocale) {
        return new DateTimeFormatter(aFormat, aLocale).format(this);
    }

    public String format(String aFormat, List<String> aMonths, List<String> aWeekdays, List<String> aAmPmIndicators) {
        return new DateTimeFormatter(aFormat, aMonths, aWeekdays, aAmPmIndicators).format(this);
    }

    public static DateTime now(TimeZone aTimeZone) {
        return forInstant(System.currentTimeMillis(), aTimeZone);
    }

    public static DateTime today(TimeZone aTimeZone) {
        return now(aTimeZone).truncate(Unit.DAY);
    }

    public boolean isInTheFuture(TimeZone aTimeZone) {
        return now(aTimeZone).lt(this);
    }

    public boolean isInThePast(TimeZone aTimeZone) {
        return now(aTimeZone).gt(this);
    }

    public DateTime changeTimeZone(TimeZone aFromTimeZone, TimeZone aToTimeZone) {
        DateTime result;
        ensureHasYearMonthDay();
        if (unitsAllAbsent(Unit.HOUR)) {
            throw new IllegalArgumentException("DateTime does not include the hour. Cannot change the time zone if no hour is present.");
        }
        Calendar fromDate = new GregorianCalendar(aFromTimeZone);
        fromDate.set(Calendar.YEAR, getYear());
        fromDate.set(Calendar.MONTH, getMonth() - 1);
        fromDate.set(Calendar.DAY_OF_MONTH, getDay());
        fromDate.set(Calendar.HOUR_OF_DAY, getHour());
        if (getMinute() != null) {
            fromDate.set(Calendar.MINUTE, getMinute());
        } else {
            fromDate.set(Calendar.MINUTE, 0);
        }
        fromDate.set(Calendar.SECOND, 0);
        fromDate.set(Calendar.MILLISECOND, 0);

        Calendar toDate = new GregorianCalendar(aToTimeZone);
        toDate.setTimeInMillis(fromDate.getTimeInMillis());
        Integer minute = getMinute() != null ? toDate.get(Calendar.MINUTE) : null;
        result = new DateTime(toDate.get(Calendar.YEAR), toDate.get(Calendar.MONTH) + 1, toDate.get(Calendar.DAY_OF_MONTH), toDate.get(Calendar.HOUR_OF_DAY), minute, getSecond(), getNanoseconds());
        return result;
    }

    public int compareTo(DateTime aThat) {
        if (this == aThat) return EQUAL;
        ensureParsed();
        aThat.ensureParsed();

        ModelUtil.NullsGo nullsGo = ModelUtil.NullsGo.FIRST;
        int comparison = ModelUtil.comparePossiblyNull(this.fYear, aThat.fYear, nullsGo);
        if (comparison != EQUAL) return comparison;

        comparison = ModelUtil.comparePossiblyNull(this.fMonth, aThat.fMonth, nullsGo);
        if (comparison != EQUAL) return comparison;

        comparison = ModelUtil.comparePossiblyNull(this.fDay, aThat.fDay, nullsGo);
        if (comparison != EQUAL) return comparison;

        comparison = ModelUtil.comparePossiblyNull(this.fHour, aThat.fHour, nullsGo);
        if (comparison != EQUAL) return comparison;

        comparison = ModelUtil.comparePossiblyNull(this.fMinute, aThat.fMinute, nullsGo);
        if (comparison != EQUAL) return comparison;

        comparison = ModelUtil.comparePossiblyNull(this.fSecond, aThat.fSecond, nullsGo);
        if (comparison != EQUAL) return comparison;

        comparison = ModelUtil.comparePossiblyNull(this.fNanosecond, aThat.fNanosecond, nullsGo);
        if (comparison != EQUAL) return comparison;

        return EQUAL;
    }

    @Override
    public boolean equals(Object aThat) {
        ensureParsed();
        Boolean result = ModelUtil.quickEquals(this, aThat);
        if (result == null) {
            DateTime that = (DateTime) aThat;
            that.ensureParsed();
            result = ModelUtil.equalsFor(this.getSignificantFields(), that.getSignificantFields());
        }
        return result;
    }

    @Override
    public int hashCode() {
        if (fHashCode == 0) {
            ensureParsed();
            fHashCode = ModelUtil.hashCodeFor(getSignificantFields());
        }
        return fHashCode;
    }

    @Override
    public String toString() {
        String result = "";
        if (Util.textHasContent(fDateTime)) {
            result = fDateTime;
        } else {
            String format = calcToStringFormat();
            if (format != null) {
                result = format(calcToStringFormat());
            } else {
                StringBuilder builder = new StringBuilder();
                addToString("Y", fYear, builder);
                addToString("M", fMonth, builder);
                addToString("D", fDay, builder);
                addToString("h", fHour, builder);
                addToString("m", fMinute, builder);
                addToString("s", fSecond, builder);
                addToString("f", fNanosecond, builder);
                result = builder.toString().trim();
            }
        }
        return result;
    }

    static final class ItemOutOfRange extends RuntimeException {
        ItemOutOfRange(String aMessage) {
            super(aMessage);
        }

        private static final long serialVersionUID = 4760138291907517660L;
    }

    static final class MissingItem extends RuntimeException {
        MissingItem(String aMessage) {
            super(aMessage);
        }

        private static final long serialVersionUID = -7359967338896127755L;
    }

    void ensureParsed() {
        if (!fIsAlreadyParsed) {
            parseDateTimeText();
        }
    }

    static Integer getNumDaysInMonth(Integer aYear, Integer aMonth) {
        Integer result = null;
        if (aYear != null && aMonth != null) {
            if (aMonth == 1) {
                result = 31;
            } else if (aMonth == 2) {
                result = isLeapYear(aYear) ? 29 : 28;
            } else if (aMonth == 3) {
                result = 31;
            } else if (aMonth == 4) {
                result = 30;
            } else if (aMonth == 5) {
                result = 31;
            } else if (aMonth == 6) {
                result = 30;
            } else if (aMonth == 7) {
                result = 31;
            } else if (aMonth == 8) {
                result = 31;
            } else if (aMonth == 9) {
                result = 30;
            } else if (aMonth == 10) {
                result = 31;
            } else if (aMonth == 11) {
                result = 30;
            } else if (aMonth == 12) {
                result = 31;
            } else {
                throw new AssertionError("Month is out of range 1..12:" + aMonth);
            }
        }
        return result;
    }

    static DateTime fromJulianDayNumberAtNoon(int aJDAtNoon) {
        int l = aJDAtNoon + 68569;
        int n = (4 * l) / 146097;
        l = l - (146097 * n + 3) / 4;
        int i = (4000 * (l + 1)) / 1461001;
        l = l - (1461 * i) / 4 + 31;
        int j = (80 * l) / 2447;
        int d = l - (2447 * j) / 80;
        l = j / 11;
        int m = j + 2 - (12 * l);
        int y = 100 * (n - 49) + i + l;
        return DateTime.forDateOnly(y, m, d);
    }

    private String fDateTime;
    private Integer fYear;
    private Integer fMonth;
    private Integer fDay;
    private Integer fHour;
    private Integer fMinute;
    private Integer fSecond;
    private Integer fNanosecond;

    private boolean fIsAlreadyParsed;

    private int fHashCode;

    private static final int EQUAL = 0;

    private static int EPOCH_MODIFIED_JD = 2400000;

    private static final int MILLION = 1000000;

    private static final long serialVersionUID = -1300068157085493891L;

    private int calculateJulianDayNumberAtNoon() {
        int y = fYear;
        int m = fMonth;
        int d = fDay;
        return (1461 * (y + 4800 + (m - 14) / 12)) / 4 + (367 * (m - 2 - 12 * ((m - 14) / 12))) / 12 - (3 * ((y + 4900 + (m - 14) / 12) / 100)) / 4 + d - 32075;
    }

    private void ensureHasYearMonthDay() {
        ensureParsed();
        if (!hasYearMonthDay()) {
            throw new MissingItem("DateTime does not include year/month/day.");
        }
    }

    private int numSecondsInTimePortion() {
        int result = 0;
        if (fSecond != null) {
            result = result + fSecond;
        }
        if (fMinute != null) {
            result = result + 60 * fMinute;
        }
        if (fHour != null) {
            result = result + 3600 * fHour;
        }
        return result;
    }

    private void validateState() {
        checkRange(fYear, 1, 9999, "Year");
        checkRange(fMonth, 1, 12, "Month");
        checkRange(fDay, 1, 31, "Day");
        checkRange(fHour, 0, 23, "Hour");
        checkRange(fMinute, 0, 59, "Minute");
        checkRange(fSecond, 0, 59, "Second");
        checkRange(fNanosecond, 0, 999999999, "Nanosecond");
        checkNumDaysInMonth(fYear, fMonth, fDay);
    }

    private void checkRange(Integer aValue, int aMin, int aMax, String aName) {
        if (aValue != null) {
            if (aValue < aMin || aValue > aMax) {
                throw new ItemOutOfRange(aName + " is not in the range " + aMin + ".." + aMax + ". Value is:" + aValue);
            }
        }
    }

    private void checkNumDaysInMonth(Integer aYear, Integer aMonth, Integer aDay) {
        if (hasYearMonthDay(aYear, aMonth, aDay) && aDay > getNumDaysInMonth(aYear, aMonth)) {
            throw new ItemOutOfRange("The day-of-the-month value '" + aDay + "' exceeds the number of days in the month: " + getNumDaysInMonth(aYear, aMonth));
        }
    }

    private void parseDateTimeText() {
        DateTimeParser parser = new DateTimeParser();
        DateTime dateTime = parser.parse(fDateTime);
        fYear = dateTime.fYear;
        fMonth = dateTime.fMonth;
        fDay = dateTime.fDay;
        fHour = dateTime.fHour;
        fMinute = dateTime.fMinute;
        fSecond = dateTime.fSecond;
        fNanosecond = dateTime.fNanosecond;
        validateState();
    }

    private boolean hasYearMonthDay(Integer aYear, Integer aMonth, Integer aDay) {
        return isPresent(aYear, aMonth, aDay);
    }

    private static boolean isLeapYear(Integer aYear) {
        boolean result = false;
        if (aYear % 100 == 0) {
            if (aYear % 400 == 0) {
                result = true;
            }
        } else if (aYear % 4 == 0) {
            result = true;
        }
        return result;
    }

    private Object[] getSignificantFields() {
        return new Object[]{fYear, fMonth, fDay, fHour, fMinute, fSecond, fNanosecond};
    }

    private void addToString(String aName, Object aValue, StringBuilder aBuilder) {
        aBuilder.append(aName).append(":").append(aValue).append(" ");
    }

    private boolean isPresent(Object... aItems) {
        boolean result = true;
        for (Object item : aItems) {
            if (item == null) {
                result = false;
                break;
            }
        }
        return result;
    }

    private DateTime getStartEndDateTime(Integer aDay, Integer aHour, Integer aMinute, Integer aSecond, Integer aNanosecond) {
        ensureHasYearMonthDay();
        return new DateTime(fYear, fMonth, aDay, aHour, aMinute, aSecond, aNanosecond);
    }

    private String calcToStringFormat() {
        String result = null;
        if (unitsAllPresent(Unit.YEAR) && unitsAllAbsent(Unit.MONTH, Unit.DAY, Unit.HOUR, Unit.MINUTE, Unit.SECOND, Unit.NANOSECONDS)) {
            result = "YYYY";
        } else if (unitsAllPresent(Unit.YEAR, Unit.MONTH) && unitsAllAbsent(Unit.DAY, Unit.HOUR, Unit.MINUTE, Unit.SECOND, Unit.NANOSECONDS)) {
            result = "YYYY-MM";
        } else if (unitsAllPresent(Unit.YEAR, Unit.MONTH, Unit.DAY) && unitsAllAbsent(Unit.HOUR, Unit.MINUTE, Unit.SECOND, Unit.NANOSECONDS)) {
            result = "YYYY-MM-DD";
        } else if (unitsAllPresent(Unit.YEAR, Unit.MONTH, Unit.DAY, Unit.HOUR) && unitsAllAbsent(Unit.MINUTE, Unit.SECOND, Unit.NANOSECONDS)) {
            result = "YYYY-MM-DD hh";
        } else if (unitsAllPresent(Unit.YEAR, Unit.MONTH, Unit.DAY, Unit.HOUR, Unit.MINUTE) && unitsAllAbsent(Unit.SECOND, Unit.NANOSECONDS)) {
            result = "YYYY-MM-DD hh:mm";
        } else if (unitsAllPresent(Unit.YEAR, Unit.MONTH, Unit.DAY, Unit.HOUR, Unit.MINUTE, Unit.SECOND) && unitsAllAbsent(Unit.NANOSECONDS)) {
            result = "YYYY-MM-DD hh:mm:ss";
        } else if (unitsAllPresent(Unit.YEAR, Unit.MONTH, Unit.DAY, Unit.HOUR, Unit.MINUTE, Unit.SECOND, Unit.NANOSECONDS)) {
            result = "YYYY-MM-DD hh:mm:ss.fffffffff";
        } else if (unitsAllAbsent(Unit.YEAR, Unit.MONTH, Unit.DAY) && unitsAllPresent(Unit.HOUR, Unit.MINUTE, Unit.SECOND, Unit.NANOSECONDS)) {
            result = "hh:mm:ss.fffffffff";
        } else if (unitsAllAbsent(Unit.YEAR, Unit.MONTH, Unit.DAY, Unit.NANOSECONDS) && unitsAllPresent(Unit.HOUR, Unit.MINUTE, Unit.SECOND)) {
            result = "hh:mm:ss";
        } else if (unitsAllAbsent(Unit.YEAR, Unit.MONTH, Unit.DAY, Unit.SECOND, Unit.NANOSECONDS) && unitsAllPresent(Unit.HOUR, Unit.MINUTE)) {
            result = "hh:mm";
        }
        return result;
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
        validateState();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }
}
package vk.help.calender.format;

import androidx.annotation.NonNull;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.Locale;

import vk.help.calender.CalendarDay;

public class DateFormatDayFormatter implements DayFormatter {

    private final DateTimeFormatter dateFormat;

    DateFormatDayFormatter() {
        this(DateTimeFormatter.ofPattern(DEFAULT_FORMAT, Locale.getDefault()));
    }

    private DateFormatDayFormatter(@NonNull final DateTimeFormatter format) {
        this.dateFormat = format;
    }

    @Override
    @NonNull
    public String format(@NonNull final CalendarDay day) {
        return dateFormat.format(day.getDate());
    }
}

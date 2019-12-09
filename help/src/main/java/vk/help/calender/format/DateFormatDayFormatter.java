package vk.help.calender.format;

import androidx.annotation.NonNull;

import org.threeten.bp.format.DateTimeFormatter;

import java.text.DateFormat;
import java.util.Locale;

import vk.help.calender.CalendarDay;

/**
 * Format using a {@linkplain DateFormat} instance.
 */
public class DateFormatDayFormatter implements DayFormatter {

    private final DateTimeFormatter dateFormat;

    /**
     * Format using a default format
     */
    public DateFormatDayFormatter() {
        this(DateTimeFormatter.ofPattern(DEFAULT_FORMAT, Locale.getDefault()));
    }

    /**
     * Format using a specific {@linkplain DateFormat}
     *
     * @param format the format to use
     */
    public DateFormatDayFormatter(@NonNull final DateTimeFormatter format) {
        this.dateFormat = format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public String format(@NonNull final CalendarDay day) {
        return dateFormat.format(day.getDate());
    }
}

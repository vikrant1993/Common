package vk.help.calender.format;

import org.threeten.bp.format.DateTimeFormatter;

import vk.help.calender.CalendarDay;

public class DateFormatTitleFormatter implements TitleFormatter {

    private final DateTimeFormatter dateFormat;

    DateFormatTitleFormatter() {
        this(DateTimeFormatter.ofPattern(DEFAULT_FORMAT));
    }

    private DateFormatTitleFormatter(final DateTimeFormatter format) {
        this.dateFormat = format;
    }

    @Override
    public CharSequence format(final CalendarDay day) {
        return dateFormat.format(day.getDate());
    }
}

package vk.help.calender.format;

import androidx.annotation.NonNull;

import vk.help.calender.CalendarDay;

public interface DayFormatter {

    String DEFAULT_FORMAT = "d";

    DayFormatter DEFAULT = new DateFormatDayFormatter();

    @NonNull
    String format(@NonNull CalendarDay day);
}

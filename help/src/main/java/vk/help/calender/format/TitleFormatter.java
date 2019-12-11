package vk.help.calender.format;


import vk.help.calender.CalendarDay;

public interface TitleFormatter {

    String DEFAULT_FORMAT = "LLLL yyyy";

    TitleFormatter DEFAULT = new DateFormatTitleFormatter();

    CharSequence format(CalendarDay day);
}
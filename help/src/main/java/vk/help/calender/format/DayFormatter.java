package vk.help.calender.format;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;

import vk.help.calender.CalendarDay;
import vk.help.calender.MaterialCalendarView;

/**
 * Supply labels for a given day. Default implementation is to format using a {@linkplain SimpleDateFormat}
 */
public interface DayFormatter {

  /**
   * Default format for displaying the day.
   */
  String DEFAULT_FORMAT = "d";

  /**
   * Default implementation used by {@linkplain MaterialCalendarView}
   */
  DayFormatter DEFAULT = new DateFormatDayFormatter();

  /**
   * Format a given day into a string
   *
   * @param day the day
   * @return a label for the day
   */
  @NonNull
  String format(@NonNull CalendarDay day);
}

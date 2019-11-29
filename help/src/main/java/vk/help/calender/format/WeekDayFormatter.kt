package vk.help.calender.format

import org.threeten.bp.DayOfWeek

/**
 * Supply labels for a given day of the week.
 */
interface WeekDayFormatter {
    /**
     * Convert a given day of the week into a label.
     *
     * @param dayOfWeek the day of the week as returned by [DayOfWeek.getValue].
     * @return a label for the day of week.
     */
    fun format(dayOfWeek: DayOfWeek?): CharSequence?

    companion object {
        /**
         * Default implementation used by [MaterialCalendarView]
         */
        @kotlin.jvm.JvmField
        val DEFAULT: WeekDayFormatter = CalendarWeekDayFormatter()
    }
}
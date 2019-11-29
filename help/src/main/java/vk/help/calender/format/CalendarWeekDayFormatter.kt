package vk.help.calender.format

import org.threeten.bp.DayOfWeek
import org.threeten.bp.format.TextStyle
import java.util.*

/**
 * Format the day of the week with using [TextStyle.SHORT] by default.
 *
 * @see java.time.DayOfWeek.getDisplayName
 */
class CalendarWeekDayFormatter : WeekDayFormatter {
    /**
     * {@inheritDoc}
     */
    override fun format(dayOfWeek: DayOfWeek?): CharSequence? {
        return dayOfWeek!!.getDisplayName(
            TextStyle.SHORT,
            Locale.getDefault()
        )
    }
}
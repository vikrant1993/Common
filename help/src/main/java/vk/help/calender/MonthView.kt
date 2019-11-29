package vk.help.calender

import android.annotation.SuppressLint
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Display a month of [DayView]s and
 * seven [WeekDayView]s.
 */
@SuppressLint("ViewConstructor")
internal class MonthView(
    view: MaterialCalendarView,
    month: CalendarDay?,
    firstDayOfWeek: DayOfWeek?,
    showWeekDays: Boolean
) : CalendarPagerView(view, month, firstDayOfWeek, showWeekDays) {

    override fun buildDayViews(dayViews: Collection<DayView>, calendar: LocalDate) {
        var temp = calendar
        for (r in 0 until DEFAULT_MAX_WEEKS) {
            for (i in 0 until DEFAULT_DAYS_IN_WEEK) {
                addDayView(dayViews, temp)
                temp = temp.plusDays(1)
            }
        }
    }

    val month: CalendarDay
        get() = firstViewDay

    override fun isDayEnabled(day: CalendarDay): Boolean {
        return day.month == firstViewDay.month
    }

    override fun getRows(): Int {
        return if (showWeekDays) DEFAULT_MAX_WEEKS + DAY_NAMES_ROW else DEFAULT_MAX_WEEKS
    }
}
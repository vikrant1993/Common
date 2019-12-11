package vk.help.calender.format

import org.threeten.bp.DayOfWeek

interface WeekDayFormatter {
    fun format(dayOfWeek: DayOfWeek?): CharSequence?

    companion object {
        @kotlin.jvm.JvmField
        val DEFAULT: WeekDayFormatter = CalendarWeekDayFormatter()
    }
}
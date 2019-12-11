package vk.help.calender.format

import org.threeten.bp.DayOfWeek

class ArrayWeekDayFormatter(weekDayLabels: Array<CharSequence>?) :
    WeekDayFormatter {
    private val weekDayLabels: Array<CharSequence>
    override fun format(dayOfWeek: DayOfWeek?): CharSequence? {
        return weekDayLabels[dayOfWeek!!.value - 1]
    }

    init {
        requireNotNull(weekDayLabels) { "Cannot be null" }
        require(weekDayLabels.size == 7) { "Array must contain exactly 7 elements" }
        this.weekDayLabels = weekDayLabels
    }
}
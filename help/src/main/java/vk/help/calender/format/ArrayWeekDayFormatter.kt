package vk.help.calender.format

import org.threeten.bp.DayOfWeek

/**
 * Use an array to supply week day labels
 */
class ArrayWeekDayFormatter(weekDayLabels: Array<CharSequence>?) :
    WeekDayFormatter {
    private val weekDayLabels: Array<CharSequence>
    /**
     * {@inheritDoc}
     */
    override fun format(dayOfWeek: DayOfWeek?): CharSequence? {
        return weekDayLabels[dayOfWeek!!.value - 1]
    }

    /**
     * @param weekDayLabels an array of 7 labels, starting with Sunday
     */
    init {
        requireNotNull(weekDayLabels) { "Cannot be null" }
        require(weekDayLabels.size == 7) { "Array must contain exactly 7 elements" }
        this.weekDayLabels = weekDayLabels
    }
}
package vk.help.calendar

import android.view.View

import java.util.Date

abstract class CalendarListener {
    abstract fun onSelectDate(date: Date, view: View)

    open fun onLongClickDate(date: Date, view: View) {}

    open fun onChangeMonth(month: Int, year: Int) {}

    open fun onCalendarViewCreated() {}
}
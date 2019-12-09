package vk.help.common

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.temp.*
import org.threeten.bp.LocalDate
import vk.help.MasterActivity
import vk.help.calender.CalendarDay
import vk.help.calender.MaterialCalendarView
import vk.help.calender.OnDateClickListener
import vk.help.calender.OnMonthChangedListener
import java.util.*

class InitialScreenActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)

        calendarView.setDateSelected(CalendarDay.from(LocalDate.of(2019, 11, 25)), true)
        calendarView.setDateSelected(CalendarDay.from(LocalDate.of(2019, 11, 15)), true)
        calendarView.setDateSelected(CalendarDay.from(LocalDate.of(2019, 11, 5)), true)

        calendarView.setOnMonthChangedListener(object : OnMonthChangedListener {
            override fun onMonthChanged(widget: MaterialCalendarView, date: CalendarDay) {
                val c = Calendar.getInstance()
                c.set(date.year, date.month - 1, date.day)
                val numOfDaysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH)
                val startDate = "1-${date.month}-${date.year}"
                val endDate = "$numOfDaysInMonth-${date.month}-${date.year}"
                loadData(startDate, endDate)
            }
        })

        calendarView.setOnDateClickListener(object : OnDateClickListener {
            override fun onDateClick(date: CalendarDay) {
                showToast(date.toString())
            }
        })
    }

    private fun loadData(startDate: String, endDate: String) {
        Log.i("Date Ranger", "start: $startDate end: $endDate")
    }
}
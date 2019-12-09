package vk.help.calender

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import org.threeten.bp.DayOfWeek
import vk.help.calender.format.WeekDayFormatter

/**
 * Display a day of the week
 */
@SuppressLint("ViewConstructor")
internal class WeekDayView(context: Context?, dayOfWeek: DayOfWeek?) : AppCompatTextView(context) {
    private var formatter = WeekDayFormatter.DEFAULT
    private var dayOfWeek: DayOfWeek? = null

    fun setWeekDayFormatter(formatter: WeekDayFormatter?) {
        this.formatter = formatter ?: WeekDayFormatter.DEFAULT
        setDayOfWeek(dayOfWeek)
    }

    fun setDayOfWeek(dayOfWeek: DayOfWeek?) {
        this.dayOfWeek = dayOfWeek
        text = formatter.format(dayOfWeek)
    }

    init {
        gravity = Gravity.CENTER
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        setDayOfWeek(dayOfWeek)
    }
}
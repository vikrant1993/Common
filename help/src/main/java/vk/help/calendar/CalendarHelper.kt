package vk.help.calendar

import vk.help.calendar.date.DateTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CalendarHelper {

    private var yyyyMMddFormat: SimpleDateFormat? = null

    fun setup() {
        yyyyMMddFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    }

    fun getFullWeeks(month: Int, year: Int, startDayOfWeek: Int, sixWeeksInCalendar: Boolean): ArrayList<DateTime> {
        val datetimeList = ArrayList<DateTime>()
        val firstDateOfMonth = DateTime(year, month, 1, 0, 0, 0, 0)
        val lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.numDaysInMonth - 1)
        var weekdayOfFirstDate = firstDateOfMonth.weekDay
        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7
        }
        while (weekdayOfFirstDate > 0) {
            val dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate - startDayOfWeek)
            if (!dateTime.lt(firstDateOfMonth)) {
                break
            }
            datetimeList.add(dateTime)
            weekdayOfFirstDate--
        }

        for (i in 0 until lastDateOfMonth.day) {
            datetimeList.add(firstDateOfMonth.plusDays(i))
        }

        var endDayOfWeek = startDayOfWeek - 1
        if (endDayOfWeek == 0) {
            endDayOfWeek = 7
        }
        if (lastDateOfMonth.weekDay != endDayOfWeek) {
            var i = 1
            while (true) {
                val nextDay = lastDateOfMonth.plusDays(i)
                datetimeList.add(nextDay)
                i++
                if (nextDay.weekDay == endDayOfWeek) {
                    break
                }
            }
        }

        if (sixWeeksInCalendar) {
            val size = datetimeList.size
            val row = size / 7
            val numOfDays = (6 - row) * 7
            val lastDateTime = datetimeList[size - 1]
            for (i in 1..numOfDays) {
                val nextDateTime = lastDateTime.plusDays(i)
                datetimeList.add(nextDateTime)
            }
        }
        return datetimeList
    }

    fun convertDateToDateTime(date: Date): DateTime {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val javaMonth = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DATE)

        return DateTime(year, javaMonth + 1, day, 0, 0, 0, 0)
    }

    fun convertDateTimeToDate(dateTime: DateTime): Date {
        val year = dateTime.year!!
        val datetimeMonth = dateTime.month!!
        val day = dateTime.day!!
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, datetimeMonth - 1, day)
        return calendar.time
    }

    @Throws(ParseException::class)
    fun getDateFromString(dateString: String, dateFormat: String?): Date {
        val formatter: SimpleDateFormat? = if (dateFormat == null) {
            if (yyyyMMddFormat == null) {
                setup()
            }
            yyyyMMddFormat
        } else {
            SimpleDateFormat(dateFormat, Locale.ENGLISH)
        }
        return formatter!!.parse(dateString)!!
    }

    fun getDateTimeFromString(dateString: String, dateFormat: String): DateTime? {
        val date: Date
        try {
            date = getDateFromString(dateString, dateFormat)
            return convertDateToDateTime(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    fun convertToStringList(dateTimes: ArrayList<DateTime>): ArrayList<String> {
        val list = ArrayList<String>()
        for (dateTime in dateTimes) {
            list.add(dateTime.format("YYYY-MM-DD"))
        }
        return list
    }

}
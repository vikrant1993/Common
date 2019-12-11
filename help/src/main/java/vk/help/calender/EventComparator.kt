package vk.help.calender

import java.util.*

class EventComparator : Comparator<Event> {
    override fun compare(lhs: Event, rhs: Event): Int {
        return lhs.timeInMillis.compareTo(rhs.timeInMillis)
    }
}
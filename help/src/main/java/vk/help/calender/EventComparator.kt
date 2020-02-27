package vk.help.calender

class EventComparator : Comparator<Event> {
    override fun compare(lhs: Event, rhs: Event): Int {
        return lhs.timeInMillis.compareTo(rhs.timeInMillis)
    }
}
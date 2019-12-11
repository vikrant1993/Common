package vk.help.calender;


import java.util.Comparator;

public class EventComparator implements Comparator<Event> {

    @Override
    public int compare(Event lhs, Event rhs) {
        return Long.compare(lhs.getTimeInMillis(), rhs.getTimeInMillis());
    }
}
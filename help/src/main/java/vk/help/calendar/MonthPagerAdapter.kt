package vk.help.calendar

import java.util.ArrayList

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MonthPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var fragments: ArrayList<DateGridFragment>? = null
        get() {
            if (field == null) {
                this.fragments = ArrayList()
                for (i in 0 until count) {
                    field!!.add(DateGridFragment())
                }
            }
            return field
        }

    override fun getItem(position: Int): Fragment {
        return fragments!![position]
    }

    override fun getCount(): Int {
        return CalendarFragment.NUMBER_OF_PAGES
    }
}
package vk.help.calender

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CalendarPager : ViewPager {
    var isPagingEnabled = true

    constructor(context: Context) : super(context)
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return isPagingEnabled && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return isPagingEnabled && super.onTouchEvent(ev)
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return isPagingEnabled && super.canScrollVertically(direction)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return isPagingEnabled && super.canScrollHorizontally(direction)
    }
}
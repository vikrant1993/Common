package vk.help.calender;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * Custom ViewPager that allows swiping to be disabled.
 */
class CalendarPager extends ViewPager {

    private boolean pagingEnabled = true;

    public CalendarPager(@NonNull final Context context) {
        super(context);
    }

    public CalendarPager(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return pagingEnabled && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return pagingEnabled && super.onTouchEvent(ev);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return pagingEnabled && super.canScrollVertically(direction);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return pagingEnabled && super.canScrollHorizontally(direction);
    }
}
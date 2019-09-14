package vk.help.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import vk.help.calendar.date.DateTime;

public class InfiniteViewPager extends ViewPager {

    public static final int OFFSET = 1000;

    private ArrayList<DateTime> datesInMonth;

    private boolean sixWeeksInCalendar = false;

    private int rowHeight = 0;

    public boolean isSixWeeksInCalendar() {
        return sixWeeksInCalendar;
    }

    public ArrayList<DateTime> getDatesInMonth() {
        return datesInMonth;
    }

    public void setDatesInMonth(ArrayList<DateTime> datesInMonth) {
        this.datesInMonth = datesInMonth;
    }

    public void setSixWeeksInCalendar(boolean sixWeeksInCalendar) {
        this.sixWeeksInCalendar = sixWeeksInCalendar;
        rowHeight = 0;
    }

    public InfiniteViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InfiniteViewPager(Context context) {
        super(context);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        setCurrentItem(OFFSET);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return super.onInterceptTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int rows = datesInMonth.size() / 7;
        if (getChildCount() > 0 && rowHeight == 0) {
            View firstChild = getChildAt(0);
            int width = getMeasuredWidth();
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            firstChild.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            rowHeight = firstChild.getMeasuredHeight();
        }

        int calHeight = rowHeight * (sixWeeksInCalendar ? 6 : rows);
        calHeight -= 12;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(calHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
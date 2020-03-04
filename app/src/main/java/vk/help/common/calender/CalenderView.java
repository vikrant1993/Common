package vk.help.common.calender;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CalenderView extends CalendarView {

    private ArrayList<Long> eventsDate = new ArrayList<>();

    public CalenderView(@NonNull Context context) {
        super(context);
        init();
    }

    public CalenderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CalenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {

    }

    public void setEvents(ArrayList<Long> events) {
        this.eventsDate = events;
        clearAllEvents();
    }

    private void renderEvent() {

        for (Long singleEvent : eventsDate) {

        }
    }

    public void clearAllEvents() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawCircle();
//        compactCalendarController.onDraw(canvas);
    }

}

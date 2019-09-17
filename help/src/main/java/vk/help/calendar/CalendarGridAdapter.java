package vk.help.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vk.help.R;
import vk.help.calendar.date.DateTime;

public class CalendarGridAdapter extends BaseAdapter {
    protected ArrayList<DateTime> datetimeList;
    protected int month;
    protected int year;
    protected Context context;
    protected ArrayList<DateTime> disableDates;
    protected ArrayList<DateTime> selectedDates;

    protected Map<DateTime, Integer> disableDatesMap = new HashMap<>();
    protected Map<DateTime, Integer> selectedDatesMap = new HashMap<>();

    protected DateTime minDateTime;
    protected DateTime maxDateTime;
    protected DateTime today;
    protected int startDayOfWeek;
    protected boolean sixWeeksInCalendar;
    protected boolean squareTextViewCell;
    protected int themeResource;
    protected Resources resources;

    protected int defaultCellBackgroundRes = -1;
    protected ColorStateList defaultTextColorRes;

    protected Map<String, Object> caldroidData;
    protected Map<String, Object> extraData;

    protected LayoutInflater localInflater;

    public void setAdapterDateTime(DateTime dateTime) {
        this.month = dateTime.getMonth();
        this.year = dateTime.getYear();
        this.datetimeList = CalendarHelper.INSTANCE.getFullWeeks(this.month, this.year,
                startDayOfWeek, sixWeeksInCalendar);
    }

    public ArrayList<DateTime> getDatetimeList() {
        return datetimeList;
    }

    public DateTime getMinDateTime() {
        return minDateTime;
    }

    public void setMinDateTime(DateTime minDateTime) {
        this.minDateTime = minDateTime;
    }

    public DateTime getMaxDateTime() {
        return maxDateTime;
    }

    public void setMaxDateTime(DateTime maxDateTime) {
        this.maxDateTime = maxDateTime;
    }

    public ArrayList<DateTime> getDisableDates() {
        return disableDates;
    }

    public void setDisableDates(ArrayList<DateTime> disableDates) {
        this.disableDates = disableDates;
    }

    public ArrayList<DateTime> getSelectedDates() {
        return selectedDates;
    }

    public void setSelectedDates(ArrayList<DateTime> selectedDates) {
        this.selectedDates = selectedDates;
    }

    public int getThemeResource() {
        return themeResource;
    }

    public Map<String, Object> getCaldroidData() {
        return caldroidData;
    }

    public void setCaldroidData(Map<String, Object> caldroidData) {
        this.caldroidData = caldroidData;
        populateFromCaldroidData();
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    public CalendarGridAdapter(Context context, int month, int year, Map<String, Object> caldroidData, Map<String, Object> extraData) {
        super();
        this.month = month;
        this.year = year;
        this.context = context;
        this.caldroidData = caldroidData;
        this.extraData = extraData;
        this.resources = context.getResources();

        populateFromCaldroidData();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        localInflater = CalendarFragment.getThemeInflater(context, inflater, themeResource);
    }

    @SuppressWarnings("unchecked")
    private void populateFromCaldroidData() {
        disableDates = (ArrayList<DateTime>) caldroidData.get(CalendarFragment.DISABLE_DATES);
        if (disableDates != null) {
            disableDatesMap.clear();
            for (DateTime dateTime : disableDates) {
                disableDatesMap.put(dateTime, 1);
            }
        }

        selectedDates = (ArrayList<DateTime>) caldroidData.get(CalendarFragment.SELECTED_DATES);
        if (selectedDates != null) {
            selectedDatesMap.clear();
            for (DateTime dateTime : selectedDates) {
                selectedDatesMap.put(dateTime, 1);
            }
        }

        minDateTime = (DateTime) caldroidData
                .get(CalendarFragment._MIN_DATE_TIME);
        maxDateTime = (DateTime) caldroidData
                .get(CalendarFragment._MAX_DATE_TIME);
        startDayOfWeek = (Integer) caldroidData.get(CalendarFragment.START_DAY_OF_WEEK);
        sixWeeksInCalendar = (Boolean) caldroidData.get(CalendarFragment.SIX_WEEKS_IN_CALENDAR);
        squareTextViewCell = (Boolean) caldroidData.get(CalendarFragment.SQUARE_TEXT_VIEW_CELL);

        // Get theme
        themeResource = (Integer) caldroidData.get(CalendarFragment.THEME_RESOURCE);

        this.datetimeList = CalendarHelper.INSTANCE.getFullWeeks(this.month, this.year,
                startDayOfWeek, sixWeeksInCalendar);

        getDefaultResources();
    }

    private void getDefaultResources() {
        Context wrapped = new ContextThemeWrapper(context, themeResource);
        Resources.Theme theme = wrapped.getTheme();
        TypedValue styleCellVal = new TypedValue();
        if (squareTextViewCell) {
            theme.resolveAttribute(R.attr.CalendarSquareCell, styleCellVal, true);
        } else {
            theme.resolveAttribute(R.attr.CalendarNormalCell, styleCellVal, true);
        }
        TypedArray typedArray = wrapped.obtainStyledAttributes(styleCellVal.data, R.styleable.CalendarCell);
        defaultCellBackgroundRes = typedArray.getResourceId(R.styleable.CalendarCell_android_background, -1);
        defaultTextColorRes = typedArray.getColorStateList(R.styleable.CalendarCell_android_textColor);
        typedArray.recycle();
    }

    public void updateToday() {
        today = CalendarHelper.INSTANCE.convertDateToDateTime(new Date());
    }

    protected DateTime getToday() {
        if (today == null) {
            today = CalendarHelper.INSTANCE.convertDateToDateTime(new Date());
        }
        return today;
    }

    protected void setCustomResources(DateTime dateTime, View backgroundView, TextView textView) {
        Map<DateTime, Drawable> backgroundForDateTimeMap = (Map<DateTime, Drawable>) caldroidData
                .get(CalendarFragment._BACKGROUND_FOR_DATETIME_MAP);
        if (backgroundForDateTimeMap != null) {
            Drawable drawable = backgroundForDateTimeMap.get(dateTime);
            if (drawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    backgroundView.setBackground(drawable);
                } else {
                    backgroundView.setBackgroundDrawable(drawable);
                }
            }
        }

        Map<DateTime, Integer> textColorForDateTimeMap = (Map<DateTime, Integer>) caldroidData.get(CalendarFragment._TEXT_COLOR_FOR_DATETIME_MAP);
        if (textColorForDateTimeMap != null) {
            Integer textColorResource = textColorForDateTimeMap.get(dateTime);
            if (textColorResource != null) {
                textView.setTextColor(resources.getColor(textColorResource));
            }
        }
    }

    private void resetCustomResources(CellView cellView) {
        cellView.setBackgroundResource(defaultCellBackgroundRes);
        cellView.setTextColor(defaultTextColorRes);
    }

    protected void customizeTextView(int position, CellView cellView) {
        int topPadding = cellView.getPaddingTop();
        int leftPadding = cellView.getPaddingLeft();
        int bottomPadding = cellView.getPaddingBottom();
        int rightPadding = cellView.getPaddingRight();

        DateTime dateTime = this.datetimeList.get(position);

        cellView.resetCustomStates();
        resetCustomResources(cellView);

        if (dateTime.equals(getToday())) {
            cellView.addCustomState(CellView.Companion.getSTATE_TODAY());
        }

        if (dateTime.getMonth() != month) {
            cellView.addCustomState(CellView.Companion.getSTATE_PREV_NEXT_MONTH());
        }

        if ((minDateTime != null && dateTime.lt(minDateTime))
                || (maxDateTime != null && dateTime.gt(maxDateTime))
                || (disableDates != null && disableDatesMap
                .containsKey(dateTime))) {

            cellView.addCustomState(CellView.Companion.getSTATE_DISABLED());
        }

        if (selectedDates != null && selectedDatesMap.containsKey(dateTime)) {
            cellView.addCustomState(CellView.Companion.getSTATE_SELECTED());
        }

        cellView.refreshDrawableState();
        cellView.setText(String.valueOf(dateTime.getDay()));
        setCustomResources(dateTime, cellView, cellView);
        cellView.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
    }

    @Override
    public int getCount() {
        return this.datetimeList.size();
    }

    @Override
    public Object getItem(int position) {
        return datetimeList.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CellView cellView;
        if (convertView == null) {
            final int squareDateCellResource = squareTextViewCell ? R.layout.square_date_cell : R.layout.normal_date_cell;
            cellView = (CellView) localInflater.inflate(squareDateCellResource, parent, false);
        } else {
            cellView = (CellView) convertView;
        }

        customizeTextView(position, cellView);

        return cellView;
    }

}
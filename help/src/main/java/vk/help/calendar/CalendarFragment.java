package vk.help.calendar;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import vk.help.R;
import vk.help.calendar.date.DateTime;

public class CalendarFragment extends Fragment {

    private static int
            SUNDAY = 1,
            MONDAY = 2,
            TUESDAY = 3,
            WEDNESDAY = 4,
            THURSDAY = 5,
            FRIDAY = 6,
            SATURDAY = 7;

    private static final int MONTH_YEAR_FLAG = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;

    private Time firstMonthTime = new Time();
    private final StringBuilder monthYearStringBuilder = new StringBuilder(50);
    private Formatter monthYearFormatter = new Formatter(monthYearStringBuilder, Locale.getDefault());

    public final static int NUMBER_OF_PAGES = 4;

    public static int disabledBackgroundDrawable = -1;
    public static int disabledTextColor = Color.GRAY;

    private AppCompatImageButton leftArrowButton, rightArrowButton;
    private AppCompatTextView monthTitleTextView;
    private GridView weekdayGridView;
    private InfiniteViewPager dateViewPager;
    private DatePageChangeListener pageChangeListener;
    private ArrayList<DateGridFragment> fragments;

    private int themeResource = R.style.CalendarDefault;

    public final static String DIALOG_TITLE = "dialogTitle", MONTH = "month", YEAR = "year", SHOW_NAVIGATION_ARROWS = "showNavigationArrows", DISABLE_DATES = "disableDates",
            SELECTED_DATES = "selectedDates", MIN_DATE = "minDate", MAX_DATE = "maxDate", ENABLE_SWIPE = "enableSwipe", START_DAY_OF_WEEK = "startDayOfWeek",
            SIX_WEEKS_IN_CALENDAR = "sixWeeksInCalendar", ENABLE_CLICK_ON_DISABLED_DATES = "enableClickOnDisabledDates", SQUARE_TEXT_VIEW_CELL = "squareTextViewCell",
            THEME_RESOURCE = "themeResource";

    public final static String _MIN_DATE_TIME = "_minDateTime", _MAX_DATE_TIME = "_maxDateTime", _BACKGROUND_FOR_DATETIME_MAP = "_backgroundForDateTimeMap",
            _TEXT_COLOR_FOR_DATETIME_MAP = "_textColorForDateTimeMap";

    private String dialogTitle;
    protected int month = -1;
    protected int year = -1;
    protected ArrayList<DateTime> disableDates = new ArrayList<>();
    protected ArrayList<DateTime> selectedDates = new ArrayList<>();

    protected DateTime minDateTime;
    protected DateTime maxDateTime;
    protected ArrayList<DateTime> dateInMonthsList;

    protected Map<String, Object> calendarData = new HashMap<>();
    protected Map<String, Object> extraData = new HashMap<>();
    protected Map<DateTime, Drawable> backgroundForDateTimeMap = new HashMap<>();
    protected Map<DateTime, Integer> textColorForDateTimeMap = new HashMap<>();
    protected int startDayOfWeek = SUNDAY;
    private boolean sixWeeksInCalendar = true;
    protected ArrayList<CalendarGridAdapter> datePagerAdapters = new ArrayList<>();
    protected boolean showNavigationArrows = true;
    protected boolean enableClickOnDisabledDates = false;
    protected boolean squareTextViewCell;
    private OnItemClickListener dateItemClickListener;
    private OnItemLongClickListener dateItemLongClickListener;
    private CalendarListener calendarListener;

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public CalendarListener getCalendarListener() {
        return calendarListener;
    }

    public CalendarGridAdapter getNewDatesGridAdapter(int month, int year) {
        return new CalendarGridAdapter(getActivity(), month, year, getCalendarData(), extraData);
    }

    public WeekdayArrayAdapter getNewWeekdayAdapter(int themeResource) {
        return new WeekdayArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, getDaysOfWeek(), themeResource);
    }

    public GridView getWeekdayGridView() {
        return weekdayGridView;
    }

    public ArrayList<DateGridFragment> getFragments() {
        return fragments;
    }

    public InfiniteViewPager getDateViewPager() {
        return dateViewPager;
    }

    public Map<DateTime, Drawable> getBackgroundForDateTimeMap() {
        return backgroundForDateTimeMap;
    }

    public Map<DateTime, Integer> getTextColorForDateTimeMap() {
        return textColorForDateTimeMap;
    }

    public void setMonthTitleTextView(AppCompatTextView monthTitleTextView) {
        this.monthTitleTextView = monthTitleTextView;
    }

    public ArrayList<CalendarGridAdapter> getDatePagerAdapters() {
        return datePagerAdapters;
    }

    public Map<String, Object> getCalendarData() {
        calendarData.clear();
        calendarData.put(DISABLE_DATES, disableDates);
        calendarData.put(SELECTED_DATES, selectedDates);
        calendarData.put(_MIN_DATE_TIME, minDateTime);
        calendarData.put(_MAX_DATE_TIME, maxDateTime);
        calendarData.put(START_DAY_OF_WEEK, startDayOfWeek);
        calendarData.put(SIX_WEEKS_IN_CALENDAR, sixWeeksInCalendar);
        calendarData.put(SQUARE_TEXT_VIEW_CELL, squareTextViewCell);
        calendarData.put(THEME_RESOURCE, themeResource);
        calendarData.put(_BACKGROUND_FOR_DATETIME_MAP, backgroundForDateTimeMap);
        calendarData.put(_TEXT_COLOR_FOR_DATETIME_MAP, textColorForDateTimeMap);
        return calendarData;
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    public void setBackgroundDrawableForDates(Map<Date, Drawable> backgroundForDateMap) {
        if (backgroundForDateMap == null || backgroundForDateMap.size() == 0) {
            return;
        }

        backgroundForDateTimeMap.clear();

        for (Date date : backgroundForDateMap.keySet()) {
            Drawable drawable = backgroundForDateMap.get(date);
            DateTime dateTime = CalendarHelper.INSTANCE.convertDateToDateTime(date);
            backgroundForDateTimeMap.put(dateTime, drawable);
        }
    }

    public void clearBackgroundDrawableForDates(List<Date> dates) {
        if (dates == null || dates.size() == 0) {
            return;
        }

        for (Date date : dates) {
            clearBackgroundDrawableForDate(date);
        }
    }

    public void setBackgroundDrawableForDateTimes(Map<DateTime, Drawable> backgroundForDateTimeMap) {
        this.backgroundForDateTimeMap.putAll(backgroundForDateTimeMap);
    }

    public void clearBackgroundDrawableForDateTimes(List<DateTime> dateTimes) {
        if (dateTimes == null || dateTimes.size() == 0) return;

        for (DateTime dateTime : dateTimes) {
            backgroundForDateTimeMap.remove(dateTime);
        }
    }

    public void setBackgroundDrawableForDate(Drawable drawable, Date date) {
        DateTime dateTime = CalendarHelper.INSTANCE.convertDateToDateTime(date);
        backgroundForDateTimeMap.put(dateTime, drawable);
    }

    public void clearBackgroundDrawableForDate(Date date) {
        DateTime dateTime = CalendarHelper.INSTANCE.convertDateToDateTime(date);
        backgroundForDateTimeMap.remove(dateTime);
    }

    public void clearAllBackgroundDrawable() {
        backgroundForDateTimeMap.clear();
    }

    public void setBackgroundDrawableForDateTime(Drawable drawable, DateTime dateTime) {
        backgroundForDateTimeMap.put(dateTime, drawable);
    }

    public void clearBackgroundDrawableForDateTime(DateTime dateTime) {
        backgroundForDateTimeMap.remove(dateTime);
    }

    public void setTextColorForDates(Map<Date, Integer> textColorForDateMap) {
        if (textColorForDateMap == null || textColorForDateMap.size() == 0) {
            return;
        }

        textColorForDateTimeMap.clear();

        for (Date date : textColorForDateMap.keySet()) {
            Integer resource = textColorForDateMap.get(date);
            DateTime dateTime = CalendarHelper.INSTANCE.convertDateToDateTime(date);
            textColorForDateTimeMap.put(dateTime, resource);
        }
    }

    public void clearTextColorForDates(List<Date> dates) {
        if (dates == null || dates.size() == 0) return;

        for (Date date : dates) {
            clearTextColorForDate(date);
        }
    }

    public void setTextColorForDateTimes(Map<DateTime, Integer> textColorForDateTimeMap) {
        this.textColorForDateTimeMap.putAll(textColorForDateTimeMap);
    }

    public void setTextColorForDate(int textColorRes, Date date) {
        DateTime dateTime = CalendarHelper.INSTANCE.convertDateToDateTime(date);
        textColorForDateTimeMap.put(dateTime, textColorRes);
    }

    public void clearTextColorForDate(Date date) {
        DateTime dateTime = CalendarHelper.INSTANCE.convertDateToDateTime(date);
        textColorForDateTimeMap.remove(dateTime);
    }

    public void setTextColorForDateTime(int textColorRes, DateTime dateTime) {
        textColorForDateTimeMap.put(dateTime, textColorRes);
    }

    public Bundle getSavedStates() {
        Bundle bundle = new Bundle();
        bundle.putInt(MONTH, month);
        bundle.putInt(YEAR, year);

        if (dialogTitle != null) {
            bundle.putString(DIALOG_TITLE, dialogTitle);
        }

        if (selectedDates != null && selectedDates.size() > 0) {
            bundle.putStringArrayList(SELECTED_DATES, CalendarHelper.INSTANCE.convertToStringList(selectedDates));
        }

        if (disableDates != null && disableDates.size() > 0) {
            bundle.putStringArrayList(DISABLE_DATES, CalendarHelper.INSTANCE.convertToStringList(disableDates));
        }

        if (minDateTime != null) {
            bundle.putString(MIN_DATE, minDateTime.format("YYYY-MM-DD"));
        }

        if (maxDateTime != null) {
            bundle.putString(MAX_DATE, maxDateTime.format("YYYY-MM-DD"));
        }

        bundle.putBoolean(SHOW_NAVIGATION_ARROWS, showNavigationArrows);
        bundle.putInt(START_DAY_OF_WEEK, startDayOfWeek);
        bundle.putBoolean(SIX_WEEKS_IN_CALENDAR, sixWeeksInCalendar);
        bundle.putInt(THEME_RESOURCE, themeResource);

        Bundle args = getArguments();
        if (args != null && args.containsKey(SQUARE_TEXT_VIEW_CELL)) {
            bundle.putBoolean(SQUARE_TEXT_VIEW_CELL, args.getBoolean(SQUARE_TEXT_VIEW_CELL));
        }

        return bundle;
    }

    public void saveStatesToKey(Bundle outState, String key) {
        outState.putBundle(key, getSavedStates());
    }

    public void restoreStatesFromKey(Bundle savedInstanceState, String key) {
        if (savedInstanceState != null && savedInstanceState.containsKey(key)) {
            Bundle caldroidSavedState = savedInstanceState.getBundle(key);
            setArguments(caldroidSavedState);
        }
    }

    public int getCurrentVirtualPosition() {
        int currentPage = dateViewPager.getCurrentItem();
        return pageChangeListener.getCurrent(currentPage);
    }

    public void moveToDate(Date date) {
        moveToDateTime(CalendarHelper.INSTANCE.convertDateToDateTime(date));
    }

    public void moveToDateTime(DateTime dateTime) {
        DateTime firstOfMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
        DateTime lastOfMonth = firstOfMonth.getEndOfMonth();
        if (dateTime.lt(firstOfMonth)) {
            DateTime firstDayNextMonth = dateTime.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
            pageChangeListener.setCurrentDateTime(firstDayNextMonth);
            int currentItem = dateViewPager.getCurrentItem();
            pageChangeListener.refreshAdapters(currentItem);
            dateViewPager.setCurrentItem(currentItem - 1);
        } else if (dateTime.gt(lastOfMonth)) {
            DateTime firstDayLastMonth = dateTime.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
            pageChangeListener.setCurrentDateTime(firstDayLastMonth);
            int currentItem = dateViewPager.getCurrentItem();
            pageChangeListener.refreshAdapters(currentItem);
            dateViewPager.setCurrentItem(currentItem + 1);
        }
    }

    public void setCalendarDate(Date date) {
        setCalendarDateTime(CalendarHelper.INSTANCE.convertDateToDateTime(date));
    }

    public void setCalendarDateTime(DateTime dateTime) {
        month = dateTime.getMonth();
        year = dateTime.getYear();
        if (calendarListener != null) {
            calendarListener.onChangeMonth(month, year);
        }
        refreshView();
    }

    public void prevMonth() {
        dateViewPager.setCurrentItem(pageChangeListener.getCurrentPage() - 1);
    }

    public void nextMonth() {
        dateViewPager.setCurrentItem(pageChangeListener.getCurrentPage() + 1);
    }

    public void clearDisableDates() {
        disableDates.clear();
    }

    public void setDisableDates(ArrayList<Date> disableDateList) {
        if (disableDateList == null || disableDateList.size() == 0) {
            return;
        }
        disableDates.clear();
        for (Date date : disableDateList) {
            DateTime dateTime = CalendarHelper.INSTANCE.convertDateToDateTime(date);
            disableDates.add(dateTime);
        }

    }

    public void setDisableDatesFromString(ArrayList<String> disableDateStrings) {
        setDisableDatesFromString(disableDateStrings, null);
    }

    public void setDisableDatesFromString(ArrayList<String> disableDateStrings, String dateFormat) {
        if (disableDateStrings == null) {
            return;
        }

        disableDates.clear();
        for (String dateString : disableDateStrings) {
            DateTime dateTime = CalendarHelper.INSTANCE.getDateTimeFromString(dateString, dateFormat);
            disableDates.add(dateTime);
        }
    }

    public void clearSelectedDates() {
        selectedDates.clear();
    }

    public void setSelectedDates(Date fromDate, Date toDate) {
        if (fromDate == null || toDate == null || fromDate.after(toDate)) {
            return;
        }

        selectedDates.clear();
        DateTime fromDateTime = CalendarHelper.INSTANCE.convertDateToDateTime(fromDate);
        DateTime toDateTime = CalendarHelper.INSTANCE.convertDateToDateTime(toDate);
        DateTime dateTime = fromDateTime;
        while (dateTime.lt(toDateTime)) {
            selectedDates.add(dateTime);
            dateTime = dateTime.plusDays(1);
        }
        selectedDates.add(toDateTime);
    }

    public void setSelectedDateStrings(String fromDateString, String toDateString, String dateFormat) throws ParseException {
        Date fromDate = CalendarHelper.INSTANCE.getDateFromString(fromDateString, dateFormat);
        Date toDate = CalendarHelper.INSTANCE.getDateFromString(toDateString, dateFormat);
        setSelectedDates(fromDate, toDate);
    }

    public void setSelectedDate(Date date) {
        if (date == null) {
            return;
        }
        DateTime dateTime = CalendarHelper.INSTANCE.convertDateToDateTime(date);
        selectedDates.add(dateTime);
    }

    public void clearSelectedDate(Date date) {
        if (date == null) {
            return;
        }
        DateTime dateTime = CalendarHelper.INSTANCE.convertDateToDateTime(date);
        selectedDates.remove(dateTime);
    }

    public boolean isSelectedDate(Date date) {
        if (date == null) {
            return false;
        }
        DateTime dateTime = CalendarHelper.INSTANCE.convertDateToDateTime(date);
        return selectedDates.contains(dateTime);
    }

    public boolean isShowNavigationArrows() {
        return showNavigationArrows;
    }

    public void setShowNavigationArrows(boolean showNavigationArrows) {
        this.showNavigationArrows = showNavigationArrows;
        leftArrowButton.setVisibility(showNavigationArrows ? View.VISIBLE : View.INVISIBLE);
        rightArrowButton.setVisibility(showNavigationArrows ? View.VISIBLE : View.INVISIBLE);
    }

    public void setMinDate(Date minDate) {
        if (minDate == null) {
            minDateTime = null;
        } else {
            minDateTime = CalendarHelper.INSTANCE.convertDateToDateTime(minDate);
        }
    }

    public boolean isSixWeeksInCalendar() {
        return sixWeeksInCalendar;
    }

    public void setSixWeeksInCalendar(boolean sixWeeksInCalendar) {
        this.sixWeeksInCalendar = sixWeeksInCalendar;
        dateViewPager.setSixWeeksInCalendar(sixWeeksInCalendar);
    }

    public void setMinDateFromString(String minDateString, String dateFormat) {
        if (minDateString == null) {
            setMinDate(null);
        } else {
            minDateTime = CalendarHelper.INSTANCE.getDateTimeFromString(minDateString, dateFormat);
        }
    }

    public void setMaxDate(Date maxDate) {
        if (maxDate == null) {
            maxDateTime = null;
        } else {
            maxDateTime = CalendarHelper.INSTANCE.convertDateToDateTime(maxDate);
        }
    }

    public void setMaxDateFromString(String maxDateString, String dateFormat) {
        if (maxDateString == null) {
            setMaxDate(null);
        } else {
            maxDateTime = CalendarHelper.INSTANCE.getDateTimeFromString(maxDateString, dateFormat);
        }
    }

    public void setCalendarListener(CalendarListener calendarListener) {
        this.calendarListener = calendarListener;
    }

    public OnItemClickListener getDateItemClickListener() {
        if (dateItemClickListener == null) {
            dateItemClickListener = (parent, view, position, id) -> {
                DateTime dateTime = dateInMonthsList.get(position);
                if (calendarListener != null) {
                    if (!enableClickOnDisabledDates) {
                        if ((minDateTime != null && dateTime.lt(minDateTime)) || (maxDateTime != null && dateTime.gt(maxDateTime)) || (disableDates != null && disableDates.indexOf(dateTime) != -1)) {
                            return;
                        }
                    }
                    Date date = CalendarHelper.INSTANCE.convertDateTimeToDate(dateTime);
                    calendarListener.onSelectDate(date, view);
                }
            };
        }
        return dateItemClickListener;
    }

    public OnItemLongClickListener getDateItemLongClickListener() {
        if (dateItemLongClickListener == null) {
            dateItemLongClickListener = (parent, view, position, id) -> {
                DateTime dateTime = dateInMonthsList.get(position);
                if (calendarListener != null) {
                    if (!enableClickOnDisabledDates) {
                        if ((minDateTime != null && dateTime.lt(minDateTime)) || (maxDateTime != null && dateTime.gt(maxDateTime)) || (disableDates != null && disableDates.indexOf(dateTime) != -1)) {
                            return false;
                        }
                    }
                    Date date = CalendarHelper.INSTANCE.convertDateTimeToDate(dateTime);
                    calendarListener.onLongClickDate(date, view);
                }
                return true;
            };
        }
        return dateItemLongClickListener;
    }

    protected void refreshMonthTitleTextView() {
        firstMonthTime.year = year;
        firstMonthTime.month = month - 1;
        firstMonthTime.monthDay = 15;
        long millis = firstMonthTime.toMillis(true);
        monthYearStringBuilder.setLength(0);
        String monthTitle = DateUtils.formatDateRange(getActivity(), monthYearFormatter, millis, millis, MONTH_YEAR_FLAG).toString();
        monthTitleTextView.setText(monthTitle.toUpperCase(Locale.getDefault()));
    }

    public void refreshView() {
        if (month == -1 || year == -1) {
            return;
        }
        refreshMonthTitleTextView();
        for (CalendarGridAdapter adapter : datePagerAdapters) {
            adapter.setCaldroidData(getCalendarData());
            adapter.setExtraData(extraData);
            adapter.updateToday();
            adapter.notifyDataSetChanged();
        }
    }

    protected void retrieveInitialArgs() {
        Bundle args = getArguments();
        CalendarHelper.INSTANCE.setup();
        if (args != null) {
            month = args.getInt(MONTH, -1);
            year = args.getInt(YEAR, -1);
            dialogTitle = args.getString(DIALOG_TITLE);


            startDayOfWeek = args.getInt(START_DAY_OF_WEEK, 1);
            if (startDayOfWeek > 7) {
                startDayOfWeek = startDayOfWeek % 7;
            }

            showNavigationArrows = args.getBoolean(SHOW_NAVIGATION_ARROWS, true);

            sixWeeksInCalendar = args.getBoolean(SIX_WEEKS_IN_CALENDAR, true);
            int orientation = getResources().getConfiguration().orientation;
            squareTextViewCell = args.getBoolean(SQUARE_TEXT_VIEW_CELL, orientation == Configuration.ORIENTATION_PORTRAIT);
            enableClickOnDisabledDates = args.getBoolean(ENABLE_CLICK_ON_DISABLED_DATES, false);
            ArrayList<String> disableDateStrings = args.getStringArrayList(DISABLE_DATES);
            if (disableDateStrings != null && disableDateStrings.size() > 0) {
                disableDates.clear();
                for (String dateString : disableDateStrings) {
                    DateTime dt = CalendarHelper.INSTANCE.getDateTimeFromString(dateString, null);
                    disableDates.add(dt);
                }
            }

            ArrayList<String> selectedDateStrings = args.getStringArrayList(SELECTED_DATES);
            if (selectedDateStrings != null && selectedDateStrings.size() > 0) {
                selectedDates.clear();
                for (String dateString : selectedDateStrings) {
                    DateTime dt = CalendarHelper.INSTANCE.getDateTimeFromString(dateString, null);
                    selectedDates.add(dt);
                }
            }

            String minDateTimeString = args.getString(MIN_DATE);
            if (minDateTimeString != null) {
                minDateTime = CalendarHelper.INSTANCE.getDateTimeFromString(minDateTimeString, null);
            }

            String maxDateTimeString = args.getString(MAX_DATE);
            if (maxDateTimeString != null) {
                maxDateTime = CalendarHelper.INSTANCE.getDateTimeFromString(maxDateTimeString, null);
            }

            themeResource = args.getInt(THEME_RESOURCE, R.style.CalendarDefault);
        }
        if (month == -1 || year == -1) {
            DateTime dateTime = DateTime.today(TimeZone.getDefault());
            month = dateTime.getMonth();
            year = dateTime.getYear();
        }
    }

    public static CalendarFragment newInstance(String dialogTitle, int month, int year) {
        CalendarFragment f = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, dialogTitle);
        args.putInt(MONTH, month);
        args.putInt(YEAR, year);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setThemeResource(int id) {
        themeResource = id;
    }

    public int getThemeResource() {
        return themeResource;
    }

    public static LayoutInflater getThemeInflater(Context context, LayoutInflater origInflater, int themeResource) {
        Context wrapped = new ContextThemeWrapper(context, themeResource);
        return origInflater.cloneInContext(wrapped);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        retrieveInitialArgs();
        LayoutInflater localInflater = getThemeInflater(getActivity(), inflater, themeResource);
        getActivity().setTheme(themeResource);
        View view = localInflater.inflate(R.layout.calendar_view, container, false);
        monthTitleTextView = view.findViewById(R.id.calendar_month_year_textview);
        leftArrowButton = view.findViewById(R.id.calendar_left_arrow);
        rightArrowButton = view.findViewById(R.id.calendar_right_arrow);
        leftArrowButton.setOnClickListener(v -> prevMonth());
        rightArrowButton.setOnClickListener(v -> nextMonth());
        setShowNavigationArrows(showNavigationArrows);
        weekdayGridView = view.findViewById(R.id.weekday_gridview);
        weekdayGridView.setAdapter(getNewWeekdayAdapter(themeResource));
        setupDateGridPages(view);
        refreshView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (calendarListener != null) {
            calendarListener.onCalendarViewCreated();
        }
    }

    protected int getGridViewRes() {
        return R.layout.date_grid_fragment;
    }

    private void setupDateGridPages(View view) {
        DateTime currentDateTime = new DateTime(year, month, 1, 0, 0, 0, 0);
        pageChangeListener = new DatePageChangeListener();
        pageChangeListener.setCurrentDateTime(currentDateTime);
        CalendarGridAdapter adapter0 = getNewDatesGridAdapter(currentDateTime.getMonth(), currentDateTime.getYear());
        dateInMonthsList = adapter0.getDatetimeList();
        DateTime nextDateTime = currentDateTime.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
        CalendarGridAdapter adapter1 = getNewDatesGridAdapter(nextDateTime.getMonth(), nextDateTime.getYear());
        DateTime next2DateTime = nextDateTime.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
        CalendarGridAdapter adapter2 = getNewDatesGridAdapter(next2DateTime.getMonth(), next2DateTime.getYear());
        DateTime prevDateTime = currentDateTime.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
        CalendarGridAdapter adapter3 = getNewDatesGridAdapter(prevDateTime.getMonth(), prevDateTime.getYear());

        datePagerAdapters.add(adapter0);
        datePagerAdapters.add(adapter1);
        datePagerAdapters.add(adapter2);
        datePagerAdapters.add(adapter3);

        pageChangeListener.setCalendarGridAdapters(datePagerAdapters);
        dateViewPager = view.findViewById(R.id.months_infinite_pager);
        dateViewPager.setSixWeeksInCalendar(sixWeeksInCalendar);
        dateViewPager.setDatesInMonth(dateInMonthsList);
        final MonthPagerAdapter pagerAdapter = new MonthPagerAdapter(getChildFragmentManager());

        fragments = pagerAdapter.getFragments();

        for (int i = 0; i < NUMBER_OF_PAGES; i++) {
            DateGridFragment dateGridFragment = fragments.get(i);
            CalendarGridAdapter adapter = datePagerAdapters.get(i);
            dateGridFragment.setGridViewRes(getGridViewRes());
            dateGridFragment.setGridAdapter(adapter);
            dateGridFragment.setOnItemClickListener(getDateItemClickListener());
            dateGridFragment.setOnItemLongClickListener(getDateItemLongClickListener());
        }

        dateViewPager.setAdapter(new InfinitePagerAdapter(pagerAdapter));
        dateViewPager.setOnPageChangeListener(pageChangeListener);
    }

    protected ArrayList<String> getDaysOfWeek() {
        ArrayList<String> list = new ArrayList<String>();
        SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.getDefault());
        DateTime sunday = new DateTime(2013, 2, 17, 0, 0, 0, 0);
        DateTime nextDay = sunday.plusDays(startDayOfWeek - SUNDAY);
        for (int i = 0; i < 7; i++) {
            Date date = CalendarHelper.INSTANCE.convertDateTimeToDate(nextDay);
            list.add(fmt.format(date).toUpperCase());
            nextDay = nextDay.plusDays(1);
        }

        return list;
    }

    public class DatePageChangeListener implements ViewPager.OnPageChangeListener {
        private int currentPage = InfiniteViewPager.OFFSET;
        private DateTime currentDateTime;
        private ArrayList<CalendarGridAdapter> calendarGridAdapters;

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public DateTime getCurrentDateTime() {
            return currentDateTime;
        }

        public void setCurrentDateTime(DateTime dateTime) {
            this.currentDateTime = dateTime;
            setCalendarDateTime(currentDateTime);
        }

        public ArrayList<CalendarGridAdapter> getCalendarGridAdapters() {
            return calendarGridAdapters;
        }

        public void setCalendarGridAdapters(
                ArrayList<CalendarGridAdapter> calendarGridAdapters) {
            this.calendarGridAdapters = calendarGridAdapters;
        }

        private int getNext(int position) {
            return (position + 1) % CalendarFragment.NUMBER_OF_PAGES;
        }

        private int getPrevious(int position) {
            return (position + 3) % CalendarFragment.NUMBER_OF_PAGES;
        }

        public int getCurrent(int position) {
            return position % CalendarFragment.NUMBER_OF_PAGES;
        }

        @Override
        public void onPageScrollStateChanged(int position) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void refreshAdapters(int position) {
            CalendarGridAdapter currentAdapter = calendarGridAdapters.get(getCurrent(position));
            CalendarGridAdapter prevAdapter = calendarGridAdapters.get(getPrevious(position));
            CalendarGridAdapter nextAdapter = calendarGridAdapters.get(getNext(position));
            if (position == currentPage) {
                currentAdapter.setAdapterDateTime(currentDateTime);
                currentAdapter.notifyDataSetChanged();
                prevAdapter.setAdapterDateTime(currentDateTime.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay));
                prevAdapter.notifyDataSetChanged();
                nextAdapter.setAdapterDateTime(currentDateTime.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay));
                nextAdapter.notifyDataSetChanged();
            } else if (position > currentPage) {
                currentDateTime = currentDateTime.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                nextAdapter.setAdapterDateTime(currentDateTime.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay));
                nextAdapter.notifyDataSetChanged();
            } else {
                currentDateTime = currentDateTime.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                prevAdapter.setAdapterDateTime(currentDateTime.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay));
                prevAdapter.notifyDataSetChanged();
            }
            currentPage = position;
        }

        @Override
        public void onPageSelected(int position) {
            refreshAdapters(position);
            setCalendarDateTime(currentDateTime);
            CalendarGridAdapter currentAdapter = calendarGridAdapters.get(position % CalendarFragment.NUMBER_OF_PAGES);
            dateInMonthsList.clear();
            dateInMonthsList.addAll(currentAdapter.getDatetimeList());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}

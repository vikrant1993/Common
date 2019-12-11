package vk.help.calender;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.WeekFields;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import vk.help.R;
import vk.help.calender.format.ArrayWeekDayFormatter;
import vk.help.calender.format.MonthArrayTitleFormatter;
import vk.help.calender.format.TitleFormatter;
import vk.help.calender.format.WeekDayFormatter;

/**
 * <p>
 * This class is a calendar widget for displaying and selecting dates.
 * The range of dates supported by this calendar is configurable.
 * A user can select a date by taping on it and can page the calendar to a desired date.
 * </p>
 * <p>
 * By default, the range of dates shown is from 200 years in the past to 200 years in the future.
 * This can be extended or shortened by configuring the minimum and maximum dates.
 * </p>
 * <p>
 * When selecting a date out of range, or when the range changes so the selection becomes outside,
 * The date closest to the previous selection will become selected. This will also trigger the
 * </p>
 * <p>
 * <strong>Note:</strong> if this view's size isn't divisible by 7,
 * the contents will be centered inside such that the days in the calendar are equally square.
 * For example, 600px isn't divisible by 7, so a tile size of 85 is choosen, making the calendar
 * 595px wide. The extra 5px are distributed left and right to get to 600px.
 * </p>
 */
public class MaterialCalendarView extends ViewGroup {

    public static final int INVALID_TILE_DIMENSION = -10;

    /**
     * {@linkplain IntDef} annotation for showOtherDates.
     *
     * @see #setShowOtherDates(int)
     * @see #getShowOtherDates()
     */
    @SuppressLint("UniqueConstants")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true, value = {
            SHOW_NONE, SHOW_ALL, SHOW_DEFAULTS,
            SHOW_OUT_OF_RANGE, SHOW_OTHER_MONTHS, SHOW_DECORATED_DISABLED
    })
    @interface ShowOtherDates {
    }

    /**
     * Do not show any non-enabled dates
     */
    public static final int SHOW_NONE = 0;

    /**
     * Show dates from the proceeding and successive months, in a disabled state.
     * This flag also enables the {@link #SHOW_OUT_OF_RANGE} flag to prevent odd blank areas.
     */
    public static final int SHOW_OTHER_MONTHS = 1;

    /**
     * Show dates that are outside of the min-max range.
     * This will only show days from the current month unless {@link #SHOW_OTHER_MONTHS} is enabled.
     */
    public static final int SHOW_OUT_OF_RANGE = 1 << 1;

    /**
     * Show days that are individually disabled with decorators.
     * This will only show dates in the current month and inside the minimum and maximum date range.
     */
    public static final int SHOW_DECORATED_DISABLED = 1 << 2;

    /**
     * The default flags for showing non-enabled dates. Currently only shows {@link
     * #SHOW_DECORATED_DISABLED}
     */
    public static final int SHOW_DEFAULTS = SHOW_DECORATED_DISABLED;

    /**
     * Show all the days
     */
    public static final int SHOW_ALL = SHOW_OTHER_MONTHS | SHOW_OUT_OF_RANGE | SHOW_DECORATED_DISABLED;

    /**
     * Use this orientation to animate the title vertically
     */
    public static final int VERTICAL = 0;

    /**
     * Use this orientation to animate the title horizontally
     */
    public static final int HORIZONTAL = 1;

    /**
     * Default tile size in DIPs. This is used in cases where there is no tile size specificed and the
     * view is set to {@linkplain ViewGroup.LayoutParams#WRAP_CONTENT WRAP_CONTENT}
     */
    public static final int DEFAULT_TILE_SIZE_DP = 44;
    private static final int DEFAULT_DAYS_IN_WEEK = 7;
    private static final int DAY_NAMES_ROW = 1;

    private final TitleChanger titleChanger;

    private final TextView title;
    private final ImageView buttonPast;
    private final ImageView buttonFuture;
    private final CalendarPager pager;
    private CalendarPagerAdapter<?> adapter;
    private CalendarDay currentMonth;
    private LinearLayout topbar;
    private CalendarMode calendarMode;
    /**
     * Used for the dynamic calendar height.
     */
    private boolean mDynamicHeightEnabled;

    private CalendarDay minDate = null;
    private CalendarDay maxDate = null;

    CharSequence calendarContentDescription;
    private int accentColor = 0;
    private int tileHeight = INVALID_TILE_DIMENSION;
    private int tileWidth = INVALID_TILE_DIMENSION;
    private boolean allowClickDaysOutsideCurrentMonth = true;
    private DayOfWeek firstDayOfWeek;
    private boolean showWeekDays;

    private State state;

    public MaterialCalendarView(Context context) {
        this(context, null);
    }

    public MaterialCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //If we're on good Android versions, turn off clipping for cool effects
            setClipToPadding(false);
            setClipChildren(false);
        } else {
            //Old Android does not like _not_ clipping view pagers, we need to clip
            setClipChildren(true);
            setClipToPadding(true);
        }

        @SuppressLint("InflateParams") final View content = LayoutInflater.from(getContext()).inflate(R.layout.calendar_view_layout, null, false);

        topbar = content.findViewById(R.id.header);
        buttonPast = content.findViewById(R.id.previous);
        title = content.findViewById(R.id.month_name);
        buttonFuture = content.findViewById(R.id.next);
        pager = new CalendarPager(getContext());

        OnClickListener onClickListener = v -> {
            if (v == buttonFuture) {
                pager.setCurrentItem(pager.getCurrentItem() + 1, true);
            } else if (v == buttonPast) {
                pager.setCurrentItem(pager.getCurrentItem() - 1, true);
            }
        };
        buttonPast.setOnClickListener(onClickListener);
        buttonFuture.setOnClickListener(onClickListener);

        titleChanger = new TitleChanger(title);

        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                titleChanger.setPreviousMonth(currentMonth);
                currentMonth = adapter.getItem(position);
                updateUi();

                dispatchOnMonthChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
        };
        pager.setOnPageChangeListener(pageChangeListener);
        pager.setPageTransformer(false, (page, position) -> {
            position = (float) Math.sqrt(1 - Math.abs(position));
            page.setAlpha(position);
        });

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MaterialCalendarView, 0, 0);
        try {
            int calendarModeIndex = a.getInteger(R.styleable.MaterialCalendarView_mcv_calendarMode, 0);
            int firstDayOfWeekInt = a.getInteger(R.styleable.MaterialCalendarView_mcv_firstDayOfWeek, -1);
            titleChanger.setOrientation(a.getInteger(R.styleable.MaterialCalendarView_mcv_titleAnimationOrientation, VERTICAL));

            if (firstDayOfWeekInt >= 1 && firstDayOfWeekInt <= 7) {
                firstDayOfWeek = DayOfWeek.of(firstDayOfWeekInt);
            } else {
                firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
            }

            showWeekDays = a.getBoolean(R.styleable.MaterialCalendarView_mcv_showWeekDays, true);

            newState().setFirstDayOfWeek(firstDayOfWeek)
                    .setCalendarDisplayMode(CalendarMode.values()[calendarModeIndex])
                    .setShowWeekDays(showWeekDays).commit();

            final int tileSize = a.getLayoutDimension(R.styleable.MaterialCalendarView_mcv_tileSize, INVALID_TILE_DIMENSION);
            if (tileSize > INVALID_TILE_DIMENSION) {
                setTileSize(tileSize);
            }

            final int tileWidth = a.getLayoutDimension(R.styleable.MaterialCalendarView_mcv_tileWidth, INVALID_TILE_DIMENSION);
            if (tileWidth > INVALID_TILE_DIMENSION) {
                setTileWidth(tileWidth);
            }

            final int tileHeight = a.getLayoutDimension(R.styleable.MaterialCalendarView_mcv_tileHeight, INVALID_TILE_DIMENSION);
            if (tileHeight > INVALID_TILE_DIMENSION) {
                setTileHeight(tileHeight);
            }

            setLeftArrow(a.getResourceId(R.styleable.MaterialCalendarView_mcv_leftArrow, R.drawable.left_arrow));
            setRightArrow(a.getResourceId(R.styleable.MaterialCalendarView_mcv_rightArrow, R.drawable.right_arrow));

            setSelectionColor(a.getColor(R.styleable.MaterialCalendarView_mcv_selectionColor, getThemeAccentColor(context)));

            CharSequence[] array = a.getTextArray(R.styleable.MaterialCalendarView_mcv_weekDayLabels);
            if (array != null) {
                setWeekDayFormatter(new ArrayWeekDayFormatter(array));
            }

            array = a.getTextArray(R.styleable.MaterialCalendarView_mcv_monthLabels);
            if (array != null) {
                setTitleFormatter(new MonthArrayTitleFormatter(array));
            }

            setHeaderTextAppearance(a.getResourceId(R.styleable.MaterialCalendarView_mcv_headerTextAppearance, R.style.TextAppearance_MaterialCalendarWidget_Header));
            setWeekDayTextAppearance(a.getResourceId(R.styleable.MaterialCalendarView_mcv_weekDayTextAppearance, R.style.TextAppearance_MaterialCalendarWidget_WeekDay));
            setDateTextAppearance(a.getResourceId(R.styleable.MaterialCalendarView_mcv_dateTextAppearance, R.style.TextAppearance_MaterialCalendarWidget_Date));
            //noinspection ResourceType
            setShowOtherDates(a.getInteger(R.styleable.MaterialCalendarView_mcv_showOtherDates, SHOW_DEFAULTS));
            setAllowClickDaysOutsideCurrentMonth(a.getBoolean(R.styleable.MaterialCalendarView_mcv_allowClickDaysOutsideCurrentMonth, true));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }

        // Adapter is created while parsing the TypedArray attrs, so setup has to happen after
        setupChildren();

        currentMonth = CalendarDay.today();
        setCurrentDate(currentMonth);

        if (isInEditMode()) {
            removeView(pager);
            MonthView monthView = new MonthView(this, currentMonth, getFirstDayOfWeek(), true);
            monthView.setSelectionColor(getSelectionColor());
            monthView.setDateTextAppearance(adapter.getDateTextAppearance());
            monthView.setWeekDayTextAppearance(adapter.getWeekDayTextAppearance());
            monthView.setShowOtherDates(getShowOtherDates());
            addView(monthView, new LayoutParams(calendarMode.getVisibleWeeksCount() + DAY_NAMES_ROW));
        }
    }

    private void setupChildren() {
        addView(topbar);
        pager.setId(R.id.mcv_pager);
        pager.setOffscreenPageLimit(1);
        addView(pager, new LayoutParams(showWeekDays ? calendarMode.getVisibleWeeksCount() + DAY_NAMES_ROW : calendarMode.getVisibleWeeksCount()));
    }

    private void updateUi() {
        titleChanger.change(currentMonth);
        enableView(buttonPast, canGoBack());
        enableView(buttonFuture, canGoForward());
    }

    public void goToPrevious() {
        if (canGoBack()) {
            pager.setCurrentItem(pager.getCurrentItem() - 1, true);
        }
    }

    /**
     * Go to next month or week without using the button {@link #buttonFuture}. Should only go to
     * next if {@link #canGoForward()} is enabled, meaning it's possible to go to the next month or
     * week.
     */
    public void goToNext() {
        if (canGoForward()) {
            pager.setCurrentItem(pager.getCurrentItem() + 1, true);
        }
    }


    @Deprecated
    public int getTileSize() {
        return Math.max(tileHeight, tileWidth);
    }

    /**
     * Set the size of each tile that makes up the calendar.
     * Each day is 1 tile, so the widget is 7 tiles wide and 7 or 8 tiles tall
     * depending on the visibility of the {@link #topbar}.
     *
     * @param size the new size for each tile in pixels
     */
    public void setTileSize(int size) {
        this.tileWidth = size;
        this.tileHeight = size;
        requestLayout();
    }

    /**
     * Set the height of each tile that makes up the calendar.
     *
     * @param height the new height for each tile in pixels
     */
    public void setTileHeight(int height) {
        this.tileHeight = height;
        requestLayout();
    }

    /**
     * Set the width of each tile that makes up the calendar.
     *
     * @param width the new width for each tile in pixels
     */
    public void setTileWidth(int width) {
        this.tileWidth = width;
        requestLayout();
    }

    private int dpToPx() {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MaterialCalendarView.DEFAULT_TILE_SIZE_DP, getResources().getDisplayMetrics());
    }

    /**
     * Whether the pager can page forward, meaning the future month is enabled.
     *
     * @return true if there is a future month that can be shown
     */
    public boolean canGoForward() {
        return pager.getCurrentItem() < (adapter.getCount() - 1);
    }

    /**
     * Whether the pager can page backward, meaning the previous month is enabled.
     *
     * @return true if there is a previous month that can be shown
     */
    public boolean canGoBack() {
        return pager.getCurrentItem() > 0;
    }

    /**
     * Pass all touch events to the pager so scrolling works on the edges of the calendar view.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return pager.dispatchTouchEvent(event);
    }

    /**
     * @return the color used for the selection
     */
    public int getSelectionColor() {
        return accentColor;
    }

    /**
     * @param color The selection color
     */
    public void setSelectionColor(int color) {
        if (color == 0) {
            if (!isInEditMode()) {
                return;
            } else {
                color = Color.GRAY;
            }
        }
        accentColor = color;
        adapter.setSelectionColor(color);
        invalidate();
    }

    /**
     * Get content description for calendar
     *
     * @return calendar's content description
     */
    public CharSequence getCalendarContentDescription() {
        return calendarContentDescription != null ? calendarContentDescription : getContext().getString(R.string.calendar);
    }

    /**
     * @param icon the new icon to use for the left paging arrow
     */
    public void setLeftArrow(@DrawableRes final int icon) {
        buttonPast.setImageResource(icon);
    }

    /**
     * @param icon the new icon to use for the right paging arrow
     */
    public void setRightArrow(@DrawableRes final int icon) {
        buttonFuture.setImageResource(icon);
    }

    /**
     * @param resourceId The text appearance resource id.
     */
    public void setHeaderTextAppearance(int resourceId) {
        title.setTextAppearance(getContext(), resourceId);
    }

    /**
     * @param resourceId The text appearance resource id.
     */
    public void setDateTextAppearance(int resourceId) {
        adapter.setDateTextAppearance(resourceId);
    }

    /**
     * @param resourceId The text appearance resource id.
     */
    public void setWeekDayTextAppearance(int resourceId) {
        adapter.setWeekDayTextAppearance(resourceId);
    }

    @Nullable
    public CalendarDay getSelectedDate() {
        List<CalendarDay> dates = adapter.getSelectedDates();
        if (dates.isEmpty()) {
            return null;
        } else {
            return dates.get(dates.size() - 1);
        }
    }

    /**
     * @return All of the currently selected dates.
     * @see MaterialCalendarView#getSelectedDate()
     */
    @NonNull
    public List<CalendarDay> getSelectedDates() {
        return adapter.getSelectedDates();
    }

    /**
     * Clear the currently selected date(s)
     */
    public void clearSelection() {
        adapter.clearSelections();
    }

    /**
     * @param day      a CalendarDay to change. Passing null does nothing
     * @param selected true if day should be selected, false to deselect
     */
    public void setDateSelected(@Nullable CalendarDay day, boolean selected) {
        if (day == null) {
            return;
        }
        adapter.setDateSelected(day, selected);
    }


    /**
     * Get the current first day of the month in month mode, or the first visible day of the
     * currently visible week.
     * <p>
     * For example, in week mode, if the week is July 29th, 2018 to August 4th, 2018,
     * this will return July 29th, 2018. If in month mode and the month is august, then this method
     * will return August 1st, 2018.
     *
     * @return The current month or week shown, will be set to first day of the month in month mode,
     * or the first visible day for a week.
     */
    public CalendarDay getCurrentDate() {
        return adapter.getItem(pager.getCurrentItem());
    }

    /**
     * Set the calendar to a specific month or week based on a date.
     * <p>
     * In month mode, the calendar will be set to the corresponding month.
     * <p>
     * In week mode, the calendar will be set to the corresponding week.
     *
     * @param day a CalendarDay to focus the calendar on. Null will do nothing
     */
    public void setCurrentDate(@Nullable CalendarDay day) {
        setCurrentDate(day, true);
    }

    /**
     * Set the calendar to a specific month or week based on a date.
     * <p>
     * In month mode, the calendar will be set to the corresponding month.
     * <p>
     * In week mode, the calendar will be set to the corresponding week.
     *
     * @param day             a CalendarDay to focus the calendar on. Null will do nothing
     * @param useSmoothScroll use smooth scroll when changing months.
     */
    public void setCurrentDate(@Nullable CalendarDay day, boolean useSmoothScroll) {
        if (day == null) {
            return;
        }
        int index = adapter.getIndexForDay(day);
        pager.setCurrentItem(index, useSmoothScroll);
        updateUi();
    }

    /**
     * @return the minimum selectable date for the calendar, if any
     */
    public CalendarDay getMinimumDate() {
        return minDate;
    }

    /**
     * @return the maximum selectable date for the calendar, if any
     */
    public CalendarDay getMaximumDate() {
        return maxDate;
    }

    /**
     * The default value is {@link #SHOW_DEFAULTS}, which currently is just {@link
     * #SHOW_DECORATED_DISABLED}.
     * This means that the default visible days are of the current month, in the min-max range.
     *
     * @param showOtherDates flags for showing non-enabled dates
     * @see #SHOW_ALL
     * @see #SHOW_NONE
     * @see #SHOW_DEFAULTS
     * @see #SHOW_OTHER_MONTHS
     * @see #SHOW_OUT_OF_RANGE
     * @see #SHOW_DECORATED_DISABLED
     */
    public void setShowOtherDates(@ShowOtherDates int showOtherDates) {
        adapter.setShowOtherDates(showOtherDates);
    }

    /**
     * Allow the user to click on dates from other months that are not out of range. Go to next or
     * previous month if a day outside the current month is clicked. The day still need to be
     * enabled to be selected.
     * Default value is true. Should be used with {@link #SHOW_OTHER_MONTHS}.
     *
     * @param enabled True to allow the user to click on a day outside current month displayed
     */
    public void setAllowClickDaysOutsideCurrentMonth(final boolean enabled) {
        this.allowClickDaysOutsideCurrentMonth = enabled;
    }

    /**
     * Set a formatter for weekday labels.
     *
     * @param formatter the new formatter, null for default
     */
    public void setWeekDayFormatter(WeekDayFormatter formatter) {
        adapter.setWeekDayFormatter(formatter == null ? WeekDayFormatter.DEFAULT : formatter);
    }

    /**
     * @return int of flags used for showing non-enabled dates
     * @see #SHOW_ALL
     * @see #SHOW_NONE
     * @see #SHOW_DEFAULTS
     * @see #SHOW_OTHER_MONTHS
     * @see #SHOW_OUT_OF_RANGE
     * @see #SHOW_DECORATED_DISABLED
     */
    @ShowOtherDates
    public int getShowOtherDates() {
        return adapter.getShowOtherDates();
    }

    /**
     * @return true if allow click on days outside current month displayed
     */
    public boolean allowClickDaysOutsideCurrentMonth() {
        return allowClickDaysOutsideCurrentMonth;
    }

    /**
     * Set a custom formatter for the month/year title
     *
     * @param titleFormatter new formatter to use, null to use default formatter
     */
    public void setTitleFormatter(@Nullable TitleFormatter titleFormatter) {
        titleChanger.setTitleFormatter(titleFormatter);
        adapter.setTitleFormatter(titleFormatter);
        updateUi();
    }

    /**
     * Sets the visibility {@link #topbar}, which contains
     * the previous month button {@link #buttonPast}, next month button {@link #buttonFuture},
     * and the month title {@link #title}.
     *
     * @param visible Boolean indicating if the topbar is visible
     */
    public void setTopbarVisible(boolean visible) {
        topbar.setVisibility(visible ? View.VISIBLE : View.GONE);
        requestLayout();
    }

    /**
     * @return true if the topbar is visible
     */
    public boolean getTopbarVisible() {
        return topbar.getVisibility() == View.VISIBLE;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.showOtherDates = getShowOtherDates();
        ss.allowClickDaysOutsideCurrentMonth = allowClickDaysOutsideCurrentMonth();
        ss.minDate = getMinimumDate();
        ss.maxDate = getMaximumDate();
        ss.selectedDates = getSelectedDates();
        ss.topbarVisible = getTopbarVisible();
        ss.dynamicHeightEnabled = mDynamicHeightEnabled;
        ss.currentMonth = currentMonth;
        ss.cacheCurrentPosition = state.cacheCurrentPosition;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        state().edit().setMinimumDate(ss.minDate).setMaximumDate(ss.maxDate).isCacheCalendarPositionEnabled(ss.cacheCurrentPosition).commit();

        setShowOtherDates(ss.showOtherDates);
        setAllowClickDaysOutsideCurrentMonth(ss.allowClickDaysOutsideCurrentMonth);
        clearSelection();
        for (CalendarDay calendarDay : ss.selectedDates) {
            setDateSelected(calendarDay, true);
        }
        setTopbarVisible(ss.topbarVisible);
        setDynamicHeightEnabled(ss.dynamicHeightEnabled);
        setCurrentDate(ss.currentMonth);
    }

    @Override
    protected void dispatchSaveInstanceState(@NonNull SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(@NonNull SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    private void setRangeDates(CalendarDay min, CalendarDay max) {
        CalendarDay c = currentMonth;
        adapter.setRangeDates(min, max);
        currentMonth = c;
        if (min != null) {
            currentMonth = min.isAfter(currentMonth) ? min : currentMonth;
        }
        int position = adapter.getIndexForDay(c);
        pager.setCurrentItem(position, false);
        updateUi();
    }

    public static class SavedState extends BaseSavedState {

        int showOtherDates = SHOW_DEFAULTS;
        boolean allowClickDaysOutsideCurrentMonth = true;
        CalendarDay minDate = null;
        CalendarDay maxDate = null;
        List<CalendarDay> selectedDates = new ArrayList<>();
        boolean topbarVisible = true;
        boolean dynamicHeightEnabled = false;
        CalendarDay currentMonth = null;
        boolean cacheCurrentPosition;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(showOtherDates);
            out.writeByte((byte) (allowClickDaysOutsideCurrentMonth ? 1 : 0));
            out.writeParcelable(minDate, 0);
            out.writeParcelable(maxDate, 0);
            out.writeTypedList(selectedDates);
            out.writeInt(topbarVisible ? 1 : 0);
            out.writeInt(dynamicHeightEnabled ? 1 : 0);
            out.writeParcelable(currentMonth, 0);
            out.writeByte((byte) (cacheCurrentPosition ? 1 : 0));
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState(Parcel in) {
            super(in);
            showOtherDates = in.readInt();
            allowClickDaysOutsideCurrentMonth = in.readByte() != 0;
            ClassLoader loader = CalendarDay.class.getClassLoader();
            minDate = in.readParcelable(loader);
            maxDate = in.readParcelable(loader);
            in.readTypedList(selectedDates, CalendarDay.CREATOR);
            topbarVisible = in.readInt() == 1;
            dynamicHeightEnabled = in.readInt() == 1;
            currentMonth = in.readParcelable(loader);
            cacheCurrentPosition = in.readByte() != 0;
        }
    }

    private static int getThemeAccentColor(Context context) {
        int colorAttr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAttr = android.R.attr.colorAccent;
        } else {
            //Get colorAccent defined for AppCompat
            colorAttr = context.getResources().getIdentifier("colorAccent", "attr", context.getPackageName());
        }
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, outValue, true);
        return outValue.data;
    }

    /**
     * @return The first day of the week as a {@linkplain Calendar} day constant.
     */
    public DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    public void setDynamicHeightEnabled(boolean useDynamicHeight) {
        this.mDynamicHeightEnabled = useDynamicHeight;
    }

    public void invalidateDecorators() {
        adapter.invalidateDecorators();
    }

    protected void dispatchOnDateSelected() {

    }

    protected void dispatchOnMonthChanged() {
    }

    protected void onDateClicked(final DayView dayView) {
        final CalendarDay currentDate = getCurrentDate();
        final CalendarDay selectedDate = dayView.getDate();
        final int currentMonth = currentDate.getMonth();
        final int selectedMonth = selectedDate.getMonth();

        if (calendarMode == CalendarMode.MONTHS && allowClickDaysOutsideCurrentMonth && currentMonth != selectedMonth) {
            if (currentDate.isAfter(selectedDate)) {
                goToPrevious();
            } else if (currentDate.isBefore(selectedDate)) {
                goToNext();
            }
        }
    }

    protected void onDateUnselected() {
        dispatchOnDateSelected();
    }

    public static boolean showOtherMonths(@ShowOtherDates int showOtherDates) {
        return (showOtherDates & SHOW_OTHER_MONTHS) != 0;
    }

    public static boolean showOutOfRange(@ShowOtherDates int showOtherDates) {
        return (showOtherDates & SHOW_OUT_OF_RANGE) != 0;
    }

    public static boolean showDecoratedDisabled(@ShowOtherDates int showOtherDates) {
        return (showOtherDates & SHOW_DECORATED_DISABLED) != 0;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(1);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int specHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        final int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        //We need to disregard padding for a while. This will be added back later
        final int desiredWidth = specWidthSize - getPaddingLeft() - getPaddingRight();
        final int desiredHeight = specHeightSize - getPaddingTop() - getPaddingBottom();

        final int weekCount = getWeekCountBasedOnMode();

        final int viewTileHeight = getTopbarVisible() ? (weekCount + 1) : weekCount;

        //Calculate independent tile sizes for later
        int desiredTileWidth = desiredWidth / DEFAULT_DAYS_IN_WEEK;
        int desiredTileHeight = desiredHeight / viewTileHeight;

        int measureTileSize = -1;
        int measureTileWidth = -1;
        int measureTileHeight = -1;

        if (this.tileWidth != INVALID_TILE_DIMENSION || this.tileHeight != INVALID_TILE_DIMENSION) {
            if (this.tileWidth > 0) {
                //We have a tileWidth set, we should use that
                measureTileWidth = this.tileWidth;
            } else {
                measureTileWidth = desiredTileWidth;
            }
            if (this.tileHeight > 0) {
                //We have a tileHeight set, we should use that
                measureTileHeight = this.tileHeight;
            } else {
                measureTileHeight = desiredTileHeight;
            }
        } else if (specWidthMode == MeasureSpec.EXACTLY || specWidthMode == MeasureSpec.AT_MOST) {
            if (specHeightMode == MeasureSpec.EXACTLY) {
                //Pick the smaller of the two explicit sizes
                measureTileSize = Math.min(desiredTileWidth, desiredTileHeight);
            } else {
                //Be the width size the user wants
                measureTileSize = desiredTileWidth;
            }
        } else if (specHeightMode == MeasureSpec.EXACTLY || specHeightMode == MeasureSpec.AT_MOST) {
            //Be the height size the user wants
            measureTileSize = desiredTileHeight;
        }

        if (measureTileSize > 0) {
            //Use measureTileSize if set
            measureTileHeight = measureTileSize;
            measureTileWidth = measureTileSize;
        } else {
            if (measureTileWidth <= 0) {
                //Set width to default if no value were set
                measureTileWidth = dpToPx();
            }
            if (measureTileHeight <= 0) {
                //Set height to default if no value were set
                measureTileHeight = dpToPx();
            }
        }

        //Calculate our size based off our measured tile size
        int measuredWidth = measureTileWidth * DEFAULT_DAYS_IN_WEEK;
        int measuredHeight = measureTileHeight * viewTileHeight;

        //Put padding back in from when we took it away
        measuredWidth += getPaddingLeft() + getPaddingRight();
        measuredHeight += getPaddingTop() + getPaddingBottom();

        //Contract fulfilled, setting out measurements
        setMeasuredDimension(
                //We clamp inline because we want to use un-clamped versions on the children
                clampSize(measuredWidth, widthMeasureSpec),
                clampSize(measuredHeight, heightMeasureSpec)
        );

        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            LayoutParams p = (LayoutParams) child.getLayoutParams();

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_DAYS_IN_WEEK * measureTileWidth, MeasureSpec.EXACTLY);

            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(p.height * measureTileHeight, MeasureSpec.EXACTLY);

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    private int getWeekCountBasedOnMode() {
        int weekCount = calendarMode.getVisibleWeeksCount();
        final boolean isInMonthsMode = calendarMode.equals(CalendarMode.MONTHS);
        if (isInMonthsMode && mDynamicHeightEnabled && adapter != null && pager != null) {
            final LocalDate cal = adapter.getItem(pager.getCurrentItem()).getDate();
            final LocalDate tempLastDay = cal.withDayOfMonth(cal.lengthOfMonth());
            weekCount = tempLastDay.get(WeekFields.of(firstDayOfWeek, 1).weekOfMonth());
        }
        return showWeekDays ? weekCount + DAY_NAMES_ROW : weekCount;
    }

    /**
     * Clamp the size to the measure spec.
     *
     * @param size Size we want to be
     * @param spec Measure spec to clamp against
     * @return the appropriate size to pass to {@linkplain View#setMeasuredDimension(int, int)}
     */
    private static int clampSize(int size, int spec) {
        int specMode = MeasureSpec.getMode(spec);
        int specSize = MeasureSpec.getSize(spec);
        switch (specMode) {
            case MeasureSpec.EXACTLY: {
                return specSize;
            }
            case MeasureSpec.AT_MOST: {
                return Math.min(size, specSize);
            }
            case MeasureSpec.UNSPECIFIED:
            default: {
                return size;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        final int parentLeft = getPaddingLeft();
        final int parentWidth = right - left - parentLeft - getPaddingRight();

        int childTop = getPaddingTop();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            int delta = (parentWidth - width) / 2;
            int childLeft = parentLeft + delta;

            child.layout(childLeft, childTop, childLeft + width, childTop + height);

            childTop += height;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(1);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(1);
    }

    @Override
    public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(MaterialCalendarView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(MaterialCalendarView.class.getName());
    }

    protected static class LayoutParams extends MarginLayoutParams {

        LayoutParams(int tileHeight) {
            super(MATCH_PARENT, tileHeight);
        }
    }

    public State state() {
        return state;
    }

    public StateBuilder newState() {
        return new StateBuilder();
    }

    public class State {
        private final CalendarMode calendarMode;
        private final DayOfWeek firstDayOfWeek;
        private final CalendarDay minDate;
        private final CalendarDay maxDate;
        private final boolean cacheCurrentPosition;
        private final boolean showWeekDays;

        private State(final StateBuilder builder) {
            calendarMode = builder.calendarMode;
            firstDayOfWeek = builder.firstDayOfWeek;
            minDate = builder.minDate;
            maxDate = builder.maxDate;
            cacheCurrentPosition = builder.cacheCurrentPosition;
            showWeekDays = builder.showWeekDays;
        }

        StateBuilder edit() {
            return new StateBuilder(this);
        }
    }

    public class StateBuilder {
        private CalendarMode calendarMode;
        private DayOfWeek firstDayOfWeek;
        private boolean cacheCurrentPosition = false;
        private CalendarDay minDate = null;
        private CalendarDay maxDate = null;
        private boolean showWeekDays;

        StateBuilder() {
            calendarMode = CalendarMode.MONTHS;
            firstDayOfWeek = LocalDate.now().with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).getDayOfWeek();
        }

        private StateBuilder(final State state) {
            calendarMode = state.calendarMode;
            firstDayOfWeek = state.firstDayOfWeek;
            minDate = state.minDate;
            maxDate = state.maxDate;
            cacheCurrentPosition = state.cacheCurrentPosition;
            showWeekDays = state.showWeekDays;
        }

        StateBuilder setFirstDayOfWeek(DayOfWeek day) {
            this.firstDayOfWeek = day;
            return this;
        }

        StateBuilder setCalendarDisplayMode(CalendarMode mode) {
            this.calendarMode = mode;
            return this;
        }

        StateBuilder setMinimumDate(@Nullable CalendarDay calendar) {
            minDate = calendar;
            return this;
        }

        StateBuilder setMaximumDate(@Nullable CalendarDay calendar) {
            maxDate = calendar;
            return this;
        }

        StateBuilder setShowWeekDays(boolean showWeekDays) {
            this.showWeekDays = showWeekDays;
            return this;
        }

        StateBuilder isCacheCalendarPositionEnabled(final boolean cacheCurrentPosition) {
            this.cacheCurrentPosition = cacheCurrentPosition;
            return this;
        }

        void commit() {
            MaterialCalendarView.this.commit(new State(this));
        }
    }

    private void commit(State state) {
        CalendarDay calendarDayToShow = null;
        if (adapter != null && state.cacheCurrentPosition) {
            calendarDayToShow = adapter.getItem(pager.getCurrentItem());
            if (calendarMode != state.calendarMode) {
                CalendarDay currentlySelectedDate = getSelectedDate();
                if (calendarMode == CalendarMode.MONTHS && currentlySelectedDate != null) {
                    // Going from months to weeks
                    LocalDate lastVisibleCalendar = calendarDayToShow.getDate();
                    CalendarDay lastVisibleCalendarDay = CalendarDay.from(lastVisibleCalendar.plusDays(1));
                    if (currentlySelectedDate.equals(calendarDayToShow) || (currentlySelectedDate.isAfter(calendarDayToShow) && currentlySelectedDate.isBefore(lastVisibleCalendarDay))) {
                        // Currently selected date is within view, so center on that
                        calendarDayToShow = currentlySelectedDate;
                    }
                } else if (calendarMode == CalendarMode.WEEKS) {
                    // Going from weeks to months
                    LocalDate lastVisibleCalendar = calendarDayToShow.getDate();
                    CalendarDay lastVisibleCalendarDay = CalendarDay.from(lastVisibleCalendar.plusDays(6));
                    if (currentlySelectedDate != null && (currentlySelectedDate.equals(calendarDayToShow) || currentlySelectedDate.equals(lastVisibleCalendarDay) || (currentlySelectedDate.isAfter(calendarDayToShow) && currentlySelectedDate.isBefore(lastVisibleCalendarDay)))) {
                        // Currently selected date is within view, so center on that
                        calendarDayToShow = currentlySelectedDate;
                    } else {
                        calendarDayToShow = lastVisibleCalendarDay;
                    }
                }
            }
        }

        this.state = state;
        // Save states parameters
        calendarMode = state.calendarMode;
        firstDayOfWeek = state.firstDayOfWeek;
        minDate = state.minDate;
        maxDate = state.maxDate;
        showWeekDays = state.showWeekDays;

        // Recreate adapter
        final CalendarPagerAdapter<?> newAdapter;
        switch (calendarMode) {
            case MONTHS:
                newAdapter = new MonthPagerAdapter(this);
                break;
            case WEEKS:
                newAdapter = new WeekPagerAdapter(this);
                break;
            default:
                throw new IllegalArgumentException("Provided display mode which is not yet implemented");
        }
        if (adapter == null) {
            adapter = newAdapter;
        } else {
            adapter = adapter.migrateStateAndReturn(newAdapter);
        }
        adapter.setShowWeekDays(showWeekDays);
        pager.setAdapter(adapter);
        setRangeDates(minDate, maxDate);

        // Reset height params after mode change
        int tileHeight = showWeekDays ? calendarMode.getVisibleWeeksCount() + DAY_NAMES_ROW : calendarMode.getVisibleWeeksCount();
        pager.setLayoutParams(new LayoutParams(tileHeight));

        setCurrentDate(CalendarDay.today());

        if (calendarDayToShow != null) {
            pager.setCurrentItem(adapter.getIndexForDay(calendarDayToShow));
        }

        invalidateDecorators();
        updateUi();
    }

    private static void enableView(final View view, final boolean enable) {
        view.setEnabled(enable);
        view.setAlpha(enable ? 1f : 0.1f);
    }
}
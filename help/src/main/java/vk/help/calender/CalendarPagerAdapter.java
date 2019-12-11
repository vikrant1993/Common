package vk.help.calender;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vk.help.calender.format.DayFormatter;
import vk.help.calender.format.TitleFormatter;
import vk.help.calender.format.WeekDayFormatter;

/**
 * Pager adapter backing the calendar view
 */
abstract class CalendarPagerAdapter<V extends CalendarPagerView> extends PagerAdapter {

    private final ArrayDeque<V> currentViews;

    final MaterialCalendarView mcv;
    private final CalendarDay today;

    @NonNull
    private TitleFormatter titleFormatter = TitleFormatter.DEFAULT;
    private Integer color = null;
    private Integer dateTextAppearance = null;
    private Integer weekDayTextAppearance = null;
    @MaterialCalendarView.ShowOtherDates
    private int showOtherDates = MaterialCalendarView.SHOW_DEFAULTS;
    private CalendarDay minDate = null;
    private CalendarDay maxDate = null;
    private DateRangeIndex rangeIndex;
    private List<CalendarDay> selectedDates = new ArrayList<>();
    private WeekDayFormatter weekDayFormatter = WeekDayFormatter.DEFAULT;
    private DayFormatter dayFormatter = DayFormatter.DEFAULT;
    private DayFormatter dayFormatterContentDescription = dayFormatter;
    private List<DayViewDecorator> decorators = new ArrayList<>();
    private List<DecoratorResult> decoratorResults = null;
    private boolean selectionEnabled = true;
    boolean showWeekDays;

    CalendarPagerAdapter(MaterialCalendarView mcv) {
        this.mcv = mcv;
        this.today = CalendarDay.today();
        currentViews = new ArrayDeque<>();
        currentViews.iterator();
        setRangeDates(null, null);
    }

    void invalidateDecorators() {
        decoratorResults = new ArrayList<>();
        for (DayViewDecorator decorator : decorators) {
            DayViewFacade facade = new DayViewFacade();
            decorator.decorate(facade);
            if (facade.isDecorated()) {
                decoratorResults.add(new DecoratorResult(decorator, facade));
            }
        }
        for (V pagerView : currentViews) {
            pagerView.setDayViewDecorators(decoratorResults);
        }
    }

    @Override
    public int getCount() {
        return rangeIndex.getCount();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleFormatter.format(getItem(position));
    }

    CalendarPagerAdapter<?> migrateStateAndReturn(CalendarPagerAdapter<?> newAdapter) {
        newAdapter.titleFormatter = titleFormatter;
        newAdapter.color = color;
        newAdapter.dateTextAppearance = dateTextAppearance;
        newAdapter.weekDayTextAppearance = weekDayTextAppearance;
        newAdapter.showOtherDates = showOtherDates;
        newAdapter.minDate = minDate;
        newAdapter.maxDate = maxDate;
        newAdapter.selectedDates = selectedDates;
        newAdapter.weekDayFormatter = weekDayFormatter;
        newAdapter.dayFormatter = dayFormatter;
        newAdapter.dayFormatterContentDescription = dayFormatterContentDescription;
        newAdapter.decorators = decorators;
        newAdapter.decoratorResults = decoratorResults;
        newAdapter.selectionEnabled = selectionEnabled;
        return newAdapter;
    }

    int getIndexForDay(CalendarDay day) {
        if (day == null) {
            return getCount() / 2;
        }
        if (minDate != null && day.isBefore(minDate)) {
            return 0;
        }
        if (maxDate != null && day.isAfter(maxDate)) {
            return getCount() - 1;
        }
        return rangeIndex.indexOf(day);
    }

    protected abstract V createView(int position);

    protected abstract int indexOf(V view);

    protected abstract boolean isInstanceOfView(Object object);

    protected abstract DateRangeIndex createRangeIndex(CalendarDay min, CalendarDay max);

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (!(isInstanceOfView(object))) {
            return POSITION_NONE;
        }
        CalendarPagerView pagerView = (CalendarPagerView) object;
        CalendarDay firstViewDay = pagerView.getFirstViewDay();
        if (firstViewDay == null) {
            return POSITION_NONE;
        }
        int index = indexOf((V) object);
        if (index < 0) {
            return POSITION_NONE;
        }
        return index;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        V pagerView = createView(position);
        pagerView.setContentDescription(mcv.getCalendarContentDescription());
        pagerView.setAlpha(0);
        pagerView.setSelectionEnabled(selectionEnabled);

        pagerView.setWeekDayFormatter(weekDayFormatter);
        pagerView.setDayFormatter(dayFormatter);
        pagerView.setDayFormatterContentDescription(dayFormatterContentDescription);
        if (color != null) {
            pagerView.setSelectionColor(color);
        }
        if (dateTextAppearance != null) {
            pagerView.setDateTextAppearance(dateTextAppearance);
        }
        if (weekDayTextAppearance != null) {
            pagerView.setWeekDayTextAppearance(weekDayTextAppearance);
        }
        pagerView.setShowOtherDates(showOtherDates);
        pagerView.setMinimumDate(minDate);
        pagerView.setMaximumDate(maxDate);
        pagerView.setSelectedDates(selectedDates);

        container.addView(pagerView);
        currentViews.add(pagerView);

        pagerView.setDayViewDecorators(decoratorResults);

        return pagerView;
    }

    void setShowWeekDays(boolean showWeekDays) {
        this.showWeekDays = showWeekDays;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        V pagerView = (V) object;
        currentViews.remove(pagerView);
        container.removeView(pagerView);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    void setTitleFormatter(@Nullable TitleFormatter titleFormatter) {
        this.titleFormatter = titleFormatter == null ? TitleFormatter.DEFAULT : titleFormatter;
    }

    void setSelectionColor(int color) {
        this.color = color;
        for (V pagerView : currentViews) {
            pagerView.setSelectionColor(color);
        }
    }

    void setDateTextAppearance(int taId) {
        if (taId == 0) {
            return;
        }
        this.dateTextAppearance = taId;
        for (V pagerView : currentViews) {
            pagerView.setDateTextAppearance(taId);
        }
    }

    void setShowOtherDates(@MaterialCalendarView.ShowOtherDates int showFlags) {
        this.showOtherDates = showFlags;
        for (V pagerView : currentViews) {
            pagerView.setShowOtherDates(showFlags);
        }
    }

    void setWeekDayFormatter(WeekDayFormatter formatter) {
        this.weekDayFormatter = formatter;
        for (V pagerView : currentViews) {
            pagerView.setWeekDayFormatter(formatter);
        }
    }

    @MaterialCalendarView.ShowOtherDates
    int getShowOtherDates() {
        return showOtherDates;
    }

    void setWeekDayTextAppearance(int taId) {
        if (taId == 0) {
            return;
        }
        this.weekDayTextAppearance = taId;
        for (V pagerView : currentViews) {
            pagerView.setWeekDayTextAppearance(taId);
        }
    }

    void setRangeDates(CalendarDay min, CalendarDay max) {
        this.minDate = min;
        this.maxDate = max;
        for (V pagerView : currentViews) {
            pagerView.setMinimumDate(min);
            pagerView.setMaximumDate(max);
        }

        if (min == null) {
            min = CalendarDay.from(today.getYear() - 200, today.getMonth(), today.getDay());
        }

        if (max == null) {
            max = CalendarDay.from(today.getYear() + 200, today.getMonth(), today.getDay());
        }

        rangeIndex = createRangeIndex(min, max);

        notifyDataSetChanged();
        invalidateSelectedDates();
    }

    DateRangeIndex getRangeIndex() {
        return rangeIndex;
    }

    void clearSelections() {
        selectedDates.clear();
        invalidateSelectedDates();
    }

    /**
     * Select or un-select a day.
     *
     * @param day      Day to select or un-select
     * @param selected Whether to select or un-select the day from the list.
     */
    void setDateSelected(CalendarDay day, boolean selected) {
        if (selected) {
            if (!selectedDates.contains(day)) {
                selectedDates.add(day);
                invalidateSelectedDates();
            }
        } else {
            if (selectedDates.contains(day)) {
                selectedDates.remove(day);
                invalidateSelectedDates();
            }
        }
    }

    private void invalidateSelectedDates() {
        validateSelectedDates();
        for (V pagerView : currentViews) {
            pagerView.setSelectedDates(selectedDates);
        }
    }

    private void validateSelectedDates() {
        for (int i = 0; i < selectedDates.size(); i++) {
            CalendarDay date = selectedDates.get(i);

            if ((minDate != null && minDate.isAfter(date)) || (maxDate != null && maxDate.isBefore(date))) {
                selectedDates.remove(i);
                mcv.onDateUnselected();
                i -= 1;
            }
        }
    }

    CalendarDay getItem(int position) {
        return rangeIndex.getItem(position);
    }

    @NonNull
    List<CalendarDay> getSelectedDates() {
        return Collections.unmodifiableList(selectedDates);
    }

    int getDateTextAppearance() {
        return dateTextAppearance == null ? 0 : dateTextAppearance;
    }

    int getWeekDayTextAppearance() {
        return weekDayTextAppearance == null ? 0 : weekDayTextAppearance;
    }
}
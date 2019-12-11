package vk.help.calender;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstraction layer to help in decorating Day views
 */
public class DayViewFacade {

    private boolean isDecorated;

    private Drawable backgroundDrawable = null;
    private Drawable selectionDrawable = null;
    private final LinkedList<Span> spans = new LinkedList<>();
    private boolean daysDisabled = false;

    DayViewFacade() {
        isDecorated = false;
    }

    /**
     * Set a drawable to draw behind everything else
     *
     * @param drawable Drawable to draw behind everything
     */
    private void setBackgroundDrawable(@NonNull Drawable drawable) {
        this.backgroundDrawable = drawable;
        isDecorated = true;
    }

    /**
     * Set a custom selection drawable
     * TODO: define states that can/should be used in StateListDrawables
     *
     * @param drawable the drawable for selection
     */
    private void setSelectionDrawable(@NonNull Drawable drawable) {
        selectionDrawable = drawable;
        isDecorated = true;
    }

    void reset() {
        backgroundDrawable = null;
        selectionDrawable = null;
        spans.clear();
        isDecorated = false;
        daysDisabled = false;
    }

    /**
     * Apply things set this to other
     *
     * @param other facade to apply our data to
     */
    void applyTo(DayViewFacade other) {
        if (selectionDrawable != null) {
            other.setSelectionDrawable(selectionDrawable);
        }
        if (backgroundDrawable != null) {
            other.setBackgroundDrawable(backgroundDrawable);
        }
        other.spans.addAll(spans);
        other.isDecorated |= this.isDecorated;
        other.daysDisabled = daysDisabled;
    }

    boolean isDecorated() {
        return isDecorated;
    }

    Drawable getSelectionDrawable() {
        return selectionDrawable;
    }

    Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    List<Span> getSpans() {
        return Collections.unmodifiableList(spans);
    }

    /**
     * Are days from this facade disabled
     *
     * @return true if disabled, false if not re-enabled
     */
    boolean areDaysDisabled() {
        return daysDisabled;
    }

    static class Span {

        final Object span;

        public Span(Object span) {
            this.span = span;
        }
    }
}
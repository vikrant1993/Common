package vk.help.fastscroll.viewprovider;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import vk.help.fastscroll.FastScroller;

public abstract class ScrollerViewProvider {

    private FastScroller scroller;
    private ViewBehavior handleBehavior;
    private ViewBehavior bubbleBehavior;

    public void setFastScroller(FastScroller scroller) {
        this.scroller = scroller;
    }

    protected Context getContext() {
        return scroller.getContext();
    }

    FastScroller getScroller() {
        return scroller;
    }

    public abstract View provideHandleView(ViewGroup container);

    public abstract View provideBubbleView(ViewGroup container);

    public abstract TextView provideBubbleTextView();

    public abstract int getBubbleOffset();

    @Nullable
    protected abstract ViewBehavior provideHandleBehavior();

    @Nullable
    protected abstract ViewBehavior provideBubbleBehavior();

    private ViewBehavior getHandleBehavior() {
        if (handleBehavior == null) handleBehavior = provideHandleBehavior();
        return handleBehavior;
    }

    private ViewBehavior getBubbleBehavior() {
        if (bubbleBehavior == null) bubbleBehavior = provideBubbleBehavior();
        return bubbleBehavior;
    }

    public void onHandleGrabbed() {
        if (getHandleBehavior() != null) getHandleBehavior().onHandleGrabbed();
        if (getBubbleBehavior() != null) getBubbleBehavior().onHandleGrabbed();
    }

    public void onHandleReleased() {
        if (getHandleBehavior() != null) getHandleBehavior().onHandleReleased();
        if (getBubbleBehavior() != null) getBubbleBehavior().onHandleReleased();
    }

    public void onScrollStarted() {
        if (getHandleBehavior() != null) getHandleBehavior().onScrollStarted();
        if (getBubbleBehavior() != null) getBubbleBehavior().onScrollStarted();
    }

    public void onScrollFinished() {
        if (getHandleBehavior() != null) getHandleBehavior().onScrollFinished();
        if (getBubbleBehavior() != null) getBubbleBehavior().onScrollFinished();
    }
}
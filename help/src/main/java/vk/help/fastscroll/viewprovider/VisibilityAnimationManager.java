package vk.help.fastscroll.viewprovider;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.view.View;

import androidx.annotation.AnimatorRes;

import vk.help.R;

public class VisibilityAnimationManager {

    protected final View view;

    private AnimatorSet hideAnimator;
    private AnimatorSet showAnimator;

    private float pivotXRelative;
    private float pivotYRelative;

    private VisibilityAnimationManager(final View view, @AnimatorRes int showAnimator, @AnimatorRes int hideAnimator, float pivotXRelative, float pivotYRelative, int hideDelay) {
        this.view = view;
        this.pivotXRelative = pivotXRelative;
        this.pivotYRelative = pivotYRelative;
        this.hideAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), hideAnimator);
        this.hideAnimator.setStartDelay(hideDelay);
        this.hideAnimator.setTarget(view);
        this.showAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), showAnimator);
        this.showAnimator.setTarget(view);
        this.hideAnimator.addListener(new AnimatorListenerAdapter() {
            boolean wasCanceled;
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!wasCanceled) view.setVisibility(View.INVISIBLE);
                wasCanceled = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                wasCanceled = true;
            }
        });
        updatePivot();
    }

    public void show() {
        hideAnimator.cancel();
        if (view.getVisibility() == View.INVISIBLE) {
            view.setVisibility(View.VISIBLE);
            updatePivot();
            showAnimator.start();
        }
    }

    void hide() {
        updatePivot();
        hideAnimator.start();
    }

    private void updatePivot() {
        view.setPivotX(pivotXRelative * view.getMeasuredWidth());
        view.setPivotY(pivotYRelative * view.getMeasuredHeight());
    }

    public static abstract class AbsBuilder<T extends VisibilityAnimationManager> {
        protected final View view;
        int showAnimatorResource = R.animator.default_show;
        int hideAnimatorResource = R.animator.default_hide;
        int hideDelay = 1000;
        float pivotX = 0.5f;
        float pivotY = 0.5f;

        AbsBuilder(View view) {
            this.view = view;
        }

        public AbsBuilder<T> withShowAnimator(@AnimatorRes int showAnimatorResource) {
            this.showAnimatorResource = showAnimatorResource;
            return this;
        }

        public AbsBuilder<T> withHideAnimator(@AnimatorRes int hideAnimatorResource) {
            this.hideAnimatorResource = hideAnimatorResource;
            return this;
        }

        public AbsBuilder<T> withHideDelay(int hideDelay) {
            this.hideDelay = hideDelay;
            return this;
        }

        AbsBuilder<T> withPivotX() {
            this.pivotX = 1f;
            return this;
        }

        AbsBuilder<T> withPivotY() {
            this.pivotY = 1f;
            return this;
        }

        public abstract T build();
    }

    public static class Builder extends AbsBuilder<VisibilityAnimationManager> {

        public Builder(View view) {
            super(view);
        }

        public VisibilityAnimationManager build() {
            return new VisibilityAnimationManager(view, showAnimatorResource, hideAnimatorResource, pivotX, pivotY, hideDelay);
        }
    }
}
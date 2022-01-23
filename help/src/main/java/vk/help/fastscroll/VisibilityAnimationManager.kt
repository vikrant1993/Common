package vk.help.fastscroll

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.view.View
import androidx.annotation.AnimatorRes
import vk.help.R

open class VisibilityAnimationManager(
    private var view: View,
    @AnimatorRes showAnimator: Int,
    @AnimatorRes hideAnimator: Int,
    private var pivotXRelative: Float,
    private var pivotYRelative: Float,
    hideDelay: Int
) {

    private var hideAnimator: AnimatorSet =
        AnimatorInflater.loadAnimator(view.context, hideAnimator) as AnimatorSet
    private var showAnimator: AnimatorSet =
        AnimatorInflater.loadAnimator(view.context, showAnimator) as AnimatorSet

    init {
        this.hideAnimator.startDelay = hideDelay.toLong()
        this.showAnimator.setTarget(view)
        this.hideAnimator.addListener(object : AnimatorListenerAdapter() {

            var wasCanceled: Boolean = false

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                if (!wasCanceled) view.visibility = View.INVISIBLE
                wasCanceled = false
            }

            override fun onAnimationCancel(animation: Animator?) {
                super.onAnimationCancel(animation)
                wasCanceled = true
            }
        })
        updatePivot()
    }

    public fun show() {
        hideAnimator.cancel()
        if (view.visibility == View.INVISIBLE) {
            view.visibility = View.VISIBLE
            updatePivot()
            showAnimator.start()
        }
    }

    fun hide() {
        updatePivot()
        hideAnimator.start()
    }

    private fun updatePivot() {
        view.pivotX = pivotXRelative * view.measuredWidth
        view.pivotY = pivotYRelative * view.measuredHeight
    }


    companion object {
        abstract class AbsBuilder<T : VisibilityAnimationManager>(var view: View) {
            var showAnimatorResource = R.animator.default_show
            var hideAnimatorResource = R.animator.default_hide
            var hideDelay = 1000
            var pivotX = 0.5f
            var pivotY = 0.5f

            fun withShowAnimator(@AnimatorRes showAnimatorResource: Int): AbsBuilder<T> {
                this.showAnimatorResource = showAnimatorResource
                return this
            }

            fun withHideAnimator(@AnimatorRes hideAnimatorResource: Int): AbsBuilder<T> {
                this.hideAnimatorResource = hideAnimatorResource
                return this
            }

            fun withHideDelay(hideDelay: Int): AbsBuilder<T> {
                this.hideDelay = hideDelay
                return this
            }

            fun withPivotX(): AbsBuilder<T> {
                this.pivotX = 1f
                return this
            }

            fun withPivotY(): AbsBuilder<T> {
                this.pivotY = 1f
                return this
            }

            abstract fun build(): T
        }

        class Builder(view: View) : AbsBuilder<VisibilityAnimationManager>(view) {
            override fun build(): VisibilityAnimationManager {
                return VisibilityAnimationManager(
                    view,
                    showAnimatorResource,
                    hideAnimatorResource,
                    pivotX,
                    pivotY,
                    hideDelay
                )
            }
        }
    }
}
package vk.help.calender

import android.view.animation.Animation

abstract class AnimationListener : Animation.AnimationListener {
    override fun onAnimationStart(animation: Animation) {}
    override fun onAnimationEnd(animation: Animation) {}
    override fun onAnimationRepeat(animation: Animation) {}
}
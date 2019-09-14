package vk.help.fastscroll.viewprovider

class DefaultBubbleBehavior internal constructor(private val animationManager: VisibilityAnimationManager) : ViewBehavior {

    override fun onHandleGrabbed() {
        animationManager.show()
    }

    override fun onHandleReleased() {
        animationManager.hide()
    }

    override fun onScrollStarted() {

    }

    override fun onScrollFinished() {

    }
}
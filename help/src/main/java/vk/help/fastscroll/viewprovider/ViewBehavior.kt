package vk.help.fastscroll.viewprovider

interface ViewBehavior {
    fun onHandleGrabbed()
    fun onHandleReleased()
    fun onScrollStarted()
    fun onScrollFinished()
}
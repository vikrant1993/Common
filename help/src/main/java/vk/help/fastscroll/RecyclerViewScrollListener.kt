package vk.help.fastscroll

import java.util.ArrayList
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewScrollListener internal constructor(private val scroller: FastScroller) : RecyclerView.OnScrollListener() {
    private val listeners = ArrayList<ScrollerListener>()
    private var oldScrollState = RecyclerView.SCROLL_STATE_IDLE

    fun addScrollerListener(listener: ScrollerListener) {
        listeners.add(listener)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newScrollState: Int) {
        super.onScrollStateChanged(recyclerView, newScrollState)
        if (newScrollState == RecyclerView.SCROLL_STATE_IDLE && oldScrollState != RecyclerView.SCROLL_STATE_IDLE) {
            scroller.viewProvider.onScrollFinished()
        } else if (newScrollState != RecyclerView.SCROLL_STATE_IDLE && oldScrollState == RecyclerView.SCROLL_STATE_IDLE) {
            scroller.viewProvider.onScrollStarted()
        }
        oldScrollState = newScrollState
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (scroller.shouldUpdateHandlePosition()) {
            updateHandlePosition(recyclerView)
        }
    }

    fun updateHandlePosition(rv: RecyclerView) {
        val relativePos: Float
        relativePos = if (scroller.isVertical) {
            val offset = rv.computeVerticalScrollOffset()
            val extent = rv.computeVerticalScrollExtent()
            val range = rv.computeVerticalScrollRange()
            offset / (range - extent).toFloat()
        } else {
            val offset = rv.computeHorizontalScrollOffset()
            val extent = rv.computeHorizontalScrollExtent()
            val range = rv.computeHorizontalScrollRange()
            offset / (range - extent).toFloat()
        }
        scroller.setScrollerPosition(relativePos)
        notifyListeners(relativePos)
    }

    private fun notifyListeners(relativePos: Float) {
        for (listener in listeners) listener.onScroll(relativePos)
    }

    interface ScrollerListener {
        fun onScroll(relativePos: Float)
    }
}
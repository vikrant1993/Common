package vk.help

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView

class HideFabOnScroll(val view: View) : RecyclerView.OnScrollListener() {
    private var scrollDist = 0
    private var fabMargin: Int = view.context.resources.getDimensionPixelSize(R.dimen.dimen_24dp)
    private var isVisible = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        //  Check scrolled distance against the minimum
        if (isVisible && scrollDist > MINIMUM) {
            //  Hide fab & reset scrollDist
            hide()
            scrollDist = 0
            isVisible = false
        } else if (!isVisible && scrollDist < -MINIMUM) {
            //  Show fab & reset scrollDist
            show()
            scrollDist = 0
            isVisible = true
        }//  -MINIMUM because scrolling up gives - dy values

        //  Whether we scroll up or down, calculate scroll distance
        if (isVisible && dy > 0 || !isVisible && dy < 0) {
            scrollDist += dy
        }
    }

    private fun show() {
        view.animate().translationY(0f).setInterpolator(DecelerateInterpolator(2f)).start()
    }

    private fun hide() {
        view.animate().translationY((view.height + fabMargin).toFloat()).setInterpolator(AccelerateInterpolator(2f)).start()
    }

    companion object {
        private const val MINIMUM = 25f
    }


}
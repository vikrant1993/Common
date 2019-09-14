package vk.help.fastscroll

import android.graphics.drawable.Drawable
import android.view.View

class Utils {

    companion object {
        fun getViewRawY(view: View): Float {
            val location = IntArray(2)
            location[1] = view.y.toInt()
            (view.parent as View).getLocationInWindow(location)
            return location[1].toFloat()
        }

        fun getViewRawX(view: View): Float {
            val location = IntArray(2)
            location[0] = view.x.toInt()
            (view.parent as View).getLocationInWindow(location)
            return location[0].toFloat()
        }

        fun getValueInRange(max: Float, value: Float): Float {
            val minimum = Math.max(0.toFloat(), value)
            return Math.min(minimum, max)
        }

        fun setBackground(view: View, drawable: Drawable) {
            view.background = drawable
        }
    }
}
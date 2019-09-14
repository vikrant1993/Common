package vk.help.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.View

import androidx.appcompat.widget.AppCompatTextView
import vk.help.R

import java.util.ArrayList

open class CellView : AppCompatTextView {

    private var customStates: ArrayList<Int>? = ArrayList()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        if (null == customStates) customStates = ArrayList()
    }

    fun resetCustomStates() {
        customStates!!.clear()
    }

    fun addCustomState(state: Int) {
        if (!customStates!!.contains(state)) {
            customStates!!.add(state)
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        init()
        val customStateSize = customStates!!.size
        return if (customStateSize > 0) {
            val drawableState = super.onCreateDrawableState(extraSpace + customStateSize)
            val stateArray = IntArray(customStateSize)
            for ((i, state) in customStates!!.withIndex()) {
                stateArray[i] = state
            }
            View.mergeDrawableStates(drawableState, stateArray)
            drawableState
        } else {
            super.onCreateDrawableState(extraSpace)
        }
    }

    companion object {
        val STATE_TODAY = R.attr.state_date_today
        val STATE_SELECTED = R.attr.state_date_selected
        val STATE_DISABLED = R.attr.state_date_disabled
        val STATE_PREV_NEXT_MONTH = R.attr.state_date_prev_next_month
    }
}
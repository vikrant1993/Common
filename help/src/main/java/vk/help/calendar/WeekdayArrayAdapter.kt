package vk.help.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import vk.help.R

class WeekdayArrayAdapter(context: Context, textViewResourceId: Int, objects: List<String>, themeResource: Int) : ArrayAdapter<String>(context, textViewResourceId, objects) {

    private val localInflater: LayoutInflater

    init {
        localInflater = getLayoutInflater(getContext(), themeResource)
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = localInflater.inflate(R.layout.weekday_textview, parent, false) as TextView
        val item = getItem(position)
        textView.text = item
        return textView
    }

    private fun getLayoutInflater(context: Context, themeResource: Int): LayoutInflater {
        val wrapped = ContextThemeWrapper(context, themeResource)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return inflater.cloneInContext(wrapped)
    }
}
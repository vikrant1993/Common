package vk.help.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vk.help.AdapterView
import vk.help.MasterAdapter

class CustomMasterAdapter(
    recyclerView: RecyclerView,
    errorView: View?,
    adapterView: AdapterView
) : MasterAdapter(recyclerView, errorView, adapterView) {

    override fun setData(data: ArrayList<*>) {
        _list.clear()
        _list.addAll(data)
        updateUI()
    }
}
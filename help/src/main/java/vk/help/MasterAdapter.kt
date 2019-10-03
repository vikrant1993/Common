package vk.help

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MasterAdapter(
    private val recyclerView: RecyclerView,
    private val errorView: View?,
    private val adapterView: AdapterView
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        recyclerView.adapter = this
    }

    private var _list = ArrayList<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return adapterView.createChildView(parent)
    }

    override fun getItemCount(): Int {
        return _list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        adapterView.getChildView(holder, position)
    }

    fun setData(data: ArrayList<*>) {
        _list.clear()
        _list.addAll(data)
        notifyDataSetChanged()
        updateUI()
    }

    fun deleteItem(position: Int) {
        _list.removeAt(position)
        notifyItemRemoved(position)
        updateUI()
    }

    fun addItem(position: Int, item: Any) {
        _list.add(position, item)
        notifyItemInserted(position)
        updateUI()
    }

    fun updateItem(position: Int, item: Any) {
        _list[position] = item
        notifyItemChanged(position)
        updateUI()
    }

    fun getData(): ArrayList<Any> {
        return _list
    }

    private fun updateUI() {
        if (_list.isEmpty()) {
            recyclerView.visibility = View.GONE
            errorView?.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            errorView?.visibility = View.GONE
        }
    }
}

interface AdapterView {
    fun createChildView(parent: ViewGroup): RecyclerView.ViewHolder
    fun getChildView(holder: RecyclerView.ViewHolder, position: Int)
}
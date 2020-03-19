package vk.help

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
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

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return adapterView.createChildView(parent, viewType)
    }

    override fun getItemCount(): Int {
        return _list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        adapterView.getChildView(holder, position)
    }

    fun setData(data: ArrayList<*>) {
        if (_list.isEmpty()) {
            val diffResult = DiffUtil.calculateDiff(PostDiffCallback(_list, data))
            _list.clear()
            _list.addAll(data)
            diffResult.dispatchUpdatesTo(this)
        } else {
            _list.addAll(data)
            notifyDataSetChanged()
        }

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

    inner class PostDiffCallback(private val oldPosts: List<*>, private val newPosts: List<*>) :
        DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldPosts.size
        }

        override fun getNewListSize(): Int {
            return newPosts.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldPosts[oldItemPosition].hashCode() == newPosts[newItemPosition].hashCode()
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldPosts[oldItemPosition] == newPosts[newItemPosition];
        }
    }

}

interface AdapterView {
    fun createChildView(parent: ViewGroup, position: Int): RecyclerView.ViewHolder
    fun getChildView(holder: RecyclerView.ViewHolder, position: Int)
}
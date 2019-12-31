package vk.help.common

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.temp.*
import vk.help.AdapterView
import vk.help.MasterActivity
import vk.help.MasterAdapter

class InitialScreenActivity : MasterActivity() {
    lateinit var adapter: MasterAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)

        adapter = MasterAdapter(recyclerView, null, object : AdapterView {
            override fun createChildView(
                parent: ViewGroup,
                position: Int
            ): RecyclerView.ViewHolder {
                return if ((adapter.getData()[position] as Int) % 2 == 0) {
                    CustomViewOther(AppCompatButton(context))
                } else {
                    CustomView(AppCompatTextView(context))
                }
            }

            override fun getChildView(holder: RecyclerView.ViewHolder, position: Int) {
                if (holder is CustomView) {
                    holder.setData()
                } else if (holder is CustomViewOther) {
                    holder.setData()
                }
            }
        })

        updateDate.setOnClickListener {
            val list = ArrayList<Int>()
            list.add(0)
            list.add(1)
            list.add(2)
            list.add(3)
            list.add(3)
            list.add(4)
            adapter.setData(list)
        }
    }

    inner class CustomView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData() {
            (itemView as AppCompatTextView).text = "data is going here"
        }
    }

    inner class CustomViewOther(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData() {
            (itemView as AppCompatButton).text = "this is button"
        }
    }
}
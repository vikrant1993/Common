package vk.help.common

import android.os.Bundle
import kotlinx.android.synthetic.main.temp.*
import vk.help.MasterActivity


class InitialScreenActivity : MasterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)

        val events = ArrayList<Long>()
        events.add(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 2))
        calenderView.setEvents(events)

        submit.setOnClickListener {
        }
    }
}
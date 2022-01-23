package vk.help.common

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vk.help.base.AdapterView
import vk.help.base.MasterActivity
import vk.help.base.MasterAdapter

class InitialScreenActivity : MasterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAG.toToast()
    }
}
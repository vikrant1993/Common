package vk.help.common

import android.os.Bundle
import android.view.View
import vk.help.base.MasterActivity

class InitialScreenActivity : MasterActivity(),View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAG.toToast()
    }

    override fun onClick(view: View) {

    }

}
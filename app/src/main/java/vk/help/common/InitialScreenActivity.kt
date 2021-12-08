package vk.help.common

import android.os.Bundle
import vk.help.base.MasterActivity
import vk.help.base.MasterApplication

class InitialScreenActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)
        MasterApplication.context
    }
}
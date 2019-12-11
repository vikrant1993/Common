package vk.help.common

import android.os.Bundle
import vk.help.MasterActivity

class InitialScreenActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)
    }
}
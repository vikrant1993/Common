package vk.help.common

import android.os.Bundle
import vk.help.MasterActivity
import vk.help.cast
import vk.help.models.JList

class InitialScreenActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)

        val data: JList = PHONE_NUMBER.getBytes().cast()

    }
}
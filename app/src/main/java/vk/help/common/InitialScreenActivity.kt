package vk.help.common

import android.os.Bundle
import vk.help.MasterActivity
import vk.help.network.NetworkRequest

class InitialScreenActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkRequest(output = { output ->

        }).execute("")
    }
}
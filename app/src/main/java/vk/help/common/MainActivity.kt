package vk.help.common

import android.os.Bundle
import android.util.Log
import vk.help.MasterActivity
import vk.help.network.NetworkRequest

class MainActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NetworkRequest(listener = { result ->
            Log.i("output", result.data)
        }).execute("http//www.google.com")
    }
}
package vk.help.common

import android.net.Uri
import android.os.Bundle
import android.util.Log
import vk.help.MasterActivity
import vk.help.crop.Crop
import vk.help.network.NetworkRequest
import vk.help.network.NetworkResponse
import vk.help.network.ResultsListener
import java.io.File

class MainActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NetworkRequest(listener = { result ->
            Log.i("output",result.data)
        }).execute("http//www.google.com")

//        ( object : ResultsListener {
//            override fun onResultsSucceeded(result: NetworkResponse) {
//
//            }
//        }, "").execute("")

        Crop.of(
            Uri.fromFile(File("/sdcard/Download/images.jpeg")),
            Uri.fromFile(File("/sdcard/Download/images.jpeg"))
        )
            .withAspectRatio(16f, 9f)
            .start(this)

    }
}
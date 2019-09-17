package vk.help.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import vk.help.network.NetworkRequest
import vk.help.network.NetworkResponse
import vk.help.network.ResultsListener

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NetworkRequest(null, null, object : ResultsListener {
            override fun onResultsSucceeded(result: NetworkResponse) {

            }
        }, "").execute("")
    }
}
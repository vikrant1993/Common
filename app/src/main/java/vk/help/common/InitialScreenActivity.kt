package vk.help.common

import android.os.Bundle
import vk.help.MasterActivity
import vk.help.views.ExtendedWebView


class InitialScreenActivity : MasterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = ExtendedWebView(context)
        setContentView(webView)
        webView.loadUrl("file:///android_asset/test.html");
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {


    }
}
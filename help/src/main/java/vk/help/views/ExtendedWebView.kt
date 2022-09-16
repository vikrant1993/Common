package vk.help.views

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vk.help.Common.showToast
import vk.help.R
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ExtendedWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {
    private var filePathCallback: ValueCallback<Array<Uri?>>? = null
    private var pickFileType = "*/*"
    private val mNotifyManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val channelID = "WebDownloadID"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelID,
                "download notification",
                NotificationManager.IMPORTANCE_LOW
            )
            mNotifyManager.createNotificationChannel(notificationChannel)
        }

        settings.allowFileAccess = true
        settings.javaScriptEnabled = true
        addJavascriptInterface(WebViewJavaScriptInterface(), "android")
        webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                _filePathCallback: ValueCallback<Array<Uri?>>?,
                fileChooserParams: FileChooserParams
            ): Boolean {
                filePathCallback = _filePathCallback
                return checkPermissions()
            }
        }
    }

    private fun setFileType(_type: String) {
        pickFileType = _type
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionList = ArrayList<String>()
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (permissionList.size > 0) {
                ActivityCompat.requestPermissions(
                    (context as AppCompatActivity),
                    permissionList.toTypedArray(),
                    100
                )
                false
            } else {
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.type = pickFileType
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                (context as AppCompatActivity).startActivityForResult(
                    Intent.createChooser(
                        i,
                        "File Browser"
                    ), 101
                )
                true
            }
        } else {
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.type = pickFileType
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            (context as AppCompatActivity).startActivityForResult(
                Intent.createChooser(
                    i,
                    "File Browser"
                ), 101
            )
            true
        }
    }

    fun handleResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        val result =
            if (requestCode == 101 && resultCode == Activity.RESULT_OK && intent != null) intent.data else null
        onReceiveValue(arrayOf(result))
    }

    private fun onReceiveValue(result: Array<Uri?>) {
        try {
            filePathCallback?.onReceiveValue(result)
        } catch (e: Exception) {
            filePathCallback?.onReceiveValue(null)
            e.printStackTrace()
        } finally {
            filePathCallback = null
        }
    }

    inner class WebViewJavaScriptInterface {
        @JavascriptInterface
        fun downloadFile(url: String?) {
            val permissionList = ArrayList<String>()
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (permissionList.size > 0) {
                ActivityCompat.requestPermissions(
                    (context as AppCompatActivity),
                    permissionList.toTypedArray(),
                    1122
                )
            } else {
                FileDownload().execute(url)
            }
        }
    }

    internal inner class FileDownload : AsyncTask<String, Int, Boolean>() {

        private lateinit var mBuilder: NotificationCompat.Builder
        private var savePath: String? = null

        override fun onPreExecute() {
            super.onPreExecute()
            mBuilder = NotificationCompat.Builder(context, channelID)
            mBuilder.setContentTitle("File Download").setContentText("Download in progress")
                .setSmallIcon(
                    R.drawable.ic_cloud_download_black_24dp
                ).setDefaults(Notification.DEFAULT_ALL)

            mNotifyManager.notify(0, mBuilder.build())
            showToast(context, "Download File, See Notification")
        }

        override fun doInBackground(vararg params: String): Boolean {
            return try {
                var count: Int
                val url = URL(params[0])
                val f = File(Environment.getExternalStorageDirectory().path)
                if (f.exists()) {
                    val con = url.openConnection() as HttpURLConnection
                    val inputStream = con.inputStream
                    val path = url.path
                    val filename = path.substring(path.lastIndexOf('/') + 1)
                    savePath = f.path + "/Download/" + filename
                    val fos = FileOutputStream(savePath)
                    val lengthOfFile = con.contentLength
                    val data = ByteArray(1024)
                    var total: Long = 0
                    while (inputStream.read(data).also { count = it } != -1) {
                        total += count.toLong()
                        fos.write(data, 0, count)
                        publishProgress((total * 100 / lengthOfFile).toInt())
                        if (isCancelled) {
                            return false
                        }
                    }
                    inputStream.close()
                    fos.flush()
                    fos.close()
                    true
                } else {
                    Log.e("Error", "Not found: " + f.path)
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            try {
                if (savePath?.let { File(it).delete() } == true) {
                    showToast(context, "Canceled")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onProgressUpdate(vararg progress: Int?) {
            progress[0]?.let {
                mBuilder.setProgress(100, it, false)
                mNotifyManager.notify(0, mBuilder.build())
            }
        }

        override fun onPostExecute(result: Boolean) {
            mBuilder.setContentText(if (result) "Download complete" else "Failed")
            mBuilder.setProgress(0, 0, false)
            mNotifyManager.notify(0, mBuilder.build())
        }
    }


}
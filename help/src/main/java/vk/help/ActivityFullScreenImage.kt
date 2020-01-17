package vk.help

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_full_screen_image.*
import java.io.File


class ActivityFullScreenImage : MasterActivity() {

    private var downloadID: Long = 0

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                showToast("Download Completed")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)
        registerReceiver(
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
        saveImage.visibility = GONE
        val imageUrl = intent.getStringExtra(DATA)
        Glide.with(context).load(imageUrl).into(imageView)

        if (intent.hasExtra("enableDownload")) {
            if (intent.getBooleanExtra("enableDownload", false)) {
                saveImage.visibility = VISIBLE
            }
        }

        saveImage.setOnClickListener {
            val file = File(getExternalFilesDir(null), Common.nameFromURL(imageUrl))
            val request =
                DownloadManager.Request(Uri.parse(imageUrl))
                    .setTitle("Download") // Title of the Download Notification
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationUri(Uri.fromFile(file))
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
            downloadID =
                (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }
}
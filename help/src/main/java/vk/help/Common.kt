package vk.help

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import vk.help.base.MasterApplication
import java.io.*
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

object Common {

    lateinit var sharedPreferences: SharedPreferences
    private var toast: Toast? = null
    const val OnlyDatePattern = "dd-MMM-yyyy"
    const val ServerCommonDateTimePattern = "yyyy-MM-dd'T'HH:mm:ss"

    private val gson: Gson = Gson()

    fun convertDate(formatFrom: String, formatTo: String, value: String): String {
        try {
            val dateFormat = SimpleDateFormat(
                formatFrom.ifEmpty { ServerCommonDateTimePattern },
                Locale.getDefault()
            )
            val sourceDate = dateFormat.parse(value)
            val targetFormat = SimpleDateFormat(
                formatTo.ifEmpty { OnlyDatePattern },
                Locale.getDefault()
            )

            return if (sourceDate != null) {
                targetFormat.format(sourceDate)
            } else {
                value
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return value
        }
    }

    fun showToast(context: Context, message: String) {
        toast?.cancel()
        toast = Toast.makeText(context, toastMessageStyle(message), Toast.LENGTH_LONG)
        toast?.show()
    }

    private fun toastMessageStyle(message: String): String {
        if (MasterApplication.toastMessageStyle == MasterApplication.ToastMessageStyle.NONE) {
            return message
        } else if (MasterApplication.toastMessageStyle == MasterApplication.ToastMessageStyle.WORDS_CAPITAL) {
            return message.capitalize()
        } else if (MasterApplication.toastMessageStyle == MasterApplication.ToastMessageStyle.ALL_CAPITAL) {
            return message.uppercase(Locale.getDefault())
        } else if (MasterApplication.toastMessageStyle == MasterApplication.ToastMessageStyle.FIRST_WORD_CAPITAL) {
            if (message.isNotEmpty()) {
                return message.substring(0, 1).uppercase(Locale.getDefault()) + message.substring(
                    1
                ).lowercase(Locale.getDefault())
            }
        }
        return message
    }

    fun saveString(key: String, value: String) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String {
        return sharedPreferences.getString(key, "") ?: ""
    }

    fun formatSeconds(timeInSeconds: Long): String {
        val secondsLeft = timeInSeconds % 3600 % 60
        val minutes: Long = floor(timeInSeconds % 3600 / 60.toDouble()).toLong()
        val hours: Long = floor(timeInSeconds / 3600.toDouble()).toLong()
        val hh = if (hours < 10) "0$hours" else "" + hours
        val mm = if (minutes < 10) "0$minutes" else "" + minutes
        val ss = if (secondsLeft < 10) "0$secondsLeft" else "" + secondsLeft
        return "$hh:$mm:$ss"
    }

    fun formatSecondsInMinutes(timeInSeconds: Long): String {
        val secondsLeft = timeInSeconds % 3600 % 60
        val minutes: Long = floor(timeInSeconds % 3600 / 60.toDouble()).toLong()
        val mm = if (minutes < 10) "0$minutes" else "" + minutes
        val ss = if (secondsLeft < 10) "0$secondsLeft" else "" + secondsLeft
        return "$mm:$ss"
    }

    fun longLog(tag: String, str: String) {
        try {
            if (str.length > 4000) {
                Log.d(tag, str.substring(0, 4000))
                longLog(tag, str.substring(4000))
            } else Log.d(tag, str)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getJSON(obj: Any): String {
        return gson.toJson(obj)
    }

    fun getObject(jsonString: String, type: Type): Any {
        return gson.fromJson(jsonString, type)
    }

    fun getBytes(obj: Any): ByteArray {
        var bytes: ByteArray? = null
        try {
            val bos = ByteArrayOutputStream()
            val out = ObjectOutputStream(bos)
            out.writeObject(obj)
            out.flush()
            bytes = bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bytes!!
    }

    fun getObject(bytes: ByteArray): Any {
        var temp: Any? = null
        try {
            temp = ObjectInputStream(ByteArrayInputStream(bytes)).readObject()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return temp!!
    }

    fun setEnableViews(value: Boolean, vararg views: View) {
        for (view in views) {
            view.isEnabled = value
        }
    }

    fun getBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    fun nameFromURL(url: String?): String {
        return try {
            url?.substring(url.lastIndexOf('/') + 1) ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getFileSize(size: Long): String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        return when {
            size < sizeKb -> df.format(1) + " Kb"
            size < sizeMb -> df.format(size / sizeKb) + " Kb"
            size < sizeGb -> df.format(size / sizeMb) + " Mb"
            else -> ""
        }
    }

    fun openFile(url: File): Intent {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        //StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        val uri = Uri.fromFile(url)
        val intent = Intent(Intent.ACTION_VIEW)
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            intent.setDataAndType(uri, "application/msword")
        } else if (url.toString().contains(".pdf")) {
            intent.setDataAndType(uri, "application/pdf")
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            intent.setDataAndType(uri, "application/x-wav")
        } else if (url.toString().contains(".rtf")) {
            intent.setDataAndType(uri, "application/rtf")
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            intent.setDataAndType(uri, "audio/x-wav")
        } else if (url.toString().contains(".gif")) {
            intent.setDataAndType(uri, "image/gif")
        } else if (url.toString().contains(".jpg") || url.toString()
                .contains(".jpeg") || url.toString().contains(
                ".png"
            )
        ) {
            intent.setDataAndType(uri, "image/jpeg")
        } else if (url.toString().contains(".txt")) {
            intent.setDataAndType(uri, "text/plain")
        } else if (url.toString().contains(".3gp") || url.toString()
                .contains(".mpg") || url.toString().contains(
                ".mpeg"
            ) || url.toString().contains(".mpe") || url.toString()
                .contains(".mp4") || url.toString().contains(
                ".avi"
            )
        ) {
            intent.setDataAndType(uri, "video/*")
        } else {
            intent.setDataAndType(uri, "*/*")
        }
        return intent
    }
}
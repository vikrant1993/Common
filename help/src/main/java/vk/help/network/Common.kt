package vk.help.network

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import vk.help.Common
import vk.help.R
import java.io.*
import java.lang.Exception
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

object Common {

    private var toast: Toast? = null

    fun convertDate(formatFrom: String, formatTo: String, value: String): String {
        try {
            val dateFormat = SimpleDateFormat(
                if (formatFrom.isEmpty()) Common.ServerCommonDateTimePattern else formatFrom,
                Locale.getDefault()
            )
            val sourceDate = dateFormat.parse(value)
            val targetFormat = SimpleDateFormat(
                if (formatTo.isEmpty()) Common.OnlyDatePattern else formatTo,
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
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast(context)
        toast!!.setGravity(Gravity.BOTTOM, 0, 100)
        toast!!.duration = Toast.LENGTH_LONG
        toast!!.view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        toast!!.view.findViewById<AppCompatTextView>(R.id.toast_message).text = capitalize(message)
        toast!!.show()
    }

    fun showErrorToast(context: Context, message: String) {
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast(context)
        toast!!.setGravity(Gravity.BOTTOM, 0, 100)
        toast!!.duration = Toast.LENGTH_LONG
        toast!!.view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        toast!!.view.findViewById<AppCompatTextView>(R.id.toast_message).text = capitalize(message)
        toast!!.view.findViewById<AppCompatTextView>(R.id.toast_message)
            .setTextColor(ContextCompat.getColor(context, R.color.white))
        toast!!.view.findViewById<CardView>(R.id.cardView)
            .setCardBackgroundColor(ContextCompat.getColor(context, R.color.material_red))
        toast!!.show()
    }

    fun capitalize(value: String): String {
        val words = value.toLowerCase(Locale.getDefault()).trim().split(" ")
        val ret = StringBuilder()
        for (i in words.indices) {
            if (words[i].trim { it <= ' ' }.isNotEmpty()) {
                ret.append(Character.toUpperCase(words[i].trim { it <= ' ' }[0]))
                ret.append(words[i].trim { it <= ' ' }.substring(1))
                if (i < words.size - 1) {
                    ret.append(' ')
                }
            }
        }
        return ret.toString()
    }

    fun getJSON(obj: Any): String {
        return Gson().toJson(obj)
    }

    fun getObject(jsonString: String, type: Type): Any {
        return Gson().fromJson(jsonString, type)
    }

    fun getBytes(obj: Any): ByteArray {
        var bytes: ByteArray? = null
        try {
            val bos = ByteArrayOutputStream()
            val out = ObjectOutputStream(ByteArrayOutputStream())
            out.writeObject(obj)
            out.flush()
            bytes = bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bytes!!
    }

    fun getObject(bytes: ByteArray): Any {
        var obj: Any? = null
        val bis = ByteArrayInputStream(bytes)
        try {
            val inputStream = ObjectInputStream(bis)
            obj = inputStream.readObject()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj!!
    }

}
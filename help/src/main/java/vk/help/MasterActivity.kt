package vk.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import vk.help.Common.OnlyDatePattern
import vk.help.Common.ServerCommonDateTimePattern
import java.io.*
import java.lang.Exception
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

open class MasterActivity : AppCompatActivity(), CommonTask {

    companion object {
        const val DATA = "DATA"
        const val KEY = "KEY"
        const val ID = "ID"
        const val PHONE_NUMBER = "PHONE_NUMBER"
    }

    public lateinit var context: Context
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun convertDate(formatFrom: String, formatTo: String, value: String): String {
        try {
            val dateFormat = SimpleDateFormat(
                if (formatFrom.isEmpty()) ServerCommonDateTimePattern else formatFrom,
                Locale.getDefault()
            )
            val sourceDate = dateFormat.parse(value)
            val targetFormat = SimpleDateFormat(
                if (formatTo.isEmpty()) OnlyDatePattern else formatTo,
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

    override fun showToast(message: String) {
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast(context)
        toast!!.setGravity(Gravity.BOTTOM, 0, 100)
        toast!!.duration = Toast.LENGTH_LONG
        toast!!.view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        toast!!.view.findViewById<AppCompatTextView>(R.id.toast_message).text =
            Common.capitalize(message)
        toast!!.show()
    }

    override fun showErrorToast(message: String) {
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast(context)
        toast!!.setGravity(Gravity.BOTTOM, 0, 100)
        toast!!.duration = Toast.LENGTH_LONG
        toast!!.view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        toast!!.view.findViewById<AppCompatTextView>(R.id.toast_message).text =
            Common.capitalize(message)
        toast!!.view.findViewById<AppCompatTextView>(R.id.toast_message)
            .setTextColor(ContextCompat.getColor(context, R.color.white))
        toast!!.view.findViewById<CardView>(R.id.cardView)
            .setCardBackgroundColor(ContextCompat.getColor(context, R.color.material_red))
        toast!!.show()
    }

    override fun capitalize(value: String): String {
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

    override fun getJSON(obj: Any): String {
        return Gson().toJson(obj)
    }

    override fun getObject(jsonString: String, type: Type): Any {
        return Gson().fromJson(jsonString, type)
    }

    override fun getBytes(obj: Any): ByteArray {
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

    override fun getObject(bytes: ByteArray): Any {
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
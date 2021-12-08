package vk.help

import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import vk.help.base.MasterApplication

interface CommonTask {

    val TAG: String

    //String Related Start
    fun String.toToast() {
        Common.showToast(MasterApplication.context, this)
    }

    fun String.capitalize(): String {
        return Common.capitalize(this)
    }

    fun Any.toJSON(): String {
        return Common.getJSON(this)
    }

    fun Any.getBytes(): ByteArray {
        return Common.getBytes(this)
    }

    fun ByteArray.getObject(): Any {
        return Common.getObject(this)
    }

    //String Related End
    fun ImageView.setImage(url: String) {
        Glide.with(MasterApplication.context).load(url).into(this)
    }

    fun View.text(): String {
        return try {
            when (this) {
                is TextView -> {
                    this.text.toString()
                }
                is EditText -> {
                    this.text.toString()
                }
                is Button -> {
                    this.text.toString()
                }
                is RadioButton -> {
                    this.text.toString()
                }
                is CheckBox -> {
                    this.text.toString()
                }
                is CharSequence -> {
                    this.toString()
                }
                else -> {
                    ""
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun convertDate(formatFrom: String, formatTo: String, value: String): String {
        return Common.convertDate(formatFrom, formatTo, value)
    }

    fun log(value: String)
    fun setOnClickListeners(listener: View.OnClickListener, vararg views: View)
    fun saveString(key: String, value: String)
    fun getSaveString(key: String): String

    val DATA: String
        get() = "DATA"
    val KEY: String
        get() = "KEY"
    val ID: String
        get() = "ID"
    val EXTRA: String
        get() = "EXTRA"
    val PHONE_NUMBER: String
        get() = "PHONE_NUMBER"

}
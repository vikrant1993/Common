package vk.help

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import vk.help.base.MasterApplication
import kotlin.coroutines.CoroutineContext

interface CommonTask : CoroutineScope {

    //    val TAG: String
    val handler: Handler
        get() = Handler(Looper.getMainLooper())

    val Any.TAG: String
        get() = this.javaClass.simpleName

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()

    //String Related Start
    fun String.toToast() {
        Common.showToast(MasterApplication.context, this)
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

    fun convertDate(formatFrom: String, formatTo: String, value: String): String {
        return Common.convertDate(formatFrom, formatTo, value)
    }

    open fun String.log() {
        Common.longLog(TAG, this)
    }

    fun View.OnClickListener.setOnClickListeners(vararg views: View) {
        for (view in views) {
            view.setOnClickListener(this)
        }
    }

    fun saveString(key: String, value: String) {
        Common.saveString(key, value)
    }

    fun getSaveString(key: String): String {
        return Common.getString(key)
    }

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
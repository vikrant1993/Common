package vk.help

import android.os.Handler
import android.view.View
import android.widget.ImageView
import java.lang.reflect.Type

interface CommonTask {

    val TAG: String
    val handler: Handler

    fun ImageView.setImage(url: String)
    fun View.text(): String
    fun String.toToast()
    fun convertDate(formatFrom: String, formatTo: String, value: String): String
    fun showToast(message: String)
    fun showErrorToast(message: String)
    fun capitalize(value: String): String
    fun getJSON(obj: Any): String
    fun getObject(jsonString: String, type: Type): Any
    fun getBytes(obj: Any): ByteArray
    fun getObject(bytes: ByteArray): Any
    fun log(value: String)
    fun setOnClickListeners(listener: View.OnClickListener, vararg views: View)
    fun saveString(key: String, value: String)
    fun getSaveString(key: String): String

}
package vk.help

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import java.lang.reflect.Type

open class Fragment : Fragment(), CommonTask {

    companion object {
        const val DATA = "DATA"
        const val KEY = "KEY"
        const val ID = "ID"
        const val PHONE_NUMBER = "PHONE_NUMBER"
    }

    public lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = context!!
    }

    override fun convertDate(formatFrom: String, formatTo: String, value: String): String {
        return Common.convertDate(formatFrom, formatTo, value)
    }

    override fun showToast(message: String) {
        Common.showToast(context!!, message)
    }

    override fun showErrorToast(message: String) {
        Common.showErrorToast(context!!, message)
    }

    override fun capitalize(value: String): String {
        return Common.capitalize(value)
    }

    override fun getJSON(obj: Any): String {
        return Common.getJSON(obj)
    }

    override fun getObject(jsonString: String, type: Type): Any {
        return Common.getObject(jsonString, type)
    }

    override fun getBytes(obj: Any): ByteArray {
        return Common.getBytes(obj)
    }

    override fun getObject(bytes: ByteArray): Any {
        return Common.getObject(bytes)
    }
}
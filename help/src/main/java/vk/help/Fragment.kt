package vk.help

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import java.lang.reflect.Type

open class Fragment : Fragment(), CommonTask {

    companion object {
        const val DATA = "DATA"
        const val KEY = "KEY"
        const val ID = "ID"
        const val EXTRA = "EXTRA"
        const val PHONE_NUMBER = "PHONE_NUMBER"
    }

    public lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = context!!
    }

    override val TAG: String = this.javaClass.simpleName
    override val handler: Handler = Handler()

    override fun convertDate(formatFrom: String, formatTo: String, value: String): String {
        return Common.convertDate(formatFrom, formatTo, value)
    }

    override fun showToast(message: String) {
        Common.showToast(ctx, message)
    }

    override fun showErrorToast(message: String) {
        Common.showErrorToast(ctx, message)
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

    override fun log(value: String) {
        Common.longLog(TAG, value)
    }

    override fun setOnClickListeners(listener: View.OnClickListener, vararg views: View) {
        Common.setOnClickListener(listener, *views)
    }

    fun setOnClickListeners(vararg views: View) {
        if (this is View.OnClickListener) {
            setOnClickListeners(this, *views)
        } else {
            showErrorToast("no listener found")
        }
    }

    override fun ImageView.setImage(url: String) {
        Glide.with(ctx).load(url).into(this)
    }

    override fun View.text(): String {
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
                else -> {
                    ""
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

    override fun saveString(key: String, value: String) {
        Common.saveString(key, value)
    }

    override fun getSaveString(key: String): String {
        return Common.getString(key)
    }

    override fun String.toToast() {
        showToast(this)
    }
}
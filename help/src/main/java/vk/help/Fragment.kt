package vk.help

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        var animation = super.onCreateAnimation(transit, enter, nextAnim)
        // HW layer support only exists on API 11+
        // HW layer support only exists on API 11+
        if (animation == null && nextAnim != 0) {
            animation = AnimationUtils.loadAnimation(activity, nextAnim)
        }
        if (animation != null) {
            if (view != null) {
                view!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    if (view != null) {
                        view!!.setLayerType(View.LAYER_TYPE_NONE, null)
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        }
        return animation
    }

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

    override fun String.toToast() {
        showToast(this)
    }
}
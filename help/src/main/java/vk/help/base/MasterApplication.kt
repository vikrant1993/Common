package vk.help.base

import android.content.Context
import android.graphics.Typeface
import androidx.multidex.MultiDexApplication
import vk.help.Common
import vk.help.LocaleHelper
import vk.help.R

open class MasterApplication : MultiDexApplication() {

    enum class ToastMessageStyle {
        ALL_CAPITAL, FIRST_WORD_CAPITAL, WORDS_CAPITAL, NONE
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        Common.sharedPreferences =
            getSharedPreferences(getString(R.string.shared_vk_offline), Context.MODE_PRIVATE)
        setToastMessageStyle()
    }

    fun setToastMessageStyle(style: ToastMessageStyle = ToastMessageStyle.WORDS_CAPITAL) {
        toastMessageStyle = style
    }

    fun setFont(typeface: Typeface?) {
        try {
            val staticField = Typeface::class.java.getDeclaredField("MONOSPACE")
            staticField.isAccessible = true
            staticField[null] =
                typeface // Typeface.createFromAsset(getAssets(), "OpenSansRegular.ttf"));
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"))
    }

    companion object {
        var toastMessageStyle: ToastMessageStyle? = null
        val context: Context by lazy {
            instance.applicationContext
        }
        private lateinit var mInstance: MasterApplication

        @get:Synchronized
        val instance: MasterApplication
            get() = mInstance.also {
                mInstance = it
            }
    }
}
package vk.help.base

import android.content.Context
import android.graphics.Typeface
import androidx.multidex.MultiDexApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import vk.help.Common
import vk.help.R
import vk.help.networks.ApiClient

open class MasterApplication : MultiDexApplication() {

    enum class ToastMessageStyle {
        ALL_CAPITAL, FIRST_WORD_CAPITAL, WORDS_CAPITAL, NONE
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        Common.sharedPreferences = getSharedPreferences(getString(R.string.shared_vk_offline), Context.MODE_PRIVATE)
        setToastMessageStyle(ToastMessageStyle.WORDS_CAPITAL)

        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(getAppModule())
        }
    }

    open fun getAppModule(): Module {
        return module {
//            viewModel { NetworkViewModel() }
//            viewModel { SignupViewModel() }
//            viewModel { LoginViewModel() }
//            viewModel { ForgotViewModel() }
//            viewModel { HomeViewModel() }
//            viewModel { AddressViewModel() }
//            viewModel { CheckoutViewModel() }
//            viewModel { ProductsViewModel() }
            factory { ApiClient.NetworkConnectionInterceptor(get()) }
        }
    }

    fun setToastMessageStyle(style: ToastMessageStyle?) {
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
    } //    @Override

    //    protected void attachBaseContext(Context base) {
    //        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    //    }

    companion object {
        var toastMessageStyle: ToastMessageStyle? = null
        val context: Context by lazy {
            instance.applicationContext
        }
        private var mInstance: MasterApplication? = null

        @get:Synchronized
        val instance: MasterApplication
            get() = mInstance ?: MasterApplication().also {
                mInstance = it
            }
    }
}
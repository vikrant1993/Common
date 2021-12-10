package vk.help.base

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import vk.help.Common
import vk.help.CommonTask

abstract class MasterActivity : AppCompatActivity(), CommonTask {

    public val context: Context by lazy {
        this
    }

    override val handler = Handler(Looper.getMainLooper())

    override fun log(value: String) {
        Common.longLog(TAG, value)
    }

    override fun saveString(key: String, value: String) {
        Common.saveString(key, value)
    }

    override fun getSaveString(key: String): String {
        return Common.getString(key)
    }
}
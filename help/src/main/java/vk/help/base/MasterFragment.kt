package vk.help.base

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import vk.help.Common
import vk.help.CommonTask

open class MasterFragment : Fragment(), CommonTask {

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
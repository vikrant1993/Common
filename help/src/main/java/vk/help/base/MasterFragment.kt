package vk.help.base

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import vk.help.Common
import vk.help.CommonTask

open class MasterFragment : Fragment(), CommonTask {

    override val TAG: String = this.javaClass.simpleName
    override val handler = Handler(Looper.getMainLooper())

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
            "no listener found".toToast()
        }
    }

    override fun saveString(key: String, value: String) {
        Common.saveString(key, value)
    }

    override fun getSaveString(key: String): String {
        return Common.getString(key)
    }
}
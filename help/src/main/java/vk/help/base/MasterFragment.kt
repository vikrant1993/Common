package vk.help.base

import androidx.fragment.app.Fragment
import vk.help.Common
import vk.help.CommonTask

open class MasterFragment : Fragment(), CommonTask {

    override fun saveString(key: String, value: String) {
        Common.saveString(key, value)
    }

    override fun getSaveString(key: String): String {
        return Common.getString(key)
    }
}
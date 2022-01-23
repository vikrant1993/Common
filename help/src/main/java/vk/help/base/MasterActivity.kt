package vk.help.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import vk.help.Common
import vk.help.CommonTask

abstract class MasterActivity : AppCompatActivity(), CommonTask {

    public val context: Context by lazy {
        this
    }

    override fun saveString(key: String, value: String) {
        Common.saveString(key, value)
    }

    override fun getSaveString(key: String): String {
        return Common.getString(key)
    }
}
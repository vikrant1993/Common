package vk.help.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import vk.help.Common
import vk.help.CommonTask

abstract class MasterActivity : AppCompatActivity(), CommonTask {

    public val context: Context by lazy {
        this
    }
}
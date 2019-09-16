package vk.help

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MasterActivity : AppCompatActivity() {

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
    }
}
package vk.help.common

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.temp.*
import vk.help.MasterActivity
import vk.help.placepicker.ActivityPlacePicker

class InitialScreenActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)
        updateDate.setOnClickListener {
            startActivity(Intent(context, ActivityPlacePicker::class.java))
        }
    }
}
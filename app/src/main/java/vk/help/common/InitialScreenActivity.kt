package vk.help.common

import android.os.Bundle
import kotlinx.android.synthetic.main.temp.*
import vk.help.MasterActivity

class InitialScreenActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)
        updateDate.setOnClickListener {
//            startActivity(Intent(context, ActivityPlacePicker::class.java))
        }
    }
}
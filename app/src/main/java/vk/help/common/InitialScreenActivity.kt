package vk.help.common

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.temp.*
import vk.help.MasterActivity

class InitialScreenActivity : MasterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)

        setOnClickListeners(View.OnClickListener {

        }, updateDate)

//        updateDate.setOnClickListener {
//            startActivity(Intent(context, ActivityPlacePicker::class.java))
//        }
    }
}
package vk.help.common

import android.os.Bundle
import kotlinx.android.synthetic.main.temp.*
import vk.help.MasterActivity

class InitialScreenActivity : MasterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)

        imageView.setImage("https://image.shutterstock.com/image-photo/colorful-flower-on-dark-tropical-260nw-721703848.jpg")

        updateDate.setOnClickListener {

        }
    }
}
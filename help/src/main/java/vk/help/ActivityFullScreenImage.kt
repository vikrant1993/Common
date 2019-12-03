package vk.help

import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_full_screen_image.*

class ActivityFullScreenImage : MasterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)
        val imageUrl = intent.getStringExtra(DATA)
        Glide.with(context).load(imageUrl).into(imageView)
    }
}
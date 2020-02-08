package vk.help.common

import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.temp.*
import vk.help.MasterActivity
import vk.help.imagepicker.features.ImagePicker
import java.io.File

class InitialScreenActivity : MasterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)

        imageView.setImage("https://image.shutterstock.com/image-photo/colorful-flower-on-dark-tropical-260nw-721703848.jpg")

        updateDate.setOnClickListener {
            ImagePicker.create(this).single().showCamera(true).start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val images = ImagePicker.getImages(data)
            if (images.size > 0) {
                Glide.with(context).load(File(images[0].path)).into(imageView)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
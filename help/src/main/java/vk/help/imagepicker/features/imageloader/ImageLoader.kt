package vk.help.imagepicker.features.imageloader

import android.widget.ImageView
import java.io.Serializable

interface ImageLoader : Serializable {
    fun loadImage(
        path: String?,
        imageView: ImageView?,
        imageType: ImageType?
    )
}
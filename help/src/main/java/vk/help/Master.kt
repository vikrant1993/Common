package vk.help

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import vk.help.base.MasterApplication
import java.util.*


inline fun <reified T : Any> String.cast(): T {
    return Common.getObject(this, T::class.java) as T
}

inline fun <reified T : Any> ByteArray.cast(): T {
    return Common.getObject(this) as T
}

fun View.text(): String {
    return try {
        when (this) {
            is TextView -> {
                this.text.toString()
            }
            is CharSequence -> {
                this.toString()
            }
            else -> {
                ""
            }
        }
    } catch (e: Exception) {
        ""
    }
}

fun ImageView.setImage(url: String) {
    Glide.with(MasterApplication.context).load(url).into(this)
}

fun String.capitalize():String{
    val words = lowercase(Locale.getDefault()).trim().split(" ")
    val ret = StringBuilder()
    for (i in words.indices) {
        if (words[i].trim { it <= ' ' }.isNotEmpty()) {
            ret.append(Character.toUpperCase(words[i].trim { it <= ' ' }[0]))
            ret.append(words[i].trim { it <= ' ' }.substring(1))
            if (i < words.size - 1) {
                ret.append(' ')
            }
        }
    }
    return ret.toString()
}
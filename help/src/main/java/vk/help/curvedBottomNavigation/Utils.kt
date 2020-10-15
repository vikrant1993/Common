package vk.help.curvedBottomNavigation

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Int.toPx(context: Context) = (this * context.resources.displayMetrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT

fun Context.getColorRes(@ColorRes colorId: Int) = ContextCompat.getColor(this, colorId)
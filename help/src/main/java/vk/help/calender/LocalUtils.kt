package vk.help.calender

import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import java.util.*

internal object LocalUtils {
    val isRTL: Boolean
        get() = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL
}
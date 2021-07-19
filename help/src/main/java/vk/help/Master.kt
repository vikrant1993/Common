package vk.help


inline fun <reified T : Any> String.cast(): T {
    return Common.getObject(this, T::class.java) as T
}

inline fun <reified T : Any> ByteArray.cast(): T {
    return Common.getObject(this) as T
}
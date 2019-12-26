package vk.help

import java.lang.reflect.Type

interface CommonTask {

    val TAG: String

    fun convertDate(formatFrom: String, formatTo: String, value: String): String
    fun showToast(message: String)
    fun showErrorToast(message: String)
    fun capitalize(value: String): String
    fun getJSON(obj: Any): String
    fun getObject(jsonString: String, type: Type): Any
    fun getBytes(obj: Any): ByteArray
    fun getObject(bytes: ByteArray): Any
    fun log(value: String)
}
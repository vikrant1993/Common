package vk.help.models

import java.io.Serializable

open class JList(var id: String = "", var name: String = "", var extra: String = "") :
    Serializable {

    override fun toString(): String {
        return if (name.isEmpty()) extra else name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JList) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (extra != other.extra) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + extra.hashCode()
        return result
    }
}
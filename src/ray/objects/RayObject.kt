package ray.objects

import com.google.common.base.Objects
import org.ascore.lang.objects.ASCObject

abstract class RayObject<T : Any>(value: T, open val type: RayInstanceType) : ASCObject<T>(value) {
    companion object {
        val RAY_NOTHING = object : RayObject<Any>(0, RaySimpleType.NOTHING) {}
    }

    inline fun <reified U : Any> value(): U {
        return value as U
    }

    open fun repr(): String = toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RayObject<*>) return false

        if (type != other.type) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        return Objects.hashCode(type, value)
    }
}
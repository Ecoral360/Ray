package ray.objects

import org.ascore.lang.objects.ASCObject

abstract class RayObject<T : Any>(value: T, open val type: RayInstanceType) : ASCObject<T>(value) {
    companion object {
        val RAY_NOTHING = object : RayObject<Any>(0, RaySimpleType.NOTHING) {}
    }

    inline fun <reified U : Any> value(): U {
        return value as U
    }
}
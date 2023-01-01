package ray.objects

import org.ascore.lang.objects.ASCObject

abstract class RayObject<T : Any>(value: T, open val type: RayInstanceType) : ASCObject<T>(value) {
    inline fun <reified U : Any> value(): U {
        return value as U
    }
}
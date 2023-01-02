package ray.objects.function

import ray.objects.RayInstanceType
import ray.objects.RayObject
import ray.objects.RaySimpleType

abstract class RayCallable(type: RayInstanceType) : RayObject<Any>(0, type) {
    abstract fun call(args: Pair<RayObject<*>?, RayObject<*>?>): RayObject<*>

    abstract fun matches(type: RayInstanceType): Boolean

    override fun getValue(): Any = this
}
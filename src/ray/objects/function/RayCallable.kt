package ray.objects.function

import ray.objects.RayFunctionType
import ray.objects.RayInstanceType
import ray.objects.RayObject

abstract class RayCallable(val name: String, type: RayFunctionType) :
    RayObject<Any>("`FUNCTION ${type.getTypeSignature()}`", type) {

    abstract fun withName(name: String): RayCallable

    abstract fun call(args: Pair<RayObject<*>?, RayObject<*>?>): RayObject<*>

    abstract fun matches(type: RayInstanceType): Boolean

    fun getFuncSignature() = "$name@${type.getTypeSignature()}"

    override val type: RayFunctionType
        get() = super.type as RayFunctionType

    override fun getValue(): Any = this
}
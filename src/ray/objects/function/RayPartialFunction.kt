package ray.objects.function

import ray.objects.RayFunctionType
import ray.objects.RayObject
import ray.objects.RaySimpleType

class RayPartialFunction(name: String, functions: List<RayFunction>) : RayObject<Any>(0, RaySimpleType.ANY), RayCallable {
    override fun call(args: Pair<RayObject<*>?, RayObject<*>?>): RayObject<*> {
        TODO("Not yet implemented")
    }
}
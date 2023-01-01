package ray.objects.function

import ray.objects.RayObject

interface RayCallable {
    fun call(args: Pair<RayObject<*>?, RayObject<*>?>): RayObject<*>
}
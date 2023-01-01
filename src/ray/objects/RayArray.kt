package ray.objects

import ray.objects.primitive.RayNumber
import ray.objects.primitive.plus


class RayArray<T : RayObject<*>>(value: Array<T>) : RayObject<Array<T>>(value, value.getSmallestCommonRayType()) {

    override fun toString(): String {
        return value.joinToString(" ")
    }
}


/** Finds the smallest common type in the array.
 */
fun <T : RayObject<*>> Array<T>.getSmallestCommonRayType(): RayInstanceType {
    val firstType = this[0].type
    return if (this.all { firstType.matches(it.type) }) RayArrayType(firstType) else RayArrayType(RaySimpleType.ANY)
}


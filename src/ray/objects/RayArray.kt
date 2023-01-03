package ray.objects

import ray.execution.ARRAY_BOXING


class RayArray<T : RayObject<*>>(value: Array<T>) : RayObject<Array<T>>(value, value.getSmallestCommonRayType()) {

    override fun toString(): String {
        return if (ARRAY_BOXING) value.joinToString(" ", prefix = "[", postfix = "]")
        else value.joinToString(" ") { if (it is RayArray<*>) " $it " else it.toString() }
    }

    override fun equals(other: Any?): Boolean {
        return other is RayArray<*> && value.contentDeepEquals(other.value)
    }

    override fun hashCode(): Int = super.hashCode()
}


/** Finds the smallest common type in the array.
 */
fun <T : RayObject<*>> Array<T>.getSmallestCommonRayType(): RayInstanceType {
    val firstType = this[0].type
    return if (this.all { firstType.matches(it.type) }) RayArrayType(firstType) else RayArrayType(RaySimpleType.ANY)
}


package ray.objects.primitive

import org.ascore.lang.objects.ASCObject
import ray.objects.RayObject

typealias RayNumber = RayObject<Number>

fun Number.isInt() = "." !in this.toString()


fun Number.plus(other: Number): Number =
        if (!this.isInt() || !other.isInt()) this.toDouble() + other.toDouble()
        else this.toInt() + other.toInt()

fun Number.toRayNumber(): RayNumber =
        if (this.isInt()) RayInt(this)
        else RayFloat(this)

fun RayNumber.plus(other: RayNumber): RayNumber = this.value.plus(other.value).toRayNumber()


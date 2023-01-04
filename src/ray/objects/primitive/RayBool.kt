package ray.objects.primitive

import org.ascore.tokens.Token
import ray.objects.RaySimpleType

/**
 * An example of an object for the Ray programming main.language
 */
class RayBool(value: Boolean) : RayInt(if (value) 1 else 0) {
    constructor(value: Number) : this(value.toInt() == 1)

    override fun getValue(): Int = super.getValue().toInt()

    override fun toString(): String {
        return super.toString().replace("-", "_")
    }
}

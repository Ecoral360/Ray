package ray.objects.primitive

import org.ascore.tokens.Token
import ray.objects.RaySimpleType

/**
 * An example of an object for the Ray programming main.language
 */
class RayFloat(value: Double) : RayNumber(value, RaySimpleType.NUMBER) {
    constructor(token: Token) : this(token.value().replaceFirstChar { if (it == '_') '-' else it }.toDouble())

    constructor(value: Number) : this(value.toDouble())

    override fun getValue(): Double = super.getValue().toDouble()

    override fun toString(): String {
        return super.toString().replace("-", "_")
    }
}

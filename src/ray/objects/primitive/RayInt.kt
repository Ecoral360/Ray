package ray.objects.primitive

import org.ascore.tokens.Token
import ray.objects.RaySimpleType

/**
 * An example of an object for the Ray programming main.language
 */
class RayInt(value: Int) : RayNumber(value, RaySimpleType.NUMBER) {
    constructor(token: Token) : this(token.value().replaceFirstChar { if (it == '_') '-' else it }.toInt())

    constructor(value: Number) : this(value.toInt())

    override fun getValue(): Int = super.getValue().toInt()

    override fun toString(): String {
        return super.toString().replace("-", "_")
    }
}

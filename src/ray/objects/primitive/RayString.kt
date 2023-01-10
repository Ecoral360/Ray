package ray.objects.primitive

import org.ascore.lang.objects.ASCObject
import org.ascore.tokens.Token
import ray.objects.RayObject
import ray.objects.RaySimpleType

/**
 * An example of an object for the Ray programming main.language
 */
class RayString(value: String) : RayObject<String>(value, RaySimpleType.STRING) {
    constructor(token: Token) : this(
        token.value().substring(1, token.value().length - 1) // removing the enclosing `"` from the string
            .replace("\\n", "\n")
    )

    override fun toString(): String = value

    override fun repr(): String = "\"$value\""
}

package ray.errors

import org.ascore.errors.ASCErrors.ASCError


class RayError(val errorType: RayErrors, message: String) : ASCError(message, errorType.prettyName()) {
    companion object {
        fun new(errorType: RayErrors, vararg args: Any): RayError {
            return RayError(errorType, errorType.transform(args))
        }
    }
}

enum class RayErrors(val transform: (Array<out Any>) -> String) {

    UNKNOWN_VARIABLE("There are no variable, constant or function named '%s'"),

    NON_INTEGER_RANGE("A range must be made from an integer, not a floating point number"),

    UNKNOWN_FUNCTION_SIGNATURE({
        "No function was found with a signature matching: ${it[0]}" +
                "\nThe functions defined with the same name have the following signatures:" +
                (it[1] as List<*>).joinToString("\n\t", prefix = "\n\t")
    }),

    INVALID_FUNCTION_CALL("Function '%s' was called with args %s, but the signature is %s");

    constructor(format: String = "%s") : this({ format.format(*it) })

    fun prettyName() = this.name.split("_").joinToString("") {
        it.lowercase().replaceFirstChar { c -> c.uppercase() }
    }

}

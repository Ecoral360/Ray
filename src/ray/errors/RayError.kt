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
    TYPE_ERROR("The value was suppose to be of type '%s' but was of type '%s'"),

    NON_INTEGER_RANGE("A range must be made from an integer, not a floating point number"),

    UNKNOWN_FUNCTION_SIGNATURE({
        "No function was found with a signature matching: ${it[0]}" +
                "\nThe functions defined with the same name have the following signatures:" +
                (it[1] as List<*>).joinToString("\n\t", prefix = "\n\t")
    }),

    INVALID_FUNCTION_CALL("Function '%s' was called with args %s, but the signature is %s"),

    INVALID_ARG_REFERENCE("Arg '%s' was referenced inside a function that doesn't take such argument");

    companion object {
        fun fromString(s: String): RayErrors {
            return valueOf(s.replace("[A-Z]".toRegex()) { "_${it.value}" }.substring(1).uppercase())
        }

    }

    constructor(format: String = "%s") : this({ format.format(*it) })

    fun prettyName() = this.name.split("_").joinToString("") {
        it.lowercase().replaceFirstChar { c -> c.uppercase() }
    }


}

package ray.errors

import org.ascore.errors.ASCErrors.ASCError


class RayError(val errorType: RayErrors, message: String) : ASCError(message, errorType.prettyName()) {
    companion object {
        fun new(errorType: RayErrors, vararg args: String): RayError {
            return RayError(errorType, errorType.format.format(*args))
        }
    }
}

enum class RayErrors(val format: String = "%s") {
    UNKNOWN_VARIABLE("There are no variable, constant or function named '%s'"),

    UNKNOWN_FUNCTION_SIGNATURE("No function was found with a signature matching: %s" +
            "\nThe functions defined with the same name have the following signatures:\n\t%s"),

    INVALID_FUNCTION_CALL("Function %s was called");

    fun prettyName() = this.name.split("_").joinToString("") {
        it.lowercase().replaceFirstChar { c -> c.uppercase() }
    }

}

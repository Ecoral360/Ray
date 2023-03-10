package ray.objects.function

import ray.errors.RayError
import ray.errors.RayErrors
import ray.objects.*

class RayPartialFunction(
    private val functionName: String,
    private val functions: List<RayCallable>
) : RayCallable(functionName, functions.getCommonType()) {

    override fun call(args: Pair<RayObject<*>?, RayObject<*>?>): RayObject<*> {
        val (leftArg, rightArg) = args
        val typeSignature = RayFunctionType(
            leftArg?.type ?: RaySimpleType.NOTHING, rightArg?.type
                ?: RaySimpleType.NOTHING, RaySimpleType.UNKNOWN
        )

        val function = functions.filter { typeSignature.matches(it.type) }
            .minByOrNull { it.type.getTypeSignature().count { c -> c == RaySimpleType.ANY.getTypeSymbol()[0] } }

        return function?.call(Pair(leftArg, rightArg))
        // no function with a matching type signature: error is an UNKNOWN_FUNCTION_SIGNATURE
            ?: throw RayError.new(RayErrors.UNKNOWN_FUNCTION_SIGNATURE,
                RayModuleFunction.formatSignature(functionName, typeSignature),
                functions.map { it.getFuncSignature() })
    }

    override fun withName(name: String): RayCallable {
        return RayPartialFunction(name, functions)
    }

    override fun matches(type: RayInstanceType): Boolean = functions.any { it.type.matches(type) }
}

/** Finds the smallest common type in the array.
 */
fun <T : RayCallable> List<T>.getCommonType(): RayFunctionType {
    val leftType = when (filter { it.type.leftType == RaySimpleType.NOTHING }.size) {
        size -> RaySimpleType.NOTHING
        0 -> RaySimpleType.ANY
        else -> return RayFunctionType.ANY_FUNCTION
    }
    val rightType = when (filter { it.type.rightType == RaySimpleType.NOTHING }.size) {
        size -> RaySimpleType.NOTHING
        0 -> RaySimpleType.ANY
        else -> return RayFunctionType.ANY_FUNCTION
    }
    val retType = if (all { it.type.returnType == RaySimpleType.NOTHING }) RaySimpleType.NOTHING else RaySimpleType.ANY

    return RayFunctionType(leftType, rightType, retType)
}

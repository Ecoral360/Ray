package ray.objects.function

import ray.errors.RayError
import ray.errors.RayErrors
import ray.objects.*

class RayPartialFunction(private val functionName: String,
                         private val functions: List<RayFunction>) : RayCallable(functions.getCommonType()) {
    override fun call(args: Pair<RayObject<*>?, RayObject<*>?>): RayObject<*> {
        val (leftArg, rightArg) = args
        val typeSignature = RayFunctionType(leftArg?.type ?: RaySimpleType.NOTHING, rightArg?.type
                ?: RaySimpleType.NOTHING, RaySimpleType.UNKNOWN)

        val function = functions.filter { typeSignature.matches(it.type) }.minByOrNull { it.type.getTypeSignature().count { c -> c == RaySimpleType.ANY.getTypeSymbol()[0] } }

        return function?.call(Pair(leftArg, rightArg))
        // no function with a matching type signature: error is an UNKNOWN_FUNCTION_SIGNATURE
                ?: throw RayError.new(RayErrors.UNKNOWN_FUNCTION_SIGNATURE,
                        RayFunction.formatSignature(functionName, typeSignature),
                        functions.map { it.getFuncSignature() })
    }

    override fun matches(type: RayInstanceType): Boolean = functions.any { it.type.matches(type) }
}

/** Finds the smallest common type in the array.
 */
fun <T : RayFunction> List<T>.getCommonType(): RayFunctionType {
    val leftType = if (all { it.type.leftType == RaySimpleType.NOTHING }) RaySimpleType.NOTHING else RaySimpleType.ANY
    val rightType = if (all { it.type.rightType == RaySimpleType.NOTHING }) RaySimpleType.NOTHING else RaySimpleType.ANY
    val retType = if (all { it.type.returnType == RaySimpleType.NOTHING }) RaySimpleType.NOTHING else RaySimpleType.ANY

    return RayFunctionType(leftType, rightType, retType)
}

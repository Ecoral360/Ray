package ray.objects.function

import ray.errors.RayError
import ray.errors.RayErrors
import ray.objects.RayFunctionType
import ray.objects.RayObject
import ray.objects.RaySimpleType

class RayPartialFunction(private val functionName: String,
                         private val functions: List<RayFunction>) : RayCallable(RayFunctionType()) {
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
}
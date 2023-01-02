package ray.objects.function


import ray.errors.RayError
import ray.errors.RayErrors
import ray.objects.RayFunctionType
import ray.objects.RayInstanceType
import ray.objects.RayObject
import ray.objects.RaySimpleType

typealias RayFunctionCallback = (Pair<RayObject<*>?, RayObject<*>?>) -> RayObject<*>

open class RayFunction(val name: String, type: RayFunctionType, private val callback: RayFunctionCallback) : RayCallable(type) {

    companion object {
        fun infix(name: String, leftType: RayInstanceType, rightType: RayInstanceType, retType: RayInstanceType, callback: RayFunctionCallback): RayFunction {
            return RayFunction(name, RayFunctionType(leftType, rightType, retType), callback)
        }

        fun postfix(name: String, rightType: RayInstanceType, retType: RayInstanceType, callback: RayFunctionCallback): RayFunction {
            return RayFunction(name, RayFunctionType(RaySimpleType.UNKNOWN, rightType, retType), callback)
        }

        fun prefix(name: String, leftType: RayInstanceType, retType: RayInstanceType, callback: RayFunctionCallback): RayFunction {
            return RayFunction(name, RayFunctionType(leftType, RaySimpleType.UNKNOWN, retType), callback)
        }

        fun formatSignature(funcName: String, functionType: RayFunctionType): String {
            return "$funcName@${functionType.getTypeSignature()}"
        }
    }

    fun getFuncSignature(): String {
        return "$name@${type.getTypeSignature()}"
    }

    override fun call(args: Pair<RayObject<*>?, RayObject<*>?>): RayObject<*> {
        // may be necessary because if it goes to this step, the args were probably already validated
        // if (!validateArgs(args)) throw RayError(RayErrors.INVALID_FUNCTION_CALL, "")
        return callback(args)
    }

    private fun validateArgs(args: Pair<RayObject<*>?, RayObject<*>?>): Boolean {
        TODO("Validate that the type of each args matches the associated type for the function")
    }

    override val type get(): RayFunctionType = super.type as RayFunctionType

    override fun getValue(): Any = this
}
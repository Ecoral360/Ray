package ray.objects.function


import ray.errors.RayError
import ray.errors.RayErrors
import ray.objects.RayFunctionType
import ray.objects.RayInstanceType
import ray.objects.RayObject
import ray.objects.RaySimpleType

typealias RayFunctionCallback = (Pair<RayObject<*>?, RayObject<*>?>) -> RayObject<*>

open class RayFunction(name: String, type: RayFunctionType, private val callback: RayFunctionCallback) :
    RayCallable(name, type) {

    companion object {
        fun reverse(function: RayFunction): RayFunction {
            val revType = RayFunctionType(function.type.rightType, function.type.leftType, function.type.returnType)
            return RayFunction(function.name, revType) { (leftArg, rightArg) ->
                function.call(
                    Pair(rightArg, leftArg)
                )
            }
        }

        fun withAliases(function: RayFunction, vararg aliases: String): Array<RayFunction> {
            return arrayOf(
                function,
                *aliases.map { alias -> RayFunction(alias, function.type, function.callback) }.toTypedArray()
            )
        }

        fun infix(
            name: String,
            leftType: RayInstanceType,
            rightType: RayInstanceType,
            retType: RayInstanceType,
            callback: RayFunctionCallback
        ): RayFunction {
            return RayFunction(name, RayFunctionType(leftType, rightType, retType), callback)
        }

        fun postfix(
            name: String,
            rightType: RayInstanceType,
            retType: RayInstanceType,
            callback: RayFunctionCallback
        ): RayFunction {
            return RayFunction(name, RayFunctionType(RaySimpleType.UNKNOWN, rightType, retType), callback)
        }

        fun prefix(
            name: String,
            leftType: RayInstanceType,
            retType: RayInstanceType,
            callback: RayFunctionCallback
        ): RayFunction {
            return RayFunction(name, RayFunctionType(leftType, RaySimpleType.UNKNOWN, retType), callback)
        }

        fun formatSignature(funcName: String, functionType: RayFunctionType): String {
            return "$funcName@${functionType.getTypeSignature()}"
        }
    }

    fun reversed(): RayFunction = reverse(this)

    fun withReversed(): Array<RayFunction> = arrayOf(this, reverse(this))

    fun withAliases(vararg aliases: String): Array<RayFunction> {
        return arrayOf(
            this,
            *aliases.map { alias -> RayFunction(alias, this.type, this.callback) }.toTypedArray()
        )
    }

    override fun withName(name: String): RayCallable {
        return RayFunction(name, type, callback)
    }

    override fun call(args: Pair<RayObject<*>?, RayObject<*>?>): RayObject<*> {
        val argType = RayFunctionType(
            args.first?.type ?: RaySimpleType.NOTHING,
            args.second?.type ?: RaySimpleType.NOTHING,
            RaySimpleType.UNKNOWN
        )

        if (!validateArgs(argType)) throw RayError.new(
            RayErrors.INVALID_FUNCTION_CALL,
            name,
            argType.getTypeSignature(),
            getFuncSignature()
        )
        return callback(args)
    }

    private fun validateArgs(argType: RayFunctionType): Boolean {
        return type.matches(argType)
    }

    override fun matches(type: RayInstanceType): Boolean = this.type.matches(type)

    override val type get(): RayFunctionType = super.type as RayFunctionType

    override fun getValue(): Any = this
}
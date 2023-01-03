package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.RayArray
import ray.objects.RayArrayType
import ray.objects.RayFunctionType
import ray.objects.RaySimpleType
import ray.objects.function.RayFunction
import ray.objects.primitive.*

object RayNumberModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayFunction> =
        arrayOf(
            // Addition of numbers
            RayFunction(
                "+",
                RayFunctionType(RaySimpleType.NUMBER, RaySimpleType.NUMBER, RaySimpleType.NUMBER)
            ) { args ->
                val left = args.first!!.value as Number
                val right = args.second!!.value as Number

                left.toRayNumber().plus(right.toRayNumber())
            },

            // iota (sequence)
            RayFunction(
                "i.",
                RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.NUMBER, RayArrayType(RaySimpleType.NUMBER))
            ) { args ->
                val size = args.second!!.value<Number>()
                val descending = size.toDouble() < 0
                if (!size.isInt()) throw RayError.new(RayErrors.NON_INTEGER_RANGE)

                if (descending) RayArray<RayNumber>((0 downTo size.toInt() + 1).map { RayInt(it) }.toTypedArray())
                else RayArray<RayNumber>((0 until size.toInt()).map { RayInt(it) }.toTypedArray())

            },
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf(
            ASCVariable("π", RayFloat(Math.PI)),
            ASCVariable("pi", RayFloat(Math.PI)),
        )
}
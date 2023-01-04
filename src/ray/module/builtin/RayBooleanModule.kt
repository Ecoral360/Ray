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

object RayBooleanModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayFunction> =
        arrayOf(
            // Addition of numbers
            RayFunction(
                "^",
                RayFunctionType(RaySimpleType.NUMBER, RaySimpleType.NUMBER, RaySimpleType.NUMBER)
            ) { args ->
                val left = args.first!!.value as Number
                val right = args.second!!.value as Number

                RayBool(left == 1 && right == 1)
            },

            RayFunction(
                "|",
                RayFunctionType(RaySimpleType.NUMBER, RaySimpleType.NUMBER, RaySimpleType.NUMBER)
            ) { args ->
                val left = args.first!!.value as Number
                val right = args.second!!.value as Number

                left.toRayNumber().plus(right.toRayNumber())
            },
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf(
            ASCVariable("π", RayFloat(Math.PI)),
            ASCVariable("pi", RayFloat(Math.PI)),
        )
}
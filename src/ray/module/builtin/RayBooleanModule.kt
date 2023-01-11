package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.RayFunctionType
import ray.objects.RaySimpleType
import ray.objects.function.RayModuleFunction
import ray.objects.primitive.*

object RayBooleanModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayModuleFunction> =
        arrayOf(
            // Addition of numbers
            RayModuleFunction(
                "^",
                RayFunctionType(RaySimpleType.NUMBER, RaySimpleType.NUMBER, RaySimpleType.NUMBER)
            ) { args ->
                val left = args.first!!.value as Number
                val right = args.second!!.value as Number

                RayBool(left == 1 && right == 1)
            },

            RayModuleFunction(
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
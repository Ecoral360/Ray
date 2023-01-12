package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.RayFunctionType
import ray.objects.RayObject
import ray.objects.RaySimpleType
import ray.objects.function.RayCallable
import ray.objects.function.RayFunction
import ray.objects.function.RayModuleFunction
import ray.objects.primitive.*

object RayControlFlowModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayModuleFunction> =
        arrayOf(
            // if
            RayModuleFunction(
                "if.",
                RayFunctionType(RayFunctionType.prefix(), RaySimpleType.NUMBER, RayFunctionType.prefix())
            ) { args ->
                val function = args.first!!.value<RayCallable>()
                val condition = RayBool(args.second!!.value<Number>())
                if (condition.bool()) function
                else RayModuleFunction("", function.type) { RayObject.RAY_NOTHING }
            },

            RayModuleFunction(
                "if:",
                RayFunctionType(RayFunctionType.infix(), RaySimpleType.NUMBER, RayFunctionType.infix())
            ) { args ->
                val function = args.first!!.value<RayCallable>()
                val condition = RayBool(args.second!!.value<Number>())
                if (condition.bool()) function
                else RayModuleFunction("", function.type) { RayObject.RAY_NOTHING }
            }

            // while loop
            // for loop
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf(

        )
}
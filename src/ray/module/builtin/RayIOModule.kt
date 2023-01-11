package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.RayFunctionType
import ray.objects.RaySimpleType
import ray.objects.function.RayModuleFunction
import ray.objects.primitive.RayString

object RayIOModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayModuleFunction> =
        arrayOf(
            RayModuleFunction(
                "read.",
                RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.NOTHING, RaySimpleType.STRING)
            ) { args ->
                val output = readln()
                RayString(output)
            }
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf()
}
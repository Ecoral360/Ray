package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.function.RayModuleFunction

object RayMatrixModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayModuleFunction> =
        arrayOf()

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf()
}
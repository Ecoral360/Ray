package ray.module

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.objects.function.RayModuleFunction

interface RayModule {
    fun loadFunctions(executorState: RayExecutorState): Array<RayModuleFunction>
    fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>>
}
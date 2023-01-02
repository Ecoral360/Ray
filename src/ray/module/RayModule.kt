package ray.module

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.objects.RayObject
import ray.objects.function.RayFunction

interface RayModule {
    fun loadFunctions(executorState: RayExecutorState): Array<RayFunction>
    fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>>
}
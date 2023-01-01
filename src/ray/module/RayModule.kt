package ray.module

import ray.execution.RayExecutorState
import ray.objects.function.RayFunction

interface RayModule {
    fun load(executorState: RayExecutorState): Array<RayFunction>
}
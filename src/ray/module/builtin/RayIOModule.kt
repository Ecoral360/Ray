package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.*
import ray.objects.function.RayFunction
import ray.objects.primitive.*

object RayIOModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayFunction> =
        arrayOf(
            RayFunction("r.", RayFunctionType.infix()) { args ->
                RayObject.RAY_NOTHING
            }
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf()
}
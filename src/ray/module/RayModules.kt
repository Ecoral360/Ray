package ray.module

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.builtin.RayBuiltins

enum class RayModules(val module: RayModule) {
    Builtins(RayBuiltins)
    ;

    companion object {
        fun load(executorState: RayExecutorState) {
            values().forEach {
                val functions = it.module.loadFunctions(executorState)
                val variables = it.module.loadVariables(executorState)
                functions.forEach { func ->
                    executorState.scopeManager.currentScope.declareVariable(ASCVariable(func.getFuncSignature(), func))
                }
                variables.forEach { variable ->
                    executorState.scopeManager.currentScope.declareVariable(variable)
                }
            }
        }
    }
}
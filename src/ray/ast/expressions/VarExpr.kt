package ray.ast.expressions

import org.ascore.ast.buildingBlocs.Expression
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.objects.RayObject

open class VarExpr(val name: String, protected val executorState: RayExecutorState) : Expression<RayObject<*>> {
    override fun eval(): RayObject<*> {
        return executorState.scopeManager.currentScopeInstance.getVariable(name)?.ascObject as RayObject<*>?
                ?: throw RayError.new(RayErrors.UNKNOWN_VARIABLE, name)
    }
}

class ArgVarExpr(name: String, executorState: RayExecutorState) : VarExpr(name, executorState) {
    override fun eval(): RayObject<*> {
        return executorState.scopeManager.currentScopeInstance.getVariable(name)?.ascObject as RayObject<*>?
            ?: throw RayError.new(RayErrors.INVALID_ARG_REFERENCE, name)
    }
}
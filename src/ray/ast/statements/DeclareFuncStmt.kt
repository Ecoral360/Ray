package ray.ast.statements

import org.ascore.ast.buildingBlocs.Expression
import org.ascore.ast.buildingBlocs.Statement
import org.ascore.executor.ASCExecutor
import org.ascore.lang.objects.ASCObject
import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.objects.RayObject
import ray.objects.function.RayCallable

class DeclareFuncStmt(val name: String, val value: Expression<*>, executorInstance: ASCExecutor<RayExecutorState>) :
    Statement(executorInstance) {
    init {
        executorInstance.executorState.scopeManager.currentScope.declareVariable(
            ASCVariable<RayObject<*>>(
                name,
                ASCObject.noValue()
            )
        )
    }

    override fun execute(): Any? {
        val value = this.value.eval() as RayCallable

        executorInstance.executorState.scopeManager.currentScopeInstance.getVariable(name).ascObject =
            value.withName(name)

        return null
    }
}
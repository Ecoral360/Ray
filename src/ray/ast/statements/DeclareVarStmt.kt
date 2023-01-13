package ray.ast.statements

import org.ascore.ast.buildingBlocs.Expression
import org.ascore.ast.buildingBlocs.Statement
import org.ascore.executor.ASCExecutor
import org.ascore.lang.objects.ASCObject
import org.ascore.lang.objects.ASCVariable
import org.ascore.lang.objects.ASScope
import ray.execution.RayExecutorState
import ray.execution.getVariable
import ray.objects.RayObject

class DeclareVarStmt(val name: String, val value: Expression<*>, executorInstance: ASCExecutor<RayExecutorState>) :
    Statement(executorInstance) {
    init {
        val scopeManager = executorInstance.executorState.scopeManager
        if (scopeManager.scopeStack.empty() || scopeManager.scopeStack.all { it.getVariable { v -> v.name == name } == null }) {
            scopeManager.currentScope.declareVariable(ASCVariable<RayObject<*>>(name, ASCObject.noValue()))
        }
    }

    override fun execute(): Any? {
        val value = this.value.eval() as RayObject<*>
        executorInstance.executorState.scopeManager.currentScopeInstance.getVariable(name).ascObject = value
        return null
    }
}

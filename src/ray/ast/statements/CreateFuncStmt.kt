package ray.ast.statements

import org.ascore.ast.buildingBlocs.Statement
import org.ascore.executor.ASCExecutor
import org.ascore.executor.Coordinate
import org.ascore.lang.objects.ASCVariable
import org.ascore.lang.objects.ASScope
import org.ascore.managers.scope.ASScopeManager
import org.ascore.tokens.Token
import ray.execution.RayExecutorState
import ray.objects.RayObject
import ray.objects.function.RayFunction

class CreateFuncStmt(val name: String, val function: RayFunction, executorInstance: ASCExecutor<RayExecutorState>) :
    Statement(executorInstance) {
    private val scope: ASScope

    init {
        executorInstance.executorState.scopeManager.currentScope.declareVariable(
            ASCVariable(function.name, function)
        )
        scope = executorInstance.executorState.scopeManager.makeNewCurrentScope()
    }

    override fun execute(): Any? {
        val scope = ASScope(this.scope)
        if (function.type.isPostfix()) { // declare @L
            scope.declareVariable(ASCVariable("@L", RayObject.RAY_NOTHING))
        }
        if (function.type.isPrefix()) { // declare @R
            scope.declareVariable(ASCVariable("@R", RayObject.RAY_NOTHING))
        }

        function.scope = scope
        scope.setParent(executorInstance.executorState.scopeManager.currentScopeInstance)
        return null
    }

    override fun getNextCoordinate(coord: Coordinate, ligne: MutableList<Token>): Coordinate {
        val currScope = coord.scope
        val newScope = ASScopeManager.formatNewScope(
            ASScopeManager.ScopeKind.FONCTION, currScope, function.getFuncSignature().hashCode().toString()
        )
        return Coordinate(executorInstance.nouveauScope(newScope))
    }
}
package ray.ast.statements

import org.ascore.ast.buildingBlocs.Expression
import org.ascore.ast.buildingBlocs.Statement
import org.ascore.executor.ASCExecutor
import org.ascore.executor.Coordinate
import org.ascore.tokens.Token
import ray.execution.RayExecutorState

class EndFuncStmt(val expression: Expression<*>, executorInstance: ASCExecutor<RayExecutorState>) :
    Statement(executorInstance) {
    init {
        executorInstance.executorState.scopeManager.popCurrentScope()
    }

    override fun execute(): Any? {
        return expression.eval()
    }

    override fun getNextCoordinate(coord: Coordinate, ligne: MutableList<Token>): Coordinate {
        return Coordinate(executorInstance.finScope())
    }
}
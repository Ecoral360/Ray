package ray.ast.statements

import org.ascore.ast.buildingBlocs.Expression
import org.ascore.ast.buildingBlocs.Statement
import org.ascore.executor.ASCExecutor
import org.ascore.executor.Coordinate
import org.ascore.lang.objects.ASCObject
import org.ascore.lang.objects.ASCVariable
import org.ascore.lang.objects.ASScope
import org.ascore.tokens.Token
import ray.execution.RayExecutorState
import ray.objects.RayObject

class WhileStmt(val value: Expression<*>, executorInstance: ASCExecutor<RayExecutorState>) :
    Statement(executorInstance) {
    private val scope: ASScope

    init {
        scope = executorInstance.executorState.scopeManager.makeNewCurrentScope()
    }

    override fun execute(): Any? {
        val value = this.value.eval() as RayObject<*>
        return null
    }

    override fun getNextCoordinate(coord: Coordinate?, ligne: MutableList<Token>?): Coordinate {
        return super.getNextCoordinate(coord, ligne)
    }
}
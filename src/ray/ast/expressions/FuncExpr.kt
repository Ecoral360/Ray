package ray.ast.expressions

import org.ascore.ast.buildingBlocs.Expression
import ray.execution.RayExecutorState
import ray.execution.getVariables
import ray.objects.function.RayCallable
import ray.objects.function.RayPartialFunction

class FuncExpr(val funcName: String, val executorState: RayExecutorState) : Expression<RayCallable> {

    fun toPartial(): PartialFuncExpr = PartialFuncExpr(funcName, executorState)

    override fun eval(): RayPartialFunction {
        val functions = executorState.scopeManager.currentScopeInstance.getVariables {
            val obj = it.ascObject
            obj is RayCallable && obj.name == funcName
        }.map { it.ascObject as RayCallable }

        return RayPartialFunction(funcName, functions)
    }
}
package ray.ast.expressions

import org.ascore.ast.buildingBlocs.Expression
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.execution.getVariables
import ray.objects.RayFunctionType
import ray.objects.function.RayFunction

class PartialFuncExpr(val funcName: String) : Expression<RayFunction> {

    fun possibleFunctionsFrom(executorState: RayExecutorState, possibleTypes: List<RayFunctionType>): RayFunction {
        val functions = executorState.scopeManager.currentScopeInstance.getVariables {
            val obj = it.ascObject
            obj is RayFunction && obj.name == funcName
        }.map { it.ascObject as RayFunction }

        // no function with the same name: error is an UNKNOWN_VARIABLE
        if (functions.isEmpty()) throw RayError.new(RayErrors.UNKNOWN_VARIABLE, funcName)

        val matchingFunctions = functions.filter { functionCandidate -> possibleTypes.any { functionCandidate.type.matches(it) } }

        if (matchingFunctions.size > 1) throw RayError(RayErrors.INVALID_FUNCTION_CALL, "")

        return matchingFunctions[0]
    }

    override fun eval(): RayFunction {
        TODO("Not yet implemented")
    }
}
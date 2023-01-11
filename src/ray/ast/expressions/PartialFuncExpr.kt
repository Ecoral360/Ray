package ray.ast.expressions

import org.ascore.ast.buildingBlocs.Expression
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.execution.getVariables
import ray.objects.RayFunctionType
import ray.objects.function.RayCallable
import ray.objects.function.RayModuleFunction
import ray.objects.function.RayPartialFunction

class PartialFuncExpr(val funcName: String, val executorState: RayExecutorState) : Expression<RayCallable> {

    fun evalPartialFunction(possibleTypes: List<RayFunctionType>): RayPartialFunction {
        val functions = executorState.scopeManager.currentScopeInstance.getVariables {
            val obj = it.ascObject
            obj is RayModuleFunction && obj.name == funcName
        }.map { it.ascObject as RayModuleFunction }

        // no function with the same name: error is an UNKNOWN_VARIABLE
        if (functions.isEmpty()) throw RayError.new(RayErrors.UNKNOWN_VARIABLE, funcName)

        val matchingFunctions =
            functions.filter { functionCandidate -> possibleTypes.any { functionCandidate.type.matches(it) } }

        if (matchingFunctions.isEmpty()) {
            throw RayError.new(RayErrors.UNKNOWN_FUNCTION_SIGNATURE,
                functions.joinToString(" or ") { it.getFuncSignature() },
                possibleTypes.map { it.getTypeSignature() })
        }

        return RayPartialFunction(funcName, matchingFunctions)
    }

    override fun eval(): RayPartialFunction {
        val functions = executorState.scopeManager.currentScopeInstance.getVariables {
            val obj = it.ascObject
            obj is RayModuleFunction && obj.name == funcName
        }.map { it.ascObject as RayModuleFunction }
        if (functions.isEmpty()) throw RayError.new(RayErrors.UNKNOWN_VARIABLE, funcName)

        return RayPartialFunction(funcName, functions)
    }
}
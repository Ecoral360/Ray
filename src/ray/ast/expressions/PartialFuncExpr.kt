package ray.ast.expressions

import org.ascore.ast.buildingBlocs.Expression
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.execution.getVariables
import ray.objects.RayFunctionType
import ray.objects.RaySimpleType
import ray.objects.function.RayCallable
import ray.objects.function.RayFunction
import ray.objects.function.RayPartialFunction

class PartialFuncExpr(val funcName: String) : Expression<RayCallable> {

    fun evalPartialFunction(executorState: RayExecutorState, possibleTypes: List<RayFunctionType>): RayPartialFunction {
        val functions = executorState.scopeManager.currentScopeInstance.getVariables {
            val obj = it.ascObject
            obj is RayFunction && obj.name == funcName
        }.map { it.ascObject as RayFunction }

        // no function with the same name: error is an UNKNOWN_VARIABLE
        if (functions.isEmpty()) throw RayError.new(RayErrors.UNKNOWN_VARIABLE, funcName)

        val matchingFunctions = functions.filter { functionCandidate -> possibleTypes.any { functionCandidate.type.matches(it) } }

        if (matchingFunctions.isEmpty()) {
            throw RayError.new(RayErrors.UNKNOWN_FUNCTION_SIGNATURE,
                    "", //RayFunction.formatSignature(funcName, functions.map {  }),
                    possibleTypes.map { it.getTypeSignature() })
        }

        return RayPartialFunction(funcName, matchingFunctions)
    }

    override fun eval(): RayFunction {
        TODO("Not yet implemented")
    }
}
package ray.ast.expressions

import org.ascore.ast.buildingBlocs.Expression
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.execution.getVariables
import ray.objects.RayFunctionType
import ray.objects.RayObject
import ray.objects.RaySimpleType
import ray.objects.function.RayFunction

class CallFuncExpr(
    val functionName: String,
    val leftArg: Expression<*>?,
    val rightArg: Expression<*>?,
    val executorState: RayExecutorState
) : Expression<RayObject<*>> {

    private fun evalLeftArgIsFunction(): RayObject<*> {
        leftArg as PartialFuncExpr
        val rightArg = this.rightArg?.eval() as RayObject<*>?

        val typeSignature = RayFunctionType(
            RaySimpleType.UNKNOWN,
            rightArg?.type ?: RaySimpleType.NOTHING,
            RaySimpleType.UNKNOWN
        )

        // find all the functions with a matching name
        val functions = executorState.scopeManager.currentScopeInstance.getVariables {
            val obj = it.ascObject
            obj is RayFunction && obj.name == functionName
        }.map { it.ascObject as RayFunction }

        // no function with the same name: error is an UNKNOWN_VARIABLE
        if (functions.isEmpty()) throw RayError.new(RayErrors.UNKNOWN_VARIABLE, functionName)

        val functionCandidates =
            functions.filter { typeSignature.matches(it.type) && it.type.leftType is RayFunctionType }

        val leftArgFunction =
            leftArg.evalPartialFunction(functionCandidates.map { it.type.leftType as RayFunctionType })

        val function = functionCandidates.find { leftArgFunction.matches(it.type.leftType) }

        return function?.call(Pair(leftArgFunction, rightArg))
            ?: throw RayError.new(RayErrors.UNKNOWN_FUNCTION_SIGNATURE,
                RayFunction.formatSignature(functionName, typeSignature),
                functions.map { it.getFuncSignature() })
    }

    private fun evalRightArgIsFunction(): RayObject<*> {
        rightArg as PartialFuncExpr
        val leftArg = this.leftArg?.eval() as RayObject<*>?

        val typeSignature = RayFunctionType(
            leftArg?.type ?: RaySimpleType.NOTHING,
            RaySimpleType.UNKNOWN,
            RaySimpleType.UNKNOWN
        )

        // find all the functions with a matching name
        val functions = executorState.scopeManager.currentScopeInstance.getVariables {
            val obj = it.ascObject
            obj is RayFunction && obj.name == functionName
        }.map { it.ascObject as RayFunction }

        // no function with the same name: error is an UNKNOWN_VARIABLE
        if (functions.isEmpty()) throw RayError.new(RayErrors.UNKNOWN_VARIABLE, functionName)

        val functionCandidates =
            functions.filter { typeSignature.matches(it.type) && it.type.rightType is RayFunctionType }

        val rightArgFunction =
            rightArg.evalPartialFunction(functionCandidates.map { it.type.rightType as RayFunctionType })

        val function = functionCandidates.find { rightArgFunction.matches(it.type.rightType) }

        return function?.call(Pair(leftArg, rightArgFunction))
            ?: throw RayError.new(RayErrors.UNKNOWN_FUNCTION_SIGNATURE,
                RayFunction.formatSignature(functionName, typeSignature),
                functions.map { it.getFuncSignature() })
    }

    override fun eval(): RayObject<*> {
        if (leftArg is PartialFuncExpr) return evalLeftArgIsFunction()
        if (rightArg is PartialFuncExpr) return evalRightArgIsFunction()

        val leftArg = this.leftArg?.eval() as RayObject<*>?
        val rightArg = this.rightArg?.eval() as RayObject<*>?

        val typeSignature = RayFunctionType(
            leftArg?.type ?: RaySimpleType.NOTHING, rightArg?.type
                ?: RaySimpleType.NOTHING, RaySimpleType.UNKNOWN
        )

        // find all the functions with a matching name
        val functions = executorState.scopeManager.currentScopeInstance.getVariables {
            val obj = it.ascObject
            obj is RayFunction && obj.name == functionName
        }.map { it.ascObject as RayFunction }

        // no function with the same name: error is an UNKNOWN_VARIABLE
        if (functions.isEmpty()) throw RayError.new(RayErrors.UNKNOWN_VARIABLE, functionName)

        val function = functions.filter { typeSignature.matches(it.type) }
            .minByOrNull { it.type.getTypeSignature().count { c -> c == RaySimpleType.ANY.getTypeSymbol()[0] } }

        return function?.call(Pair(leftArg, rightArg))
        // no function with a matching type signature: error is an UNKNOWN_FUNCTION_SIGNATURE
            ?: throw RayError.new(RayErrors.UNKNOWN_FUNCTION_SIGNATURE,
                RayFunction.formatSignature(functionName, typeSignature),
                functions.map { it.getFuncSignature() })
    }
}
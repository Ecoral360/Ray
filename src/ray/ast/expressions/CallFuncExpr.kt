package ray.ast.expressions

import org.ascore.ast.buildingBlocs.Expression
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.execution.getVariables
import ray.objects.RayFunctionType
import ray.objects.RayObject
import ray.objects.RaySimpleType
import ray.objects.function.RayCallable
import ray.objects.function.RayModuleFunction

class CallFuncExpr(
    val funcName: String,
    val leftArg: Expression<*>?,
    val rightArg: Expression<*>?,
    val executorState: RayExecutorState,
    val functionToCall: CallFuncExpr? = null  // only set if the function is CallFuncExpr
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
            obj is RayCallable && obj.name == funcName
        }.map { it.ascObject as RayCallable }

        // no function with the same name: error is an UNKNOWN_VARIABLE
        if (functions.isEmpty()) throw RayError.new(RayErrors.UNKNOWN_VARIABLE, funcName)

        val functionCandidates =
            functions.filter { typeSignature.matches(it.type) && it.type.leftType is RayFunctionType }

        val leftArgFunction =
            leftArg.evalPartialFunction(functionCandidates.map { it.type.leftType as RayFunctionType })

        val function = functionCandidates.find { leftArgFunction.matches(it.type.leftType) }

        return function?.call(Pair(leftArgFunction, rightArg))
            ?: throw RayError.new(RayErrors.UNKNOWN_FUNCTION_SIGNATURE,
                RayModuleFunction.formatSignature(funcName, typeSignature),
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
            obj is RayCallable && obj.name == funcName
        }.map { it.ascObject as RayCallable }

        // no function with the same name: error is an UNKNOWN_VARIABLE
        if (functions.isEmpty()) throw RayError.new(RayErrors.UNKNOWN_VARIABLE, funcName)

        val functionCandidates =
            functions.filter { typeSignature.matches(it.type) && it.type.rightType is RayFunctionType }

        val rightArgFunction =
            rightArg.evalPartialFunction(functionCandidates.map { it.type.rightType as RayFunctionType })

        val function = functionCandidates.find { rightArgFunction.matches(it.type.rightType) }

        return function?.call(Pair(leftArg, rightArgFunction))
            ?: throw RayError.new(RayErrors.UNKNOWN_FUNCTION_SIGNATURE,
                RayModuleFunction.formatSignature(funcName, typeSignature),
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

        if (functionToCall != null) {
            // the middle operator is a function call in of itself
            val functionResult = functionToCall.eval()
            if (functionResult is RayCallable)
                return functionResult.call(Pair(leftArg, rightArg))
            else throw RayError.new(
                RayErrors.TYPE_ERROR,
                typeSignature.getTypeSignature(),
                functionResult.type.getTypeSignature()
            )
        }

        // find all the functions with a matching name
        val functions = executorState.scopeManager.currentScopeInstance.getVariables {
            val obj = it.ascObject
            obj is RayCallable && obj.name == funcName
        }.map { it.ascObject as RayCallable }

        // no function with the same name: error is an UNKNOWN_VARIABLE
        if (functions.isEmpty()) throw RayError.new(RayErrors.UNKNOWN_VARIABLE, funcName)

        val function = functions.filter { typeSignature.matches(it.type) }
            .minByOrNull { it.type.getTypeSignature().count { c -> c == RaySimpleType.ANY.getTypeSymbol()[0] } }

        return function?.call(Pair(leftArg, rightArg))
        // no function with a matching type signature: error is an UNKNOWN_FUNCTION_SIGNATURE
            ?: throw RayError.new(RayErrors.UNKNOWN_FUNCTION_SIGNATURE,
                RayModuleFunction.formatSignature(funcName, typeSignature),
                functions.map { it.getFuncSignature() })


    }
}
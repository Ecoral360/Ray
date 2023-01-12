package ray.parser

import org.ascore.ast.AstNode
import org.ascore.ast.buildingBlocs.Expression
import org.ascore.ast.buildingBlocs.Statement
import org.ascore.errors.ASCErrors
import org.ascore.executor.ASCExecutor
import org.ascore.generators.ast.AstGenerator
import org.ascore.lang.objects.ASCVariable
import org.ascore.managers.scope.ASScopeManager
import org.ascore.tokens.Token
import ray.ast.expressions.*
import ray.ast.statements.*
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.lexer.RayLexer
import ray.objects.RayFunctionType
import ray.objects.RayObject
import ray.objects.function.RayCallable
import ray.objects.function.RayFunction
import ray.objects.function.RayModuleFunction
import ray.objects.primitive.RayFloat
import ray.objects.primitive.RayInt
import ray.objects.primitive.RayString
import java.util.*


/**
 * The parser for the Ray language.
 *
 * This parser is responsible for defining the rules for parsing the Ray language. The actual parsing is done by the
 * [AstGenerator] class in accordance with the rules defined in this class.
 *
 *  * Edit the [addExpressions] method to add new expressions to the language.
 *  * Edit the [addStatements] method to add new statements to the language.
 *
 *  @param executorInstance the executor instance to use for executing the AST
 */
class RayParser(executorInstance: ASCExecutor<RayExecutorState>) : AstGenerator<RayAstFrameKind>() {
    private val executorInstance: ASCExecutor<RayExecutorState>

    init {
        setPatternProccessor(executorInstance.getLexer<RayLexer>()::remplaceCategoriesByMembers)
        defineAstFrame(RayAstFrameKind.DEFAULT)
        addStatements()
        addExpressions()
        defineAstFrame(RayAstFrameKind.FUNCTION)
        addStatementsFunction()
        addExpressionsFunction()
        pushAstFrame(RayAstFrameKind.DEFAULT)
        this.executorInstance = executorInstance
    }

    /**
     * Defines the rules of the statements of the language.
     */
    private fun addStatements() {
        // add your statements here
        addStatement("VARIABLE ASSIGN expression") { p: List<Any> ->
            DeclareVarStmt((p[0] as Token).value, p[2] as Expression<*>, executorInstance)
        }

        addStatement("FUNCTION ASSIGN expression") { p: List<Any> ->
            DeclareFuncStmt((p[0] as Token).value, p[2] as Expression<*>, executorInstance)
        }


        val subAstFunction = Hashtable<String, AstNode<out Expression<*>>>()
        subAstFunction["LEFT_ARG~RIGHT_ARG"] = AstNode.from(1) { p ->
            VarExpr(
                (p[0] as Token).value,
                executorInstance.executorState
            )
        }

        subAstFunction["PAREN_OPEN #expression PAREN_CLOSE"] = AstNode.from(-2) { p: List<Any> ->
            evalOneExpr(ArrayList(p.subList(1, p.size - 1)), subAstFunction)
        }

        addStatement(
            "DEF FUNCTION_DEF ASSIGN expression",
            object : AstNode<DeclareFuncStmt>(*subAstFunction.entries.toTypedArray()) {
                override fun apply(p: List<Any>, idxVariante: Int): DeclareFuncStmt {
                    val (functionName, functionSignature) = (p[1] as Token).value.split("@", limit = 2)
                    val body = p[3] as Expression<*>

                    val functionType = RayFunctionType.parseTypeSignature(functionSignature) ?: throw RayError.new(
                        RayErrors.INVALID_FUNCTION_CALL,
                        functionName,
                        "AAAAAAA"
                    ) // TODO: real error

                    val currentScope = executorInstance.executorState.scopeManager.currentScope
                    if (functionType.isPostfix()) { // declare @L
                        currentScope.declareVariable(ASCVariable("@L", RayObject.RAY_NOTHING))
                    }
                    if (functionType.isPrefix()) { // declare @R
                        currentScope.declareVariable(ASCVariable("@R", RayObject.RAY_NOTHING))
                    }

                    val function = RayModuleFunction(functionName, functionType) { args ->
                        val (left, right) = args
                        val scopeInstance = executorInstance.executorState.scopeManager.currentScopeInstance
                        if (left != null) scopeInstance.getVariable("@L").ascObject = left
                        if (right != null) scopeInstance.getVariable("@R").ascObject = right
                        body.eval() as RayObject<*>
                    }

                    return DeclareFuncStmt(functionName, { function }, executorInstance)
                }
            }
        )

        addStatement(
            "DEF FUNCTION_DEF"
        ) { p ->
            pushAstFrame(RayAstFrameKind.FUNCTION)
            val functionDef = (p[1] as Token).value
            val (functionName, functionSignature) = functionDef.split("(?>`.*?`)?@".toRegex(), limit = 2)

            val functionType = RayFunctionType.parseTypeSignature(functionSignature) ?: throw RayError.new(
                RayErrors.INVALID_FUNCTION_CALL,
                functionName,
                "AAAAAAA"
            ) // TODO: real error

            val function = RayFunction(functionName, functionType, executorInstance)

            CreateFuncStmt(functionName, function, executorInstance)
        }


        addStatement("expression")
        { p: List<Any> -> PrintStmt(p[0] as Expression<*>) }

        addStatement("")
        { _: List<Any> -> Statement.EMPTY_STATEMENT }
    }

    /**
     * Defines the rules of the expressions of the language.
     */
    private fun addExpressions() {

        // Parentheses
        addExpression("PAREN_OPEN #expression PAREN_CLOSE") { p: List<Any> ->
            evalOneExpr(ArrayList(p.subList(1, p.size - 1)), null)
        }

        // add your expressions here
        addExpression("{datatypes}~VARIABLE") { p: List<Any> ->
            val token = p[0] as Token
            when (token.name()) {
                "INT" -> ConstValueExpr(RayInt(token))
                "FLOAT" -> ConstValueExpr(RayFloat(token))
                "STRING" -> ConstValueExpr(RayString(token))
                "VARIABLE" -> VarExpr(token.value, executorInstance.executorState)
                else -> throw NoSuchElementException(token.name())
            }
        }

        addExpression("{func_arg}") { p ->
            throw ASCErrors.ErreurSyntaxe("The '${(p[0] as Token).value}' keyword is not allowed outside of function definition.")
        }

        // To make the arrays
        addExpression(
            "expression expression expression~" +
                    "expression expression"
        ) { p: List<Any>, variant ->
            when (variant) {
                0 -> {
                    val expr1 = p[0] as Expression<*>
                    val expr2 = p[1] as Expression<*>
                    val expr3 = p[2] as Expression<*>

                    when {
                        expr1 is MakeArrayExpr && (expr2 is ConstValueExpr || expr2 is VarExpr) && (expr3 is ConstValueExpr || expr3 is VarExpr) -> {
                            expr1.addElement(expr2)
                            expr1.addElement(expr3)
                            expr1
                        }

                        (expr1 is ConstValueExpr || expr1 is VarExpr) && (expr2 is ConstValueExpr || expr2 is VarExpr) && (expr3 is ConstValueExpr || expr3 is VarExpr) -> {
                            MakeArrayExpr(expr1, expr2, expr3)
                        }

                        else -> parseFunctionThreeExpressions(p)
                    }
                }

                1 -> {
                    val expr1 = p[0] as Expression<*>
                    val expr2 = p[1] as Expression<*>

                    when {
                        expr1 is MakeArrayExpr && (expr2 is ConstValueExpr || expr2 is VarExpr) -> {
                            expr1.addElement(expr2)
                            expr1
                        }

                        (expr1 is ConstValueExpr || expr1 is VarExpr) && (expr2 is ConstValueExpr || expr2 is VarExpr) -> {
                            MakeArrayExpr(expr1, expr2)
                        }

                        else -> parseFunctionTwoExpressions(p)
                    }
                }

                else -> TODO("Can't happen")
            }
        }

        addExpression("FUNCTION") { p ->
            FuncExpr((p[0] as Token).value, executorInstance.executorState)
        }


        // Function calls
        addExpression(
            "expression expression expression~" +
                    "expression expression~" +
                    "expression"
        ) { p: List<Any>, variant: Int ->
            when (variant) {
                0 -> parseFunctionThreeExpressions(p)
                1 -> parseFunctionTwoExpressions(p)
                2 -> {
                    val expr = p[0]
                    if (expr is FuncExpr) CallFuncExpr(expr.funcName, null, null, executorInstance.executorState)
                    else expr as Expression<*>
                }

                else -> TODO("ERROR")
            }
        }
    }

    private fun addStatementsFunction() {
        addStatement("expression END") { p ->
            val currScope = executorInstance.obtenirCoordRunTime().scope

            popAstFrame()
            when (ASScopeManager.getScopeKind(currScope)) {
                ASScopeManager.ScopeKind.FONCTION -> EndFuncStmt(p[0] as Expression<*>, executorInstance)

                else -> TODO("Handle other cases")
            }
        }

        addStatements()
    }

    private fun addExpressionsFunction() {
        addExpressions()

        addExpression("{func_arg}", AstNode.from(2) { p ->
            VarExpr(
                (p[0] as Token).value,
                executorInstance.executorState
            )
        })
    }

    private fun parseFunctionThreeExpressions(p: List<Any>): CallFuncExpr {
        val (expr1, expr2, expr3) = p
        val funcName = when (expr2) {
            is FuncExpr -> expr2.funcName
            is CallFuncExpr -> expr2.funcName
            else -> null
        }

        return when {
            // the `expression FUNCTION expression` call (infix)
            expr1 !is FuncExpr && funcName != null && expr3 !is FuncExpr -> {
                CallFuncExpr(
                    funcName,
                    expr1 as Expression<*>,
                    expr3 as Expression<*>,
                    executorInstance.executorState,
                    if (expr2 is CallFuncExpr) expr2 else null
                )
            }

            // the `FUNCTION FUNCTION expression` call (infix)
            expr1 is FuncExpr && funcName != null && expr3 !is FuncExpr -> {
                CallFuncExpr(
                    funcName,
                    expr1.toPartial(),
                    expr3 as Expression<*>,
                    executorInstance.executorState,
                    if (expr2 is CallFuncExpr) expr2 else null
                )
            }

            // the `expression FUNCTION FUNCTION` call (infix)
            expr1 !is FuncExpr && funcName != null && expr3 is FuncExpr -> {
                CallFuncExpr(
                    funcName,
                    expr1 as Expression<*>,
                    expr3.toPartial(),
                    executorInstance.executorState,
                    if (expr2 is CallFuncExpr) expr2 else null
                )
            }

            // the `FUNCTION FUNCTION FUNCTION` call (infix)
            expr1 is FuncExpr && funcName != null && expr3 is FuncExpr -> {
                CallFuncExpr(
                    funcName,
                    expr1.toPartial(),
                    expr3.toPartial(),
                    executorInstance.executorState,
                    if (expr2 is CallFuncExpr) expr2 else null
                )
            }


            else -> TODO("Add a real error $p")
        }
    }

    private fun parseFunctionTwoExpressions(p: List<Any>): Expression<RayObject<*>> {
        val (expr1, expr2) = p
        return when {
            // the `FUNCTION expression` call (prefix)
            expr1 is FuncExpr && expr2 !is FuncExpr -> {
                CallFuncExpr(
                    expr1.funcName,
                    null,
                    expr2 as Expression<*>,
                    executorInstance.executorState
                )
            }

            // the `expression FUNCTION` call (postfix)
            expr1 !is FuncExpr && expr2 is FuncExpr -> {
                CallFuncExpr(
                    expr2.funcName,
                    expr1 as Expression<*>,
                    null,
                    executorInstance.executorState
                )
            }

            expr1 is CallFuncExpr && expr2 is Expression<*> -> {
                Expression { (expr1.eval() as RayCallable).call(Pair(null, expr2.eval() as RayObject<*>)) }
            }

            expr1 is Expression<*> && expr2 is CallFuncExpr -> {
                Expression { (expr2.eval() as RayCallable).call(Pair(expr1.eval() as RayObject<*>, null)) }
            }

            // the `FUNCTION FUNCTION` call (prefix)
            expr1 is FuncExpr && expr2 is FuncExpr -> {
                CallFuncExpr(
                    expr2.funcName,
                    expr1.toPartial(),
                    null,
                    executorInstance.executorState
                )
            }

            else -> TODO("Add a real error for case $p")
        }
    }
}


package ray.parser

import ray.ast.statements.PrintStmt
import ray.execution.RayExecutorState
import ray.lexer.RayLexer
import ray.objects.primitive.RayFloat
import ray.objects.primitive.RayInt
import ray.objects.primitive.RayString
import org.ascore.ast.buildingBlocs.Expression
import org.ascore.ast.buildingBlocs.Statement
import org.ascore.errors.ASCErrors
import org.ascore.executor.ASCExecutor
import org.ascore.generators.ast.AstGenerator
import org.ascore.tokens.Token
import ray.ast.expressions.*
import ray.ast.statements.DeclareFuncStmt
import ray.ast.statements.DeclareVarStmt
import ray.objects.RayObject
import ray.objects.function.RayCallable

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

        addStatement("FUNCTION FUNC_DEF expression") { p: List<Any> ->
            DeclareFuncStmt((p[0] as Token).value, p[2] as Expression<*>, executorInstance)
        }

        addStatement("expression") { p: List<Any> -> PrintStmt(p[0] as Expression<*>) }
        addStatement("") { _: List<Any> -> Statement.EMPTY_STATEMENT }
    }

    /**
     * Defines the rules of the expressions of the language.
     */
    private fun addExpressions() {
        addExpression("{func_arg}") { p ->
            throw ASCErrors.ErreurSyntaxe("The '${(p[0] as Token).value}' keyword is not allowed outside of function definition.")
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

        // Parentheses
        addExpression("PAREN_OPEN #expression PAREN_CLOSE") { p: List<Any> ->
            evalOneExpr(ArrayList(p.subList(1, p.size - 1)), null)
        }

        // To make the arrays
        addExpression("expression expression") { p: List<Any> ->
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

        addExpression("FUNCTION") { p ->
            FuncExpr((p[0] as Token).value, executorInstance.executorState)
        }

        // Function calls
        addExpression(
            "expression expression expression~" +
                    "expression expression"
        ) { p: List<Any>, variant: Int ->
            if (variant == 1) {
                parseFunctionTwoExpressions(p)
            } else {
                parseFunctionThreeExpressions(p)
            }
        }
    }

    private fun parseFunctionThreeExpressions(p: List<Any>): CallFuncExpr {
        val (expr1, expr2, expr3) = p
        return when {
            // the `expression FUNCTION expression` call (infix)
            expr1 !is FuncExpr && expr2 is FuncExpr && expr3 !is FuncExpr -> {
                CallFuncExpr(
                    expr2.funcName,
                    expr1 as Expression<*>,
                    expr3 as Expression<*>,
                    executorInstance.executorState
                )
            }

            // the `FUNCTION FUNCTION expression` call (infix)
            expr1 is FuncExpr && expr2 is FuncExpr && expr3 !is FuncExpr -> {
                CallFuncExpr(
                    expr2.funcName,
                    expr1.toPartial(),
                    expr3 as Expression<*>,
                    executorInstance.executorState
                )
            }

            // the `expression FUNCTION FUNCTION` call (infix)
            expr1 !is FuncExpr && expr2 is FuncExpr && expr3 is FuncExpr -> {
                CallFuncExpr(
                    expr2.funcName,
                    expr1 as Expression<*>,
                    expr3.toPartial(),
                    executorInstance.executorState
                )
            }

            // the `FUNCTION FUNCTION FUNCTION` call (infix)
            expr1 is FuncExpr && expr2 is FuncExpr && expr3 is FuncExpr -> {
                CallFuncExpr(
                    expr2.funcName,
                    expr1.toPartial(),
                    expr3.toPartial(),
                    executorInstance.executorState
                )
            }


            else -> TODO("Add a real error")
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

            else -> TODO("Add a real error")
        }
    }
}


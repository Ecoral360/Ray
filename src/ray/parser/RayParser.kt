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
import org.ascore.errors.ASCErrors.ASCError
import org.ascore.executor.ASCExecutor
import org.ascore.generators.ast.AstGenerator
import org.ascore.tokens.Token
import ray.ast.expressions.*
import ray.ast.statements.DeclareVarStmt

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

        addStatement("expression") { p: List<Any> -> PrintStmt(p[0] as Expression<*>) }
        addStatement("") { _: List<Any> -> Statement.EMPTY_STATEMENT }
    }

    /**
     * Defines the rules of the expressions of the language.
     */
    private fun addExpressions() {
        addExpression("{arg}") { p ->
            throw ASCErrors.ErreurSyntaxe("The '${(p[0] as Token).value}' keyword is not allowed outside of function definition.")
        }

        // add your expressions here
        addExpression("{datatypes}") { p: List<Any> ->
            val token = p[0] as Token
            when (token.name()) {
                "INT" -> ConstValueExpr(RayInt(token))
                "FLOAT" -> ConstValueExpr(RayFloat(token))
                "STRING" -> ConstValueExpr(RayString(token))
                else -> throw NoSuchElementException(token.name())
            }
        }

        addExpression("VARIABLE") { p: List<Any> ->
            val token = p[0] as Token
            VarExpr(token.value, executorInstance.executorState)
        }


        // Parenthesises
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

                else -> {
                    null
                }
            }
        }

        // TODO IDEA: add an expression to make partial function calls?

        // Function calls
        addExpression("FUNCTION FUNCTION expression~" +
                "expression FUNCTION FUNCTION~" +
                "expression FUNCTION expression~" +
                "FUNCTION expression~" +
                "expression FUNCTION") { p: List<Any>, variant: Int ->
            when (variant) {
                // the `FUNCTION FUNCTION expression` call (infix)
                0 -> {
                    CallFuncExpr((p[1] as Token).value, PartialFuncExpr((p[0] as Token).value), p[2] as Expression<*>, executorInstance.executorState)
                }

                // the `expression FUNCTION FUNCTION` call (infix)
                1 -> {
                    CallFuncExpr((p[1] as Token).value, p[0] as Expression<*>, PartialFuncExpr((p[2] as Token).value), executorInstance.executorState)
                }

                // the `expression FUNCTION expression` call (infix)
                2 -> {
                    CallFuncExpr((p[1] as Token).value, p[0] as Expression<*>, p[2] as Expression<*>, executorInstance.executorState)
                }

                // the `FUNCTION expression` call (prefix)
                3 -> {
                    CallFuncExpr((p[0] as Token).value, null, p[1] as Expression<*>, executorInstance.executorState)
                }

                // the `expression FUNCTION` call (postfix)
                4 -> {
                    CallFuncExpr((p[1] as Token).value, p[0] as Expression<*>, null, executorInstance.executorState)
                }

                else -> null
            }
        }
    }
}


private fun combinators(f1: Token, f2: Token, f3: Token): Expression<*> {
    TODO()
}


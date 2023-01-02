package ray

import ray.execution.RayExecutorState
import ray.execution.RayPreCompiler
import ray.lexer.RayLexer
import ray.parser.RayParser
import org.ascore.executor.ASCExecutor
import org.ascore.executor.ASCExecutorBuilder
import org.json.JSONArray
import ray.module.RayModules

val CODE = """
    a @= 1 2 3
    3 a 4 a
    """.trimIndent()

fun main() {
    val lexer = RayLexer("/ray/grammar_rules/Grammar.yaml");
    val executor = ASCExecutorBuilder<RayExecutorState>() // create an executor builder
            .withLexer(lexer) // add the lexer to the builder
            .withParser { executorInstance: ASCExecutor<RayExecutorState> ->
                RayParser(
                        executorInstance
                )
            } // add the parser to the builder
            .withExecutorState(RayExecutorState()) // add the executor state to the builder
            .withPrecompiler(RayPreCompiler()) // add the precompiler to the builder
            .build() // build the executor

    // main scope

    val compilationResult = executor.compiler(CODE, true) // compile the code

    RayModules.load(executor.executorState)

    if (compilationResult.length() != 0) {
        println(compilationResult)
        return
    }


    val executionResult = executor.executerMain(false) // execute the code

    //println(executionResult) // print the result

    showErrors(executionResult)
}


fun showErrors(execResults: JSONArray) {
    for (i in 0 until execResults.length()) {
        val execResult = execResults.getJSONObject(i)
        val id = execResult.getInt("id")
        val p = execResult.getJSONArray("p")
        if (id == 400) {
            val errorName = p.getString(0)
            val message = p.getString(1)
            val line = p.getInt(2)
            System.err.println("Error $errorName occurred at line $line: $message")
        }
    }
}



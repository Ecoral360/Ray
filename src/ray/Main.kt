package ray

import ray.execution.RayExecutorState
import ray.execution.RayPreCompiler
import ray.lexer.RayLexer
import ray.parser.RayParser
import org.ascore.executor.ASCExecutor
import org.ascore.executor.ASCExecutorBuilder
import org.json.JSONArray
import ray.module.RayModules
import java.io.File
import java.util.Scanner

val CODE = """
    
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

    val code = File("src/tictacto.ray").readLines().joinToString("\n")

    val compilationResult = executor.compiler(code, true) // compile the code

    showErrors(compilationResult)
    if (compilationResult.length() != 0) {
        return
    }

    RayModules.load(executor.executorState)

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



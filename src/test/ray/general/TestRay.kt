package test.ray.general

import org.ascore.executor.ASCExecutor
import org.ascore.executor.ASCExecutorBuilder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.testng.annotations.Test
import ray.execution.RayExecutorState
import ray.execution.RayPreCompiler
import ray.lexer.RayLexer
import ray.module.RayModules
import ray.parser.RayParser
import ray.showErrors

class TestRay {

    @ParameterizedTest
    @ValueSource(strings = [
        // code 1
        """
1 + 3
+ "56"
5 + 1 3 5 6
1 3 5.8 6 + _8 

+/1 2 3 4

" hey " , "hello" "world!" "you"

,/ "hello" " , " 1
""",
        // code 2
        """
a @= 1 2 3
a
"""
    ])
    fun test1(code: String) {
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

        val compilationResult = executor.compiler(code, true) // compile the code

        RayModules.load(executor.executorState)

        if (compilationResult.length() != 0) {
            println(compilationResult)
            return
        }


        val executionResult = executor.executerMain(false) // execute the code

        //println(executionResult) // print the result

        showErrors(executionResult)
    }
}
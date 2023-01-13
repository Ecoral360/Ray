package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.RayFunctionType
import ray.objects.RayObject
import ray.objects.RaySimpleType
import ray.objects.function.RayCallable
import ray.objects.function.RayFunction
import ray.objects.function.RayModuleFunction
import ray.objects.primitive.*

object RayControlFlowModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayModuleFunction> =
        arrayOf(

            // if
            RayModuleFunction(
                "do:",
                RayFunctionType(RayFunctionType.ANY_FUNCTION, RaySimpleType.NUMBER, RayFunctionType.ANY_FUNCTION)
            ) { args ->
                val function = args.first!!.value<RayCallable>()
                val condition = args.second!!.value<Number>().toDouble()
                RayModuleFunction("", function.type) { (leftArg, rightArg) ->
                    var i = 0.0
                    var result: RayObject<*> = RayObject.RAY_NOTHING
                    var previousResult: RayObject<*> = RayInt(0)

                    /*while (previousResult != result && i++ < condition) {
                        previousResult = result
                        result = function.call(Pair(leftArg, rightArg))
                    }*/

                    while (i++ < condition) {
                        function.call(Pair(leftArg, rightArg))
                    }

                    RayBool(false)
                }
            },

            // if
            RayModuleFunction(
                "if?",
                RayFunctionType(RayFunctionType.ANY_FUNCTION, RaySimpleType.NUMBER, RayFunctionType.ANY_FUNCTION)
            ) { args ->
                val function = args.first!!.value<RayCallable>()
                val condition = RayBool(args.second!!.value<Number>())
                println(function.name)
                if (condition.bool()) function
                else RayModuleFunction("", function.type) { RayObject.RAY_NOTHING }
            },

            // while loop
            RayModuleFunction(
                "while?",
                RayFunctionType(
                    RayFunctionType.ANY_FUNCTION,
                    RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.NOTHING, RaySimpleType.NUMBER),
                    RayFunctionType.ANY_FUNCTION
                )
            ) { args ->
                val function = args.first!!.value<RayCallable>()
                val condition = args.second!!.value<RayCallable>()
                RayModuleFunction("", function.type) { (leftArg, rightArg) ->
                    while (RayBool(condition.call(Pair(null, null)).value<Number>()).bool()) function.call(
                        Pair(
                            leftArg,
                            rightArg
                        )
                    )
                    RayObject.RAY_NOTHING
                }
            },


            // for loop
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf(

        )
}
package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.*
import ray.objects.function.RayCallable
import ray.objects.function.RayFunction
import ray.objects.primitive.RayInt
import ray.objects.primitive.RayString

object RayFunctionModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayFunction> =
        arrayOf(
            // Reduce
            RayFunction(
                "/", RayFunctionType(
                    RayFunctionType(RaySimpleType.ANY, RaySimpleType.ANY, RaySimpleType.ANY),
                    RayArrayType(RaySimpleType.ANY),
                    RaySimpleType.ANY
                )
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!.value<Array<RayObject<*>>>()

                right.reduce { acc, value -> left.call(Pair(acc, value)) }
            },

            // Map
            RayFunction(
                ".", RayFunctionType(
                    RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.ANY, RaySimpleType.ANY),
                    RayArrayType(RaySimpleType.ANY),
                    RayArrayType(RaySimpleType.ANY)
                )
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!.value<Array<RayObject<*>>>()

                RayArray(right.map { value -> left.call(Pair(null, value)) }.toTypedArray())
            },
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf()
}
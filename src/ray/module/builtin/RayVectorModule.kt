package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.*
import ray.objects.function.RayCallable
import ray.objects.function.RayFunction
import ray.objects.primitive.RayInt
import ray.objects.primitive.RayNumber
import ray.objects.primitive.plus
import ray.objects.primitive.toRayNumber

object RayVectorModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayFunction> =
        arrayOf(
            // Addition of a number and an array of numbers
            RayFunction(
                "+",
                RayFunctionType(RaySimpleType.NUMBER, RayArrayType(RaySimpleType.NUMBER), RaySimpleType.NUMBER)
            ) { args ->
                val left = args.first!!.value<Number>().toRayNumber()

                @Suppress("UNCHECKED_CAST")
                val right = args.second!!.value<Array<RayNumber>>()

                RayArray(right.map { it.plus(left) }.toTypedArray())
            },

            // Addition of an array of numbers and a number
            RayFunction(
                "+",
                RayFunctionType(RayArrayType(RaySimpleType.NUMBER), RaySimpleType.NUMBER, RaySimpleType.NUMBER)
            ) { args ->
                @Suppress("UNCHECKED_CAST")
                val left = args.first!!.value<Array<RayNumber>>()

                val right = args.second!!.value<Number>().toRayNumber()

                RayArray(left.map { it.plus(right) }.toTypedArray())
            },

            // Equals
            RayFunction(
                "=",
                RayFunctionType(
                    RayArrayType(RaySimpleType.ANY),
                    RayArrayType(RaySimpleType.ANY),
                    RayArrayType(RaySimpleType.NUMBER)
                )
            ) { args ->
                val left = args.first!!.value<Array<RayObject<*>>>()
                val right = args.second!!.value<Array<RayObject<*>>>()

                RayArray(left.zip(right)
                    .map { (l, r) -> (if (l == r) 1 else 0).toRayNumber() }
                    .toTypedArray())
            },

            // Index
            RayFunction(
                "[",
                RayFunctionType(RayArrayType(), RaySimpleType.NUMBER, RaySimpleType.ANY)
            ) { args ->
                val left = args.first!!.value<Array<RayObject<*>>>()
                val right = args.second!!.value<Int>().let { if (it < 0) left.size - it else it }

                left[right]
            },
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf()
}
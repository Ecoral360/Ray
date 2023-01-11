package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.*
import ray.objects.function.RayModuleFunction
import ray.objects.primitive.RayNumber
import ray.objects.primitive.plus
import ray.objects.primitive.toRayNumber

object RayVectorModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayModuleFunction> =
        arrayOf(
            // Addition of a number and an array of numbers
            RayModuleFunction(
                "+",
                RayFunctionType(RaySimpleType.NUMBER, RayArrayType(RaySimpleType.NUMBER), RaySimpleType.NUMBER)
            ) { args ->
                val left = args.first!!.value<Number>().toRayNumber()

                @Suppress("UNCHECKED_CAST")
                val right = args.second!!.value<Array<RayNumber>>()

                RayArray(right.map { it.plus(left) }.toTypedArray())
            },

            // Addition of an array of numbers and a number
            RayModuleFunction(
                "+",
                RayFunctionType(RayArrayType(RaySimpleType.NUMBER), RaySimpleType.NUMBER, RaySimpleType.NUMBER)
            ) { args ->
                @Suppress("UNCHECKED_CAST")
                val left = args.first!!.value<Array<RayNumber>>()

                val right = args.second!!.value<Number>().toRayNumber()

                RayArray(left.map { it.plus(right) }.toTypedArray())
            },

            // Equals
            RayModuleFunction(
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
            RayModuleFunction(
                "[",
                RayFunctionType(RayArrayType(), RaySimpleType.NUMBER, RaySimpleType.ANY)
            ) { args ->
                val left = args.first!!.value<Array<RayObject<*>>>()
                val right = args.second!!.value<Int>().let { if (it < 0) left.size - it else it }

                left[right]
            },

//            // Shape
//            RayFunction(
//                "s.",
//                RayFunctionType(RayArrayType(RaySimpleType.NUMBER), RayArrayType(), RayArrayType())
//            ) { args ->
//                val left = args.first!!.value<Array<RayObject<*>>>()
//                val right = args.second!!.value<Array<RayObject<*>>>()
//
//                left[right]
//            },

            // Shape
            RayModuleFunction(
                "p.",
                RayFunctionType(RaySimpleType.NUMBER, RayArrayType(), RayArrayType())
            ) { args ->
                val left = args.first!!.value<Int>()
                val right = args.second!!.value<Array<RayObject<*>>>()

                RayArray((0..right.size / left).map {
                    RayArray(right.take(it * left).toTypedArray())
                }.toTypedArray())
            },

            // Shape
            RayModuleFunction(
                "p.",
                RayFunctionType(RayArrayType(RaySimpleType.NUMBER), RaySimpleType.ANY_NON_FUNC, RayArrayType())
            ) { args ->
                val left = args.first!!.value<Array<RayObject<*>>>()
                val right = args.second!!

                RayArray((0 until left[0].value()).map {
                    RayArray(
                        (0 until left[1].value()).map { right }.toTypedArray()
                    )
                }.toTypedArray())
            },
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf()
}
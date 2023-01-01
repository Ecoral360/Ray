package ray.module.builtin

import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.*
import ray.objects.function.RayFunction
import ray.objects.primitive.*

object RayBuiltins : RayModule {
    override fun load(executorState: RayExecutorState) =
            arrayOf(
                    // Addition of numbers
                    RayFunction("+", RayFunctionType(RaySimpleType.NUMBER, RaySimpleType.NUMBER, RaySimpleType.NUMBER)) { args ->
                        val left = args.first!!.value as Number
                        val right = args.second!!.value as Number

                        left.toRayNumber().plus(right.toRayNumber())
                    },

                    // Addition of a number and an array of numbers
                    RayFunction("+", RayFunctionType(RaySimpleType.NUMBER, RayArrayType(RaySimpleType.NUMBER), RaySimpleType.NUMBER)) { args ->
                        val left = args.first!!.value<Number>().toRayNumber()

                        @Suppress("UNCHECKED_CAST")
                        val right = args.second!!.value<Array<RayNumber>>()

                        RayArray(right.map { it.plus(left) }.toTypedArray())
                    },

                    // Addition of an array of numbers and a number
                    RayFunction("+", RayFunctionType(RayArrayType(RaySimpleType.NUMBER), RaySimpleType.NUMBER, RaySimpleType.NUMBER)) { args ->
                        @Suppress("UNCHECKED_CAST")
                        val left = args.first!!.value<Array<RayNumber>>()

                        val right = args.second!!.value<Number>().toRayNumber()

                        RayArray(left.map { it.plus(right) }.toTypedArray())
                    },


                    RayFunction("+", RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.STRING, RaySimpleType.NUMBER)) { args ->
                        val right = args.second!!.value as String
                        RayInt(right.toInt())
                    },

                    // Join
                    RayFunction(",", RayFunctionType(RaySimpleType.ANY, RaySimpleType.ANY, RayArrayType(RaySimpleType.ANY))) { args ->
                        val left = args.first!!
                        val right = args.second!!
                        when {
                            left is RayArray<*> && right is RayArray<*> -> {
                                RayArray(arrayListOf(*left.value).apply { this.addAll(right.value) }.toTypedArray())
                            }

                            left is RayArray<*> -> {
                                RayArray(arrayListOf(*left.value).apply { this.add(right) }.toTypedArray())
                            }

                            right is RayArray<*> -> {
                                RayArray(arrayListOf(*right.value).apply { this.add(0, left) }.toTypedArray())
                            }

                            else -> {
                                RayArray(arrayOf(left, right))
                            }
                        }

                    },

                    // Join
                    RayFunction(",", RayFunctionType(RaySimpleType.STRING, RaySimpleType.STRING, RaySimpleType.STRING)) { args ->
                        val left = args.first!!.value<String>()
                        val right = args.second!!.value<String>()

                        RayString("$left$right")
                    },


                    // Reduce
                    RayFunction("/", RayFunctionType(
                            RayFunctionType(RaySimpleType.NUMBER, RaySimpleType.NUMBER, RaySimpleType.NUMBER),
                            RayArrayType(RaySimpleType.NUMBER),
                            RaySimpleType.NUMBER)
                    ) { args ->
                        val left = args.first!!.value as RayFunction

                        @Suppress("UNCHECKED_CAST")
                        val right = args.second!!.value as Array<RayNumber>

                        @Suppress("UNCHECKED_CAST")
                        right.reduce { acc, value -> left.call(Pair(acc, value)) as RayNumber }
                    },
            )
}
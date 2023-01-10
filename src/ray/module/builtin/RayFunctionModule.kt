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
            // Left Reduce
            RayFunction(
                "\\", RayFunctionType(
                    RayFunctionType.infix(),
                    RayArrayType(RaySimpleType.ANY),
                    RaySimpleType.ANY
                )
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!.value<Array<RayObject<*>>>()

                right.reduce { acc, value -> left.call(Pair(acc, value)) }
            },

            // Right Reduce
            RayFunction(
                "\\", RayFunctionType(
                    RayArrayType(RaySimpleType.ANY),
                    RayFunctionType.infix(),
                    RaySimpleType.ANY
                )
            ) { args ->
                val left = args.first!!.value<Array<RayObject<*>>>()
                val right = args.second!!.value<RayCallable>()

                left.reduceRight { acc, value -> right.call(Pair(acc, value)) }
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

            // Partial Right
            RayFunction(
                "&",
                RayFunctionType(RayFunctionType.any(), RaySimpleType.ANY, RayFunctionType.postfix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!

                RayFunction("", RayFunctionType.postfix()) { (leftArg, _) ->
                    left.call(Pair(leftArg!!, right))
                }
            },

            // Partial Right (right arg is a function)
            RayFunction(
                "&.",
                RayFunctionType(RayFunctionType.any(), RayFunctionType.any(), RayFunctionType.postfix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!.value<RayCallable>()

                RayFunction("", RayFunctionType.postfix()) { (leftArg, _) ->
                    left.call(Pair(leftArg!!, right))
                }
            },

            // Partial Left
            RayFunction(
                "&",
                RayFunctionType(RaySimpleType.ANY, RayFunctionType.any(), RayFunctionType.prefix())
            ) { args ->
                val left = args.first!!
                val right = args.second!!.value<RayCallable>()

                RayFunction("", RayFunctionType.prefix()) { (_, rightArg) ->
                    right.call(Pair(left, rightArg!!))
                }
            },

            // Partial Left (left arg is a function)
            RayFunction(
                "&",
                RayFunctionType(RayFunctionType.any(), RayFunctionType.any(), RayFunctionType.prefix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!.value<RayCallable>()

                RayFunction("", RayFunctionType.prefix()) { (_, rightArg) ->
                    right.call(Pair(left, rightArg!!))
                }
            },

            // Swap
            RayFunction(
                "~:",
                RayFunctionType(RayFunctionType.postfix(), RaySimpleType.ANY, RaySimpleType.ANY)
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!

                left.call(Pair(right, null))
            },

            // Swap
            RayFunction(
                "~:",
                RayFunctionType(RayFunctionType.infix(), RaySimpleType.NOTHING, RayFunctionType.infix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val type = if (left is RayFunction) left.type.reversed() else RayFunctionType.infix()

                RayFunction("", type) { (leftArg, rightArg) ->
                    left.call(Pair(rightArg!!, leftArg!!))
                }
            },

            // Swap
            RayFunction(
                "~:",
                RayFunctionType(RayFunctionType.postfix(), RaySimpleType.NOTHING, RayFunctionType.prefix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val type = if (left is RayFunction) left.type.reversed() else RayFunctionType.prefix()

                RayFunction("", type) { (_, rightArg) ->
                    left.call(Pair(rightArg!!, null))
                }
            },

            // Swap
            RayFunction(
                "~:",
                RayFunctionType(RayFunctionType.prefix(), RaySimpleType.NOTHING, RayFunctionType.postfix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val type = if (left is RayFunction) left.type.reversed() else RayFunctionType.postfix()

                RayFunction("", type) { (leftArg, _) ->
                    left.call(Pair(null, leftArg!!))
                }
            },

            // Swap
            RayFunction(
                "~:",
                RayFunctionType(RayFunctionType.prefix(), RaySimpleType.ANY, RayFunctionType.postfix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!
                val type = if (left is RayFunction) left.type.reversed() else RayFunctionType.postfix()

                RayFunction("", type) { (_, _) ->
                    left.call(Pair(null, right))
                }
            },
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf()
}
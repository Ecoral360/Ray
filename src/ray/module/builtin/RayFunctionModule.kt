package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.*
import ray.objects.function.RayCallable
import ray.objects.function.RayModuleFunction

object RayFunctionModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayModuleFunction> =
        arrayOf(
            // Left Reduce
            RayModuleFunction(
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
            RayModuleFunction(
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
            RayModuleFunction(
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

            // To function
            RayModuleFunction(
                "&:",
                RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.ANY, RayFunctionType.nothing())
            ) { args ->
                val right = args.second!!

                RayModuleFunction("", RayFunctionType.nothing()) { right }
            },

            // Partial Right
            RayModuleFunction(
                "&",
                RayFunctionType(RayFunctionType.any(), RaySimpleType.ANY, RayFunctionType.postfix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!

                RayModuleFunction("", RayFunctionType.postfix()) { (leftArg, _) ->
                    left.call(Pair(leftArg!!, right))
                }
            },

            // Partial Right
            RayModuleFunction(
                "&",
                RayFunctionType(
                    RayFunctionType.prefix(),
                    RaySimpleType.ANY,
                    RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.NOTHING, RaySimpleType.ANY)
                )
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!

                RayModuleFunction("", RayFunctionType.postfix()) { (_, _) ->
                    left.call(Pair(null, right))
                }
            },

            // Partial Right (right arg is a function)
            RayModuleFunction(
                "&.",
                RayFunctionType(RayFunctionType.any(), RayFunctionType.any(), RayFunctionType.postfix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!.value<RayCallable>()

                RayModuleFunction("", RayFunctionType.postfix()) { (leftArg, _) ->
                    left.call(Pair(leftArg!!, right))
                }
            },

            // Partial Left
            RayModuleFunction(
                "&",
                RayFunctionType(RaySimpleType.ANY, RayFunctionType.any(), RayFunctionType.prefix())
            ) { args ->
                val left = args.first!!
                val right = args.second!!.value<RayCallable>()

                RayModuleFunction("", RayFunctionType.prefix()) { (_, rightArg) ->
                    right.call(Pair(left, rightArg!!))
                }
            },

            // Partial Left (left arg is a function)
            RayModuleFunction(
                "&",
                RayFunctionType(RayFunctionType.any(), RayFunctionType.any(), RayFunctionType.prefix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!.value<RayCallable>()

                RayModuleFunction("", RayFunctionType.prefix()) { (_, rightArg) ->
                    right.call(Pair(left, rightArg!!))
                }
            },

            // Swap
            RayModuleFunction(
                "~:",
                RayFunctionType(RayFunctionType.postfix(), RaySimpleType.ANY, RaySimpleType.ANY)
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!

                left.call(Pair(right, null))
            },

            // Swap
            RayModuleFunction(
                "~:",
                RayFunctionType(RayFunctionType.infix(), RaySimpleType.NOTHING, RayFunctionType.infix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val type = if (left is RayModuleFunction) left.type.reversed() else RayFunctionType.infix()

                RayModuleFunction("", type) { (leftArg, rightArg) ->
                    left.call(Pair(rightArg!!, leftArg!!))
                }
            },

            // Swap
            RayModuleFunction(
                "~:",
                RayFunctionType(RayFunctionType.postfix(), RaySimpleType.NOTHING, RayFunctionType.prefix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val type = if (left is RayModuleFunction) left.type.reversed() else RayFunctionType.prefix()

                RayModuleFunction("", type) { (_, rightArg) ->
                    left.call(Pair(rightArg!!, null))
                }
            },

            // Swap
            RayModuleFunction(
                "~:",
                RayFunctionType(RayFunctionType.prefix(), RaySimpleType.NOTHING, RayFunctionType.postfix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val type = if (left is RayModuleFunction) left.type.reversed() else RayFunctionType.postfix()

                RayModuleFunction("", type) { (leftArg, _) ->
                    left.call(Pair(null, leftArg!!))
                }
            },

            // Swap
            RayModuleFunction(
                "~:",
                RayFunctionType(RayFunctionType.prefix(), RaySimpleType.ANY, RayFunctionType.postfix())
            ) { args ->
                val left = args.first!!.value<RayCallable>()
                val right = args.second!!
                val type = if (left is RayModuleFunction) left.type.reversed() else RayFunctionType.postfix()

                RayModuleFunction("", type) { (_, _) ->
                    left.call(Pair(null, right))
                }
            },
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf()
}
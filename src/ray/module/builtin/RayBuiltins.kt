package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.*
import ray.objects.function.RayCallable
import ray.objects.function.RayFunction
import ray.objects.function.RayPartialFunction
import ray.objects.primitive.*

object RayBuiltins : RayModule {
    override fun loadFunctions(executorState: RayExecutorState) =
        arrayOf(
            *RayMatrixModule.loadFunctions(executorState),
            *RayStringModule.loadFunctions(executorState),
            *RayNumberModule.loadFunctions(executorState),
            *RayVectorModule.loadFunctions(executorState),
            *RayFunctionModule.loadFunctions(executorState),

            // Equals
            RayFunction("=", RayFunctionType.any(returnType = RaySimpleType.NUMBER)) { args ->
                val left = args.first!!
                val right = args.second!!

                RayInt(if (left == right) 1 else 0)
            },

            // Partial
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

            // Partial
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

            // Join
            RayFunction(
                ",",
                RayFunctionType(RaySimpleType.ANY, RaySimpleType.ANY, RayArrayType(RaySimpleType.ANY))
            ) { args ->
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

            //----------------- Meta functions -----------------//
            // typeOf (returns Type Signature)
            RayFunction(
                "`typeOf`",
                RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.ANY, RaySimpleType.STRING)
            ) { args ->
                val obj = args.second!!
                RayString(obj.type.getTypeSignature())
            },

            RayFunction(
                "`call`",
                RayFunctionType(RayFunctionType.prefix(), RaySimpleType.ANY, RaySimpleType.ANY)
            ) { args ->
                val func = args.first!!.value<RayCallable>()
                val arg = args.second!!
                func.call(Pair(null, arg))
            },

            // getVar
            RayFunction(
                "`getVar`",
                RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.STRING, RaySimpleType.ANY)
            ) { args ->
                val varName = args.second!!.value<String>()
                executorState.scopeManager.currentScopeInstance.getVariable(varName)?.ascObject as RayObject<*>?
                    ?: RayObject.RAY_NOTHING
            },
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf(
            *RayMatrixModule.loadVariables(executorState),
            *RayStringModule.loadVariables(executorState),
            *RayNumberModule.loadVariables(executorState),
            *RayVectorModule.loadVariables(executorState),
            *RayFunctionModule.loadVariables(executorState),
        )
}
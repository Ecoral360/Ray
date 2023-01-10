package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.*
import ray.objects.function.RayCallable
import ray.objects.function.RayFunction
import ray.objects.primitive.*

object RayBuiltins : RayModule {
    override fun loadFunctions(executorState: RayExecutorState) =
        arrayOf(
            *RayMatrixModule.loadFunctions(executorState),
            *RayStringModule.loadFunctions(executorState),
            *RayNumberModule.loadFunctions(executorState),
            *RayBooleanModule.loadFunctions(executorState),
            *RayVectorModule.loadFunctions(executorState),
            *RayFunctionModule.loadFunctions(executorState),
            *RayIOModule.loadFunctions(executorState),

            // Equals
            RayFunction("=", RayFunctionType.anyNonFonc(returnType = RaySimpleType.NUMBER)) { args ->
                val left = args.first!!
                val right = args.second!!

                RayInt(if (left == right) 1 else 0)
            },

            // Join
            RayFunction(
                ",",
                RayFunctionType(RaySimpleType.ANY_NON_FUNC, RaySimpleType.ANY_NON_FUNC, RayArrayType(RaySimpleType.ANY))
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
            *RayBooleanModule.loadVariables(executorState),
            *RayVectorModule.loadVariables(executorState),
            *RayFunctionModule.loadVariables(executorState),
            *RayIOModule.loadVariables(executorState),
        )
}
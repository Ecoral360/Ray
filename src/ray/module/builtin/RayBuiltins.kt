package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.*
import ray.objects.function.RayCallable
import ray.objects.function.RayFunction
import ray.objects.primitive.*

object RayBuiltins : RayModule {
    override fun loadFunctions(executorState: RayExecutorState) =
        arrayOf(
            // Addition of numbers
            RayFunction(
                "+",
                RayFunctionType(RaySimpleType.NUMBER, RaySimpleType.NUMBER, RaySimpleType.NUMBER)
            ) { args ->
                val left = args.first!!.value as Number
                val right = args.second!!.value as Number

                left.toRayNumber().plus(right.toRayNumber())
            },

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

            // Parse the string to a number
            RayFunction(
                "+",
                RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.STRING, RaySimpleType.NUMBER)
            ) { args ->
                val right = args.second!!.value as String
                RayInt(right.toInt())
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

            // Join
            RayFunction(
                ",",
                RayFunctionType(RaySimpleType.STRING, RaySimpleType.STRING, RaySimpleType.STRING)
            ) { args ->
                val left = args.first!!.value<String>()
                val right = args.second!!.value<String>()

                RayString("$left$right")
            },


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

            // iota (sequence)
            RayFunction(
                "i.",
                RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.NUMBER, RayArrayType(RaySimpleType.NUMBER))
            ) { args ->
                val size = args.second!!.value<Number>()
                val descending = size.toDouble() < 0
                if (!size.isInt()) throw RayError.new(RayErrors.NON_INTEGER_RANGE)

                if (descending) RayArray<RayNumber>((0 downTo size.toInt() + 1).map { RayInt(it) }.toTypedArray())
                else RayArray<RayNumber>((0 until size.toInt()).map { RayInt(it) }.toTypedArray())

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
            ASCVariable("PI", RayFloat(Math.PI))
        )
}
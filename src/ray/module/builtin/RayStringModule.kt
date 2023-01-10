package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.RayArrayType
import ray.objects.RayFunctionType
import ray.objects.RayObject
import ray.objects.RaySimpleType
import ray.objects.function.RayFunction
import ray.objects.primitive.RayInt
import ray.objects.primitive.RayString

/**
 * Function:
 * - Split: `;:@(",">["`
 * - Join
 * - Replace
 * - ToInt
 */
object RayStringModule : RayModule {
    override fun loadFunctions(executorState: RayExecutorState): Array<RayFunction> =
        arrayOf(
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
                RayFunctionType(RaySimpleType.STRING, RaySimpleType.STRING, RaySimpleType.STRING)
            ) { args ->
                val left = args.first!!.value<String>()
                val right = args.second!!.value<String>()

                RayString("$left$right")
            },

            // Join
            *RayFunction(
                ",",
                RayFunctionType(RaySimpleType.STRING, RayArrayType(RaySimpleType.STRING), RaySimpleType.STRING)
            ) { args ->
                val left = args.first!!.value<String>()
                val right = args.second!!.value<Array<RayObject<*>>>()

                RayString(right.joinToString(left) { it.value.toString() })
            }.withReversed(),

            // Split
            RayFunction(
                ";",
                RayFunctionType(RaySimpleType.STRING, RaySimpleType.STRING, RaySimpleType.STRING)
            ) { args ->
                val left = args.first!!.value<String>()
                val right = args.second!!.value<String>()

                RayString("$left$right")
            },
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf()
}
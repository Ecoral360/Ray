package ray.module.builtin

import org.ascore.lang.objects.ASCVariable
import ray.execution.RayExecutorState
import ray.module.RayModule
import ray.objects.*
import ray.objects.function.RayModuleFunction
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
    override fun loadFunctions(executorState: RayExecutorState): Array<RayModuleFunction> =
        arrayOf(
            // Parse the string to a number
            RayModuleFunction(
                "+",
                RayFunctionType(RaySimpleType.NOTHING, RaySimpleType.STRING, RaySimpleType.NUMBER)
            ) { args ->
                val right = args.second!!.value as String
                RayInt(right.toInt())
            },

            // Join
            RayModuleFunction(
                ",",
                RayFunctionType(RaySimpleType.STRING, RaySimpleType.STRING, RaySimpleType.STRING)
            ) { args ->
                val left = args.first!!.value<String>()
                val right = args.second!!.value<String>()

                RayString("$left$right")
            },

            // Join
            *RayModuleFunction(
                ",",
                RayFunctionType(RaySimpleType.STRING, RayArrayType(RaySimpleType.STRING), RaySimpleType.STRING)
            ) { args ->
                val left = args.first!!.value<String>()
                val right = args.second!!.value<Array<RayObject<*>>>()

                RayString(right.joinToString(left) { it.value.toString() })
            }.withReversed(),

            // Split
            RayModuleFunction(
                ";",
                RayFunctionType(RaySimpleType.STRING, RaySimpleType.STRING, RayArrayType(RaySimpleType.STRING))
            ) { args ->
                val left = args.first!!.value<String>()
                val right = args.second!!.value<String>()

                RayArray(left.split(right).map { RayString(it) }.toTypedArray())
            },
        )

    override fun loadVariables(executorState: RayExecutorState): Array<ASCVariable<*>> =
        arrayOf()
}
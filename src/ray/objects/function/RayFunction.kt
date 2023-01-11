package ray.objects.function

import org.ascore.executor.ASCExecutor
import org.ascore.lang.objects.ASCVariable
import org.ascore.lang.objects.ASScope
import org.ascore.managers.scope.ASScopeManager
import org.json.JSONArray
import org.json.JSONObject
import ray.errors.RayError
import ray.errors.RayErrors
import ray.execution.RayExecutorState
import ray.objects.RayFunctionType
import ray.objects.RayInstanceType
import ray.objects.RayObject
import ray.objects.RaySimpleType
import java.util.*

open class RayFunction(
    name: String,
    type: RayFunctionType,
    val executorInstance: ASCExecutor<RayExecutorState>
) :
    RayCallable(name, type) {
    lateinit var scope: ASScope

    override fun withName(name: String): RayCallable {
        return RayFunction(name, type, executorInstance)
    }

    override fun call(args: Pair<RayObject<*>?, RayObject<*>?>): RayObject<*> {
        val argType = RayFunctionType(
            args.first?.type ?: RaySimpleType.NOTHING,
            args.second?.type ?: RaySimpleType.NOTHING,
            RaySimpleType.UNKNOWN
        )

        if (!validateArgs(argType)) throw RayError.new(
            RayErrors.INVALID_FUNCTION_CALL,
            name,
            argType.getTypeSignature(),
            getFuncSignature()
        )
        val called = RayFunctionInstance(this)
        return called.call(args)
    }

    private fun validateArgs(argType: RayFunctionType): Boolean {
        return type.matches(argType)
    }

    override fun matches(type: RayInstanceType): Boolean = this.type.matches(type)

    override val type get(): RayFunctionType = super.type as RayFunctionType

    override fun getValue(): Any = this

}

class RayFunctionInstance(private val parent: RayFunction) : RayCallable(parent.name, parent.type) {
    private val scopeInstance: ASScope.ScopeInstance = parent.scope.makeScopeInstanceFromScopeParent()


    override fun withName(name: String) = parent.withName(name)
    override fun matches(type: RayInstanceType) = parent.matches(type)

    override fun call(args: Pair<RayObject<*>?, RayObject<*>?>): RayObject<*> {
        parent.executorInstance.executorState.scopeManager.pushCurrentScopeInstance(scopeInstance)
        if (parent.type.isPostfix()) { // declare @L
            scopeInstance.getVariable("@L").ascObject = args.first!!
        }
        if (parent.type.isPrefix()) { // declare @R
            scopeInstance.getVariable("@R").ascObject = args.second!!
        }

        val ancienneCoord = parent.executorInstance.obtenirCoordRunTime().copy()
        val currScope = ancienneCoord.scope

        val output = parent.executorInstance.executerScope(
            ASScopeManager.formatNewScope(
                ASScopeManager.ScopeKind.FONCTION, currScope, parent.getFuncSignature().hashCode().toString()
            ), null, null
        )

        if (output is String) {
            val jsonOutput = JSONArray(output)
            val error = jsonOutput.getJSONObject(0).getJSONArray("p")
            throw RayError(RayErrors.fromString(error.getString(0)), error.getString(1))
        }

        parent.executorInstance.setCoordRunTime(ancienneCoord.toString())
        parent.executorInstance.executorState.scopeManager.popCurrentScopeInstance()
        return output as RayObject<*>
    }
}
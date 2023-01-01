package ray.execution

import org.ascore.lang.objects.ASCVariable
import org.ascore.lang.objects.ASScope

fun ASScope.getVariable(predicate: (ASCVariable<*>) -> Boolean): ASCVariable<*>? {
    return variablesDeclarees.filter(predicate).getOrNull(0)
}

fun ASScope.ScopeInstance.getVariable(predicate: (ASCVariable<*>) -> Boolean): ASCVariable<*>? {
    return variableStack.filter(predicate).getOrNull(0) ?: parent?.getVariable(predicate)
}

fun ASScope.ScopeInstance.getVariables(includeParents: Boolean = false, predicate: (ASCVariable<*>) -> Boolean): List<ASCVariable<*>> {
    return ArrayList(variableStack.filter(predicate)) +
            if (!includeParents || parent == null) arrayListOf() else parent.getVariables(true, predicate)
}
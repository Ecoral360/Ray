package ray.ast.expressions

import org.ascore.ast.buildingBlocs.Expression
import org.ascore.lang.objects.ASCObject
import ray.objects.RayArray
import ray.objects.RayObject

class MakeArrayExpr(vararg elements: Expression<*>) : Expression<RayArray<*>> {
    private val elements: ArrayList<Expression<*>>

    init {
        this.elements = arrayListOf(*elements)
    }

    fun addElement(element: Expression<*>) {
        elements += element
    }

    override fun eval(): RayArray<*> {
        return RayArray(elements.map { it.eval() as RayObject<*> }.toTypedArray())
    }

    override fun toString(): String {
        return "MakeArrayExpr(elements=$elements)"
    }
}
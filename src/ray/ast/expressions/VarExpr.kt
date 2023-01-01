package ray.ast.expressions

import org.ascore.ast.buildingBlocs.Expression
import ray.objects.RayObject

class VarExpr(val name: String) : Expression<RayObject<*>> {
    override fun eval(): RayObject<*> {
        TODO("Not yet implemented")
    }
}
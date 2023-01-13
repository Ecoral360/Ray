package ray.ast.statements

import org.ascore.ast.buildingBlocs.Expression
import org.ascore.ast.buildingBlocs.Statement
import ray.objects.function.RayCallable

/**
 * Class representing a print statement. It takes an [Expression] as argument and prints it when executed at runtime.
 */
class PrintStmt(private val expression: Expression<*>) : Statement() {

    /**
     * Method called at runtime that executes the print statement, thus printing the expression.
     */
    override fun execute(): Any? {
        var obj = expression.eval()
        while (obj is RayCallable) obj = obj.call(Pair(null, null))
        println(obj)
        return null
    }
}

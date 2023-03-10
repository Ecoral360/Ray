package test.ray.types.function


import org.testng.Assert.*
import org.testng.annotations.Test
import ray.objects.RayArrayType
import ray.objects.RayFunctionType
import ray.objects.RaySimpleType
import ray.objects.RayType


class TestRayModuleFunctionType {
    @Test
    fun testRayFunctionMatching() {
        val partialFunction = RayFunctionType.partial(RaySimpleType.UNKNOWN, RaySimpleType.NUMBER)
        val matchingFunction = RayFunctionType.any()
        val nonMatchingFunction = RayFunctionType.any()
    }

    fun testSignature(signature: String, expected: RayFunctionType) {
        assertTrue(RayType.parseType(signature)?.matches(expected) ?: false)
    }

    @Test
    fun testRayFunctionParsing() {
        var expectedType = RayFunctionType(RaySimpleType.NUMBER, RaySimpleType.NUMBER, RaySimpleType.NUMBER)
        testSignature("(#,#>#", expectedType)

        expectedType = RayFunctionType(
            RayFunctionType(RaySimpleType.ANY, RaySimpleType.ANY, RaySimpleType.ANY),
            RayArrayType(RaySimpleType.ANY),
            RaySimpleType.ANY
        )

        testSignature("((^,^>^,[^>^", expectedType)
        testSignature("((#,^>^,[#>^", expectedType)
    }
}








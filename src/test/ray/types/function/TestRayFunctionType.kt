package test.ray.types.function


import org.testng.Assert.*
import org.testng.annotations.Test
import ray.objects.RayFunctionType
import ray.objects.RaySimpleType
import ray.objects.RayType
import ray.objects.function.RayFunction


class TestRayFunctionType {
    @Test
    fun testRayFunctionMatching() {
        val partialFunction = RayFunctionType.partial(RaySimpleType.UNKNOWN, RaySimpleType.NUMBER)
        val matchingFunction = RayFunctionType()
        val nonMatchingFunction = RayFunctionType()
    }

    fun testSignature(signature: String, expected: RayFunctionType) {
        assertTrue(RayType.parseType(signature)?.matches(expected) ?: false)
    }

    @Test
    fun testRayFunctionParsing() {
        val expectedType = RayFunctionType(RaySimpleType.NUMBER, RaySimpleType.NUMBER, RaySimpleType.NUMBER)
        testSignature("(#,#>#", expectedType)
    }
}







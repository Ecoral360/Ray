package ray.objects

import ray.objects.function.RayFunction


sealed interface RayType {
    companion object {
        fun parseType(typeSignature: String): RayInstanceType? {
            return RaySimpleType.parseTypeSignature(typeSignature)
                    ?: RayArrayType.parseTypeSignature(typeSignature)
                    ?: RayFunctionType.parseTypeSignature(typeSignature)
        }
    }

    fun getTypeSymbol(): String

    fun getTypeSignature(): String
}

interface RayInstanceType : RayType {
    fun matches(rayType: RayInstanceType): Boolean
}

enum class RaySimpleType(private val typeSymbol: String) : RayInstanceType {
    ANY("^"),
    NOTHING("."),
    NUMBER("#"),
    STRING("\""),
    UNKNOWN("_");

    override fun getTypeSymbol() = typeSymbol

    override fun getTypeSignature() = typeSymbol

    override fun matches(rayType: RayInstanceType) =
            when (rayType) {
                ANY -> this != NOTHING
                UNKNOWN -> this != NOTHING
                NOTHING, NUMBER, STRING -> getTypeSymbol() == rayType.getTypeSymbol()
                else -> false
            } || when (this) {
                ANY -> rayType != NOTHING
                UNKNOWN -> rayType != NOTHING
                NOTHING, NUMBER, STRING -> getTypeSymbol() == rayType.getTypeSymbol()
            }

    companion object {
        fun parseTypeSignature(typeSignature: String): RaySimpleType? {
            return RaySimpleType.values().find { typeSignature == it.getTypeSymbol() }
        }
    }
}

class RayArrayType(private val innerType: RayInstanceType = RaySimpleType.ANY) : RayInstanceType {

    companion object : RayType {
        override fun getTypeSymbol() = "["

        override fun getTypeSignature() = "[^"

        fun parseTypeSignature(typeSignature: String): RayArrayType? {
            if (!typeSignature.startsWith(getTypeSymbol())) return null

            return RayArrayType(RayType.parseType(typeSignature.substring(1))!!)
        }
    }

    override fun matches(rayType: RayInstanceType): Boolean =
            rayType == RaySimpleType.ANY || rayType == RaySimpleType.UNKNOWN || (rayType is RayArrayType && innerType.matches(rayType.innerType))


    override fun getTypeSymbol() = "["

    override fun getTypeSignature() = "${getTypeSymbol()}${innerType.getTypeSignature()}"
}

class RayFunctionType(val leftType: RayInstanceType = RaySimpleType.ANY,
                      val rightType: RayInstanceType = RaySimpleType.ANY,
                      val returnType: RayInstanceType = RaySimpleType.ANY) : RayInstanceType {

    companion object : RayType {
        const val TYPE_SYMBOL = '('
        const val RET_SYMBOL = '>'
        const val ARG_SEP_SYMBOL = ','

        fun partial(leftType: RayInstanceType = RaySimpleType.UNKNOWN,
                    rightType: RayInstanceType = RaySimpleType.UNKNOWN,
                    returnType: RayInstanceType = RaySimpleType.UNKNOWN): RayFunctionType {
            return RayFunctionType(leftType, rightType, returnType)
        }

        override fun getTypeSymbol() = "("

        override fun getTypeSignature() = "(^,^>^"

        fun parseTypeSignature(typeSignature: String): RayFunctionType? {
            if (!typeSignature.startsWith(getTypeSymbol())) return null

            // must have in function signature: `(` and `>`
            // algo to get the index of the `,` and the `>`: traverse each character until count == 0
            //  count + 1 if `(`, count - 1 if `>`
            //  when count == 1, if we encounter a `,`, it is the correct one: save the index
            //  when count == 0, the `>` at that index is the correct one: save the index
            var argSepIndex = -1
            var retSepIndex = -1
            var counter = 0
            for ((idx, char) in typeSignature.mapIndexed { i, c -> Pair(i, c) }) {
                when {
                    char == TYPE_SYMBOL -> counter++
                    char == RET_SYMBOL && --counter == 0 -> {
                        retSepIndex = idx
                        break
                    }

                    char == ARG_SEP_SYMBOL && counter == 1 -> {
                        argSepIndex = idx
                    }
                }
            }

            // ERROR, should not happen
            if (retSepIndex == -1) {
                return null
            }

            val leftType: RayInstanceType
            val rightType: RayInstanceType
            val retType: RayInstanceType

            // If argSepIndex == -1, we have no left arg to the function
            if (argSepIndex == -1) {
                leftType = RaySimpleType.NOTHING
                rightType = RayType.parseType(typeSignature.substring(1, retSepIndex)) ?: RaySimpleType.UNKNOWN
                retType = RayType.parseType(typeSignature.substring(retSepIndex + 1)) ?: RaySimpleType.UNKNOWN
            } else {
                leftType = RayType.parseType(typeSignature.substring(1, argSepIndex)) ?: RaySimpleType.UNKNOWN
                rightType = RayType.parseType(typeSignature.substring(argSepIndex + 1, retSepIndex))
                        ?: RaySimpleType.UNKNOWN
                retType = RayType.parseType(typeSignature.substring(retSepIndex + 1)) ?: RaySimpleType.UNKNOWN
            }
            return RayFunctionType(leftType, rightType, retType)
        }
    }

    fun isInfix() = leftType != RaySimpleType.NOTHING && rightType != RaySimpleType.NOTHING

    fun isPostfix() = leftType != RaySimpleType.NOTHING

    fun isPrefix() = rightType != RaySimpleType.NOTHING

    fun isPartial() = leftType == RaySimpleType.UNKNOWN || rightType == RaySimpleType.UNKNOWN

    fun isLeftPartial() = leftType == RaySimpleType.UNKNOWN

    fun isRightPartial() = rightType == RaySimpleType.UNKNOWN

    override fun matches(rayType: RayInstanceType): Boolean {
        if (rayType == RaySimpleType.UNKNOWN) return true
        if (rayType !is RayFunctionType) return false

        return this.leftType.matches(rayType.leftType) && this.rightType.matches(rayType.rightType)
    }

    override fun getTypeSymbol() = RayFunctionType.getTypeSymbol()

    override fun getTypeSignature() = "$TYPE_SYMBOL${leftType.getTypeSignature()}$ARG_SEP_SYMBOL" +
            "${rightType.getTypeSignature()}$RET_SYMBOL" +
            (returnType.getTypeSignature()) +
            (if (isPartial()) "@~" else "")
}

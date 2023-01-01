package ray.lexer

import org.ascore.lang.ASCLexer

/**
 * This class is used to lex the source code. Override and edit the [lex] method to change the
 * lexing behavior.
 */
class RayLexer(filePath: String) : ASCLexer(RayLexer::class.java.getResourceAsStream(filePath)) {
    override fun sortTokenRules() {
    }
}

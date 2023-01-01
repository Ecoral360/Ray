package ray.execution

import org.ascore.executor.ASCPrecompiler

/**
 * This class is used to precompile the source code. Edit the [preCompile] method to change the precompilation behavior.
 *
 * PreCompilation is **guarantied** to be executed before the compilation of the source code.
 */
class RayPreCompiler : ASCPrecompiler() {
    /**
     * This method is used to precompile the source code.
     *
     * @param program The source code to precompile as an array of strings.
     * @return The precompiled source code as an array of strings. (aka the actual code that will be compiled)
     */
    override fun preCompile(program: String): String {
        return program
    }
}

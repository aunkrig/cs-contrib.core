
package de.unkrig.cscontrib.checks;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks for redundant zero-parameter superconstructor invocations:
 * <pre>
 * class Foo extends Bar {
 *     Foo(int a, int b) {
 *         super(); // <===
 *     }
 * }</pre> 
 */
public class ZeroParameterSuperconstructorInvocation extends Check {

    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.CTOR_DEF };
    }

    public void visitToken(DetailAST ast) {

        // Find the constructor body.
        DetailAST statementList = ast.findFirstToken(TokenTypes.SLIST);

        // Find the superconstructor call.
        DetailAST superconstructorCall = statementList.findFirstToken(TokenTypes.SUPER_CTOR_CALL);
        if (superconstructorCall == null) return;

        // Find the argument list.
        DetailAST arguments = superconstructorCall.findFirstToken(TokenTypes.ELIST);

        // Determine the argument count.
        int argumentCount = arguments.getChildCount(TokenTypes.EXPR);

        // Complain about redundant zero-parameter superconstructor invocation.
        if (argumentCount == 0) {
            log(superconstructorCall, "Redundant invocation of zero-parameter superconstructor");
        }
    }
}

package de.unkrig.cscontrib.checks;

import com.puppycrawl.tools.checkstyle.api.*;
import com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAroundCheck;

import static de.unkrig.cscontrib.util.AST.*;

/***/
public
class WhitespaceAround extends WhitespaceAroundCheck {

    private boolean allowEmptyCatches;
    private boolean allowEmptyTypes;

    public void
    setAllowEmptyCatches(boolean value) { this.allowEmptyCatches = value; }

    public void
    setAllowEmptyTypes(boolean value) { this.allowEmptyTypes = value; }

    @Override public void
    visitToken(DetailAST ast) {
        switch (ast.getType()) {

        case TokenTypes.LCURLY:

            // Conditionally allow empty type body.
            if (
                this.allowEmptyTypes
                && nextSiblingTypeIs(ast, TokenTypes.RCURLY)
                && parentTypeIs(ast, TokenTypes.OBJBLOCK)
                && grandparentTypeIs(ast, TokenTypes.CLASS_DEF,  TokenTypes.INTERFACE_DEF)
            ) return;
            break;

        case TokenTypes.SLIST:

            // Conditionally allow empty catch block.
            if (
                this.allowEmptyCatches
                && parentTypeIs(ast, TokenTypes.LITERAL_CATCH)
                && firstChildTypeIs(ast, TokenTypes.RCURLY)
            ) return;
            break;

        case TokenTypes.RCURLY:
    
            // Check for anonymous class instantiation; unconditionally allow "}.".
            if (
                parentTypeIs(ast, TokenTypes.OBJBLOCK)
                && grandparentTypeIs(ast, TokenTypes.LITERAL_NEW)
            ) return;

            // Conditionally allow empty catch block.
            if (
                this.allowEmptyCatches
                && parentTypeIs(ast, TokenTypes.SLIST)
                && grandparentTypeIs(ast, TokenTypes.LITERAL_CATCH)
            ) return;

            // Conditionally allow empty class or interface body.
            if (
                this.allowEmptyTypes
                && parentTypeIs(ast, TokenTypes.OBJBLOCK)
                && grandparentTypeIs(ast, TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF)
                && previousSiblingTypeIs(ast, TokenTypes.LCURLY)
            ) return;
            break;

        default:
            ;
            break;
        }

        // None of the exceptions applies; pass control to the "original" WhitespaceAroundCheck.
        super.visitToken(ast);
    }
}

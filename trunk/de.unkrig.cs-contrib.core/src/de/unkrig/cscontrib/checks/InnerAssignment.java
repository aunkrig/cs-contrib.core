
package de.unkrig.cscontrib.checks;

import com.puppycrawl.tools.checkstyle.api.*;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Assignments in expressions must be parenthesized, like "a = (b = c)" or "while ((a = b))".
 */
public class InnerAssignment extends Check {

    @Override public int[]
    getDefaultTokens() {
        return new int[] {
            ASSIGN,            // "="
            DIV_ASSIGN,        // "/="
            PLUS_ASSIGN,       // "+="
            MINUS_ASSIGN,      // "-="
            STAR_ASSIGN,       // "*="
            MOD_ASSIGN,        // "%="
            SR_ASSIGN,         // ">>="
            BSR_ASSIGN,        // ">>>="
            SL_ASSIGN,         // "<<="
            BXOR_ASSIGN,       // "^="
            BOR_ASSIGN,        // "|="
            BAND_ASSIGN,       // "&="
        };
    }

    @Override public void
    visitToken(DetailAST ast) {
        DetailAST parent      = ast.getParent();
        DetailAST grandparent = parent.getParent();

        // Field or variable initializer?
        if (parent.getType() == VARIABLE_DEF) return; // int a = 3;

        // Assignment statement?
        if (parent.getType() == EXPR && (
            grandparent.getType() == SLIST           // { ... a = b
            || (                                                // if (...) a = b
                parent.getPreviousSibling() != null
                && parent.getPreviousSibling().getType() == RPAREN
            )
            || grandparent.getType() == LITERAL_ELSE // if (...) {...} else a = b
            || (                                     // for (...; ...; a += b)
                grandparent.getType() == ELIST
                && grandparent.getParent().getType() == FOR_ITERATOR
            )
            || (                                     // for (a = b; ...; ...)
                grandparent.getType() == ELIST
                && grandparent.getParent().getType() == FOR_INIT
            )
        )) return;

        // For iterator?
        if (
            parent.getType() == EXPR
            && grandparent.getType() == ELIST
            && grandparent.getParent().getType() == FOR_ITERATOR
        ) return; // for (...; ...; a += b)

        // Parenthesized assignment?
        if (ast.getPreviousSibling() != null && ast.getPreviousSibling().getType() == LPAREN) return;

        log(ast.getLineNo(), ast.getColumnNo(), "Assignments in expressions must be parenthesized");
    }
}

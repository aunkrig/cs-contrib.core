
package de.unkrig.cscontrib.util;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

/**
 * Utility methods related to CHECKSTYLE's DetailAST model.
 */
public class AST {
    private AST() {}

    public static boolean grandparentTypeIs(DetailAST ast, int... types) {
        int grandparentType = ast.getParent().getParent().getType();
        for (int type : types) {
            if (grandparentType == type) return true;
        }
        return false;
    }

    public static boolean parentTypeIs(DetailAST ast, int type) {
        DetailAST parent = ast.getParent();

        return parent.getType() == type;
    }

    public static boolean nextSiblingTypeIs(DetailAST ast, int type) {
        DetailAST nextSibling = ast.getNextSibling();

        return nextSibling != null && nextSibling.getType() == type;
    }

    public static boolean firstChildTypeIs(DetailAST ast, int type) {
        DetailAST firstChild = ast.getFirstChild();

        return firstChild != null && firstChild.getType() == type;
    }

    public static boolean previousSiblingTypeIs(DetailAST ast, int type) {
        DetailAST previousSibling = ast.getPreviousSibling();

        return previousSibling != null && previousSibling.getType() == type;
    }
}

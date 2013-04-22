
package de.unkrig.cscontrib.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

public
class ASTDumper {
    private DetailAST ast;

    ASTDumper(DetailAST ast) {
        this.ast = ast;
    }

    @Override public String
    toString() {
        StringBuilder sb = new StringBuilder();
        dumpSiblings("", this.ast, sb);
        return sb.toString();
    }

    private static void
    dumpSiblings(String prefix, DetailAST sibling, StringBuilder sb) {
        for (; sibling != null; sibling = sibling.getNextSibling()) {
            sb.append(prefix).append(sibling).append(sibling.getType()).append('\n');
            dumpSiblings(prefix + "  ", sibling.getFirstChild(), sb);
        }
    }
}
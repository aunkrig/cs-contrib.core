
/*
 * de.unkrig.cs-contrib - Additional checks, filters and quickfixes for CheckStyle and Eclipse-CS
 *
 * Copyright (c) 2013, Arno Unkrig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *       following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *       following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package de.unkrig.cscontrib.checks;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

/**
 * Assignments in expressions must be parenthesized, like "a = (b = c)" or "while ((a = b))".
 *
 * An enhanced version of 'InnerAssignment': It comes with a quickfix.
 */
@NotNullByDefault(false) public
class InnerAssignment extends Check {

    @Override public int[]
    getDefaultTokens() {
        return new int[] {
            TokenTypes.ASSIGN,            // "="
            TokenTypes.DIV_ASSIGN,        // "/="
            TokenTypes.PLUS_ASSIGN,       // "+="
            TokenTypes.MINUS_ASSIGN,      // "-="
            TokenTypes.STAR_ASSIGN,       // "*="
            TokenTypes.MOD_ASSIGN,        // "%="
            TokenTypes.SR_ASSIGN,         // ">>="
            TokenTypes.BSR_ASSIGN,        // ">>>="
            TokenTypes.SL_ASSIGN,         // "<<="
            TokenTypes.BXOR_ASSIGN,       // "^="
            TokenTypes.BOR_ASSIGN,        // "|="
            TokenTypes.BAND_ASSIGN,       // "&="
        };
    }

    @Override public void
    visitToken(DetailAST ast) {
        DetailAST parent      = ast.getParent();
        DetailAST grandparent = parent.getParent();

        // Field or variable initializer?
        if (parent.getType() == TokenTypes.VARIABLE_DEF) return; // int a = 3;

        // Assignment statement?
        if (parent.getType() == TokenTypes.EXPR && (
            grandparent.getType() == TokenTypes.SLIST           // { ... a = b
            || (                                                // if (...) a = b
                parent.getPreviousSibling() != null
                && parent.getPreviousSibling().getType() == TokenTypes.RPAREN
            )
            || grandparent.getType() == TokenTypes.LITERAL_ELSE // if (...) {...} else a = b
            || (                                                // for (...; ...; a += b)
                grandparent.getType() == TokenTypes.ELIST
                && grandparent.getParent().getType() == TokenTypes.FOR_ITERATOR
            )
            || (                                                // for (a = b; ...; ...)
                grandparent.getType() == TokenTypes.ELIST
                && grandparent.getParent().getType() == TokenTypes.FOR_INIT
            )
        )) return;

        // For iterator?
        if (
            parent.getType() == TokenTypes.EXPR
            && grandparent.getType() == TokenTypes.ELIST
            && grandparent.getParent().getType() == TokenTypes.FOR_ITERATOR
        ) return; // for (...; ...; a += b)

        // Parenthesized assignment?
        if (ast.getPreviousSibling() != null && ast.getPreviousSibling().getType() == TokenTypes.LPAREN) return;

        // Annotation member-value pair?
        if (parent.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) return;

        this.log(ast.getLineNo(), ast.getColumnNo(), "Assignments in expressions must be parenthesized");
    }
}

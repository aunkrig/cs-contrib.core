
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

import com.puppycrawl.tools.checkstyle.api.*;
import com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAroundCheck;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

import static de.unkrig.cscontrib.util.AstUtil.*;

/**
 * An enhanced version of 'WhitespaceAround': Optionally ignores empty CATCH clauses and empty types.
 */
@NotNullByDefault(false) public
class WhitespaceAround extends WhitespaceAroundCheck {

    private boolean allowEmptyCatches;
    private boolean allowEmptyTypes;

    // CONFIGURATION SETTERS -- CHECKSTYLE JavadocMethod:OFF
    public void setAllowEmptyCatches(boolean value) { this.allowEmptyCatches = value; }
    public void setAllowEmptyTypes(boolean value)   { this.allowEmptyTypes   = value; }
    // END CONFIGURATION SETTERS -- CHECKSTYLE JavadocMethod:ON

    @Override public void
    visitToken(DetailAST ast) {
        switch (ast.getType()) {

        case TokenTypes.LCURLY:

            // Conditionally allow empty type body.
            if (
                this.allowEmptyTypes
                && nextSiblingTypeIs(ast, TokenTypes.RCURLY)
                && parentTypeIs(ast, TokenTypes.OBJBLOCK)
                && grandparentTypeIs(
                    ast,
                    TokenTypes.CLASS_DEF,
                    TokenTypes.INTERFACE_DEF,
                    TokenTypes.LITERAL_NEW,
                    TokenTypes.ANNOTATION_DEF
                )
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
                && grandparentTypeIs(ast, TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.ANNOTATION_DEF)
                && previousSiblingTypeIs(ast, TokenTypes.LCURLY)
            ) return;
            break;

        default:
            ;
            break;
        }

        // None of the exceptions apply; pass control to the "original" WhitespaceAroundCheck.
        super.visitToken(ast);
    }
}

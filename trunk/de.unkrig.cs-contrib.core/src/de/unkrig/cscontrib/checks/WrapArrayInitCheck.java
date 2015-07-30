
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

import static de.unkrig.cscontrib.LocalTokenType.*;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.*;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent1;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent2;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent3;
import de.unkrig.cscontrib.util.AstUtil;
import de.unkrig.csdoclet.BooleanRuleProperty;
import de.unkrig.csdoclet.Rule;
import de.unkrig.csdoclet.SingleSelectRuleProperty;

/**
 * Verifies that array initializers are uniformly wrapped and indented.
 * <p>
 *   Array initializers appear in two different flavors:
 * </p>
 * <pre>
 * // NEW expression:
 * x = new String[3][4][] { { { "a", "b" } } };
 *
 * // Field or local variable initializer:
 * String[][] x = { { "a", "b" } };
 * </pre>
 */
@Rule(
    group      = "%Whitespace.group",
    groupName  = "Whitespace",
    name       = "de.unkrig: Wrap array initializer",
    parent     = "TreeWalker",
    quickfixes = { WrapAndIndent1.class, WrapAndIndent2.class, WrapAndIndent3.class }
)
@NotNullByDefault(false) public
class WrapArrayInitCheck extends AbstractWrapCheck {

    // ============================================= BEGIN CONFIGURATION =============================================

    /**
     * Whether to wrap array initializers before the opening curly brace. Example:
     * <pre>
     * int[] ia =
     * { ...
     * </pre>
     */
    @SingleSelectRuleProperty(
        optionProvider = WrapOptionProvider.class,
        defaultValue   = WrapArrayInitCheck.DEFAULT_WRAP_BEFORE_LCURLY
    ) public void
    setWrapBeforeLCurly(String value) { this.wrapBeforeLCurly = AbstractWrapCheck.toWrap(value); }

    private Control
    wrapBeforeLCurly = AbstractWrapCheck.toWrap(WrapArrayInitCheck.DEFAULT_WRAP_BEFORE_LCURLY);

    private static final String
    DEFAULT_WRAP_BEFORE_LCURLY = "never";

    /**
     * Whether multiple array initializer values in one line are allowed. Example:
     * <pre>
     * String[] x = {
     *     "a", "b",   // <= Two values in one line.
     *     "c"
     * };
     */
    @BooleanRuleProperty(defaultValue = WrapArrayInitCheck.DEFAULT_ALLOW_MULTIPLE_VALUES_PER_LINE)
    public void
    setAllowMultipleValuesPerLine(boolean value) { this.allowMultipleValuesPerLine = value; }

    private boolean
    allowMultipleValuesPerLine = WrapArrayInitCheck.DEFAULT_ALLOW_MULTIPLE_VALUES_PER_LINE;

    private static final boolean
    DEFAULT_ALLOW_MULTIPLE_VALUES_PER_LINE = true;

    // ============================================= END CONFIGURATION =============================================

    @Override public int[]
    getDefaultTokens() {
        return LocalTokenType.delocalize(new LocalTokenType[] {
            LocalTokenType.LITERAL_NEW,
            LocalTokenType.ASSIGN,
        });
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        switch (LocalTokenType.localize(ast.getType())) {

        case LITERAL_NEW:
            this.checkChildren(
                ast,
                ANY, FORK1, TYPE_ARGUMENTS,
                LABEL1, FORK3, ARRAY_DECLARATOR, FORK2, this.wrapBeforeLCurly, ARRAY_INIT,
                LABEL2, END,
                LABEL3, LPAREN, INDENT_IF_CHILDREN, ELIST, UNINDENT, RPAREN, OPTIONAL, MAY_WRAP, OBJBLOCK, END // SUPPRESS CHECKSTYLE LineLength
            );
            break;

        case ASSIGN:
            if (ast.getChildCount() == 1) {

                // A field or local variable initialization.
                this.checkChildren(
                    ast,
                    FORK1, this.wrapBeforeLCurly, ARRAY_INIT, END,
                    LABEL1, ANY, END
                );
            }
            break;

        default:
            throw new AssertionError(ast);
        }
    }

    @Override protected boolean
    checkMultipleElementsPerLine(DetailAST child) {

        if (
            AstUtil.parentTypeIs(child, LocalTokenType.ARRAY_INIT)
            && !this.allowMultipleValuesPerLine
        ) return false;

        return true;
    }


}

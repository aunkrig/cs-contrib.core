
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

import static de.unkrig.cscontrib.LocalTokenType.ARRAY_DECLARATOR;
import static de.unkrig.cscontrib.LocalTokenType.ARRAY_INIT;
import static de.unkrig.cscontrib.LocalTokenType.ELIST;
import static de.unkrig.cscontrib.LocalTokenType.LPAREN;
import static de.unkrig.cscontrib.LocalTokenType.OBJBLOCK;
import static de.unkrig.cscontrib.LocalTokenType.RPAREN;
import static de.unkrig.cscontrib.LocalTokenType.TYPE_ARGUMENTS;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.*;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent1;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent2;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent3;
import de.unkrig.csdoclet.Rule;
import de.unkrig.csdoclet.SingleSelectRuleProperty;

/**
 * Verifies that local class declarations are uniformly wrapped and indented.
 */
@Rule(
    group      = "%Whitespace.group",
    groupName  = "Whitespace",
    name       = "de.unkrig: Wrap local class",
    parent     = "TreeWalker",
    quickfixes = { WrapAndIndent1.class, WrapAndIndent2.class, WrapAndIndent3.class }
)
@NotNullByDefault(false) public
class WrapLocalClassCheck extends AbstractWrapCheck {

    // ============================================= BEGIN CONFIGURATION =============================================

    /**
     * Whether to wrap anonymous class declarations before the opening curly brace. Example:
     * <pre>
     * new Object()
     * {
     * </pre>
     */
    @SingleSelectRuleProperty(
        optionProvider = WrapOptionProvider.class,
        defaultValue   = WrapLocalClassCheck.DEFAULT_WRAP_DECL_BEFORE_LCURLY
    ) public void
    setWrapDeclBeforeLCurly(String value) { this.wrapDeclBeforeLCurly = AbstractWrapCheck.toWrap(value); }

    private Control
    wrapDeclBeforeLCurly = AbstractWrapCheck.toWrap(WrapLocalClassCheck.DEFAULT_WRAP_DECL_BEFORE_LCURLY);

    private static final String
    DEFAULT_WRAP_DECL_BEFORE_LCURLY = "never";

    // ============================================= END CONFIGURATION =============================================

    @Override public int[]
    getDefaultTokens() {
        return LocalTokenType.delocalize(new LocalTokenType[] { LocalTokenType.LITERAL_NEW });
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        this.checkChildren(
            ast,
            ANY, FORK1, TYPE_ARGUMENTS,
            LABEL1, FORK3, ARRAY_DECLARATOR, FORK2, MAY_WRAP, ARRAY_INIT,
            LABEL2, END,
            LABEL3, LPAREN, INDENT_IF_CHILDREN, ELIST, UNINDENT, RPAREN, OPTIONAL, this.wrapDeclBeforeLCurly, OBJBLOCK, END // SUPPRESS CHECKSTYLE LineLength
        );
    }
}

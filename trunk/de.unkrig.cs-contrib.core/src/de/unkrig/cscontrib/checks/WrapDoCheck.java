
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

import static de.unkrig.cscontrib.LocalTokenType.DO_WHILE;
import static de.unkrig.cscontrib.LocalTokenType.EXPR;
import static de.unkrig.cscontrib.LocalTokenType.LPAREN;
import static de.unkrig.cscontrib.LocalTokenType.RPAREN;
import static de.unkrig.cscontrib.LocalTokenType.SEMI;
import static de.unkrig.cscontrib.LocalTokenType.SLIST;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.END;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.MAY_INDENT;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.UNINDENT;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent1;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent2;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent3;
import de.unkrig.csdoclet.Rule;
import de.unkrig.csdoclet.SingleSelectRuleProperty;

/**
 * Verifies that DO statements are uniformly wrapped and indented.
 */
@Rule(
    group      = "%Whitespace.group",
    groupName  = "Whitespace",
    name       = "de.unkrig: Wrap DO statement",
    parent     = "TreeWalker",
    quickfixes = { WrapAndIndent1.class, WrapAndIndent2.class, WrapAndIndent3.class }
)
@NotNullByDefault(false) public
class WrapDoCheck extends AbstractWrapCheck {

    // ============================================= BEGIN CONFIGURATION =============================================

    /**
     * Whether to wrap {@code DO} statements before the opening curly brace. Example:
     * <pre>
     * do
     * {
     * </pre>
     *
     * @cs-intertitle <h3>Other Elements Wrapping</h3>
     *                <p>
     *                  The phrase "wrap before X" means that a line break and space appear right before "X", such
     *                  that "X" is vertically aligned with the first token in the immediately preceding line.
     *                </p>
     */
    @SingleSelectRuleProperty(
        optionProvider = WrapOptionProvider.class,
        defaultValue   = WrapDoCheck.DEFAULT_WRAP_BEFORE_LCURLY
    ) public void
    setWrapBeforeLCurly(String value) { this.wrapBeforeLCurly = AbstractWrapCheck.toWrap(value); }

    private Control
    wrapBeforeLCurly = AbstractWrapCheck.toWrap(WrapDoCheck.DEFAULT_WRAP_BEFORE_LCURLY);

    private static final String
    DEFAULT_WRAP_BEFORE_LCURLY = "never";

    // ============================================= END CONFIGURATION =============================================

    @Override public int[]
    getDefaultTokens() {
        return LocalTokenType.delocalize(new LocalTokenType[] { LocalTokenType.LITERAL_DO });
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        this.checkChildren(
            ast,
            this.wrapBeforeLCurly, SLIST, DO_WHILE, LPAREN, MAY_INDENT, EXPR, UNINDENT, RPAREN, SEMI, END
        );
    }
}

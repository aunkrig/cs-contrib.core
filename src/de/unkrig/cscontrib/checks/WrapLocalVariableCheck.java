
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
 *    3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.unkrig.cscontrib.checks;

import static de.unkrig.cscontrib.LocalTokenType.ASSIGN;
import static de.unkrig.cscontrib.LocalTokenType.IDENT;
import static de.unkrig.cscontrib.LocalTokenType.MODIFIERS;
import static de.unkrig.cscontrib.LocalTokenType.OBJBLOCK;
import static de.unkrig.cscontrib.LocalTokenType.SEMI;
import static de.unkrig.cscontrib.LocalTokenType.TYPE;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.END;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.FORK1;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.FORK2;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.LABEL1;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.LABEL2;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent1;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent2;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent3;
import de.unkrig.csdoclet.annotation.Rule;
import de.unkrig.csdoclet.annotation.SingleSelectRuleProperty;

/**
 * Verifies that local variable declarations are uniformly wrapped and indented.
 */
@Rule(
    group      = "%Whitespace.group",
    groupName  = "Whitespace",
    name       = "de.unkrig: Wrap local variable",
    parent     = "TreeWalker",
    quickfixes = { WrapAndIndent1.class, WrapAndIndent2.class, WrapAndIndent3.class }
)
@NotNullByDefault(false) public
class WrapLocalVariableCheck extends AbstractWrapCheck {

    // ============================================= BEGIN CONFIGURATION =============================================

    /**
     * Whether to wrap local variable declarations between the type and the variable name. Example:
     * <pre>
     * int
     * locvar = 7;
     * </pre>
     */
    @SingleSelectRuleProperty(
        optionProvider = Wrap.class,
        defaultValue   = WrapLocalVariableCheck.DEFAULT_WRAP_DECL_BEFORE_NAME
    ) public void
    setWrapDeclBeforeName(String value) { this.wrapDeclBeforeName = AbstractWrapCheck.toWrap(value); }

    private Control
    wrapDeclBeforeName = AbstractWrapCheck.toWrap(WrapLocalVariableCheck.DEFAULT_WRAP_DECL_BEFORE_NAME);

    private static final String
    DEFAULT_WRAP_DECL_BEFORE_NAME = "optional";

    // ============================================= END CONFIGURATION =============================================

    @Override public int[]
    getDefaultTokens() {
        return LocalTokenType.delocalize(new LocalTokenType[] { LocalTokenType.VARIABLE_DEF });
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        if (ast.getParent().getType() != OBJBLOCK.delocalize()) {

            // SUPPRESS CHECKSTYLE WrapMethod:6
            this.checkChildren(
                ast,
                MODIFIERS, TYPE, this.wrapDeclBeforeName, IDENT, FORK1, ASSIGN,
                // Field declarations DO have a SEMI, local variable declarations DON'T!?
                LABEL1, FORK2, SEMI,
                LABEL2, END
            );
        }
    }
}

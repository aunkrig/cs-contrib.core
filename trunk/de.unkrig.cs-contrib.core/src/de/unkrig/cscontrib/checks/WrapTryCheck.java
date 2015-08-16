
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

import static de.unkrig.cscontrib.LocalTokenType.LITERAL_CATCH;
import static de.unkrig.cscontrib.LocalTokenType.LITERAL_FINALLY;
import static de.unkrig.cscontrib.LocalTokenType.SLIST;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.END;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.FORK1;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.FORK2;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.FORK3;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.LABEL1;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.LABEL2;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.LABEL3;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent1;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent2;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent3;
import de.unkrig.csdoclet.annotation.Rule;
import de.unkrig.csdoclet.annotation.SingleSelectRuleProperty;

/**
 * Verifies that TRY statements are uniformly wrapped and indented.
 */
@Rule(
    group      = "%Whitespace.group",
    groupName  = "Whitespace",
    name       = "de.unkrig: Wrap TRY statement",
    parent     = "TreeWalker",
    quickfixes = { WrapAndIndent1.class, WrapAndIndent2.class, WrapAndIndent3.class }
)
@NotNullByDefault(false) public
class WrapTryCheck extends AbstractWrapCheck {

    // ============================================= BEGIN CONFIGURATION =============================================

    /**
     * Whether to wrap {@code TRY} statements before the {@code CATCH} keyword. Example:
     * <pre>
     * try { ... }
     * catch { ... }
     * </pre>
     */
    @SingleSelectRuleProperty(
        optionProvider = WrapOptionProvider.class,
        defaultValue   = WrapTryCheck.DEFAULT_WRAP_BEFORE_CATCH
    ) public void
    setWrapBeforeCatch(String value) { this.wrapBeforeCatch = AbstractWrapCheck.toWrap(value); }

    private Control
    wrapBeforeCatch = AbstractWrapCheck.toWrap(WrapTryCheck.DEFAULT_WRAP_BEFORE_CATCH);

    private static final String
    DEFAULT_WRAP_BEFORE_CATCH = "optional";

    /**
     * Whether to wrap {@code TRY} statements before the {@code FINALLY} keyword. Example:
     * <pre>
     * try { ... }
     * finally { ... }
     * </pre>
     */
    @SingleSelectRuleProperty(
        optionProvider = WrapOptionProvider.class,
        defaultValue   = WrapTryCheck.DEFAULT_WRAP_BEFORE_FINALLY
    ) public void
    setWrapBeforeFinally(String value) { this.wrapBeforeFinally = AbstractWrapCheck.toWrap(value); }

    private Control
    wrapBeforeFinally = AbstractWrapCheck.toWrap(WrapTryCheck.DEFAULT_WRAP_BEFORE_FINALLY);

    private static final String
    DEFAULT_WRAP_BEFORE_FINALLY = "optional";

    // ============================================= END CONFIGURATION =============================================

    @Override public int[]
    getDefaultTokens() {
        return LocalTokenType.delocalize(new LocalTokenType[] { LocalTokenType.LITERAL_TRY });
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        this.checkChildren(
            ast,
            SLIST, FORK2,
            LABEL1, this.wrapBeforeCatch, LITERAL_CATCH, FORK1, FORK3,
            LABEL2, this.wrapBeforeFinally, LITERAL_FINALLY,
            LABEL3, END
        );
    }
}

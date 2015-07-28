
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

import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.MAY_WRAP;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.NO_WRAP;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent1;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent2;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent3;
import de.unkrig.csdoclet.Rule;
import de.unkrig.csdoclet.SingleSelectRuleProperty;

/**
 * Verifies that package declarations are uniformly wrapped and indented.
 */
@Rule(
    group      = "%Whitespace.group",
    groupName  = "Whitespace",
    name       = "de.unkrig: Wrap package",
    parent     = "TreeWalker",
    quickfixes = { WrapAndIndent1.class, WrapAndIndent2.class, WrapAndIndent3.class }
)
@NotNullByDefault(false) public
class WrapPackageCheck extends AbstractWrapCheck {

    // ============================================= BEGIN CONFIGURATION =============================================

    /**
     * Whether to wrap package declarations before the {@code PACKAGE} keyword (in "{@code package-info.java}").
     * Example:
     * <pre>
     * &#64;NonNullByDefault
     * package com.acme.product;
     * </pre>
     *
     * @cs-intertitle <h3>Declaration Wrapping</h3>
     *                <p>
     *                  The phrase "wrap before X" means that a line break and spaces appear right before "X", such
     *                  that "X" is vertically aligned with the first token in the immediately preceding line.
     *                </p>
     */
    @SingleSelectRuleProperty(
        optionProvider = WrapOptionProvider.class,
        defaultValue   = WrapPackageCheck.DEFAULT_WRAP_DECL_BEFORE_PACKAGE
    ) public void
    setWrapDeclBeforePackage(String value) { this.wrapDeclBeforePackage = AbstractWrapCheck.toWrap(value); }

    private Control
    wrapDeclBeforePackage = AbstractWrapCheck.toWrap(WrapPackageCheck.DEFAULT_WRAP_DECL_BEFORE_PACKAGE);

    private static final String
    DEFAULT_WRAP_DECL_BEFORE_PACKAGE = "always";

    // ============================================= END CONFIGURATION =============================================

    @Override public int[]
    getDefaultTokens() {
        return LocalTokenType.delocalize(new LocalTokenType[] { LocalTokenType.PACKAGE_DEF });
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        // The AST of a PACKAGE declaration is quite extraordinary and thus difficult to check.
        //
        //    package                    [1x0]   [3x0]
        //        ANNOTATIONS            [1x15]  [2x0]
        //            ANNOTATION                 [2x0]
        //                @                      [2x0]
        //                SuppressWarnings       [2x1]
        //                (                      [2x17]
        //                EXPR                   [2x18]
        //                    "null"             [2x18]
        //                )                      [2x24]
        //        .                      [1x17]  [3x17]
        //            .                  [1x12]  [3x12]
        //                foo1           [1x8]   [3x8]
        //                foo2           [1x13]  [3x13]
        //            foo3               [1x18]  [3x18]
        //        ;                      [1x22]  [3x22]
        //    import                     [3x0]   [5x0]

        this.checkSameLine(ast, AbstractWrapCheck.getLeftmostDescendant(ast.getFirstChild().getNextSibling()));

        if (ast.getFirstChild().getFirstChild() == null) return; // No annotation(s)

        if (this.wrapDeclBeforePackage == NO_WRAP) {
            this.checkSameLine(ast, ast.getFirstChild().getFirstChild().getFirstChild());
            return;
        }

        if (this.wrapDeclBeforePackage == MAY_WRAP && AbstractWrapCheck.isSingleLine(ast)) return;

        // Check that "@" is vertically aligned with the "package" keyword.
        this.checkWrapped(ast.getFirstChild().getFirstChild().getFirstChild(), ast);

        // Check that the "package" keyword appears in the same line as the terminal ";".
        this.checkSameLine(ast, ast.getFirstChild().getNextSibling().getNextSibling());
    }
}

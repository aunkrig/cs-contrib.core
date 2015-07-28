
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

import static de.unkrig.cscontrib.LocalTokenType.AT;
import static de.unkrig.cscontrib.LocalTokenType.IDENT;
import static de.unkrig.cscontrib.LocalTokenType.LITERAL_INTERFACE;
import static de.unkrig.cscontrib.LocalTokenType.MODIFIERS;
import static de.unkrig.cscontrib.LocalTokenType.OBJBLOCK;
import static de.unkrig.cscontrib.checks.AbstractWrapCheck.Control.END;

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
 * Verifies that annotation type declarations are uniformly wrapped and indented.
 */
@Rule(
    group      = "%Whitespace.group",
    groupName  = "Whitespace",
    name       = "de.unkrig: Wrap annotation type",
    parent     = "TreeWalker",
    quickfixes = { WrapAndIndent1.class, WrapAndIndent2.class, WrapAndIndent3.class }
)
@NotNullByDefault(false) public
class WrapAnnotationTypeCheck extends AbstractWrapCheck {

    // ============================================= BEGIN CONFIGURATION =============================================

    /**
     * Whether to allow a complete annotation declaration in one single line. Example:
     * <pre>
     * public @interface MyAnno {}
     * </pre>
     */
    @BooleanRuleProperty(defaultValue = WrapAnnotationTypeCheck.DEFAULT_ALLOW_ONE_LINE_DECL)
    public void
    setAllowOneLineDecl(boolean value) { this.allowOneLineDecl = value; }

    private boolean
    allowOneLineDecl = WrapAnnotationTypeCheck.DEFAULT_ALLOW_ONE_LINE_DECL;

    private static final boolean
    DEFAULT_ALLOW_ONE_LINE_DECL = true;

    /**
     * Whether to wrap annotation declarations before '@'. Example:
     * <pre>
     * private
     * &#64;interface MyAnno {
     * </pre>
     */
    @SingleSelectRuleProperty(
        optionProvider = WrapOptionProvider.class,
        defaultValue   = WrapAnnotationTypeCheck.DEFAULT_WRAP_DECL_BEFORE_AT
    ) public void
    setWrapDeclBeforeAt(String value) { this.wrapDeclBeforeAt = AbstractWrapCheck.toWrap(value); }

    private Control
    wrapDeclBeforeAt = AbstractWrapCheck.toWrap(WrapAnnotationTypeCheck.DEFAULT_WRAP_DECL_BEFORE_AT);

    private static final String
    DEFAULT_WRAP_DECL_BEFORE_AT = "always";

    /**
     * Whether to wrap annotation type declarations before the opening curly brace. Example:
     * <pre>
     * public interface &#64;MyAnnotationType
     * {
     * </pre>
     */
    @SingleSelectRuleProperty(
        optionProvider = WrapOptionProvider.class,
        defaultValue   = WrapAnnotationTypeCheck.DEFAULT_WRAP_DECL_BEFORE_LCURLY
    ) public void
    setWrapDeclBeforeLCurly(String value) { this.wrapDeclBeforeLCurly = AbstractWrapCheck.toWrap(value); }

    private Control
    wrapDeclBeforeLCurly = AbstractWrapCheck.toWrap(WrapAnnotationTypeCheck.DEFAULT_WRAP_DECL_BEFORE_LCURLY);

    private static final String
    DEFAULT_WRAP_DECL_BEFORE_LCURLY = "never";

    /**
     * Whether multiple annotations in one line are allowed.
     */
    @BooleanRuleProperty(defaultValue = WrapAnnotationTypeCheck.DEFAULT_ALLOW_MULTIPLE_PER_LINE)
    public void
    setAllowMultiplePerLine(boolean value) { this.allowMultiplePerLine = value; }

    private boolean
    allowMultiplePerLine = WrapAnnotationTypeCheck.DEFAULT_ALLOW_MULTIPLE_PER_LINE;

    private static final boolean
    DEFAULT_ALLOW_MULTIPLE_PER_LINE = false;

    /**
     * Whether multiple annotation initializers in one line are allowed.
     */
    @BooleanRuleProperty(defaultValue = WrapAnnotationTypeCheck.DEFAULT_ALLOW_MULTIPLE_INITIALIZERS_PER_LINE)
    public void
    setAllowMultipleInitializersPerLine(boolean value) { this.allowMultipleInitializersPerLine = value; }

    private boolean
    allowMultipleInitializersPerLine = WrapAnnotationTypeCheck.DEFAULT_ALLOW_MULTIPLE_INITIALIZERS_PER_LINE;

    private static final boolean
    DEFAULT_ALLOW_MULTIPLE_INITIALIZERS_PER_LINE = false;

    // ============================================= END CONFIGURATION =============================================

    @Override public int[]
    getDefaultTokens() {
        return LocalTokenType.delocalize(new LocalTokenType[] { LocalTokenType.ANNOTATION_DEF });
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        if (this.allowOneLineDecl && AbstractWrapCheck.isSingleLine(ast)) return;

        this.checkChildren(
            ast,
            MODIFIERS, this.wrapDeclBeforeAt, AT, LITERAL_INTERFACE, IDENT, this.wrapDeclBeforeLCurly, OBJBLOCK, END // SUPPRESS CHECKSTYLE LineLength
        );
    }

    @Override protected boolean
    checkMultipleElementsPerLine(DetailAST child) {

        if (AstUtil.parentTypeIs(child, LocalTokenType.ANNOTATION) && !this.allowMultiplePerLine) return false;

        if (
            AstUtil.parentTypeIs(child, LocalTokenType.ANNOTATION_ARRAY_INIT)
            && !this.allowMultipleInitializersPerLine
        ) return false;

        return true;
    }
}

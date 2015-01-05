
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

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAroundCheck;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
import de.unkrig.cscontrib.util.AstUtil;

/**
 * Verifies that a token is surrounded by whitespace.
 * <p>
 *   <span style="color: red"><b>This check is superseded by {@code de.unkrig.Whitespace}.</b></span>
 * </p>
 * <p>
 *   Empty constructor bodies, method bodies, catch blocks and type bodies of the form
 * </p>
 * <pre>
 * public MyClass() {}           // empty constructor body
 *
 * public void func() {}         // empty method body
 *
 * public void func() {
 *     new Object() {
 *         // ...
 *     }.hashCode();             // No space between '}' and '.' -- always allowed
 *     try {
 *         // ...
 *     } catch {}                // empty catch block
 * }
 *
 * interface MyInterface {}      // emtpy type body
 * </pre>
 * <p>
 *   may optionally be exempted from the policy using the allowEmptyMethods, allowEmptyConstructors, allowEmptyCatches
 *   and allowEmptyTypes properties.
 * </p>
 *
 * @cs-rule-group  %Whitespace.group
 * @cs-rule-name   de.unkrig: Whitespace around
 * @cs-rule-parent TreeWalker
 */
@NotNullByDefault(false) public
class WhitespaceAround extends WhitespaceAroundCheck {

    // CONFIGURATION SETTERS

    // SUPPRESS CHECKSTYLE LineLength:6
    /**
     * Tokens to check.
     *
     * @cs-property-name          tokens
     * @cs-property-datatype      MultiCheck
     * @cs-property-default-value assign,band,band_assign,bor,bor_assign,bsr,bsr_assign,bxor,bxor_assign,colon,div,div_assign,equal,ge,gt,land,lcurly,le,literal_assert,literal_catch,literal_do,literal_else,literal_finally,literal_for,literal_if,literal_return,literal_synchronized,literal_try,literal_while,lor,lt,minus,minus_assign,mod,mod_assign,not_equal,plus,plus_assign,question,rcurly,sl,slist,sl_assign,sr,sr_assign,star,star_assign,literal_assert,type_extension_and,wildcard_type
     * @cs-property-value-option  assign
     * @cs-property-value-option  band
     * @cs-property-value-option  band_assign
     * @cs-property-value-option  bor
     * @cs-property-value-option  bor_assign
     * @cs-property-value-option  bsr
     * @cs-property-value-option  bsr_assign
     * @cs-property-value-option  bxor
     * @cs-property-value-option  bxor_assign
     * @cs-property-value-option  colon
     * @cs-property-value-option  div
     * @cs-property-value-option  div_assign
     * @cs-property-value-option  equal
     * @cs-property-value-option  ge
     * @cs-property-value-option  gt
     * @cs-property-value-option  land
     * @cs-property-value-option  lcurly
     * @cs-property-value-option  le
     * @cs-property-value-option  literal_assert
     * @cs-property-value-option  literal_catch
     * @cs-property-value-option  literal_do
     * @cs-property-value-option  literal_else
     * @cs-property-value-option  literal_finally
     * @cs-property-value-option  literal_for
     * @cs-property-value-option  literal_if
     * @cs-property-value-option  literal_return
     * @cs-property-value-option  literal_synchronized
     * @cs-property-value-option  literal_try
     * @cs-property-value-option  literal_while
     * @cs-property-value-option  lor
     * @cs-property-value-option  lt
     * @cs-property-value-option  minus
     * @cs-property-value-option  minus_assign
     * @cs-property-value-option  mod
     * @cs-property-value-option  mod_assign
     * @cs-property-value-option  not_equal
     * @cs-property-value-option  plus
     * @cs-property-value-option  plus_assign
     * @cs-property-value-option  question
     * @cs-property-value-option  rcurly
     * @cs-property-value-option  sl
     * @cs-property-value-option  slist
     * @cs-property-value-option  sl_assign
     * @cs-property-value-option  sr
     * @cs-property-value-option  sr_assign
     * @cs-property-value-option  star
     * @cs-property-value-option  star_assign
     * @cs-property-value-option  literal_assert
     * @cs-property-value-option  type_extension_and
     * @cs-property-value-option  wildcard_type
     */
    public void
    setTokens(int x) {}

    /**
     * Allow empty constructor bodies.
     *
     * @cs-property-name          allowEmptyConstructors
     * @cs-property-default-value false
     */
    @Override public void
    setAllowEmptyConstructors(boolean value) { super.setAllowEmptyConstructors(value); }

    /**
     * Allow empty method bodies.
     *
     * @cs-property-name          allowEmptyMethods
     * @cs-property-default-value false
     */
    @Override public void
    setAllowEmptyMethods(boolean value) { super.setAllowEmptyMethods(value); }

    /**
     * Allow empty catch blocks.
     *
     * @cs-property-name          allowEmptyCatches
     * @cs-property-default-value false
     */
    public void
    setAllowEmptyCatches(boolean value) { this.allowEmptyCatches = value; }
    private boolean allowEmptyCatches;

    /**
     * Allow empty class and interface bodies.
     *
     * @cs-property-name          allowEmptyTypes
     * @cs-property-default-value false
     */
//    @Override  // CS 6 adds this method.
    public void
    setAllowEmptyTypes(boolean value) {
//        super.setAllowEmptyTypes(value);
        this.allowEmptyTypes = value;
    }
    private boolean allowEmptyTypes;

    /**
     * Ignore the colon (":") token in enhanced {@code for} statements ("{@code for (x : y) ...}").
     *
     * @cs-property-name          ignoreEnhancedForColon
     * @cs-property-desc          Ignore the colon token in enhanced FOR statements ("for (x : y) ...").
     * @cs-property-default-value false
     */
    @Override public void
    setIgnoreEnhancedForColon(boolean value) { super.setIgnoreEnhancedForColon(value); }

    // END CONFIGURATION SETTERS

    @Override public void
    visitToken(DetailAST ast) {
        switch (LocalTokenType.localize(ast.getType())) {

        case LCURLY:

            // Conditionally allow empty type body.
            if (
                this.allowEmptyTypes
                && AstUtil.nextSiblingTypeIs(ast, LocalTokenType.RCURLY)
                && AstUtil.parentTypeIs(ast, LocalTokenType.OBJBLOCK)
                && AstUtil.grandParentTypeIs(
                    ast,
                    LocalTokenType.CLASS_DEF,
                    LocalTokenType.INTERFACE_DEF,
                    LocalTokenType.LITERAL_NEW,
                    LocalTokenType.ANNOTATION_DEF
                )
            ) return;
            break;

        case SLIST:

            // Conditionally allow empty catch block.
            if (
                this.allowEmptyCatches
                && AstUtil.parentTypeIs(ast, LocalTokenType.LITERAL_CATCH)
                && AstUtil.firstChildTypeIs(ast, LocalTokenType.RCURLY)
            ) return;
            break;

        case RCURLY:

            // Check for anonymous class instantiation; unconditionally allow "}.".
            if (
                AstUtil.parentTypeIs(ast, LocalTokenType.OBJBLOCK)
                && AstUtil.grandParentTypeIs(ast, LocalTokenType.LITERAL_NEW)
            ) return;

            // Conditionally allow empty catch block.
            if (
                this.allowEmptyCatches
                && AstUtil.parentTypeIs(ast, LocalTokenType.SLIST)
                && AstUtil.grandParentTypeIs(ast, LocalTokenType.LITERAL_CATCH)
            ) return;

            // Conditionally allow empty class or interface body.
            if (
                this.allowEmptyTypes
                && AstUtil.parentTypeIs(ast, LocalTokenType.OBJBLOCK)
                && AstUtil.grandParentTypeIs(
                    ast,
                    LocalTokenType.CLASS_DEF,
                    LocalTokenType.INTERFACE_DEF,
                    LocalTokenType.ANNOTATION_DEF
                )
                && AstUtil.previousSiblingTypeIs(ast, LocalTokenType.LCURLY)
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


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
 * An enhanced version of 'WhitespaceAround': Optionally ignores empty CATCH clauses and empty types.
 *
 * @cs-rule-group  %Whitespace.group
 * @cs-rule-name   de.unkrig.WhitespaceAround
 * @cs-rule-parent TreeWalker
 * @cs-message-key ws.notPreceded
 * @cs-message-key ws.notFollowed
 */
@NotNullByDefault(false) public
class WhitespaceAround extends WhitespaceAroundCheck {

    private boolean allowEmptyCatches;
    private boolean allowEmptyTypes;

    // CONFIGURATION SETTERS -- CHECKSTYLE JavadocMethod:OFF

    // SUPPRESS CHECKSTYLE LineLength:4
    /**
     * @cs-property-name          tokens
     * @cs-property-datatype      MultiCheck
     * @cs-property-default-value ASSIGN,BAND,BAND_ASSIGN,BOR,BOR_ASSIGN,BSR,BSR_ASSIGN,BXOR,BXOR_ASSIGN,COLON,DIV,DIV_ASSIGN,EQUAL,GE,GT,LAND,LCURLY,LE,LITERAL_ASSERT,LITERAL_CATCH,LITERAL_DO,LITERAL_ELSE,LITERAL_FINALLY,LITERAL_FOR,LITERAL_IF,LITERAL_RETURN,LITERAL_SYNCHRONIZED,LITERAL_TRY,LITERAL_WHILE,LOR,LT,MINUS,MINUS_ASSIGN,MOD,MOD_ASSIGN,NOT_EQUAL,PLUS,PLUS_ASSIGN,QUESTION,RCURLY,SL,SLIST,SL_ASSIGN,SR,SR_ASSIGN,STAR,STAR_ASSIGN,LITERAL_ASSERT,TYPE_EXTENSION_AND,WILDCARD_TYPE
     * @cs-property-value-option  ASSIGN
     * @cs-property-value-option  BAND
     * @cs-property-value-option  BAND_ASSIGN
     * @cs-property-value-option  BOR
     * @cs-property-value-option  BOR_ASSIGN
     * @cs-property-value-option  BSR
     * @cs-property-value-option  BSR_ASSIGN
     * @cs-property-value-option  BXOR
     * @cs-property-value-option  BXOR_ASSIGN
     * @cs-property-value-option  COLON
     * @cs-property-value-option  DIV
     * @cs-property-value-option  DIV_ASSIGN
     * @cs-property-value-option  EQUAL
     * @cs-property-value-option  GE
     * @cs-property-value-option  GT
     * @cs-property-value-option  LAND
     * @cs-property-value-option  LCURLY
     * @cs-property-value-option  LE
     * @cs-property-value-option  LITERAL_ASSERT
     * @cs-property-value-option  LITERAL_CATCH
     * @cs-property-value-option  LITERAL_DO
     * @cs-property-value-option  LITERAL_ELSE
     * @cs-property-value-option  LITERAL_FINALLY
     * @cs-property-value-option  LITERAL_FOR
     * @cs-property-value-option  LITERAL_IF
     * @cs-property-value-option  LITERAL_RETURN
     * @cs-property-value-option  LITERAL_SYNCHRONIZED
     * @cs-property-value-option  LITERAL_TRY
     * @cs-property-value-option  LITERAL_WHILE
     * @cs-property-value-option  LOR
     * @cs-property-value-option  LT
     * @cs-property-value-option  MINUS
     * @cs-property-value-option  MINUS_ASSIGN
     * @cs-property-value-option  MOD
     * @cs-property-value-option  MOD_ASSIGN
     * @cs-property-value-option  NOT_EQUAL
     * @cs-property-value-option  PLUS
     * @cs-property-value-option  PLUS_ASSIGN
     * @cs-property-value-option  QUESTION
     * @cs-property-value-option  RCURLY
     * @cs-property-value-option  SL
     * @cs-property-value-option  SLIST
     * @cs-property-value-option  SL_ASSIGN
     * @cs-property-value-option  SR
     * @cs-property-value-option  SR_ASSIGN
     * @cs-property-value-option  STAR
     * @cs-property-value-option  STAR_ASSIGN
     * @cs-property-value-option  LITERAL_ASSERT
     * @cs-property-value-option  TYPE_EXTENSION_AND
     * @cs-property-value-option  WILDCARD_TYPE
     */
    public void
    setTokens(int x) {}

    /**
     * @cs-property-name          allowEmptyConstructors
     * @cs-property-datatype      Boolean
     * @cs-property-default-value false
     */
    @Override public void
    setAllowEmptyConstructors(boolean value) {
        super.setAllowEmptyConstructors(value);
    }

    /**
     * @cs-property-name          allowEmptyMethods
     * @cs-property-datatype      Boolean
     * @cs-property-default-value false
     */
    @Override public void
    setAllowEmptyMethods(boolean value) { super.setAllowEmptyMethods(value); }

    /**
     * @cs-property-name          allowEmptyCatches
     * @cs-property-datatype      Boolean
     * @cs-property-default-value false
     */
    public void
    setAllowEmptyCatches(boolean value) { this.allowEmptyCatches = value; }

    /**
     * @cs-property-name          allowEmptyTypes
     * @cs-property-datatype      Boolean
     * @cs-property-default-value false
     */
    @Override public void
    setAllowEmptyTypes(boolean value) {
        super.setAllowEmptyTypes(value);
        this.allowEmptyTypes = value;
    }

    /**
     * @cs-property-name          ignoreEnhancedForColon
     * @cs-property-datatype      Boolean
     * @cs-property-default-value false
     */
    @Override public void
    setIgnoreEnhancedForColon(boolean value) { super.setIgnoreEnhancedForColon(value); }

    // END CONFIGURATION SETTERS -- CHECKSTYLE JavadocMethod:ON

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

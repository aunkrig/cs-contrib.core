
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
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.eclipsecs.core.config.meta.IOptionProvider;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.Utils;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;

/**
 * Verifies that statements are uniformly wrapped and indented.
 * <p style="color: red">
 *   <b>This check supersedes the following checks:</b>
 * </p>
 * <ul style="color: red">
 *   <li>Left curly brace placement</li>
 *   <li>Right curly brace placement</li>
 *   <li>Operator wrap</li>
 * </ul>
 *
 * @cs-rule-group         %Whitespace.group
 * @cs-rule-name          de.unkrig.WrapAndIndent
 * @cs-rule-parent        TreeWalker
 * @cs-quickfix-classname de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent1
 * @cs-quickfix-classname de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent2
 * @cs-quickfix-classname de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent3
 */
@NotNullByDefault(false) public
class WrapAndIndent extends Check {

    /**
     * Message key as it appears in 'src/de/unkrig/cscontrib/checks/checkstyle-metadata.xml'.
     * <dl>
     *   <dt><code>{0}</code>
     *   <dd>Text of token <i>before</i> the (missing) line break
     *   <dt><code>{1}</code>
     *   <dd>Text of token <i>after</i> the (missing) line break
     * </dl>
     *
     * @cs-message "Must wrap line before ''{1}''"
     */
    public static final String
    MESSAGE_KEY_MUST_WRAP = "de.unkrig.cscontrib.checks.WrapAndIndent.mustWrap";

    /**
     * Message key as it appears in 'src/de/unkrig/cscontrib/checks/checkstyle-metadata.xml'.
     * <dl>
     *   <dt><code>{0}</code>
     *   <dd>Text of token <i>before</i> the (unwanted) line break
     *   <dt><code>{1}</code>
     *   <dd>Text of token <i>after</i> the (unwanted) line break
     * </dl>
     *
     * @cs-message ''{0}'' must appear on same line as ''{1}''
     */
    public static final String
    MESSAGE_KEY_MUST_JOIN = "de.unkrig.cscontrib.checks.WrapAndIndent.mustJoin";

    /**
     * Message key as it appears in 'src/de/unkrig/cscontrib/checks/checkstyle-metadata.xml'.
     * <dl>
     *   <dt><code>{0}</code>
     *   <dd>Text the vertically misaligned token
     *   <dt><code>{1}</code>
     *   <dd>Current (wrong) column number of the token
     *   <dt><code>{2}</code>
     *   <dd>Correct column number of the token
     * </dl>
     *
     * @cs-message ''{0}'' must appear in column {1}, not {2}
     */
    public static final String
    MESSAGE_KEY_WRONG_COLUMN = "de.unkrig.cscontrib.checks.WrapAndIndent.wrongColumn";

    /**
     * The constants of this enum may appear in the '{@code args}' of {@link
     * WrapAndIndent#checkChildren(DetailAST, Object...)} and modify the 'control flow'.
     */
    enum Control {

        /**
         * Indicates that the previous and the next token <i>must</i> either:
         * <ul>
         *   <li>appear in the same line</li>
         *   <li>
         *     appear in different lines, and the next token must appear N columns right from the first
         *     non-space character in the preceding line
         *   </li>
         * </ul>
         */
        MAY_INDENT,

        /**
         * Same as {@link #MAY_INDENT}, but if the next token has no children, then the previous and the next token
         * must appear in the same line.
         */
        INDENT_IF_CHILDREN,

        /**
         * If the tokens of the matching {@link #MAY_INDENT} or {@link #INDENT_IF_CHILDREN} were actually indented,
         * the the previous and the next token must be 'unindented', i.e. the next token must appear in a different
         * line, and its first character must appear N positions left from the first non-space character of the
         * preceding line.
         */
        UNINDENT,

        /** Indicates that the previous and the next token <i>must</i> appear in the same line. */
        NO_WRAP,

        /**
         * Indicates that the previous and the next token <i>must</i> either:
         * <ul>
         *   <li>appear in the same line</li>
         *   <li>
         *     appear in different lines, and the next token must appear N columns right from the first
         *     non-space character in the preceding line
         *   </li>
         * </ul>
         */
        MAY_WRAP,

        /**
         * Indicates that the previous and the next token <i>must</i> appear in different lines, and the
         * next token must appear N columns right from the first non-space character in the preceding line.
         */
        MUST_WRAP,

        /**
         * Indicates that the next of {@code args} is a {@link LocalTokenType}, and that is consumed iff it equals
         * the next token.
         */
        OPTIONAL,

        /**
         * Indicates that at least one more token must exist.
         */
        ANY,

        /**
         * Indicates that the processing can either continue with the next element, or at the element with value
         * {@link #LABEL1}.
         * <p>
         * The same holds true for the other FORK-LABEL pairs.
         */
        FORK1,
        /** @see #FORK1 */
        FORK2,
        /** @see #FORK1 */
        FORK3,
        /** @see #FORK1 */
        FORK4,
        /** @see #FORK1 */
        FORK5,
        /** @see #FORK1 */
        FORK6,
        /** @see #FORK1 */
        FORK7,
        /** @see #FORK1 */
        FORK8,
        /** @see #FORK1 */
        FORK9,

        /**
         * Indicates that the processing continues at the element with value {@link #LABEL1}.
         * <p>
         * The same holds true for the other BRANCH-LABEL pairs.
         */
        BRANCH1,
        /** @see #BRANCH1 */
        BRANCH2,
        /** @see #BRANCH1 */
        BRANCH3,
        /** @see #BRANCH1 */
        BRANCH4,
        /** @see #BRANCH1 */
        BRANCH5,
        /** @see #BRANCH1 */
        BRANCH6,
        /** @see #BRANCH1 */
        BRANCH7,
        /** @see #BRANCH1 */
        BRANCH8,
        /** @see #BRANCH1 */
        BRANCH9,

        /**
         * Merely a target for {@link #FORK1} and {@link #BRANCH1} elements.
         *
         * @see #FORK1
         * @see #BRANCH1
         */
        LABEL1,
        /** @see #LABEL1 */
        LABEL2,
        /** @see #LABEL1 */
        LABEL3,
        /** @see #LABEL1 */
        LABEL4,
        /** @see #LABEL1 */
        LABEL5,
        /** @see #LABEL1 */
        LABEL6,
        /** @see #LABEL1 */
        LABEL7,
        /** @see #LABEL1 */
        LABEL8,
        /** @see #LABEL1 */
        LABEL9,

        /** Indicates that the previous token must not have a sibling. */
        END,
    }

    // BEGIN CONFIGURATION

    /**
     * How many spaces to use for each new indentation level.
     *
     * @cs-intertitle             <h3>Indentation</h3>
     * @cs-property-name          basicOffset
     * @cs-property-datatype      Integer
     * @cs-property-default-value 4
     */
    public void
    setBasicOffset(int value) { this.basicOffset = value; }
    private int basicOffset = 4;

    /**
     * Whether to allow a complete class declaration in one single line:
     * <pre>
     * public class Pojo { int fld; }
     * </pre>
     *
     * @cs-intertitle             <h3>One-line Declarations</h3>
     *                            <p>
     *                              The following properties refer to 'one-line declarations', i.e. declarations
     *                              completely without line breaks.
     *                            </p>
     * @cs-property-name          allowOneLineClassDecl
     * @cs-property-desc          Whether to allow a complete class declaration in one single line
     * @cs-property-datatype      Boolean
     * @cs-property-default-value true
     */
    public void
    setAllowOneLineClassDecl(boolean value) { this.allowOneLineClassDecl = value; }
    private boolean allowOneLineClassDecl = true;

    /**
     * Whether to allow a complete interface declaration in one single line:
     * <pre>
     * public interface Interf { void meth(); }
     * </pre>
     *
     * @cs-property-name          allowOneLineInterfaceDecl
     * @cs-property-desc          Whether to allow a complete interface declaration in one single line
     * @cs-property-datatype      Boolean
     * @cs-property-default-value true
     */
    public void
    setAllowOneLineInterfaceDecl(boolean value) { this.allowOneLineInterfaceDecl = value; }
    private boolean allowOneLineInterfaceDecl = true;

    /**
     * Whether to allow a complete enum declaration in one single line:
     * <pre>
     * private enum Color { BLACK, WHITE }
     * </pre>
     *
     * @cs-property-name          allowOneLineEnumDecl
     * @cs-property-desc          Whether to allow a complete enum declaration in one single line
     * @cs-property-datatype      Boolean
     * @cs-property-default-value true
     */
    public void
    setAllowOneLineEnumDecl(boolean value) { this.allowOneLineEnumDecl = value; }
    private boolean allowOneLineEnumDecl = true;

    /**
     * Whether to allow a complete annotation declaration in one single line:
     * <pre>
     * public @interface MyAnno {}
     * </pre>
     *
     * @cs-property-name          allowOneLineAnnoDecl
     * @cs-property-desc          Whether to allow a complete annotation declaration in one single line
     * @cs-property-datatype      Boolean
     * @cs-property-default-value true
     */
    public void
    setAllowOneLineAnnoDecl(boolean value) { this.allowOneLineAnnoDecl = value; }
    private boolean allowOneLineAnnoDecl = true;

    /**
     * Whether to allow a complete constructor declaration in one single line:
     * <pre>
     * protected MyClass() { super(null); }
     * </pre>
     *
     * @cs-property-name          allowOneLineCtorDecl
     * @cs-property-desc          Whether to allow a complete constructor declaration in one single line
     * @cs-property-datatype      Boolean
     * @cs-property-default-value true
     */
    public void
    setAllowOneLineCtorDecl(boolean value) { this.allowOneLineCtorDecl = value; }
    private boolean allowOneLineCtorDecl = true;

    /**
     * Whether to allow a complete method declaration in one single line:
     * <pre>
     * private void meth() { ... }
     * </pre>
     *
     * @cs-property-name          allowOneLineMethDecl
     * @cs-property-desc          Whether to allow a complete method declaration in one single line
     * @cs-property-datatype      Boolean
     * @cs-property-default-value true
     */
    public void
    setAllowOneLineMethDecl(boolean value) { this.allowOneLineMethDecl = value; }
    private boolean allowOneLineMethDecl = true;

    /**
     * Whether to allow a complete {@code switch} block statement group in one single line:
     * <pre>
     * case 1: case 2: a = 3; break;
     * </pre>
     *
     * @cs-property-name          allowOneLineSwitchBlockStmtGroup
     * @cs-property-desc          Whether to allow a complete {@code switch} block statement group in one single line
     * @cs-property-datatype      Boolean
     * @cs-property-default-value true
     */
    public void
    setAllowOneLineSwitchBlockStmtGroup(boolean value) { this.allowOneLineSwitchBlockStmtGroup = value; }
    private boolean allowOneLineSwitchBlockStmtGroup = true;

    /**
     * Whether to wrap package declarations before the {@code package} keyword (in "{@code package-info.java}"):
     * <pre>
     * &#64;NonNullByDefault
     * package com.acme.product;
     * </pre>
     *
     * @cs-intertitle               <h3>Declaration Wrapping</h3>
     *                              <p>
     *                                The phrase "wrap before X" means that a line break and spaces appear right before
     *                                "X", such that "X" is vertically aligned with the first token in the immediately
     *                                preceding line.
     *                              </p>
     * @cs-property-name            wrapPackageDeclBeforePackage
     * @cs-property-desc            Whether to wrap package declarations before the {@code package} keyword (in
     *                              '"{@code package-info.java}")
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   always
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapPackageDeclBeforePackage(String value) { this.wrapPackageDeclBeforePackage = WrapAndIndent.toWrap(value); }
    private Control wrapPackageDeclBeforePackage = MUST_WRAP;

    /**
     * Whether to wrap class declarations before the {@code class} keyword:
     * <pre>
     * public static final
     * class MyClass {
     * </pre>
     *
     * @cs-property-name            wrapClassDeclBeforeClass
     * @cs-property-desc            Whether to wrap class declarations before the {@code class} keyword
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   always
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapClassDeclBeforeClass(String value) { this.wrapClassDeclBeforeClass = WrapAndIndent.toWrap(value); }
    private Control wrapClassDeclBeforeClass = MUST_WRAP;

    /**
     * Whether to wrap interface declarations before the {@code interface} keyword:
     * <pre>
     * public
     * interface MyInterf {
     * </pre>
     *
     * @cs-property-name            wrapInterfaceDeclBeforeInterface
     * @cs-property-desc            Whether to wrap interface declarations before the {@code interface} keyword
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   always
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapInterfaceDeclBeforeInterface(String value) {
        this.wrapInterfaceDeclBeforeInterface = WrapAndIndent.toWrap(value);
    }
    private Control wrapInterfaceDeclBeforeInterface = MUST_WRAP;

    /**
     * Whether to wrap enum declarations before the {@code enum} keyword:
     * <pre>
     * protected
     * enum MyEnum {
     * </pre>
     *
     * @cs-property-name            wrapEnumDeclBeforeEnum
     * @cs-property-desc            Whether to wrap enum declarations before the {@code enum} keyword
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   always
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapEnumDeclBeforeEnum(String value) { this.wrapEnumDeclBeforeEnum = WrapAndIndent.toWrap(value); }
    private Control wrapEnumDeclBeforeEnum = MUST_WRAP;

    /**
     * Whether to wrap annotation declarations before '@':
     * <pre>
     * private
     * &#64;interface MyAnno {
     * </pre>
     *
     * @cs-property-name            wrapAnnoDeclBeforeAt
     * @cs-property-desc            Whether to wrap annotation declarations before '@'
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   always
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapAnnoDeclBeforeAt(String value) { this.wrapAnnoDeclBeforeAt = WrapAndIndent.toWrap(value); }
    private Control wrapAnnoDeclBeforeAt = MUST_WRAP;

    /**
     * Whether to wrap field declarations before the field name:
     * <pre>
     * private int
     * width = 7;
     * </pre>
     *
     * @cs-property-name            wrapFieldDeclBeforeName
     * @cs-property-desc            Whether to wrap field declarations before the field name
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   optional
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapFieldDeclBeforeName(String value) { this.wrapFieldDeclBeforeName = WrapAndIndent.toWrap(value); }
    private Control wrapFieldDeclBeforeName = MAY_WRAP;

    /**
     * Whether to wrap constructor declarations between the modifiers and the class name:
     * <pre>
     * protected
     * MyClass(int x) {
     * </pre>
     *
     * @cs-property-name            wrapCtorDeclBeforeName
     * @cs-property-desc            Whether to wrap constructor declarations between the modifiers and the class name
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   always
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapCtorDeclBeforeName(String value) { this.wrapCtorDeclBeforeName = WrapAndIndent.toWrap(value); }
    private Control wrapCtorDeclBeforeName = MUST_WRAP;

    /**
     * Whether to wrap method declarations between the return type and the method name:
     * <pre>
     * private static
     * myMeth(int arg1) {
     * </pre>
     *
     * @cs-property-name            wrapMethDeclBeforeName
     * @cs-property-desc            Whether to wrap method declarations between the return type and the method name
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   always
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapMethDeclBeforeName(String value) { this.wrapMethDeclBeforeName = WrapAndIndent.toWrap(value); }
    private Control wrapMethDeclBeforeName = MUST_WRAP;

    /**
     * Whether to wrap local variable declarations between the type and the variable name:
     * <pre>
     * int
     * locvar = 7;
     * </pre>
     *
     * @cs-property-name            wrapLocVarDeclBeforeName
     * @cs-property-desc            Whether to wrap local variable declarations between the type and the variable name
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   optional
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapLocVarDeclBeforeName(String value) { this.wrapLocVarDeclBeforeName = WrapAndIndent.toWrap(value); }
    private Control wrapLocVarDeclBeforeName = MAY_WRAP;

    /**
     * Whether to wrap type declarations before the opening curly brace:
     * <pre>
     * public class MyClass
     * {
     * </pre>
     *
     * @cs-property-name            wrapTypeDeclBeforeLCurly
     * @cs-property-desc            Whether to wrap type declarations before the opening curly brace
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   never
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapTypeDeclBeforeLCurly(String value) { this.wrapTypeDeclBeforeLCurly = WrapAndIndent.toWrap(value); }
    private Control wrapTypeDeclBeforeLCurly = NO_WRAP;

    /**
     * Whether to wrap constructor declarations before the opening curly brace:
     * <pre>
     * protected MyClass(int x)
     * {
     * </pre>
     *
     * @cs-property-name            wrapCtorDeclBeforeLCurly
     * @cs-property-desc            Whether to wrap constructors declaration before the opening curly brace
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   never
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapCtorDeclBeforeLCurly(String value) { this.wrapCtorDeclBeforeLCurly = WrapAndIndent.toWrap(value); }
    private Control wrapCtorDeclBeforeLCurly = NO_WRAP;

    /**
     * Whether to wrap method declarations before the opening curly brace:
     * <pre>
     * private static myMeth(int arg1)
     * {
     * </pre>
     *
     * @cs-property-name            wrapMethodDeclBeforeLCurly
     * @cs-property-desc            Whether to wrap method declarations before the opening curly brace
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   never
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapMethodDeclBeforeLCurly(String value) { this.wrapMethodDeclBeforeLCurly = WrapAndIndent.toWrap(value); }
    private Control wrapMethodDeclBeforeLCurly = NO_WRAP;

    /**
     * Whether to wrap anonymous class declarations before the opening curly brace:
     * <pre>
     * new Object()
     * {
     * </pre>
     *
     * @cs-property-name            wrapAnonClassDeclBeforeLCurly
     * @cs-property-desc            Whether to wrap anonymous class declarations before the opening curly brace
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   never
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapAnonClassDeclBeforeLCurly(String value) { this.wrapAnonClassDeclBeforeLCurly = WrapAndIndent.toWrap(value); }
    private Control wrapAnonClassDeclBeforeLCurly = NO_WRAP;

    /**
     * Whether to wrap {@code do} statements before the opening curly brace:
     * <pre>
     * do
     * {
     * </pre>
     *
     * @cs-intertitle               <h3>Other Elements Wrapping</h3>
     *                              <p>
     *                                The phrase "wrap before X" means that a line break and space appear right before
     *                                "X", such that "X" is vertically aligned with the first token in the immediately
     *                                preceding line.
     *                              </p>
     * @cs-property-name            wrapDoBeforeLCurly
     * @cs-property-desc            Whether to wrap {@code do} statements before the opening curly brace
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   never
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapDoBeforeLCurly(String value) { this.wrapDoBeforeLCurly = WrapAndIndent.toWrap(value); }
    private Control wrapDoBeforeLCurly = NO_WRAP;

    /**
     * Whether to wrap {@code try} statements before the {@code catch} keyword:
     * <pre>
     * try { ... }
     * catch { ... }
     * </pre>
     *
     * @cs-property-name            wrapTryBeforeCatch
     * @cs-property-desc            Whether to wrap {@code try} statements before the {@code catch} keyword
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   optional
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapTryBeforeCatch(String value) { this.wrapTryBeforeCatch = WrapAndIndent.toWrap(value); }
    private Control wrapTryBeforeCatch = MAY_WRAP;

    /**
     * Whether to wrap {@code try} statements before the {@code finally} keyword:
     * <pre>
     * try { ... }
     * finally { ... }
     * </pre>
     *
     * @cs-property-name            wrapTryBeforeFinally
     * @cs-property-desc            Whether to wrap {@code try} statements before the {@code finally} keyword
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   optional
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapTryBeforeFinally(String value) { this.wrapTryBeforeFinally = WrapAndIndent.toWrap(value); }
    private Control wrapTryBeforeFinally = MAY_WRAP;

    /**
     * Whether to wrap array initializers before the opening curly brace:
     * <pre>
     * int[] ia =
     * {
     * </pre>
     *
     * @cs-property-name            wrapArrayInitBeforeLCurly
     * @cs-property-desc            Whether to wrap array initializers before the opening curly brace
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   never
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapArrayInitBeforeLCurly(String value) { this.wrapArrayInitBeforeLCurly = WrapAndIndent.toWrap(value); }
    private Control wrapArrayInitBeforeLCurly = NO_WRAP;

    /**
     * Whether to wrap expressions before "{@code + - * / % &amp; | ^ << >> >>>}", assignment operators and/or "{@code
     * . <= < == != >= > &amp;&amp; || instanceof}":
     * <pre>
     * a
     * + b
     * + c
     * </pre>
     *
     * @cs-property-name            wrapBeforeBinaryOperator
     * @cs-property-desc            Whether to wrap expression before "+ - * / % &amp; | ^ << >> >>>", assignment
     *                              operators and/or ". <= < == != >= > &amp;&amp; || instanceof"
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   optional
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapBeforeBinaryOperator(String value) { this.wrapBeforeBinaryOperator = WrapAndIndent.toWrap(value); }
    private Control wrapBeforeBinaryOperator = MAY_WRAP;

    /**
     * Whether to wrap expression after "{@code + - * / % && | ^ << >> >>>}", assignment operators and/or "{@code . <=
     * < == != >= > &&&& || instanceof}":
     * <pre>
     * a +
     * b +
     * c
     * </pre>
     *
     * @cs-property-name            wrapAfterBinaryOperator
     * @cs-property-desc            Whether to wrap expression after "{@code + - * / % && | ^ << >> >>>", assignment
     *                              operators and/or "{@code . <= < == != >= > && || instanceof}"
     * @cs-property-datatype        SingleSelect
     * @cs-property-default-value   never
     * @cs-property-option-provider de.unkrig.cscontrib.checks.WrapAndIndent$WrapOptionProvider
     */
    public void
    setWrapAfterBinaryOperator(String value) { this.wrapAfterBinaryOperator = WrapAndIndent.toWrap(value); }
    private Control wrapAfterBinaryOperator = NO_WRAP;

    // END CONFIGURATION

    /**
     * For a more compact notation in 'checkstyle-metadata.xml' we define this {@link IOptionProvider}.
     */
    public static
    class WrapOptionProvider implements IOptionProvider {

        private static final List<String>
        WRAP_OPTIONS = Collections.unmodifiableList(Arrays.asList("always", "optional", "never"));

        @Override public List<String>
        getOptions() { return WrapOptionProvider.WRAP_OPTIONS; }
    }

    private static Control
    toWrap(String value) {
        return (
            "always".equals(value)   ? MUST_WRAP :
            "optional".equals(value) ? MAY_WRAP :
            "never".equals(value)    ? NO_WRAP :
            WrapAndIndent.throwException(RuntimeException.class, Control.class, "Invalid string value '" + value + "'")
        );
    }

    private static <ET extends Exception, RT> RT
    throwException(Class<ET> exceptionType, Class<RT> returnType, String message) throws ET {
        ET exception;

        try {
            exception = exceptionType.getConstructor(String.class).newInstance(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw exception;
    }

    @Override public int[]
    getDefaultTokens() {
        return LocalTokenType.delocalize(new LocalTokenType[] {
//            ABSTRACT,
            ANNOTATION,
            ANNOTATIONS,
            ANNOTATION_ARRAY_INIT,
            ANNOTATION_DEF,
            ANNOTATION_FIELD_DEF,
            ANNOTATION_MEMBER_VALUE_PAIR,
            ARRAY_DECLARATOR,
            ARRAY_INIT,
            ASSIGN,  // To check 'int[] ia = { 1, 2, 3 };'.
//            AT,
//            BAND,
//            BAND_ASSIGN,
//            BNOT,
//            BOR,
//            BOR_ASSIGN,
//            BSR,
//            BSR_ASSIGN,
//            BXOR,
//            BXOR_ASSIGN,
            CASE_GROUP,
//            CHAR_LITERAL,
            CLASS_DEF,
//            COLON,
//            COMMA,
            CTOR_CALL,
            CTOR_DEF,
//            DEC,
//            DIV,
//            DIV_ASSIGN,
            DOT,
//            DO_WHILE,
            ELIST,
//            ELLIPSIS,
//            EMPTY_STAT,
//            ENUM,
            ENUM_CONSTANT_DEF,
            ENUM_DEF,
//            EOF,
//            EQUAL,
            EXPR,
            EXTENDS_CLAUSE,
//            FINAL,
            FOR_CONDITION,
            FOR_EACH_CLAUSE,
            FOR_INIT,
            FOR_ITERATOR,
//            GE,
//            GENERIC_END,
//            GENERIC_START,
//            GT,
//            IDENT,
            IMPLEMENTS_CLAUSE,
            IMPORT,
//            INC,
//            INDEX_OP,
            INSTANCE_INIT,
            INTERFACE_DEF,
            LABELED_STAT,
//            LAND,
//            LCURLY,
//            LE,
            LITERAL_ASSERT,
//            LITERAL_BOOLEAN,
            LITERAL_BREAK,
//            LITERAL_BYTE,
            LITERAL_CASE,
            LITERAL_CATCH,
//            LITERAL_CHAR,
//            LITERAL_CLASS,
            LITERAL_CONTINUE,
//            LITERAL_DEFAULT,
            LITERAL_DO,
//            LITERAL_DOUBLE,
//            LITERAL_ELSE,
//            LITERAL_FALSE,
            LITERAL_FINALLY,
//            LITERAL_FLOAT,
            LITERAL_FOR,
            LITERAL_IF,
//            LITERAL_INSTANCEOF,
//            LITERAL_INT,
//            LITERAL_INTERFACE,
//            LITERAL_LONG,
//            LITERAL_NATIVE,
            LITERAL_NEW,
//            LITERAL_NULL,
//            LITERAL_PRIVATE,
//            LITERAL_PROTECTED,
//            LITERAL_PUBLIC,
            LITERAL_RETURN,
//            LITERAL_SHORT,
//            LITERAL_STATIC,
//            LITERAL_SUPER,
            LITERAL_SWITCH,
            LITERAL_SYNCHRONIZED,
//            LITERAL_THIS,
            LITERAL_THROW,
            LITERAL_THROWS,
//            LITERAL_TRANSIENT,
//            LITERAL_TRUE,
            LITERAL_TRY,
//            LITERAL_VOID,
//            LITERAL_VOLATILE,
            LITERAL_WHILE,
//            LNOT,
//            LOR,
//            LPAREN,
//            LT,
//            METHOD_CALL,
            METHOD_DEF,
//            MINUS,
//            MINUS_ASSIGN,
//            MOD,
            MODIFIERS,
//            MOD_ASSIGN,
//            NOT_EQUAL,
//            NUM_DOUBLE,
//            NUM_FLOAT,
//            NUM_INT,
//            NUM_LONG,
            OBJBLOCK,
            PACKAGE_DEF,
            PARAMETERS,
            PARAMETER_DEF,
//            PLUS,
//            PLUS_ASSIGN,
//            POST_DEC,
//            POST_INC,
//            QUESTION,
//            RBRACK,
//            RCURLY,
//            RPAREN,
//            SEMI,
//            SL,
            SLIST,
//            SL_ASSIGN,
//            SR,
//            SR_ASSIGN,
//            STAR,
//            STAR_ASSIGN,
            STATIC_IMPORT,
            STATIC_INIT,
//            STRICTFP,
//            STRING_LITERAL,
            SUPER_CTOR_CALL,
//            TYPE,
//            TYPECAST,
            TYPE_ARGUMENT,
            TYPE_ARGUMENTS,
//            TYPE_EXTENSION_AND,
            TYPE_LOWER_BOUNDS,
            TYPE_PARAMETER,
            TYPE_PARAMETERS,
            TYPE_UPPER_BOUNDS,
//            UNARY_MINUS,
//            UNARY_PLUS,
            VARIABLE_DEF,
//            WILDCARD_TYPE,
        });
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        @SuppressWarnings("unused") AstDumper dumper = new AstDumper(ast); // For debugging

        switch (LocalTokenType.localize(ast.getType())) {

        case ANNOTATION:
            this.checkChildren(
                ast,
                AT, FORK1, DOT, BRANCH2,
                LABEL1, IDENT,
                LABEL2, FORK3, END,
                LABEL3, LPAREN, BRANCH5,
                LABEL4, COMMA,
                LABEL5, FORK6, MAY_INDENT, ANNOTATION_MEMBER_VALUE_PAIR, BRANCH9,
                LABEL6, FORK7, MAY_INDENT, ANNOTATION, BRANCH9,
                LABEL7, FORK8, MAY_INDENT, EXPR, BRANCH9,
                LABEL8, MAY_INDENT, ANNOTATION_ARRAY_INIT,
                LABEL9, FORK4, UNINDENT, RPAREN, END
            );
            break;

        case ANNOTATION_ARRAY_INIT:
            this.checkChildren(
                ast,
                FORK2,
                LABEL1, MAY_INDENT, EXPR, FORK2, COMMA, FORK1,
                LABEL2, UNINDENT, RCURLY, END
            );
            break;

        case ANNOTATION_DEF:
            if (this.allowOneLineAnnoDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,
                MODIFIERS, this.wrapAnnoDeclBeforeAt, AT, LITERAL_INTERFACE, IDENT, this.wrapTypeDeclBeforeLCurly, OBJBLOCK, END // SUPPRESS CHECKSTYLE LineLength
            );
            break;

        case ARRAY_INIT:
            this.checkChildren(
                ast,
                FORK2,
                LABEL1, MAY_INDENT, ANY, FORK2, COMMA, FORK1,
                LABEL2, UNINDENT, RCURLY, END
            );
            break;

        case CASE_GROUP:
            this.checkChildren(
                ast,
                FORK3, LITERAL_CASE,        // case 1: case 2:
                LABEL1, FORK2, MAY_WRAP, LITERAL_CASE, BRANCH1,
                LABEL2, FORK4,
                LABEL3, MAY_WRAP, LITERAL_DEFAULT,
                LABEL4, INDENT_IF_CHILDREN, SLIST, END
            );
            break;

        case CLASS_DEF:
            if (this.allowOneLineClassDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,
                MODIFIERS, this.wrapClassDeclBeforeClass, LITERAL_CLASS, IDENT, FORK1, TYPE_PARAMETERS,
                LABEL1, FORK2, MAY_WRAP, EXTENDS_CLAUSE,
                LABEL2, FORK3, MAY_WRAP, IMPLEMENTS_CLAUSE,
                LABEL3, this.wrapTypeDeclBeforeLCurly, OBJBLOCK, END
            );
            break;

        case CTOR_CALL:
            this.checkChildren(
                ast,
                LPAREN, INDENT_IF_CHILDREN, ELIST, UNINDENT, RPAREN, SEMI, END
            );
            break;

        case CTOR_DEF:
            if (this.allowOneLineCtorDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,
                MODIFIERS, FORK1, TYPE_PARAMETERS,
                LABEL1, this.wrapCtorDeclBeforeName, IDENT, LPAREN, INDENT_IF_CHILDREN, PARAMETERS, UNINDENT, RPAREN, FORK2, MAY_WRAP, LITERAL_THROWS, // SUPPRESS CHECKSTYLE LineLength
                LABEL2, this.wrapCtorDeclBeforeLCurly, SLIST, END
            );
            break;

        case ELIST:
            this.checkChildren(
                ast,
                FORK2,
                LABEL1, MAY_INDENT, EXPR, FORK2, COMMA, BRANCH1,
                LABEL2, END
            );
            break;

        case ENUM_DEF:
            if (this.allowOneLineEnumDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,
                MODIFIERS, this.wrapEnumDeclBeforeEnum, ENUM, IDENT, this.wrapTypeDeclBeforeLCurly, OBJBLOCK, END
            );
            break;

        case EXPR:
            {
                DetailAST child = ast.getFirstChild();
                if (child.getType() == LPAREN.delocalize()) {
                    child = this.checkParenthesizedExpression(child, false);
                    assert child == null;
                } else {
                    boolean inline;
                    switch (LocalTokenType.localize(ast.getParent().getType())) {

                    case INDEX_OP:                     // a[#]
                    case ANNOTATION:                   // @SuppressWarnings(#)
                    case ANNOTATION_ARRAY_INIT:        // @SuppressWarnings({ "rawtypes", "unchecked" })
                    case ANNOTATION_MEMBER_VALUE_PAIR: // @Author(@Name(first = "Joe", last = "Hacker"))
                    case ASSIGN:                       // a = #
                    case FOR_CONDITION:                // for (; #;)
                    case FOR_EACH_CLAUSE:              // for (Object o : #)
                    case LITERAL_ASSERT:               // assert #
                    case LITERAL_CASE:                 // case #:
                    case LITERAL_DEFAULT:              // @interface MyAnnotation { boolean value() default true; }
                    case LITERAL_ELSE:                 // else #;
                    case LITERAL_FOR:                  // for (...; ...; ...) #;
                    case LITERAL_RETURN:               // return #
                    case LITERAL_THROW:                // throw #
                    case SLIST:                        // #;
                        inline = true;
                        break;

                    case ARRAY_DECLARATOR:     // new String[#]
                    case ARRAY_INIT:           // int[] a = { # }
                    case LITERAL_DO:           // do { ... } while (#)
                    case LITERAL_IF:           // if (#)
                    case LITERAL_SWITCH:       // switch (#)
                    case LITERAL_SYNCHRONIZED: // synchronized (#)
                    case LITERAL_WHILE:        // while (#)
                        inline = ast.getParent().getLineNo() == ast.getLineNo();
                        break;

                    case ELIST:                // meth(#, #, #)
                        inline = ast.getParent().getChildCount() != 1;
                        break;

                    default:
                        assert false : (
                            this.getFileContents().getFilename()
                            + ":"
                            + ast.getLineNo()
                            + ": EXPR has unexpected parent "
                            + LocalTokenType.localize(ast.getParent().getType())
                        );
                        inline = false;
                        break;
                    }
                    this.checkExpression(child, inline);
                }
            }
            break;

        case FOR_EACH_CLAUSE:
            this.checkChildren(
                ast,
                VARIABLE_DEF, MAY_WRAP, COLON, EXPR, END
            );
            break;

        case INTERFACE_DEF:
            if (this.allowOneLineInterfaceDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,
                MODIFIERS, this.wrapInterfaceDeclBeforeInterface, LITERAL_INTERFACE, IDENT, FORK1, TYPE_PARAMETERS,
                LABEL1, FORK2, MAY_WRAP, EXTENDS_CLAUSE,
                LABEL2, this.wrapTypeDeclBeforeLCurly, OBJBLOCK, END
            );
            break;

        case LABELED_STAT:
            this.checkChildren(
                ast,
                IDENT, MAY_WRAP, ANY, END
            );
            break;

        case LITERAL_DO:
            this.checkChildren(
                ast,
                this.wrapDoBeforeLCurly, SLIST, DO_WHILE, LPAREN, MAY_INDENT, EXPR, UNINDENT, RPAREN, SEMI, END
            );
            break;

        case LITERAL_FOR:
            this.checkChildren(
                ast,
                LPAREN, FORK1, MAY_INDENT, FOR_INIT, SEMI, MAY_INDENT, FOR_CONDITION, SEMI, INDENT_IF_CHILDREN, FOR_ITERATOR, FORK2, // SUPPRESS CHECKSTYLE LineLength
                LABEL1, MAY_INDENT, FOR_EACH_CLAUSE,
                LABEL2, UNINDENT, RPAREN, FORK3, EXPR, SEMI, END,
                LABEL3, ANY, END
            );
            break;

        case LITERAL_IF:
            this.checkChildren(
                ast,
                LPAREN, MAY_INDENT, EXPR, UNINDENT, RPAREN, FORK1, EXPR, SEMI, END,
                LABEL1, ANY, FORK2, LITERAL_ELSE,
                LABEL2, END
            );
            break;

        case LITERAL_NEW:
            this.checkChildren(
                ast,
                ANY, FORK1, TYPE_ARGUMENTS,
                LABEL1, FORK3, ARRAY_DECLARATOR, FORK2, this.wrapArrayInitBeforeLCurly, ARRAY_INIT,
                LABEL2, END,
                LABEL3, LPAREN, INDENT_IF_CHILDREN, ELIST, UNINDENT, RPAREN, OPTIONAL, this.wrapAnonClassDeclBeforeLCurly, OBJBLOCK, END // SUPPRESS CHECKSTYLE LineLength
            );
            break;

        case LITERAL_SWITCH:
            this.checkChildren(
                ast,
                LPAREN, MAY_INDENT, EXPR, UNINDENT, RPAREN, LCURLY, FORK2,
                LABEL1, MAY_INDENT, CASE_GROUP, FORK1,
                LABEL2, UNINDENT, RCURLY, END
            );
            break;

        case METHOD_DEF:
            if (this.allowOneLineMethDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,
                MODIFIERS, FORK1, TYPE_PARAMETERS,
                LABEL1, TYPE, this.wrapMethDeclBeforeName, IDENT, LPAREN, INDENT_IF_CHILDREN, PARAMETERS, UNINDENT, RPAREN, FORK2, MAY_WRAP, LITERAL_THROWS, // SUPPRESS CHECKSTYLE LineLength
                LABEL2, FORK3, this.wrapMethodDeclBeforeLCurly, SLIST, END,
                LABEL3, SEMI, END
            );
            break;

        case LITERAL_WHILE:
            this.checkChildren(
                ast,
                LPAREN, MAY_INDENT, EXPR, UNINDENT, RPAREN, FORK1,  EXPR, SEMI, END,
                LABEL1, ANY, END
            );
            break;

        case MODIFIERS:
            this.checkChildren(
                ast,
                FORK2,
                LABEL1, ANY, FORK1,
                LABEL2, END
            );
            break;

        case OBJBLOCK:
            this.checkChildren(
                ast,
                LCURLY, FORK3,
                LABEL1, MAY_INDENT, ENUM_CONSTANT_DEF, FORK2, COMMA, FORK1,
                LABEL2, FORK3, MAY_INDENT, SEMI,
                LABEL3, FORK5, MAY_INDENT, VARIABLE_DEF,
                LABEL4, FORK3, COMMA, VARIABLE_DEF, BRANCH4,
                LABEL5, FORK6, UNINDENT, RCURLY, END,
                LABEL6, MAY_INDENT, ANY, BRANCH2
            );
            break;

        case PARAMETERS:
            this.checkChildren(
                ast,
                FORK2,
                LABEL1, MAY_INDENT, PARAMETER_DEF, FORK2, COMMA, BRANCH1,
                LABEL2, END
            );
            break;

        case SLIST:
            // Single-line case group?
            if (
                ast.getParent().getType() == CASE_GROUP.delocalize()
                && this.allowOneLineSwitchBlockStmtGroup
                && WrapAndIndent.isSingleLine(ast)
                && ast.getParent().getLineNo() == ast.getLineNo()
            ) return;

            this.checkChildren(
                ast,
                LABEL1, FORK2, MAY_INDENT, EXPR, SEMI, BRANCH1,
                LABEL2, FORK5, MAY_INDENT, VARIABLE_DEF,
                LABEL3, FORK4, COMMA, VARIABLE_DEF, BRANCH3,
                LABEL4, SEMI, BRANCH1,
                // SLIST in CASE_GROUP ends _without_ an RCURLY!
                LABEL5, FORK6, END,
                LABEL6, FORK7, UNINDENT, RCURLY, END,
                LABEL7, MAY_INDENT, ANY, BRANCH1
            );
            break;

        case SUPER_CTOR_CALL:
            this.checkChildren(
                ast,
                FORK1, ANY, DOT,
                LABEL1, LPAREN, INDENT_IF_CHILDREN, ELIST, UNINDENT, RPAREN, SEMI, END
            );
            break;

        case VARIABLE_DEF:
            if (ast.getParent().getType() == OBJBLOCK.delocalize()) {
                this.checkChildren(
                    ast,
                    MODIFIERS, TYPE, this.wrapFieldDeclBeforeName, IDENT, FORK1, ASSIGN,
                    // Field declarations DO have a SEMI, local variable declarations DON'T!?
                    LABEL1, FORK2, SEMI,
                    LABEL2, END
                );
            } else {
                this.checkChildren(
                    ast,
                    MODIFIERS, TYPE, this.wrapLocVarDeclBeforeName, IDENT, FORK1, ASSIGN,
                    // Field declarations DO have a SEMI, local variable declarations DON'T!?
                    LABEL1, FORK2, SEMI,
                    LABEL2, END
                );
            }
            break;

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
        case PACKAGE_DEF:
            this.checkSameLine(ast, WrapAndIndent.getLeftmostDescendant(ast.getFirstChild().getNextSibling()));
            if (ast.getFirstChild().getFirstChild() == null) break; // No annotation(s)
            if (this.wrapPackageDeclBeforePackage == NO_WRAP) {
                this.checkSameLine(ast, ast.getFirstChild().getFirstChild().getFirstChild());
                break;
            }
            if (this.wrapPackageDeclBeforePackage == MAY_WRAP && WrapAndIndent.isSingleLine(ast)) break;
            this.checkWrapped(ast.getFirstChild().getFirstChild().getFirstChild(), ast);
            this.checkSameLine(ast, ast.getFirstChild().getNextSibling().getNextSibling());
            break;

        case ASSIGN:
            if (ast.getChildCount() == 1) {

                // A field or local variable initialization.
                this.checkChildren(
                    ast,
                    FORK1, this.wrapArrayInitBeforeLCurly, ARRAY_INIT, END,
                    LABEL1, ANY, END
                );
            }
            break;

        case LITERAL_TRY:
            this.checkChildren(
                ast,
                SLIST, FORK2,
                LABEL1, this.wrapTryBeforeCatch, LITERAL_CATCH, FORK1, FORK3,
                LABEL2, this.wrapTryBeforeFinally, LITERAL_FINALLY,
                LABEL3, END
            );
            break;

            // Those which were not registered for.
        case ABSTRACT:
        case AT:
        case BAND:
        case BAND_ASSIGN:
        case BNOT:
        case BOR:
        case BOR_ASSIGN:
        case BSR:
        case BSR_ASSIGN:
        case BXOR:
        case BXOR_ASSIGN:
        case CHAR_LITERAL:
        case COLON:
        case COMMA:
        case DEC:
        case DIV:
        case DIV_ASSIGN:
        case DO_WHILE: // The 'while' keyword at the end of the DO...WHILE loop.
        case ELLIPSIS:
        case EMPTY_STAT:
        case ENUM:
        case EOF:
        case EQUAL:
        case FINAL:
        case GE:
        case GENERIC_END:
        case GENERIC_START:
        case GT:
        case IDENT:
        case INC:
        case INDEX_OP:
        case LAND:
        case LCURLY:
        case LE:
        case LITERAL_BOOLEAN:
        case LITERAL_BYTE:
        case LITERAL_CHAR:
        case LITERAL_CLASS:
        case LITERAL_DEFAULT:
        case LITERAL_DOUBLE:
        case LITERAL_ELSE:
        case LITERAL_FALSE:
        case LITERAL_FLOAT:
        case LITERAL_INSTANCEOF:
        case LITERAL_INT:
        case LITERAL_INTERFACE:
        case LITERAL_LONG:
        case LITERAL_NATIVE:
        case LITERAL_NULL:
        case LITERAL_PRIVATE:
        case LITERAL_PROTECTED:
        case LITERAL_PUBLIC:
        case LITERAL_SHORT:
        case LITERAL_STATIC:
        case LITERAL_SUPER:
        case LITERAL_THIS:
        case LITERAL_TRANSIENT:
        case LITERAL_TRUE:
        case LITERAL_VOID:
        case LITERAL_VOLATILE:
        case LNOT:
        case LOR:
        case LPAREN:
        case LT:
        case METHOD_CALL:
        case MINUS:
        case MINUS_ASSIGN:
        case MOD:
        case MOD_ASSIGN:
        case NOT_EQUAL:
        case NUM_DOUBLE:
        case NUM_FLOAT:
        case NUM_INT:
        case NUM_LONG:
        case PLUS:
        case PLUS_ASSIGN:
        case POST_DEC:
        case POST_INC:
        case QUESTION:
        case RBRACK:
        case RCURLY:
        case RPAREN:
        case SEMI:
        case SL:
        case SL_ASSIGN:
        case SR:
        case SR_ASSIGN:
        case STAR:
        case STAR_ASSIGN:
        case STRICTFP:
        case STRING_LITERAL:
        case TYPE:
        case TYPECAST:
        case UNARY_MINUS:
        case UNARY_PLUS:
        case TYPE_EXTENSION_AND:
        case WILDCARD_TYPE:
            throw new AssertionError("Token type '" + ast.getType() + "' was not registered for, was visited though");

        // Other tokens which may have children.
        case DOT:
            // Verify that the dot is used in a PACKAGE declaration or in an IMPORT directive, and not in an
            // expression.
            PACKAGE_OR_IMPORT: {
                for (DetailAST a = ast; a != null; a = a.getParent()) {
                    LocalTokenType aType = LocalTokenType.localize(a.getType());
                    if (aType == PACKAGE_DEF || aType == IMPORT) break PACKAGE_OR_IMPORT;
                }
                break;
            }
            /*FALLTHROUGH*/
        case ANNOTATION_FIELD_DEF:
        case ANNOTATION_MEMBER_VALUE_PAIR:
        case ANNOTATIONS:
        case ARRAY_DECLARATOR:
        case ENUM_CONSTANT_DEF:
        case EXTENDS_CLAUSE:
        case FOR_CONDITION:
        case FOR_INIT:
        case FOR_ITERATOR:
        case IMPLEMENTS_CLAUSE:
        case IMPORT:
        case INSTANCE_INIT:
        case LITERAL_ASSERT:
        case LITERAL_BREAK:
        case LITERAL_CASE:
        case LITERAL_CATCH:
        case LITERAL_CONTINUE:
        case LITERAL_FINALLY:
        case LITERAL_RETURN:
        case LITERAL_SYNCHRONIZED:
        case LITERAL_THROW:
        case LITERAL_THROWS:
        case PARAMETER_DEF:
        case STATIC_INIT:
        case STATIC_IMPORT:
        case TYPE_ARGUMENT:
        case TYPE_ARGUMENTS:
        case TYPE_LOWER_BOUNDS:
        case TYPE_PARAMETER:
        case TYPE_PARAMETERS:
        case TYPE_UPPER_BOUNDS:
            // All children must appear in the same line.
            this.checkChildren(
                ast,

                FORK2,

                LABEL1,
                ANY,
                FORK1,

                LABEL2,
                END
            );
            break;

        default:
            throw new AssertionError("Unknown token type '" + ast.getType() + "'");
        }
    }

    private static boolean
    isSingleLine(DetailAST ast) {
        return (
            WrapAndIndent.getLeftmostDescendant(ast).getLineNo()
            == WrapAndIndent.getRightmostDescendant(ast).getLineNo()
        );
    }

    /**
     * @param inline Iff {@code true}, then the entire expression must appear on one line.
     */
    private void
    checkExpression(DetailAST expression, boolean inline) {

        if (expression.getType() == QUESTION.delocalize()) {
            System.currentTimeMillis();
        }
        switch (LocalTokenType.localize(expression.getType())) {

        // Ternary operation
        case QUESTION:
            {
                DetailAST c = this.checkParenthesizedExpression(expression.getFirstChild(), inline);
                c = this.checkParenthesizedExpression(c, inline);
                assert c.getType() == COLON.delocalize();
                c = c.getNextSibling();
                c = this.checkParenthesizedExpression(c, inline);
                assert c == null;
            }
            break;

        case INDEX_OP:
            {
                DetailAST c = this.checkParenthesizedExpression(expression.getFirstChild(), inline);
                assert c != null;
                this.checkSameLine(WrapAndIndent.getRightmostDescendant(expression.getFirstChild()), expression);
                this.checkSameLine(expression, WrapAndIndent.getLeftmostDescendant(c));
                c = this.checkParenthesizedExpression(c, inline);
                assert c != null;
                assert c.getType() == RBRACK.delocalize();
                this.checkSameLine(expression, c);
            }
            break;

        // Binary operations
        case ASSIGN:
        case BAND:
        case BAND_ASSIGN:
        case BOR:
        case BOR_ASSIGN:
        case BSR:
        case BSR_ASSIGN:
        case BXOR:
        case BXOR_ASSIGN:
        case DIV:
        case DIV_ASSIGN:
        case DOT:
        case EQUAL:
        case GE:
        case GT:
        case LAND:
        case LITERAL_INSTANCEOF:
        case LOR:
        case LE:
        case LT:
        case MINUS:
        case MINUS_ASSIGN:
        case MOD:
        case MOD_ASSIGN:
        case NOT_EQUAL:
        case PLUS:
        case PLUS_ASSIGN:
        case SL:
        case SL_ASSIGN:
        case SR:
        case SR_ASSIGN:
        case STAR:
        case STAR_ASSIGN:
            {
                DetailAST c = this.checkParenthesizedExpression(expression.getFirstChild(), inline);
                if (c != null && c.getType() == TYPE_ARGUMENTS.delocalize()) {

                    // TYPE_ARGUMENTS checked by "visitToken()".
                    ;
                    c = c.getNextSibling();
                }
                assert c != null : (
                    this.getFileContents().getFilename()
                    + ":"
                    + expression.getLineNo()
                    + ": Second operand for '"
                    + LocalTokenType.localize(expression.getType())
                    + "' missing"
                );

                // Check wrapping and alignment of LHS and operator.
                {
                    DetailAST lhs = WrapAndIndent.getRightmostDescendant(c.getPreviousSibling());
                    switch (inline ? Control.NO_WRAP : this.wrapBeforeBinaryOperator) {

                    case NO_WRAP:
                        this.checkSameLine(lhs, expression);
                        break;

                    case MAY_WRAP:
                        if (lhs.getLineNo() != expression.getLineNo()) {
                            this.checkWrapped(
                                WrapAndIndent.getLeftmostDescendant(expression.getFirstChild()),
                                expression
                            );
                        } else {
                            this.checkSameLine(lhs, expression);
                        }
                        break;

                    case MUST_WRAP:
                        this.checkWrapped(lhs, WrapAndIndent.getLeftmostDescendant(expression.getFirstChild()));
                        if (lhs.getLineNo() == expression.getLineNo()) {
                            this.log(
                                expression,
                                WrapAndIndent.MESSAGE_KEY_MUST_WRAP,
                                lhs.getText(),
                                expression.getText()
                            );
                        } else {
                            this.checkWrapped(
                                WrapAndIndent.getLeftmostDescendant(expression.getFirstChild()),
                                expression
                            );
                        }
                        break;

                    default:
                        throw new IllegalStateException();
                    }
                }

                // Check wrapping and alignment of operator and RHS.
                {
                    DetailAST rhs = WrapAndIndent.getLeftmostDescendant(c);
                    switch (inline ? Control.NO_WRAP : this.wrapAfterBinaryOperator) {

                    case NO_WRAP:
                        this.checkSameLine(expression, rhs);
                        break;

                    case MAY_WRAP:
                        if (expression.getLineNo() != rhs.getLineNo()) {
                            this.checkWrapped(WrapAndIndent.getLeftmostDescendant(expression.getFirstChild()), rhs);
                        } else {
                            this.checkSameLine(expression, rhs);
                        }
                        break;

                    case MUST_WRAP:
                        if (expression.getLineNo() == rhs.getLineNo()) {
                            this.log(
                                rhs,
                                WrapAndIndent.MESSAGE_KEY_MUST_WRAP,
                                expression.getText(),
                                rhs.getText()
                            );
                        } else {
                            this.checkWrapped(WrapAndIndent.getLeftmostDescendant(expression.getFirstChild()), rhs);
                        }
                        break;

                    default:
                        throw new IllegalStateException();
                    }
                }

                c = this.checkParenthesizedExpression(c, inline);
                assert c == null : (
                    this.getFileContents().getFilename()
                    + ":"
                    + expression.getLineNo()
                    + ": Unexpected third operand "
                    + LocalTokenType.localize(c.getType())
                    + "/'"
                    + c.getText()
                    + "' for '"
                    + LocalTokenType.localize(expression.getType())
                    + "'"
                );
            }
            break;

        // Unary operations
        case BNOT:
        case DEC:
        case EXPR:
        case INC:
        case LNOT:
        case POST_DEC:
        case POST_INC:
        case UNARY_MINUS:
        case UNARY_PLUS:
            {
                DetailAST c = this.checkParenthesizedExpression(expression.getFirstChild(), inline);
                assert c == null;
            }
            break;

        case ARRAY_DECLARATOR:
            {
                DetailAST c = this.checkParenthesizedExpression(expression.getFirstChild(), inline);
                assert c.getType() == RBRACK.delocalize();
            }
            break;

        case CHAR_LITERAL:
        case IDENT:
        case LITERAL_CLASS:
        case LITERAL_FALSE:
        case LITERAL_NULL:
        case LITERAL_SUPER:
        case LITERAL_THIS:
        case LITERAL_TRUE:
        case NUM_DOUBLE:
        case NUM_FLOAT:
        case NUM_INT:
        case NUM_LONG:
        case STRING_LITERAL:
        case LITERAL_BOOLEAN:
        case LITERAL_BYTE:
        case LITERAL_SHORT:
        case LITERAL_INT:
        case LITERAL_LONG:
        case LITERAL_CHAR:
        case LITERAL_FLOAT:
        case LITERAL_DOUBLE:
        case LITERAL_VOID:
            {
                DetailAST c = expression.getFirstChild();
                assert c == null : Integer.toString(expression.getChildCount());
            }
            break;

        case TYPE:
            break;

        case METHOD_CALL:
            {
                DetailAST method = expression.getFirstChild(); // Everything up to and including the method name.
                this.checkExpression(method, inline);
                this.checkSameLine(method, expression);

                DetailAST arguments = method.getNextSibling();
                DetailAST rparen    = arguments.getNextSibling();

                assert rparen.getType() == RPAREN.delocalize();
                assert rparen.getNextSibling() == null;

                DetailAST firstArgument = arguments.getFirstChild();
                if (
                    firstArgument == null
                    || WrapAndIndent.getLeftmostDescendant(firstArgument).getLineNo() == expression.getLineNo()
                ) {
                    this.checkSameLine(WrapAndIndent.getRightmostDescendant(arguments), rparen);
                } else {
                    this.checkWrapped(WrapAndIndent.getLeftmostDescendant(expression), rparen);
                }
            }
            break;

        case LITERAL_NEW:
        case ARRAY_INIT:
        case TYPECAST:

            // Checked by "visitToken()".
            ;
            break;

        default:
            this.log(
                expression,
                "Uncheckable: " + LocalTokenType.localize(expression.getType()) + " / " + expression.toString()
            );
        }
    }

    private static DetailAST
    getLeftmostDescendant(DetailAST ast) {
        for (;;) {
            DetailAST tmp = ast.getFirstChild();
            if (tmp == null && ast.getType() == MODIFIERS.delocalize()) tmp = ast.getNextSibling();
            if (
                tmp == null
                || tmp.getLineNo() > ast.getLineNo()
                || (tmp.getLineNo() == ast.getLineNo() && tmp.getColumnNo() > ast.getColumnNo())
            ) return ast;
            ast = tmp;
        }
    }

    private static DetailAST
    getRightmostDescendant(DetailAST ast) {
        for (;;) {
            DetailAST tmp = ast.getLastChild();
            if (
                tmp == null
                || tmp.getLineNo() < ast.getLineNo()
                || (tmp.getLineNo() == ast.getLineNo() && tmp.getColumnNo() < ast.getColumnNo())
            ) return ast;
            ast = tmp;
        }
    }

    /**
     * @return The {@link DetailAST} <b>after</b> the parenthesized expression
     */
    private DetailAST
    checkParenthesizedExpression(DetailAST previous, boolean inline) {
        if (previous.getType() != LPAREN.delocalize()) {
            this.checkExpression(previous, inline);
            return previous.getNextSibling();
        }

        @SuppressWarnings("unused") AstDumper dumper = new AstDumper(previous); // For debugging

        DetailAST next = previous.getNextSibling();
        for (;;) {
            if (next.getType() != LPAREN.delocalize()) {
                break;
            }
            this.checkSameLine(previous, next);
            previous = next;
            next     = next.getNextSibling();
        }

        if (previous.getLineNo() == WrapAndIndent.getLeftmostDescendant(next).getLineNo()) {
            this.checkExpression(next, true);
            previous = next;
            next     = next.getNextSibling();
            this.checkSameLine(WrapAndIndent.getRightmostDescendant(previous), next);
        } else {
            this.checkIndented(previous, WrapAndIndent.getLeftmostDescendant(next));
            this.checkExpression(next, false);
            previous = next;
            next     = next.getNextSibling();
            this.checkUnindented(WrapAndIndent.getRightmostDescendant(previous), next);
        }

        previous = next;
        assert next.getType() == RPAREN.delocalize();
        return next.getNextSibling();
    }

    /**
     * Verifies that the children of the given {@code ast} are positioned as specified.
     *
     * @param args A squence of {@link LocalTokenType}s and {@link Control}s
     */
    private void
    checkChildren(DetailAST ast, Object... args) {

        @SuppressWarnings("unused") AstDumper astDumper = new AstDumper(ast); // For debugging

        DetailAST child = ast.getFirstChild();

        // Determine the "indentation parent".
        switch (LocalTokenType.localize(ast.getType())) {

        case ELIST:
            ast = ast.getParent();
            break;

        case SLIST:
            if (ast.getParent().getType() == CASE_GROUP.delocalize()) {
                ast = ast.getParent().getParent();
            }
            break;

        case PARAMETERS:
            ast = ast.getParent().findFirstToken(IDENT.delocalize());
            break;

        case DOT:
            ast = WrapAndIndent.getLeftmostDescendant(ast);
            break;

        default:
            ;
        }

        DetailAST previousAst = ast;
        int       mode        = 0; // SUPPRESS CHECKSTYLE UsageDistance
        for (int idx = 0;;) {
            Object tokenType = args[idx++];

            if (tokenType instanceof Control) {

                Control control = (Control) tokenType;
                switch (control) {

                case END:
                    if (child == null) return;
                    this.log(child, "Unexpected extra token ''{0}''", child.getText());
                    return;

                case OPTIONAL:
                    tokenType = args[idx++];
                    while (WrapAndIndent.SKIPPABLES.contains(tokenType)) tokenType = args[idx++];
                    if (
                        child != null
                        && (tokenType == ANY || tokenType == LocalTokenType.localize(child.getType()))
                    ) {
                        previousAst = child;
                        child       = child.getNextSibling();
                    }
                    break;

                case FORK1:
                case FORK2:
                case FORK3:
                case FORK4:
                case FORK5:
                case FORK6:
                case FORK7:
                case FORK8:
                case FORK9:
                    {
                        Control label = Control.values()[control.ordinal() - FORK1.ordinal() + LABEL1.ordinal()];

                        int destination = Arrays.asList(args).indexOf(label);
                        assert destination != -1 : tokenType + ": Label '" + label + "' undefined";

                        destination++;

                        // Decide whether to branch or to continue;
                        boolean doBranch;
                        DO_BRANCH:
                        for (int i = destination;; i++) {
                            Object da = args[i];
                            if (WrapAndIndent.SKIPPABLES.contains(da)) {
                                ;
                            } else
                            if (da == END) {
                                doBranch = child == null;
                                break DO_BRANCH;
                            } else
                            if (da instanceof LocalTokenType) {
                                doBranch = child != null && ((LocalTokenType) da).delocalize() == child.getType();
                                break DO_BRANCH;
                            } else
                            {
                                for (int j = idx;; j++) {
                                    Object na = args[j];
                                    if (WrapAndIndent.SKIPPABLES.contains(na)) {
                                        ;
                                    } else
                                    if (na == END) {
                                        doBranch = child != null;
                                        break DO_BRANCH;
                                    } else
                                    if (na instanceof LocalTokenType) {
                                        doBranch = child == null || ((LocalTokenType) na).delocalize() != child.getType(); // SUPPRESS CHECKSTYLE LineLength
                                        break DO_BRANCH;
                                    } else
                                    if (na == ANY) {
                                        assert da != ANY;
                                        doBranch = child == null;
                                        break DO_BRANCH;
                                    } else
                                    if (da == ANY) {
                                        doBranch = child != null;
                                        break DO_BRANCH;
                                    } else
                                    {
                                        assert false : na + " / " + da;
                                    }
                                }
                            }
                        }

                        if (doBranch) idx = destination;
                    }
                    break;

                case BRANCH1:
                case BRANCH2:
                case BRANCH3:
                case BRANCH4:
                case BRANCH5:
                case BRANCH6:
                case BRANCH7:
                case BRANCH8:
                case BRANCH9:
                    {
                        Control label = Control.values()[control.ordinal() - BRANCH1.ordinal() + LABEL1.ordinal()];

                        int destination = Arrays.asList(args).indexOf(label);
                        if (destination == -1) {
                            throw new AssertionError(tokenType + ": Label '" + label + "' undefined");
                        }

                        idx = destination + 1;
                    }
                    break;

                case LABEL1:
                case LABEL2:
                case LABEL3:
                case LABEL4:
                case LABEL5:
                case LABEL6:
                case LABEL7:
                case LABEL8:
                case LABEL9:
                    ;
                    break;

                case ANY:
                    if (child == null) {
                        this.log(
                            previousAst,
                            "Token missing after ''{0}''",
                            previousAst.getText()
                        );
                        return;
                    }

                    previousAst = WrapAndIndent.getRightmostDescendant(child);
                    child       = child.getNextSibling();
                    break;

                case INDENT_IF_CHILDREN:
                    assert child != null;
                    if (child.getFirstChild() == null) break;
                    /*FALLTHROUGH*/

                case MAY_INDENT:
                    assert child != null;
                    switch (mode) {

                    case 0:
                        {
                            DetailAST c = WrapAndIndent.getLeftmostDescendant(child);
                            if (c.getLineNo() == previousAst.getLineNo()) {
                                mode = 1;
                            } else {
                                mode = 2;
                                if (child.getType() == CASE_GROUP.delocalize()) {
                                    this.checkWrapped(ast, c);
                                } else {
                                    this.checkIndented(ast, c);
                                }
                            }
                        }
                        break;

                    case 1:
                        this.checkSameLine(previousAst, WrapAndIndent.getLeftmostDescendant(child));
                        break;

                    case 2:
                        {
                            DetailAST l = WrapAndIndent.getLeftmostDescendant(child);
                            if (l.getLineNo() == previousAst.getLineNo()) {
                                if (
                                    ast.getType() == ARRAY_INIT.delocalize()
                                    || ast.getType() == METHOD_CALL.delocalize()
                                    || ast.getParent().getType() == ENUM_DEF.delocalize()
                                ) {

                                    // Allow multiple children in the same line.
                                    ;
                                } else {
                                    this.log(
                                        l,
                                        WrapAndIndent.MESSAGE_KEY_MUST_WRAP,
                                        previousAst.getText(),
                                        l.getText()
                                    );
                                }
                            } else {
                                if (child.getType() == CASE_GROUP.delocalize()) {
                                    this.checkWrapped(ast, l);
                                } else {
                                    this.checkIndented(ast, l);
                                }
                            }
                        }
                        break;
                    }
                    break;

                case UNINDENT:
                    assert child != null;
                    switch (mode) {

                    case 0:
                        if (previousAst.getLineNo() != child.getLineNo()) {
                            this.checkWrapped(ast, child);
                        }
                        break;

                    case 1:
                        this.checkSameLine(previousAst, child);
                        break;

                    case 2:
                        this.checkWrapped(ast, child);
                        break;
                    }
                    mode = 0;
                    break;

                case MAY_WRAP:
                    assert child != null;
                    assert mode == 0;
                    if (child.getLineNo() != previousAst.getLineNo()) {
                        this.checkWrapped(previousAst, child);
                    }
                    break;

                case MUST_WRAP:
                    assert mode == 0;
                    if (previousAst.getType() == MODIFIERS.delocalize()) {
                        ;
                    } else
                    {
                        this.checkWrapped(previousAst, child);
                    }
                    break;

                case NO_WRAP:
                    this.checkSameLine(previousAst, WrapAndIndent.getLeftmostDescendant(child));
                    break;
                }
            } else
            if (tokenType instanceof LocalTokenType) {

                if (child == null) {
                    this.log(
                        previousAst,
                        "''{0}'' after ''{1}''",
                        tokenType,
                        previousAst.getText()
                    );
                    return;
                }

                if (child.getType() != ((LocalTokenType) tokenType).delocalize()) {
                    this.log(
                        child,
                        "Expected ''{0}'' instead of ''{1}''",
                        tokenType,
                        child.getText() + "'"
                    );
                    return;
                }

                assert child != null;
                previousAst = WrapAndIndent.getRightmostDescendant(child);
                child       = child.getNextSibling();
            } else
            {
                throw new AssertionError(tokenType);
            }
        }
    }
    private static final Set<Object> SKIPPABLES;
    static {
        Set<Object> ss = new HashSet<Object>();
        ss.addAll(Arrays.asList(
            MAY_INDENT, UNINDENT, INDENT_IF_CHILDREN,
            MAY_WRAP,  MUST_WRAP, NO_WRAP,
            LABEL1, LABEL2, LABEL3, LABEL4, LABEL5, LABEL6, LABEL7, LABEL8, LABEL9
        ));
        SKIPPABLES = Collections.unmodifiableSet(ss);
    }

    /**
     * Checks that the line where {@code next} occurs is indented by {@link #DEFAULT_INDENTATION}, compared to the line
     * where {@code previous} occurs.
     */
    private void
    checkIndented(DetailAST previous, DetailAST next) {
        if (next.getLineNo() == previous.getLineNo()) {
            this.log(next, WrapAndIndent.MESSAGE_KEY_MUST_WRAP, previous.getText(), next.getText());
        } else {
            this.checkAlignment(next, this.calculateIndentation(previous) + this.basicOffset);
        }
    }

    /**
     * Checks that the line where {@code next} occurs is unindented by {@link #DEFAULT_INDENTATION}, compared to the
     * line where {@code previous} occurs.
     */
    private void
    checkUnindented(DetailAST previous, DetailAST next) {
        if (next.getLineNo() == previous.getLineNo()) {
            this.log(next, WrapAndIndent.MESSAGE_KEY_MUST_WRAP, previous.getText(), next.getText());
        } else {
            this.checkAlignment(next, this.calculateIndentation(previous) - this.basicOffset);
        }
    }

    /**
     * Checks that the line where {@code next} occurs is indented exactly as the line where {@code previous} occurs.
     */
    private void
    checkWrapped(DetailAST previous, DetailAST next) {
        if (next.getLineNo() == previous.getLineNo()) {
            this.log(next, WrapAndIndent.MESSAGE_KEY_MUST_WRAP, previous.getText(), next.getText());
        } else {
            this.checkAlignment(next, this.calculateIndentation(previous));
        }
    }

    /**
     * Checks that {@code left} and {@code right} appear in the same line.
     */
    private void
    checkSameLine(DetailAST left, DetailAST right) {
        if (left.getLineNo() != right.getLineNo()) {
            this.log(
                right,
                WrapAndIndent.MESSAGE_KEY_MUST_JOIN,
                right.getText(),
                left.getText()
            );
        }
    }

    /**
     * Logs a problem iff the given {@code ast} is not vertically positioned at the given {@code targetColumnNo}.
     *
     * @param targetColumnNo Counting from zero
     */
    private void
    checkAlignment(DetailAST ast, int targetColumnNo) {
        int actualColumnNo = Utils.lengthExpandedTabs(
            this.getLines()[ast.getLineNo() - 1],
            ast.getColumnNo(),
            this.getTabWidth()
        );
        if (actualColumnNo != targetColumnNo) {
            this.log(
                ast,
                WrapAndIndent.MESSAGE_KEY_WRONG_COLUMN,
                ast.getText(),
                targetColumnNo + 1,
                actualColumnNo + 1
            );
        }
    }

    /**
     * Calculate the indentation of the line of the given {@code ast}, honoring TAB characters. Notice that the
     * {@code ast} need not be the FIRST element in that line.
     */
    private int
    calculateIndentation(DetailAST ast) {
        String line = this.getLines()[ast.getLineNo() - 1];

        int result = 0;
        for (int i = 0; i < line.length(); ++i) {
            switch (line.charAt(i)) {

            case ' ':
                ++result;
                break;

            case '\t':
                {
                    int tabWidth = this.getTabWidth();
                    result += tabWidth - (result % tabWidth);
                }
                break;

            default:
                return result;
            }
        }
        return 0;
    }
}


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

import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.ANY;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.BRANCH1;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.BRANCH2;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.BRANCH3;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.BRANCH4;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.BRANCH5;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.BRANCH9;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.END;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.FORK1;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.FORK2;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.FORK3;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.FORK4;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.FORK5;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.FORK6;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.FORK7;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.FORK8;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.INDENT_IF_CHILDREN;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.LABEL1;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.LABEL2;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.LABEL3;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.LABEL4;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.LABEL5;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.LABEL6;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.LABEL7;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.LABEL8;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.LABEL9;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.MAY_INDENT;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.MAY_WRAP;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.MUST_WRAP;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.NO_WRAP;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.OPTIONAL;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.UNINDENT;

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
 * Statements must be uniformly wrapped and indented.
 */
@NotNullByDefault(false) public
class WrapAndIndent extends Check {

    /**
     * Message keys as it appears in 'src/de/unkrig/cscontrib/checks/checkstyle-metadata.xml'.
     * <dl>
     *   <dt><code>{0}</code>
     *   <dd>Text of token <i>before</i> the (missing) line break
     *   <dt><code>{1}</code>
     *   <dd>Text of token <i>after</i> the (missing) line break
     * </dl>
     */
    public static final String // SUPPRESS CHECKSTYLE ConstantName
    MESSAGE_KEY__MUST_WRAP_LINE_BEFORE_1 = "Must wrap line before ''{1}''";

    /**
     * Message keys as it appears in 'src/de/unkrig/cscontrib/checks/checkstyle-metadata.xml'.
     * <dl>
     *   <dt><code>{0}</code>
     *   <dd>Text of token <i>before</i> the (unwanted) line break
     *   <dt><code>{1}</code>
     *   <dd>Text of token <i>after</i> the (unwanted) line break
     * </dl>
     */
    public static final String // SUPPRESS CHECKSTYLE ConstantName
    MESSAGE_KEY__0_MUST_APPEAR_ON_SAME_LINE_AS_1 = "''{0}'' must appear on same line as ''{1}''";

    /**
     * Message keys as it appears in 'src/de/unkrig/cscontrib/checks/checkstyle-metadata.xml'.
     * <dl>
     *   <dt><code>{0}</code>
     *   <dd>Text the vertically misaligned token
     *   <dt><code>{1}</code>
     *   <dd>Current (wrong) column number of the token
     *   <dt><code>{2}</code>
     *   <dd>Correct column number of the token
     * </dl>
     */
    public static final String // SUPPRESS CHECKSTYLE ConstantName
    MESSAGE_KEY__0_MUST_APPEAR_IN_COLUMN_1_NOT_2 = "''{0}'' must appear in column {1}, not {2}";

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

    // CONFIGURATION VARIABLES
    private int     basicOffset                      = 4;
    private boolean allowOneLineClassDecl            = true;
    private boolean allowOneLineInterfaceDecl        = true;
    private boolean allowOneLineEnumDecl             = true;
    private boolean allowOneLineAnnoDecl             = true;
    private boolean allowOneLineCtorDecl             = true;
    private boolean allowOneLineMethDecl             = true;
    private boolean allowOneLineSwitchBlockStmtGroup = true;
    private Control wrapPackageDeclBeforePackage     = MUST_WRAP;
    private Control wrapClassDeclBeforeClass         = MUST_WRAP;
    private Control wrapInterfaceDeclBeforeInterface = MUST_WRAP;
    private Control wrapEnumDeclBeforeEnum           = MUST_WRAP;
    private Control wrapAnnoDeclBeforeAt             = MUST_WRAP;
    private Control wrapFieldDeclBeforeName          = MAY_WRAP;
    private Control wrapCtorDeclBeforeName           = MUST_WRAP;
    private Control wrapMethDeclBeforeName           = MUST_WRAP;
    private Control wrapLocVarDeclBeforeName         = MAY_WRAP;
    private Control wrapTypeDeclBeforeLCurly         = NO_WRAP;
    private Control wrapCtorDeclBeforeLCurly         = NO_WRAP;
    private Control wrapMethodDeclBeforeLCurly       = NO_WRAP;
    private Control wrapDoBeforeLCurly               = NO_WRAP;
    private Control wrapTryBeforeCatch               = MAY_WRAP;
    private Control wrapTryBeforeFinally             = MAY_WRAP;
    private Control wrapArrayInitBeforeLCurly        = NO_WRAP;
    private Control wrapAnonClassDeclBeforeLCurly    = NO_WRAP;
    private Control wrapBeforeBinaryOperator         = MAY_WRAP;
    private Control wrapAfterBinaryOperator          = NO_WRAP;

    // CONFIGURATION SETTERS
    // CHECKSTYLE JavadocMethod:OFF
    // CHECKSTYLE LineLength:OFF
    public void setBasicOffset(int value)                          { this.basicOffset = value; }

    public void setAllowOneLineClassDecl(boolean value)            { this.allowOneLineClassDecl            = value; }
    public void setAllowOneLineInterfaceDecl(boolean value)        { this.allowOneLineInterfaceDecl        = value; }
    public void setAllowOneLineEnumDecl(boolean value)             { this.allowOneLineEnumDecl             = value; }
    public void setAllowOneLineAnnoDecl(boolean value)             { this.allowOneLineAnnoDecl             = value; }
    public void setAllowOneLineCtorDecl(boolean value)             { this.allowOneLineCtorDecl             = value; }
    public void setAllowOneLineMethDecl(boolean value)             { this.allowOneLineMethDecl             = value; }
    public void setAllowOneLineSwitchBlockStmtGroup(boolean value) { this.allowOneLineSwitchBlockStmtGroup = value; }

    public void setWrapClassDeclBeforeClass(String value)          { this.wrapClassDeclBeforeClass         = WrapAndIndent.toWrap(value); }
    public void setWrapPackageDeclBeforePackage(String value)      { this.wrapPackageDeclBeforePackage     = WrapAndIndent.toWrap(value); }
    public void setWrapInterfaceDeclBeforeInterface(String value)  { this.wrapInterfaceDeclBeforeInterface = WrapAndIndent.toWrap(value); }
    public void setWrapEnumDeclBeforeEnum(String value)            { this.wrapEnumDeclBeforeEnum           = WrapAndIndent.toWrap(value); }
    public void setWrapAnnoDeclBeforeAt(String value)              { this.wrapAnnoDeclBeforeAt             = WrapAndIndent.toWrap(value); }
    public void setWrapFieldDeclBeforeName(String value)           { this.wrapFieldDeclBeforeName          = WrapAndIndent.toWrap(value); }
    public void setWrapCtorDeclBeforeName(String value)            { this.wrapCtorDeclBeforeName           = WrapAndIndent.toWrap(value); }
    public void setWrapMethDeclBeforeName(String value)            { this.wrapMethDeclBeforeName           = WrapAndIndent.toWrap(value); }
    public void setWrapLocVarDeclBeforeName(String value)          { this.wrapLocVarDeclBeforeName         = WrapAndIndent.toWrap(value); }

    public void setWrapTypeDeclBeforeLCurly(String value)          { this.wrapTypeDeclBeforeLCurly         = WrapAndIndent.toWrap(value); }
    public void setWrapCtorDeclBeforeLCurly(String value)          { this.wrapCtorDeclBeforeLCurly         = WrapAndIndent.toWrap(value); }
    public void setWrapMethodDeclBeforeLCurly(String value)        { this.wrapMethodDeclBeforeLCurly       = WrapAndIndent.toWrap(value); }
    public void setWrapDoBeforeLCurly(String value)                { this.wrapDoBeforeLCurly               = WrapAndIndent.toWrap(value); }
    public void setWrapTryBeforeCatch(String value)                { this.wrapTryBeforeCatch               = WrapAndIndent.toWrap(value); }
    public void setWrapTryBeforeFinally(String value)              { this.wrapTryBeforeFinally             = WrapAndIndent.toWrap(value); }
    public void setWrapArrayInitBeforeLCurly(String value)         { this.wrapArrayInitBeforeLCurly        = WrapAndIndent.toWrap(value); }
    public void setWrapAnonClassDeclBeforeLCurly(String value)     { this.wrapAnonClassDeclBeforeLCurly    = WrapAndIndent.toWrap(value); }
    public void setWrapBeforeBinaryOperator(String value)          { this.wrapBeforeBinaryOperator         = WrapAndIndent.toWrap(value); }
    public void setWrapAfterBinaryOperator(String value)           { this.wrapAfterBinaryOperator          = WrapAndIndent.toWrap(value); }
    // CHECKSTYLE LineLength:ON
    // CHECKSTYLE MethodCheck:ON
    // END CONFIGURATION SETTERS

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
//            LocalTokenType.ABSTRACT,
            LocalTokenType.ANNOTATION,
            LocalTokenType.ANNOTATIONS,
            LocalTokenType.ANNOTATION_ARRAY_INIT,
            LocalTokenType.ANNOTATION_DEF,
            LocalTokenType.ANNOTATION_FIELD_DEF,
            LocalTokenType.ANNOTATION_MEMBER_VALUE_PAIR,
            LocalTokenType.ARRAY_DECLARATOR,
            LocalTokenType.ARRAY_INIT,
            LocalTokenType.ASSIGN,  // To check 'int[] ia = { 1, 2, 3 };'.
//            LocalTokenType.AT,
//            LocalTokenType.BAND,
//            LocalTokenType.BAND_ASSIGN,
//            LocalTokenType.BNOT,
//            LocalTokenType.BOR,
//            LocalTokenType.BOR_ASSIGN,
//            LocalTokenType.BSR,
//            LocalTokenType.BSR_ASSIGN,
//            LocalTokenType.BXOR,
//            LocalTokenType.BXOR_ASSIGN,
            LocalTokenType.CASE_GROUP,
//            LocalTokenType.CHAR_LITERAL,
            LocalTokenType.CLASS_DEF,
//            LocalTokenType.COLON,
//            LocalTokenType.COMMA,
            LocalTokenType.CTOR_CALL,
            LocalTokenType.CTOR_DEF,
//            LocalTokenType.DEC,
//            LocalTokenType.DIV,
//            LocalTokenType.DIV_ASSIGN,
            LocalTokenType.DOT,
//            LocalTokenType.DO_WHILE,
            LocalTokenType.ELIST,
//            LocalTokenType.ELLIPSIS,
//            LocalTokenType.EMPTY_STAT,
//            LocalTokenType.ENUM,
            LocalTokenType.ENUM_CONSTANT_DEF,
            LocalTokenType.ENUM_DEF,
//            LocalTokenType.EOF,
//            LocalTokenType.EQUAL,
            LocalTokenType.EXPR,
            LocalTokenType.EXTENDS_CLAUSE,
//            LocalTokenType.FINAL,
            LocalTokenType.FOR_CONDITION,
            LocalTokenType.FOR_EACH_CLAUSE,
            LocalTokenType.FOR_INIT,
            LocalTokenType.FOR_ITERATOR,
//            LocalTokenType.GE,
//            LocalTokenType.GENERIC_END,
//            LocalTokenType.GENERIC_START,
//            LocalTokenType.GT,
//            LocalTokenType.IDENT,
            LocalTokenType.IMPLEMENTS_CLAUSE,
            LocalTokenType.IMPORT,
//            LocalTokenType.INC,
//            LocalTokenType.INDEX_OP,
            LocalTokenType.INSTANCE_INIT,
            LocalTokenType.INTERFACE_DEF,
            LocalTokenType.LABELED_STAT,
//            LocalTokenType.LAND,
//            LocalTokenType.LCURLY,
//            LocalTokenType.LE,
            LocalTokenType.LITERAL_ASSERT,
//            LocalTokenType.LITERAL_BOOLEAN,
            LocalTokenType.LITERAL_BREAK,
//            LocalTokenType.LITERAL_BYTE,
            LocalTokenType.LITERAL_CASE,
            LocalTokenType.LITERAL_CATCH,
//            LocalTokenType.LITERAL_CHAR,
//            LocalTokenType.LITERAL_CLASS,
            LocalTokenType.LITERAL_CONTINUE,
//            LocalTokenType.LITERAL_DEFAULT,
            LocalTokenType.LITERAL_DO,
//            LocalTokenType.LITERAL_DOUBLE,
//            LocalTokenType.LITERAL_ELSE,
//            LocalTokenType.LITERAL_FALSE,
            LocalTokenType.LITERAL_FINALLY,
//            LocalTokenType.LITERAL_FLOAT,
            LocalTokenType.LITERAL_FOR,
            LocalTokenType.LITERAL_IF,
//            LocalTokenType.LITERAL_INSTANCEOF,
//            LocalTokenType.LITERAL_INT,
//            LocalTokenType.LITERAL_INTERFACE,
//            LocalTokenType.LITERAL_LONG,
//            LocalTokenType.LITERAL_NATIVE,
            LocalTokenType.LITERAL_NEW,
//            LocalTokenType.LITERAL_NULL,
//            LocalTokenType.LITERAL_PRIVATE,
//            LocalTokenType.LITERAL_PROTECTED,
//            LocalTokenType.LITERAL_PUBLIC,
            LocalTokenType.LITERAL_RETURN,
//            LocalTokenType.LITERAL_SHORT,
//            LocalTokenType.LITERAL_STATIC,
//            LocalTokenType.LITERAL_SUPER,
            LocalTokenType.LITERAL_SWITCH,
            LocalTokenType.LITERAL_SYNCHRONIZED,
//            LocalTokenType.LITERAL_THIS,
            LocalTokenType.LITERAL_THROW,
            LocalTokenType.LITERAL_THROWS,
//            LocalTokenType.LITERAL_TRANSIENT,
//            LocalTokenType.LITERAL_TRUE,
            LocalTokenType.LITERAL_TRY,
//            LocalTokenType.LITERAL_VOID,
//            LocalTokenType.LITERAL_VOLATILE,
            LocalTokenType.LITERAL_WHILE,
//            LocalTokenType.LNOT,
//            LocalTokenType.LOR,
//            LocalTokenType.LPAREN,
//            LocalTokenType.LT,
//            LocalTokenType.METHOD_CALL,
            LocalTokenType.METHOD_DEF,
//            LocalTokenType.MINUS,
//            LocalTokenType.MINUS_ASSIGN,
//            LocalTokenType.MOD,
            LocalTokenType.MODIFIERS,
//            LocalTokenType.MOD_ASSIGN,
//            LocalTokenType.NOT_EQUAL,
//            LocalTokenType.NUM_DOUBLE,
//            LocalTokenType.NUM_FLOAT,
//            LocalTokenType.NUM_INT,
//            LocalTokenType.NUM_LONG,
            LocalTokenType.OBJBLOCK,
            LocalTokenType.PACKAGE_DEF,
            LocalTokenType.PARAMETERS,
            LocalTokenType.PARAMETER_DEF,
//            LocalTokenType.PLUS,
//            LocalTokenType.PLUS_ASSIGN,
//            LocalTokenType.POST_DEC,
//            LocalTokenType.POST_INC,
//            LocalTokenType.QUESTION,
//            LocalTokenType.RBRACK,
//            LocalTokenType.RCURLY,
//            LocalTokenType.RPAREN,
//            LocalTokenType.SEMI,
//            LocalTokenType.SL,
            LocalTokenType.SLIST,
//            LocalTokenType.SL_ASSIGN,
//            LocalTokenType.SR,
//            LocalTokenType.SR_ASSIGN,
//            LocalTokenType.STAR,
//            LocalTokenType.STAR_ASSIGN,
            LocalTokenType.STATIC_IMPORT,
            LocalTokenType.STATIC_INIT,
//            LocalTokenType.STRICTFP,
//            LocalTokenType.STRING_LITERAL,
            LocalTokenType.SUPER_CTOR_CALL,
//            LocalTokenType.TYPE,
//            LocalTokenType.TYPECAST,
            LocalTokenType.TYPE_ARGUMENT,
            LocalTokenType.TYPE_ARGUMENTS,
//            LocalTokenType.TYPE_EXTENSION_AND,
            LocalTokenType.TYPE_LOWER_BOUNDS,
            LocalTokenType.TYPE_PARAMETER,
            LocalTokenType.TYPE_PARAMETERS,
            LocalTokenType.TYPE_UPPER_BOUNDS,
//            LocalTokenType.UNARY_MINUS,
//            LocalTokenType.UNARY_PLUS,
            LocalTokenType.VARIABLE_DEF,
//            LocalTokenType.WILDCARD_TYPE,
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

                LocalTokenType.AT,
                FORK1,
                LocalTokenType.DOT,
                BRANCH2,

                LABEL1,
                LocalTokenType.IDENT,

                LABEL2,
                FORK3,
                END,

                LABEL3,
                LocalTokenType.LPAREN,
                BRANCH5,

                LABEL4,
                LocalTokenType.COMMA,

                LABEL5,
                FORK6,
                MAY_INDENT,
                LocalTokenType.ANNOTATION_MEMBER_VALUE_PAIR,
                BRANCH9,

                LABEL6,
                FORK7,
                MAY_INDENT,
                LocalTokenType.ANNOTATION,
                BRANCH9,

                LABEL7,
                FORK8,
                MAY_INDENT,
                LocalTokenType.EXPR,
                BRANCH9,

                LABEL8,
                MAY_INDENT,
                LocalTokenType.ANNOTATION_ARRAY_INIT,

                LABEL9,
                FORK4,
                UNINDENT,
                LocalTokenType.RPAREN,
                END
            );
            break;

        case ANNOTATION_ARRAY_INIT:
            this.checkChildren(
                ast,

                FORK2,

                LABEL1,
                MAY_INDENT,
                LocalTokenType.EXPR,
                FORK2,
                LocalTokenType.COMMA,
                FORK1,

                LABEL2,
                UNINDENT,
                LocalTokenType.RCURLY,
                END
            );
            break;

        case ANNOTATION_DEF:
            if (this.allowOneLineAnnoDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                LocalTokenType.MODIFIERS,
                this.wrapAnnoDeclBeforeAt,
                LocalTokenType.AT,
                LocalTokenType.LITERAL_INTERFACE,
                LocalTokenType.IDENT,
                this.wrapTypeDeclBeforeLCurly,
                LocalTokenType.OBJBLOCK,
                END
            );
            break;

        case ARRAY_INIT:
            this.checkChildren(
                ast,

                FORK2,

                LABEL1,
                MAY_INDENT,
                ANY,
                FORK2,
                LocalTokenType.COMMA,
                FORK1,

                LABEL2,
                UNINDENT,
                LocalTokenType.RCURLY,
                END
            );
            break;

        case CASE_GROUP:
            this.checkChildren(
                ast,

                FORK3,
                LocalTokenType.LITERAL_CASE,        // case 1: case 2:

                LABEL1,
                FORK2,
                MAY_WRAP,
                LocalTokenType.LITERAL_CASE,
                BRANCH1,

                LABEL2,
                FORK4,

                LABEL3,
                MAY_WRAP,
                LocalTokenType.LITERAL_DEFAULT,

                LABEL4,
                INDENT_IF_CHILDREN,
                LocalTokenType.SLIST,
                END
            );
            break;

        case CLASS_DEF:
            if (this.allowOneLineClassDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                LocalTokenType.MODIFIERS,
                this.wrapClassDeclBeforeClass,
                LocalTokenType.LITERAL_CLASS,
                LocalTokenType.IDENT,

                FORK1,
                LocalTokenType.TYPE_PARAMETERS,

                LABEL1,
                FORK2,
                MAY_WRAP,
                LocalTokenType.EXTENDS_CLAUSE,

                LABEL2,
                FORK3,
                MAY_WRAP,
                LocalTokenType.IMPLEMENTS_CLAUSE,

                LABEL3,
                this.wrapTypeDeclBeforeLCurly,
                LocalTokenType.OBJBLOCK,
                END
            );
            break;

        case CTOR_CALL:
            this.checkChildren(
                ast,

                LocalTokenType.LPAREN,
                INDENT_IF_CHILDREN,
                LocalTokenType.ELIST,
                UNINDENT,
                LocalTokenType.RPAREN,
                LocalTokenType.SEMI,
                END
            );
            break;

        case CTOR_DEF:
            if (this.allowOneLineCtorDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                LocalTokenType.MODIFIERS,

                FORK1,
                LocalTokenType.TYPE_PARAMETERS,

                LABEL1,
                this.wrapCtorDeclBeforeName,
                LocalTokenType.IDENT,

                LocalTokenType.LPAREN,
                INDENT_IF_CHILDREN,
                LocalTokenType.PARAMETERS,
                UNINDENT,
                LocalTokenType.RPAREN,
                FORK2,
                MAY_WRAP,
                LocalTokenType.LITERAL_THROWS,

                LABEL2,
                this.wrapCtorDeclBeforeLCurly,
                LocalTokenType.SLIST,
                END
            );
            break;

        case ELIST:
            this.checkChildren(
                ast,

                FORK2,

                LABEL1,
                MAY_INDENT,
                LocalTokenType.EXPR,
                FORK2,
                LocalTokenType.COMMA,
                BRANCH1,

                LABEL2,
                END
            );
            break;

        case ENUM_DEF:
            if (this.allowOneLineEnumDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                LocalTokenType.MODIFIERS,
                this.wrapEnumDeclBeforeEnum,
                LocalTokenType.ENUM,
                LocalTokenType.IDENT,
                this.wrapTypeDeclBeforeLCurly,
                LocalTokenType.OBJBLOCK,
                END
            );
            break;

        case EXPR:
            {
                DetailAST child = ast.getFirstChild();
                if (child.getType() == LocalTokenType.LPAREN.delocalize()) {
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

                LocalTokenType.VARIABLE_DEF,
                MAY_WRAP,
                LocalTokenType.COLON,
                LocalTokenType.EXPR,
                END
            );
            break;

        case INTERFACE_DEF:
            if (this.allowOneLineInterfaceDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                LocalTokenType.MODIFIERS,
                this.wrapInterfaceDeclBeforeInterface,
                LocalTokenType.LITERAL_INTERFACE,
                LocalTokenType.IDENT,
                FORK1,
                LocalTokenType.TYPE_PARAMETERS,

                LABEL1,
                FORK2,
                MAY_WRAP,
                LocalTokenType.EXTENDS_CLAUSE,

                LABEL2,
                this.wrapTypeDeclBeforeLCurly,
                LocalTokenType.OBJBLOCK,
                END
            );
            break;

        case LABELED_STAT:
            this.checkChildren(
                ast,

                LocalTokenType.IDENT,
                MAY_WRAP,
                ANY,
                END
            );
            break;

        case LITERAL_DO:
            this.checkChildren(
                ast,

                this.wrapDoBeforeLCurly,
                LocalTokenType.SLIST,
                LocalTokenType.DO_WHILE,
                LocalTokenType.LPAREN,
                MAY_INDENT,
                LocalTokenType.EXPR,
                UNINDENT,
                LocalTokenType.RPAREN,
                LocalTokenType.SEMI,
                END
            );
            break;

        case LITERAL_FOR:
            this.checkChildren(
                ast,

                LocalTokenType.LPAREN,
                FORK1,
                MAY_INDENT,
                LocalTokenType.FOR_INIT,
                LocalTokenType.SEMI,
                MAY_INDENT,
                LocalTokenType.FOR_CONDITION,
                LocalTokenType.SEMI,
                INDENT_IF_CHILDREN,
                LocalTokenType.FOR_ITERATOR,
                FORK2,

                LABEL1,
                MAY_INDENT,
                LocalTokenType.FOR_EACH_CLAUSE,

                LABEL2,
                UNINDENT,
                LocalTokenType.RPAREN,

                FORK3,
                LocalTokenType.EXPR,
                LocalTokenType.SEMI,
                END,

                LABEL3,
                ANY,
                END
            );
            break;

        case LITERAL_IF:
            this.checkChildren(
                ast,

                LocalTokenType.LPAREN,
                MAY_INDENT,
                LocalTokenType.EXPR,
                UNINDENT,
                LocalTokenType.RPAREN,
                FORK1,
                LocalTokenType.EXPR,
                LocalTokenType.SEMI,
                END,

                LABEL1,
                ANY,
                FORK2,
                LocalTokenType.LITERAL_ELSE,

                LABEL2,
                END
            );
            break;

        case LITERAL_NEW:
            this.checkChildren(
                ast,

                ANY,
                FORK1,
                LocalTokenType.TYPE_ARGUMENTS,

                LABEL1,
                FORK3,
                LocalTokenType.ARRAY_DECLARATOR,
                FORK2,
                this.wrapArrayInitBeforeLCurly,
                LocalTokenType.ARRAY_INIT,

                LABEL2,
                END,

                LABEL3,
                LocalTokenType.LPAREN,
                INDENT_IF_CHILDREN,
                LocalTokenType.ELIST,
                UNINDENT,
                LocalTokenType.RPAREN,
                OPTIONAL,
                this.wrapAnonClassDeclBeforeLCurly,
                LocalTokenType.OBJBLOCK,
                END
            );
            break;

        case LITERAL_SWITCH:
            this.checkChildren(
                ast,

                LocalTokenType.LPAREN,
                MAY_INDENT,
                LocalTokenType.EXPR,
                UNINDENT,
                LocalTokenType.RPAREN,
                LocalTokenType.LCURLY,
                FORK2,

                LABEL1,
                MAY_INDENT,
                LocalTokenType.CASE_GROUP,
                FORK1,

                LABEL2,
                UNINDENT,
                LocalTokenType.RCURLY,
                END
            );
            break;

        case METHOD_DEF:
            if (this.allowOneLineMethDecl && WrapAndIndent.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                LocalTokenType.MODIFIERS,
                FORK1,
                LocalTokenType.TYPE_PARAMETERS,

                LABEL1,
                LocalTokenType.TYPE,
                this.wrapMethDeclBeforeName,
                LocalTokenType.IDENT,
                LocalTokenType.LPAREN,
                INDENT_IF_CHILDREN,
                LocalTokenType.PARAMETERS,
                UNINDENT,
                LocalTokenType.RPAREN,
                FORK2,
                MAY_WRAP,
                LocalTokenType.LITERAL_THROWS,

                LABEL2,
                FORK3,
                this.wrapMethodDeclBeforeLCurly,
                LocalTokenType.SLIST,
                END,

                LABEL3,
                LocalTokenType.SEMI,
                END
            );
            break;

        case LITERAL_WHILE:
            this.checkChildren(
                ast,

                LocalTokenType.LPAREN,
                MAY_INDENT,
                LocalTokenType.EXPR,
                UNINDENT,
                LocalTokenType.RPAREN,
                FORK1,

                LocalTokenType.EXPR,
                LocalTokenType.SEMI,
                END,

                LABEL1,
                ANY,
                END
            );
            break;

        case MODIFIERS:
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

        case OBJBLOCK:
            this.checkChildren(
                ast,

                LocalTokenType.LCURLY,
                FORK3,

                LABEL1,//2
                MAY_INDENT,
                LocalTokenType.ENUM_CONSTANT_DEF,
                FORK2,
                LocalTokenType.COMMA,
                FORK1,

                LABEL2,//8
                FORK3,
                MAY_INDENT,
                LocalTokenType.SEMI,

                LABEL3,//12
                FORK5,
                MAY_INDENT,
                LocalTokenType.VARIABLE_DEF,

                LABEL4,//16
                FORK3,
                LocalTokenType.COMMA,                  // int a = 3, b = 7;
                LocalTokenType.VARIABLE_DEF,
                BRANCH4,

                LABEL5,//21
                FORK6,
                UNINDENT,
                LocalTokenType.RCURLY,
                END,

                LABEL6,//26
                MAY_INDENT,
                ANY,
                BRANCH2//29
            );
            break;

        case PARAMETERS:
            this.checkChildren(
                ast,

                FORK2,

                LABEL1,
                MAY_INDENT,
                LocalTokenType.PARAMETER_DEF,
                FORK2,
                LocalTokenType.COMMA,
                BRANCH1,

                LABEL2,
                END
            );
            break;

        case SLIST:
            // Single-line case group?
            if (
                ast.getParent().getType() == LocalTokenType.CASE_GROUP.delocalize()
                && this.allowOneLineSwitchBlockStmtGroup
                && WrapAndIndent.isSingleLine(ast)
                && ast.getParent().getLineNo() == ast.getLineNo()
            ) return;

            this.checkChildren(
                ast,

                LABEL1,
                FORK2,
                MAY_INDENT,
                LocalTokenType.EXPR,
                LocalTokenType.SEMI,
                BRANCH1,

                LABEL2,
                FORK5,
                MAY_INDENT,
                LocalTokenType.VARIABLE_DEF,

                LABEL3,
                FORK4,
                LocalTokenType.COMMA,
                LocalTokenType.VARIABLE_DEF,
                BRANCH3,

                LABEL4,
                LocalTokenType.SEMI,
                BRANCH1,

                // SLIST in CASE_GROUP ends _without_ an RCURLY!
                LABEL5,
                FORK6,
                END,

                LABEL6,
                FORK7,
                UNINDENT,
                LocalTokenType.RCURLY,
                END,

                LABEL7,
                MAY_INDENT,
                ANY,
                BRANCH1,
                0
            );
            break;

        case SUPER_CTOR_CALL:
            this.checkChildren(
                ast,

                FORK1,
                ANY,
                LocalTokenType.DOT,

                LABEL1,
                LocalTokenType.LPAREN,
                INDENT_IF_CHILDREN,
                LocalTokenType.ELIST,
                UNINDENT,
                LocalTokenType.RPAREN,
                LocalTokenType.SEMI,
                END
            );
            break;

        case VARIABLE_DEF:
            this.checkChildren(
                ast,

                LocalTokenType.MODIFIERS,
                LocalTokenType.TYPE,
                (
                    ast.getParent().getType() == LocalTokenType.OBJBLOCK.delocalize()
                    ? this.wrapFieldDeclBeforeName
                    : this.wrapLocVarDeclBeforeName
                ),
                LocalTokenType.IDENT,
                FORK1,
                LocalTokenType.ASSIGN,

                LABEL1,
                FORK2,
                LocalTokenType.SEMI, // Field declarations DO have a SEMI, local variable declarations DON'T!?

                LABEL2,
                END
            );
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

                    FORK1,
                    this.wrapArrayInitBeforeLCurly,
                    LocalTokenType.ARRAY_INIT,
                    END,

                    LABEL1,
                    ANY,
                    END
                );
            }
            break;

        case LITERAL_TRY:
            this.checkChildren(
                ast,

                LocalTokenType.SLIST,
                FORK2,

                LABEL1,
                this.wrapTryBeforeCatch,
                LocalTokenType.LITERAL_CATCH,
                FORK1,
                FORK3,

                LABEL2,
                this.wrapTryBeforeFinally,
                LocalTokenType.LITERAL_FINALLY,

                LABEL3,
                END
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
                    if (aType == LocalTokenType.PACKAGE_DEF || aType == LocalTokenType.IMPORT) break PACKAGE_OR_IMPORT;
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

        if (expression.getType() == LocalTokenType.QUESTION.delocalize()) {
            System.currentTimeMillis();
        }
        switch (LocalTokenType.localize(expression.getType())) {

        // Ternary operation
        case QUESTION:
            {
                DetailAST c = this.checkParenthesizedExpression(expression.getFirstChild(), inline);
                c = this.checkParenthesizedExpression(c, inline);
                assert c.getType() == LocalTokenType.COLON.delocalize();
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
                assert c.getType() == LocalTokenType.RBRACK.delocalize();
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
                if (c != null && c.getType() == LocalTokenType.TYPE_ARGUMENTS.delocalize()) {

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
                                WrapAndIndent.MESSAGE_KEY__MUST_WRAP_LINE_BEFORE_1,
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
                                WrapAndIndent.MESSAGE_KEY__MUST_WRAP_LINE_BEFORE_1,
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
                assert c.getType() == LocalTokenType.RBRACK.delocalize();
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

                assert rparen.getType() == LocalTokenType.RPAREN.delocalize();
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
            if (tmp == null && ast.getType() == LocalTokenType.MODIFIERS.delocalize()) tmp = ast.getNextSibling();
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
        if (previous.getType() != LocalTokenType.LPAREN.delocalize()) {
            this.checkExpression(previous, inline);
            return previous.getNextSibling();
        }

        @SuppressWarnings("unused") AstDumper dumper = new AstDumper(previous); // For debugging

        DetailAST next = previous.getNextSibling();
        for (;;) {
            if (next.getType() != LocalTokenType.LPAREN.delocalize()) {
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
        assert next.getType() == LocalTokenType.RPAREN.delocalize();
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
            if (ast.getParent().getType() == LocalTokenType.CASE_GROUP.delocalize()) {
                ast = ast.getParent().getParent();
            }
            break;

        case PARAMETERS:
            ast = ast.getParent().findFirstToken(LocalTokenType.IDENT.delocalize());
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
                                        doBranch = child == null || ((LocalTokenType) na).delocalize() != child.getType();
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
                    child = child.getNextSibling();
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
                                if (child.getType() == LocalTokenType.CASE_GROUP.delocalize()) {
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
                                    ast.getType() == LocalTokenType.ARRAY_INIT.delocalize()
                                    || ast.getType() == LocalTokenType.METHOD_CALL.delocalize()
                                    || ast.getParent().getType() == LocalTokenType.ENUM_DEF.delocalize()
                                ) {

                                    // Allow multiple children in the same line.
                                    ;
                                } else {
                                    this.log(
                                        l,
                                        WrapAndIndent.MESSAGE_KEY__MUST_WRAP_LINE_BEFORE_1,
                                        previousAst.getText(),
                                        l.getText()
                                    );
                                }
                            } else {
                                if (child.getType() == LocalTokenType.CASE_GROUP.delocalize()) {
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
                    if (previousAst.getType() == LocalTokenType.MODIFIERS.delocalize()) {
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
                child = child.getNextSibling();
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
            this.log(next, WrapAndIndent.MESSAGE_KEY__MUST_WRAP_LINE_BEFORE_1, previous.getText(), next.getText());
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
            this.log(next, WrapAndIndent.MESSAGE_KEY__MUST_WRAP_LINE_BEFORE_1, previous.getText(), next.getText());
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
            this.log(next, WrapAndIndent.MESSAGE_KEY__MUST_WRAP_LINE_BEFORE_1, previous.getText(), next.getText());
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
                WrapAndIndent.MESSAGE_KEY__0_MUST_APPEAR_ON_SAME_LINE_AS_1,
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
                WrapAndIndent.MESSAGE_KEY__0_MUST_APPEAR_IN_COLUMN_1_NOT_2,
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

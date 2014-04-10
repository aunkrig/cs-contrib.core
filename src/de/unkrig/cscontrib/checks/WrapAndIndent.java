
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

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.eclipsecs.core.config.meta.IOptionProvider;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.Utils;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

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

    /** May be ORed to the {@link TokenTypes}. */
    private static final int OPTIONAL           = 0x80000000;
    private static final int INDENT_IF_CHILDREN = 0x40000000;
    private static final int INDENT             = 0x20000000;
    private static final int UNINDENT           = 0x10000000;
    private static final int WRAP               = 0x08000000;
    private static final int MUST_WRAP          = 0x04000000;
    private static final int MASK               = 0x03ffffff;

    private static final int ANY     = 999;
    private static final int FORK    = 800;
    private static final int BRANCH  = 900;
    private static final int END     = 996;

    // CONFIGURATION VARIABLES
    private int     basicOffset                      = 4;
    private boolean allowOneLineClassDecl            = true;
    private boolean allowOneLineInterfaceDecl        = true;
    private boolean allowOneLineEnumDecl             = true;
    private boolean allowOneLineAnnoDecl             = true;
    private boolean allowOneLineCtorDecl             = true;
    private boolean allowOneLineMethDecl             = true;
    private boolean allowOneLineSwitchBlockStmtGroup = true;
    private int     wrapClassDeclBeforeClass         = WrapAndIndent.MUST_WRAP;
    private int     wrapInterfaceDeclBeforeInterface = WrapAndIndent.MUST_WRAP;
    private int     wrapEnumDeclBeforeEnum           = WrapAndIndent.MUST_WRAP;
    private int     wrapAnnoDeclBeforeAt             = WrapAndIndent.MUST_WRAP;
    private int     wrapFieldDeclBeforeName          = WrapAndIndent.WRAP;
    private int     wrapCtorDeclBeforeName           = WrapAndIndent.MUST_WRAP;
    private int     wrapMethDeclBeforeName           = WrapAndIndent.MUST_WRAP;
    private int     wrapLocVarDeclBeforeName         = WrapAndIndent.WRAP;
    private int     wrapTypeDeclBeforeLCurly;
    private int     wrapCtorDeclBeforeLCurly;
    private int     wrapMethodDeclBeforeLCurly;
    private int     wrapDoBeforeLCurly;
    private int     wrapTryBeforeCatch               = WrapAndIndent.WRAP;
    private int     wrapTryBeforeFinally             = WrapAndIndent.WRAP;
    private int     wrapArrayInitBeforeLCurly;
    private int     wrapAnonClassDeclBeforeLCurly;
    private int     wrapBeforeBinaryOperator         = WrapAndIndent.WRAP;
    private int     wrapAfterBinaryOperator;

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

    private static int
    toWrap(String value) {
        return (
            "always".equals(value)   ? WrapAndIndent.MUST_WRAP :
            "optional".equals(value) ? WrapAndIndent.WRAP :
            "never".equals(value)    ? 0 :
            WrapAndIndent.throwException(RuntimeException.class, Integer.class, "Invalid string value '" + value + "'")
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
        return new int[] {
//            TokenTypes.ABSTRACT,
            TokenTypes.ANNOTATION,
            TokenTypes.ANNOTATIONS,
            TokenTypes.ANNOTATION_ARRAY_INIT,
            TokenTypes.ANNOTATION_DEF,
            TokenTypes.ANNOTATION_FIELD_DEF,
            TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR,
            TokenTypes.ARRAY_DECLARATOR,
            TokenTypes.ARRAY_INIT,
            TokenTypes.ASSIGN,  // To check 'int[] ia = { 1, 2, 3 };'.
//            TokenTypes.AT,
//            TokenTypes.BAND,
//            TokenTypes.BAND_ASSIGN,
//            TokenTypes.BNOT,
//            TokenTypes.BOR,
//            TokenTypes.BOR_ASSIGN,
//            TokenTypes.BSR,
//            TokenTypes.BSR_ASSIGN,
//            TokenTypes.BXOR,
//            TokenTypes.BXOR_ASSIGN,
            TokenTypes.CASE_GROUP,
//            TokenTypes.CHAR_LITERAL,
            TokenTypes.CLASS_DEF,
//            TokenTypes.COLON,
//            TokenTypes.COMMA,
            TokenTypes.CTOR_CALL,
            TokenTypes.CTOR_DEF,
//            TokenTypes.DEC,
//            TokenTypes.DIV,
//            TokenTypes.DIV_ASSIGN,
            TokenTypes.DOT,
//            TokenTypes.DO_WHILE,
            TokenTypes.ELIST,
//            TokenTypes.ELLIPSIS,
//            TokenTypes.EMPTY_STAT,
//            TokenTypes.ENUM,
            TokenTypes.ENUM_CONSTANT_DEF,
            TokenTypes.ENUM_DEF,
//            TokenTypes.EOF,
//            TokenTypes.EQUAL,
            TokenTypes.EXPR,
            TokenTypes.EXTENDS_CLAUSE,
//            TokenTypes.FINAL,
            TokenTypes.FOR_CONDITION,
            TokenTypes.FOR_EACH_CLAUSE,
            TokenTypes.FOR_INIT,
            TokenTypes.FOR_ITERATOR,
//            TokenTypes.GE,
//            TokenTypes.GENERIC_END,
//            TokenTypes.GENERIC_START,
//            TokenTypes.GT,
//            TokenTypes.IDENT,
            TokenTypes.IMPLEMENTS_CLAUSE,
            TokenTypes.IMPORT,
//            TokenTypes.INC,
//            TokenTypes.INDEX_OP,
            TokenTypes.INSTANCE_INIT,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.LABELED_STAT,
//            TokenTypes.LAND,
//            TokenTypes.LCURLY,
//            TokenTypes.LE,
            TokenTypes.LITERAL_ASSERT,
//            TokenTypes.LITERAL_BOOLEAN,
            TokenTypes.LITERAL_BREAK,
//            TokenTypes.LITERAL_BYTE,
            TokenTypes.LITERAL_CASE,
            TokenTypes.LITERAL_CATCH,
//            TokenTypes.LITERAL_CHAR,
//            TokenTypes.LITERAL_CLASS,
            TokenTypes.LITERAL_CONTINUE,
//            TokenTypes.LITERAL_DEFAULT,
            TokenTypes.LITERAL_DO,
//            TokenTypes.LITERAL_DOUBLE,
//            TokenTypes.LITERAL_ELSE,
//            TokenTypes.LITERAL_FALSE,
            TokenTypes.LITERAL_FINALLY,
//            TokenTypes.LITERAL_FLOAT,
            TokenTypes.LITERAL_FOR,
            TokenTypes.LITERAL_IF,
//            TokenTypes.LITERAL_INSTANCEOF,
//            TokenTypes.LITERAL_INT,
//            TokenTypes.LITERAL_INTERFACE,
//            TokenTypes.LITERAL_LONG,
//            TokenTypes.LITERAL_NATIVE,
            TokenTypes.LITERAL_NEW,
//            TokenTypes.LITERAL_NULL,
//            TokenTypes.LITERAL_PRIVATE,
//            TokenTypes.LITERAL_PROTECTED,
//            TokenTypes.LITERAL_PUBLIC,
            TokenTypes.LITERAL_RETURN,
//            TokenTypes.LITERAL_SHORT,
//            TokenTypes.LITERAL_STATIC,
//            TokenTypes.LITERAL_SUPER,
            TokenTypes.LITERAL_SWITCH,
            TokenTypes.LITERAL_SYNCHRONIZED,
//            TokenTypes.LITERAL_THIS,
            TokenTypes.LITERAL_THROW,
            TokenTypes.LITERAL_THROWS,
//            TokenTypes.LITERAL_TRANSIENT,
//            TokenTypes.LITERAL_TRUE,
            TokenTypes.LITERAL_TRY,
//            TokenTypes.LITERAL_VOID,
//            TokenTypes.LITERAL_VOLATILE,
            TokenTypes.LITERAL_WHILE,
//            TokenTypes.LNOT,
//            TokenTypes.LOR,
//            TokenTypes.LPAREN,
//            TokenTypes.LT,
//            TokenTypes.METHOD_CALL,
            TokenTypes.METHOD_DEF,
//            TokenTypes.MINUS,
//            TokenTypes.MINUS_ASSIGN,
//            TokenTypes.MOD,
            TokenTypes.MODIFIERS,
//            TokenTypes.MOD_ASSIGN,
//            TokenTypes.NOT_EQUAL,
//            TokenTypes.NUM_DOUBLE,
//            TokenTypes.NUM_FLOAT,
//            TokenTypes.NUM_INT,
//            TokenTypes.NUM_LONG,
            TokenTypes.OBJBLOCK,
            TokenTypes.PACKAGE_DEF,
            TokenTypes.PARAMETERS,
            TokenTypes.PARAMETER_DEF,
//            TokenTypes.PLUS,
//            TokenTypes.PLUS_ASSIGN,
//            TokenTypes.POST_DEC,
//            TokenTypes.POST_INC,
//            TokenTypes.QUESTION,
//            TokenTypes.RBRACK,
//            TokenTypes.RCURLY,
//            TokenTypes.RPAREN,
//            TokenTypes.SEMI,
//            TokenTypes.SL,
            TokenTypes.SLIST,
//            TokenTypes.SL_ASSIGN,
//            TokenTypes.SR,
//            TokenTypes.SR_ASSIGN,
//            TokenTypes.STAR,
//            TokenTypes.STAR_ASSIGN,
            TokenTypes.STATIC_IMPORT,
            TokenTypes.STATIC_INIT,
//            TokenTypes.STRICTFP,
//            TokenTypes.STRING_LITERAL,
            TokenTypes.SUPER_CTOR_CALL,
//            TokenTypes.TYPE,
//            TokenTypes.TYPECAST,
            TokenTypes.TYPE_ARGUMENT,
            TokenTypes.TYPE_ARGUMENTS,
//            TokenTypes.TYPE_EXTENSION_AND,
            TokenTypes.TYPE_LOWER_BOUNDS,
            TokenTypes.TYPE_PARAMETER,
            TokenTypes.TYPE_PARAMETERS,
            TokenTypes.TYPE_UPPER_BOUNDS,
//            TokenTypes.UNARY_MINUS,
//            TokenTypes.UNARY_PLUS,
            TokenTypes.VARIABLE_DEF,
//            TokenTypes.WILDCARD_TYPE,
        };
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        @SuppressWarnings("unused") AstDumper dumper = new AstDumper(ast); // For debugging

        switch (ast.getType()) {

        case ANNOTATION:
            this.checkChildren(
                ast,

                TokenTypes.AT,
                WrapAndIndent.FORK + 4,
                TokenTypes.DOT,
                WrapAndIndent.BRANCH + 5,

/* 4 */         TokenTypes.IDENT,

/* 5 */         WrapAndIndent.FORK + 7,
                WrapAndIndent.END,

/* 7 */         TokenTypes.LPAREN,
                WrapAndIndent.BRANCH + 10,

/* 9 */         TokenTypes.COMMA,

/* 10 */        WrapAndIndent.FORK + 13,
                WrapAndIndent.INDENT | TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR,
                WrapAndIndent.BRANCH + 20,

/* 13 */        WrapAndIndent.FORK + 16,
                WrapAndIndent.INDENT | TokenTypes.ANNOTATION,
                WrapAndIndent.BRANCH + 20,

/* 16 */        WrapAndIndent.FORK + 19,
                WrapAndIndent.INDENT | TokenTypes.EXPR,
                WrapAndIndent.BRANCH + 20,

/* 19 */        WrapAndIndent.INDENT | TokenTypes.ANNOTATION_ARRAY_INIT,

/* 20 */        WrapAndIndent.FORK + 9,
                WrapAndIndent.UNINDENT | TokenTypes.RPAREN,
                WrapAndIndent.END
            );
            break;

        case ANNOTATION_ARRAY_INIT:
            this.checkChildren(
                ast,

                WrapAndIndent.FORK + 5,

/* 1 */         WrapAndIndent.INDENT | TokenTypes.EXPR,
                WrapAndIndent.FORK + 5,
                TokenTypes.COMMA,
                WrapAndIndent.FORK + 1,

/* 5 */         WrapAndIndent.UNINDENT | TokenTypes.RCURLY,
                WrapAndIndent.END
            );
            break;

        case ANNOTATION_DEF:
            if (this.allowOneLineAnnoDecl && this.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                TokenTypes.MODIFIERS,
                this.wrapAnnoDeclBeforeAt | TokenTypes.AT,
                TokenTypes.LITERAL_INTERFACE,
                TokenTypes.IDENT,
                this.wrapTypeDeclBeforeLCurly | TokenTypes.OBJBLOCK,
                WrapAndIndent.END
            );
            break;

        case ARRAY_INIT:
            this.checkChildren(
                ast,

/* 0 */         WrapAndIndent.FORK + 5,
                WrapAndIndent.INDENT | WrapAndIndent.ANY,
                WrapAndIndent.FORK + 5,
                TokenTypes.COMMA,
                WrapAndIndent.BRANCH + 0,
/* 5 */         WrapAndIndent.UNINDENT | TokenTypes.RCURLY,
                WrapAndIndent.END
            );
            break;

        case CASE_GROUP:
            this.checkChildren(
                ast,

                WrapAndIndent.FORK + 6,
                TokenTypes.LITERAL_CASE,                              // case 1: case 2:
/* 2 */         WrapAndIndent.FORK + 5,
                WrapAndIndent.WRAP | TokenTypes.LITERAL_CASE,
                WrapAndIndent.BRANCH + 2,

/* 5 */         WrapAndIndent.FORK + 7,
/* 6 */         WrapAndIndent.WRAP | TokenTypes.LITERAL_DEFAULT,

/* 7 */         WrapAndIndent.INDENT_IF_CHILDREN | TokenTypes.SLIST,
                WrapAndIndent.END
            );
            break;

        case CLASS_DEF:
            if (this.allowOneLineClassDecl && this.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                TokenTypes.MODIFIERS,
                this.wrapClassDeclBeforeClass | TokenTypes.LITERAL_CLASS,
                TokenTypes.IDENT,

                WrapAndIndent.FORK + 5,
                TokenTypes.TYPE_PARAMETERS,

/* 5 */         WrapAndIndent.FORK + 7,
                WrapAndIndent.WRAP | TokenTypes.EXTENDS_CLAUSE,

/* 7 */         WrapAndIndent.FORK + 9,
                WrapAndIndent.WRAP | TokenTypes.IMPLEMENTS_CLAUSE,

/* 9 */         this.wrapTypeDeclBeforeLCurly | TokenTypes.OBJBLOCK,
                WrapAndIndent.END
            );
            break;

        case CTOR_CALL:
            this.checkChildren(
                ast,

                TokenTypes.LPAREN,
                WrapAndIndent.INDENT_IF_CHILDREN | TokenTypes.ELIST,
                WrapAndIndent.UNINDENT | TokenTypes.RPAREN,
                TokenTypes.SEMI,
                WrapAndIndent.END
            );
            break;

        case CTOR_DEF:
            if (this.allowOneLineCtorDecl && this.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                TokenTypes.MODIFIERS,
                this.wrapCtorDeclBeforeName | TokenTypes.IDENT,

                TokenTypes.LPAREN,
                WrapAndIndent.INDENT_IF_CHILDREN | TokenTypes.PARAMETERS,
                WrapAndIndent.UNINDENT | TokenTypes.RPAREN,

                WrapAndIndent.FORK + 7,
                WrapAndIndent.WRAP | TokenTypes.LITERAL_THROWS,

/* 7 */         this.wrapCtorDeclBeforeLCurly | TokenTypes.SLIST,
                WrapAndIndent.END
            );
            break;

        case ELIST:
            this.checkChildren(
                ast,

                WrapAndIndent.FORK + 5,
/* 1 */         WrapAndIndent.INDENT | TokenTypes.EXPR,
                WrapAndIndent.FORK + 5,
/* 3 */         TokenTypes.COMMA,
                WrapAndIndent.BRANCH + 1,
/* 6 */         WrapAndIndent.END
            );
            break;

        case ENUM_DEF:
            if (this.allowOneLineEnumDecl && this.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                TokenTypes.MODIFIERS,
                this.wrapEnumDeclBeforeEnum | TokenTypes.ENUM,
                TokenTypes.IDENT,
                this.wrapTypeDeclBeforeLCurly | TokenTypes.OBJBLOCK,
                WrapAndIndent.END
            );
            break;

        case EXPR:
            {
                DetailAST child = ast.getFirstChild();
                if (child.getType() == TokenTypes.LPAREN) {
                    child = this.checkParenthesizedExpression(child, false);
                    assert child == null;
                } else {
                    boolean inline;
                    switch (ast.getParent().getType()) {

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
                            + TokenTypes.getTokenName(ast.getParent().getType())
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

                TokenTypes.VARIABLE_DEF,
                WrapAndIndent.WRAP | TokenTypes.COLON,
                TokenTypes.EXPR,
                WrapAndIndent.END
            );
            break;

        case INTERFACE_DEF:
            if (this.allowOneLineInterfaceDecl && this.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

                TokenTypes.MODIFIERS,
                this.wrapInterfaceDeclBeforeInterface | TokenTypes.LITERAL_INTERFACE,
                TokenTypes.IDENT,
                WrapAndIndent.FORK + 5,
                TokenTypes.TYPE_PARAMETERS,

/* 5 */         WrapAndIndent.FORK + 7,
                WrapAndIndent.WRAP | TokenTypes.EXTENDS_CLAUSE,

/* 7 */         this.wrapTypeDeclBeforeLCurly | TokenTypes.OBJBLOCK,
                WrapAndIndent.END
            );
            break;

        case LABELED_STAT:
            this.checkChildren(
                ast,

                TokenTypes.IDENT,
                WrapAndIndent.WRAP | WrapAndIndent.ANY,
                WrapAndIndent.END
            );
            break;

        case LITERAL_DO:
            this.checkChildren(
                ast,

                this.wrapDoBeforeLCurly | TokenTypes.SLIST,
                TokenTypes.DO_WHILE,
                TokenTypes.LPAREN,
                WrapAndIndent.INDENT | TokenTypes.EXPR,
                WrapAndIndent.UNINDENT | TokenTypes.RPAREN,
                TokenTypes.SEMI,
                WrapAndIndent.END
            );
            break;

        case LITERAL_FOR:
            this.checkChildren(
                ast,

                TokenTypes.LPAREN,
                WrapAndIndent.FORK + 8,
                WrapAndIndent.INDENT | TokenTypes.FOR_INIT,
                TokenTypes.SEMI,
                WrapAndIndent.INDENT | TokenTypes.FOR_CONDITION,
                TokenTypes.SEMI,
                WrapAndIndent.INDENT_IF_CHILDREN | TokenTypes.FOR_ITERATOR,
                WrapAndIndent.FORK + 9,

/* 8 */         WrapAndIndent.INDENT | TokenTypes.FOR_EACH_CLAUSE,
/* 9 */         WrapAndIndent.UNINDENT | TokenTypes.RPAREN,

                WrapAndIndent.FORK + 14,
                TokenTypes.EXPR,
                TokenTypes.SEMI,
                WrapAndIndent.END,

/* 14 */        WrapAndIndent.ANY,
                WrapAndIndent.END
            );
            break;

        case LITERAL_IF:
            this.checkChildren(
                ast,

                TokenTypes.LPAREN,
                WrapAndIndent.INDENT | TokenTypes.EXPR,
                WrapAndIndent.UNINDENT | TokenTypes.RPAREN,
                WrapAndIndent.FORK + 7,
                TokenTypes.EXPR,
                TokenTypes.SEMI,
                WrapAndIndent.END,

/* 7 */         WrapAndIndent.ANY,
                WrapAndIndent.FORK + 10,
                TokenTypes.LITERAL_ELSE,

/* 10 */        WrapAndIndent.END
            );
            break;

        case LITERAL_NEW:
            this.checkChildren(
                ast,

/* 0 */         WrapAndIndent.ANY,
                WrapAndIndent.FORK + 3,
                TokenTypes.TYPE_ARGUMENTS,
/* 3 */         WrapAndIndent.FORK + 8,
                TokenTypes.ARRAY_DECLARATOR,
                WrapAndIndent.FORK + 7,
                this.wrapArrayInitBeforeLCurly | TokenTypes.ARRAY_INIT,
/* 7 */         WrapAndIndent.END,

/* 8 */         TokenTypes.LPAREN,
                WrapAndIndent.INDENT_IF_CHILDREN | TokenTypes.ELIST,
                WrapAndIndent.UNINDENT | TokenTypes.RPAREN,
                this.wrapAnonClassDeclBeforeLCurly | TokenTypes.OBJBLOCK | WrapAndIndent.OPTIONAL,
                WrapAndIndent.END
            );
            break;

        case LITERAL_SWITCH:
            this.checkChildren(
                ast,

                TokenTypes.LPAREN,
                WrapAndIndent.INDENT | TokenTypes.EXPR,
                WrapAndIndent.UNINDENT | TokenTypes.RPAREN,
                TokenTypes.LCURLY,
                WrapAndIndent.FORK + 7,
/* 5 */         WrapAndIndent.INDENT | TokenTypes.CASE_GROUP,
                WrapAndIndent.FORK + 5,
/* 7 */         WrapAndIndent.UNINDENT | TokenTypes.RCURLY,
                WrapAndIndent.END
            );
            break;

        case METHOD_DEF:
            if (this.allowOneLineMethDecl && this.isSingleLine(ast)) break;
            this.checkChildren(
                ast,

/* 0 */         TokenTypes.MODIFIERS,
                WrapAndIndent.FORK + 3,
                TokenTypes.TYPE_PARAMETERS,

/* 3 */         TokenTypes.TYPE,

                this.wrapMethDeclBeforeName | TokenTypes.IDENT,

                TokenTypes.LPAREN,
                WrapAndIndent.INDENT_IF_CHILDREN | TokenTypes.PARAMETERS,
                WrapAndIndent.UNINDENT | TokenTypes.RPAREN,

                WrapAndIndent.FORK + 10,
                WrapAndIndent.WRAP | TokenTypes.LITERAL_THROWS,

/* 10 */        WrapAndIndent.FORK + 13,
                this.wrapMethodDeclBeforeLCurly | TokenTypes.SLIST,
                WrapAndIndent.END,

/* 13 */        TokenTypes.SEMI,
                WrapAndIndent.END
            );
            break;

        case LITERAL_WHILE:
            this.checkChildren(
                ast,

                TokenTypes.LPAREN,
                WrapAndIndent.INDENT | TokenTypes.EXPR,
                WrapAndIndent.UNINDENT | TokenTypes.RPAREN,
                WrapAndIndent.FORK + 7,

                TokenTypes.EXPR,
                TokenTypes.SEMI,
                WrapAndIndent.END,

/* 7 */         WrapAndIndent.ANY,
                WrapAndIndent.END
            );
            break;

        case MODIFIERS:
            this.checkChildren(
                ast,

                WrapAndIndent.FORK + 3,

/* 1 */         WrapAndIndent.ANY,
                WrapAndIndent.FORK + 1,

/* 3 */         WrapAndIndent.END
            );
            break;

        case OBJBLOCK:
            this.checkChildren(
                ast,

/* 0 */         TokenTypes.LCURLY,

                WrapAndIndent.FORK + 8,
/* 2 */         WrapAndIndent.INDENT | TokenTypes.ENUM_CONSTANT_DEF,
                WrapAndIndent.FORK + 6,
                TokenTypes.COMMA,
                WrapAndIndent.FORK + 2,
/* 6 */         WrapAndIndent.FORK + 8,
                WrapAndIndent.INDENT | TokenTypes.SEMI,

/* 8 */         WrapAndIndent.FORK + 14,
                WrapAndIndent.INDENT | TokenTypes.VARIABLE_DEF,
/* 10 */        WrapAndIndent.FORK + 8,
                TokenTypes.COMMA,                  // int a = 3, b = 7;
                TokenTypes.VARIABLE_DEF,
                WrapAndIndent.BRANCH + 10,

/* 14 */        WrapAndIndent.FORK + 17,
/* 15 */        WrapAndIndent.UNINDENT | TokenTypes.RCURLY,
                WrapAndIndent.END,

/* 17 */        WrapAndIndent.INDENT | WrapAndIndent.ANY,
                WrapAndIndent.BRANCH + 6
            );
            break;

        case PARAMETERS:
            this.checkChildren(
                ast,

                WrapAndIndent.FORK + 5,
/* 1 */         WrapAndIndent.INDENT | TokenTypes.PARAMETER_DEF,
                WrapAndIndent.FORK + 5,
/* 3 */         TokenTypes.COMMA,
                WrapAndIndent.BRANCH + 1,
/* 5 */         WrapAndIndent.END
            );
            break;

        case SLIST:
            // Single-line case group?
            if (
                ast.getParent().getType() == TokenTypes.CASE_GROUP
                && this.allowOneLineSwitchBlockStmtGroup
                && this.isSingleLine(ast)
                && ast.getParent().getLineNo() == ast.getLineNo()
            ) return;

            this.checkChildren(
                ast,

/* 0 */         WrapAndIndent.FORK + 4,
                WrapAndIndent.INDENT | TokenTypes.EXPR,
                TokenTypes.SEMI,
                WrapAndIndent.BRANCH + 0,

/* 4 */         WrapAndIndent.FORK + 12,
                WrapAndIndent.INDENT | TokenTypes.VARIABLE_DEF,
/* 6 */         WrapAndIndent.FORK + 10,
                TokenTypes.COMMA,
                TokenTypes.VARIABLE_DEF,
                WrapAndIndent.BRANCH + 6,
/* 10 */        TokenTypes.SEMI,
                WrapAndIndent.BRANCH + 0,

                // SLIST in CASE_GROUP ends _without_ an RCURLY!
/* 12 */        WrapAndIndent.FORK + 14,
                WrapAndIndent.END,

/* 14 */        WrapAndIndent.FORK + 17,
                WrapAndIndent.UNINDENT | TokenTypes.RCURLY,
                WrapAndIndent.END,

/* 17 */        WrapAndIndent.INDENT | WrapAndIndent.ANY,
                WrapAndIndent.BRANCH + 0
            );
            break;

        case SUPER_CTOR_CALL:
            this.checkChildren(
                ast,

                WrapAndIndent.FORK + 3,
                WrapAndIndent.ANY,
                TokenTypes.DOT,

/* 3 */         TokenTypes.LPAREN,
                WrapAndIndent.INDENT_IF_CHILDREN | TokenTypes.ELIST,
                WrapAndIndent.UNINDENT | TokenTypes.RPAREN,
                TokenTypes.SEMI,
                WrapAndIndent.END
            );
            break;

        case VARIABLE_DEF:
            this.checkChildren(
                ast,

                TokenTypes.MODIFIERS,
                TokenTypes.TYPE,
                (
                    ast.getParent().getType() == TokenTypes.OBJBLOCK
                    ? this.wrapFieldDeclBeforeName
                    : this.wrapLocVarDeclBeforeName
                ) | TokenTypes.IDENT,
                WrapAndIndent.FORK + 5,
                TokenTypes.ASSIGN,

/* 5 */         WrapAndIndent.FORK + 7,
                TokenTypes.SEMI, // Field declarations DO have a SEMI, local variable declarations DON'T!?

/* 7 */         WrapAndIndent.END
            );
            break;

        case ASSIGN:
            if (ast.getChildCount() == 1) {

                // A field or local variable initialization.
                this.checkChildren(
                    ast,

                    WrapAndIndent.FORK + 3,
                    this.wrapArrayInitBeforeLCurly | TokenTypes.ARRAY_INIT,
                    WrapAndIndent.END,

/* 3 */             WrapAndIndent.ANY,
                    WrapAndIndent.END
                );
            }
            break;

        case LITERAL_TRY:
            this.checkChildren(
                ast,

                TokenTypes.SLIST,
                WrapAndIndent.FORK + 5,
/* 2 */         this.wrapTryBeforeCatch | TokenTypes.LITERAL_CATCH,
                WrapAndIndent.FORK + 2,
                WrapAndIndent.FORK + 6,
/* 5 */         this.wrapTryBeforeFinally | TokenTypes.LITERAL_FINALLY,
/* 6 */         WrapAndIndent.END
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
                    if (
                        a.getType() == TokenTypes.PACKAGE_DEF
                        || a.getType() == TokenTypes.IMPORT
                    ) break PACKAGE_OR_IMPORT;
                }
                break;
            }
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
        case PACKAGE_DEF:
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

                WrapAndIndent.FORK + 3,
/* 1 */         WrapAndIndent.ANY,
                WrapAndIndent.FORK + 1,
/* 3 */         WrapAndIndent.END
            );
            break;

        default:
            throw new AssertionError("Unknown token type '" + ast.getType() + "'");
        }
    }

    private boolean
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

        switch (expression.getType()) {

        // Ternary operation
        case QUESTION:
            {
                DetailAST c = this.checkParenthesizedExpression(expression.getFirstChild(), inline);
                c = this.checkParenthesizedExpression(c, inline);
                assert c.getType() == TokenTypes.COLON;
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
                assert c.getType() == TokenTypes.RBRACK;
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
                if (c != null && c.getType() == TokenTypes.TYPE_ARGUMENTS) {

                    // TYPE_ARGUMENTS checked by "visitToken()".
                    ;
                    c = c.getNextSibling();
                }
                assert c != null : (
                    this.getFileContents().getFilename()
                    + ":"
                    + expression.getLineNo()
                    + ": Second operand for '"
                    + TokenTypes.getTokenName(expression.getType())
                    + "' missing"
                );

                // Check wrapping and alignment of LHS and operator.
                {
                    DetailAST lhs = WrapAndIndent.getRightmostDescendant(c.getPreviousSibling());
                    switch (inline ? 0 : this.wrapBeforeBinaryOperator) {
                    case 0:
                        this.checkSameLine(lhs, expression);
                        break;
                    case WRAP:
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
                    }
                }

                // Check wrapping and alignment of operator and RHS.
                {
                    DetailAST rhs = WrapAndIndent.getLeftmostDescendant(c);
                    switch (inline ? 0 : this.wrapAfterBinaryOperator) {
                    case 0:
                        this.checkSameLine(expression, rhs);
                        break;
                    case WRAP:
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
                    }
                }

                c = this.checkParenthesizedExpression(c, inline);
                assert c == null : (
                    this.getFileContents().getFilename()
                    + ":"
                    + expression.getLineNo()
                    + ": Unexpected third operand "
                    + TokenTypes.getTokenName(c.getType())
                    + "/'"
                    + c.getText()
                    + "' for '"
                    + TokenTypes.getTokenName(expression.getType())
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
                assert c.getType() == TokenTypes.RBRACK;
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

                assert rparen.getType() == TokenTypes.RPAREN;
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
                "Uncheckable: " + TokenTypes.getTokenName(expression.getType()) + " / " + expression.toString()
            );
        }
    }

    private static DetailAST
    getLeftmostDescendant(DetailAST ast) {
        for (;;) {
            DetailAST tmp = ast.getFirstChild();
            if (tmp == null && ast.getType() == TokenTypes.MODIFIERS) tmp = ast.getNextSibling();
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
        if (previous.getType() != TokenTypes.LPAREN) {
            this.checkExpression(previous, inline);
            return previous.getNextSibling();
        }

        @SuppressWarnings("unused") AstDumper dumper = new AstDumper(previous); // For debugging

        int       parenthesisCount = 1;
        DetailAST next             = previous.getNextSibling();
        for (;;) {
            if (next.getType() != TokenTypes.LPAREN) {
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
        for (int i = 1; i < parenthesisCount; ++i) {
            assert previous.getType() == TokenTypes.RPAREN;
            next = next.getNextSibling();
            this.checkSameLine(previous, next);
            previous = next;
        }
        assert next.getType() == TokenTypes.RPAREN;
        return next.getNextSibling();
    }

    /**
     * Verifies that the children of the given {@code ast} are positioned as specified.
     */
    private void
    checkChildren(DetailAST ast, Integer... args) {
        int       idx   = 0;
        DetailAST child = ast.getFirstChild();

        // Determine the "indentation parent".
        if (ast.getType() == TokenTypes.ELIST) {
            ast = ast.getParent();
        } else
        if (ast.getType() == TokenTypes.SLIST && ast.getParent().getType() == TokenTypes.CASE_GROUP) {
            ast = ast.getParent().getParent();
        } else
        if (ast.getType() == TokenTypes.PARAMETERS) {
            ast = ast.getParent().findFirstToken(TokenTypes.IDENT);
        } else
        if (ast.getType() == TokenTypes.DOT) {
            ast = WrapAndIndent.getLeftmostDescendant(ast);
        }

        DetailAST previousAst = ast;
        int       mode        = 0;
        for (;;) {
            int tokenType = args[idx++];

            // Handle END.
            if ((tokenType & WrapAndIndent.MASK) == WrapAndIndent.END) {
                if (child == null) return;
                this.log(child, "Unexpected extra token ''{0}''", child.getText());
                return;
            }

            // Handle OPTIONAL.
            if ((tokenType & WrapAndIndent.OPTIONAL) != 0) {
                if (child != null && (
                    (tokenType & WrapAndIndent.MASK) == WrapAndIndent.ANY
                    || child.getType() == (tokenType & WrapAndIndent.MASK)
                )) {
                    previousAst = child;
                    child       = child.getNextSibling();
                }
                continue;
            }

            // Handle FORK.
            if (tokenType >= WrapAndIndent.FORK && tokenType <= WrapAndIndent.FORK + 90) {

                int     destination = tokenType - WrapAndIndent.FORK;
                Integer da          = args[destination] & WrapAndIndent.MASK;
                if (
                    child == null ? args[idx] != WrapAndIndent.END && da >= WrapAndIndent.FORK
                    : (
                        da == WrapAndIndent.ANY
                        || (da >= WrapAndIndent.FORK && da <= WrapAndIndent.FORK + 90)
                        || (da >= WrapAndIndent.BRANCH && da <= WrapAndIndent.BRANCH + 90)
                    ) ? (args[idx] & WrapAndIndent.MASK) != child.getType()
                    : da == child.getType()
                ) idx = destination;
                continue;
            }

            // Handle BRANCH.
            if (tokenType >= WrapAndIndent.BRANCH && tokenType <= WrapAndIndent.BRANCH + 90) {
                idx = tokenType - WrapAndIndent.BRANCH;
                continue;
            }

            if (child == null) {
                this.log(
                    previousAst,
                    "Expected ''{0}'' after ''{1}''",
                    (
                        (tokenType & WrapAndIndent.MASK) == WrapAndIndent.ANY
                        ? "ANY"
                        : TokenTypes.getTokenName(tokenType & WrapAndIndent.MASK)
                    ),
                    previousAst.getText()
                );
                return;
            }

            if (
                (tokenType & WrapAndIndent.MASK) != WrapAndIndent.ANY
                && child.getType() != (tokenType & WrapAndIndent.MASK)
            ) {
                this.log(
                    child,
                    "Expected ''{0}'' instead of ''{1}''",
                    TokenTypes.getTokenName(tokenType & WrapAndIndent.MASK),
                    child.getText() + "'"
                );
                return;
            }

            if ((tokenType & WrapAndIndent.INDENT_IF_CHILDREN) != 0 && child.getFirstChild() == null) {
                ;
            } else if ((tokenType & (WrapAndIndent.INDENT | WrapAndIndent.INDENT_IF_CHILDREN)) != 0) {
                switch (mode) {

                case 0:
                    {
                        DetailAST c = WrapAndIndent.getLeftmostDescendant(child);
                        if (c.getLineNo() == previousAst.getLineNo()) {
                            mode = 1;
                        } else {
                            mode = 2;
                            if (child.getType() == TokenTypes.CASE_GROUP) {
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
                                ast.getType() == TokenTypes.ARRAY_INIT
                                || ast.getType() == TokenTypes.METHOD_CALL
                                || ast.getParent().getType() == TokenTypes.ENUM_DEF
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
                            if (child.getType() == TokenTypes.CASE_GROUP) {
                                this.checkWrapped(ast, l);
                            } else {
                                this.checkIndented(ast, l);
                            }
                        }
                    }
                    break;
                }
            } else if ((tokenType & WrapAndIndent.UNINDENT) != 0) {
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
            } else if ((tokenType & WrapAndIndent.WRAP) != 0) {
                assert mode == 0;
                if (child.getLineNo() != previousAst.getLineNo()) {
                    this.checkWrapped(previousAst, child);
                }
            } else if ((tokenType & WrapAndIndent.MUST_WRAP) != 0) {
                assert mode == 0;
                if (previousAst.getType() == TokenTypes.MODIFIERS) {
                    ;
                } else
                {
                    this.checkWrapped(previousAst, child);
                }
            } else {
                this.checkSameLine(previousAst, WrapAndIndent.getLeftmostDescendant(child));
            }
            previousAst = WrapAndIndent.getRightmostDescendant(child);
            child       = child.getNextSibling();
        }
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

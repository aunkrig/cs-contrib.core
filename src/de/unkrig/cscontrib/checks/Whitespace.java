
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

import static de.unkrig.cscontrib.util.JavaElement.*;

import java.util.EnumSet;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConversionException;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.util.AstUtil;
import de.unkrig.cscontrib.util.JavaElement;

/**
 * An enhanced replacement for all other '*Whitespace*' checks.
 */
@NotNullByDefault(false) public
class Whitespace extends Check {

    private EnumSet<JavaElement> whitespaceBefore = EnumSet.of(
        AND__EXPR,
        AND__TYPE_BOUND,
        AND_ASSIGN,
        ASSERT,
        ASSIGN__ASSIGNMENT,
        ASSIGN__VAR_DECL,
        BREAK,
        CASE,
        CATCH,
        CLASS__CLASS_DECL,
        COLON__ENHANCED_FOR,
        COLON__TERNARY,
        CONDITIONAL_AND,
        CONDITIONAL_OR,
        CONTINUE,
        DEFAULT__ANNO_ELEM,
        DEFAULT__SWITCH,
        DIVIDE,
        DIVIDE_ASSIGN,
        DO,
        ELSE,
        ENUM,
        EQUAL,
        EXTENDS__TYPE,
        EXTENDS__TYPE_BOUND,
        FINALLY,
        FOR,
        GREATER,
        GREATER_EQUAL,
        IF,
        IMPLEMENTS,
        IMPORT,
        IMPORT__STATIC_IMPORT,
        INSTANCEOF,
        L_ANGLE__METH_DECL_TYPE_PARAMS,
        L_CURLY__ANON_CLASS,
        L_CURLY__BLOCK,
        L_CURLY__CATCH,
        L_CURLY__DO,
        L_CURLY__EMPTY_ANON_CLASS,
        L_CURLY__EMPTY_CATCH,
        L_CURLY__EMPTY_METH_DECL,
        L_CURLY__EMPTY_TYPE_DECL,
        L_CURLY__ENUM_CONST,
        L_CURLY__FINALLY,
        L_CURLY__FOR,
        L_CURLY__IF,
        L_CURLY__INSTANCE_INIT,
        L_CURLY__LABELED_STAT,
        L_CURLY__METH_DECL,
        L_CURLY__STATIC_INIT,
        L_CURLY__SWITCH,
        L_CURLY__SYNCHRONIZED,
        L_CURLY__TRY,
        L_CURLY__TYPE_DECL,
        L_CURLY__WHILE,
        L_PAREN__CATCH,
        L_PAREN__DO_WHILE,
        L_PAREN__FOR,
        L_PAREN__FOR_NO_INIT,
        L_PAREN__IF,
        LEFT_SHIFT,
        LEFT_SHIFT_ASSIGN,
        LESS,
        LESS_EQUAL,
        MINUS__ADDITIVE,
        MINUS_ASSIGN,
        MODULO,
        MODULO_ASSIGN,
        MULTIPLY,
        MULTIPLY_ASSIGN,
        NAME__CTOR_DECL,
        NAME__METH_DECL,
        NAME__PARAM,
        NAME__TYPE_DECL,
        NAME__LOCAL_VAR_DECL,
        NOT_EQUAL,
        OR,
        OR_ASSIGN,
        PACKAGE,
        PLUS__ADDITIVE,
        PLUS_ASSIGN,
        QUESTION__TERNARY,
        R_CURLY__ANNO_ARRAY_INIT,
        R_CURLY__ANON_CLASS,
        R_CURLY__ARRAY_INIT,
        R_CURLY__BLOCK,
        R_CURLY__CATCH,
        R_CURLY__DO,
        R_CURLY__ELSE,
        R_CURLY__FINALLY,
        R_CURLY__FOR,
        R_CURLY__IF,
        R_CURLY__INSTANCE_INIT,
        R_CURLY__LABELED_STAT,
        R_CURLY__METH_DECL,
        R_CURLY__STATIC_INIT,
        R_CURLY__SWITCH,
        R_CURLY__SYNCHRONIZED,
        R_CURLY__TRY,
        R_CURLY__TYPE_DECL,
        R_CURLY__WHILE,
        RETURN__EXPR,
        RETURN__NO_EXPR,
        RIGHT_SHIFT,
        RIGHT_SHIFT_ASSIGN,
        SEMI__TYPE_DECL,
        STATIC__STATIC_IMPORT,
        STATIC__STATIC_INIT,
        SUPER__TYPE_BOUND,
        SWITCH,
        SYNCHRONIZED__MOD,
        SYNCHRONIZED__SYNCHRONIZED,
        THIS__CTOR_CALL,
        THROW,
        THROWS,
        TRY,
        UNSIGNED_RIGHT_SHIFT,
        UNSIGNED_RIGHT_SHIFT_ASSIGN,
        VOID,
        WHILE__DO,
        WHILE__WHILE,
        XOR,
        XOR_ASSIGN
    );
    private EnumSet<JavaElement> noWhitespaceBefore = EnumSet.of(
        CLASS__CLASS_LITERAL,
        COLON__DEFAULT,
        COLON__CASE,
        COLON__LABELED_STAT,
        COMMA,
        DOT__IMPORT,
        DOT__PACKAGE_DECL,
        DOT__QUALIFIED_TYPE,
        DOT__SELECTOR,
        ELLIPSIS,
        L_ANGLE__METH_INVOCATION_TYPE_ARGS,
        L_ANGLE__TYPE_ARGS,
        L_ANGLE__TYPE_PARAMS,
        L_BRACK__ARRAY_DECL,
        L_BRACK__INDEX,
        L_PAREN__ANNO,
        L_PAREN__ANNO_ELEM_DECL,
        L_PAREN__METH_INVOCATION,
        L_PAREN__PARAMS,
        NAME__ANNO,
        POST_DECR,
        POST_INCR,
        R_ANGLE__METH_DECL_TYPE_PARAMS,
        R_ANGLE__METH_INVOCATION_TYPE_ARGS,
        R_ANGLE__TYPE_ARGS,
        R_ANGLE__TYPE_PARAMS,
        R_BRACK__ARRAY_DECL,
        R_BRACK__INDEX,
        R_CURLY__EMPTY_ANNO_ARRAY_INIT,
        R_CURLY__EMPTY_ANON_CLASS,
        R_CURLY__EMPTY_ARRAY_INIT,
        R_CURLY__EMPTY_CATCH,
        R_CURLY__EMPTY_METH_DECL,
        R_CURLY__EMPTY_TYPE_DECL,
        R_CURLY__ENUM_CONST_DECL,
        R_PAREN__ANNO,
        R_PAREN__ANNO_ELEM_DECL,
        R_PAREN__METH_INVOCATION,
        R_PAREN__CAST,
        R_PAREN__CATCH,
        R_PAREN__DO_WHILE,
        R_PAREN__FOR,
        R_PAREN__FOR_NO_UPDATE,
        R_PAREN__IF,
        R_PAREN__PARAMS,
        R_PAREN__PARENTHESIZED,
        SEMI__ABSTRACT_METH_DECL,
        SEMI__ANNO_ELEM_DECL,
        SEMI__ENUM_DECL,
        SEMI__FIELD_DECL,
        SEMI__FOR_CONDITION_NO_UPDATE,
        SEMI__FOR_CONDITION_UPDATE,
        SEMI__FOR_INIT_CONDITION,
        SEMI__FOR_INIT_NO_CONDITION,
        SEMI__FOR_NO_CONDITION_NO_UPDATE,
        SEMI__FOR_NO_CONDITION_UPDATE,
        SEMI__FOR_NO_INIT_CONDITION,
        SEMI__FOR_NO_INIT_NO_CONDITION,
        SEMI__IMPORT,
        SEMI__PACKAGE_DECL,
        SEMI__STATEMENT,
        SEMI__STATIC_IMPORT,
        STAR__TYPE_IMPORT_ON_DEMAND
    );
    private EnumSet<JavaElement> whitespaceAfter = EnumSet.of(
        ABSTRACT,
        AND__EXPR,
        AND__TYPE_BOUND,
        AND_ASSIGN,
        ASSERT,
        ASSIGN__ASSIGNMENT,
        ASSIGN__VAR_DECL,
        CASE,
        CATCH,
        CLASS__CLASS_DECL,
        COLON__CASE,
        COLON__DEFAULT,
        COLON__ENHANCED_FOR,
        COLON__LABELED_STAT,
        COLON__TERNARY,
        COMMA,
        CONDITIONAL_AND,
        CONDITIONAL_OR,
        DEFAULT__ANNO_ELEM,
        DIVIDE,
        DIVIDE_ASSIGN,
        DO,
        ELLIPSIS,
        ELSE,
        ENUM,
        EQUAL,
        EXTENDS__TYPE,
        EXTENDS__TYPE_BOUND,
        FINAL,
        FINALLY,
        FOR,
        GREATER,
        GREATER_EQUAL,
        IF,
        IMPLEMENTS,
        IMPORT,
        IMPORT__STATIC_IMPORT,
        INSTANCEOF,
        INTERFACE,
        L_CURLY__ANNO_ARRAY_INIT,
        L_CURLY__ANON_CLASS,
        L_CURLY__ARRAY_INIT,
        L_CURLY__BLOCK,
        L_CURLY__CATCH,
        L_CURLY__DO,
        L_CURLY__ENUM_CONST,
        L_CURLY__FINALLY,
        L_CURLY__FOR,
        L_CURLY__IF,
        L_CURLY__INSTANCE_INIT,
        L_CURLY__LABELED_STAT,
        L_CURLY__METH_DECL,
        L_CURLY__STATIC_INIT,
        L_CURLY__SWITCH,
        L_CURLY__SYNCHRONIZED,
        L_CURLY__TRY,
        L_CURLY__TYPE_DECL,
        L_CURLY__WHILE,
        LEFT_SHIFT,
        LEFT_SHIFT_ASSIGN,
        LESS,
        LESS_EQUAL,
        MINUS__ADDITIVE,
        MINUS_ASSIGN,
        MODULO,
        MODULO_ASSIGN,
        MULTIPLY,
        MULTIPLY_ASSIGN,
        NAME__ANNO_MEMBER,
        NATIVE,
        NEW,
        NOT_EQUAL,
        OR,
        OR_ASSIGN,
        PACKAGE,
        PLUS__ADDITIVE,
        PLUS_ASSIGN,
        PRIVATE,
        PROTECTED,
        PUBLIC,
        QUESTION__TERNARY,
        R_ANGLE__METH_DECL_TYPE_PARAMS,
        R_CURLY__BLOCK,
        R_CURLY__CATCH,
        R_CURLY__DO,
        R_CURLY__ELSE,
        R_CURLY__EMPTY_CATCH,
        R_CURLY__EMPTY_METH_DECL,
        R_CURLY__EMPTY_TYPE_DECL,
        R_CURLY__FINALLY,
        R_CURLY__FOR,
        R_CURLY__IF,
        R_CURLY__INSTANCE_INIT,
        R_CURLY__LABELED_STAT,
        R_CURLY__METH_DECL,
        R_CURLY__STATIC_INIT,
        R_CURLY__SWITCH,
        R_CURLY__SYNCHRONIZED,
        R_CURLY__TRY,
        R_CURLY__TYPE_DECL,
        R_CURLY__WHILE,
        R_PAREN__CAST,
        R_PAREN__CATCH,
        R_PAREN__IF,
        RETURN__EXPR,
        RIGHT_SHIFT,
        RIGHT_SHIFT_ASSIGN,
        SEMI__ABSTRACT_METH_DECL,
        SEMI__ANNO_ELEM_DECL,
        SEMI__EMPTY_STAT,
        SEMI__ENUM_DECL,
        SEMI__FIELD_DECL,
        SEMI__FOR_CONDITION_UPDATE,
        SEMI__FOR_INIT_CONDITION,
        SEMI__FOR_NO_CONDITION_UPDATE,
        SEMI__FOR_NO_INIT_CONDITION,
        SEMI__IMPORT,
        SEMI__PACKAGE_DECL,
        SEMI__STATEMENT,
        SEMI__STATIC_IMPORT,
        SEMI__TYPE_DECL,
        STATIC__MOD,
        STATIC__STATIC_IMPORT,
        STATIC__STATIC_INIT,
        SUPER__TYPE_BOUND,
        SWITCH,
        SYNCHRONIZED__MOD,
        SYNCHRONIZED__SYNCHRONIZED,
        THROW,
        THROWS,
        TRANSIENT,
        TRY,
        UNSIGNED_RIGHT_SHIFT,
        UNSIGNED_RIGHT_SHIFT_ASSIGN,
        VOLATILE,
        WHILE__DO,
        WHILE__WHILE,
        XOR,
        XOR_ASSIGN
    );
    private EnumSet<JavaElement> noWhitespaceAfter = EnumSet.of(
        AT__ANNO,
        AT__ANNO_DECL,
        BITWISE_COMPLEMENT,
        DEFAULT__SWITCH,
        DOT__IMPORT,
        DOT__PACKAGE_DECL,
        DOT__QUALIFIED_TYPE,
        DOT__SELECTOR,
        L_ANGLE__METH_DECL_TYPE_PARAMS,
        L_ANGLE__METH_INVOCATION_TYPE_ARGS,
        L_ANGLE__TYPE_ARGS,
        L_ANGLE__TYPE_PARAMS,
        L_BRACK__ARRAY_DECL,
        L_BRACK__INDEX,
        L_CURLY__EMPTY_ANNO_ARRAY_INIT,
        L_CURLY__EMPTY_ANON_CLASS,
        L_CURLY__EMPTY_ARRAY_INIT,
        L_CURLY__EMPTY_CATCH,
        L_CURLY__EMPTY_METH_DECL,
        L_CURLY__EMPTY_TYPE_DECL,
        L_PAREN__ANNO,
        L_PAREN__ANNO_ELEM_DECL,
        L_PAREN__METH_INVOCATION,
        L_PAREN__CAST,
        L_PAREN__CATCH,
        L_PAREN__DO_WHILE,
        L_PAREN__FOR,
        L_PAREN__FOR_NO_INIT,
        L_PAREN__IF,
        L_PAREN__PARAMS,
        L_PAREN__PARENTHESIZED,
        LOGICAL_COMPLEMENT,
        MINUS__UNARY,
        NAME__ANNO_ELEM_DECL,
        NAME__CTOR_DECL,
        NAME__IMPORT_COMPONENT,
        NAME__IMPORT_TYPE,
        NAME__METH_DECL,
        NAME__PACKAGE_DECL,
        NAME__PARAM,
        PLUS__UNARY,
        PRE_DECR,
        PRE_INCR,
        R_ANGLE__METH_INVOCATION_TYPE_ARGS,
        R_PAREN__DO_WHILE,
        RETURN__NO_EXPR,
        SEMI__FOR_CONDITION_NO_UPDATE,
        SEMI__FOR_INIT_NO_CONDITION,
        SEMI__FOR_NO_CONDITION_NO_UPDATE,
        SEMI__FOR_NO_INIT_NO_CONDITION,
        STAR__TYPE_IMPORT_ON_DEMAND,
        SUPER__CTOR_CALL,
        SUPER__EXPR,
        THIS__CTOR_CALL
    );

    // BEGIN CONFIGURATION SETTERS

    // SUPPRESS CHECKSTYLE JavadocMethod:5
    // SUPPRESS CHECKSTYLE LineLength:4
    public void setWhitespaceBefore(String[] sa)   { this.whitespaceBefore   = Whitespace.toEnumSet(sa, JavaElement.class); }
    public void setNoWhitespaceBefore(String[] sa) { this.noWhitespaceBefore = Whitespace.toEnumSet(sa, JavaElement.class); }
    public void setWhitespaceAfter(String[] sa)    { this.whitespaceAfter    = Whitespace.toEnumSet(sa, JavaElement.class); }
    public void setNoWhitespaceAfter(String[] sa)  { this.noWhitespaceAfter  = Whitespace.toEnumSet(sa, JavaElement.class); }

    // END CONFIGURATION SETTERS

    private static <E extends Enum<E>> E
    toEnum(String s, Class<E> enumType) {
        try {
            return Enum.valueOf(enumType, s.trim().toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new ConversionException("Unable to parse " + s, iae);
        }
    }

    private static <E extends Enum<E>> EnumSet<E>
    toEnumSet(String[] values, Class<E> enumClass) {
        EnumSet<E> result = EnumSet.noneOf(enumClass);
        for (String value : values) result.add(Whitespace.toEnum(value, enumClass));
        return result;
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        @SuppressWarnings("unused") AstDumper dumper = new AstDumper(ast); // For debugging

        JavaElement javaElement = AstUtil.toJavaElement(ast);

        if (javaElement == null) {
            return;
        }

//        log(ast, "CHECK {0}={1} => {2}", ast, ast.getType(), whitespaceable);

        boolean mustBeWhitespaceBefore    = this.whitespaceBefore.contains(javaElement);
        boolean mustNotBeWhitespaceBefore = this.noWhitespaceBefore.contains(javaElement);

        boolean mustBeWhitespaceAfter    = this.whitespaceAfter.contains(javaElement);
        boolean mustNotBeWhitespaceAfter = this.noWhitespaceAfter.contains(javaElement);

        // Short-circuit.
        if (
            !mustBeWhitespaceBefore
            && !mustNotBeWhitespaceBefore
            && !mustBeWhitespaceAfter
            && !mustNotBeWhitespaceAfter
        ) return;

        final String line = this.getLines()[ast.getLineNo() - 1];

        // Check whitespace BEFORE token.
        if (mustBeWhitespaceBefore || mustNotBeWhitespaceBefore) {
            int before = ast.getColumnNo() - 1;

            if (before > 0 && !Whitespace.LINE_PREFIX.matcher(line).region(0, before).matches()) {
                boolean isWhitespace = Character.isWhitespace(line.charAt(before));
                if (mustBeWhitespaceBefore && !isWhitespace) {
                    this.log(ast, "de.unkrig.cscontrib.checks.Whitespace.notPreceded", ast.getText(), javaElement);
                } else
                if (mustNotBeWhitespaceBefore && isWhitespace) {
                    this.log(ast, "de.unkrig.cscontrib.checks.Whitespace.preceded", ast.getText(), javaElement);
                }
            }
        }

        // Check whitespace AFTER token.
        if (mustBeWhitespaceAfter || mustNotBeWhitespaceAfter) {
            int after = ast.getColumnNo() + ast.getText().length();

            if (after < line.length() && !Whitespace.LINE_SUFFIX.matcher(line).region(after, line.length()).matches()) {
                boolean isWhitespace = Character.isWhitespace(line.charAt(after));
                if (mustBeWhitespaceAfter && !isWhitespace) {
                    this.log(
                        ast.getLineNo(),
                        after,
                        "de.unkrig.cscontrib.checks.Whitespace.notFollowed",
                        ast.getText(),
                        javaElement
                    );
                } else
                if (mustNotBeWhitespaceAfter && isWhitespace) {
                    this.log(
                        ast.getLineNo(),
                        after,
                        "de.unkrig.cscontrib.checks.Whitespace.followed",
                        ast.getText(),
                        javaElement
                    );
                }
            }
        }
    }
    private static final Pattern LINE_PREFIX = Pattern.compile("\\s*");
    private static final Pattern LINE_SUFFIX = Pattern.compile("\\s*(?://.*)?");

    @Override public int[]
    getDefaultTokens() {
        return new int[] {
            TokenTypes.ABSTRACT,
            TokenTypes.ANNOTATION,
            TokenTypes.ANNOTATION_ARRAY_INIT,
            TokenTypes.ANNOTATION_DEF,
            TokenTypes.ANNOTATION_FIELD_DEF,
            TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR,
            TokenTypes.ANNOTATIONS,
            TokenTypes.ARRAY_DECLARATOR,
            TokenTypes.ARRAY_INIT,
            TokenTypes.ASSIGN,
            TokenTypes.AT,
            TokenTypes.BAND,
            TokenTypes.BAND_ASSIGN,
            TokenTypes.BNOT,
            TokenTypes.BOR,
            TokenTypes.BOR_ASSIGN,
            TokenTypes.BSR,
            TokenTypes.BSR_ASSIGN,
            TokenTypes.BXOR,
            TokenTypes.BXOR_ASSIGN,
            TokenTypes.CASE_GROUP,
            TokenTypes.CHAR_LITERAL,
            TokenTypes.CLASS_DEF,
            TokenTypes.COLON,
            TokenTypes.COMMA,
            TokenTypes.CTOR_CALL,
            TokenTypes.CTOR_DEF,
            TokenTypes.DEC,
            TokenTypes.DIV,
            TokenTypes.DIV_ASSIGN,
            TokenTypes.DO_WHILE,
            TokenTypes.DOT,
            TokenTypes.ELIST,
            TokenTypes.ELLIPSIS,
            TokenTypes.EMPTY_STAT,
            TokenTypes.ENUM,
            TokenTypes.ENUM_CONSTANT_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.EOF,
            TokenTypes.EQUAL,
            TokenTypes.EXPR,
            TokenTypes.EXTENDS_CLAUSE,
            TokenTypes.FINAL,
            TokenTypes.FOR_CONDITION,
            TokenTypes.FOR_EACH_CLAUSE,
            TokenTypes.FOR_INIT,
            TokenTypes.FOR_ITERATOR,
            TokenTypes.GE,
            TokenTypes.GENERIC_END,
            TokenTypes.GENERIC_START,
            TokenTypes.GT,
            TokenTypes.IDENT,
            TokenTypes.IMPLEMENTS_CLAUSE,
            TokenTypes.IMPORT,
            TokenTypes.INC,
            TokenTypes.INDEX_OP,
            TokenTypes.INSTANCE_INIT,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.LABELED_STAT,
            TokenTypes.LAND,
            TokenTypes.LCURLY,
            TokenTypes.LE,
            TokenTypes.LITERAL_ASSERT,
            TokenTypes.LITERAL_BOOLEAN,
            TokenTypes.LITERAL_BREAK,
            TokenTypes.LITERAL_BYTE,
            TokenTypes.LITERAL_CASE,
            TokenTypes.LITERAL_CATCH,
            TokenTypes.LITERAL_CHAR,
            TokenTypes.LITERAL_CLASS,
            TokenTypes.LITERAL_CONTINUE,
            TokenTypes.LITERAL_DEFAULT,
            TokenTypes.LITERAL_DO,
            TokenTypes.LITERAL_DOUBLE,
            TokenTypes.LITERAL_ELSE,
            TokenTypes.LITERAL_FALSE,
            TokenTypes.LITERAL_FINALLY,
            TokenTypes.LITERAL_FLOAT,
            TokenTypes.LITERAL_FOR,
            TokenTypes.LITERAL_IF,
            TokenTypes.LITERAL_INSTANCEOF,
            TokenTypes.LITERAL_INT,
            TokenTypes.LITERAL_INTERFACE,
            TokenTypes.LITERAL_LONG,
            TokenTypes.LITERAL_NATIVE,
            TokenTypes.LITERAL_NEW,
            TokenTypes.LITERAL_NULL,
            TokenTypes.LITERAL_PRIVATE,
            TokenTypes.LITERAL_PROTECTED,
            TokenTypes.LITERAL_PUBLIC,
            TokenTypes.LITERAL_RETURN,
            TokenTypes.LITERAL_SHORT,
            TokenTypes.LITERAL_STATIC,
            TokenTypes.LITERAL_SUPER,
            TokenTypes.LITERAL_SWITCH,
            TokenTypes.LITERAL_SYNCHRONIZED,
            TokenTypes.LITERAL_THIS,
            TokenTypes.LITERAL_THROW,
            TokenTypes.LITERAL_THROWS,
            TokenTypes.LITERAL_TRANSIENT,
            TokenTypes.LITERAL_TRUE,
            TokenTypes.LITERAL_TRY,
            TokenTypes.LITERAL_VOID,
            TokenTypes.LITERAL_VOLATILE,
            TokenTypes.LITERAL_WHILE,
            TokenTypes.LNOT,
            TokenTypes.LOR,
            TokenTypes.LPAREN,
            TokenTypes.LT,
            TokenTypes.METHOD_CALL,
            TokenTypes.METHOD_DEF,
            TokenTypes.MINUS,
            TokenTypes.MINUS_ASSIGN,
            TokenTypes.MOD,
            TokenTypes.MOD_ASSIGN,
            TokenTypes.MODIFIERS,
            TokenTypes.NOT_EQUAL,
            TokenTypes.NUM_DOUBLE,
            TokenTypes.NUM_FLOAT,
            TokenTypes.NUM_INT,
            TokenTypes.NUM_LONG,
            TokenTypes.OBJBLOCK,
            TokenTypes.PACKAGE_DEF,
            TokenTypes.PARAMETER_DEF,
            TokenTypes.PARAMETERS,
            TokenTypes.PLUS,
            TokenTypes.PLUS_ASSIGN,
            TokenTypes.POST_DEC,
            TokenTypes.POST_INC,
            TokenTypes.QUESTION,
            TokenTypes.RBRACK,
            TokenTypes.RCURLY,
            TokenTypes.RESOURCE,
            TokenTypes.RESOURCE_SPECIFICATION,
            TokenTypes.RESOURCES,
            TokenTypes.RPAREN,
            TokenTypes.SEMI,
            TokenTypes.SL,
            TokenTypes.SL_ASSIGN,
            TokenTypes.SLIST,
            TokenTypes.SR,
            TokenTypes.SR_ASSIGN,
            TokenTypes.STAR,
            TokenTypes.STAR_ASSIGN,
            TokenTypes.STATIC_IMPORT,
            TokenTypes.STATIC_INIT,
            TokenTypes.STRICTFP,
            TokenTypes.STRING_LITERAL,
            TokenTypes.SUPER_CTOR_CALL,
            TokenTypes.TYPE,
            TokenTypes.TYPE_ARGUMENT,
            TokenTypes.TYPE_ARGUMENTS,
            TokenTypes.TYPE_EXTENSION_AND,
            TokenTypes.TYPE_LOWER_BOUNDS,
            TokenTypes.TYPE_PARAMETER,
            TokenTypes.TYPE_PARAMETERS,
            TokenTypes.TYPE_UPPER_BOUNDS,
            TokenTypes.TYPECAST,
            TokenTypes.UNARY_MINUS,
            TokenTypes.UNARY_PLUS,
            TokenTypes.VARIABLE_DEF,
            TokenTypes.WILDCARD_TYPE,
        };
    }
}

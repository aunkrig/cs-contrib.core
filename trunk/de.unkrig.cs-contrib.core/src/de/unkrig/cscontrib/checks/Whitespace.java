
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

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
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
        return LocalTokenType.delocalize(new LocalTokenType[] {
            LocalTokenType.ABSTRACT,
            LocalTokenType.ANNOTATION,
            LocalTokenType.ANNOTATION_ARRAY_INIT,
            LocalTokenType.ANNOTATION_DEF,
            LocalTokenType.ANNOTATION_FIELD_DEF,
            LocalTokenType.ANNOTATION_MEMBER_VALUE_PAIR,
            LocalTokenType.ANNOTATIONS,
            LocalTokenType.ARRAY_DECLARATOR,
            LocalTokenType.ARRAY_INIT,
            LocalTokenType.ASSIGN,
            LocalTokenType.AT,
            LocalTokenType.BAND,
            LocalTokenType.BAND_ASSIGN,
            LocalTokenType.BNOT,
            LocalTokenType.BOR,
            LocalTokenType.BOR_ASSIGN,
            LocalTokenType.BSR,
            LocalTokenType.BSR_ASSIGN,
            LocalTokenType.BXOR,
            LocalTokenType.BXOR_ASSIGN,
            LocalTokenType.CASE_GROUP,
            LocalTokenType.CHAR_LITERAL,
            LocalTokenType.CLASS_DEF,
            LocalTokenType.COLON,
            LocalTokenType.COMMA,
            LocalTokenType.CTOR_CALL,
            LocalTokenType.CTOR_DEF,
            LocalTokenType.DEC,
            LocalTokenType.DIV,
            LocalTokenType.DIV_ASSIGN,
            LocalTokenType.DO_WHILE,
            LocalTokenType.DOT,
            LocalTokenType.ELIST,
            LocalTokenType.ELLIPSIS,
            LocalTokenType.EMPTY_STAT,
            LocalTokenType.ENUM,
            LocalTokenType.ENUM_CONSTANT_DEF,
            LocalTokenType.ENUM_DEF,
            LocalTokenType.EOF,
            LocalTokenType.EQUAL,
            LocalTokenType.EXPR,
            LocalTokenType.EXTENDS_CLAUSE,
            LocalTokenType.FINAL,
            LocalTokenType.FOR_CONDITION,
            LocalTokenType.FOR_EACH_CLAUSE,
            LocalTokenType.FOR_INIT,
            LocalTokenType.FOR_ITERATOR,
            LocalTokenType.GE,
            LocalTokenType.GENERIC_END,
            LocalTokenType.GENERIC_START,
            LocalTokenType.GT,
            LocalTokenType.IDENT,
            LocalTokenType.IMPLEMENTS_CLAUSE,
            LocalTokenType.IMPORT,
            LocalTokenType.INC,
            LocalTokenType.INDEX_OP,
            LocalTokenType.INSTANCE_INIT,
            LocalTokenType.INTERFACE_DEF,
            LocalTokenType.LABELED_STAT,
            LocalTokenType.LAND,
            LocalTokenType.LCURLY,
            LocalTokenType.LE,
            LocalTokenType.LITERAL_ASSERT,
            LocalTokenType.LITERAL_BOOLEAN,
            LocalTokenType.LITERAL_BREAK,
            LocalTokenType.LITERAL_BYTE,
            LocalTokenType.LITERAL_CASE,
            LocalTokenType.LITERAL_CATCH,
            LocalTokenType.LITERAL_CHAR,
            LocalTokenType.LITERAL_CLASS,
            LocalTokenType.LITERAL_CONTINUE,
            LocalTokenType.LITERAL_DEFAULT,
            LocalTokenType.LITERAL_DO,
            LocalTokenType.LITERAL_DOUBLE,
            LocalTokenType.LITERAL_ELSE,
            LocalTokenType.LITERAL_FALSE,
            LocalTokenType.LITERAL_FINALLY,
            LocalTokenType.LITERAL_FLOAT,
            LocalTokenType.LITERAL_FOR,
            LocalTokenType.LITERAL_IF,
            LocalTokenType.LITERAL_INSTANCEOF,
            LocalTokenType.LITERAL_INT,
            LocalTokenType.LITERAL_INTERFACE,
            LocalTokenType.LITERAL_LONG,
            LocalTokenType.LITERAL_NATIVE,
            LocalTokenType.LITERAL_NEW,
            LocalTokenType.LITERAL_NULL,
            LocalTokenType.LITERAL_PRIVATE,
            LocalTokenType.LITERAL_PROTECTED,
            LocalTokenType.LITERAL_PUBLIC,
            LocalTokenType.LITERAL_RETURN,
            LocalTokenType.LITERAL_SHORT,
            LocalTokenType.LITERAL_STATIC,
            LocalTokenType.LITERAL_SUPER,
            LocalTokenType.LITERAL_SWITCH,
            LocalTokenType.LITERAL_SYNCHRONIZED,
            LocalTokenType.LITERAL_THIS,
            LocalTokenType.LITERAL_THROW,
            LocalTokenType.LITERAL_THROWS,
            LocalTokenType.LITERAL_TRANSIENT,
            LocalTokenType.LITERAL_TRUE,
            LocalTokenType.LITERAL_TRY,
            LocalTokenType.LITERAL_VOID,
            LocalTokenType.LITERAL_VOLATILE,
            LocalTokenType.LITERAL_WHILE,
            LocalTokenType.LNOT,
            LocalTokenType.LOR,
            LocalTokenType.LPAREN,
            LocalTokenType.LT,
            LocalTokenType.METHOD_CALL,
            LocalTokenType.METHOD_DEF,
            LocalTokenType.MINUS,
            LocalTokenType.MINUS_ASSIGN,
            LocalTokenType.MOD,
            LocalTokenType.MOD_ASSIGN,
            LocalTokenType.MODIFIERS,
            LocalTokenType.NOT_EQUAL,
            LocalTokenType.NUM_DOUBLE,
            LocalTokenType.NUM_FLOAT,
            LocalTokenType.NUM_INT,
            LocalTokenType.NUM_LONG,
            LocalTokenType.OBJBLOCK,
            LocalTokenType.PACKAGE_DEF,
            LocalTokenType.PARAMETER_DEF,
            LocalTokenType.PARAMETERS,
            LocalTokenType.PLUS,
            LocalTokenType.PLUS_ASSIGN,
            LocalTokenType.POST_DEC,
            LocalTokenType.POST_INC,
            LocalTokenType.QUESTION,
            LocalTokenType.RBRACK,
            LocalTokenType.RCURLY,
            LocalTokenType.RESOURCE,
            LocalTokenType.RESOURCE_SPECIFICATION,
            LocalTokenType.RESOURCES,
            LocalTokenType.RPAREN,
            LocalTokenType.SEMI,
            LocalTokenType.SL,
            LocalTokenType.SL_ASSIGN,
            LocalTokenType.SLIST,
            LocalTokenType.SR,
            LocalTokenType.SR_ASSIGN,
            LocalTokenType.STAR,
            LocalTokenType.STAR_ASSIGN,
            LocalTokenType.STATIC_IMPORT,
            LocalTokenType.STATIC_INIT,
            LocalTokenType.STRICTFP,
            LocalTokenType.STRING_LITERAL,
            LocalTokenType.SUPER_CTOR_CALL,
            LocalTokenType.TYPE,
            LocalTokenType.TYPE_ARGUMENT,
            LocalTokenType.TYPE_ARGUMENTS,
            LocalTokenType.TYPE_EXTENSION_AND,
            LocalTokenType.TYPE_LOWER_BOUNDS,
            LocalTokenType.TYPE_PARAMETER,
            LocalTokenType.TYPE_PARAMETERS,
            LocalTokenType.TYPE_UPPER_BOUNDS,
            LocalTokenType.TYPECAST,
            LocalTokenType.UNARY_MINUS,
            LocalTokenType.UNARY_PLUS,
            LocalTokenType.VARIABLE_DEF,
            LocalTokenType.WILDCARD_TYPE,
        });
    }
}

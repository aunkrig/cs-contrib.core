
/*
 * cs-contrib - Additional checks, filters and quickfixes for CheckStyle and Eclipse-CS
 *
 * Copyright (c) 2014, Arno Unkrig
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

package de.unkrig.cscontrib;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import de.unkrig.commons.nullanalysis.Nullable;

/**
 * An internal ('local') representation of CheckStyle's {@link TokenTypes}. The reason being is that the values of the
 * integer constants declared in {@link TokenTypes} changed (slightly!) between CheckStyle 5.7 and 6.1, so they cannot
 * be used if one wants to be compatible with CS 5.7 <i>and</i> 6.1.
 */
public
enum LocalTokenType {
    // CHECKSTYLE JavadocVariable:OFF

    // The following tokens exist both in CHECKSTYLE 5.7 and 6.1:

    ABSTRACT,
    ANNOTATION,
    ANNOTATIONS,
    ANNOTATION_ARRAY_INIT,
    ANNOTATION_DEF,
    ANNOTATION_FIELD_DEF,
    ANNOTATION_MEMBER_VALUE_PAIR,
    ARRAY_DECLARATOR,
    ARRAY_INIT,
    ASSIGN,
    AT,
    BAND,
    BAND_ASSIGN,
    BNOT,
    BOR,
    BOR_ASSIGN,
    BSR,
    BSR_ASSIGN,
    BXOR,
    BXOR_ASSIGN,
    CASE_GROUP,
    CHAR_LITERAL,
    CLASS_DEF,
    COLON,
    COMMA,
    CTOR_CALL,
    CTOR_DEF,
    DEC,
    DIV,
    DIV_ASSIGN,
    DOT,
    DO_WHILE,
    ELIST,
    ELLIPSIS,
    EMPTY_STAT,
    ENUM,
    ENUM_CONSTANT_DEF,
    ENUM_DEF,
    EOF,
    EQUAL,
    EXPR,
    EXTENDS_CLAUSE,
    FINAL,
    FOR_CONDITION,
    FOR_EACH_CLAUSE,
    FOR_INIT,
    FOR_ITERATOR,
    GE,
    GENERIC_END,
    GENERIC_START,
    GT,
    IDENT,
    IMPLEMENTS_CLAUSE,
    IMPORT,
    INC,
    INDEX_OP,
    INSTANCE_INIT,
    INTERFACE_DEF,
    LABELED_STAT,
    LAND,
    LCURLY,
    LE,
    LITERAL_ASSERT,
    LITERAL_BOOLEAN,
    LITERAL_BREAK,
    LITERAL_BYTE,
    LITERAL_CASE,
    LITERAL_CATCH,
    LITERAL_CHAR,
    LITERAL_CLASS,
    LITERAL_CONTINUE,
    LITERAL_DEFAULT,
    LITERAL_DO,
    LITERAL_DOUBLE,
    LITERAL_ELSE,
    LITERAL_FALSE,
    LITERAL_FINALLY,
    LITERAL_FLOAT,
    LITERAL_FOR,
    LITERAL_IF,
    LITERAL_INSTANCEOF,
    LITERAL_INT,
    LITERAL_INTERFACE,
    LITERAL_LONG,
    LITERAL_NATIVE,
    LITERAL_NEW,
    LITERAL_NULL,
    LITERAL_PRIVATE,
    LITERAL_PROTECTED,
    LITERAL_PUBLIC,
    LITERAL_RETURN,
    LITERAL_SHORT,
    LITERAL_STATIC,
    LITERAL_SUPER,
    LITERAL_SWITCH,
    LITERAL_SYNCHRONIZED,
    LITERAL_THIS,
    LITERAL_THROW,
    LITERAL_THROWS,
    LITERAL_TRANSIENT,
    LITERAL_TRUE,
    LITERAL_TRY,
    LITERAL_VOID,
    LITERAL_VOLATILE,
    LITERAL_WHILE,
    LNOT,
    LOR,
    LPAREN,
    LT,
    METHOD_CALL,
    METHOD_DEF,
    MINUS,
    MINUS_ASSIGN,
    MOD,
    MODIFIERS,
    MOD_ASSIGN,
    NOT_EQUAL,
    NUM_DOUBLE,
    NUM_FLOAT,
    NUM_INT,
    NUM_LONG,
    OBJBLOCK,
    PACKAGE_DEF,
    PARAMETERS,
    PARAMETER_DEF,
    PLUS,
    PLUS_ASSIGN,
    POST_DEC,
    POST_INC,
    QUESTION,
    RBRACK,
    RCURLY,
    RESOURCE,
    RESOURCES,
    RESOURCE_SPECIFICATION,
    RPAREN,
    SEMI,
    SL,
    SLIST,
    SL_ASSIGN,
    SR,
    SR_ASSIGN,
    STAR,
    STAR_ASSIGN,
    STATIC_IMPORT,
    STATIC_INIT,
    STRICTFP,
    STRING_LITERAL,
    SUPER_CTOR_CALL,
    TYPE,
    TYPECAST,
    TYPE_ARGUMENT,
    TYPE_ARGUMENTS,
    TYPE_EXTENSION_AND,
    TYPE_LOWER_BOUNDS,
    TYPE_PARAMETER,
    TYPE_PARAMETERS,
    TYPE_UPPER_BOUNDS,
    UNARY_MINUS,
    UNARY_PLUS,
    VARIABLE_DEF,
    WILDCARD_TYPE,

    // The following token types were removed somewhere between CHECKSTYLE 5.7, 6.1:

//    BLOCK,
//    ESC,
//    EXPONENT,
//    FLOAT_SUFFIX,
//    HEX_DIGIT,
//    LBRACK,
//    LITERAL_EXTENDS,
//    LITERAL_IMPLEMENTS,
//    LITERAL_IMPORT,
//    LITERAL_PACKAGE,
//    ML_COMMENT,
//    NULL_TREE_LOOKAHEAD,
//    SL_COMMENT,
//    VOCAB,
//    WS,
    ;

    // CHECKSTYLE JavadocVariable:ON

    private static final Map<Integer, LocalTokenType> TO_LOCAL;
    private static final Map<LocalTokenType, Integer> FROM_LOCAL;
    static {
        Map<Integer, LocalTokenType> toLocal   = new HashMap<Integer, LocalTokenType>();
        Map<LocalTokenType, Integer> fromLocal = new HashMap<LocalTokenType, Integer>();

        for (Field f : TokenTypes.class.getFields()) {
            String tn = LocalTokenType.notNull(f.getName());

            // Skip fields which are obviously not a token type.
            // Notice: Intentionally do not check for const'ness, because that could change in future CHECKSTYLE
            // versions.
            if (
                !Modifier.isStatic(f.getModifiers())
                || !LocalTokenType.isConstantName(tn)
                || f.getType() != Integer.TYPE
            ) continue;

            // Find the LocalTokenType corresponding with the TokenType.
            LocalTokenType ltt;
            try {
                ltt = LocalTokenType.valueOf(tn);
            } catch (IllegalArgumentException iae) {

                // Ignore any tokens that newer CHECKSTYLE versions add.
                continue;
            }

            Integer tt;
            try {
                tt = (Integer) f.get(null);
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }

            // Populate the maps.
            toLocal.put(tt, ltt);
            fromLocal.put(ltt, tt);
        }

        TO_LOCAL   = LocalTokenType.notNull(Collections.unmodifiableMap(toLocal));
        FROM_LOCAL = LocalTokenType.notNull(Collections.unmodifiableMap(fromLocal));
    }

    /**
     * @return The {@link LocalTokenType} corresponding with the given {@link TokenTypes} constant, or {@code null}
     *         iff {@code tt} is a token type that was introduced <i>after</i> CheckStyle 6.1
     */
    @Nullable public static LocalTokenType
    localize(int tt) { return LocalTokenType.TO_LOCAL.get(tt); }

    /**
     * @return The {@link TokenTypes} corresponding with the given {@link LocalTokenType} constant, or -1
     *         iff {@code ltt} has no counterpart in this CheckStyle version
     */
    public int
    delocalize() {
        Integer result = LocalTokenType.FROM_LOCAL.get(this);
        return result == null ? -1 : result;
    }

    /** @return The value of the constant delcared in {@link TokenTypes} that maps the given {@code ltt} */
    public static int[]
    delocalize(LocalTokenType[] ltts) {

        int[] tts = new int[ltts.length];
        for (int i = 0; i < tts.length; i++) {

            LocalTokenType ltt = ltts[i];
            assert ltt != null;
            tts[i] = ltt.delocalize();
        }

        return tts;
    }

    private static boolean
    isConstantName(String name) {

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isUpperCase(c) && !Character.isDigit(c) && c != '_') return false;
        }

        return true;
    }

    private static <T> T
    notNull(@Nullable T object) {

        assert object != null : String.valueOf(object);

        return object;
    }
}

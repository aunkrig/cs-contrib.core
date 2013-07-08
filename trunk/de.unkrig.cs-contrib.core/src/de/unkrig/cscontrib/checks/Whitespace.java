
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

import static de.unkrig.cscontrib.checks.Whitespace.Whitespaceable.*;

import java.util.EnumSet;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConversionException;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

/**
 * An enhanced replacement for all other '*Whitespace*' checks.
 */
@NotNullByDefault(false) public
class Whitespace extends Check {

    /**
     * The elements that must or must not be preceded and/or followed by whitespace.
     */
    public
    enum Whitespaceable {
        /** 'a + b', 'a - b' */
        ADDITIVE_OPERATORS,
        /** 'int a = 7;' */
        ASSIGN_VARIABLE_DEF,
        /**
         * 'a &= b', 'a |= b', 'a >>>= b', 'a ^= b', ' a /= b', 'a -= b', 'a %= b', 'a += b', 'a <<= b', 'a >>= b',
         * 'a *= b'
         */
        ASSIGNS,
        /** '@MyAnno' */
        AT_ANNOTATION,
        /** 'interface @MyAnno {' */
        AT_ANNOTATION_DEF,
        /** 'a & b', 'a | b', 'a ^ b' */
        BITWISE_OPERATORS,
        /** '~a' */
        BITWISE_COMPLEMENT,
        /** '(int) a' */
        CAST,
        /** 'default:' */
        COLON_DEFAULT,
        /** 'case 77:' */
        COLON_CASE,
        /** 'for (Object o : list) {' */
        COLON_ENHANCED_FOR,
        /** 'LABEL: while (...) {' */
        COLON_LABELED_STAT,
        /** 'a ? b : c' */
        COLON_TERNARY,
        /** ',' */
        COMMA,
        /** 'a || b', 'a && b' */
        CONDITIONAL_OPERATORS,
        /** 'import pkg.*;', 'import pkg.Type;' */
        DOT_IMPORT,
        /** 'package pkg.pkg;' */
        DOT_PACKAGE_DEF,
        /** 'pkg.MyType', 'pkg.MyType[]' */
        DOT_QUALIFIED_TYPE,
        /** 'a.b', 'a().b' */
        DOT_SELECTOR,
        /** 'meth(Object... o)' */
        ELLIPSIS_PARAMETER,
        /** ';' */
        EMPTY_STAT,
        /** 'a < b', 'a <= b', 'a == b', 'a != b', 'a >= b', 'a > b' */
        EQUALITY_OPERATORS,
        /** 'class MyClass extends BaseClass {' */
        EXTENDS_TYPE,
        /** 'List<T extends MyClass>' */
        EXTENDS_TYPE_BOUND,
        /** 'List<T implements MyInterface1, MyInterface2>' */
        IMPLEMENTS,
        /** 'List<String>' */
        L_ANGLE,
        /** 'Object[]' */
        L_BRACK_ARRAY_DECL,
        /** 'a[3]' */
        L_BRACK_INDEX,
        /** '@SuppressWarnings({ "foo", "bar" })' */
        L_CURLY_ANNOTATION_ARRAY_INIT,
        /** 'int[] ia = { 1, 2 }', 'new int[] { 1, 2 }' */
        L_CURLY_ARRAY_INIT,
        /** '{ int i = 0; i++; }' */
        L_CURLY_BLOCK,
        /** 'try { ... } catch (...) { ...' */
        L_CURLY_CATCH,
        /** 'do { ...' */
        L_CURLY_DO,
        /** '@SuppressWarnings({})' */
        L_CURLY_EMPTY_ANNOTATION_ARRAY_INIT,
        /** 'int[] ia = {}', 'new int[] {}' */
        L_CURLY_EMPTY_ARRAY_INIT,
        /** 'void meth(...) {}' */
        L_CURLY_EMPTY_METHOD_DEF,
        /** 'class MyClass {}' */
        L_CURLY_EMPTY_TYPE_DEF,
        /** 'enum MyEnum { FOO { ... } }' */
        L_CURLY_ENUM_CONSTANT_DEF,
        L_CURLY_FINALLY,
        L_CURLY_FOR,
        L_CURLY_IF,
        L_CURLY_INSTANCE_INIT,
        L_CURLY_LABELED_STAT,
        /** 'void meth(...) { ... }' */
        L_CURLY_METHOD_DEF,
        L_CURLY_STATIC_INIT,
        L_CURLY_SWITCH,
        L_CURLY_SYNCHRONIZED,
        L_CURLY_TRY,
        /** 'class MyClass { ... }' */
        L_CURLY_TYPE_DEF,
        L_CURLY_WHILE,
        L_PAREN_ANNOTATION,
        L_PAREN_ANNOTATION_FIELD_DEF,
        L_PAREN_CALL,
        L_PAREN_DO_WHILE,
        L_PAREN_FOR,
        L_PAREN_FOR_NO_INIT,
        L_PAREN_IF,
        L_PAREN_PARAMETERS,
        L_PAREN_PARENTHESIZED,
        LITERAL,
        LOGICAL_COMPLEMENT,
        MINUS_UNARY,
        MULTIPLICATIVE_OPERATORS,
        NAME_AMBIGUOUS,
        NAME_ANNOTATION,
        NAME_ANNOTATION_FIELD_DEF,
        NAME_ANNOTATION_MEMBER,
        NAME_CTOR_DEF,
        NAME_IMPORT_COMPONENT,
        NAME_IMPORT_TYPE,
        NAME_METHOD_DEF,
        NAME_PACKAGE_DEF,
        NAME_PARAMETER,
        NAME_QUALIFIED_TYPE,
        NAME_SIMPLE_TYPE,
        NAME_TYPE_DEF,
        NAME_VARIABLE_DEF,
        OTHER_KEYWORDS,
        PLUS_UNARY,
        POST_DEC,
        POST_INC,
        PRE_DEC,
        PRE_INC,
        PRIMITIVE_TYPE,
        QUESTION_TERNARY,
        QUESTION_WILDCARD_TYPE,
        R_ANGLE,
        /** 'Object[]' */
        R_BRACK_ARRAY_DECL,
        /** '@SuppressWarnings({ "foo", "bar" })' */
        R_CURLY_ANNOTATION_ARRAY_INIT,
        R_CURLY_ANON_CLASS,
        /** 'int[] ia = { 1, 2 }', 'new int[] { 1, 2 }' */
        R_CURLY_ARRAY_INIT,
        /** '{ int i = 0; i++; }' */
        R_CURLY_BLOCK,
        /** 'try { ... } catch (...) { ...' */
        R_CURLY_CATCH,
        /** 'do { ... } while (...);' */
        R_CURLY_DO_WHILE,
        R_CURLY_ELSE,
        /** '@SuppressWarnings({})' */
        R_CURLY_EMPTY_ANNOTATION_ARRAY_INIT,
        R_CURLY_EMPTY_ANON_CLASS,
        /** 'int[] ia = {}', 'new int[] {}' */
        R_CURLY_EMPTY_ARRAY_INIT,
        /** 'try { ... } catch (...) {}' */
        R_CURLY_EMPTY_CATCH,
        /** 'void meth(...) {}' */
        R_CURLY_EMPTY_METHOD_DEF,
        /** 'class MyClass {}' */
        R_CURLY_EMPTY_TYPE_DEF,
        /** 'enum MyEnum { FOO { ... } }' */
        R_CURLY_ENUM_CONSTANT_DEF,
        R_CURLY_FINALLY,
        R_CURLY_FOR,
        R_CURLY_IF,
        R_CURLY_INSTANCE_INIT,
        R_CURLY_LABELED_STAT,
        /** 'void meth(...) { ... }' */
        R_CURLY_METHOD_DEF,
        R_CURLY_STATIC_INIT,
        R_CURLY_SWITCH,
        R_CURLY_SYNCHRONIZED,
        R_CURLY_TRY,
        /** 'class MyClass { ... }' */
        R_CURLY_TYPE_DEF,
        R_CURLY_WHILE,
        R_PAREN_ANNOTATION,
        R_PAREN_ANNOTATION_FIELD_DEF,
        R_PAREN_CALL,
        R_PAREN_DO_WHILE,
        R_PAREN_FOR,
        R_PAREN_FOR_NO_UPDATE,
        R_PAREN_IF,
        R_PAREN_PARAMETERS,
        R_PAREN_PARENTHESIZED,
        RETURN_EXPR,
        RETURN_NO_EXPR,
        SEMI_ABSTRACT_METH_DEF,
        SEMI_ANNOTATION_FIELD_DEF,
        SEMI_ENUM_DEF,
        SEMI_FIELD_DEF,
        SEMI_FOR_CONDITION_NO_UPDATE,
        SEMI_FOR_CONDITION_UPDATE,
        SEMI_FOR_INIT_CONDITION,
        SEMI_FOR_INIT_NO_CONDITION,
        SEMI_FOR_NO_CONDITION_NO_UPDATE,
        SEMI_FOR_NO_CONDITION_UPDATE,
        SEMI_FOR_NO_INIT_CONDITION,
        SEMI_FOR_NO_INIT_NO_CONDITION,
        SEMI_IMPORT,
        SEMI_PACKAGE_DEF,
        SEMI_STATEMENT,
        SEMI_STATIC_IMPORT,
        SHIFT_OPERATORS,
        STAR_TYPE_IMPORT_ON_DEMAND,
        SUPER_CTOR_CALL,
        /** 'List<T super MyClass>' */
        SUPER_TYPE_BOUND,
        THIS_CTOR_CALL,
    }

    private EnumSet<Whitespaceable> whitespaceBefore = EnumSet.of(
        ASSIGN_VARIABLE_DEF,
        ASSIGNS,
        BITWISE_OPERATORS,
        COLON_ENHANCED_FOR,
        COLON_TERNARY,
        L_CURLY_DO,
        EQUALITY_OPERATORS,
        L_CURLY_ARRAY_INIT,
        L_CURLY_METHOD_DEF,
        L_CURLY_STATIC_INIT,
        L_CURLY_TYPE_DEF,
        L_PAREN_DO_WHILE,
        L_PAREN_FOR,
        MULTIPLICATIVE_OPERATORS,
        NAME_TYPE_DEF,
        NAME_VARIABLE_DEF,
        OTHER_KEYWORDS,
        R_CURLY_ANON_CLASS,
        R_CURLY_ARRAY_INIT,
        /** 'catch (Exception e) { ... }' */
        R_CURLY_CATCH,
        R_CURLY_METHOD_DEF,
        R_CURLY_STATIC_INIT,
        R_CURLY_TYPE_DEF,
        RETURN_EXPR,
        RETURN_NO_EXPR
    );
    private EnumSet<Whitespaceable> noWhitespaceBefore = EnumSet.of(
        COLON_DEFAULT,
        COLON_CASE,
        COMMA,
        DOT_IMPORT,
        DOT_PACKAGE_DEF,
        EMPTY_STAT,
        R_ANGLE,
        L_BRACK_ARRAY_DECL,
        L_PAREN_ANNOTATION,
        L_PAREN_CALL,
        L_PAREN_PARAMETERS,
        NAME_ANNOTATION,
        R_BRACK_ARRAY_DECL,
        R_PAREN_ANNOTATION,
        R_PAREN_CALL,
        R_PAREN_DO_WHILE,
        R_PAREN_FOR,
        R_PAREN_PARAMETERS,
        SEMI_ABSTRACT_METH_DEF,
        SEMI_FOR_CONDITION_NO_UPDATE,
        SEMI_FOR_CONDITION_UPDATE,
        SEMI_FOR_INIT_CONDITION,
        SEMI_FOR_INIT_NO_CONDITION,
        SEMI_IMPORT,
        SEMI_PACKAGE_DEF,
        SEMI_STATEMENT
    );
    private EnumSet<Whitespaceable> whitespaceAfter = EnumSet.of(
        ASSIGN_VARIABLE_DEF,
        ASSIGNS,
        BITWISE_OPERATORS,
        COLON_DEFAULT,
        COLON_CASE,
        COLON_ENHANCED_FOR,
        COLON_TERNARY,
        COMMA,
        L_CURLY_DO,
        EMPTY_STAT,
        EQUALITY_OPERATORS,
        L_CURLY_ARRAY_INIT,
        L_CURLY_METHOD_DEF,
        L_CURLY_STATIC_INIT,
        L_CURLY_TYPE_DEF,
        MULTIPLICATIVE_OPERATORS,
        NAME_TYPE_DEF,
        OTHER_KEYWORDS,
        R_CURLY_METHOD_DEF,
        R_CURLY_STATIC_INIT,
        R_CURLY_TYPE_DEF,
        R_PAREN_FOR,
        RETURN_EXPR,
        SEMI_ABSTRACT_METH_DEF,
        SEMI_FOR_CONDITION_UPDATE,
        SEMI_FOR_INIT_CONDITION,
        SEMI_IMPORT,
        SEMI_PACKAGE_DEF,
        SEMI_STATEMENT
    );
    private EnumSet<Whitespaceable> noWhitespaceAfter = EnumSet.of(
        AT_ANNOTATION,
        BITWISE_COMPLEMENT,
        DOT_IMPORT,
        DOT_PACKAGE_DEF,
        L_ANGLE,
        NAME_IMPORT_COMPONENT,
        NAME_IMPORT_TYPE,
        PRE_DEC,
        L_BRACK_ARRAY_DECL,
        L_PAREN_ANNOTATION,
        L_PAREN_CALL,
        L_PAREN_DO_WHILE,
        L_PAREN_FOR,
        L_PAREN_PARAMETERS,
        RETURN_NO_EXPR,
        SEMI_FOR_CONDITION_NO_UPDATE,
        SEMI_FOR_INIT_NO_CONDITION
    );

//    public
//    enum Compactable {
//        EMPTY_ANNOTATION_ARRAY_INIT,
//        EMPTY_ANON_CLASS,
//        EMPTY_ARRAY_INIT,
//        EMPTY_CATCH_BLOCK,
//        EMPTY_FOR_CONDITION,
//        EMPTY_FOR_INIT,
//        EMPTY_FOR_UPDATE,
//        EMPTY_METHOD,
//        EMPTY_TYPE,
//    }
//
//    private EnumSet<Compactable> compactables = EnumSet.of(
//        EMPTY_ANON_CLASS,
//        EMPTY_ARRAY_INIT,
//        EMPTY_CATCH_BLOCK,
//        EMPTY_FOR_CONDITION,
//        EMPTY_FOR_INIT,
//        EMPTY_FOR_UPDATE,
//        EMPTY_ARRAY_INIT,
//        EMPTY_METHOD,
//        EMPTY_TYPE
//    );

    // BEGIN CONFIGURATION SETTERS

    // CHECKSTYLE JavadocMethod:OFF
    public void setWhitespaceBefore(String[] sa)   { this.whitespaceBefore   = toEnumSet(sa, Whitespaceable.class); }
    public void setNoWhitespaceBefore(String[] sa) { this.noWhitespaceBefore = toEnumSet(sa, Whitespaceable.class); }
    public void setWhitespaceAfter(String[] sa)    { this.whitespaceAfter    = toEnumSet(sa, Whitespaceable.class); }
    public void setNoWhitespaceAfter(String[] sa)  { this.noWhitespaceAfter  = toEnumSet(sa, Whitespaceable.class); }
    // CHECKSTYLE JavadocMethod:ON

    // END CONFIGURATION SETTERS

    private static <E extends Enum<E>> E
    toEnum(String value, Class<E> enumClass) {
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new ConversionException("Unable to parse " + value, iae);
        }
    }
    
    private static <E extends Enum<E>> EnumSet<E>
    toEnumSet(String[] values, Class<E> enumClass) {
        EnumSet<E> result = EnumSet.noneOf(enumClass);
        for (String value : values) result.add(toEnum(value, enumClass));
        return result;
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        @SuppressWarnings("unused") AstDumper dumper = new AstDumper(ast); // For debugging

        final int parentType, grandparentType, previousSiblingType, nextSiblingType, firstChildType;
        {
            DetailAST parent = ast.getParent();
            if (parent == null) {
                parentType      = -1;
                grandparentType = -1;
            } else {
                parentType = parent.getType();
                DetailAST grandparent = parent.getParent();
                grandparentType = grandparent == null ? -1 : grandparent.getType();
            }

            DetailAST previousSibling = ast.getPreviousSibling();
            previousSiblingType = previousSibling == null ? -1 : previousSibling.getType();
            
            DetailAST nextSibling = ast.getNextSibling();
            nextSiblingType = nextSibling == null ? -1 : nextSibling.getType();

            DetailAST firstChild = ast.getFirstChild();
            firstChildType = firstChild == null ? -1 : firstChild.getType();
        }

        // Find out how this token is to be checked.
        Whitespaceable whitespaceable = null;
        switch (ast.getType()) {

        case TokenTypes.ARRAY_DECLARATOR:
            whitespaceable = L_BRACK_ARRAY_DECL;
            break;
        case TokenTypes.ARRAY_INIT:
            whitespaceable = firstChildType == TokenTypes.RCURLY ? L_CURLY_EMPTY_ARRAY_INIT : L_CURLY_ARRAY_INIT;
            break;

        case TokenTypes.ASSIGN:
            if (parentType == TokenTypes.VARIABLE_DEF) {
                whitespaceable = ASSIGN_VARIABLE_DEF;
                break;
            }
            // FALLTHROUGH
        case TokenTypes.BAND_ASSIGN:
        case TokenTypes.BOR_ASSIGN:
        case TokenTypes.BSR_ASSIGN:
        case TokenTypes.BXOR_ASSIGN:
        case TokenTypes.DIV_ASSIGN:
        case TokenTypes.MINUS_ASSIGN:
        case TokenTypes.MOD_ASSIGN:
        case TokenTypes.PLUS_ASSIGN:
        case TokenTypes.SL_ASSIGN:
        case TokenTypes.SR_ASSIGN:
        case TokenTypes.STAR_ASSIGN:
            whitespaceable = ASSIGNS;
            break;

        case TokenTypes.AT:
            if (parentType == TokenTypes.ANNOTATION) {
                whitespaceable = AT_ANNOTATION;
            } else
            if (parentType == TokenTypes.ANNOTATION_DEF) {
                whitespaceable = AT_ANNOTATION_DEF;
            }
            break;

        case TokenTypes.BAND:
        case TokenTypes.BOR:
        case TokenTypes.BXOR:
            whitespaceable = BITWISE_OPERATORS;
            break;

        case TokenTypes.BNOT:
            whitespaceable = BITWISE_COMPLEMENT;
            break;

        case TokenTypes.LOR: // 'a || b'
        case TokenTypes.LAND: // 'a && b'
            whitespaceable = CONDITIONAL_OPERATORS;
            break;

        case TokenTypes.LNOT: // '!a'
            whitespaceable = LOGICAL_COMPLEMENT;
            break;

        case TokenTypes.TYPECAST:
            whitespaceable = CAST;
            break;

        case TokenTypes.COLON:
            if (parentType == TokenTypes.LITERAL_DEFAULT) {
                whitespaceable = COLON_DEFAULT;
            } else
            if (parentType == TokenTypes.LITERAL_CASE) {
                whitespaceable = COLON_CASE;
            } else
            if (parentType == TokenTypes.FOR_EACH_CLAUSE) {
                whitespaceable = COLON_ENHANCED_FOR;
            } else
            {
                whitespaceable = COLON_TERNARY;
            }
            break;

        case TokenTypes.COMMA:
            whitespaceable = COMMA;
            break;

        case TokenTypes.DEC:
            whitespaceable = PRE_DEC; // '--x'
            break;

        case TokenTypes.INC:
            whitespaceable = PRE_INC; // '++x'
            break;

        case TokenTypes.POST_DEC:
            whitespaceable = POST_DEC; // 'x--'
            break;

        case TokenTypes.POST_INC:
            whitespaceable = POST_INC; // 'x++'
            break;

        case TokenTypes.STAR:
            if (parentType == TokenTypes.DOT) { // 'import pkg.pkg.*;'
                whitespaceable = STAR_TYPE_IMPORT_ON_DEMAND;
                break;
            }                  // 'a * b'
            // FALLTHROUGH
        case TokenTypes.DIV:   // 'a / b'
        case TokenTypes.MOD:   // 'a % b'
            whitespaceable = MULTIPLICATIVE_OPERATORS;
            break;

        case TokenTypes.PLUS:
        case TokenTypes.MINUS:
            whitespaceable = ADDITIVE_OPERATORS;
            break;


        case TokenTypes.UNARY_MINUS:
            whitespaceable = MINUS_UNARY;
            break;

        case TokenTypes.UNARY_PLUS:
            whitespaceable = PLUS_UNARY;
            break;

        case TokenTypes.DOT:
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.PACKAGE_DEF) {
                whitespaceable = DOT_PACKAGE_DEF;
            } else
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.IMPORT) {
                whitespaceable = DOT_IMPORT;
            } else
            if (
                getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR, TokenTypes.DOT) == TokenTypes.TYPE
            ) {
                whitespaceable = DOT_QUALIFIED_TYPE;
            } else
            {
                whitespaceable = DOT_SELECTOR;
            }
            break;

        case TokenTypes.EMPTY_STAT: // ';'
            whitespaceable = EMPTY_STAT;
            break;

        case TokenTypes.LT:
        case TokenTypes.LE:
        case TokenTypes.EQUAL:
        case TokenTypes.NOT_EQUAL:
        case TokenTypes.GE:
        case TokenTypes.GT:
            whitespaceable = EQUALITY_OPERATORS;
            break;

        case TokenTypes.GENERIC_END:
            whitespaceable = R_ANGLE;
            break;

        case TokenTypes.GENERIC_START:
            whitespaceable = L_ANGLE;
            break;

        case TokenTypes.IDENT:
            if (
                parentType == TokenTypes.CLASS_DEF                                       // 'class MyClass {'
                || parentType == TokenTypes.INTERFACE_DEF                                // 'interface MyInterface {'
                || parentType == TokenTypes.ANNOTATION_DEF                               // 'interface @MyAnnotation {'
                || parentType == TokenTypes.ENUM_DEF                                     // 'enum MyEnum {'
            ) {
                whitespaceable = NAME_TYPE_DEF;
            } else
            if (parentType == TokenTypes.ANNOTATION) {                                   // '@MyAnnotation("x")'
                whitespaceable = NAME_ANNOTATION;
            } else
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) {
                whitespaceable = NAME_ANNOTATION_FIELD_DEF;
            } else
            if (parentType == TokenTypes.VARIABLE_DEF) {                                 // 'int a;'
                whitespaceable = NAME_VARIABLE_DEF;
            } else
            if (parentType == TokenTypes.CTOR_DEF) {                                     // 'MyClass(...) {'
                whitespaceable = NAME_CTOR_DEF;
            } else
            if (parentType == TokenTypes.METHOD_DEF) {                                   // 'void main(...) {'
                whitespaceable = NAME_METHOD_DEF;
            } else
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.PACKAGE_DEF) { // 'package pkg.pkg.pkg;'
                whitespaceable = NAME_PACKAGE_DEF;
            } else
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.IMPORT) {      // 'import pkg.pkg.*;'
                whitespaceable = ast.getNextSibling() == null ? NAME_IMPORT_TYPE : NAME_IMPORT_COMPONENT;
            } else
            if (
                getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR) == TokenTypes.TYPE           // 'MyType'
                || getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR) == TokenTypes.LITERAL_NEW // 'new MyType'
            ) {
                whitespaceable = NAME_SIMPLE_TYPE;
            } else
            if (                                                                         // 'pkg.MyType'
                getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR, TokenTypes.DOT) == TokenTypes.TYPE
            ) {
                whitespaceable = NAME_QUALIFIED_TYPE;
            } else
            if (parentType == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {               // '@MyAnnotation(value = "x")'
                whitespaceable = NAME_ANNOTATION_MEMBER;
            } else
            if (parentType == TokenTypes.PARAMETER_DEF) {                               // 'meth(String parm)'
                whitespaceable = NAME_PARAMETER;
            } else
            {
                whitespaceable = NAME_AMBIGUOUS;                                             // 'a.b.c'
            }
            break;

        case TokenTypes.LCURLY:
            if (parentType == TokenTypes.OBJBLOCK && (
                grandparentType == TokenTypes.CLASS_DEF         // 'class MyClass() {...}'
                || grandparentType == TokenTypes.INTERFACE_DEF  // 'interface MyInterface() {...}'
                || grandparentType == TokenTypes.ANNOTATION_DEF // 'interface @MyAnnotation {...}'
                || grandparentType == TokenTypes.ENUM_DEF       // 'enum MyEnum {...}'
                || grandparentType == TokenTypes.LITERAL_NEW    // 'new MyClass() {...}'
            )) {
                whitespaceable = nextSiblingType == TokenTypes.RCURLY ? L_CURLY_EMPTY_TYPE_DEF : L_CURLY_TYPE_DEF;
            } else
            if ( // 'enum MyEnum { CONST {'
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.ENUM_CONSTANT_DEF
            ) {
                whitespaceable = L_CURLY_ENUM_CONSTANT_DEF;
            } else
            if (parentType == TokenTypes.ARRAY_INIT) {
                whitespaceable = nextSiblingType == TokenTypes.RCURLY ? L_CURLY_EMPTY_ARRAY_INIT : L_CURLY_ARRAY_INIT;
            } else
            if (parentType == TokenTypes.LITERAL_SWITCH) { // 'switch {'
                whitespaceable = L_CURLY_SWITCH;
            }
            break;

        case TokenTypes.ANNOTATION_ARRAY_INIT:
            whitespaceable  = firstChildType == TokenTypes.RCURLY ? L_CURLY_EMPTY_ANNOTATION_ARRAY_INIT : L_CURLY_ANNOTATION_ARRAY_INIT;
            break;

        case TokenTypes.INDEX_OP:
            whitespaceable = L_BRACK_INDEX;
            break;

        case TokenTypes.IMPLEMENTS_CLAUSE:
            whitespaceable = IMPLEMENTS;
            break;

        case TokenTypes.LITERAL_BYTE:
        case TokenTypes.LITERAL_INT:
        case TokenTypes.LITERAL_FLOAT:
        case TokenTypes.LITERAL_BOOLEAN:
        case TokenTypes.LITERAL_CHAR:
        case TokenTypes.LITERAL_LONG:
        case TokenTypes.LITERAL_DOUBLE:
        case TokenTypes.LITERAL_SHORT:
            whitespaceable = PRIMITIVE_TYPE;
            break;

        case TokenTypes.CHAR_LITERAL:
        case TokenTypes.LITERAL_FALSE:
        case TokenTypes.LITERAL_TRUE:
        case TokenTypes.LITERAL_NULL:
        case TokenTypes.NUM_DOUBLE:
        case TokenTypes.NUM_FLOAT:
        case TokenTypes.NUM_INT:
        case TokenTypes.NUM_LONG:
        case TokenTypes.STRING_LITERAL:
            whitespaceable = LITERAL;
            break;

        case TokenTypes.ABSTRACT:
        case TokenTypes.DO_WHILE: // '... } while (...);'
        case TokenTypes.ENUM:
        case TokenTypes.FINAL:
        case TokenTypes.IMPORT:   // 'import ...'
        case TokenTypes.LITERAL_ASSERT:
        case TokenTypes.LITERAL_BREAK:
        case TokenTypes.LITERAL_CASE:
        case TokenTypes.LITERAL_CATCH:
        case TokenTypes.LITERAL_CLASS:
        case TokenTypes.LITERAL_CONTINUE:
        case TokenTypes.LITERAL_DEFAULT:
        case TokenTypes.LITERAL_DO:
        case TokenTypes.LITERAL_ELSE:
        case TokenTypes.LITERAL_FINALLY:
        case TokenTypes.LITERAL_FOR:
        case TokenTypes.LITERAL_IF:
        case TokenTypes.LITERAL_INSTANCEOF:
        case TokenTypes.LITERAL_INTERFACE:
        case TokenTypes.LITERAL_NATIVE:
        case TokenTypes.LITERAL_NEW:
        case TokenTypes.LITERAL_PRIVATE:
        case TokenTypes.LITERAL_PROTECTED:
        case TokenTypes.LITERAL_PUBLIC:
        case TokenTypes.LITERAL_STATIC:
        case TokenTypes.LITERAL_SUPER:
        case TokenTypes.LITERAL_SWITCH:
        case TokenTypes.LITERAL_SYNCHRONIZED:
        case TokenTypes.LITERAL_THIS:
        case TokenTypes.LITERAL_THROW:
        case TokenTypes.LITERAL_THROWS:
        case TokenTypes.LITERAL_TRANSIENT:
        case TokenTypes.LITERAL_TRY:
        case TokenTypes.LITERAL_VOID:
        case TokenTypes.LITERAL_VOLATILE:
        case TokenTypes.LITERAL_WHILE: // 'while (...) {'
        case TokenTypes.PACKAGE_DEF:   // 'package ...'
        case TokenTypes.STATIC_IMPORT: // '_import_ static ...' 
        case TokenTypes.STATIC_INIT:   // 'static {'
            whitespaceable = OTHER_KEYWORDS;
            break;

        case TokenTypes.LITERAL_RETURN:
            if (firstChildType == TokenTypes.SEMI) { // 'return;'
                whitespaceable = RETURN_NO_EXPR;
                break;
            } else { // 'return x;'
                whitespaceable = RETURN_EXPR;
                break;
            }

        case TokenTypes.LPAREN:
            if (parentType == TokenTypes.ANNOTATION) {
                whitespaceable = L_PAREN_ANNOTATION;
            } else
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) {
                whitespaceable = L_PAREN_ANNOTATION_FIELD_DEF;
            } else
            if (nextSiblingType == TokenTypes.PARAMETERS) {
                whitespaceable = L_PAREN_PARAMETERS;
            } else
            if (parentType == TokenTypes.SUPER_CTOR_CALL || parentType == TokenTypes.LITERAL_NEW) {
                whitespaceable = L_PAREN_CALL;
            } else
            if (parentType == TokenTypes.LITERAL_DO) {
                whitespaceable = L_PAREN_DO_WHILE;
            } else
            if (parentType == TokenTypes.LITERAL_IF) {
                whitespaceable = L_PAREN_IF;
            } else
            if (parentType == TokenTypes.LITERAL_FOR) {
                whitespaceable = ast.getNextSibling().getFirstChild() == null ? L_PAREN_FOR_NO_INIT : L_PAREN_FOR;
            } else
            {
                whitespaceable = L_PAREN_PARENTHESIZED;
            }
            break;

        case TokenTypes.METHOD_CALL:
            whitespaceable = L_PAREN_CALL;
            break;

        case TokenTypes.METHOD_DEF:
        case TokenTypes.MODIFIERS:
        case TokenTypes.OBJBLOCK:
        case TokenTypes.PARAMETER_DEF:
        case TokenTypes.PARAMETERS:
            break;

        case TokenTypes.QUESTION:
            whitespaceable = QUESTION_TERNARY;
            break;

        case TokenTypes.RBRACK:
            whitespaceable = R_BRACK_ARRAY_DECL;
            break;

        case TokenTypes.RCURLY:
            if ( // 'new MyClass() {...}'
                parentType == TokenTypes.OBJBLOCK
                && grandparentType == TokenTypes.LITERAL_NEW
            ) {
                whitespaceable = previousSiblingType == TokenTypes.LCURLY ? R_CURLY_EMPTY_ANON_CLASS : R_CURLY_ANON_CLASS;
            } else
            if ( // 'catch (Exception e) {...}'
                parentType == TokenTypes.SLIST
                && grandparentType == TokenTypes.LITERAL_CATCH
            ) {
                whitespaceable = ast.getPreviousSibling() == null ? R_CURLY_EMPTY_CATCH : R_CURLY_CATCH;
            } else
            if ( // 'synchronized (...) { ... }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_SYNCHRONIZED
            ) {
                whitespaceable = R_CURLY_SYNCHRONIZED;
            } else
            if ( // 'class MyClass {...}', 'interface MyInterface {...}', '@interface MyAnnotation {...}'
                parentType == TokenTypes.OBJBLOCK
                && (
                    grandparentType == TokenTypes.CLASS_DEF
                    || grandparentType == TokenTypes.INTERFACE_DEF
                    || grandparentType == TokenTypes.ANNOTATION_DEF
                    || grandparentType == TokenTypes.ENUM_DEF
                )
            ) {
                whitespaceable = previousSiblingType == TokenTypes.LCURLY ? R_CURLY_EMPTY_TYPE_DEF : R_CURLY_TYPE_DEF;
            } else
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.ENUM_CONSTANT_DEF
            ) {
                whitespaceable = R_CURLY_ENUM_CONSTANT_DEF;
            } else
            if ( // 'public MyClass(...) {...}', 'public method(...) {...}'
                parentType == TokenTypes.SLIST
                && (grandparentType == TokenTypes.CTOR_DEF || grandparentType == TokenTypes.METHOD_DEF)
            ) {
                whitespaceable = ast.getPreviousSibling() == null ? R_CURLY_EMPTY_METHOD_DEF : R_CURLY_METHOD_DEF;
            } else
            if ( // 'for (...) { ... }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_FOR
            ) {
                whitespaceable = R_CURLY_FOR;
            } else
            if ( // 'if (...) { ... }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_IF
            ) {
                whitespaceable = R_CURLY_IF;
            } else
            if ( // 'else { ... }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_ELSE
            ) {
                whitespaceable = R_CURLY_IF;
            } else
            if ( // 'while (...) { ... }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_WHILE
            ) {
                whitespaceable = R_CURLY_WHILE;
            } else
            if ( // 'do { ... } while (...);'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_DO
            ) {
                whitespaceable = R_CURLY_DO_WHILE;
            } else
            if ( // 'try { ... }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_TRY
            ) {
                whitespaceable = R_CURLY_TRY;
            } else
            if ( // 'finally { ... }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_FINALLY
            ) {
                whitespaceable = R_CURLY_FINALLY;
            } else
            if ( // 'LABEL: { ... }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LABELED_STAT
            ) {
                whitespaceable = R_CURLY_LABELED_STAT;
            } else
            if ( // 'meth() { { ... } }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.SLIST
            ) {
                whitespaceable = R_CURLY_BLOCK;
            } else
            if (parentType == TokenTypes.LITERAL_SWITCH) { // 'switch { ... }'
                whitespaceable = R_CURLY_SWITCH;
            } else
            if (parentType == TokenTypes.ARRAY_INIT) { // 'Object[] oa = { ... }', 'new Object[] { ... }'
                whitespaceable = ast.getPreviousSibling() == null ? R_CURLY_EMPTY_ARRAY_INIT : R_CURLY_ARRAY_INIT;
            } else
            if (parentType == TokenTypes.ANNOTATION_ARRAY_INIT) { // '@MyAnno({ x, y })'
                whitespaceable = ast.getPreviousSibling() == null ? R_CURLY_EMPTY_ANNOTATION_ARRAY_INIT : R_CURLY_ANNOTATION_ARRAY_INIT;
            } else
            if ( // 'class MyClass { static { ... } }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.STATIC_INIT
            ) {
                whitespaceable = R_CURLY_STATIC_INIT;
            } else
            if ( // 'class MyClass { { ... } }'
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.INSTANCE_INIT
            ) {
                whitespaceable = R_CURLY_INSTANCE_INIT;
            }
            break;

        case TokenTypes.RESOURCE:
        case TokenTypes.RESOURCE_SPECIFICATION:
        case TokenTypes.RESOURCES:
            break;

        case TokenTypes.RPAREN:
            if (parentType == TokenTypes.ANNOTATION) {
                whitespaceable = R_PAREN_ANNOTATION;
            } else
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) {
                whitespaceable = R_PAREN_ANNOTATION_FIELD_DEF;
            } else
            if (parentType == TokenTypes.CTOR_DEF || parentType == TokenTypes.METHOD_DEF) {
                whitespaceable = R_PAREN_PARAMETERS;
            } else
            if (
                parentType == TokenTypes.SUPER_CTOR_CALL
                || parentType == TokenTypes.LITERAL_NEW
                || parentType == TokenTypes.METHOD_CALL
            ) {
                whitespaceable = R_PAREN_CALL;
            } else
            if (parentType == TokenTypes.LITERAL_IF) {
                whitespaceable = R_PAREN_IF;
            } else
            if (parentType == TokenTypes.LITERAL_DO) {
                whitespaceable = R_PAREN_DO_WHILE;
            } else
            if (parentType == TokenTypes.LITERAL_FOR) {
                whitespaceable = ast.getPreviousSibling().getFirstChild() == null ? R_PAREN_FOR_NO_UPDATE : R_PAREN_FOR;
            } else
            {
                whitespaceable = R_PAREN_PARENTHESIZED;
            }
            break;

        case TokenTypes.SEMI:
            if (parentType == TokenTypes.PACKAGE_DEF) {
                whitespaceable = SEMI_PACKAGE_DEF;
            } else
            if (parentType == TokenTypes.IMPORT) {
                whitespaceable = SEMI_IMPORT;
            } else
            if (parentType == TokenTypes.STATIC_IMPORT) {
                whitespaceable = SEMI_STATIC_IMPORT;
            } else
            if (
                parentType == TokenTypes.SLIST
                || parentType == TokenTypes.SUPER_CTOR_CALL
                || parentType == TokenTypes.CTOR_CALL
                || parentType == TokenTypes.LITERAL_DO
                || parentType == TokenTypes.LITERAL_RETURN
                || parentType == TokenTypes.LITERAL_BREAK
                || parentType == TokenTypes.LITERAL_CONTINUE
                || parentType == TokenTypes.LITERAL_IF
                || parentType == TokenTypes.LITERAL_FOR
                || parentType == TokenTypes.LITERAL_WHILE
                || parentType == TokenTypes.LITERAL_ASSERT
                || parentType == TokenTypes.LITERAL_THROW
            ) {
                whitespaceable = SEMI_STATEMENT;
            } else
            if (parentType == TokenTypes.METHOD_DEF) {
                whitespaceable = SEMI_ABSTRACT_METH_DEF;
            } else
            if (previousSiblingType == TokenTypes.FOR_INIT) {
                whitespaceable = ast.getPreviousSibling().getFirstChild() == null ? (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI_FOR_NO_INIT_NO_CONDITION       // 'for (;;...'
                    : SEMI_FOR_NO_INIT_CONDITION          // 'for (; ...'
                ) : (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI_FOR_INIT_NO_CONDITION          // 'for (int i = 0;;...'
                    : SEMI_FOR_INIT_CONDITION             // 'for (int i = 0; i < 3;...'
                );
            } else
            if (previousSiblingType == TokenTypes.FOR_CONDITION) {
                whitespaceable = ast.getPreviousSibling().getFirstChild() == null ? (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI_FOR_NO_CONDITION_NO_UPDATE     // 'for (...;;) {'
                    : SEMI_FOR_NO_CONDITION_UPDATE        // 'for (...;; i++) {'
                ) : (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI_FOR_CONDITION_NO_UPDATE        // 'for (...; i < 3;) {'
                    : SEMI_FOR_CONDITION_UPDATE           // 'for (...; i < 3; i++) {'
                );
            } else
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) {
                whitespaceable = SEMI_ANNOTATION_FIELD_DEF;
            } else
            if ( // 'enum MyEnum { 1, B, C; ... }'
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.ENUM_DEF
            ) {
                whitespaceable = SEMI_ENUM_DEF;
            } else
            if (parentType == TokenTypes.VARIABLE_DEF && grandparentType == TokenTypes.OBJBLOCK) {
                whitespaceable = SEMI_FIELD_DEF;
            }
            break;

        case TokenTypes.SLIST:
            if (parentType == TokenTypes.STATIC_INIT) { // 'class MyClass { static { ... } }
                whitespaceable = L_CURLY_STATIC_INIT;
            } else
            if (parentType == TokenTypes.INSTANCE_INIT) { // 'class MyClass { { ... } }
                whitespaceable = L_CURLY_INSTANCE_INIT;
            } else
            if (parentType == TokenTypes.LITERAL_IF) { // 'if (...) { ... }'
                whitespaceable = L_CURLY_IF;
            } else
            if (parentType == TokenTypes.LITERAL_ELSE) { // 'else { ... }'
                whitespaceable = R_CURLY_ELSE;
            } else
            if (parentType == TokenTypes.LITERAL_DO) { // 'do { ... } while (...)'
                whitespaceable = L_CURLY_DO;
            } else
            if (parentType == TokenTypes.LITERAL_WHILE) { // 'while (...) {'
                whitespaceable = L_CURLY_WHILE;
            } else
            if (parentType == TokenTypes.LITERAL_FOR) { // 'for (...) {'
                whitespaceable = L_CURLY_FOR;
            } else
            if (parentType == TokenTypes.LITERAL_TRY) { // 'try {'
                whitespaceable = L_CURLY_TRY;
            } else
            if (parentType == TokenTypes.LITERAL_CATCH) { // 'catch (...) {'
                whitespaceable = L_CURLY_CATCH;
            } else
            if (parentType == TokenTypes.LITERAL_FINALLY) { // 'finally {'
                whitespaceable = L_CURLY_FINALLY;
            } else
            if (parentType == TokenTypes.LITERAL_SYNCHRONIZED) { // 'synchronized (...) {'
                whitespaceable = L_CURLY_SYNCHRONIZED;
            } else
            if (parentType == TokenTypes.LABELED_STAT) { // 'LABEL: {'
                whitespaceable = L_CURLY_LABELED_STAT;
            } else
            if (parentType == TokenTypes.SLIST) { // '{ ... }'
                whitespaceable = L_CURLY_BLOCK;
            } else
            if (parentType == TokenTypes.CTOR_DEF || parentType == TokenTypes.METHOD_DEF) {
                whitespaceable = firstChildType == TokenTypes.RCURLY ? Whitespaceable.L_CURLY_EMPTY_METHOD_DEF : Whitespaceable.L_CURLY_METHOD_DEF;
            }
            break;

        case TokenTypes.SL:
        case TokenTypes.SR:
        case TokenTypes.BSR:
            whitespaceable = SHIFT_OPERATORS;
            break;
            
        case TokenTypes.ELLIPSIS:
            whitespaceable = ELLIPSIS_PARAMETER;
            break;

        case TokenTypes.TYPE:
            break;
            
        case TokenTypes.CTOR_CALL:
            whitespaceable = THIS_CTOR_CALL;
            break;

        case TokenTypes.SUPER_CTOR_CALL:
            whitespaceable = SUPER_CTOR_CALL;
            break;

        case TokenTypes.TYPE_UPPER_BOUNDS:
            whitespaceable = EXTENDS_TYPE_BOUND;
            break;

        case TokenTypes.TYPE_LOWER_BOUNDS:
            whitespaceable = SUPER_TYPE_BOUND;
            break;

        case TokenTypes.WILDCARD_TYPE:
            whitespaceable = QUESTION_WILDCARD_TYPE;
            break;

        case TokenTypes.EXTENDS_CLAUSE:
            whitespaceable = EXTENDS_TYPE;
            break;
            
        case TokenTypes.LABELED_STAT:
            whitespaceable = COLON_LABELED_STAT;
            break;
            
        case TokenTypes.STRICTFP:
        case TokenTypes.TYPE_ARGUMENT:
        case TokenTypes.TYPE_ARGUMENTS:
        case TokenTypes.TYPE_EXTENSION_AND:
        case TokenTypes.TYPE_PARAMETER:
        case TokenTypes.TYPE_PARAMETERS:
        case TokenTypes.VARIABLE_DEF:
            break;

        default:
            assert false;
        }

        if (whitespaceable == null) {
            return;
        }

//        log(ast, "CHECK {0}={1} => {2}", ast, ast.getType(), whitespaceable);

        boolean mustBeWhitespaceBefore    = this.whitespaceBefore.contains(whitespaceable);
        boolean mustNotBeWhitespaceBefore = this.noWhitespaceBefore.contains(whitespaceable);

        boolean mustBeWhitespaceAfter    = this.whitespaceAfter.contains(whitespaceable);
        boolean mustNotBeWhitespaceAfter = this.noWhitespaceAfter.contains(whitespaceable);

        // Short-circuit.
        if (
            !mustBeWhitespaceBefore
            && !mustNotBeWhitespaceBefore
            && !mustBeWhitespaceAfter
            && !mustNotBeWhitespaceAfter
        ) return;

        final String[] lines = getLines();
        final String   line  = lines[ast.getLineNo() - 1];

        // Check whitespace BEFORE token.
        if (mustBeWhitespaceBefore || mustNotBeWhitespaceBefore) {
            int before = ast.getColumnNo() - 1;

            if (before > 0 && !LINE_PREFIX.matcher(line).region(0, before).matches()) {
                boolean isWhitespace = Character.isWhitespace(line.charAt(before));
                if (mustBeWhitespaceBefore && !isWhitespace) {
                    log(ast.getLineNo(), ast.getColumnNo(), "ws.notPreceded", ast.getText());
                }
                if (mustNotBeWhitespaceBefore && isWhitespace) {
                    log(ast.getLineNo(), ast.getColumnNo(), "ws.preceded", ast.getText());
                }
            }
        }

        // Check whitespace AFTER token.
        if (mustBeWhitespaceAfter || mustNotBeWhitespaceAfter) {
            int after = ast.getColumnNo() + ast.getText().length();

            if (after < line.length() && !LINE_SUFFIX.matcher(line).region(after, line.length()).matches()) {
                boolean isWhitespace = Character.isWhitespace(line.charAt(after));
                if (mustBeWhitespaceAfter && !isWhitespace) {
                    log(ast.getLineNo(), ast.getColumnNo() + ast.getText().length(), "ws.notFollowed", ast.getText());
                }
                if (mustNotBeWhitespaceAfter && isWhitespace) {
                    log(ast.getLineNo(), ast.getColumnNo() + ast.getText().length(), "ws.followed", ast.getText());
                }
            }
        }
    }

    /**
     * @return The type of the closest ancestor who's type is no the given {@code tokenType}, or -1
     */
    private static int
    getAncestorWithTypeNot(DetailAST ast, int tokenType) {
        for (DetailAST a = ast.getParent();; a = a.getParent()) {

            if (a == null) return -1;

            int t = a.getType();
            if (t != tokenType) return t;
        }
    }
    
    /**
     * @return The type of the closest ancestor who's type is not {@code tokenType1} or {@code tokenType2}, or -1
     */
    private static int
    getAncestorWithTypeNot(DetailAST ast, int tokenType1, int tokenType2) {
        for (DetailAST a = ast.getParent();; a = a.getParent()) {
            
            if (a == null) return -1;
            
            int t = a.getType();
            if (t != tokenType1 && t != tokenType2) return t;
        }
    }

    private static final Pattern LINE_PREFIX = Pattern.compile("\\s*(?:/\\*(?:.(?!\\*/))*\\*/\\s*)*");
    private static final Pattern LINE_SUFFIX = Pattern.compile("\\s*(?:/\\*(?:.(?!\\*/))*\\*/\\s*)*(?://.*)?");

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

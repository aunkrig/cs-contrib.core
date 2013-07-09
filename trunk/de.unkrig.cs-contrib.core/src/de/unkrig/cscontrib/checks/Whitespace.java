
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
        /** 'assert x == 0;', 'assert x == 0 : "x not zero";' */
        ASSERT,
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
        /** 'break;', 'break LABEL;' */
        BREAK,
        /** 'case 7:' */
        CASE,
        /** 'catch (Exception e) {' */
        CATCH,
        /** 'class MyClass {' */
        CLASS_DECL,
        /** 'Class c = Object.class;' */
        CLASS_LITERAL,
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
        /** 'continue;', 'continue LABEL;' */
        CONTINUE,
        /** 'String engineer() default "[unassigned]";' */
        DEFAULT_ANNOTATION_TYPE_ELEMENT,
        /** 'switch (x) { default: break; }' */
        DEFAULT_SWITCH,
        /** 'do { ... } while (x > 0);' */
        DO,
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
        /** 'if (a == 0) { ... } else { ... } */
        ELSE,
        /** ';' */
        EMPTY_STAT,
        /** 'public enum Color { RED, BLUE, GREEN }' */
        ENUM,
        /** 'a < b', 'a <= b', 'a == b', 'a != b', 'a >= b', 'a > b' */
        EQUALITY_OPERATORS,
        /** 'class MyClass extends BaseClass {' */
        EXTENDS_TYPE,
        /** 'List<T extends MyClass>' */
        EXTENDS_TYPE_BOUND,
        /** 'try { ... } finally { ... }' */
        FINALLY,
        /** 'for (int i = 0; i < 3; i++) {', 'for (Object o : list) {' */
        FOR,
        /** 'if (a == 0) {' */
        IF,
        /** 'List<T implements MyInterface1, MyInterface2>' */
        IMPLEMENTS,
        /** 'import pkg.MyClass;', 'import pkg.*;' */
        IMPORT,
        /** 'import static pkg.MyClass.member;', import static pkg.MyClass.*;' */
        IMPORT_STATIC,
        /** 'a instanceof MyClass' */
        INSTANCEOF,
        /** 'interface { ... }' */
        INTERFACE,
        /** 'List<String>' */
        L_ANGLE,
        /** 'Object[]' */
        L_BRACK_ARRAY_DECL,
        /** 'a[3]' */
        L_BRACK_INDEX,
        /** '@SuppressWarnings({ "foo", "bar" })' */
        L_CURLY_ANNOTATION_ARRAY_INIT,
        /** 'new Object() { ... }' */
        L_CURLY_ANON_CLASS,
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
        /** 'new Object() {}' */
        L_CURLY_EMPTY_ANON_CLASS,
        /** 'int[] ia = {}', 'new int[] {}' */
        L_CURLY_EMPTY_ARRAY_INIT,
        /** 'try { ... } catch (...) {}' */
        L_CURLY_EMPTY_CATCH,
        /** 'void meth(...) {}' */
        L_CURLY_EMPTY_METHOD_DEF,
        /** 'class MyClass() {}', 'interface MyInterface() {}', 'interface @MyAnnotation {}', 'enum MyEnum {}' */
        L_CURLY_EMPTY_TYPE_DEF,
        /** 'enum MyEnum { FOO { ... } }' */
        L_CURLY_ENUM_CONSTANT_DEF,
        /** 'finally { ...' */
        L_CURLY_FINALLY,
        /** 'for (...) {' */
        L_CURLY_FOR,
        /** 'if (...) {' */
        L_CURLY_IF,
        /** 'class MyClass { { ... } }' */
        L_CURLY_INSTANCE_INIT,
        /** 'LABEL: {' */
        L_CURLY_LABELED_STAT,
        /** 'void meth(...) { ... }' */
        L_CURLY_METHOD_DEF,
        /** 'class MyClass { static { ... } }' */
        L_CURLY_STATIC_INIT,
        /** 'switch (a) {' */
        L_CURLY_SWITCH,
        /** 'synchronized (a) {' */
        L_CURLY_SYNCHRONIZED,
        /** 'try {' */
        L_CURLY_TRY,
        /**
         * 'class MyClass() {...}', 'interface MyInterface() {...}', 'interface @MyAnnotation {...}',
         * 'enum MyEnum {...}'
         */
        L_CURLY_TYPE_DEF,
        /** 'while (...) {' */
        L_CURLY_WHILE,
        /** '@SuppressWarnings("foo")' */
        L_PAREN_ANNOTATION,
        L_PAREN_ANNOTATION_FIELD_DEF,
        L_PAREN_CALL,
        /** '(int) a' */
        L_PAREN_CAST,
        L_PAREN_CATCH,
        L_PAREN_DO_WHILE,
        L_PAREN_FOR,
        L_PAREN_FOR_NO_INIT,
        L_PAREN_IF,
        L_PAREN_PARAMETERS,
        L_PAREN_PARENTHESIZED,
        LITERAL,
        LOGICAL_COMPLEMENT,
        MINUS_UNARY,
        MODIFIER,
        /** 'a * b', 'a / b', 'a % b' */
        MULTIPLICATIVE_OPERATORS,
        /** 'a.b.c' */
        NAME_AMBIGUOUS,
        /** '@MyAnnotation("x")' */
        NAME_ANNOTATION,
        NAME_ANNOTATION_FIELD_DEF,
        /** '@MyAnnotation(value = "x")' */
        NAME_ANNOTATION_MEMBER,
        /** 'MyClass(...) {' */
        NAME_CTOR_DEF,
        /** 'import pkg.pkg.*;' */
        NAME_IMPORT_COMPONENT,
        /** 'import pkg.pkg.*;' */
        NAME_IMPORT_TYPE,
        /** 'void main(...) {' */
        NAME_METHOD_DEF,
        /** 'package pkg.pkg.pkg;' */
        NAME_PACKAGE_DEF,
        /** 'meth(String parm)' */
        NAME_PARAMETER,
        /** 'pkg.MyType' */
        NAME_QUALIFIED_TYPE,
        /** 'MyType', 'new MyType' */
        NAME_SIMPLE_TYPE,
        NAME_TYPE_DEF,
        /** 'int a;' */
        NAME_VARIABLE_DEF,
        NEW,
        /** 'package ...' */
        PACKAGE,
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
        /** 'new Object() { ... }' */
        R_CURLY_ANON_CLASS,
        /** 'int[] ia = { 1, 2 }', 'new int[] { 1, 2 }' */
        R_CURLY_ARRAY_INIT,
        /** '{ int i = 0; i++; }' */
        R_CURLY_BLOCK,
        /** 'try { ... } catch (...) { ...' */
        R_CURLY_CATCH,
        /** 'do { ... } while (...);' */
        R_CURLY_DO_WHILE,
        /** 'else { ... }' */
        R_CURLY_ELSE,
        /** '@SuppressWarnings({})' */
        R_CURLY_EMPTY_ANNOTATION_ARRAY_INIT,
        /** 'new Object() {}' */
        R_CURLY_EMPTY_ANON_CLASS,
        /** 'int[] ia = {}', 'new int[] {}' */
        R_CURLY_EMPTY_ARRAY_INIT,
        /** 'try { ... } catch (...) {}' */
        R_CURLY_EMPTY_CATCH,
        /** 'public MyClass(...) {}', 'public method(...) {}' */
        R_CURLY_EMPTY_METHOD_DEF,
        /** 'class MyClass {}', 'interface MyInterface {}', '@interface MyAnnotation {}', 'enum MyEnum {}' */
        R_CURLY_EMPTY_TYPE_DEF,
        /** 'enum MyEnum { FOO { ... } }' */
        R_CURLY_ENUM_CONSTANT_DEF,
        /** 'finally { ... }' */
        R_CURLY_FINALLY,
        /** 'for (...) { ... }' */
        R_CURLY_FOR,
        /** 'if (...) { ... }' */
        R_CURLY_IF,
        /** 'class MyClass { { ... } }' */
        R_CURLY_INSTANCE_INIT,
        /** 'LABEL: { ... }' */
        R_CURLY_LABELED_STAT,
        /** 'public MyClass(...) { ... }', 'public method(...) { ... }' */
        R_CURLY_METHOD_DEF,
        /** 'class MyClass { static { ... } }' */
        R_CURLY_STATIC_INIT,
        /** 'switch (a) { ... }' */
        R_CURLY_SWITCH,
        /** 'synchronized (a) { ... }' */
        R_CURLY_SYNCHRONIZED,
        /** 'try { ... }' */
        R_CURLY_TRY,
        /**
         * 'class MyClass { ... }', 'interface MyInterface { ... }', '@interface MyAnnotation { ... }',
         * 'enum MyEnum { ... }'
         */
        R_CURLY_TYPE_DEF,
        /** 'while (...) { ... }' */
        R_CURLY_WHILE,
        /** '@SuppressWarnings("foo")' */
        R_PAREN_ANNOTATION,
        R_PAREN_ANNOTATION_FIELD_DEF,
        R_PAREN_CALL,
        /** '(int) a' */
        R_PAREN_CAST,
        R_PAREN_CATCH,
        R_PAREN_DO_WHILE,
        R_PAREN_FOR,
        R_PAREN_FOR_NO_UPDATE,
        R_PAREN_IF,
        R_PAREN_PARAMETERS,
        R_PAREN_PARENTHESIZED,
        /** 'return x;' */
        RETURN_EXPR,
        /** 'return;' */
        RETURN_NO_EXPR,
        SEMI_ABSTRACT_METH_DEF,
        SEMI_ANNOTATION_FIELD_DEF,
        /** 'enum MyEnum { 1, B, C; ... }' */
        SEMI_ENUM_DEF,
        SEMI_FIELD_DEF,
        /** 'for (...; i < 3;) {' */
        SEMI_FOR_CONDITION_NO_UPDATE,
        /** 'for (...; i < 3; i++) {' */
        SEMI_FOR_CONDITION_UPDATE,
        /** 'for (int i = 0; i < 3;...' */
        SEMI_FOR_INIT_CONDITION,
        /** 'for (int i = 0;;...' */
        SEMI_FOR_INIT_NO_CONDITION,
        /** 'for (...;;) {' */
        SEMI_FOR_NO_CONDITION_NO_UPDATE,
        /** 'for (...;; i++) {'*/
        SEMI_FOR_NO_CONDITION_UPDATE,
        /** 'for (; ...' */
        SEMI_FOR_NO_INIT_CONDITION,
        /** 'for (;;...' */
        SEMI_FOR_NO_INIT_NO_CONDITION,
        SEMI_IMPORT,
        SEMI_PACKAGE_DEF,
        SEMI_STATEMENT,
        SEMI_STATIC_IMPORT,
        SHIFT_OPERATORS,
        /** 'import pkg.pkg.*;' */
        STAR_TYPE_IMPORT_ON_DEMAND,
        /** 'class MyClass { static { ... } }' */
        STATIC_INIT,
        SUPER_CTOR_CALL,
        SUPER_EXPR,
        /** 'List<T super MyClass>' */
        SUPER_TYPE_BOUND,
        SWITCH,
        THIS_CTOR_CALL,
        THIS_EXPR,
        THROW,
        THROWS,
        TRY,
        VOID,
        /** 'while (a > 0) { ... }' */
        WHILE,
        /** 'do { ... } while (a > 0);' */
        WHILE_DO,
    }

    private EnumSet<Whitespaceable> whitespaceBefore = EnumSet.of(
        ADDITIVE_OPERATORS,
        ASSERT,
        ASSIGN_VARIABLE_DEF,
        ASSIGNS,
        BITWISE_OPERATORS,
        BREAK,
        CASE,
        CATCH,
        CLASS_DECL,
        COLON_ENHANCED_FOR,
        COLON_TERNARY,
        CONDITIONAL_OPERATORS,
        CONTINUE,
        DEFAULT_ANNOTATION_TYPE_ELEMENT,
        DEFAULT_SWITCH,
        DO,
        ELSE,
        ENUM,
        EQUALITY_OPERATORS,
        EXTENDS_TYPE,
        EXTENDS_TYPE_BOUND,
        FINALLY,
        FOR,
        IF,
        IMPLEMENTS,
        IMPORT,
        IMPORT_STATIC,
        INSTANCEOF,
        L_CURLY_ANON_CLASS,
        L_CURLY_BLOCK,
        L_CURLY_CATCH,
        L_CURLY_DO,
        L_CURLY_EMPTY_ANON_CLASS,
        L_CURLY_EMPTY_CATCH,
        L_CURLY_EMPTY_METHOD_DEF,
        L_CURLY_EMPTY_TYPE_DEF,
        L_CURLY_ENUM_CONSTANT_DEF,
        L_CURLY_FINALLY,
        L_CURLY_FOR,
        L_CURLY_IF,
        L_CURLY_INSTANCE_INIT,
        L_CURLY_LABELED_STAT,
        L_CURLY_METHOD_DEF,
        L_CURLY_STATIC_INIT,
        L_CURLY_SWITCH,
        L_CURLY_SYNCHRONIZED,
        L_CURLY_TRY,
        L_CURLY_TYPE_DEF,
        L_CURLY_WHILE,
        L_PAREN_CATCH,
        L_PAREN_DO_WHILE,
        L_PAREN_FOR,
        L_PAREN_FOR_NO_INIT,
        L_PAREN_IF,
        MULTIPLICATIVE_OPERATORS,
        NAME_CTOR_DEF,
        NAME_METHOD_DEF,
        NAME_PARAMETER,
        NAME_TYPE_DEF,
        NAME_VARIABLE_DEF,
        PACKAGE,
        QUESTION_TERNARY,
        R_CURLY_ANNOTATION_ARRAY_INIT,
        R_CURLY_ANON_CLASS,
        R_CURLY_ARRAY_INIT,
        R_CURLY_BLOCK,
        R_CURLY_CATCH,
        R_CURLY_DO_WHILE,
        R_CURLY_ELSE,
        R_CURLY_FINALLY,
        R_CURLY_FOR,
        R_CURLY_IF,
        R_CURLY_INSTANCE_INIT,
        R_CURLY_LABELED_STAT,
        R_CURLY_METHOD_DEF,
        R_CURLY_STATIC_INIT,
        R_CURLY_SWITCH,
        R_CURLY_SYNCHRONIZED,
        R_CURLY_TRY,
        R_CURLY_TYPE_DEF,
        R_CURLY_WHILE,
        RETURN_EXPR,
        RETURN_NO_EXPR,
        SHIFT_OPERATORS,
        STATIC_INIT,
        SUPER_CTOR_CALL,
        SUPER_TYPE_BOUND,
        SWITCH,
        THIS_CTOR_CALL,
        THROW,
        THROWS,
        TRY,
        VOID,
        WHILE,
        WHILE_DO
    );
    private EnumSet<Whitespaceable> noWhitespaceBefore = EnumSet.of(
        CLASS_LITERAL,
        COLON_DEFAULT,
        COLON_CASE,
        COLON_LABELED_STAT,
        COMMA,
        DOT_IMPORT,
        DOT_PACKAGE_DEF,
        DOT_QUALIFIED_TYPE,
        DOT_SELECTOR,
        ELLIPSIS_PARAMETER,
        L_BRACK_ARRAY_DECL,
        L_BRACK_INDEX,
        L_PAREN_ANNOTATION,
        L_PAREN_ANNOTATION_FIELD_DEF,
        L_PAREN_CALL,
        L_PAREN_PARAMETERS,
        NAME_ANNOTATION,
        POST_DEC,
        POST_INC,
        R_ANGLE,
        R_BRACK_ARRAY_DECL,
        R_CURLY_EMPTY_ANNOTATION_ARRAY_INIT,
        R_CURLY_EMPTY_ANON_CLASS,
        R_CURLY_EMPTY_ARRAY_INIT,
        R_CURLY_EMPTY_CATCH,
        R_CURLY_EMPTY_METHOD_DEF,
        R_CURLY_EMPTY_TYPE_DEF,
        R_CURLY_ENUM_CONSTANT_DEF,
        R_PAREN_ANNOTATION,
        R_PAREN_ANNOTATION_FIELD_DEF,
        R_PAREN_CALL,
        R_PAREN_CAST,
        R_PAREN_CATCH,
        R_PAREN_DO_WHILE,
        R_PAREN_FOR,
        R_PAREN_FOR_NO_UPDATE,
        R_PAREN_IF,
        R_PAREN_PARAMETERS,
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
        STAR_TYPE_IMPORT_ON_DEMAND
    );
    private EnumSet<Whitespaceable> whitespaceAfter = EnumSet.of(
        ADDITIVE_OPERATORS,
        ASSERT,
        ASSIGN_VARIABLE_DEF,
        ASSIGNS,
        BITWISE_OPERATORS,
        CASE,
        CATCH,
        CLASS_DECL,
        COLON_DEFAULT,
        COLON_CASE,
        COLON_ENHANCED_FOR,
        COLON_LABELED_STAT,
        COLON_TERNARY,
        COMMA,
        CONDITIONAL_OPERATORS,
        DEFAULT_ANNOTATION_TYPE_ELEMENT,
        DO,
        ELLIPSIS_PARAMETER,
        ELSE,
        EMPTY_STAT,
        ENUM,
        EQUALITY_OPERATORS,
        EXTENDS_TYPE,
        EXTENDS_TYPE_BOUND,
        FINALLY,
        FOR,
        IF,
        IMPLEMENTS,
        IMPORT,
        IMPORT_STATIC,
        INSTANCEOF,
        INTERFACE,
        L_CURLY_ANNOTATION_ARRAY_INIT,
        L_CURLY_ANON_CLASS,
        L_CURLY_ARRAY_INIT,
        L_CURLY_BLOCK,
        L_CURLY_CATCH,
        L_CURLY_DO,
        L_CURLY_ENUM_CONSTANT_DEF,
        L_CURLY_FINALLY,
        L_CURLY_FOR,
        L_CURLY_IF,
        L_CURLY_INSTANCE_INIT,
        L_CURLY_LABELED_STAT,
        L_CURLY_METHOD_DEF,
        L_CURLY_STATIC_INIT,
        L_CURLY_SWITCH,
        L_CURLY_SYNCHRONIZED,
        L_CURLY_TRY,
        L_CURLY_TYPE_DEF,
        L_CURLY_WHILE,
        MODIFIER,
        NEW,
        MULTIPLICATIVE_OPERATORS,
        NAME_ANNOTATION_MEMBER,
        PACKAGE,
        QUESTION_TERNARY,
        R_CURLY_BLOCK,
        R_CURLY_CATCH,
        R_CURLY_DO_WHILE,
        R_CURLY_ELSE,
        R_CURLY_EMPTY_CATCH,
        R_CURLY_EMPTY_METHOD_DEF,
        R_CURLY_EMPTY_TYPE_DEF,
        R_CURLY_FINALLY,
        R_CURLY_FOR,
        R_CURLY_IF,
        R_CURLY_INSTANCE_INIT,
        R_CURLY_LABELED_STAT,
        R_CURLY_METHOD_DEF,
        R_CURLY_STATIC_INIT,
        R_CURLY_SWITCH,
        R_CURLY_SYNCHRONIZED,
        R_CURLY_TRY,
        R_CURLY_TYPE_DEF,
        R_CURLY_WHILE,
        R_PAREN_ANNOTATION,
        R_PAREN_CAST,
        R_PAREN_CATCH,
        R_PAREN_IF,
        RETURN_EXPR,
        SEMI_ABSTRACT_METH_DEF,
        SEMI_ANNOTATION_FIELD_DEF,
        SEMI_ENUM_DEF,
        SEMI_FIELD_DEF,
        SEMI_FOR_CONDITION_UPDATE,
        SEMI_FOR_INIT_CONDITION,
        SEMI_FOR_NO_CONDITION_UPDATE,
        SEMI_FOR_NO_INIT_CONDITION,
        SEMI_IMPORT,
        SEMI_PACKAGE_DEF,
        SEMI_STATEMENT,
        SEMI_STATIC_IMPORT,
        SHIFT_OPERATORS,
        STATIC_INIT,
        SUPER_TYPE_BOUND,
        SWITCH,
        THROW,
        THROWS,
        TRY,
        VOID,
        WHILE,
        WHILE_DO
    );
    private EnumSet<Whitespaceable> noWhitespaceAfter = EnumSet.of(
        AT_ANNOTATION,
        AT_ANNOTATION_DEF,
        BITWISE_COMPLEMENT,
        CONTINUE,
        DEFAULT_SWITCH,
        DOT_IMPORT,
        DOT_PACKAGE_DEF,
        DOT_QUALIFIED_TYPE,
        DOT_SELECTOR,
        L_ANGLE,
        L_BRACK_ARRAY_DECL,
        L_BRACK_INDEX,
        L_CURLY_EMPTY_ANNOTATION_ARRAY_INIT,
        L_CURLY_EMPTY_ANON_CLASS,
        L_CURLY_EMPTY_ARRAY_INIT,
        L_CURLY_EMPTY_CATCH,
        L_CURLY_EMPTY_METHOD_DEF,
        L_CURLY_EMPTY_TYPE_DEF,
        L_PAREN_ANNOTATION,
        L_PAREN_ANNOTATION_FIELD_DEF,
        L_PAREN_CALL,
        L_PAREN_CAST,
        L_PAREN_CATCH,
        L_PAREN_DO_WHILE,
        L_PAREN_FOR,
        L_PAREN_FOR_NO_INIT,
        L_PAREN_IF,
        L_PAREN_PARAMETERS,
        L_PAREN_PARENTHESIZED,
        LOGICAL_COMPLEMENT,
        MINUS_UNARY,
        NAME_ANNOTATION_FIELD_DEF,
        NAME_ANNOTATION_MEMBER,
        NAME_CTOR_DEF,
        NAME_IMPORT_COMPONENT,
        NAME_IMPORT_TYPE,
        NAME_METHOD_DEF,
        NAME_PACKAGE_DEF,
        NAME_PARAMETER,
        PLUS_UNARY,
        PRE_DEC,
        PRE_INC,
        R_PAREN_DO_WHILE,
        RETURN_NO_EXPR,
        SEMI_FOR_CONDITION_NO_UPDATE,
        SEMI_FOR_INIT_NO_CONDITION,
        SEMI_FOR_NO_CONDITION_NO_UPDATE,
        SEMI_FOR_NO_INIT_NO_CONDITION,
        STAR_TYPE_IMPORT_ON_DEMAND,
        SUPER_CTOR_CALL,
        SUPER_EXPR,
        THIS_CTOR_CALL
    );

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

        Whitespaceable whitespaceable = toWhitespaceable(ast);

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
    private Whitespaceable
    toWhitespaceable(DetailAST ast) {

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
        switch (ast.getType()) {

        case TokenTypes.ARRAY_DECLARATOR:
            return L_BRACK_ARRAY_DECL;

        case TokenTypes.ARRAY_INIT:
            return firstChildType == TokenTypes.RCURLY ? L_CURLY_EMPTY_ARRAY_INIT : L_CURLY_ARRAY_INIT;

        case TokenTypes.ASSIGN:
            if (parentType == TokenTypes.VARIABLE_DEF) return ASSIGN_VARIABLE_DEF;
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
            return ASSIGNS;

        case TokenTypes.AT:
            if (parentType == TokenTypes.ANNOTATION) return AT_ANNOTATION;
            if (parentType == TokenTypes.ANNOTATION_DEF) return AT_ANNOTATION_DEF;
            assert false : "AT has unexpected parent '" + ast.getParent() + "'";
            return null;

        case TokenTypes.BAND:
        case TokenTypes.BOR:
        case TokenTypes.BXOR:
            return BITWISE_OPERATORS;

        case TokenTypes.BNOT:
            return BITWISE_COMPLEMENT;

        case TokenTypes.LOR: // 'a || b'
        case TokenTypes.LAND: // 'a && b'
            return CONDITIONAL_OPERATORS;

        case TokenTypes.LNOT: // '!a'
            return LOGICAL_COMPLEMENT;

        case TokenTypes.TYPECAST:
            return L_PAREN_CAST;

        case TokenTypes.COLON:
            if (parentType == TokenTypes.LITERAL_DEFAULT) return COLON_DEFAULT;
            if (parentType == TokenTypes.LITERAL_CASE) return COLON_CASE;
            if (parentType == TokenTypes.FOR_EACH_CLAUSE) return COLON_ENHANCED_FOR;
            return COLON_TERNARY;

        case TokenTypes.COMMA:
            return COMMA;

        case TokenTypes.DEC:
            return PRE_DEC; // '--x'

        case TokenTypes.INC:
            return PRE_INC; // '++x'

        case TokenTypes.POST_DEC:
            return POST_DEC; // 'x--'

        case TokenTypes.POST_INC:
            return POST_INC; // 'x++'

        case TokenTypes.STAR:
            if (parentType == TokenTypes.DOT) return STAR_TYPE_IMPORT_ON_DEMAND;
            // FALLTHROUGH
        case TokenTypes.DIV:
        case TokenTypes.MOD:
            return MULTIPLICATIVE_OPERATORS;

        case TokenTypes.PLUS:
        case TokenTypes.MINUS:
            return ADDITIVE_OPERATORS;

        case TokenTypes.UNARY_MINUS:
            return MINUS_UNARY;

        case TokenTypes.UNARY_PLUS:
            return PLUS_UNARY;

        case TokenTypes.DOT:
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.PACKAGE_DEF) return DOT_PACKAGE_DEF;
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.IMPORT) return DOT_IMPORT;
            if (
                getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR, TokenTypes.DOT) == TokenTypes.TYPE
            ) return DOT_QUALIFIED_TYPE;
            return DOT_SELECTOR;

        case TokenTypes.EMPTY_STAT:
            return EMPTY_STAT;

        case TokenTypes.LT:
        case TokenTypes.LE:
        case TokenTypes.EQUAL:
        case TokenTypes.NOT_EQUAL:
        case TokenTypes.GE:
        case TokenTypes.GT:
            return EQUALITY_OPERATORS;

        case TokenTypes.GENERIC_END:
            return R_ANGLE;

        case TokenTypes.GENERIC_START:
            return L_ANGLE;

        case TokenTypes.IDENT:
            if (
                parentType == TokenTypes.CLASS_DEF                                       // 'class MyClass {'
                || parentType == TokenTypes.INTERFACE_DEF                                // 'interface MyInterface {'
                || parentType == TokenTypes.ANNOTATION_DEF                               // 'interface @MyAnnotation {'
                || parentType == TokenTypes.ENUM_DEF                                     // 'enum MyEnum {'
            ) return NAME_TYPE_DEF;
            if (parentType == TokenTypes.ANNOTATION) return NAME_ANNOTATION;
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) return NAME_ANNOTATION_FIELD_DEF;
            if (parentType == TokenTypes.VARIABLE_DEF) return NAME_VARIABLE_DEF;
            if (parentType == TokenTypes.CTOR_DEF) return NAME_CTOR_DEF;
            if (parentType == TokenTypes.METHOD_DEF) return NAME_METHOD_DEF;
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.PACKAGE_DEF) return NAME_PACKAGE_DEF;
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.IMPORT) {
                return ast.getNextSibling() == null ? NAME_IMPORT_TYPE : NAME_IMPORT_COMPONENT;
            }
            if (
                getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR) == TokenTypes.TYPE           // 'MyType'
                || getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR) == TokenTypes.LITERAL_NEW // 'new MyType'
            ) return NAME_SIMPLE_TYPE;
            if (
                getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR, TokenTypes.DOT) == TokenTypes.TYPE
            ) return NAME_QUALIFIED_TYPE;
            if (parentType == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) return NAME_ANNOTATION_MEMBER;
            if (parentType == TokenTypes.PARAMETER_DEF) return NAME_PARAMETER;
            return NAME_AMBIGUOUS;

        case TokenTypes.LCURLY:
            if (parentType == TokenTypes.OBJBLOCK && (
                grandparentType == TokenTypes.CLASS_DEF         // 'class MyClass() {...}'
                || grandparentType == TokenTypes.INTERFACE_DEF  // 'interface MyInterface() {...}'
                || grandparentType == TokenTypes.ANNOTATION_DEF // 'interface @MyAnnotation {...}'
                || grandparentType == TokenTypes.ENUM_DEF       // 'enum MyEnum {...}'
            )) return nextSiblingType == TokenTypes.RCURLY ? L_CURLY_EMPTY_TYPE_DEF : L_CURLY_TYPE_DEF;
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.LITERAL_NEW
            ) return nextSiblingType == TokenTypes.RCURLY ? L_CURLY_EMPTY_ANON_CLASS : L_CURLY_ANON_CLASS;
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.ENUM_CONSTANT_DEF
            ) return L_CURLY_ENUM_CONSTANT_DEF;
            if (
                parentType == TokenTypes.ARRAY_INIT
            ) return nextSiblingType == TokenTypes.RCURLY ? L_CURLY_EMPTY_ARRAY_INIT : L_CURLY_ARRAY_INIT;
            if (parentType == TokenTypes.LITERAL_SWITCH) return L_CURLY_SWITCH;
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;

        case TokenTypes.ANNOTATION_ARRAY_INIT:
            return (
                firstChildType == TokenTypes.RCURLY
                ? L_CURLY_EMPTY_ANNOTATION_ARRAY_INIT
                : L_CURLY_ANNOTATION_ARRAY_INIT
            );

        case TokenTypes.INDEX_OP:
            return L_BRACK_INDEX;

        case TokenTypes.IMPLEMENTS_CLAUSE:
            return IMPLEMENTS;

        case TokenTypes.LITERAL_BYTE:
        case TokenTypes.LITERAL_INT:
        case TokenTypes.LITERAL_FLOAT:
        case TokenTypes.LITERAL_BOOLEAN:
        case TokenTypes.LITERAL_CHAR:
        case TokenTypes.LITERAL_LONG:
        case TokenTypes.LITERAL_DOUBLE:
        case TokenTypes.LITERAL_SHORT:
            return PRIMITIVE_TYPE;

        case TokenTypes.CHAR_LITERAL:
        case TokenTypes.LITERAL_FALSE:
        case TokenTypes.LITERAL_TRUE:
        case TokenTypes.LITERAL_NULL:
        case TokenTypes.NUM_DOUBLE:
        case TokenTypes.NUM_FLOAT:
        case TokenTypes.NUM_INT:
        case TokenTypes.NUM_LONG:
        case TokenTypes.STRING_LITERAL:
            return LITERAL;

        case TokenTypes.DO_WHILE:
            return WHILE_DO;

        case TokenTypes.ENUM:
            return ENUM;

        case TokenTypes.IMPORT:
            return IMPORT;

        case TokenTypes.LITERAL_ASSERT:
            return ASSERT;

        case TokenTypes.LITERAL_BREAK:
            return BREAK;

        case TokenTypes.LITERAL_CASE:
            return CASE;

        case TokenTypes.LITERAL_CATCH:
            return CATCH;

        case TokenTypes.LITERAL_CLASS:
            return parentType == TokenTypes.CLASS_DEF ? CLASS_DECL : CLASS_LITERAL;

        case TokenTypes.LITERAL_CONTINUE:
            return CONTINUE;

        case TokenTypes.LITERAL_DEFAULT:
            return (
                parentType == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR || parentType == TokenTypes.ANNOTATION_FIELD_DEF
            ) ? DEFAULT_ANNOTATION_TYPE_ELEMENT : DEFAULT_SWITCH;

        case TokenTypes.LITERAL_DO:
            return DO;

        case TokenTypes.LITERAL_ELSE:
            return ELSE;

        case TokenTypes.LITERAL_FINALLY:
            return FINALLY;

        case TokenTypes.LITERAL_FOR:
            return FOR;

        case TokenTypes.LITERAL_IF:
            return IF;

        case TokenTypes.LITERAL_INSTANCEOF:
            return INSTANCEOF;

        case TokenTypes.LITERAL_INTERFACE:
            return INTERFACE;

        case TokenTypes.LITERAL_NEW:
            return NEW;

        case TokenTypes.LITERAL_SUPER:
            return SUPER_EXPR;

        case TokenTypes.LITERAL_SWITCH:
            return SWITCH;

        case TokenTypes.LITERAL_THIS:
            return THIS_EXPR;

        case TokenTypes.LITERAL_THROW:
            return THROW;

        case TokenTypes.LITERAL_THROWS:
            return THROWS;

        case TokenTypes.LITERAL_TRY:
            return TRY;

        case TokenTypes.LITERAL_VOID:
            return VOID;

        case TokenTypes.ABSTRACT:
        case TokenTypes.FINAL:
        case TokenTypes.LITERAL_NATIVE:
        case TokenTypes.LITERAL_PRIVATE:
        case TokenTypes.LITERAL_PROTECTED:
        case TokenTypes.LITERAL_PUBLIC:
        case TokenTypes.LITERAL_STATIC:
        case TokenTypes.LITERAL_SYNCHRONIZED:
        case TokenTypes.LITERAL_TRANSIENT:
        case TokenTypes.LITERAL_VOLATILE:
            return MODIFIER;

        case TokenTypes.LITERAL_WHILE:
            return WHILE;

        case TokenTypes.PACKAGE_DEF:
            return PACKAGE;

        case TokenTypes.STATIC_IMPORT: 
            return IMPORT_STATIC;

        case TokenTypes.STATIC_INIT:
            ast.setText("static");
            return STATIC_INIT;

        case TokenTypes.LITERAL_RETURN:
            return firstChildType == TokenTypes.SEMI ? RETURN_NO_EXPR : RETURN_EXPR;

        case TokenTypes.LPAREN:
            if (parentType == TokenTypes.ANNOTATION) return L_PAREN_ANNOTATION;
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) return L_PAREN_ANNOTATION_FIELD_DEF;
            if (nextSiblingType == TokenTypes.PARAMETERS) return L_PAREN_PARAMETERS;
            if (
                parentType == TokenTypes.SUPER_CTOR_CALL || parentType == TokenTypes.LITERAL_NEW
            ) return L_PAREN_CALL;
            if (parentType == TokenTypes.LITERAL_DO) return L_PAREN_DO_WHILE;
            if (parentType == TokenTypes.LITERAL_IF) return L_PAREN_IF;
            if (parentType == TokenTypes.LITERAL_FOR) {
                return ast.getNextSibling().getFirstChild() == null ? L_PAREN_FOR_NO_INIT : L_PAREN_FOR;
            }
            if (parentType == TokenTypes.LITERAL_CATCH) return L_PAREN_CATCH;
            return L_PAREN_PARENTHESIZED;

        case TokenTypes.METHOD_CALL:
            return L_PAREN_CALL;

        case TokenTypes.METHOD_DEF:
        case TokenTypes.MODIFIERS:
        case TokenTypes.OBJBLOCK:
        case TokenTypes.PARAMETER_DEF:
        case TokenTypes.PARAMETERS:
            return null;

        case TokenTypes.QUESTION:
            return QUESTION_TERNARY;

        case TokenTypes.RBRACK:
            return R_BRACK_ARRAY_DECL;

        case TokenTypes.RCURLY:
            if (
                parentType == TokenTypes.SLIST
                && grandparentType == TokenTypes.LITERAL_CATCH
            ) return ast.getPreviousSibling() == null ? R_CURLY_EMPTY_CATCH : R_CURLY_CATCH;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_SYNCHRONIZED
            ) return R_CURLY_SYNCHRONIZED;
            if (
                parentType == TokenTypes.OBJBLOCK
                && (
                    grandparentType == TokenTypes.CLASS_DEF
                    || grandparentType == TokenTypes.INTERFACE_DEF
                    || grandparentType == TokenTypes.ANNOTATION_DEF
                    || grandparentType == TokenTypes.ENUM_DEF
                )
            ) return previousSiblingType == TokenTypes.LCURLY ? R_CURLY_EMPTY_TYPE_DEF : R_CURLY_TYPE_DEF;
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.LITERAL_NEW 
            ) return previousSiblingType == TokenTypes.LCURLY ? R_CURLY_EMPTY_ANON_CLASS : R_CURLY_ANON_CLASS;
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.ENUM_CONSTANT_DEF
            ) return R_CURLY_ENUM_CONSTANT_DEF;
            if (
                parentType == TokenTypes.SLIST
                && (grandparentType == TokenTypes.CTOR_DEF || grandparentType == TokenTypes.METHOD_DEF)
            ) return ast.getPreviousSibling() == null ? R_CURLY_EMPTY_METHOD_DEF : R_CURLY_METHOD_DEF;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_FOR
            ) return R_CURLY_FOR;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_IF
            ) return R_CURLY_IF;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_ELSE
            ) return R_CURLY_IF;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_WHILE
            ) return R_CURLY_WHILE;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_DO
            ) return R_CURLY_DO_WHILE;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_TRY
            ) return R_CURLY_TRY;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_FINALLY
            ) return R_CURLY_FINALLY;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LABELED_STAT
            ) return R_CURLY_LABELED_STAT;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.SLIST
            ) return R_CURLY_BLOCK;
            if (parentType == TokenTypes.LITERAL_SWITCH) return R_CURLY_SWITCH;
            if (parentType == TokenTypes.ARRAY_INIT) {
                return ast.getPreviousSibling() == null ? R_CURLY_EMPTY_ARRAY_INIT : R_CURLY_ARRAY_INIT;
            } 
            if (parentType == TokenTypes.ANNOTATION_ARRAY_INIT) {
                return (
                    ast.getPreviousSibling() == null
                    ? R_CURLY_EMPTY_ANNOTATION_ARRAY_INIT
                    : R_CURLY_ANNOTATION_ARRAY_INIT
                );
            }
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.STATIC_INIT
            ) return R_CURLY_STATIC_INIT;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.INSTANCE_INIT
            ) return R_CURLY_INSTANCE_INIT;
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;

        case TokenTypes.RESOURCE:
        case TokenTypes.RESOURCE_SPECIFICATION:
        case TokenTypes.RESOURCES:
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;

        case TokenTypes.RPAREN:
            if (parentType == TokenTypes.ANNOTATION) return R_PAREN_ANNOTATION;
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) return R_PAREN_ANNOTATION_FIELD_DEF;
            if (parentType == TokenTypes.CTOR_DEF || parentType == TokenTypes.METHOD_DEF) return R_PAREN_PARAMETERS;
            if (
                parentType == TokenTypes.SUPER_CTOR_CALL
                || parentType == TokenTypes.LITERAL_NEW
                || parentType == TokenTypes.METHOD_CALL
            ) return R_PAREN_CALL;
            if (parentType == TokenTypes.LITERAL_IF) return R_PAREN_IF;
            if (parentType == TokenTypes.LITERAL_DO) return R_PAREN_DO_WHILE;
            if (parentType == TokenTypes.LITERAL_FOR) {
                return ast.getPreviousSibling().getFirstChild() == null ? R_PAREN_FOR_NO_UPDATE : R_PAREN_FOR;
            }
            if (parentType == TokenTypes.LITERAL_CATCH) return R_PAREN_CATCH;
            if (previousSiblingType == TokenTypes.TYPE) return R_PAREN_CAST;
            return R_PAREN_PARENTHESIZED;

        case TokenTypes.SEMI:
            if (parentType == TokenTypes.PACKAGE_DEF) return SEMI_PACKAGE_DEF;
            if (parentType == TokenTypes.IMPORT) return SEMI_IMPORT;
            if (parentType == TokenTypes.STATIC_IMPORT) return SEMI_STATIC_IMPORT;
            if (
                parentType == TokenTypes.SLIST
                || parentType == TokenTypes.SUPER_CTOR_CALL
                || parentType == TokenTypes.CTOR_CALL
                || parentType == TokenTypes.LITERAL_DO
                || parentType == TokenTypes.LITERAL_RETURN
                || parentType == TokenTypes.LITERAL_BREAK
                || parentType == TokenTypes.LITERAL_CONTINUE
                || parentType == TokenTypes.LITERAL_IF
                || parentType == TokenTypes.LITERAL_WHILE
                || parentType == TokenTypes.LITERAL_ASSERT
                || parentType == TokenTypes.LITERAL_THROW
            ) return SEMI_STATEMENT;
            if (parentType == TokenTypes.METHOD_DEF) return SEMI_ABSTRACT_METH_DEF;
            if (previousSiblingType == TokenTypes.FOR_INIT) {
                return ast.getPreviousSibling().getFirstChild() == null ? (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI_FOR_NO_INIT_NO_CONDITION
                    : SEMI_FOR_NO_INIT_CONDITION
                ) : (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI_FOR_INIT_NO_CONDITION
                    : SEMI_FOR_INIT_CONDITION
                );
            }
            if (previousSiblingType == TokenTypes.FOR_CONDITION) {
                return ast.getPreviousSibling().getFirstChild() == null ? (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI_FOR_NO_CONDITION_NO_UPDATE
                    : SEMI_FOR_NO_CONDITION_UPDATE
                ) : (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI_FOR_CONDITION_NO_UPDATE
                    : SEMI_FOR_CONDITION_UPDATE
                );
            }
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) return SEMI_ANNOTATION_FIELD_DEF;
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.ENUM_DEF
            ) return SEMI_ENUM_DEF;
            if (
                parentType == TokenTypes.VARIABLE_DEF && grandparentType == TokenTypes.OBJBLOCK
            ) return SEMI_FIELD_DEF;
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;

        case TokenTypes.SLIST:
            if (parentType == TokenTypes.STATIC_INIT) return L_CURLY_STATIC_INIT;
            if (parentType == TokenTypes.INSTANCE_INIT) return L_CURLY_INSTANCE_INIT;
            if (parentType == TokenTypes.LITERAL_IF) return L_CURLY_IF;
            if (parentType == TokenTypes.LITERAL_ELSE) return R_CURLY_ELSE;
            if (parentType == TokenTypes.LITERAL_DO) return L_CURLY_DO;
            if (parentType == TokenTypes.LITERAL_WHILE) return L_CURLY_WHILE;
            if (parentType == TokenTypes.LITERAL_FOR) return L_CURLY_FOR;
            if (parentType == TokenTypes.LITERAL_TRY) return L_CURLY_TRY;
            if (parentType == TokenTypes.LITERAL_CATCH) { 
                return firstChildType == TokenTypes.RCURLY ? L_CURLY_EMPTY_CATCH : L_CURLY_CATCH;
            }
            if (parentType == TokenTypes.LITERAL_FINALLY) return L_CURLY_FINALLY;
            if (parentType == TokenTypes.LITERAL_SYNCHRONIZED) return L_CURLY_SYNCHRONIZED;
            if (parentType == TokenTypes.LABELED_STAT) return L_CURLY_LABELED_STAT;
            if (parentType == TokenTypes.SLIST) return L_CURLY_BLOCK;
            if (parentType == TokenTypes.CTOR_DEF || parentType == TokenTypes.METHOD_DEF) {
                return (
                    firstChildType == TokenTypes.RCURLY
                    ? Whitespaceable.L_CURLY_EMPTY_METHOD_DEF
                    : Whitespaceable.L_CURLY_METHOD_DEF
                );
            }
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;

        case TokenTypes.SL:
        case TokenTypes.SR:
        case TokenTypes.BSR:
            return SHIFT_OPERATORS;
            
        case TokenTypes.ELLIPSIS:
            return ELLIPSIS_PARAMETER;

        case TokenTypes.TYPE:
            return null;

        case TokenTypes.CTOR_CALL:
            return THIS_CTOR_CALL;

        case TokenTypes.SUPER_CTOR_CALL:
            return SUPER_CTOR_CALL;

        case TokenTypes.TYPE_UPPER_BOUNDS:
            return EXTENDS_TYPE_BOUND;

        case TokenTypes.TYPE_LOWER_BOUNDS:
            return SUPER_TYPE_BOUND;

        case TokenTypes.WILDCARD_TYPE:
            return QUESTION_WILDCARD_TYPE;

        case TokenTypes.EXTENDS_CLAUSE:
            return EXTENDS_TYPE;
            
        case TokenTypes.LABELED_STAT:
            return COLON_LABELED_STAT;
            
        case TokenTypes.CLASS_DEF:
        case TokenTypes.INTERFACE_DEF:
        case TokenTypes.CTOR_DEF:
        case TokenTypes.FOR_INIT:
        case TokenTypes.FOR_CONDITION:
        case TokenTypes.FOR_ITERATOR:
        case TokenTypes.ELIST:
        case TokenTypes.CASE_GROUP:
        case TokenTypes.ANNOTATION:
        case TokenTypes.ANNOTATIONS:
        case TokenTypes.EXPR:
        case TokenTypes.ENUM_DEF:
        case TokenTypes.ENUM_CONSTANT_DEF:
        case TokenTypes.STRICTFP:
        case TokenTypes.TYPE_ARGUMENT:
        case TokenTypes.TYPE_ARGUMENTS:
        case TokenTypes.TYPE_EXTENSION_AND:
        case TokenTypes.TYPE_PARAMETER:
        case TokenTypes.TYPE_PARAMETERS:
        case TokenTypes.VARIABLE_DEF:
            return null;

        default:
            assert false : "Unexpected DetailAST " + ast;
            return null;
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

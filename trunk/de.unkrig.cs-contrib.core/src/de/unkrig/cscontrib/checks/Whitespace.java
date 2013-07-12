
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

import static de.unkrig.cscontrib.checks.Whitespace.JavaElement.*;

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
    enum JavaElement {

        // CHECKSTYLE __:OFF
        /** 'abstract' */
        ABSTRACT,
        /** 'a & b' */
        AND__EXPR,
        /** 'a &= b' */
        AND_ASSIGN,
        /** 'assert x == 0;', 'assert x == 0 : "x not zero";' */
        ASSERT,
        /** 'a = 7;' */
        ASSIGN__ASSIGNMENT,
        /** 'int a = 7;' */
        ASSIGN__VAR_DECL,
        /** '@MyAnno' */
        AT__ANNO,
        /** 'interface @MyAnno {' */
        AT__ANNO_DECL,
        /** '~a' */
        BITWISE_COMPLEMENT,
        /** 'boolean' */
        BOOLEAN,
        /** 'break;', 'break LABEL;' */
        BREAK,
        /** 'byte' */
        BYTE,
        /** 'case 7:' */
        CASE,
        /** 'catch (Exception e) {' */
        CATCH,
        /** 'char' */
        CHAR,
        /** 'c' */
        CHAR_LITERAL,
        /** 'class MyClass {' */
        CLASS__CLASS_DECL,
        /** 'Class c = Object.class;' */
        CLASS__CLASS_LITERAL,
        /** 'case 77:' */
        COLON__CASE,
        /** 'default:' */
        COLON__DEFAULT,
        /** 'for (Object o : list) {' */
        COLON__ENHANCED_FOR,
        /** 'LABEL: while (...) {' */
        COLON__LABELED_STAT,
        /** 'a ? b : c' */
        COLON__TERNARY,
        /** ',' */
        COMMA,
        /** 'a && b' */
        CONDITIONAL_AND,
        /** 'a || b' */
        CONDITIONAL_OR,
        /** 'continue;', 'continue LABEL;' */
        CONTINUE,
        /** 'String engineer() default "[unassigned]";' */
        DEFAULT__ANNO_ELEM,
        /** 'switch (x) { default: break; }' */
        DEFAULT__SWITCH,
        /** 'a / b' */
        DIVIDE,
        /** 'a /= b' */
        DIVIDE_ASSIGN,
        /** 'do { ... } while (x > 0);' */
        DO,
        /** 'import pkg.*;', 'import pkg.Type;' */
        DOT__IMPORT,
        /** 'package pkg.pkg;' */
        DOT__PACKAGE_DECL,
        /** 'pkg.MyType', 'pkg.MyType[]' */
        DOT__QUALIFIED_TYPE,
        /** 'a.b', 'a().b' */
        DOT__SELECTOR,
        /** 'double' */
        DOUBLE,
        /** '1.0', '.1', '1E3', '1D' */
        DOUBLE_LITERAL,
        /** 'meth(Object... o)' */
        ELLIPSIS,
        /** 'if (a == 0) { ... } else { ... } */
        ELSE,
        /** 'public enum Color { RED, BLUE, GREEN }' */
        ENUM,
        /** 'a == b' */
        EQUAL,
        /** 'class MyClass extends BaseClass {' */
        EXTENDS__TYPE,
        /** 'List<T extends MyClass>' */
        EXTENDS__TYPE_BOUND,
        /** 'false' */
        FALSE,
        /** 'final' */
        FINAL,
        /** 'try { ... } finally { ... }' */
        FINALLY,
        /** 'float' */
        FLOAT,
        /** '1F' */
        FLOAT_LITERAL,
        /** 'for (int i = 0; i < 3; i++) {', 'for (Object o : list) {' */
        FOR,
        /** 'a > b' */
        GREATER,
        /** 'a >= b' */
        GREATER_EQUAL,
        /** 'if (a == 0) {' */
        IF,
        /** 'List<T implements MyInterface1, MyInterface2>' */
        IMPLEMENTS,
        /** 'import pkg.MyClass;', 'import pkg.*;' */
        IMPORT,
        /** 'import static pkg.MyClass.member;', import static pkg.MyClass.*;' */
        IMPORT__STATIC_IMPORT,
        /** 'a instanceof MyClass' */
        INSTANCEOF,
        /** 'int' */
        INT,
        /** '11', '0xB', '013', '0B1011' */
        INT_LITERAL,
        /** 'interface { ... }' */
        INTERFACE,
        /** 'public &lt;T extends Number> void meth(T parm) {' */
        L_ANGLE__METH_DECL_TYPE_PARAMS,
        /** 'MyClass.&lt;Double>meth(x)' */
        L_ANGLE__METH_INVOCATION_TYPE_ARGS,
        /** 'MyClass&lt;String>' */
        L_ANGLE__TYPE_ARGS,
        /** 'class MyClass&lt;T extends Number> {' */
        L_ANGLE__TYPE_PARAMS,
        /** 'Object[]' */
        L_BRACK__ARRAY_DECL,
        /** 'a[3]' */
        L_BRACK__INDEX,
        /** '@SuppressWarnings({ "foo", "bar" })' */
        L_CURLY__ANNO_ARRAY_INIT,
        /** 'new Object() { ... }' */
        L_CURLY__ANON_CLASS,
        /** 'int[] ia = { 1, 2 }', 'new int[] { 1, 2 }' */
        L_CURLY__ARRAY_INIT,
        /** '{ int i = 0; i++; }' */
        L_CURLY__BLOCK,
        /** 'try { ... } catch (...) { ...' */
        L_CURLY__CATCH,
        /** 'do { ...' */
        L_CURLY__DO,
        /** '@SuppressWarnings({})' */
        L_CURLY__EMPTY_ANNO_ARRAY_INIT,
        /** 'new Object() {}' */
        L_CURLY__EMPTY_ANON_CLASS,
        /** 'int[] ia = {}', 'new int[] {}' */
        L_CURLY__EMPTY_ARRAY_INIT,
        /** 'try { ... } catch (...) {}' */
        L_CURLY__EMPTY_CATCH,
        /** 'void meth(...) {}' */
        L_CURLY__EMPTY_METH_DEF,
        /** 'class MyClass() {}', 'interface MyInterface() {}', 'interface @MyAnnotation {}', 'enum MyEnum {}' */
        L_CURLY__EMPTY_TYPE_DEF,
        /** 'enum MyEnum { FOO { ... } }' */
        L_CURLY__ENUM_CONST_DEF,
        /** 'finally { ...' */
        L_CURLY__FINALLY,
        /** 'for (...) {' */
        L_CURLY__FOR,
        /** 'if (...) {' */
        L_CURLY__IF,
        /** 'class MyClass { { ... } }' */
        L_CURLY__INSTANCE_INIT,
        /** 'LABEL: {' */
        L_CURLY__LABELED_STAT,
        /** 'void meth(...) { ... }' */
        L_CURLY__METH_DECL,
        /** 'class MyClass { static { ... } }' */
        L_CURLY__STATIC_INIT,
        /** 'switch (a) {' */
        L_CURLY__SWITCH,
        /** 'synchronized (a) {' */
        L_CURLY__SYNCHRONIZED,
        /** 'try {' */
        L_CURLY__TRY,
        /** 'class MyClass() {', 'interface MyInterface() {', 'interface @MyAnno {', 'enum MyEnum {' */
        L_CURLY__TYPE_DECL,
        /** 'while (...) {' */
        L_CURLY__WHILE,
        /** '@SuppressWarnings("foo")' */
        L_PAREN__ANNO,
        /** 'interface @MyAnno { String engineer(); }' */
        L_PAREN__ANNO_ELEM_DECL,
        /** '(int) a' */
        L_PAREN__CAST,
        /** 'try { ... } catch (Exception e) {' */
        L_PAREN__CATCH,
        /** 'do { ... } while (...);' */
        L_PAREN__DO_WHILE,
        /** 'for (int i = 0; i  10; i++) {' */
        L_PAREN__FOR,
        /** 'for (; i  10; i++) {' */
        L_PAREN__FOR_NO_INIT,
        /** 'if (...) {' */
        L_PAREN__IF,
        /** 'a()' */
        L_PAREN__METH_INVOCATION,
        /** 'void meth(...) {' */
        L_PAREN__PARAMS,
        /** '(a + b) * c' */
        L_PAREN__PARENTHESIZED,
        /** '<<' */
        LEFT_SHIFT,
        /** 'a <<= b' */
        LEFT_SHIFT_ASSIGN,
        /** 'a < b' */
        LESS,
        /** 'a <= b' */
        LESS_EQUAL,
        /** '!a' */
        LOGICAL_COMPLEMENT,
        /** 'long' */
        LONG,
        /** '11L', '0xBL', '013L', '0B1011L' */
        LONG_LITERAL,
        /** 'a - b' */
        MINUS__ADDITIVE,
        /** '-a' */
        MINUS__UNARY,
        /** 'a -= b' */
        MINUS_ASSIGN,
        /** 'a % b' */
        MODULO,
        /** 'a %= b'*/
        MODULO_ASSIGN,
        /** 'a * b' */
        MULTIPLY,
        /** 'a *= b' */
        MULTIPLY_ASSIGN,
        /** 'a.b.c' */
        NAME__AMBIGUOUS,
        /** '@MyAnnotation("x")' */
        NAME__ANNO,
        /** 'interface @MyAnno { String engineer(); }' */
        NAME__ANNO_ELEM_DECL,
        /** '@MyAnnotation(value = "x")' */
        NAME__ANNO_MEMBER,
        /** 'MyClass(...) {' */
        NAME__CTOR_DEF,
        /** 'import pkg.pkg.*;' */
        NAME__IMPORT_COMPONENT,
        /** 'import pkg.pkg.*;' */
        NAME__IMPORT_TYPE,
        /** 'void main(...) {' */
        NAME__METH_DECL,
        /** 'package pkg.pkg.pkg;' */
        NAME__PACKAGE_DECL,
        /** 'meth(String parm)' */
        NAME__PARAM,
        /** 'pkg.MyType' */
        NAME__QUALIFIED_TYPE,
        /** 'MyType', 'new MyType' */
        NAME__SIMPLE_TYPE,
        /** 'class MyClass {', 'interface MyInterface {', 'interface @MyAnnotation {', 'enum MyEnum {' */
        NAME__TYPE_DECL,
        /** 'int a;' */
        NAME__VAR_DEF,
        /** 'native' */
        NATIVE,
        /** 'new' */
        NEW,
        /** 'a != b' */
        NOT_EQUAL,
        /** 'null' */
        NULL,
        /** 'a | b' */
        OR,
        /** 'a |= b'*/
        OR_ASSIGN,
        /** 'package ...' */
        PACKAGE,
        /** 'a + b',  */
        PLUS__ADDITIVE,
        /** '+(a + b)' */
        PLUS__UNARY,
        /** 'a += b' */
        PLUS_ASSIGN,
        /** 'x--' */
        POST_DECR,
        /** 'x++' */
        POST_INCR,
        /** '--x' */
        PRE_DECR,
        /** '++x' */
        PRE_INCR,
        /** 'private' */
        PRIVATE,
        /** 'protected' */
        PROTECTED,
        /** 'public' */
        PUBLIC,
        /** 'a ? b : c' */
        QUESTION__TERNARY,
        /** 'List&lt;? extends InputStream>' */
        QUESTION__WILDCARD_TYPE,
        /** 'public &lt;T extends Number> void meth(T parm) {' */
        R_ANGLE__METH_DECL_TYPE_PARAMS,
        /** 'MyClass.&lt;Double>meth(x)' */
        R_ANGLE__METH_INVOCATION_TYPE_ARGS,
        /** 'MyClass&lt;String>' */
        R_ANGLE__TYPE_ARGS,
        /** 'class MyClass&lt;T extends Number> {' */
        R_ANGLE__TYPE_PARAMS,
        /** 'Object[]' */
        R_BRACK__ARRAY_DECL,
        /** 'a[3]' */
        R_BRACK__INDEX,
        /** '@SuppressWarnings({ "foo", "bar" })' */
        R_CURLY__ANNO_ARRAY_INIT,
        /** 'new Object() { ... }' */
        R_CURLY__ANON_CLASS,
        /** 'int[] ia = { 1, 2 }', 'new int[] { 1, 2 }' */
        R_CURLY__ARRAY_INIT,
        /** '{ int i = 0; i++; }' */
        R_CURLY__BLOCK,
        /** 'try { ... } catch (...) { ...' */
        R_CURLY__CATCH,
        /** 'do { ... } while (...);' */
        R_CURLY__DO,
        /** 'else { ... }' */
        R_CURLY__ELSE,
        /** '@SuppressWarnings({})' */
        R_CURLY__EMPTY_ANNO_ARRAY_INIT,
        /** 'new Object() {}' */
        R_CURLY__EMPTY_ANON_CLASS,
        /** 'int[] ia = {}', 'new int[] {}' */
        R_CURLY__EMPTY_ARRAY_INIT,
        /** 'try { ... } catch (...) {}' */
        R_CURLY__EMPTY_CATCH,
        /** 'public MyClass(...) {}', 'public method(...) {}' */
        R_CURLY__EMPTY_METH_DECL,
        /** 'class MyClass {}', 'interface MyInterface {}', '@interface MyAnnotation {}', 'enum MyEnum {}' */
        R_CURLY__EMPTY_TYPE_DEF,
        /** 'enum MyEnum { FOO { ... } }' */
        R_CURLY__ENUM_CONST_DEF,
        /** 'finally { ... }' */
        R_CURLY__FINALLY,
        /** 'for (...) { ... }' */
        R_CURLY__FOR,
        /** 'if (...) { ... }' */
        R_CURLY__IF,
        /** 'class MyClass { { ... } }' */
        R_CURLY__INSTANCE_INIT,
        /** 'LABEL: { ... }' */
        R_CURLY__LABELED_STAT,
        /** 'public MyClass(...) { ... }', 'public method(...) { ... }' */
        R_CURLY__METH_DECL,
        /** 'class MyClass { static { ... } }' */
        R_CURLY__STATIC_INIT,
        /** 'switch (a) { ... }' */
        R_CURLY__SWITCH,
        /** 'synchronized (a) { ... }' */
        R_CURLY__SYNCHRONIZED,
        /** 'try { ... }' */
        R_CURLY__TRY,
        /** 'class MyClass { ... }', 'interface MyInter { ... }', '@interface MyAnno { ... }', 'enum MyEnum { ... }' */
        R_CURLY__TYPE_DEF,
        /** 'while (...) { ... }' */
        R_CURLY__WHILE,
        /** '@SuppressWarnings("foo")' */
        R_PAREN__ANNO,
        /** 'interface @MyAnno { String engineer(); }' */
        R_PAREN__ANNO_ELEM_DECL,
        /** '(int) a' */
        R_PAREN__CAST,
        /** 'try { ... } catch (Exception e) {' */
        R_PAREN__CATCH,
        /** 'do { ... } while (...); */
        R_PAREN__DO_WHILE,
        /** 'for (int i = 0; i &lt; 10; i++) {' */
        R_PAREN__FOR,
        /** 'for (int i = 0; i &lt; 10;) {' */
        R_PAREN__FOR_NO_UPDATE,
        /** 'if (...) {' */
        R_PAREN__IF,
        /** 'a()' */
        R_PAREN__METH_INVOCATION,
        /** 'void meth(...) {' */
        R_PAREN__PARAMS,
        /** '(a + b) * c' */
        R_PAREN__PARENTHESIZED,
        /** 'return x;' */
        RETURN__EXPR,
        /** 'return;' */
        RETURN__NO_EXPR,
        /** '>>' */
        RIGHT_SHIFT,
        /** 'a >>= b'*/
        RIGHT_SHIFT_ASSIGN,
        /** 'abstract meth(); */
        SEMI__ABSTRACT_METH_DEF,
        /** 'interface @MyAnno { String engineer(); }' */
        SEMI__ANNO_ELEM_DECL,
        /** ';' */
        SEMI__EMPTY_STAT,
        /** 'enum MyEnum { 1, B, C; ... }' */
        SEMI__ENUM_DEF,
        /** 'public int i;' */
        SEMI__FIELD_DEF,
        /** 'for (...; i < 3;) {' */
        SEMI__FOR_CONDITION_NO_UPDATE,
        /** 'for (...; i < 3; i++) {' */
        SEMI__FOR_CONDITION_UPDATE,
        /** 'for (int i = 0; i < 3;...' */
        SEMI__FOR_INIT_CONDITION,
        /** 'for (int i = 0;;...' */
        SEMI__FOR_INIT_NO_CONDITION,
        /** 'for (...;;) {' */
        SEMI__FOR_NO_CONDITION_NO_UPDATE,
        /** 'for (...;; i++) {'*/
        SEMI__FOR_NO_CONDITION_UPDATE,
        /** 'for (; ...; ...) {' */
        SEMI__FOR_NO_INIT_CONDITION,
        /** 'for (;;...) {' */
        SEMI__FOR_NO_INIT_NO_CONDITION,
        /** 'import pkg.*;', 'import pkg.MyClass;' */
        SEMI__IMPORT,
        /** 'package pkg.pkg;' */
        SEMI__PACKAGE_DECL,
        /** 'a = 7;' */
        SEMI__STATEMENT,
        /** 'import static MyClass.*;' */
        SEMI__STATIC_IMPORT,
        /** 'class MyClass { ; }' */
        SEMI__TYPE_DECL,
        /** 'short' */
        SHORT,
        /** 'import pkg.pkg.*;' */
        STAR__TYPE_IMPORT_ON_DEMAND,
        /** 'static int x;', 'static class MyClass {', 'static void meth() {' */
        STATIC__MOD,
        /** 'class MyClass { static { ... } }' */
        STATIC__STATIC_INIT,
        /** '"hello"' */
        STRING_LITERAL,
        /** 'super(x, y);' */
        SUPER__CTOR_CALL,
        /** 'super.meth();' */
        SUPER__EXPR,
        /** 'List<T super MyClass>' */
        SUPER__TYPE_BOUND,
        /** 'switch (a) {' */
        SWITCH,
        /** 'synchronized Object o;' */
        SYNCHRONIZED,
        /** 'this(a, b);' */
        THIS__CTOR_CALL,
        /** 'this.meth()', 'this.field' */
        THIS__EXPR,
        /** 'throw' */
        THROW,
        /** 'throws' */
        THROWS,
        /** 'transient' */
        TRANSIENT,
        /** 'true' */
        TRUE,
        /** 'try { ... } catch (...) { ... }' */
        TRY,
        /** '>>>' */
        UNSIGNED_RIGHT_SHIFT,
        /** 'a >>>= b'*/
        UNSIGNED_RIGHT_SHIFT_ASSIGN,
        /** 'void meth() {', 'void.class' */
        VOID,
        /** 'volatile' */
        VOLATILE,
        /** 'do { ... } while (a > 0);' */
        WHILE__DO,
        /** 'while (a > 0) { ... }' */
        WHILE__WHILE,
        /** 'a ^ b' */
        XOR,
        /** 'a ^= b'*/
        XOR_ASSIGN,

        // CHECKSTYLE __:OFF
    }

    private EnumSet<JavaElement> whitespaceBefore = EnumSet.of(
        AND__EXPR,
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
        L_CURLY__EMPTY_METH_DEF,
        L_CURLY__EMPTY_TYPE_DEF,
        L_CURLY__ENUM_CONST_DEF,
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
        NAME__CTOR_DEF,
        NAME__METH_DECL,
        NAME__PARAM,
        NAME__TYPE_DECL,
        NAME__VAR_DEF,
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
        R_CURLY__TYPE_DEF,
        R_CURLY__WHILE,
        RETURN__EXPR,
        RETURN__NO_EXPR,
        RIGHT_SHIFT,
        RIGHT_SHIFT_ASSIGN,
        SEMI__TYPE_DECL,
        STATIC__STATIC_INIT,
        SUPER__CTOR_CALL,
        SUPER__TYPE_BOUND,
        SWITCH,
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
        R_CURLY__EMPTY_TYPE_DEF,
        R_CURLY__ENUM_CONST_DEF,
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
        SEMI__ABSTRACT_METH_DEF,
        SEMI__ANNO_ELEM_DECL,
        SEMI__ENUM_DEF,
        SEMI__FIELD_DEF,
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
        L_CURLY__ENUM_CONST_DEF,
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
        R_CURLY__EMPTY_TYPE_DEF,
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
        R_CURLY__TYPE_DEF,
        R_CURLY__WHILE,
        R_PAREN__CAST,
        R_PAREN__CATCH,
        R_PAREN__IF,
        RETURN__EXPR,
        RIGHT_SHIFT,
        RIGHT_SHIFT_ASSIGN,
        SEMI__ABSTRACT_METH_DEF,
        SEMI__ANNO_ELEM_DECL,
        SEMI__EMPTY_STAT,
        SEMI__ENUM_DEF,
        SEMI__FIELD_DEF,
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
        STATIC__STATIC_INIT,
        SUPER__TYPE_BOUND,
        SWITCH,
        SYNCHRONIZED,
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
        L_CURLY__EMPTY_METH_DEF,
        L_CURLY__EMPTY_TYPE_DEF,
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
        NAME__CTOR_DEF,
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
    // CHECKSTYLE JavadocMethod:OFF
    public void setWhitespaceBefore(String[] sa)   { this.whitespaceBefore   = toEnumSet(sa, JavaElement.class); }
    public void setNoWhitespaceBefore(String[] sa) { this.noWhitespaceBefore = toEnumSet(sa, JavaElement.class); }
    public void setWhitespaceAfter(String[] sa)    { this.whitespaceAfter    = toEnumSet(sa, JavaElement.class); }
    public void setNoWhitespaceAfter(String[] sa)  { this.noWhitespaceAfter  = toEnumSet(sa, JavaElement.class); }
    // CHECKSTYLE JavadocMethod:ON
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
        for (String value : values) result.add(toEnum(value, enumClass));
        return result;
    }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        @SuppressWarnings("unused") AstDumper dumper = new AstDumper(ast); // For debugging

        JavaElement whitespaceable = toJavaElement(ast);

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

        final String line = getLines()[ast.getLineNo() - 1];

        // Check whitespace BEFORE token.
        if (mustBeWhitespaceBefore || mustNotBeWhitespaceBefore) {
            int before = ast.getColumnNo() - 1;

            if (before > 0 && !LINE_PREFIX.matcher(line).region(0, before).matches()) {
                boolean isWhitespace = Character.isWhitespace(line.charAt(before));
                if (mustBeWhitespaceBefore && !isWhitespace) {
                    log(ast, "de.unkrig.cscontrib.checks.Whitespace.notPreceded", ast.getText(), whitespaceable);
                } else
                if (mustNotBeWhitespaceBefore && isWhitespace) {
                    log(ast, "de.unkrig.cscontrib.checks.Whitespace.preceded", ast.getText(), whitespaceable);
                }
            }
        }

        // Check whitespace AFTER token.
        if (mustBeWhitespaceAfter || mustNotBeWhitespaceAfter) {
            int after = ast.getColumnNo() + ast.getText().length();

            if (after < line.length() && !LINE_SUFFIX.matcher(line).region(after, line.length()).matches()) {
                boolean isWhitespace = Character.isWhitespace(line.charAt(after));
                if (mustBeWhitespaceAfter && !isWhitespace) {
                    log(
                        ast.getLineNo(),
                        after,
                        "de.unkrig.cscontrib.checks.Whitespace.notFollowed",
                        ast.getText(),
                        whitespaceable
                    );
                } else
                if (mustNotBeWhitespaceAfter && isWhitespace) {
                    log(
                        ast.getLineNo(),
                        after,
                        "de.unkrig.cscontrib.checks.Whitespace.followed",
                        ast.getText(),
                        whitespaceable
                    );
                }
            }
        }
    }

    private static JavaElement
    toJavaElement(DetailAST ast) {
    
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
            return L_BRACK__ARRAY_DECL;
    
        case TokenTypes.ARRAY_INIT:
            return firstChildType == TokenTypes.RCURLY ? L_CURLY__EMPTY_ARRAY_INIT : L_CURLY__ARRAY_INIT;
    
        case TokenTypes.ASSIGN:
            return parentType == TokenTypes.VARIABLE_DEF ? ASSIGN__VAR_DECL : ASSIGN__ASSIGNMENT;
    
        case TokenTypes.BAND_ASSIGN:  return AND_ASSIGN;
        case TokenTypes.BOR_ASSIGN:   return OR_ASSIGN;
        case TokenTypes.BSR_ASSIGN:   return UNSIGNED_RIGHT_SHIFT_ASSIGN;
        case TokenTypes.BXOR_ASSIGN:  return XOR_ASSIGN;
        case TokenTypes.DIV_ASSIGN:   return DIVIDE_ASSIGN;
        case TokenTypes.MINUS_ASSIGN: return MINUS_ASSIGN;
        case TokenTypes.MOD_ASSIGN:   return MODULO_ASSIGN;
        case TokenTypes.PLUS_ASSIGN:  return PLUS_ASSIGN;
        case TokenTypes.SL_ASSIGN:    return LEFT_SHIFT_ASSIGN;
        case TokenTypes.SR_ASSIGN:    return RIGHT_SHIFT_ASSIGN;
        case TokenTypes.STAR_ASSIGN:  return MULTIPLY_ASSIGN;
    
        case TokenTypes.AT:
            if (parentType == TokenTypes.ANNOTATION)     return AT__ANNO;
            if (parentType == TokenTypes.ANNOTATION_DEF) return AT__ANNO_DECL;
            assert false : "AT has unexpected parent '" + ast.getParent() + "'";
            return null;
    
        case TokenTypes.BAND: return AND__EXPR;
        case TokenTypes.BOR:  return OR;
        case TokenTypes.BXOR: return XOR;
        case TokenTypes.BNOT: return BITWISE_COMPLEMENT;

        case TokenTypes.LAND:     return CONDITIONAL_AND;
        case TokenTypes.LOR:      return CONDITIONAL_OR;
        case TokenTypes.LNOT:     return LOGICAL_COMPLEMENT;
        case TokenTypes.TYPECAST: return L_PAREN__CAST;
    
        case TokenTypes.COLON:
            if (parentType == TokenTypes.LITERAL_DEFAULT) return COLON__DEFAULT;
            if (parentType == TokenTypes.LITERAL_CASE)    return COLON__CASE;
            if (parentType == TokenTypes.FOR_EACH_CLAUSE) return COLON__ENHANCED_FOR;
            return COLON__TERNARY;
    
        case TokenTypes.COMMA: return COMMA;

        case TokenTypes.DEC:      return PRE_DECR;
        case TokenTypes.INC:      return PRE_INCR;
        case TokenTypes.POST_DEC: return POST_DECR;
        case TokenTypes.POST_INC: return POST_INCR;
    
        case TokenTypes.STAR: return parentType == TokenTypes.DOT ? STAR__TYPE_IMPORT_ON_DEMAND : MULTIPLY;
        case TokenTypes.DIV:  return DIVIDE;
        case TokenTypes.MOD:  return MODULO;
    
        case TokenTypes.PLUS:  return PLUS__ADDITIVE;
        case TokenTypes.MINUS: return MINUS__ADDITIVE;
    
        case TokenTypes.UNARY_PLUS:  return PLUS__UNARY;
        case TokenTypes.UNARY_MINUS: return MINUS__UNARY;
    
        case TokenTypes.DOT:
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.PACKAGE_DEF) return DOT__PACKAGE_DECL;
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.IMPORT)      return DOT__IMPORT;
            if (
                getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR, TokenTypes.DOT) == TokenTypes.TYPE
            ) return DOT__QUALIFIED_TYPE;
            return DOT__SELECTOR;
    
        case TokenTypes.EMPTY_STAT: return SEMI__EMPTY_STAT;
    
        case TokenTypes.LT:        return LESS;
        case TokenTypes.LE:        return LESS_EQUAL;
        case TokenTypes.EQUAL:     return EQUAL;
        case TokenTypes.NOT_EQUAL: return NOT_EQUAL;
        case TokenTypes.GE:        return GREATER_EQUAL;
        case TokenTypes.GT:        return GREATER;
    
        case TokenTypes.GENERIC_END:
            if (parentType == TokenTypes.TYPE_PARAMETERS) {
                return (
                    grandparentType == TokenTypes.METHOD_DEF
                ) ? R_ANGLE__METH_DECL_TYPE_PARAMS : R_ANGLE__TYPE_ARGS;
            }
            if (parentType == TokenTypes.TYPE_ARGUMENTS) {
                return (
                    grandparentType == TokenTypes.TYPE
                    || grandparentType == TokenTypes.LITERAL_NEW
                    || grandparentType == TokenTypes.EXTENDS_CLAUSE
                    || grandparentType == TokenTypes.IMPLEMENTS_CLAUSE
                ) ? R_ANGLE__TYPE_ARGS : R_ANGLE__METH_INVOCATION_TYPE_ARGS;
            }
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;
            
        case TokenTypes.GENERIC_START:
            if (parentType == TokenTypes.TYPE_PARAMETERS) {
                return (
                    grandparentType == TokenTypes.METHOD_DEF
                ) ? L_ANGLE__METH_DECL_TYPE_PARAMS : L_ANGLE__TYPE_ARGS;
            }
            if (parentType == TokenTypes.TYPE_ARGUMENTS) {
                return (
                    grandparentType == TokenTypes.TYPE
                    || grandparentType == TokenTypes.LITERAL_NEW
                    || grandparentType == TokenTypes.EXTENDS_CLAUSE
                    || grandparentType == TokenTypes.IMPLEMENTS_CLAUSE
                ) ? L_ANGLE__TYPE_ARGS : L_ANGLE__METH_INVOCATION_TYPE_ARGS;
            }
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;
    
        case TokenTypes.IDENT:
            if (
                parentType == TokenTypes.CLASS_DEF
                || parentType == TokenTypes.INTERFACE_DEF
                || parentType == TokenTypes.ANNOTATION_DEF
                || parentType == TokenTypes.ENUM_DEF
            ) return NAME__TYPE_DECL;
            if (parentType == TokenTypes.ANNOTATION)           return NAME__ANNO;
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) return NAME__ANNO_ELEM_DECL;
            if (parentType == TokenTypes.VARIABLE_DEF)         return NAME__VAR_DEF;
            if (parentType == TokenTypes.CTOR_DEF)             return NAME__CTOR_DEF;
            if (parentType == TokenTypes.METHOD_DEF)           return NAME__METH_DECL;
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.PACKAGE_DEF) return NAME__PACKAGE_DECL;
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.IMPORT) {
                return ast.getNextSibling() == null ? NAME__IMPORT_TYPE : NAME__IMPORT_COMPONENT;
            }
            if (
                getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR) == TokenTypes.TYPE
                || getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR) == TokenTypes.LITERAL_NEW
            ) return NAME__SIMPLE_TYPE;
            if (
                getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR, TokenTypes.DOT) == TokenTypes.TYPE
            ) return NAME__QUALIFIED_TYPE;
            if (parentType == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) return NAME__ANNO_MEMBER;
            if (parentType == TokenTypes.PARAMETER_DEF) return NAME__PARAM;
            return NAME__AMBIGUOUS;
    
        case TokenTypes.LCURLY:
            if (parentType == TokenTypes.OBJBLOCK && (
                grandparentType == TokenTypes.CLASS_DEF
                || grandparentType == TokenTypes.INTERFACE_DEF
                || grandparentType == TokenTypes.ANNOTATION_DEF
                || grandparentType == TokenTypes.ENUM_DEF
            )) return nextSiblingType == TokenTypes.RCURLY ? L_CURLY__EMPTY_TYPE_DEF : L_CURLY__TYPE_DECL;
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.LITERAL_NEW
            ) return nextSiblingType == TokenTypes.RCURLY ? L_CURLY__EMPTY_ANON_CLASS : L_CURLY__ANON_CLASS;
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.ENUM_CONSTANT_DEF
            ) return L_CURLY__ENUM_CONST_DEF;
            if (
                parentType == TokenTypes.ARRAY_INIT
            ) return nextSiblingType == TokenTypes.RCURLY ? L_CURLY__EMPTY_ARRAY_INIT : L_CURLY__ARRAY_INIT;
            if (parentType == TokenTypes.LITERAL_SWITCH) return L_CURLY__SWITCH;
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;
    
        case TokenTypes.ANNOTATION_ARRAY_INIT:
            return (
                firstChildType == TokenTypes.RCURLY
                ? L_CURLY__EMPTY_ANNO_ARRAY_INIT
                : L_CURLY__ANNO_ARRAY_INIT
            );
    
        case TokenTypes.INDEX_OP: return L_BRACK__INDEX;

        case TokenTypes.IMPLEMENTS_CLAUSE: return IMPLEMENTS;
    
        case TokenTypes.LITERAL_BYTE:    return BYTE;
        case TokenTypes.LITERAL_SHORT:   return SHORT;
        case TokenTypes.LITERAL_INT:     return INT;
        case TokenTypes.LITERAL_LONG:    return LONG;
        case TokenTypes.LITERAL_CHAR:    return CHAR;
        case TokenTypes.LITERAL_FLOAT:   return FLOAT;
        case TokenTypes.LITERAL_DOUBLE:  return DOUBLE;
        case TokenTypes.LITERAL_BOOLEAN: return BOOLEAN;
        case TokenTypes.LITERAL_VOID:    return VOID;

        case TokenTypes.CHAR_LITERAL:   return CHAR_LITERAL;
        case TokenTypes.LITERAL_FALSE:  return FALSE;
        case TokenTypes.LITERAL_TRUE:   return TRUE;
        case TokenTypes.LITERAL_NULL:   return NULL;
        case TokenTypes.NUM_DOUBLE:     return DOUBLE_LITERAL;
        case TokenTypes.NUM_FLOAT:      return FLOAT_LITERAL;
        case TokenTypes.NUM_INT:        return INT_LITERAL;
        case TokenTypes.NUM_LONG:       return LONG_LITERAL;
        case TokenTypes.STRING_LITERAL: return STRING_LITERAL;

        case TokenTypes.IMPORT: return IMPORT;

        case TokenTypes.DO_WHILE:         return WHILE__DO;
        case TokenTypes.LITERAL_ASSERT:   return ASSERT;
        case TokenTypes.LITERAL_BREAK:    return BREAK;
        case TokenTypes.LITERAL_CASE:     return CASE;
        case TokenTypes.LITERAL_CATCH:    return CATCH;
        case TokenTypes.LITERAL_CONTINUE: return CONTINUE;
        case TokenTypes.LITERAL_DO:       return DO;
        case TokenTypes.LITERAL_ELSE:     return ELSE;
        case TokenTypes.LITERAL_FINALLY:  return FINALLY;
        case TokenTypes.LITERAL_FOR:      return FOR;
        case TokenTypes.LITERAL_IF:       return IF;
        case TokenTypes.LITERAL_SWITCH:   return SWITCH;
        case TokenTypes.LITERAL_THROW:    return THROW;
        case TokenTypes.LITERAL_TRY:      return TRY;
        case TokenTypes.LITERAL_RETURN:   return firstChildType == TokenTypes.SEMI ? RETURN__NO_EXPR : RETURN__EXPR;
        case TokenTypes.LITERAL_WHILE:    return WHILE__WHILE;
    
        case TokenTypes.LITERAL_CLASS: 
            return parentType == TokenTypes.CLASS_DEF ? CLASS__CLASS_DECL : CLASS__CLASS_LITERAL;

        case TokenTypes.LITERAL_INTERFACE: return INTERFACE;
        case TokenTypes.ENUM:              return ENUM;
    
        case TokenTypes.LITERAL_DEFAULT:
            return (
                parentType == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR || parentType == TokenTypes.ANNOTATION_FIELD_DEF
            ) ? DEFAULT__ANNO_ELEM : DEFAULT__SWITCH;
    
        case TokenTypes.LITERAL_INSTANCEOF: return INSTANCEOF;
        case TokenTypes.LITERAL_NEW:        return NEW;
        case TokenTypes.LITERAL_SUPER:      return SUPER__EXPR;
        case TokenTypes.LITERAL_THIS:       return THIS__EXPR;
        case TokenTypes.LITERAL_THROWS:     return THROWS;

        case TokenTypes.ABSTRACT:             return ABSTRACT;
        case TokenTypes.FINAL:                return FINAL;
        case TokenTypes.LITERAL_NATIVE:       return NATIVE;
        case TokenTypes.LITERAL_PRIVATE:      return PRIVATE;
        case TokenTypes.LITERAL_PROTECTED:    return PROTECTED;
        case TokenTypes.LITERAL_PUBLIC:       return PUBLIC;
        case TokenTypes.LITERAL_STATIC:       return STATIC__MOD;
        case TokenTypes.LITERAL_SYNCHRONIZED: return SYNCHRONIZED;
        case TokenTypes.LITERAL_TRANSIENT:    return TRANSIENT;
        case TokenTypes.LITERAL_VOLATILE:     return VOLATILE;
    
        case TokenTypes.PACKAGE_DEF:   return PACKAGE;
        case TokenTypes.STATIC_IMPORT: return IMPORT__STATIC_IMPORT;
        case TokenTypes.STATIC_INIT:   ast.setText("static"); return STATIC__STATIC_INIT;
    
        case TokenTypes.LPAREN:
            if (parentType == TokenTypes.ANNOTATION) return L_PAREN__ANNO;
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) return L_PAREN__ANNO_ELEM_DECL;
            if (nextSiblingType == TokenTypes.PARAMETERS) return L_PAREN__PARAMS;
            if (
                parentType == TokenTypes.SUPER_CTOR_CALL || parentType == TokenTypes.LITERAL_NEW
            ) return L_PAREN__METH_INVOCATION;
            if (parentType == TokenTypes.LITERAL_DO) return L_PAREN__DO_WHILE;
            if (parentType == TokenTypes.LITERAL_IF) return L_PAREN__IF;
            if (parentType == TokenTypes.LITERAL_FOR) {
                return ast.getNextSibling().getFirstChild() == null ? L_PAREN__FOR_NO_INIT : L_PAREN__FOR;
            }
            if (parentType == TokenTypes.LITERAL_CATCH) return L_PAREN__CATCH;
            return L_PAREN__PARENTHESIZED;
    
        case TokenTypes.METHOD_CALL: return L_PAREN__METH_INVOCATION;
    
        case TokenTypes.QUESTION: return QUESTION__TERNARY;

        case TokenTypes.RBRACK:
            if (parentType == TokenTypes.ARRAY_DECLARATOR) return R_BRACK__ARRAY_DECL;
            if (parentType == TokenTypes.INDEX_OP)         return R_BRACK__INDEX;
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;
    
        case TokenTypes.RCURLY:
            if (
                parentType == TokenTypes.SLIST
                && grandparentType == TokenTypes.LITERAL_CATCH
            ) return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_CATCH : R_CURLY__CATCH;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_SYNCHRONIZED
            ) return R_CURLY__SYNCHRONIZED;
            if (
                parentType == TokenTypes.OBJBLOCK
                && (
                    grandparentType == TokenTypes.CLASS_DEF
                    || grandparentType == TokenTypes.INTERFACE_DEF
                    || grandparentType == TokenTypes.ANNOTATION_DEF
                    || grandparentType == TokenTypes.ENUM_DEF
                )
            ) return previousSiblingType == TokenTypes.LCURLY ? R_CURLY__EMPTY_TYPE_DEF : R_CURLY__TYPE_DEF;
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.LITERAL_NEW 
            ) return previousSiblingType == TokenTypes.LCURLY ? R_CURLY__EMPTY_ANON_CLASS : R_CURLY__ANON_CLASS;
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.ENUM_CONSTANT_DEF
            ) return R_CURLY__ENUM_CONST_DEF;
            if (
                parentType == TokenTypes.SLIST
                && (grandparentType == TokenTypes.CTOR_DEF || grandparentType == TokenTypes.METHOD_DEF)
            ) return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_METH_DECL : R_CURLY__METH_DECL;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_FOR
            ) return R_CURLY__FOR;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_IF
            ) return R_CURLY__IF;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_ELSE
            ) return R_CURLY__IF;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_WHILE
            ) return R_CURLY__WHILE;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_DO
            ) return R_CURLY__DO;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_TRY
            ) return R_CURLY__TRY;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LITERAL_FINALLY
            ) return R_CURLY__FINALLY;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.LABELED_STAT
            ) return R_CURLY__LABELED_STAT;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.SLIST
            ) return R_CURLY__BLOCK;
            if (parentType == TokenTypes.LITERAL_SWITCH) return R_CURLY__SWITCH;
            if (parentType == TokenTypes.ARRAY_INIT) {
                return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_ARRAY_INIT : R_CURLY__ARRAY_INIT;
            } 
            if (parentType == TokenTypes.ANNOTATION_ARRAY_INIT) {
                return (
                    ast.getPreviousSibling() == null
                    ? R_CURLY__EMPTY_ANNO_ARRAY_INIT
                    : R_CURLY__ANNO_ARRAY_INIT
                );
            }
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.STATIC_INIT
            ) return R_CURLY__STATIC_INIT;
            if (
                parentType == TokenTypes.SLIST && grandparentType == TokenTypes.INSTANCE_INIT
            ) return R_CURLY__INSTANCE_INIT;
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;
    
        case TokenTypes.RPAREN:
            if (parentType == TokenTypes.ANNOTATION) return R_PAREN__ANNO;
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) return R_PAREN__ANNO_ELEM_DECL;
            if (parentType == TokenTypes.CTOR_DEF || parentType == TokenTypes.METHOD_DEF) return R_PAREN__PARAMS;
            if (
                parentType == TokenTypes.SUPER_CTOR_CALL
                || parentType == TokenTypes.LITERAL_NEW
                || parentType == TokenTypes.METHOD_CALL
            ) return R_PAREN__METH_INVOCATION;
            if (parentType == TokenTypes.LITERAL_IF) return R_PAREN__IF;
            if (parentType == TokenTypes.LITERAL_DO) return R_PAREN__DO_WHILE;
            if (parentType == TokenTypes.LITERAL_FOR) {
                return ast.getPreviousSibling().getFirstChild() == null ? R_PAREN__FOR_NO_UPDATE : R_PAREN__FOR;
            }
            if (parentType == TokenTypes.LITERAL_CATCH) return R_PAREN__CATCH;
            if (previousSiblingType == TokenTypes.TYPE) return R_PAREN__CAST;
            return R_PAREN__PARENTHESIZED;
    
        case TokenTypes.SEMI:
            if (parentType == TokenTypes.PACKAGE_DEF) return SEMI__PACKAGE_DECL;
            if (parentType == TokenTypes.IMPORT) return SEMI__IMPORT;
            if (parentType == TokenTypes.STATIC_IMPORT) return SEMI__STATIC_IMPORT;
            if (parentType == TokenTypes.OBJBLOCK) return SEMI__TYPE_DECL;
            if (
                parentType == TokenTypes.SLIST
                || parentType == TokenTypes.SUPER_CTOR_CALL
                || parentType == TokenTypes.CTOR_CALL
                || parentType == TokenTypes.LITERAL_DO
                || (parentType == TokenTypes.LITERAL_FOR && nextSiblingType == -1)
                || parentType == TokenTypes.LITERAL_RETURN
                || parentType == TokenTypes.LITERAL_BREAK
                || parentType == TokenTypes.LITERAL_CONTINUE
                || parentType == TokenTypes.LITERAL_IF
                || parentType == TokenTypes.LITERAL_WHILE
                || parentType == TokenTypes.LITERAL_ASSERT
                || parentType == TokenTypes.LITERAL_THROW
            ) return SEMI__STATEMENT;
            if (parentType == TokenTypes.METHOD_DEF) return SEMI__ABSTRACT_METH_DEF;
            if (previousSiblingType == TokenTypes.FOR_INIT) {
                return ast.getPreviousSibling().getFirstChild() == null ? (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI__FOR_NO_INIT_NO_CONDITION
                    : SEMI__FOR_NO_INIT_CONDITION
                ) : (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI__FOR_INIT_NO_CONDITION
                    : SEMI__FOR_INIT_CONDITION
                );
            }
            if (previousSiblingType == TokenTypes.FOR_CONDITION) {
                return ast.getPreviousSibling().getFirstChild() == null ? (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI__FOR_NO_CONDITION_NO_UPDATE
                    : SEMI__FOR_NO_CONDITION_UPDATE
                ) : (
                    ast.getNextSibling().getFirstChild() == null
                    ? SEMI__FOR_CONDITION_NO_UPDATE
                    : SEMI__FOR_CONDITION_UPDATE
                );
            }
            if (parentType == TokenTypes.ANNOTATION_FIELD_DEF) return SEMI__ANNO_ELEM_DECL;
            if (
                parentType == TokenTypes.OBJBLOCK && grandparentType == TokenTypes.ENUM_DEF
            ) return SEMI__ENUM_DEF;
            if (
                parentType == TokenTypes.VARIABLE_DEF && grandparentType == TokenTypes.OBJBLOCK
            ) return SEMI__FIELD_DEF;
            assert false : "'" + ast + "' has unexpected parent '" + ast.getParent() + "'";
            return null;
    
        case TokenTypes.SLIST:
            if (parentType == TokenTypes.STATIC_INIT) return L_CURLY__STATIC_INIT;
            if (parentType == TokenTypes.INSTANCE_INIT) return L_CURLY__INSTANCE_INIT;
            if (parentType == TokenTypes.LITERAL_IF) return L_CURLY__IF;
            if (parentType == TokenTypes.LITERAL_ELSE) return R_CURLY__ELSE;
            if (parentType == TokenTypes.LITERAL_DO) return L_CURLY__DO;
            if (parentType == TokenTypes.LITERAL_WHILE) return L_CURLY__WHILE;
            if (parentType == TokenTypes.LITERAL_FOR) return L_CURLY__FOR;
            if (parentType == TokenTypes.LITERAL_TRY) return L_CURLY__TRY;
            if (parentType == TokenTypes.LITERAL_CATCH) { 
                return firstChildType == TokenTypes.RCURLY ? L_CURLY__EMPTY_CATCH : L_CURLY__CATCH;
            }
            if (parentType == TokenTypes.LITERAL_FINALLY) return L_CURLY__FINALLY;
            if (parentType == TokenTypes.LITERAL_SYNCHRONIZED) return L_CURLY__SYNCHRONIZED;
            if (parentType == TokenTypes.LABELED_STAT) return L_CURLY__LABELED_STAT;
            if (parentType == TokenTypes.SLIST) return L_CURLY__BLOCK;
            if (parentType == TokenTypes.CTOR_DEF || parentType == TokenTypes.METHOD_DEF) {
                return (
                    firstChildType == TokenTypes.RCURLY
                    ? JavaElement.L_CURLY__EMPTY_METH_DEF
                    : JavaElement.L_CURLY__METH_DECL
                );
            }
            return null; // Not a 'physical' token.
    
        case TokenTypes.SL:                return LEFT_SHIFT;
        case TokenTypes.SR:                return RIGHT_SHIFT;
        case TokenTypes.BSR:               return UNSIGNED_RIGHT_SHIFT;
        case TokenTypes.ELLIPSIS:          return ELLIPSIS;
        case TokenTypes.CTOR_CALL:         return THIS__CTOR_CALL;
        case TokenTypes.SUPER_CTOR_CALL:   return SUPER__CTOR_CALL;
        case TokenTypes.TYPE_UPPER_BOUNDS: return EXTENDS__TYPE_BOUND;
        case TokenTypes.TYPE_LOWER_BOUNDS: return SUPER__TYPE_BOUND;
        case TokenTypes.WILDCARD_TYPE:     return QUESTION__WILDCARD_TYPE;
        case TokenTypes.EXTENDS_CLAUSE:    return EXTENDS__TYPE;
        case TokenTypes.LABELED_STAT:      return COLON__LABELED_STAT;
            
        // These are the 'virtual' tokens, i.e. token which are not uniquely related to a physical token:
        case TokenTypes.ANNOTATION:
        case TokenTypes.ANNOTATION_DEF:
        case TokenTypes.ANNOTATION_FIELD_DEF:
        case TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR:
        case TokenTypes.ANNOTATIONS:
        case TokenTypes.CASE_GROUP:
        case TokenTypes.CLASS_DEF:
        case TokenTypes.CTOR_DEF:
        case TokenTypes.ELIST:
        case TokenTypes.ENUM_DEF:
        case TokenTypes.ENUM_CONSTANT_DEF:
        case TokenTypes.EXPR:
        case TokenTypes.FOR_EACH_CLAUSE:
        case TokenTypes.FOR_INIT:
        case TokenTypes.FOR_CONDITION:
        case TokenTypes.FOR_ITERATOR:
        case TokenTypes.INTERFACE_DEF:
        case TokenTypes.INSTANCE_INIT:
        case TokenTypes.METHOD_DEF:
        case TokenTypes.MODIFIERS:
        case TokenTypes.OBJBLOCK:
        case TokenTypes.PARAMETER_DEF:
        case TokenTypes.PARAMETERS:
        case TokenTypes.RESOURCE:
        case TokenTypes.RESOURCE_SPECIFICATION:
        case TokenTypes.RESOURCES:
        case TokenTypes.STRICTFP:
        case TokenTypes.TYPE:
        case TokenTypes.TYPE_ARGUMENT:
        case TokenTypes.TYPE_ARGUMENTS:
        case TokenTypes.TYPE_EXTENSION_AND:
        case TokenTypes.TYPE_PARAMETER:
        case TokenTypes.TYPE_PARAMETERS:
        case TokenTypes.VARIABLE_DEF:
            return null;
    
        case TokenTypes.EOF:
            assert false : "Unexpected token '" + ast + "'";
            return null;
    
        default:
            assert false : "Unexpected DetailAST " + ast;
            return null;
        }
    }
    private static final Pattern LINE_PREFIX = Pattern.compile("\\s*");
    private static final Pattern LINE_SUFFIX = Pattern.compile("\\s*(?://.*)?");

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


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
        /** '<font color="red">abstract</font>' */
        ABSTRACT,
        /** 'a <font color="red">&amp;</font> b' */
        AND__EXPR,
        /** '&lt;T extends MyClass <font color="red">&amp;</font> MyInterface>' */
        AND__TYPE_BOUND,
        /** 'a <font color="red">&amp;=</font> b' */
        AND_ASSIGN,
        /** '<font color="red">assert</font> x == 0;'<br/>'<font color="red">assert</font> x == 0 : "x not zero";' */
        ASSERT,
        /** 'a <font color="red">=</font> 7;' */
        ASSIGN__ASSIGNMENT,
        /** 'int a <font color="red">=</font> 7;' */
        ASSIGN__VAR_DECL,
        /** '<font color="red">@</font>MyAnno' */
        AT__ANNO,
        /** 'interface <font color="red">@</font>MyAnno {' */
        AT__ANNO_DECL,
        /** '<font color="red">~</font>a' */
        BITWISE_COMPLEMENT,
        /** '<font color="red">boolean</font>' */
        BOOLEAN,
        /** '<font color="red">break</font>;'<br/>'<font color="red">break</font> LABEL;' */
        BREAK,
        /** '<font color="red">byte</font>' */
        BYTE,
        /** '<font color="red">case</font> 7:' */
        CASE,
        /** '<font color="red">catch</font> (Exception e) {' */
        CATCH,
        /** '<font color="red">char</font>' */
        CHAR,
        /** '<font color="red">'c'</font>' */
        CHAR_LITERAL,
        /** '<font color="red">class</font> MyClass {' */
        CLASS__CLASS_DECL,
        /** 'Class c = Object.<font color="red">class</font>;' */
        CLASS__CLASS_LITERAL,
        /** 'case 77<font color="red">:</font>' */
        COLON__CASE,
        /** 'default<font color="red">:</font>' */
        COLON__DEFAULT,
        /** 'for (Object o <font color="red">:</font> list) {' */
        COLON__ENHANCED_FOR,
        /** 'LABEL<font color="red">:</font> while (...) {' */
        COLON__LABELED_STAT,
        /** 'a ? b <font color="red">:</font> c' */
        COLON__TERNARY,
        /** '<font color="red">,</font>' */
        COMMA,
        /** 'a <font color="red">&&</font> b' */
        CONDITIONAL_AND,
        /** 'a <font color="red">||</font> b' */
        CONDITIONAL_OR,
        /** '<font color="red">continue</font>;'<br/>'<font color="red">continue</font> LABEL;' */
        CONTINUE,
        /** 'interface @MyAnno { String engineer() <font color="red">default</font> "[unassigned]"; }' */
        DEFAULT__ANNO_ELEM,
        /** 'switch (x) { <font color="red">default</font>: break; }' */
        DEFAULT__SWITCH,
        /** 'a <font color="red">/</font> b' */
        DIVIDE,
        /** 'a <font color="red">/=</font> b' */
        DIVIDE_ASSIGN,
        /** '<font color="red">do</font> { ... } while (...);' */
        DO,
        /** 'import pkg<font color="red">.</font>*;'<br/>'import pkg<font color="red">.</font>Type;' */
        DOT__IMPORT,
        /** 'package pkg<font color="red">.</font>pkg;' */
        DOT__PACKAGE_DECL,
        /** 'pkg<font color="red">.</font>MyType'<br/>'pkg<font color="red">.</font>MyType[]' */
        DOT__QUALIFIED_TYPE,
        /** 'a<font color="red">.</font>b'<br/>'a<font color="red">.</font>b()' */
        DOT__SELECTOR,
        /** '<font color="red">double</font>' */
        DOUBLE,
        /**
         * '<font color="red">1.0</font>'<br/>
         * '<font color="red">.1</font>'<br/>
         * '<font color="red">1E3</font>'<br/>
         * '<font color="red">1D</font>'
         */
        DOUBLE_LITERAL,
        /** 'meth(Object<font color="red">...</font> o)' */
        ELLIPSIS,
        /** 'if (...) { ... } <font color="red">else</font> { ... }' */
        ELSE,
        /** 'public <font color="red">enum</font> Color { RED, BLUE, GREEN }' */
        ENUM,
        /** 'a <font color="red">==</font> b' */
        EQUAL,
        /** 'class MyClass <font color="red">extends</font> BaseClass {' */
        EXTENDS__TYPE,
        /** 'List&lt;T <font color="red">extends</font> MyClass>' */
        EXTENDS__TYPE_BOUND,
        /** '<font color="red">false</font>' */
        FALSE,
        /** '<font color="red">final</font>' */
        FINAL,
        /** 'try { ... } <font color="red">finally</font> { ... }' */
        FINALLY,
        /** '<font color="red">float</font>' */
        FLOAT,
        /** '<font color="red">1F</font>' */
        FLOAT_LITERAL,
        /**
         * '<font color="red">for</font> (int i = 0; i < 3; i++) {'<br/>
         * '<font color="red">for</font> (Object o : list) {'
         */
        FOR,
        /** 'a <font color="red">></font> b' */
        GREATER,
        /** 'a <font color="red">>=</font> b' */
        GREATER_EQUAL,
        /** '<font color="red">if</font> (a == 0) {' */
        IF,
        /** 'List&lt;T <font color="red">implements</font> MyInterface1, MyInterface2>' */
        IMPLEMENTS,
        /** '<font color="red">import</font> pkg.MyClass;'<br/>'<font color="red">import</font> pkg.*;' */
        IMPORT,
        /**
         * '<font color="red">import</font> static pkg.MyClass.member;'<br/>
         * '<font color="red">import</font> static pkg.MyClass.*;'
         */
        IMPORT__STATIC_IMPORT,
        /** 'a <font color="red">instanceof</font> MyClass' */
        INSTANCEOF,
        /** '<font color="red">int</font>' */
        INT,
        /**
         * '<font color="red">11</font>'<br/>
         * '<font color="red">0xB</font>'<br/>
         * '<font color="red">013</font>'<br/>
         * '<font color="red">0B1011</font>'
         */
        INT_LITERAL,
        /** 'public <font color="red">interface</font> MyInterface { ... }' */
        INTERFACE,
        /** 'public <font color="red">&lt;</font>T extends Number> void meth(T parm) {' */
        L_ANGLE__METH_DECL_TYPE_PARAMS,
        /** 'MyClass.<font color="red">&lt;</font>Double>meth(x)' */
        L_ANGLE__METH_INVOCATION_TYPE_ARGS,
        /** 'MyClass<font color="red">&lt;</font>String>' */
        L_ANGLE__TYPE_ARGS,
        /** 'class MyClass<font color="red">&lt;</font>T extends Number> {' */
        L_ANGLE__TYPE_PARAMS,
        /** 'Object<font color="red">[</font>]' */
        L_BRACK__ARRAY_DECL,
        /** 'a<font color="red">[</font>3]' */
        L_BRACK__INDEX,
        /** '@SuppressWarnings(<font color="red">{</font> "foo", "bar" })' */
        L_CURLY__ANNO_ARRAY_INIT,
        /** 'new Object() <font color="red">{</font> ... }' */
        L_CURLY__ANON_CLASS,
        /** 'int[] ia = <font color="red">{</font> 1, 2 }'<br/>'new int[] <font color="red">{</font> 1, 2 }' */
        L_CURLY__ARRAY_INIT,
        /** '<font color="red">{</font> int i = 0; i++; }' */
        L_CURLY__BLOCK,
        /** 'try { ... } catch (...) <font color="red">{</font> ...' */
        L_CURLY__CATCH,
        /** 'do <font color="red">{</font> ... } while (...);' */
        L_CURLY__DO,
        /** '@SuppressWarnings(<font color="red">{</font>})' */
        L_CURLY__EMPTY_ANNO_ARRAY_INIT,
        /** 'new Object() <font color="red">{</font>}' */
        L_CURLY__EMPTY_ANON_CLASS,
        /** 'int[] ia = <font color="red">{</font>};'<br/>'new int[] <font color="red">{</font>}' */
        L_CURLY__EMPTY_ARRAY_INIT,
        /** 'try { ... } catch (...) <font color="red">{</font>}' */
        L_CURLY__EMPTY_CATCH,
        /** 'void meth(...) <font color="red">{</font>}' */
        L_CURLY__EMPTY_METH_DECL,
        /**
         * 'class MyClass() <font color="red">{</font>}'<br/>
         * 'interface MyInterface() <font color="red">{</font>}'<br/>
         * 'interface @MyAnnotation <font color="red">{</font>}'<br/>
         * 'enum MyEnum <font color="red">{</font>}'
         */
        L_CURLY__EMPTY_TYPE_DECL,
        /** 'enum MyEnum { FOO <font color="red">{</font> ... } }' */
        L_CURLY__ENUM_CONST,
        /** 'finally <font color="red">{</font> ... }' */
        L_CURLY__FINALLY,
        /** 'for (...) <font color="red">{</font>' */
        L_CURLY__FOR,
        /** 'if (...) <font color="red">{</font>' */
        L_CURLY__IF,
        /** 'class MyClass { <font color="red">{</font> ... } }' */
        L_CURLY__INSTANCE_INIT,
        /** 'LABEL: <font color="red">{</font>' */
        L_CURLY__LABELED_STAT,
        /** 'void meth(...) <font color="red">{</font> ... }' */
        L_CURLY__METH_DECL,
        /** 'class MyClass { static <font color="red">{</font> ... } }' */
        L_CURLY__STATIC_INIT,
        /** 'switch (a) <font color="red">{</font>' */
        L_CURLY__SWITCH,
        /** 'synchronized (a) <font color="red">{</font>' */
        L_CURLY__SYNCHRONIZED,
        /** 'try <font color="red">{' */
        L_CURLY__TRY,
        /**
         * 'class MyClass() <font color="red">{</font>'<br/>
         * 'interface MyInterface() <font color="red">{</font>'<br/>
         * 'interface @MyAnno <font color="red">{</font>'<br/>
         * 'enum MyEnum <font color="red">{</font>'
         */
        L_CURLY__TYPE_DECL,
        /** 'while (...) <font color="red">{</font>' */
        L_CURLY__WHILE,
        /** '@SuppressWarnings<font color="red">(</font>"foo")' */
        L_PAREN__ANNO,
        /** 'interface @MyAnno { String engineer<font color="red">(</font>); }' */
        L_PAREN__ANNO_ELEM_DECL,
        /** '<font color="red">(</font>int) a' */
        L_PAREN__CAST,
        /** 'try { ... } catch <font color="red">(</font>Exception e) {' */
        L_PAREN__CATCH,
        /** 'do { ... } while <font color="red">(</font>...);' */
        L_PAREN__DO_WHILE,
        /** 'for <font color="red">(</font>int i = 0; i  10; i++) {' */
        L_PAREN__FOR,
        /** 'for <font color="red">(</font>; i  10; i++) {' */
        L_PAREN__FOR_NO_INIT,
        /** 'if <font color="red">(</font>...) {' */
        L_PAREN__IF,
        /** 'a<font color="red">(</font>x, y)' */
        L_PAREN__METH_INVOCATION,
        /** 'void meth<font color="red">(</font>int x, int y) {' */
        L_PAREN__PARAMS,
        /** '<font color="red">(</font>a + b) * c' */
        L_PAREN__PARENTHESIZED,
        /** 'a <font color="red">&lt;&lt;</font> 3' */
        LEFT_SHIFT,
        /** 'a <font color="red">&lt;&lt;=</font> 1' */
        LEFT_SHIFT_ASSIGN,
        /** 'a <font color="red">&lt;</font> b' */
        LESS,
        /** 'a <font color="red">&lt;=</font> b' */
        LESS_EQUAL,
        /** '<font color="red">!</font>a' */
        LOGICAL_COMPLEMENT,
        /** '<font color="red">long</font>' */
        LONG,
        /**
         * '<font color="red">11L</font>'<br/>
         * '<font color="red">0xBL</font>'<br/>
         * '<font color="red">013L</font>'<br/>
         * '<font color="red">0B1011L</font>'
         */
        LONG_LITERAL,
        /** 'a <font color="red">-</font> b' */
        MINUS__ADDITIVE,
        /** '<font color="red">-</font>a' */
        MINUS__UNARY,
        /** 'a <font color="red">-=</font> b' */
        MINUS_ASSIGN,
        /** 'a <font color="red">%</font> b' */
        MODULO,
        /** 'a <font color="red">%=</font> b' */
        MODULO_ASSIGN,
        /** 'a <font color="red">*</font> b' */
        MULTIPLY,
        /** 'a <font color="red">*=</font> b' */
        MULTIPLY_ASSIGN,
        /** '<font color="red">a</font>.<font color="red">b</font>.<font color="red">c</font>' */
        NAME__AMBIGUOUS,
        /** '@<font color="red">MyAnnotation</font>("x")' */
        NAME__ANNO,
        /** 'interface @MyAnno { String <font color="red">engineer</font>(); }' */
        NAME__ANNO_ELEM_DECL,
        /** '@MyAnnotation(<font color="red">value</font> = "x")' */
        NAME__ANNO_MEMBER,
        /** '<font color="red">MyClass</font>(...) {' */
        NAME__CTOR_DECL,
        /** 'import <font color="red">pkg</font>.<font color="red">pkg</font>.*;' */
        NAME__IMPORT_COMPONENT,
        /** 'import pkg.pkg.<font color="red">MyType</font>;' */
        NAME__IMPORT_TYPE,
        /** 'void <font color="red">main</font>(...) {' */
        NAME__METH_DECL,
        /** 'package <font color="red">pkg</font>.<font color="red">pkg</font>.<font color="red">pkg</font>;' */
        NAME__PACKAGE_DECL,
        /** 'void meth(String <font color="red">param</font>)' */
        NAME__PARAM,
        /** '<font color="red">pkg</font>.<font color="red">MyType</font>' */
        NAME__QUALIFIED_TYPE,
        /** '<font color="red">MyType</font> x;'<br/>'y = new <font color="red">MyType</font>();' */
        NAME__SIMPLE_TYPE,
        /**
         * 'class <font color="red">MyClass</font> { ... }'<br/>
         * 'interface <font color="red">MyInterface</font> { ... }'<br/>
         * 'interface @<font color="red">MyAnnotation</font> { ... }'<br/>
         * 'enum <font color="red">MyEnum</font> { ... }'
         */
        NAME__TYPE_DECL,
        /** 'int <font color="red">a</font>;' */
        NAME__LOCAL_VAR_DECL,
        /** '<font color="red">native</font>' */
        NATIVE,
        /** '<font color="red">new</font>' */
        NEW,
        /** 'a <font color="red">!=</font> b' */
        NOT_EQUAL,
        /** '<font color="red">null</font>' */
        NULL,
        /** 'a <font color="red">|</font> b' */
        OR,
        /** 'a <font color="red">|=</font> b' */
        OR_ASSIGN,
        /** '<font color="red">package</font> ...;' */
        PACKAGE,
        /** 'a <font color="red">+</font> b' */
        PLUS__ADDITIVE,
        /** '<font color="red">+</font>(a + b)' */
        PLUS__UNARY,
        /** 'a <font color="red">+=</font> b' */
        PLUS_ASSIGN,
        /** 'x<font color="red">--</font>' */
        POST_DECR,
        /** 'x<font color="red">++</font>' */
        POST_INCR,
        /** '<font color="red">--</font>x' */
        PRE_DECR,
        /** '<font color="red">++</font>x' */
        PRE_INCR,
        /** '<font color="red">private</font>' */
        PRIVATE,
        /** '<font color="red">protected</font>' */
        PROTECTED,
        /** '<font color="red">public</font>' */
        PUBLIC,
        /** 'a <font color="red">?</font> b : c' */
        QUESTION__TERNARY,
        /** 'List&lt;<font color="red">?</font> extends InputStream>' */
        QUESTION__WILDCARD_TYPE,
        /** 'public &lt;T extends Number<font color="red">></font> void meth(T parm) {' */
        R_ANGLE__METH_DECL_TYPE_PARAMS,
        /** 'MyClass.&lt;Double<font color="red">></font>meth(x)' */
        R_ANGLE__METH_INVOCATION_TYPE_ARGS,
        /** 'MyClass&lt;String<font color="red">></font>' */
        R_ANGLE__TYPE_ARGS,
        /** 'class MyClass&lt;T extends Number<font color="red">></font> {' */
        R_ANGLE__TYPE_PARAMS,
        /** 'Object[<font color="red">]</font>' */
        R_BRACK__ARRAY_DECL,
        /** 'a[3<font color="red">]</font>' */
        R_BRACK__INDEX,
        /** '@SuppressWarnings({ "foo", "bar" <font color="red">}</font>)' */
        R_CURLY__ANNO_ARRAY_INIT,
        /** 'new Object() { ... <font color="red">}</font>' */
        R_CURLY__ANON_CLASS,
        /** 'int[] ia = { 1, 2 <font color="red">};</font>'<br/>'b = new int[] { 1, 2 <font color="red">}</font>;' */
        R_CURLY__ARRAY_INIT,
        /** '{ int i = 0; i++; <font color="red">}</font>' */
        R_CURLY__BLOCK,
        /** 'try { ... } catch (...) { ... <font color="red">}</font>' */
        R_CURLY__CATCH,
        /** 'do { ... <font color="red">}</font> while (...);' */
        R_CURLY__DO,
        /** 'else { ... <font color="red">}</font>' */
        R_CURLY__ELSE,
        /** '@SuppressWarnings({<font color="red">}</font>)' */
        R_CURLY__EMPTY_ANNO_ARRAY_INIT,
        /** 'new Object() {<font color="red">}</font>' */
        R_CURLY__EMPTY_ANON_CLASS,
        /** 'int[] ia = {<font color="red">}</font>;'<br/>'b = new int[] {<font color="red">}</font>;' */
        R_CURLY__EMPTY_ARRAY_INIT,
        /** 'try { ... } catch (...) {<font color="red">}</font>' */
        R_CURLY__EMPTY_CATCH,
        /** 'public MyClass(...) {<font color="red">}</font>'<br/>'public method(...) {<font color="red">}</font>' */
        R_CURLY__EMPTY_METH_DECL,
        /**
         * 'class MyClass {<font color="red">}</font>'<br/>
         * 'interface MyInterface {<font color="red">}</font>'<br/>
         * '@interface MyAnnotation {<font color="red">}</font>'<br/>
         * 'enum MyEnum {<font color="red">}</font>'
         */
        R_CURLY__EMPTY_TYPE_DECL,
        /** 'enum MyEnum { FOO { ... <font color="red">}</font> }' */
        R_CURLY__ENUM_CONST_DECL,
        /** 'finally { ... <font color="red">}</font>' */
        R_CURLY__FINALLY,
        /** 'for (...) { ... <font color="red">}</font>' */
        R_CURLY__FOR,
        /** 'if (...) { ... <font color="red">}</font>' */
        R_CURLY__IF,
        /** 'class MyClass { { ... <font color="red">}</font> }' */
        R_CURLY__INSTANCE_INIT,
        /** 'LABEL: { ... <font color="red">}</font>' */
        R_CURLY__LABELED_STAT,
        /**
         * 'public MyClass(...) { ... <font color="red">}</font>'<br/>
         * 'public method(...) { ... <font color="red">}</font>'
         */
        R_CURLY__METH_DECL,
        /** 'class MyClass { static { ... <font color="red">}</font> }' */
        R_CURLY__STATIC_INIT,
        /** 'switch (a) { ... <font color="red">}</font>' */
        R_CURLY__SWITCH,
        /** 'synchronized (a) { ... <font color="red">}</font>' */
        R_CURLY__SYNCHRONIZED,
        /** 'try { ... <font color="red">} catch {</font>' */
        R_CURLY__TRY,
        /**
         * 'class MyClass { ... <font color="red">}</font>'<br/>
         * 'interface MyInter { ... <font color="red">}</font>'<br/>
         * '@interface MyAnno { ... <font color="red">}</font>'<br/>
         * 'enum MyEnum { ... <font color="red">}</font>'
         */
        R_CURLY__TYPE_DECL,
        /** 'while (...) { ... <font color="red">}</font>' */
        R_CURLY__WHILE,
        /** '@SuppressWarnings("foo"<font color="red">)</font>' */
        R_PAREN__ANNO,
        /** 'interface @MyAnno { String engineer(<font color="red">)</font>; }' */
        R_PAREN__ANNO_ELEM_DECL,
        /** '(int<font color="red">)</font> a' */
        R_PAREN__CAST,
        /** 'try { ... } catch (Exception e<font color="red">)</font> {' */
        R_PAREN__CATCH,
        /** 'do { ... } while (...<font color="red">)</font>;' */
        R_PAREN__DO_WHILE,
        /** 'for (int i = 0; i &lt; 10; i++<font color="red">)</font> {' */
        R_PAREN__FOR,
        /** 'for (int i = 0; i &lt; 10;<font color="red">)</font> {' */
        R_PAREN__FOR_NO_UPDATE,
        /** 'if (...<font color="red">)</font> {' */
        R_PAREN__IF,
        /** 'a(x, y<font color="red">)</font>' */
        R_PAREN__METH_INVOCATION,
        /** 'void meth(int a, int b<font color="red">)</font> {' */
        R_PAREN__PARAMS,
        /** '(a + b<font color="red">)</font> * c' */
        R_PAREN__PARENTHESIZED,
        /** '<font color="red">return</font> x;' */
        RETURN__EXPR,
        /** '<font color="red">return</font>;' */
        RETURN__NO_EXPR,
        /** 'a <font color="red">>></font> 3' */
        RIGHT_SHIFT,
        /** 'a <font color="red">>>=</font> 2' */
        RIGHT_SHIFT_ASSIGN,
        /** 'abstract meth(int x)<font color="red">;</font>' */
        SEMI__ABSTRACT_METH_DECL,
        /** 'interface @MyAnno { String engineer()<font color="red">;</font> }' */
        SEMI__ANNO_ELEM_DECL,
        /** '<font color="red">;</font>' */
        SEMI__EMPTY_STAT,
        /** 'enum MyEnum { A, B, C<font color="red">;</font> public String toString() { ... } }' */
        SEMI__ENUM_DECL,
        /** 'public int i<font color="red">;</font>' */
        SEMI__FIELD_DECL,
        /** 'for (...; i < 3<font color="red">;</font>) {' */
        SEMI__FOR_CONDITION_NO_UPDATE,
        /** 'for (...; i < 3<font color="red">;</font> i++) {' */
        SEMI__FOR_CONDITION_UPDATE,
        /** 'for (int i = 0<font color="red">;</font> i < 3;...' */
        SEMI__FOR_INIT_CONDITION,
        /** 'for (int i = 0<font color="red">;</font>;...' */
        SEMI__FOR_INIT_NO_CONDITION,
        /** 'for (...;<font color="red">;</font>) {' */
        SEMI__FOR_NO_CONDITION_NO_UPDATE,
        /** 'for (...;<font color="red">;</font> i++) {' */
        SEMI__FOR_NO_CONDITION_UPDATE,
        /** 'for (<font color="red">;</font> ...; ...) {' */
        SEMI__FOR_NO_INIT_CONDITION,
        /** 'for (<font color="red">;</font>;...) {' */
        SEMI__FOR_NO_INIT_NO_CONDITION,
        /** 'import pkg.*<font color="red">;</font>'<br/>'import pkg.MyClass<font color="red">;</font>' */
        SEMI__IMPORT,
        /** 'package pkg.pkg<font color="red">;</font>' */
        SEMI__PACKAGE_DECL,
        /** 'a = 7<font color="red">;</font>' */
        SEMI__STATEMENT,
        /** 'import static MyClass.*<font color="red">;</font>' */
        SEMI__STATIC_IMPORT,
        /** 'class MyClass { <font color="red">;</font> }' */
        SEMI__TYPE_DECL,
        /** '<font color="red">short</font>' */
        SHORT,
        /** 'import pkg.pkg.<font color="red">*</font>;' */
        STAR__TYPE_IMPORT_ON_DEMAND,
        /** 'import <font color="red">static</font> java.util.Map.Entry;' */
        STATIC__STATIC_IMPORT,
        /**
         * '<font color="red">static</font> int x;'<br/>
         * '<font color="red">static</font> class MyClass {'<br/>
         * '<font color="red">static</font> void meth() {'
         */
        STATIC__MOD,
        /** 'class MyClass { <font color="red">static</font> { ... } }' */
        STATIC__STATIC_INIT,
        /** '<font color="red">"hello"</font>' */
        STRING_LITERAL,
        /** '<font color="red">super</font>(x, y);' */
        SUPER__CTOR_CALL,
        /** '<font color="red">super</font>.meth();' */
        SUPER__EXPR,
        /** 'List&lt;T <font color="red">super</font> MyClass>' */
        SUPER__TYPE_BOUND,
        /** '<font color="red">switch</font> (a) {' */
        SWITCH,
        /** '<font color="red">synchronized</font> Object o;' */
        SYNCHRONIZED__MOD,
        /** '<font color="red">synchronized</font> (x) { ... }' */
        SYNCHRONIZED__SYNCHRONIZED,
        /** '<font color="red">this</font>(a, b);' */
        THIS__CTOR_CALL,
        /** '<font color="red">this</font>.meth()'<br/>'<font color="red">this</font>.field' */
        THIS__EXPR,
        /** '<font color="red">throw</font> new IOException();' */
        THROW,
        /** '<font color="red">throws</font>' */
        THROWS,
        /** '<font color="red">transient</font>' */
        TRANSIENT,
        /** '<font color="red">true</font>' */
        TRUE,
        /** '<font color="red">try</font> { ... } catch (...) { ... }' */
        TRY,
        /** 'a <font color="red">>>></font> 3' */
        UNSIGNED_RIGHT_SHIFT,
        /** 'a <font color="red">>>>=</font> 2' */
        UNSIGNED_RIGHT_SHIFT_ASSIGN,
        /** '<font color="red">void</font> meth() {'<br/>'<font color="red">void</font>.class' */
        VOID,
        /** '<font color="red">volatile</font>' */
        VOLATILE,
        /** 'do { ... } <font color="red">while</font> (a > 0);' */
        WHILE__DO,
        /** '<font color="red">while</font> (a > 0) { ... }' */
        WHILE__WHILE,
        /** 'a <font color="red">^</font> b' */
        XOR,
        /** 'a <font color="red">^=</font> b' */
        XOR_ASSIGN,

        // CHECKSTYLE __:OFF
    }

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
        SUPER__CTOR_CALL,
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

        // Tokens that appear in only one context, and thus map one-to-one to a Java element.
        case TokenTypes.ABSTRACT:           return ABSTRACT;
        case TokenTypes.ARRAY_DECLARATOR:   return L_BRACK__ARRAY_DECL;
        case TokenTypes.BAND:               return AND__EXPR;
        case TokenTypes.BAND_ASSIGN:        return AND_ASSIGN;
        case TokenTypes.BNOT:               return BITWISE_COMPLEMENT;
        case TokenTypes.BOR:                return OR;
        case TokenTypes.BOR_ASSIGN:         return OR_ASSIGN;
        case TokenTypes.BSR:                return UNSIGNED_RIGHT_SHIFT;
        case TokenTypes.BSR_ASSIGN:         return UNSIGNED_RIGHT_SHIFT_ASSIGN;
        case TokenTypes.BXOR:               return XOR;
        case TokenTypes.BXOR_ASSIGN:        return XOR_ASSIGN;
        case TokenTypes.CHAR_LITERAL:       return CHAR_LITERAL;
        case TokenTypes.COMMA:              return COMMA;
        case TokenTypes.CTOR_CALL:          return THIS__CTOR_CALL;
        case TokenTypes.DEC:                return PRE_DECR;
        case TokenTypes.DIV:                return DIVIDE;
        case TokenTypes.DIV_ASSIGN:         return DIVIDE_ASSIGN;
        case TokenTypes.DO_WHILE:           return WHILE__DO;
        case TokenTypes.ELLIPSIS:           return ELLIPSIS;
        case TokenTypes.EMPTY_STAT:         return SEMI__EMPTY_STAT;
        case TokenTypes.ENUM:               return ENUM;
        case TokenTypes.EQUAL:              return EQUAL;
        case TokenTypes.EXTENDS_CLAUSE:     return EXTENDS__TYPE;
        case TokenTypes.FINAL:              return FINAL;
        case TokenTypes.GE:                 return GREATER_EQUAL;
        case TokenTypes.GT:                 return GREATER;
        case TokenTypes.IMPLEMENTS_CLAUSE:  return IMPLEMENTS;
        case TokenTypes.IMPORT:             return IMPORT;
        case TokenTypes.INC:                return PRE_INCR;
        case TokenTypes.INDEX_OP:           return L_BRACK__INDEX;
        case TokenTypes.LABELED_STAT:       return COLON__LABELED_STAT;
        case TokenTypes.LAND:               return CONDITIONAL_AND;
        case TokenTypes.LE:                 return LESS_EQUAL;
        case TokenTypes.LITERAL_ASSERT:     return ASSERT;
        case TokenTypes.LITERAL_BOOLEAN:    return BOOLEAN;
        case TokenTypes.LITERAL_BREAK:      return BREAK;
        case TokenTypes.LITERAL_BYTE:       return BYTE;
        case TokenTypes.LITERAL_CASE:       return CASE;
        case TokenTypes.LITERAL_CATCH:      return CATCH;
        case TokenTypes.LITERAL_CONTINUE:   return CONTINUE;
        case TokenTypes.LITERAL_CHAR:       return CHAR;
        case TokenTypes.LITERAL_DO:         return DO;
        case TokenTypes.LITERAL_DOUBLE:     return DOUBLE;
        case TokenTypes.LITERAL_ELSE:       return ELSE;
        case TokenTypes.LITERAL_FALSE:      return FALSE;
        case TokenTypes.LITERAL_FINALLY:    return FINALLY;
        case TokenTypes.LITERAL_FLOAT:      return FLOAT;
        case TokenTypes.LITERAL_FOR:        return FOR;
        case TokenTypes.LITERAL_IF:         return IF;
        case TokenTypes.LITERAL_INSTANCEOF: return INSTANCEOF;
        case TokenTypes.LITERAL_INT:        return INT;
        case TokenTypes.LITERAL_INTERFACE:  return INTERFACE;
        case TokenTypes.LITERAL_LONG:       return LONG;
        case TokenTypes.LITERAL_NATIVE:     return NATIVE;
        case TokenTypes.LITERAL_NEW:        return NEW;
        case TokenTypes.LITERAL_NULL:       return NULL;
        case TokenTypes.LITERAL_PRIVATE:    return PRIVATE;
        case TokenTypes.LITERAL_PROTECTED:  return PROTECTED;
        case TokenTypes.LITERAL_PUBLIC:     return PUBLIC;
        case TokenTypes.LITERAL_SHORT:      return SHORT;
        case TokenTypes.LITERAL_SUPER:      return SUPER__EXPR;
        case TokenTypes.LITERAL_SWITCH:     return SWITCH;
        case TokenTypes.LITERAL_THIS:       return THIS__EXPR;
        case TokenTypes.LITERAL_THROW:      return THROW;
        case TokenTypes.LITERAL_THROWS:     return THROWS;
        case TokenTypes.LITERAL_TRANSIENT:  return TRANSIENT;
        case TokenTypes.LITERAL_TRUE:       return TRUE;
        case TokenTypes.LITERAL_TRY:        return TRY;
        case TokenTypes.LITERAL_VOID:       return VOID;
        case TokenTypes.LITERAL_VOLATILE:   return VOLATILE;
        case TokenTypes.LITERAL_WHILE:      return WHILE__WHILE;
        case TokenTypes.LNOT:               return LOGICAL_COMPLEMENT;
        case TokenTypes.LOR:                return CONDITIONAL_OR;
        case TokenTypes.LT:                 return LESS;
        case TokenTypes.METHOD_CALL:        return L_PAREN__METH_INVOCATION;
        case TokenTypes.MINUS:              return MINUS__ADDITIVE;
        case TokenTypes.MINUS_ASSIGN:       return MINUS_ASSIGN;
        case TokenTypes.MOD:                return MODULO;
        case TokenTypes.MOD_ASSIGN:         return MODULO_ASSIGN;
        case TokenTypes.NOT_EQUAL:          return NOT_EQUAL;
        case TokenTypes.NUM_DOUBLE:         return DOUBLE_LITERAL;
        case TokenTypes.NUM_FLOAT:          return FLOAT_LITERAL;
        case TokenTypes.NUM_INT:            return INT_LITERAL;
        case TokenTypes.NUM_LONG:           return LONG_LITERAL;
        case TokenTypes.PACKAGE_DEF:        return PACKAGE;
        case TokenTypes.PLUS:               return PLUS__ADDITIVE;
        case TokenTypes.PLUS_ASSIGN:        return PLUS_ASSIGN;
        case TokenTypes.POST_DEC:           return POST_DECR;
        case TokenTypes.POST_INC:           return POST_INCR;
        case TokenTypes.QUESTION:           return QUESTION__TERNARY;
        case TokenTypes.SL:                 return LEFT_SHIFT;
        case TokenTypes.SL_ASSIGN:          return LEFT_SHIFT_ASSIGN;
        case TokenTypes.SR:                 return RIGHT_SHIFT;
        case TokenTypes.SR_ASSIGN:          return RIGHT_SHIFT_ASSIGN;
        case TokenTypes.STAR_ASSIGN:        return MULTIPLY_ASSIGN;
        case TokenTypes.STATIC_IMPORT:      return IMPORT__STATIC_IMPORT;
        case TokenTypes.STRING_LITERAL:     return STRING_LITERAL;
        case TokenTypes.SUPER_CTOR_CALL:    return SUPER__CTOR_CALL;
        case TokenTypes.TYPE_EXTENSION_AND: return AND__TYPE_BOUND;
        case TokenTypes.TYPE_LOWER_BOUNDS:  return SUPER__TYPE_BOUND;
        case TokenTypes.TYPE_UPPER_BOUNDS:  return EXTENDS__TYPE_BOUND;
        case TokenTypes.TYPECAST:           return L_PAREN__CAST;
        case TokenTypes.UNARY_PLUS:         return PLUS__UNARY;
        case TokenTypes.UNARY_MINUS:        return MINUS__UNARY;
        case TokenTypes.WILDCARD_TYPE:      return QUESTION__WILDCARD_TYPE;

        case TokenTypes.ARRAY_INIT:
            return firstChildType == TokenTypes.RCURLY ? L_CURLY__EMPTY_ARRAY_INIT : L_CURLY__ARRAY_INIT;
    
        case TokenTypes.ASSIGN:
            return parentType == TokenTypes.VARIABLE_DEF ? ASSIGN__VAR_DECL : ASSIGN__ASSIGNMENT;
    
        case TokenTypes.AT:
            switch (parentType) {

            case TokenTypes.ANNOTATION:     return AT__ANNO;
            case TokenTypes.ANNOTATION_DEF: return AT__ANNO_DECL;
            }
            break;
    
        case TokenTypes.COLON:
            switch (parentType) {

            case TokenTypes.LITERAL_DEFAULT: return COLON__DEFAULT;
            case TokenTypes.LITERAL_CASE:    return COLON__CASE;
            case TokenTypes.FOR_EACH_CLAUSE: return COLON__ENHANCED_FOR;
            }
            return COLON__TERNARY;
    
        case TokenTypes.STAR: return parentType == TokenTypes.DOT ? STAR__TYPE_IMPORT_ON_DEMAND : MULTIPLY;
    
        case TokenTypes.DOT:
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.PACKAGE_DEF) return DOT__PACKAGE_DECL;
            if (getAncestorWithTypeNot(ast, TokenTypes.DOT) == TokenTypes.IMPORT)      return DOT__IMPORT;
            if (getAncestorWithTypeNot(ast, TokenTypes.ARRAY_DECLARATOR, TokenTypes.DOT) == TokenTypes.TYPE) {
                return DOT__QUALIFIED_TYPE;
            }
            return DOT__SELECTOR;
    
        case TokenTypes.GENERIC_END:
            switch (parentType) {

            case TokenTypes.TYPE_PARAMETERS:
                return grandparentType == TokenTypes.METHOD_DEF ? R_ANGLE__METH_DECL_TYPE_PARAMS : R_ANGLE__TYPE_ARGS;

            case TokenTypes.TYPE_ARGUMENTS:
                return (
                    grandparentType == TokenTypes.TYPE
                    || grandparentType == TokenTypes.LITERAL_NEW
                    || grandparentType == TokenTypes.EXTENDS_CLAUSE
                    || grandparentType == TokenTypes.IMPLEMENTS_CLAUSE
                ) ? R_ANGLE__TYPE_ARGS : R_ANGLE__METH_INVOCATION_TYPE_ARGS;
            }
            break;
            
        case TokenTypes.GENERIC_START:
            switch (parentType) {

            case TokenTypes.TYPE_PARAMETERS:
                return (
                    grandparentType == TokenTypes.METHOD_DEF
                ) ? L_ANGLE__METH_DECL_TYPE_PARAMS : L_ANGLE__TYPE_ARGS;

            case TokenTypes.TYPE_ARGUMENTS:
                return (
                    grandparentType == TokenTypes.TYPE
                    || grandparentType == TokenTypes.LITERAL_NEW
                    || grandparentType == TokenTypes.EXTENDS_CLAUSE
                    || grandparentType == TokenTypes.IMPLEMENTS_CLAUSE
                ) ? L_ANGLE__TYPE_ARGS : L_ANGLE__METH_INVOCATION_TYPE_ARGS;
            }
            break;
    
        case TokenTypes.IDENT:
            switch (parentType) {
            
            case TokenTypes.ANNOTATION:                   return NAME__ANNO;
            case TokenTypes.ANNOTATION_FIELD_DEF:         return NAME__ANNO_ELEM_DECL;
            case TokenTypes.VARIABLE_DEF:                 return NAME__LOCAL_VAR_DECL;
            case TokenTypes.CTOR_DEF:                     return NAME__CTOR_DECL;
            case TokenTypes.METHOD_DEF:                   return NAME__METH_DECL;
            case TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR: return NAME__ANNO_MEMBER;
            case TokenTypes.PARAMETER_DEF:                return NAME__PARAM;

            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ANNOTATION_DEF:
            case TokenTypes.ENUM_DEF:
                return NAME__TYPE_DECL;
            }
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
            return NAME__AMBIGUOUS;
    
        case TokenTypes.LCURLY:
            switch (parentType) {
            
            case TokenTypes.LITERAL_SWITCH: return L_CURLY__SWITCH;

            case TokenTypes.OBJBLOCK:
                switch (grandparentType) {
                
                case TokenTypes.ENUM_CONSTANT_DEF: return L_CURLY__ENUM_CONST;

                case TokenTypes.CLASS_DEF:
                case TokenTypes.INTERFACE_DEF:
                case TokenTypes.ANNOTATION_DEF:
                case TokenTypes.ENUM_DEF:
                    return nextSiblingType == TokenTypes.RCURLY ? L_CURLY__EMPTY_TYPE_DECL : L_CURLY__TYPE_DECL;

                case TokenTypes.LITERAL_NEW:
                    return nextSiblingType == TokenTypes.RCURLY ? L_CURLY__EMPTY_ANON_CLASS : L_CURLY__ANON_CLASS;
                }
                break;

            case TokenTypes.ARRAY_INIT:
                return nextSiblingType == TokenTypes.RCURLY ? L_CURLY__EMPTY_ARRAY_INIT : L_CURLY__ARRAY_INIT;
            }
            break;
    
        case TokenTypes.ANNOTATION_ARRAY_INIT:
            return (
                firstChildType == TokenTypes.RCURLY
                ? L_CURLY__EMPTY_ANNO_ARRAY_INIT
                : L_CURLY__ANNO_ARRAY_INIT
            );

        case TokenTypes.LITERAL_RETURN:
            return firstChildType == TokenTypes.SEMI ? RETURN__NO_EXPR : RETURN__EXPR;

        case TokenTypes.LITERAL_CLASS: 
            return parentType == TokenTypes.CLASS_DEF ? CLASS__CLASS_DECL : CLASS__CLASS_LITERAL;

        case TokenTypes.LITERAL_DEFAULT:
            return (
                parentType == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR || parentType == TokenTypes.ANNOTATION_FIELD_DEF
            ) ? DEFAULT__ANNO_ELEM : DEFAULT__SWITCH;

        case TokenTypes.LITERAL_STATIC:
            return parentType == TokenTypes.STATIC_IMPORT ? STATIC__STATIC_IMPORT : STATIC__MOD;

        case TokenTypes.LITERAL_SYNCHRONIZED:
            return parentType == TokenTypes.SLIST ? SYNCHRONIZED__SYNCHRONIZED : SYNCHRONIZED__MOD;
        
        case TokenTypes.STATIC_INIT:
            ast.setText("static");
            return STATIC__STATIC_INIT;
    
        case TokenTypes.LPAREN:
            switch (parentType) {
            
            case TokenTypes.ANNOTATION:           return L_PAREN__ANNO;
            case TokenTypes.ANNOTATION_FIELD_DEF: return L_PAREN__ANNO_ELEM_DECL;
            case TokenTypes.LITERAL_DO:           return L_PAREN__DO_WHILE;
            case TokenTypes.LITERAL_IF:           return L_PAREN__IF;
            case TokenTypes.LITERAL_CATCH:        return L_PAREN__CATCH;

            case TokenTypes.SUPER_CTOR_CALL:
            case TokenTypes.LITERAL_NEW:
                return L_PAREN__METH_INVOCATION;

            case TokenTypes.LITERAL_FOR:
                return ast.getNextSibling().getFirstChild() == null ? L_PAREN__FOR_NO_INIT : L_PAREN__FOR;
            }

            if (nextSiblingType == TokenTypes.PARAMETERS) return L_PAREN__PARAMS;

            return L_PAREN__PARENTHESIZED;

        case TokenTypes.RBRACK:
            switch (parentType) {

            case TokenTypes.ARRAY_DECLARATOR: return R_BRACK__ARRAY_DECL;
            case TokenTypes.INDEX_OP:         return R_BRACK__INDEX;
            }
            break;
    
        case TokenTypes.RCURLY:
            switch (parentType) {

            case TokenTypes.LITERAL_SWITCH: return R_CURLY__SWITCH;

            case TokenTypes.ANNOTATION_ARRAY_INIT:
                return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_ANNO_ARRAY_INIT : R_CURLY__ANNO_ARRAY_INIT;

            case TokenTypes.ARRAY_INIT:
                return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_ARRAY_INIT : R_CURLY__ARRAY_INIT;

            case TokenTypes.OBJBLOCK:
                switch (grandparentType) {

                case TokenTypes.ENUM_CONSTANT_DEF: return R_CURLY__ENUM_CONST_DECL;

                case TokenTypes.CLASS_DEF:
                case TokenTypes.INTERFACE_DEF:
                case TokenTypes.ANNOTATION_DEF:
                case TokenTypes.ENUM_DEF:
                    return previousSiblingType == TokenTypes.LCURLY ? R_CURLY__EMPTY_TYPE_DECL : R_CURLY__TYPE_DECL;

                case TokenTypes.LITERAL_NEW:
                    return previousSiblingType == TokenTypes.LCURLY ? R_CURLY__EMPTY_ANON_CLASS : R_CURLY__ANON_CLASS;
                }
                break;

            case TokenTypes.SLIST:
                switch (grandparentType) {
                
                case TokenTypes.INSTANCE_INIT:        return R_CURLY__INSTANCE_INIT;
                case TokenTypes.LABELED_STAT:         return R_CURLY__LABELED_STAT;
                case TokenTypes.LITERAL_DO:           return R_CURLY__DO;
                case TokenTypes.LITERAL_ELSE:         return R_CURLY__IF;
                case TokenTypes.LITERAL_FINALLY:      return R_CURLY__FINALLY;
                case TokenTypes.LITERAL_FOR:          return R_CURLY__FOR;
                case TokenTypes.LITERAL_IF:           return R_CURLY__IF;
                case TokenTypes.LITERAL_SYNCHRONIZED: return R_CURLY__SYNCHRONIZED;
                case TokenTypes.LITERAL_TRY:          return R_CURLY__TRY;
                case TokenTypes.LITERAL_WHILE:        return R_CURLY__WHILE;
                case TokenTypes.SLIST:                return R_CURLY__BLOCK;
                case TokenTypes.STATIC_INIT:          return R_CURLY__STATIC_INIT;

                case TokenTypes.CTOR_DEF:
                case TokenTypes.METHOD_DEF:
                    return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_METH_DECL : R_CURLY__METH_DECL;

                case TokenTypes.ARRAY_INIT:
                    return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_ARRAY_INIT : R_CURLY__ARRAY_INIT;

                case TokenTypes.LITERAL_CATCH:
                    return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_CATCH : R_CURLY__CATCH;
                }
            }
            break;
    
        case TokenTypes.RPAREN:
            switch (parentType) {

            case TokenTypes.ANNOTATION:           return R_PAREN__ANNO;
            case TokenTypes.ANNOTATION_FIELD_DEF: return R_PAREN__ANNO_ELEM_DECL;
            case TokenTypes.LITERAL_CATCH:        return R_PAREN__CATCH;
            case TokenTypes.LITERAL_DO:           return R_PAREN__DO_WHILE;
            case TokenTypes.LITERAL_IF:           return R_PAREN__IF;

            case TokenTypes.CTOR_DEF:
            case TokenTypes.METHOD_DEF:
                return R_PAREN__PARAMS;

            case TokenTypes.SUPER_CTOR_CALL:
            case TokenTypes.LITERAL_NEW:
            case TokenTypes.METHOD_CALL:
                return R_PAREN__METH_INVOCATION;

            case TokenTypes.LITERAL_FOR:
                return ast.getPreviousSibling().getFirstChild() == null ? R_PAREN__FOR_NO_UPDATE : R_PAREN__FOR;
            }

            if (previousSiblingType == TokenTypes.TYPE) return R_PAREN__CAST;

            return R_PAREN__PARENTHESIZED;
    
        case TokenTypes.SEMI:
            switch (parentType) {

            case TokenTypes.PACKAGE_DEF:          return SEMI__PACKAGE_DECL;
            case TokenTypes.IMPORT:               return SEMI__IMPORT;
            case TokenTypes.STATIC_IMPORT:        return SEMI__STATIC_IMPORT;
            case TokenTypes.METHOD_DEF:           return SEMI__ABSTRACT_METH_DECL;
            case TokenTypes.ANNOTATION_FIELD_DEF: return SEMI__ANNO_ELEM_DECL;

            case TokenTypes.OBJBLOCK:
                return previousSiblingType == TokenTypes.ENUM_CONSTANT_DEF ? SEMI__ENUM_DECL :  SEMI__TYPE_DECL;

            case TokenTypes.SLIST:
            case TokenTypes.SUPER_CTOR_CALL:
            case TokenTypes.CTOR_CALL:
            case TokenTypes.LITERAL_DO:
            case TokenTypes.LITERAL_RETURN:
            case TokenTypes.LITERAL_BREAK:
            case TokenTypes.LITERAL_CONTINUE:
            case TokenTypes.LITERAL_IF:
            case TokenTypes.LITERAL_WHILE:
            case TokenTypes.LITERAL_ASSERT:
            case TokenTypes.LITERAL_THROW:
                return SEMI__STATEMENT;

            case TokenTypes.LITERAL_FOR:
                if (nextSiblingType == -1) return SEMI__STATEMENT;
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
                break;

            case TokenTypes.VARIABLE_DEF:
                if (grandparentType == TokenTypes.OBJBLOCK) return SEMI__FIELD_DECL;
                break;
            }
            break;
    
        case TokenTypes.SLIST:
            switch (parentType) {

            case TokenTypes.STATIC_INIT:          return L_CURLY__STATIC_INIT;
            case TokenTypes.INSTANCE_INIT:        return L_CURLY__INSTANCE_INIT;
            case TokenTypes.LITERAL_IF:           return L_CURLY__IF;
            case TokenTypes.LITERAL_ELSE:         return R_CURLY__ELSE;
            case TokenTypes.LITERAL_DO:           return L_CURLY__DO;
            case TokenTypes.LITERAL_WHILE:        return L_CURLY__WHILE;
            case TokenTypes.LITERAL_FOR:          return L_CURLY__FOR;
            case TokenTypes.LITERAL_TRY:          return L_CURLY__TRY;
            case TokenTypes.LITERAL_FINALLY:      return L_CURLY__FINALLY;
            case TokenTypes.LITERAL_SYNCHRONIZED: return L_CURLY__SYNCHRONIZED;
            case TokenTypes.LABELED_STAT:         return L_CURLY__LABELED_STAT;
            case TokenTypes.SLIST:                return L_CURLY__BLOCK;

            case TokenTypes.LITERAL_CATCH: 
                return firstChildType == TokenTypes.RCURLY ? L_CURLY__EMPTY_CATCH : L_CURLY__CATCH;

            case TokenTypes.CTOR_DEF:
            case TokenTypes.METHOD_DEF:
                return (
                    firstChildType == TokenTypes.RCURLY
                    ? JavaElement.L_CURLY__EMPTY_METH_DECL
                    : JavaElement.L_CURLY__METH_DECL
                );
            }
            return null; // Not a 'physical' token.
            
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
        case TokenTypes.TYPE_PARAMETER:
        case TokenTypes.TYPE_PARAMETERS:
        case TokenTypes.VARIABLE_DEF:
            // These are the 'virtual' tokens, i.e. token which are not uniquely related to a physical token.
            return null;
    
        case TokenTypes.EOF:
            assert false : "Unexpected token '" + ast + "' (" + TokenTypes.getTokenName(ast.getType()) + ')';
            return null;
        }

        assert false : (
            "'"
            + ast
            + "' (type '"
            + TokenTypes.getTokenName(ast.getType())
            + "') has unexpected parent type '"
            + TokenTypes.getTokenName(parentType)
            + "' and/or grandparent type '"
            + TokenTypes.getTokenName(grandparentType)
            + "'"
        );

        return null;

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

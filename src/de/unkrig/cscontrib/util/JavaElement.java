
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

package de.unkrig.cscontrib.util;

/**
 * The elements of the Java programming languages that are represented by exactly one token.
 *
 * @see AstUtil#toJavaElement(com.puppycrawl.tools.checkstyle.api.DetailAST)
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
    /** '<font color="red">super</font>.meth();'<br>'outer.<font color="red">super</font>.meth();' */
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


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

import static de.unkrig.cscontrib.LocalTokenType.*;
import static de.unkrig.cscontrib.checks.WrapAndIndent.Control.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.sf.eclipsecs.core.config.meta.IOptionProvider;

import org.apache.commons.beanutils.ConversionException;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.Utils;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent1;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent2;
import de.unkrig.cscontrib.ui.quickfixes.WrapAndIndent3;
import de.unkrig.cscontrib.util.AstUtil;
import de.unkrig.csdoclet.IntegerRuleProperty;
import de.unkrig.csdoclet.Message;
import de.unkrig.csdoclet.MultiCheckRuleProperty;
import de.unkrig.csdoclet.Rule;

/**
 * Verifies that statements are uniformly wrapped and indented.
 * <p style="color: red">
 *   <b>This check supersedes the following checks:</b>
 * </p>
 * <ul style="color: red">
 *   <li>Left curly brace placement</li>
 *   <li>Right curly brace placement</li>
 *   <li>Operator wrap</li>
 * </ul>
 */
@Rule(
    group      = "%Whitespace.group",
    groupName  = "Whitespace",
    name       = "de.unkrig: Wrap and indent",
    parent     = "TreeWalker",
    quickfixes = { WrapAndIndent1.class, WrapAndIndent2.class, WrapAndIndent3.class }
)
@NotNullByDefault(false) public
class WrapAndIndent extends Check {

    /**
     * Message key as it appears in 'src/de/unkrig/cscontrib/checks/checkstyle-metadata.xml'.
     * <dl>
     *   <dt><code>{0}</code>
     *   <dd>Text of token <i>before</i> the (missing) line break
     *   <dt><code>{1}</code>
     *   <dd>Text of token <i>after</i> the (missing) line break
     * </dl>
     */
    @Message("Must wrap line before ''{1}''")
    public static final String
    MESSAGE_KEY_MUST_WRAP = "WrapAndIndent.mustWrap";

    /**
     * Message key as it appears in 'src/de/unkrig/cscontrib/checks/checkstyle-metadata.xml'.
     * <dl>
     *   <dt><code>{0}</code>
     *   <dd>Text of token <i>before</i> the (unwanted) line break
     *   <dt><code>{1}</code>
     *   <dd>Text of token <i>after</i> the (unwanted) line break
     * </dl>
     */
    @Message("''{0}'' must appear on same line as ''{1}''")
    public static final String
    MESSAGE_KEY_MUST_JOIN = "WrapAndIndent.mustJoin";

    /**
     * Message key as it appears in 'src/de/unkrig/cscontrib/checks/checkstyle-metadata.xml'.
     * <dl>
     *   <dt><code>{0}</code>
     *   <dd>Text the vertically misaligned token
     *   <dt><code>{1}</code>
     *   <dd>Current (wrong) column number of the token
     *   <dt><code>{2}</code>
     *   <dd>Correct column number of the token
     * </dl>
     */
    @Message("''{0}'' must appear in column {1}, not {2}")
    public static final String
    MESSAGE_KEY_WRONG_COLUMN = "WrapAndIndent.wrongColumn";

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
        INDENT,

        /**
         * Same as {@link #INDENT}, but if the next token has no children, then the previous and the next token
         * must appear in the same line.
         */
        INDENT_IF_CHILDREN,

        /**
         * If the tokens of the matching {@link #INDENT} or {@link #INDENT_IF_CHILDREN} were actually indented,
         * the the previous and the next token must be 'unindented', i.e. the next token must appear in a different
         * line, and its first character must appear N positions left from the first non-space character of the
         * preceding line.
         */
        UNINDENT,

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
        WRAP,

        /**
         * Indicates that the next of {@code args} is a {@link LocalTokenType}, and that is consumed iff it equals
         * the next token.
         */
        OPTIONAL,

        /**
         * Indicates that at least one more token must exist.
         */
        ANY_TOKEN,

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

    // BEGIN CONFIGURATION

    /**
     * How many spaces to use for each new indentation level.
     *
     * @cs-intertitle <h3>Indentation</h3>
     */
    @IntegerRuleProperty(defaultValue = WrapAndIndent.DEFAULT_BASIC_OFFSET)
    public void
    setBasicOffset(int value) { this.basicOffset = value; }

    private int
    basicOffset = WrapAndIndent.DEFAULT_BASIC_OFFSET;

    private static final int
    DEFAULT_BASIC_OFFSET = 4;

    /**
     * Whether to allow certain elements completely in one line.
     * <p>Examples:</p>
     * <pre>
     * public class Pojo <font color="red">{ int fld; }</font>            // One-line class body.
     * public interface Interf <font color="red">{ void meth(); }</font>  // One-line interface body.
     * private enum Color <font color="red">{ BLACK, WHITE }</font>       // One-line enum body.
     * public @interface MyAnno <font color="red">{ int value(); }</font> // One-line annotation body.
     *
     * class MyClass {
     *     protected MyClass() <font color="red">{ super(null); }</font>  // One-line constructor body.
     *     private void meth() <font color="red">{ bar(); }</font>        // One-line method body.
     *
     *     void bar() {
     *         switch (a) {
     *         <font color="red">case 1: case 2: a = 3; break;</font>     // One-line case group.
     *         }
     *     }
     *
     *     &#64;Anno1<font color="red">(a = 7, b = 5)</font>                  // One-line annotation.
     *     &#64;Anno2(
     *         a = <font color="red">{ 1, 2, 3 }</font>                   // One-line annotation array initializer.
     *     )
     *     int[] bar2 = <font color="red">{ 4, 5, 6 }</font>;             // One-line array initializer.
     * }
     * </pre>
     */
    @MultiCheckRuleProperty(
        valueOptions = {
            "class_def",
            "interface_def",
            "enum_def",
            "annotation_def",
            "ctor_def",
            "method_def",
            "case_group",
            "annotation",
            "annotation_array_init",
            "array_init",
        },
        defaultValue = WrapAndIndent.DEFAULT_ALLOW_ONE_LINE_ELEMENT
    )
    public void
    setAllowOneLineElement(String[] sa) {
        this.allowOneLineElement = WrapAndIndent.toEnumSet(sa, LocalTokenType.class);
    }

    private EnumSet<LocalTokenType>
    allowOneLineElement = WrapAndIndent.toEnumSet(
        WrapAndIndent.DEFAULT_ALLOW_ONE_LINE_ELEMENT.toUpperCase(),
        LocalTokenType.class
    );

    private static final String
    DEFAULT_ALLOW_ONE_LINE_ELEMENT = "class_def,interface_def,enum_def,annotation_def,ctor_def,method_def,case_group,annotation,annotation_array_init,array_init"; // SUPPRESS CHECKSTYLE LineLength

    /**
     * Whether to allow wrapping-and-indenting of subelements; where subelements may or may not appear on one line.
     * <p>Examples:</p>
     * <pre>
     * public class Pojo <font color="red">{</font>        // Wrapped-and-indented class body.
     *     <font color="red">int fld1; fld2;
     *     fld3;
     * }</font>
     * public interface Interf <font color="red">{</font>  // Wrapped-and-indented interface body.
     *     <font color="red">void meth1();
     *     void meth2(); void meth3();
     * }</font>
     * private enum Color <font color="red">{</font>       // Wrapped-and-indented enum body.
     *     <font color="red">BLACK,
     *     WHITE, GRAY
     * }</font>
     * public @interface MyAnno <font color="red">{</font> // Wrapped-and-indented annotation body.
     *     <font color="red">int length(); int width();
     *     int height();
     * }</font>
     *
     * class MyClass {
     *     protected MyClass() <font color="red">{</font>  // Wrapped-and-indented constructor body.
     *         <font color="red">super(null);
     *         foo(); bar();
     *     }</font>
     *     private void meth() <font color="red">{</font>  // Wrapped-and-indented method body.
     *         <font color="red">bar(); bar();
     *         bar();
     *     }</font>
     *
     *     void bar() {
     *         switch (a) {
     *         <font color="red">case 1:</font>            // Wrapped-and-indented case group.
     *         <font color="red">case 2: case 3: a = 3;
     *             break;</font>
     *         }
     *     }
     *
     *     &#64;Anno1<font color="red">(</font>                // Wrapped-and-indented annotation.
     *         <font color="red">a = 7, b = 5,
     *         c = 99
     *     )</font>
     *     &#64;Anno2(
     *         a = <font color="red">{</font>              // Wrapped-and-indented annotation array initializer.
     *             <font color="red">1, 2,
     *             3, 4, 5
     *         }</font>
     *     )
     *     int[] bar2 = <font color="red">{</font>         // Wrapped-and-indented array initializer.
     *         <font color="red">1, 2, 3,
     *         4, 5, 6
     *     }</font>;
     * }
     * </pre>
     */
    @MultiCheckRuleProperty(
        valueOptions = {
            "class_def",
            "interface_def",
            "enum_def",
            "annotation_def",
            "ctor_def",
            "method_def",
            "case_group",
            "annotation",
            "annotation_array_init",
            "array_init",
        },
        defaultValue = WrapAndIndent.DEFAULT_ALLOW_ELEMENT_WRAPPING
    )
    public void
    setAllowElementWrapping(String[] sa) {
        this.allowElementWrapping = WrapAndIndent.toEnumSet(sa, LocalTokenType.class);
    }

    private EnumSet<LocalTokenType>
    allowElementWrapping = WrapAndIndent.toEnumSet(
        WrapAndIndent.DEFAULT_ALLOW_ELEMENT_WRAPPING.toUpperCase(),
        LocalTokenType.class
    );

    private static final String
    DEFAULT_ALLOW_ELEMENT_WRAPPING = "class_def,interface_def,enum_def,annotation_def,ctor_def,method_def,case_group,annotation,annotation_array_init,array_init"; // SUPPRESS CHECKSTYLE LineLength

    /**
     * Whether to allow wrapping-and-indenting of subelements, where each subelement must strictly appear on a new line.
     * <p>Examples:</p>
     * <pre>
     * public class Pojo <font color="red">{</font>        // Strictly-wrapped-and-indented class body.
     *     <font color="red">int fld1;
     *     fld2;
     *     fld3;
     * }</font>
     * public interface Interf <font color="red">{</font>  // Strictly-wrapped-and-indented interface body.
     *     <font color="red">void meth1();
     *     void meth2();
     *     void meth3();
     * }</font>
     * private enum Color <font color="red">{</font>       // Strictly-wrapped-and-indented enum body.
     *     <font color="red">BLACK,
     *     WHITE,
     *     GRAY
     * }</font>
     * public @interface MyAnno <font color="red">{</font> // Strictly-wrapped-and-indented annotation body.
     *     <font color="red">int length();
     *     int width();
     *     int height();
     * }</font>
     *
     * class MyClass {
     *     protected MyClass() <font color="red">{</font>  // Strictly-wrapped-and-indented constructor body.
     *         <font color="red">super(null);
     *         foo();
     *         bar();
     *     }</font>
     *     private void meth() <font color="red">{</font>  // Strictly-wrapped-and-indented method body.
     *         <font color="red">bar();
     *         bar();
     *         bar();
     *     }</font>
     *
     *     void bar() {
     *         switch (a) {
     *         <font color="red">case 1:</font>            // Strictly-wrapped-and-indented case group.
     *         <font color="red">case 2:
     *         case 3:
     *             a = 3;
     *             break;</font>
     *         }
     *     }
     *
     *     &#64;Anno1<font color="red">(</font>                // Strictly-wrapped-and-indented annotation.
     *         <font color="red">a = 7,
     *         b = 5,
     *         c = 99
     *     )</font>
     *     &#64;Anno2(
     *         a = <font color="red">{</font>              // Strictly-wrapped-and-indented annotation array initializer.
     *             <font color="red">1,
     *             2,
     *             3,
     *             4,
     *             5
     *         }</font>
     *     )
     *     int[] bar2 = <font color="red">{</font>         // Strictly-wrapped-and-indented array initializer.
     *         <font color="red">1,
     *         2,
     *         3,
     *         4,
     *         5,
     *         6
     *     }</font>;
     * }
     * </pre>
     */
    @MultiCheckRuleProperty(
        valueOptions = {
            "class_def",
            "interface_def",
            "enum_def",
            "annotation_def",
            "ctor_def",
            "method_def",
            "case_group",
            "annotation",
            "annotation_array_init",
            "array_init",
        },
        defaultValue = WrapAndIndent.DEFAULT_ALLOW_STRICT_ELEMENT_WRAPPING
    )
    public void
    setAllowStrictElementWrapping(String[] sa) {
        this.allowStrictElementWrapping = WrapAndIndent.toEnumSet(sa, LocalTokenType.class);
    }

    private EnumSet<LocalTokenType>
    allowStrictElementWrapping = WrapAndIndent.toEnumSet(
        WrapAndIndent.DEFAULT_ALLOW_STRICT_ELEMENT_WRAPPING.toUpperCase(),
        LocalTokenType.class
    );

    private static final String
    DEFAULT_ALLOW_STRICT_ELEMENT_WRAPPING = "class_def,interface_def,enum_def,annotation_def,ctor_def,method_def,case_group,annotation,annotation_array_init,array_init"; // SUPPRESS CHECKSTYLE LineLength

    enum WrappingContext {
        PACKAGE_DECL_BEFORE_PACKAGE,
        CLASS_DECL_BEFORE_CLASS,
        INTERFACE_DECL_BEFORE_INTERFACE,
        ENUM_DECL_BEFORE_ENUM,
        ANNO_DECL_BEFORE_AT,
        FIELD_DECL_BEFORE_NAME,
        CTOR_DECL_BEFORE_NAME,
        METH_DECL_BEFORE_NAME,
        LOCAL_VAR_DECL_BEFORE_NAME,
        TYPE_DECL_BEFORE_LCURLY,
        CTOR_DECL_BEFORE_LCURLY,
        METHOD_DECL_BEFORE_LCURLY,
        ANON_CLASS_DECL_DECL_BEFORE_LCURLY,
        DO_BEFORE_LCURLY,
        TRY_BEFORE_CATCH,
        TRY_BEFORE_FINALLY,
        ARRAY_INIT_BEFORE_LCURLY,
        BEFORE_BINARY_OPERATOR,
        AFTER_BINARY_OPERATOR
    }

    /**
     * Whether to allow wrapping at certain places.
     *
     * @see #setRequireWrapping(String[])
     */
    @MultiCheckRuleProperty(
        optionProvider = WrappingContext.class,
        defaultValue = WrapAndIndent.DEFAULT_ALLOW_WRAPPING
    )
    public void
    setAllowWrapping(String[] sa) {
        this.allowWrapping = WrapAndIndent.toEnumSet(sa, LocalTokenType.class);
    }

    private EnumSet<LocalTokenType>
    allowWrapping = WrapAndIndent.toEnumSet(
        WrapAndIndent.DEFAULT_ALLOW_WRAPPING.toUpperCase(),
        LocalTokenType.class
    );

    private static final String
    DEFAULT_ALLOW_WRAPPING = "package_decl_before_package,class_decl_before_class,interface_decl_before_interface,enum_decl_before_enum,anno_decl_before_at,field_decl_before_name,ctor_decl_before_name,meth_decl_before_name,local_var_decl_before_name,binary_operator";

    /**
     * Whether to <i>require</i> wrapping at certain places.
     * <p>Examples:</p>
     * <pre>
     * &#64;NonNullByDefault
     * package com.acme.product; // package_decl_before_package
     *
     * public static final
     * class MyClass {           // class_decl_before_class
     * }
     *
     * public
     * interface MyInterf {      // interface_decl_before_interface
     * }
     *
     * protected
     * enum MyEnum {             // enum_decl_before_enum
     * }
     *
     * private
     * &#64;interface MyAnno {   // anno_decl_before_at
     *
     * private int
     * width = 7;                // field_decl_before_name
     *
     * protected
     * MyClass(int x) {          // ctor_decl_before_name
     *
     * private static
     * myMeth(int arg1) {        // meth_decl_before_name
     *
     * int
     * locvar = 7;               // local_var_decl_before_name
     *
     * public class MyClass
     * {                         // type_decl_before_lcurly
     *
     * protected MyClass(int x)
     * {                         // ctor_decl_before_lcurly
     *
     * private static myMeth(int arg1)
     * {                         // meth_decl_before_lcurly
     *
     * new Object()
     * {                         // anon_class_decl_before_lcurly
     *
     * do
     * {                         // do_before_lcurly
     *
     * try { ... }
     * catch { ... }             // try_before_catch
     *
     * try { ... }
     * finally { ... }           // try_before_finally
     *
     * int[] ia =
     * {                         // array_init_before_lcurly
     *
     * a
     * + b                       // before_binary_operator
     *
     * a +
     * b                         // after_binary_operator
     * </pre>
     */
    @MultiCheckRuleProperty(
        optionProvider = WrappingContext.class,
        defaultValue = WrapAndIndent.DEFAULT_REQUIRE_WRAPPING
    )
    public void
    setRequireWrapping(String[] sa) {
        this.requireWrapping = WrapAndIndent.toEnumSet(sa, LocalTokenType.class);
    }

    private EnumSet<LocalTokenType>
    requireWrapping = WrapAndIndent.toEnumSet(
        WrapAndIndent.DEFAULT_REQUIRE_WRAPPING.toUpperCase(),
        LocalTokenType.class
    );

    private static final String
    DEFAULT_REQUIRE_WRAPPING = "package_decl_before_package,class_decl_before_class,interface_decl_before_interface,enum_decl_before_enum,anno_decl_before_at,field_decl_before_name,ctor_decl_before_name,meth_decl_before_name,local_var_decl_before_name,binary_operator";

    // END CONFIGURATION

    private static <E extends Enum<E>> EnumSet<E>
    toEnumSet(String values, Class<E> enumClass) {
        return WrapAndIndent.toEnumSet(values, WrapAndIndent.COMMA_PATTERN, enumClass);
    }
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    private static <E extends Enum<E>> EnumSet<E>
    toEnumSet(String values, Pattern separatorPattern, Class<E> enumClass) {
        return WrapAndIndent.toEnumSet(separatorPattern.split(values), enumClass);
    }

    private static <E extends Enum<E>> EnumSet<E>
    toEnumSet(String[] values, Class<E> enumClass) {
        EnumSet<E> result = EnumSet.noneOf(enumClass);
        for (String value : values) result.add(WrapAndIndent.toEnum(value, enumClass));
        return result;
    }

    private static <E extends Enum<E>> E
    toEnum(String s, Class<E> enumType) {
        try {
            return Enum.valueOf(enumType, s.trim().toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new ConversionException("Unable to parse " + s, iae);
        }
    }

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
//            ABSTRACT,
            ANNOTATION,
            ANNOTATIONS,
            ANNOTATION_ARRAY_INIT,
            ANNOTATION_DEF,
            ANNOTATION_FIELD_DEF,
            ANNOTATION_MEMBER_VALUE_PAIR,
            ARRAY_DECLARATOR,
            ARRAY_INIT,
            ASSIGN,  // To check 'int[] ia = { 1, 2, 3 };'.
//            AT,
//            BAND,
//            BAND_ASSIGN,
//            BNOT,
//            BOR,
//            BOR_ASSIGN,
//            BSR,
//            BSR_ASSIGN,
//            BXOR,
//            BXOR_ASSIGN,
            CASE_GROUP,
//            CHAR_LITERAL,
            CLASS_DEF,
//            COLON,
//            COMMA,
            CTOR_CALL,
            CTOR_DEF,
//            DEC,
//            DIV,
//            DIV_ASSIGN,
            DOT,
//            DO_WHILE,
            ELIST,
//            ELLIPSIS,
//            EMPTY_STAT,
//            ENUM,
            ENUM_CONSTANT_DEF,
            ENUM_DEF,
//            EOF,
//            EQUAL,
            EXPR,
            EXTENDS_CLAUSE,
//            FINAL,
            FOR_CONDITION,
            FOR_EACH_CLAUSE,
            FOR_INIT,
            FOR_ITERATOR,
//            GE,
//            GENERIC_END,
//            GENERIC_START,
//            GT,
//            IDENT,
            IMPLEMENTS_CLAUSE,
            IMPORT,
//            INC,
//            INDEX_OP,
            INSTANCE_INIT,
            INTERFACE_DEF,
            LABELED_STAT,
//            LAND,
//            LCURLY,
//            LE,
            LITERAL_ASSERT,
//            LITERAL_BOOLEAN,
            LITERAL_BREAK,
//            LITERAL_BYTE,
            LITERAL_CASE,
            LITERAL_CATCH,
//            LITERAL_CHAR,
//            LITERAL_CLASS,
            LITERAL_CONTINUE,
//            LITERAL_DEFAULT,
            LITERAL_DO,
//            LITERAL_DOUBLE,
//            LITERAL_ELSE,
//            LITERAL_FALSE,
            LITERAL_FINALLY,
//            LITERAL_FLOAT,
            LITERAL_FOR,
            LITERAL_IF,
//            LITERAL_INSTANCEOF,
//            LITERAL_INT,
//            LITERAL_INTERFACE,
//            LITERAL_LONG,
//            LITERAL_NATIVE,
            LITERAL_NEW,
//            LITERAL_NULL,
//            LITERAL_PRIVATE,
//            LITERAL_PROTECTED,
//            LITERAL_PUBLIC,
            LITERAL_RETURN,
//            LITERAL_SHORT,
//            LITERAL_STATIC,
//            LITERAL_SUPER,
            LITERAL_SWITCH,
            LITERAL_SYNCHRONIZED,
//            LITERAL_THIS,
            LITERAL_THROW,
            LITERAL_THROWS,
//            LITERAL_TRANSIENT,
//            LITERAL_TRUE,
            LITERAL_TRY,
//            LITERAL_VOID,
//            LITERAL_VOLATILE,
            LITERAL_WHILE,
//            LNOT,
//            LOR,
//            LPAREN,
//            LT,
//            METHOD_CALL,
            METHOD_DEF,
//            MINUS,
//            MINUS_ASSIGN,
//            MOD,
            MODIFIERS,
//            MOD_ASSIGN,
//            NOT_EQUAL,
//            NUM_DOUBLE,
//            NUM_FLOAT,
//            NUM_INT,
//            NUM_LONG,
            OBJBLOCK,
            PACKAGE_DEF,
            PARAMETERS,
            PARAMETER_DEF,
//            PLUS,
//            PLUS_ASSIGN,
//            POST_DEC,
//            POST_INC,
//            QUESTION,
//            RBRACK,
//            RCURLY,
//            RPAREN,
//            SEMI,
//            SL,
            SLIST,
//            SL_ASSIGN,
//            SR,
//            SR_ASSIGN,
//            STAR,
//            STAR_ASSIGN,
            STATIC_IMPORT,
            STATIC_INIT,
//            STRICTFP,
//            STRING_LITERAL,
            SUPER_CTOR_CALL,
//            TYPE,
//            TYPECAST,
            TYPE_ARGUMENT,
            TYPE_ARGUMENTS,
//            TYPE_EXTENSION_AND,
            TYPE_LOWER_BOUNDS,
            TYPE_PARAMETER,
            TYPE_PARAMETERS,
            TYPE_UPPER_BOUNDS,
//            UNARY_MINUS,
//            UNARY_PLUS,
            VARIABLE_DEF,
//            WILDCARD_TYPE,
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
                AT, FORK1, DOT, BRANCH2,
                LABEL1, IDENT,
                LABEL2, FORK3, END,
                LABEL3, LPAREN, BRANCH5,
                LABEL4, COMMA,
                LABEL5, FORK6, INDENT, ANNOTATION_MEMBER_VALUE_PAIR, BRANCH9,
                LABEL6, FORK7, INDENT, ANNOTATION, BRANCH9,
                LABEL7, FORK8, INDENT, EXPR, BRANCH9,
                LABEL8, INDENT, WRAP, ANNOTATION_ARRAY_INIT,
                LABEL9, FORK4, UNINDENT, RPAREN, END
            );
            break;

        case ANNOTATION_MEMBER_VALUE_PAIR:
            this.checkChildren(
                ast,
                IDENT, ASSIGN, FORK1, WRAP, ANNOTATION_ARRAY_INIT, END,
                LABEL1, ANY_TOKEN, END
            );
            break;

        case ANNOTATION_ARRAY_INIT:
            this.checkChildren(
                ast,
                FORK2,
                LABEL1, INDENT, EXPR, FORK2, COMMA, FORK1,
                LABEL2, UNINDENT, RCURLY, END
            );
            break;

        case ANNOTATION_DEF:
            if (
                this.allowOneLineElement.contains(LocalTokenType.ANNOTATION_DEF)
                && WrapAndIndent.isSingleLine(ast)
            ) break;
            this.checkChildren(
                ast,
                MODIFIERS, WRAP, AT, LITERAL_INTERFACE, IDENT, WRAP, OBJBLOCK, END
            );
            break;

        case ARRAY_INIT:
            this.checkChildren(
                ast,
                FORK2,
                LABEL1, INDENT, ANY_TOKEN, FORK2, COMMA, FORK1,
                LABEL2, UNINDENT, RCURLY, END
            );
            break;

        case CASE_GROUP:
            this.checkChildren(
                ast,
                FORK3, LITERAL_CASE,        // case 1: case 2:
                LABEL1, FORK2, WRAP, LITERAL_CASE, BRANCH1,
                LABEL2, FORK4,
                LABEL3, WRAP, LITERAL_DEFAULT,
                LABEL4, INDENT_IF_CHILDREN, SLIST, END
            );
            break;

        case CLASS_DEF:
            if (
                this.allowOneLineElement.contains(LocalTokenType.CLASS_DEF)
                && WrapAndIndent.isSingleLine(ast)
            ) break;
            this.checkChildren(
                ast,
                MODIFIERS, WRAP, LITERAL_CLASS, IDENT, FORK1, TYPE_PARAMETERS,
                LABEL1, FORK2, WRAP, EXTENDS_CLAUSE,
                LABEL2, FORK3, WRAP, IMPLEMENTS_CLAUSE,
                LABEL3, WRAP, OBJBLOCK, END
            );
            break;

        case CTOR_CALL:
            this.checkChildren(
                ast,
                LPAREN, INDENT_IF_CHILDREN, ELIST, UNINDENT, RPAREN, SEMI, END
            );
            break;

        case CTOR_DEF:
            if (
                this.allowOneLineElement.contains(LocalTokenType.CTOR_DEF)
                && WrapAndIndent.isSingleLine(ast)
            ) break;
            this.checkChildren(
                ast,
                MODIFIERS, FORK1, TYPE_PARAMETERS,
                LABEL1, WRAP, IDENT, LPAREN, INDENT_IF_CHILDREN, PARAMETERS, UNINDENT, RPAREN, FORK2, WRAP, LITERAL_THROWS, // SUPPRESS CHECKSTYLE LineLength
                LABEL2, WRAP, SLIST, END
            );
            break;

        case ELIST:
            this.checkChildren(
                ast,
                FORK2,
                LABEL1, INDENT, EXPR, FORK2, COMMA, BRANCH1,
                LABEL2, END
            );
            break;

        case ENUM_DEF:
            if (
                this.allowOneLineElement.contains(LocalTokenType.ENUM_DEF)
                && WrapAndIndent.isSingleLine(ast)
            ) break;
            this.checkChildren(
                ast,
                MODIFIERS, WRAP, ENUM, IDENT, WRAP, OBJBLOCK, END
            );
            break;

        case EXPR:
            {
                DetailAST child = ast.getFirstChild();
                if (child.getType() == LPAREN.delocalize()) {
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
                VARIABLE_DEF, WRAP, COLON, EXPR, END
            );
            break;

        case INTERFACE_DEF:
            if (
                this.allowOneLineElement.contains(LocalTokenType.INTERFACE_DEF)
                && WrapAndIndent.isSingleLine(ast)
            ) break;
            this.checkChildren(
                ast,
                MODIFIERS, WRAP, LITERAL_INTERFACE, IDENT, FORK1, TYPE_PARAMETERS,
                LABEL1, FORK2, WRAP, EXTENDS_CLAUSE,
                LABEL2, WRAP, OBJBLOCK, END
            );
            break;

        case LABELED_STAT:
            this.checkChildren(
                ast,
                IDENT, WRAP, ANY_TOKEN, END
            );
            break;

        case LITERAL_DO:
            this.checkChildren(
                ast,
                WRAP, SLIST, DO_WHILE, LPAREN, INDENT, EXPR, UNINDENT, RPAREN, SEMI, END
            );
            break;

        case LITERAL_FOR:
            this.checkChildren(
                ast,
                LPAREN, FORK1, INDENT, FOR_INIT, SEMI, INDENT, FOR_CONDITION, SEMI, INDENT_IF_CHILDREN, FOR_ITERATOR, FORK2, // SUPPRESS CHECKSTYLE LineLength
                LABEL1, INDENT, FOR_EACH_CLAUSE,
                LABEL2, UNINDENT, RPAREN, FORK3, EXPR, SEMI, END,
                LABEL3, ANY_TOKEN, END
            );
            break;

        case LITERAL_IF:
            this.checkChildren(
                ast,
                LPAREN, INDENT, EXPR, UNINDENT, RPAREN, FORK1, EXPR, SEMI, END,
                LABEL1, ANY_TOKEN, FORK2, LITERAL_ELSE,
                LABEL2, END
            );
            break;

        case LITERAL_NEW:
            this.checkChildren(
                ast,
                ANY_TOKEN, FORK1, TYPE_ARGUMENTS,
                LABEL1, FORK3, ARRAY_DECLARATOR, FORK2, WRAP, ARRAY_INIT,
                LABEL2, END,
                LABEL3, LPAREN, INDENT_IF_CHILDREN, ELIST, UNINDENT, RPAREN, OPTIONAL, WRAP, OBJBLOCK, END
            );
            break;

        case LITERAL_SWITCH:
            this.checkChildren(
                ast,
                LPAREN, INDENT, EXPR, UNINDENT, RPAREN, LCURLY, FORK2,
                LABEL1, INDENT, CASE_GROUP, FORK1,
                LABEL2, UNINDENT, RCURLY, END
            );
            break;

        case METHOD_DEF:
            if (
                this.allowOneLineElement.contains(LocalTokenType.METHOD_DEF)
                && WrapAndIndent.isSingleLine(ast)
            ) break;
            this.checkChildren(
                ast,
                MODIFIERS, FORK1, TYPE_PARAMETERS,
                LABEL1, TYPE, WRAP, IDENT, LPAREN, INDENT_IF_CHILDREN, PARAMETERS, UNINDENT, RPAREN, FORK2, WRAP, LITERAL_THROWS, // SUPPRESS CHECKSTYLE LineLength
                LABEL2, FORK3, WRAP, SLIST, END,
                LABEL3, SEMI, END
            );
            break;

        case LITERAL_WHILE:
            this.checkChildren(
                ast,
                LPAREN, INDENT, EXPR, UNINDENT, RPAREN, FORK1,  EXPR, SEMI, END,
                LABEL1, ANY_TOKEN, END
            );
            break;

        case MODIFIERS:
            this.checkChildren(
                ast,
                FORK2,
                LABEL1, ANY_TOKEN, FORK1,
                LABEL2, END
            );
            break;

        case OBJBLOCK:
            this.checkChildren(
                ast,
                LCURLY, FORK3,
                LABEL1, INDENT, ENUM_CONSTANT_DEF, FORK2, COMMA, FORK1,
                LABEL2, FORK3, INDENT, SEMI,
                LABEL3, FORK5, INDENT, VARIABLE_DEF,
                LABEL4, FORK3, COMMA, VARIABLE_DEF, BRANCH4,
                LABEL5, FORK6, UNINDENT, RCURLY, END,
                LABEL6, INDENT, ANY_TOKEN, BRANCH2
            );
            break;

        case PARAMETERS:
            this.checkChildren(
                ast,
                FORK2,
                LABEL1, INDENT, PARAMETER_DEF, FORK2, COMMA, BRANCH1,
                LABEL2, END
            );
            break;

        case SLIST:
            // Single-line case group?
            if (
                ast.getParent().getType() == CASE_GROUP.delocalize()
                && this.allowOneLineElement.contains(LocalTokenType.CASE_GROUP)
                && WrapAndIndent.isSingleLine(ast)
                && ast.getParent().getLineNo() == ast.getLineNo()
            ) return;

            this.checkChildren(
                ast,
                LABEL1, FORK2, INDENT, EXPR, SEMI, BRANCH1,
                LABEL2, FORK5, INDENT, VARIABLE_DEF,
                LABEL3, FORK4, COMMA, VARIABLE_DEF, BRANCH3,
                LABEL4, SEMI, BRANCH1,
                // SLIST in CASE_GROUP ends _without_ an RCURLY!
                LABEL5, FORK6, END,
                LABEL6, FORK7, UNINDENT, RCURLY, END,
                LABEL7, INDENT, ANY_TOKEN, BRANCH1
            );
            break;

        case SUPER_CTOR_CALL:
            this.checkChildren(
                ast,
                FORK1, ANY_TOKEN, DOT,
                LABEL1, LPAREN, INDENT_IF_CHILDREN, ELIST, UNINDENT, RPAREN, SEMI, END
            );
            break;

        case VARIABLE_DEF:
            if (ast.getParent().getType() == OBJBLOCK.delocalize()) {
                this.checkChildren(
                    ast,
                    MODIFIERS, TYPE, WRAP, IDENT, FORK1, ASSIGN,
                    // Field declarations DO have a SEMI, local variable declarations DON'T!?
                    LABEL1, FORK2, SEMI,
                    LABEL2, END
                );
            } else {
                this.checkChildren(
                    ast,
                    MODIFIERS, TYPE, WRAP, IDENT, FORK1, ASSIGN,
                    // Field declarations DO have a SEMI, local variable declarations DON'T!?
                    LABEL1, FORK2, SEMI,
                    LABEL2, END
                );
            }
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
            {
                DetailAST topPackage    = WrapAndIndent.getLeftmostDescendant(ast.getFirstChild().getNextSibling());
                DetailAST bottomPackage = WrapAndIndent.getLeftmostDescendant(ast.getFirstChild().getNextSibling());
                DetailAST semicolon     = ast.getLastChild();
                this.checkSameLine(ast, topPackage);
                this.checkSameLine(topPackage, bottomPackage);
                this.checkSameLine(bottomPackage, semicolon);
            }
            break;

        case ASSIGN:
            if (ast.getChildCount() == 1) {

                // A field or local variable initialization.
                this.checkChildren(
                    ast,
                    FORK1, WRAP, ARRAY_INIT, END,
                    LABEL1, ANY_TOKEN, END
                );
            }
            break;

        case LITERAL_TRY:
            this.checkChildren(
                ast,
                SLIST, FORK2,
                LABEL1, WRAP, LITERAL_CATCH, FORK1, FORK3,
                LABEL2, WRAP, LITERAL_FINALLY,
                LABEL3, END
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
                    if (aType == PACKAGE_DEF || aType == IMPORT) break PACKAGE_OR_IMPORT;
                }
                break;
            }
            /*FALLTHROUGH*/
        case ANNOTATION_FIELD_DEF:
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
                ANY_TOKEN,
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

        if (expression.getType() == QUESTION.delocalize()) {
            System.currentTimeMillis();
        }
        switch (LocalTokenType.localize(expression.getType())) {

        // Ternary operation
        case QUESTION:
            {
                DetailAST c = this.checkParenthesizedExpression(expression.getFirstChild(), inline);
                c = this.checkParenthesizedExpression(c, inline);
                assert c.getType() == COLON.delocalize();
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
                assert c.getType() == RBRACK.delocalize();
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
                if (c != null && c.getType() == TYPE_ARGUMENTS.delocalize()) {

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
                    if (lhs.getLineNo() == expression.getLineNo()) {
                        this.checkSameLine(lhs, expression, LocalTokenType.EXPR);
                    } else {
                        this.checkWrapped(lhs, expression, LocalTokenType.EXPR);
                    }
                }

                // Check wrapping and alignment of operator and RHS.
                {
                    DetailAST rhs = WrapAndIndent.getLeftmostDescendant(c);
                    if (expression.getLineNo() == rhs.getLineNo()) {
                        this.checkSameLine(expression, rhs, LocalTokenType.EXPR);
                    } else {
                        this.checkWrapped(expression, rhs, LocalTokenType.EXPR);
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
                assert c.getType() == RBRACK.delocalize();
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

                assert rparen.getType() == RPAREN.delocalize();
                assert rparen.getNextSibling() == null;

                DetailAST firstArgument = arguments.getFirstChild();
                if (
                    firstArgument == null
                    || WrapAndIndent.getLeftmostDescendant(firstArgument).getLineNo() == expression.getLineNo()
                ) {
                    this.checkSameLine(WrapAndIndent.getRightmostDescendant(arguments), rparen);
                } else {
                    this.checkWrapped(
                        WrapAndIndent.getLeftmostDescendant(expression),
                        rparen,
                        LocalTokenType.EXPR
                    );
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
            if (tmp == null && ast.getType() == MODIFIERS.delocalize()) tmp = ast.getNextSibling();
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

        if (previous.getType() != LPAREN.delocalize()) {
            this.checkExpression(previous, inline);
            return previous.getNextSibling();
        }

        @SuppressWarnings("unused") AstDumper dumper = new AstDumper(previous); // For debugging

        DetailAST next = previous.getNextSibling();
        for (;;) {
            if (next.getType() != LPAREN.delocalize()) {
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
            this.checkIndented(previous, WrapAndIndent.getLeftmostDescendant(next), LocalTokenType.EXPR);
            this.checkExpression(next, false);
            previous = next;
            next     = next.getNextSibling();
            this.checkUnindented(WrapAndIndent.getRightmostDescendant(previous), next, LocalTokenType.EXPR);
        }

        previous = next;
        assert next.getType() == RPAREN.delocalize();
        return next.getNextSibling();
    }

    /**
     * Verifies that the children of the given {@code ast} are positioned as specified.
     *
     * @param args A sequence of {@link LocalTokenType}s and {@link Control}s
     */
    private void
    checkChildren(DetailAST ast, Object... args) {

        @SuppressWarnings("unused") AstDumper astDumper = new AstDumper(ast); // For debugging

        DetailAST child = ast.getFirstChild();

        // Determine the "indentation parent".
        LocalTokenType wrappingContext;
        switch (LocalTokenType.localize(ast.getType())) {

        case ELIST:      // There's an ELIST between the METH_CALL ('(') and the argument EXPRs.
            ast = ast.getParent();
            wrappingContext = LocalTokenType.ELIST;
            break;

        case SLIST:
            if (AstUtil.parentTypeIs(ast, LocalTokenType.CASE_GROUP)) {
                ast = ast.getParent().getParent();
                wrappingContext = LocalTokenType.CASE_GROUP;
            } else {
                wrappingContext = LocalTokenType.localize(ast.getType());
            }
            break;

        case PARAMETERS:
            ast = ast.getPreviousSibling(); // Use the LPAREN, not the PARAMETERS.
            wrappingContext = LocalTokenType.PARAMETERS;
            break;

        case DOT:
            ast = WrapAndIndent.getLeftmostDescendant(ast);
            wrappingContext = LocalTokenType.DOT;
            break;

        default:
            ;
            wrappingContext = LocalTokenType.localize(ast.getType());
            break;
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
                        && (tokenType == ANY_TOKEN || tokenType == LocalTokenType.localize(child.getType()))
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
                                        doBranch = child == null || ((LocalTokenType) na).delocalize() != child.getType(); // SUPPRESS CHECKSTYLE LineLength
                                        break DO_BRANCH;
                                    } else
                                    if (na == ANY_TOKEN) {
                                        assert da != ANY_TOKEN;
                                        doBranch = child == null;
                                        break DO_BRANCH;
                                    } else
                                    if (da == ANY_TOKEN) {
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

                case ANY_TOKEN:
                    if (child == null) {
                        this.log(
                            previousAst,
                            "Token missing after ''{0}''",
                            previousAst.getText()
                        );
                        return;
                    }

                    previousAst = WrapAndIndent.getRightmostDescendant(child);
                    child       = child.getNextSibling();
                    break;

                case INDENT_IF_CHILDREN:
                    assert child != null;
                    if (child.getFirstChild() == null) break;
                    /*FALLTHROUGH*/

                case INDENT:
                    assert child != null;
                    switch (mode) {

                    case 0:
                        {
                            DetailAST c = WrapAndIndent.getLeftmostDescendant(child);
                            if (c.getLineNo() == previousAst.getLineNo()) {
                                this.checkOneLineAllowed(previousAst, c, wrappingContext);
                                mode = 1;
                            } else {
                                mode = 2;
                                if (child.getType() == CASE_GROUP.delocalize()) {
                                    this.checkWrapped(ast, c, wrappingContext);
                                } else {
                                    this.checkIndented(ast, c, wrappingContext);
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

                                this.checkMultipleSubelementsPerLineAllowed(l, previousAst, wrappingContext);
                            } else {
                                if (child.getType() == CASE_GROUP.delocalize()) {
                                    this.checkWrapped(ast, l, wrappingContext);
                                } else {
                                    this.checkIndented(ast, l, wrappingContext);
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
                            this.checkSameLine(ast, child);
                        }
                        break;

                    case 1:
                        this.checkSameLine(previousAst, child);
                        break;

                    case 2:
                        this.checkWrapped(ast, child, wrappingContext);
                        break;
                    }
                    mode = 0;
                    break;

                case WRAP:
                    assert child != null;
                    assert mode == 0;
                    if (child.getLineNo() == previousAst.getLineNo()) {
                        this.checkSameLine(previousAst, child, wrappingContext);
                    } else {
                        this.checkWrapped(previousAst, child, wrappingContext);
                    }
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
                child       = child.getNextSibling();
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
            INDENT, UNINDENT, INDENT_IF_CHILDREN,
            WRAP,
            LABEL1, LABEL2, LABEL3, LABEL4, LABEL5, LABEL6, LABEL7, LABEL8, LABEL9
        ));
        SKIPPABLES = Collections.unmodifiableSet(ss);
    }

    /**
     * Checks whether it is OK that the two tokens appear in different lines, and, if so, check whether {@code next}
     * is indented by {@link #DEFAULT_INDENTATION}, compared to the line where {@code previous} occurs.
     */
    private void
    checkIndented(DetailAST previous, DetailAST next, LocalTokenType context) {
        if (!this.allowElementWrapping.contains(context) && !this.allowStrictElementWrapping.contains(context)) {
            this.log(next, WrapAndIndent.MESSAGE_KEY_MUST_JOIN, next.getText(), previous.getText());
        } else {
            this.checkAlignment(next, previous, this.basicOffset);
        }
    }

    /**
     * Checks whether it is OK that the two tokens appear in different lines, and, if so, check whether {@code next}
     * is unindented by {@link #DEFAULT_INDENTATION}, compared to the line where {@code previous} occurs.
     */
    private void
    checkUnindented(DetailAST previous, DetailAST next, LocalTokenType context) {
        if (!this.allowElementWrapping.contains(context) && !this.allowStrictElementWrapping.contains(context)) {
            this.log(next, WrapAndIndent.MESSAGE_KEY_MUST_JOIN, next.getText(), previous.getText());
        } else {
            this.checkAlignment(next, previous, -this.basicOffset);
        }
    }

    /**
     * Checks whether it is OK that the {@code element} and the {@code subelement} are on the same line.
     */
    private void
    checkOneLineAllowed(DetailAST element, DetailAST subelement, LocalTokenType context) {
        if (!this.allowOneLineElement.contains(context)) {
            this.log(subelement, WrapAndIndent.MESSAGE_KEY_MUST_WRAP, element.getText(), subelement.getText());
        }
    }

    /**
     * Checks whether it is OK that the two subelements are on the same line.
     */
    private void
    checkMultipleSubelementsPerLineAllowed(DetailAST previous, DetailAST next, LocalTokenType context) {
        if (!this.allowElementWrapping.contains(context)) {
            this.log(next, WrapAndIndent.MESSAGE_KEY_MUST_WRAP, previous.getText(), next.getText());
        }
    }

    /**
     * Checks whether it is OK that the two tokens appear in different lines, and if so, verifies that {@code next}
     * appears vertically aligned with the first token in the line of {@code prev}.
     */
    private void
    checkWrapped(DetailAST previous, DetailAST next, LocalTokenType context) {
        if (!this.allowElementWrapping.contains(context) && !this.allowStrictElementWrapping.contains(context)) {
            this.log(next, WrapAndIndent.MESSAGE_KEY_MUST_JOIN, next.getText(), previous.getText());
        } else {
            this.checkAlignment(next, previous, 0);
        }
    }

    /**
     * Checks whether it is OK that the two tokens appear on the same line.
     */
    private void
    checkSameLine(DetailAST previous, DetailAST next, LocalTokenType context) {
        if (!this.allowOneLineElement.contains(context)) {
            this.checkSameLine(previous, next);
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
                WrapAndIndent.MESSAGE_KEY_MUST_JOIN,
                right.getText(),
                left.getText()
            );
        }
    }

    /**
     * Logs a problem iff the given {@code ast} is not vertically aligned with the given {@code reference}, plus
     * the {@code offset}.
     */
    private void
    checkAlignment(DetailAST ast, DetailAST reference, int offset) {

        int astColumn = Utils.lengthExpandedTabs(
            this.getLines()[ast.getLineNo() - 1],
            ast.getColumnNo(),
            this.getTabWidth()
        );
        int referenceColumn = Utils.lengthExpandedTabs(
            this.getLines()[reference.getLineNo() - 1],
            reference.getColumnNo(),
            this.getTabWidth()
        );

        if (astColumn != referenceColumn + offset) {
            this.log(
                ast,
                WrapAndIndent.MESSAGE_KEY_WRONG_COLUMN,
                ast.getText(),
                referenceColumn + 1,
                astColumn + 1
            );
        }
    }
}

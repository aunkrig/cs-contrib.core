
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
 * Verifies that tokens are, respectively are not preceded with (and/or followed by) whitespace.
 * <p>
 *   This check supersedes all of CheckStyle's whitespace-related checks:
 * </p>
 * <ul>
 *   <li>Generic Whitespace</li>
 *   <li>Empty For Initializer Pad</li>
 *   <li>Empty For Iterator Pad</li>
 *   <li>No Whitespace After</li>
 *   <li>No Whitespace Before</li>
 *   <li>Method Parameter Pad</li>
 *   <li>Paren Pad</li>
 *   <li>Typecast Paren Pad</li>
 *   <li>Whitespace After</li>
 *   <li>Whitespace Around</li>
 * </ul>
 * <p>
 *   , as well as
 * </p>
 * <ul>
 *   <li>de.unkrig.ParenPad</li>
 *   <li>de.unkrig.WhitespaceAround</li>
 * </ul>
 *
 * @cs-rule-group  %Whitespace.group
 * @cs-rule-name   de.unkrig: Whitespace
 * @cs-rule-parent TreeWalker
 */
@NotNullByDefault(false) public
class Whitespace extends Check {

    /** @cs-message ''{0}'' is followed by whitespace (option ''{1}'') */
    public static final String MESSAGE_KEY_FOLLOWED = "de.unkrig.cscontrib.checks.Whitespace.followed";

    /** @cs-message ''{0}'' is not followed by whitespace (option ''{1}'') */
    public static final String MESSAGE_KEY_NOT_FOLLOWED = "de.unkrig.cscontrib.checks.Whitespace.notFollowed";

    /** @cs-message ''{0}'' is preceded with whitespace (option ''{1}'') */
    public static final String MESSAGE_KEY_PRECEDED = "de.unkrig.cscontrib.checks.Whitespace.preceded";

    /** @cs-message ''{0}'' is not preceded with whitespace (option ''{1}'') */
    public static final String MESSAGE_KEY_NOT_PRECEDED = "de.unkrig.cscontrib.checks.Whitespace.notPreceded";

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

    // SUPPRESS CHECKSTYLE LineLength:6
    /**
     * The Java elements which must be preceded with whitespace (or a line break).
     *
     * @cs-property-name            whitespaceBefore
     * @cs-property-datatype        MultiCheck
     * @cs-property-default-value   and__expr,and__type_bound,and_assign,assert,assign__assignment,assign__var_decl,break,case,catch,class__class_decl,colon__enhanced_for,colon__ternary,conditional_and,conditional_or,continue,default__anno_elem,default__switch,divide,divide_assign,do,else,enum,equal,extends__type,extends__type_bound,finally,for,greater,greater_equal,if,implements,import,import__static_import,instanceof,l_angle__meth_decl_type_params,l_curly__anon_class,l_curly__block,l_curly__catch,l_curly__do,l_curly__empty_anon_class,l_curly__empty_catch,l_curly__empty_meth_decl,l_curly__empty_type_decl,l_curly__enum_const,l_curly__finally,l_curly__for,l_curly__if,l_curly__instance_init,l_curly__labeled_stat,l_curly__meth_decl,l_curly__static_init,l_curly__switch,l_curly__synchronized,l_curly__try,l_curly__type_decl,l_curly__while,l_paren__catch,l_paren__do_while,l_paren__for,l_paren__for_no_init,l_paren__if,left_shift,left_shift_assign,less,less_equal,minus__additive,minus_assign,modulo,modulo_assign,multiply,multiply_assign,name__ctor_decl,name__meth_decl,name__param,name__type_decl,name__local_var_decl,not_equal,or,or_assign,package,plus__additive,plus_assign,question__ternary,r_curly__anno_array_init,r_curly__anon_class,r_curly__array_init,r_curly__block,r_curly__catch,r_curly__do,r_curly__else,r_curly__finally,r_curly__for,r_curly__if,r_curly__instance_init,r_curly__labeled_stat,r_curly__meth_decl,r_curly__static_init,r_curly__switch,r_curly__synchronized,r_curly__try,r_curly__type_decl,r_curly__while,return__expr,return__no_expr,right_shift,right_shift_assign,semi__type_decl,static__static_import,static__static_init,super__type_bound,switch,synchronized__mod,synchronized__synchronized,this__ctor_call,throw,throws,try,unsigned_right_shift,unsigned_right_shift_assign,while__do,while__while,xor,xor_assign
     * @cs-property-option-provider de.unkrig.cscontrib.util.JavaElement
     */
    public void
    setWhitespaceBefore(String[] sa) { this.whitespaceBefore = Whitespace.toEnumSet(sa, JavaElement.class); }

    // SUPPRESS CHECKSTYLE LineLength:6
    /**
     * The Java elements which must not be preceded with whitespace (or are preceded with a line break).
     *
     * @cs-property-name            noWhitespaceBefore
     * @cs-property-datatype        MultiCheck
     * @cs-property-default-value   class__class_literal,colon__default,colon__case,colon__labeled_stat,comma,dot__import,dot__package_decl,dot__qualified_type,dot__selector,ellipsis,l_angle__meth_invocation_type_args,l_angle__type_args,l_angle__type_params,l_brack__array_decl,l_brack__index,l_paren__anno,l_paren__anno_elem_decl,l_paren__meth_invocation,l_paren__params,name__anno,post_decr,post_incr,r_angle__meth_decl_type_params,r_angle__meth_invocation_type_args,r_angle__type_args,r_angle__type_params,r_brack__array_decl,r_brack__index,r_curly__empty_anno_array_init,r_curly__empty_anon_class,r_curly__empty_array_init,r_curly__empty_catch,r_curly__empty_meth_decl,r_curly__empty_type_decl,r_curly__enum_const_decl,r_paren__anno,r_paren__anno_elem_decl,r_paren__meth_invocation,r_paren__cast,r_paren__catch,r_paren__do_while,r_paren__for,r_paren__for_no_update,r_paren__if,r_paren__params,r_paren__parenthesized,semi__abstract_meth_decl,semi__anno_elem_decl,semi__enum_decl,semi__field_decl,semi__for_condition_no_update,semi__for_condition_update,semi__for_init_condition,semi__for_init_no_condition,semi__for_no_condition_no_update,semi__for_no_condition_update,semi__for_no_init_condition,semi__for_no_init_no_condition,semi__import,semi__package_decl,semi__statement,semi__static_import,star__type_import_on_demand
     * @cs-property-option-provider de.unkrig.cscontrib.util.JavaElement
     */
    public void
    setNoWhitespaceBefore(String[] sa) { this.noWhitespaceBefore = Whitespace.toEnumSet(sa, JavaElement.class); }

    // SUPPRESS CHECKSTYLE LineLength:6
    /**
     * The Java elements which must be followed by whitespace (or a line break).
     *
     * @cs-property-name            whitespaceAfter
     * @cs-property-datatype        MultiCheck
     * @cs-property-default-value   abstract,and__expr,and__type_bound,and_assign,assert,assign__assignment,assign__var_decl,case,catch,class__class_decl,colon__case,colon__default,colon__enhanced_for,colon__labeled_stat,colon__ternary,comma,conditional_and,conditional_or,default__anno_elem,divide,divide_assign,do,ellipsis,else,enum,equal,extends__type,extends__type_bound,final,finally,for,greater,greater_equal,if,implements,import,import__static_import,instanceof,interface,l_curly__anno_array_init,l_curly__anon_class,l_curly__array_init,l_curly__block,l_curly__catch,l_curly__do,l_curly__enum_const,l_curly__finally,l_curly__for,l_curly__if,l_curly__instance_init,l_curly__labeled_stat,l_curly__meth_decl,l_curly__static_init,l_curly__switch,l_curly__synchronized,l_curly__try,l_curly__type_decl,l_curly__while,left_shift,left_shift_assign,less,less_equal,minus__additive,minus_assign,modulo,modulo_assign,multiply,multiply_assign,name__anno_member,native,new,not_equal,or,or_assign,package,plus__additive,plus_assign,private,protected,public,question__ternary,r_angle__meth_decl_type_params,r_curly__block,r_curly__catch,r_curly__do,r_curly__else,r_curly__empty_catch,r_curly__empty_meth_decl,r_curly__empty_type_decl,r_curly__finally,r_curly__for,r_curly__if,r_curly__instance_init,r_curly__labeled_stat,r_curly__meth_decl,r_curly__static_init,r_curly__switch,r_curly__synchronized,r_curly__try,r_curly__type_decl,r_curly__while,r_paren__cast,r_paren__catch,r_paren__if,return__expr,right_shift,right_shift_assign,semi__abstract_meth_decl,semi__anno_elem_decl,semi__empty_stat,semi__enum_decl,semi__field_decl,semi__for_condition_update,semi__for_init_condition,semi__for_no_condition_update,semi__for_no_init_condition,semi__import,semi__package_decl,semi__statement,semi__static_import,semi__type_decl,static__mod,static__static_import,static__static_init,super__type_bound,switch,synchronized__mod,synchronized__synchronized,throw,throws,transient,try,unsigned_right_shift,unsigned_right_shift_assign,volatile,while__do,while__while,xor,xor_assign
     * @cs-property-option-provider de.unkrig.cscontrib.util.JavaElement
     */
    public void
    setWhitespaceAfter(String[] sa) { this.whitespaceAfter = Whitespace.toEnumSet(sa, JavaElement.class); }

    // SUPPRESS CHECKSTYLE LineLength:6
    /**
     * The Java elements which must not be followed by whitespace (or are followed by a line break).
     *
     * @cs-property-name            noWhitespaceAfter
     * @cs-property-datatype        MultiCheck
     * @cs-property-default-value   at__anno,at__anno_decl,bitwise_complement,default__switch,dot__import,dot__package_decl,dot__qualified_type,dot__selector,l_angle__meth_decl_type_params,l_angle__meth_invocation_type_args,l_angle__type_args,l_angle__type_params,l_brack__array_decl,l_brack__index,l_curly__empty_anno_array_init,l_curly__empty_anon_class,l_curly__empty_array_init,l_curly__empty_catch,l_curly__empty_meth_decl,l_curly__empty_type_decl,l_paren__anno,l_paren__anno_elem_decl,l_paren__meth_invocation,l_paren__cast,l_paren__catch,l_paren__do_while,l_paren__for,l_paren__for_no_init,l_paren__if,l_paren__params,l_paren__parenthesized,logical_complement,minus__unary,name__anno_elem_decl,name__ctor_decl,name__import_component,name__import_type,name__meth_decl,name__package_decl,name__param,plus__unary,pre_decr,pre_incr,r_angle__meth_invocation_type_args,r_paren__do_while,return__no_expr,semi__for_condition_no_update,semi__for_init_no_condition,semi__for_no_condition_no_update,semi__for_no_init_no_condition,star__type_import_on_demand,super__ctor_call,super__expr,this__ctor_call
     * @cs-property-option-provider de.unkrig.cscontrib.util.JavaElement
     */
    public void
    setNoWhitespaceAfter(String[] sa)  { this.noWhitespaceAfter  = Whitespace.toEnumSet(sa, JavaElement.class); }

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
                    this.log(ast, Whitespace.MESSAGE_KEY_NOT_PRECEDED, ast.getText(), javaElement);
                } else
                if (mustNotBeWhitespaceBefore && isWhitespace) {
                    this.log(ast, Whitespace.MESSAGE_KEY_PRECEDED, ast.getText(), javaElement);
                }
            }
        }

        // Check whitespace AFTER token.
        if (mustBeWhitespaceAfter || mustNotBeWhitespaceAfter) {
            int after = ast.getColumnNo() + ast.getText().length();

            if (after < line.length() && !Whitespace.LINE_SUFFIX.matcher(line).region(after, line.length()).matches()) {
                boolean isWhitespace = Character.isWhitespace(line.charAt(after));
                if (mustBeWhitespaceAfter && !isWhitespace) {
                    this.log(ast.getLineNo(), after, Whitespace.MESSAGE_KEY_NOT_FOLLOWED, ast.getText(), javaElement);
                } else
                if (mustNotBeWhitespaceAfter && isWhitespace) {
                    this.log(ast.getLineNo(), after, Whitespace.MESSAGE_KEY_FOLLOWED, ast.getText(), javaElement);
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

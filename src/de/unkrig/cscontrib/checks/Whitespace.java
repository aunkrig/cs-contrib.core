
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
import static de.unkrig.cscontrib.util.AstUtil.previousSiblingTypeIs;

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
        ARITHMETIC_OPERATORS,
        ASSIGNMENTS,
        AT,
        BITWISE_OPERATORS,
        BITWISE_COMPLEMENT,
        COLON_DEFAULT,
        COLON_CASE,
        COLON_ENHANCED_FOR,
        COLON_TERNARY,
        COMMA,
        DO_WHILE,
        DOT,
        EMPTY_STAT,
        EQUALITIES,
        GENERIC_END,
        GENERIC_START,
        L_BRACK_ARRAY_DECL,
        L_CURLY_ARRAY_INIT,
        L_CURLY_METHOD_DEF,
        L_CURLY_STATIC_INIT,
        L_CURLY_TYPE_DEF,
        L_PAREN_ANNOTATION,
        L_PAREN_CALL,
        L_PAREN_DO_WHILE,
        L_PAREN_PARAMETERS,
        NAME_VARIABLE_DEF,
        PRE_DEC,
        R_BRACK_ARRAY_DECL,
        R_CURLY_ANON_CLASS,
        R_CURLY_ARRAY_INIT,
        R_CURLY_CATCH_BLOCK,
        R_CURLY_METHOD_DEF,
        R_CURLY_STATIC_INIT,
        R_CURLY_TYPE_DEF,
        R_PAREN_ANNOTATION,
        R_PAREN_CALL,
        R_PAREN_DO_WHILE,
        R_PAREN_PARAMETERS,
        SEMI_ABSTRACT_METH_DEF,
        SEMI_FOR_CONDITION,
        SEMI_FOR_INIT,
        SEMI_PACKAGE_DEF,
        SEMI_STATEMENT
    }

    private EnumSet<Whitespaceable> whitespaceBefore = EnumSet.of(
        ARITHMETIC_OPERATORS,
        ASSIGNMENTS,
        BITWISE_OPERATORS,
        COLON_ENHANCED_FOR,
        COLON_TERNARY,
        DO_WHILE,
        EQUALITIES,
        L_CURLY_ARRAY_INIT,
        L_CURLY_METHOD_DEF,
        L_CURLY_STATIC_INIT,
        L_CURLY_TYPE_DEF,
        L_PAREN_DO_WHILE,
        NAME_VARIABLE_DEF,
        R_CURLY_ANON_CLASS,
        R_CURLY_ARRAY_INIT,
        R_CURLY_CATCH_BLOCK,
        R_CURLY_METHOD_DEF,
        R_CURLY_STATIC_INIT,
        R_CURLY_TYPE_DEF
    );
    private EnumSet<Whitespaceable> noWhitespaceBefore = EnumSet.of(
        COLON_DEFAULT,
        COLON_CASE,
        COMMA,
        DOT,
        EMPTY_STAT,
        GENERIC_END,
        L_BRACK_ARRAY_DECL,
        L_PAREN_ANNOTATION,
        L_PAREN_CALL,
        L_PAREN_PARAMETERS,
        R_BRACK_ARRAY_DECL,
        R_PAREN_ANNOTATION,
        R_PAREN_CALL,
        R_PAREN_DO_WHILE,
        R_PAREN_PARAMETERS,
        SEMI_ABSTRACT_METH_DEF,
        SEMI_FOR_CONDITION,
        SEMI_FOR_INIT,
        SEMI_PACKAGE_DEF,
        SEMI_STATEMENT
    );
    private EnumSet<Whitespaceable> whitespaceAfter = EnumSet.of(
        ARITHMETIC_OPERATORS,
        ASSIGNMENTS,
        BITWISE_OPERATORS,
        COLON_DEFAULT,
        COLON_CASE,
        COLON_ENHANCED_FOR,
        COLON_TERNARY,
        COMMA,
        DO_WHILE,
        EMPTY_STAT,
        EQUALITIES,
        L_CURLY_ARRAY_INIT,
        L_CURLY_METHOD_DEF,
        L_CURLY_STATIC_INIT,
        L_CURLY_TYPE_DEF,
        R_CURLY_METHOD_DEF,
        R_CURLY_STATIC_INIT,
        R_CURLY_TYPE_DEF,
        R_PAREN_ANNOTATION,
        SEMI_ABSTRACT_METH_DEF,
        SEMI_FOR_CONDITION,
        SEMI_FOR_INIT,
        SEMI_PACKAGE_DEF,
        SEMI_STATEMENT
    );
    private EnumSet<Whitespaceable> noWhitespaceAfter = EnumSet.of(
        AT,
        BITWISE_COMPLEMENT,
        DOT,
        GENERIC_START,
        PRE_DEC,
        L_BRACK_ARRAY_DECL,
        L_PAREN_ANNOTATION,
        L_PAREN_CALL,
        L_PAREN_DO_WHILE,
        L_PAREN_PARAMETERS
    );

    private boolean allowEmptyAnonClass   = true;
    private boolean allowEmptyArrayInit   = true;
    private boolean allowEmptyCatchBlock  = true;
    private boolean allowEmptyInitializer = true;
    private boolean allowEmptyMethod      = true;
    private boolean allowEmptyType        = true;

    // BEGIN CONFIGURATION SETTERS

    // CHECKSTYLE JavadocMethod:OFF
    public void setWhitespaceBefore(String[] sa)   { this.whitespaceBefore   = toEnumSet(sa, Whitespaceable.class); }
    public void setNoWhitespaceBefore(String[] sa) { this.noWhitespaceBefore = toEnumSet(sa, Whitespaceable.class); }
    public void setWhitespaceAfter(String[] sa)    { this.whitespaceAfter    = toEnumSet(sa, Whitespaceable.class); }
    public void setNoWhitespaceAfter(String[] sa)  { this.noWhitespaceAfter  = toEnumSet(sa, Whitespaceable.class); }

    public void setAllowEmptyAnonClass(boolean value)   { this.allowEmptyAnonClass   = value; }
    public void setAllowEmptyArrayInit(boolean value)   { this.allowEmptyArrayInit   = value; }
    public void setAllowEmptyCatchBlock(boolean value)  { this.allowEmptyCatchBlock  = value; }
    public void setAllowEmptyInitializer(boolean value) { this.allowEmptyInitializer = value; }
    public void setAllowEmptyType(boolean value)        { this.allowEmptyType        = value; }
    public void setAllowEmptyMethod(boolean value)      { this.allowEmptyMethod      = value; }
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
        Whitespaceable whitespaceable               = null;
        boolean        allowMissingWhitespaceBefore = false;
        boolean        allowMissingWhitespaceAfter  = false;
        switch (ast.getType()) {

        case TokenTypes.ABSTRACT:
        case TokenTypes.ANNOTATION:
        case TokenTypes.ANNOTATION_ARRAY_INIT:
        case TokenTypes.ANNOTATION_DEF:
        case TokenTypes.ANNOTATION_FIELD_DEF:
        case TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR:
        case TokenTypes.ANNOTATIONS:
            break;
        case TokenTypes.ARRAY_DECLARATOR:
            whitespaceable = L_BRACK_ARRAY_DECL;
            break;
        case TokenTypes.ARRAY_INIT:
            whitespaceable              = L_CURLY_ARRAY_INIT;
            allowMissingWhitespaceAfter = firstChildType == TokenTypes.RCURLY && this.allowEmptyArrayInit;
            break;

        case TokenTypes.ASSIGN:
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
            whitespaceable = ASSIGNMENTS;
            break;

        case TokenTypes.AT:
            whitespaceable = AT;
            break;

        case TokenTypes.BAND:
        case TokenTypes.BOR:
        case TokenTypes.BSR:
        case TokenTypes.BXOR:
            whitespaceable = BITWISE_OPERATORS;
            break;

        case TokenTypes.BNOT:
            whitespaceable = BITWISE_COMPLEMENT;
            break;

        case TokenTypes.CASE_GROUP:
        case TokenTypes.CHAR_LITERAL:
        case TokenTypes.CLASS_DEF:
            break;

        case TokenTypes.COLON:
            if (parentType == TokenTypes.LITERAL_DEFAULT) { // 'default:'
                whitespaceable = COLON_DEFAULT;
            } else
            if (parentType == TokenTypes.LITERAL_CASE) {    // 'case 77:'
                whitespaceable = COLON_CASE;
            } else
            if (parentType == TokenTypes.FOR_EACH_CLAUSE) { // 'for (Object o : list) {'
                whitespaceable = COLON_ENHANCED_FOR;
            } else
            {                                               // 'a ? b : c'
                whitespaceable = COLON_TERNARY;
            }
            break;

        case TokenTypes.COMMA:
            whitespaceable = COMMA;
            break;

        case TokenTypes.CTOR_CALL:
            break;
        case TokenTypes.CTOR_DEF:
            break;

        case TokenTypes.DEC:
            whitespaceable = PRE_DEC; // '--x'
            break;

        case TokenTypes.DIV:  // 'a / b'
        case TokenTypes.PLUS: // 'a + b'
        case TokenTypes.MOD:  // 'a % b'
            whitespaceable = ARITHMETIC_OPERATORS;
            break;

        case TokenTypes.DO_WHILE: // '... } while (...);'
            whitespaceable = DO_WHILE;
            break;

        case TokenTypes.DOT: // 'a.b'
            whitespaceable = DOT;
            break;

        case TokenTypes.ELIST:
            break;
        case TokenTypes.ELLIPSIS: // 'meth(int x, ...) {'
            break;

        case TokenTypes.EMPTY_STAT: // ';'
            whitespaceable = EMPTY_STAT;
            break;

        case TokenTypes.ENUM:              // 'enum MyEnum {'
        case TokenTypes.ENUM_CONSTANT_DEF: // 'enum MyEnum { A, B }'
        case TokenTypes.ENUM_DEF:          // 'enum MyEnum {'
            break;

        case TokenTypes.EOF:
            break;

        case TokenTypes.LT:
        case TokenTypes.LE:
        case TokenTypes.EQUAL:
        case TokenTypes.NOT_EQUAL:
        case TokenTypes.GE:
        case TokenTypes.GT:
            whitespaceable = EQUALITIES;
            break;

        case TokenTypes.EXPR:
        case TokenTypes.EXTENDS_CLAUSE:
        case TokenTypes.FINAL:
        case TokenTypes.FOR_CONDITION:
        case TokenTypes.FOR_EACH_CLAUSE:
        case TokenTypes.FOR_INIT:
        case TokenTypes.FOR_ITERATOR:
            break;

        case TokenTypes.GENERIC_END:
            whitespaceable = GENERIC_END;
            break;

        case TokenTypes.GENERIC_START:
            whitespaceable = GENERIC_START;
            break;

        case TokenTypes.IDENT:
            if (parentType == TokenTypes.VARIABLE_DEF) {
                whitespaceable = NAME_VARIABLE_DEF;
            }
            break;
        case TokenTypes.IMPLEMENTS_CLAUSE:
        case TokenTypes.IMPORT:
        case TokenTypes.INC:
        case TokenTypes.INDEX_OP:
        case TokenTypes.INSTANCE_INIT:
        case TokenTypes.INTERFACE_DEF:
        case TokenTypes.LABELED_STAT:
        case TokenTypes.LAND:
            break;

        case TokenTypes.LCURLY:
            if (parentType == TokenTypes.OBJBLOCK && (
                grandparentType == TokenTypes.CLASS_DEF         // 'class MyClass() {...}'
                || grandparentType == TokenTypes.INTERFACE_DEF  // 'interface MyInterface() {...}'
                || grandparentType == TokenTypes.LITERAL_NEW    // 'new MyClass() {...}'
                || grandparentType == TokenTypes.ANNOTATION_DEF // 'new @MyAnnotation {...}'
            )) {
                whitespaceable              = L_CURLY_TYPE_DEF;
                allowMissingWhitespaceAfter = nextSiblingType == TokenTypes.RCURLY && this.allowEmptyType;
            } else
            if (parentType == TokenTypes.ARRAY_INIT) { // 'int[] ia = {...}', 'new int[] {...}'
                whitespaceable              = L_CURLY_ARRAY_INIT;
                allowMissingWhitespaceAfter = this.allowEmptyArrayInit;
            } else
            {
                break;
            }
            break;

        case TokenTypes.LITERAL_ASSERT:
        case TokenTypes.LITERAL_BOOLEAN:
        case TokenTypes.LITERAL_BREAK:
        case TokenTypes.LITERAL_BYTE:
        case TokenTypes.LITERAL_CASE:
        case TokenTypes.LITERAL_CATCH:
        case TokenTypes.LITERAL_CHAR:
        case TokenTypes.LITERAL_CLASS:
        case TokenTypes.LITERAL_CONTINUE:
        case TokenTypes.LITERAL_DEFAULT:
        case TokenTypes.LITERAL_DO:
        case TokenTypes.LITERAL_DOUBLE:
        case TokenTypes.LITERAL_ELSE:
        case TokenTypes.LITERAL_FALSE:
        case TokenTypes.LITERAL_FINALLY:
        case TokenTypes.LITERAL_FLOAT:
        case TokenTypes.LITERAL_FOR:
        case TokenTypes.LITERAL_IF:
        case TokenTypes.LITERAL_INSTANCEOF:
        case TokenTypes.LITERAL_INT:
        case TokenTypes.LITERAL_INTERFACE:
        case TokenTypes.LITERAL_LONG:
        case TokenTypes.LITERAL_NATIVE:
        case TokenTypes.LITERAL_NEW:
        case TokenTypes.LITERAL_NULL:
        case TokenTypes.LITERAL_PRIVATE:
        case TokenTypes.LITERAL_PROTECTED:
        case TokenTypes.LITERAL_PUBLIC:
            break;

        case TokenTypes.LITERAL_RETURN:
            if (firstChildType == TokenTypes.SEMI) { // 'return;'
                break;
            } else { // 'return x;'
                break;
            }

        case TokenTypes.LITERAL_SHORT:
        case TokenTypes.LITERAL_STATIC:
        case TokenTypes.LITERAL_SUPER:
        case TokenTypes.LITERAL_SWITCH:
        case TokenTypes.LITERAL_SYNCHRONIZED:
        case TokenTypes.LITERAL_THIS:
        case TokenTypes.LITERAL_THROW:
        case TokenTypes.LITERAL_THROWS:
        case TokenTypes.LITERAL_TRANSIENT:
        case TokenTypes.LITERAL_TRUE:
        case TokenTypes.LITERAL_TRY:
        case TokenTypes.LITERAL_VOID:
        case TokenTypes.LITERAL_VOLATILE:
        case TokenTypes.LITERAL_WHILE:
        case TokenTypes.LNOT:
        case TokenTypes.LOR:
            break;
        case TokenTypes.LPAREN:
            if (parentType == TokenTypes.ANNOTATION) {
                whitespaceable = L_PAREN_ANNOTATION;
            } else
            if (nextSiblingType == TokenTypes.PARAMETERS) {
                whitespaceable = L_PAREN_PARAMETERS;
            } else
            if (parentType == TokenTypes.SUPER_CTOR_CALL || parentType == TokenTypes.LITERAL_NEW) {
                whitespaceable = L_PAREN_CALL;
            } else
            if (parentType == TokenTypes.LITERAL_DO) {
                whitespaceable = L_PAREN_DO_WHILE;
            }
            break;
        case TokenTypes.METHOD_CALL:
        case TokenTypes.METHOD_DEF:
        case TokenTypes.MINUS:
        case TokenTypes.MODIFIERS:
        case TokenTypes.NUM_DOUBLE:
        case TokenTypes.NUM_FLOAT:
        case TokenTypes.NUM_INT:
        case TokenTypes.NUM_LONG:
        case TokenTypes.OBJBLOCK:
        case TokenTypes.PACKAGE_DEF:
        case TokenTypes.PARAMETER_DEF:
        case TokenTypes.PARAMETERS:
        case TokenTypes.POST_DEC:
        case TokenTypes.POST_INC:
        case TokenTypes.QUESTION:
            break;
        case TokenTypes.RBRACK:
            whitespaceable = R_BRACK_ARRAY_DECL;
            break;

        case TokenTypes.RCURLY:
            
            if ( // 'new MyClass() {...}'
                parentType == TokenTypes.OBJBLOCK
                && grandparentType == TokenTypes.LITERAL_NEW
            ) {
                whitespaceable               = R_CURLY_ANON_CLASS;
                allowMissingWhitespaceBefore = this.allowEmptyAnonClass && previousSiblingType == TokenTypes.LCURLY;
                break;
            } else

            if ( // 'catch (Exception e) {...}'
                parentType == TokenTypes.SLIST
                && grandparentType == TokenTypes.LITERAL_CATCH
            ) {
                whitespaceable               = R_CURLY_CATCH_BLOCK;
                allowMissingWhitespaceBefore = this.allowEmptyCatchBlock && ast.getPreviousSibling() == null;
                break;
            } else

            if ( // 'class MyClass {...}', 'interface MyInterface {...}', '@interface MyAnnotation {...}'
                parentType == TokenTypes.OBJBLOCK
                && (
                    grandparentType == TokenTypes.CLASS_DEF
                    || grandparentType == TokenTypes.INTERFACE_DEF
                    || grandparentType == TokenTypes.ANNOTATION_DEF
                )
            ) {
                whitespaceable               = R_CURLY_TYPE_DEF;
                allowMissingWhitespaceBefore = this.allowEmptyType && previousSiblingTypeIs(ast, TokenTypes.LCURLY);
                break;
            } else

            if ( // 'public MyClass(...) {...}', 'public method(...) {...}'
                parentType == TokenTypes.SLIST
                && (grandparentType == TokenTypes.CTOR_DEF || grandparentType == TokenTypes.METHOD_DEF)
            ) {
                whitespaceable               = R_CURLY_METHOD_DEF;
                allowMissingWhitespaceBefore = this.allowEmptyMethod && ast.getPreviousSibling() == null;
                break;
            } else
            
            if (parentType == TokenTypes.ARRAY_INIT) { // 'Object[] oa = {...}', 'new Object[] {...}'
                whitespaceable               = R_CURLY_ARRAY_INIT;
                allowMissingWhitespaceBefore = this.allowEmptyInitializer && ast.getPreviousSibling() == null;
                break;
            }
            
            if (parentType == TokenTypes.SLIST && grandparentType == TokenTypes.STATIC_INIT) { // 'static { ... }'
                whitespaceable = R_CURLY_STATIC_INIT;
                break;
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
            if (parentType == TokenTypes.CTOR_DEF || parentType == TokenTypes.METHOD_DEF) {
                whitespaceable = R_PAREN_PARAMETERS;
            } else
            if (parentType == TokenTypes.SUPER_CTOR_CALL || parentType == TokenTypes.LITERAL_NEW) {
                whitespaceable = R_PAREN_CALL;
            } else
            if (parentType == TokenTypes.LITERAL_DO) {
                whitespaceable = R_PAREN_DO_WHILE;
            }
            break;
        case TokenTypes.SEMI:
            if (parentType == TokenTypes.PACKAGE_DEF) {
                whitespaceable = SEMI_PACKAGE_DEF;
            } else
            if (parentType == TokenTypes.SLIST || parentType == TokenTypes.SUPER_CTOR_CALL || parentType == TokenTypes.LITERAL_DO) {
                whitespaceable = SEMI_STATEMENT;
            } else
            if (parentType == TokenTypes.METHOD_DEF) {
                whitespaceable = SEMI_ABSTRACT_METH_DEF;
            } else
            if (previousSiblingType == TokenTypes.FOR_INIT) {
                whitespaceable = SEMI_FOR_INIT;
            } else
            if (previousSiblingType == TokenTypes.FOR_CONDITION) {
                whitespaceable = SEMI_FOR_CONDITION;
            }
            break;
        case TokenTypes.SL:
            break;
        case TokenTypes.SLIST:
            if (parentType == TokenTypes.STATIC_INIT) {
                whitespaceable = L_CURLY_STATIC_INIT;
            } else
            if (parentType == TokenTypes.CTOR_DEF) {
                whitespaceable = Whitespaceable.L_CURLY_METHOD_DEF;
                allowMissingWhitespaceAfter = this.allowEmptyMethod && firstChildType == TokenTypes.RCURLY;
            }
            break;
        case TokenTypes.SR:
            break;

        case TokenTypes.STAR:
            if (parentType == TokenTypes.DOT) { // 'import pkg.pkg.*;'
                break;
            } else {
                break;
            }

        case TokenTypes.STATIC_IMPORT:
        case TokenTypes.STATIC_INIT:
        case TokenTypes.STRICTFP:
        case TokenTypes.STRING_LITERAL:
        case TokenTypes.SUPER_CTOR_CALL:
        case TokenTypes.TYPE:
        case TokenTypes.TYPE_ARGUMENT:
        case TokenTypes.TYPE_ARGUMENTS:
        case TokenTypes.TYPE_EXTENSION_AND:
        case TokenTypes.TYPE_LOWER_BOUNDS:
        case TokenTypes.TYPE_PARAMETER:
        case TokenTypes.TYPE_PARAMETERS:
        case TokenTypes.TYPE_UPPER_BOUNDS:
        case TokenTypes.TYPECAST:
        case TokenTypes.UNARY_MINUS:
        case TokenTypes.UNARY_PLUS:
            break;
        case TokenTypes.VARIABLE_DEF:
            break;
        case TokenTypes.WILDCARD_TYPE:
            break;

        default:
            assert false;
        }

        if (whitespaceable == null) {
            return;
        }

        boolean mustBeWhitespaceBefore    = !allowMissingWhitespaceBefore && this.whitespaceBefore.contains(whitespaceable);
        boolean mustNotBeWhitespaceBefore = this.noWhitespaceBefore.contains(whitespaceable);
        boolean mustBeWhitespaceAfter     = !allowMissingWhitespaceAfter && this.whitespaceAfter.contains(whitespaceable);
        boolean mustNotBeWhitespaceAfter  = this.noWhitespaceAfter.contains(whitespaceable);

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
    private static final Pattern LINE_PREFIX = Pattern.compile("\\s*(?:/\\*(?:.(?!\\*/))*\\*/\\s*)*");
    private static final Pattern LINE_SUFFIX = Pattern.compile("\\s*(?:/\\*(?:.(?!\\*/))*\\*/\\s*)*(?://.*)?");

    /** Whether or not empty constructor bodies are allowed. */
    private boolean mAllowEmptyCtors;
    /** Whether or not empty method bodies are allowed. */
    private boolean mAllowEmptyMethods;
    /** whether or not to ignore a colon in a enhanced for loop */
    private boolean mIgnoreEnhancedForColon = true;

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

    /**
     * Sets whether or not empty method bodies are allowed.
     * @param aAllow <code>true</code> to allow empty method bodies.
     */
    public void
    setAllowEmptyMethods(boolean aAllow) {
        this.mAllowEmptyMethods = aAllow;
    }

    /**
     * Sets whether or not empty constructor bodies are allowed.
     * @param aAllow <code>true</code> to allow empty constructor bodies.
     */
    public void
    setAllowEmptyConstructors(boolean aAllow) {
        this.mAllowEmptyCtors = aAllow;
    }

    /**
     * Sets whether or not to ignore the whitespace around the
     * colon in an enhanced for loop.
     * @param aIgnore <code>true</code> to ignore enhanced for colon.
     */
    public void
    setIgnoreEnhancedForColon(boolean aIgnore) {
        this.mIgnoreEnhancedForColon = aIgnore;
    }

//    /**
//     * Test if the given <code>DetailAST</code> is part of an allowed empty
//     * method block.
//     * @param ast the <code>DetailAST</code> to test.
//     * @param aParentType the token type of <code>ast</code>'s parent.
//     * @return <code>true</code> if <code>ast</code> makes up part of an
//     *         allowed empty method block.
//     */
//    private boolean
//    emptyMethodBlockCheck(DetailAST ast, int aParentType) {
//        return this.mAllowEmptyMethods && emptyBlockCheck(ast, aParentType, TokenTypes.METHOD_DEF);
//    }
//
//    /**
//     * Test if the given <code>DetailAST</code> is part of an allowed empty
//     * constructor (ctor) block.
//     * @param ast the <code>DetailAST</code> to test.
//     * @param aParentType the token type of <code>ast</code>'s parent.
//     * @return <code>true</code> if <code>ast</code> makes up part of an
//     *         allowed empty constructor block.
//     */
//    private boolean
//    emptyCtorBlockCheck(DetailAST ast, int aParentType) {
//        return this.mAllowEmptyCtors && emptyBlockCheck(ast, aParentType, TokenTypes.CTOR_DEF);
//    }
//
//    /**
//     * Test if the given <code>DetailAST</code> is part of an empty block.
//     * An example empty block might look like the following
//     * <p>
//     * <pre>   public void myMethod(int val) {}</pre>
//     * <p>
//     * In the above, the method body is an empty block ("{}").
//     *
//     * @param ast the <code>DetailAST</code> to test.
//     * @param aParentType the token type of <code>ast</code>'s parent.
//     * @param aMatch the parent token type we're looking to match.
//     * @return <code>true</code> if <code>ast</code> makes up part of an
//     *         empty block contained under a <code>aMatch</code> token type
//     *         node.
//     */
//    private boolean
//    emptyBlockCheck(DetailAST ast, int aParentType, int aMatch) {
//        final int type = ast.getType();
//        if (type == TokenTypes.RCURLY) {
//            final DetailAST grandParent = ast.getParent().getParent();
//            return (aParentType == TokenTypes.SLIST)
//                && (grandParent.getType() == aMatch);
//        }
//
//        return (type == TokenTypes.SLIST)
//            && (aParentType == aMatch)
//            && (ast.getFirstChild().getType() == TokenTypes.RCURLY);
//    }
}

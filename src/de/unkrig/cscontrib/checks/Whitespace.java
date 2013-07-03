
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

import static de.unkrig.cscontrib.util.AstUtil.nextSiblingTypeIs;
import static de.unkrig.cscontrib.util.AstUtil.previousSiblingTypeIs;

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
     * Whether a token must or must not be followed and/or preceeded by whitespace.
     */
    public
    enum WhitespaceOption {
        /** The token must neither be preceeded nor followed by whitespace. */
        NOT_BEFORE_AND_NOT_AFTER,
        /** The token must not be preceeded, but followed by whitespace. */
        NOT_BEFORE_BUT_AFTER,
        /** The token must not be preceededby whitespace. */
        NOT_BEFORE,
        /** The token must be preceeded, but not followed by whitespace. */
        BEFORE_BUT_NOT_AFTER,
        /** The token must be preceeded and followed by whitespace. */
        BEFORE_AND_AFTER,
        /** The token must be preceeded by whitespace. */
        BEFORE,
        /** The token must not be followed by whitespace. */
        NOT_AFTER,
        /** The token must be followed by whitespace. */
        AFTER,
        /** The token may or may not be preceeded and/or followed by whitespace. */
        ANY,
    }

    private enum WhitespaceOption2 { MUST, MAY, MUST_NOT }

    private WhitespaceOption arithmeticOperators = WhitespaceOption.BEFORE_AND_AFTER;
    private WhitespaceOption assignments         = WhitespaceOption.BEFORE_AND_AFTER;
    private WhitespaceOption at                  = WhitespaceOption.BEFORE_BUT_NOT_AFTER;
    private WhitespaceOption bitwiseOperators    = WhitespaceOption.BEFORE_AND_AFTER;
    private WhitespaceOption bitwiseComplement   = WhitespaceOption.NOT_AFTER;
    private WhitespaceOption colonDefault        = WhitespaceOption.NOT_BEFORE_BUT_AFTER;
    private WhitespaceOption colonCase           = WhitespaceOption.NOT_BEFORE_BUT_AFTER;
    private WhitespaceOption colonEnhancedFor    = WhitespaceOption.BEFORE_AND_AFTER;
    private WhitespaceOption colonTernary        = WhitespaceOption.BEFORE_AND_AFTER;
    private WhitespaceOption comma               = WhitespaceOption.NOT_BEFORE_BUT_AFTER;
    private WhitespaceOption doWhile             = WhitespaceOption.BEFORE_AND_AFTER;
    private WhitespaceOption dot                 = WhitespaceOption.NOT_BEFORE_AND_NOT_AFTER;
    private WhitespaceOption emptyStat           = WhitespaceOption.BEFORE_AND_AFTER;
    private WhitespaceOption equalities          = WhitespaceOption.BEFORE_AND_AFTER;
    private WhitespaceOption genericEnd          = WhitespaceOption.NOT_BEFORE;
    private WhitespaceOption genericStart        = WhitespaceOption.NOT_AFTER;
    private WhitespaceOption lCurlyAnonClass     = WhitespaceOption.BEFORE_AND_AFTER;
    private WhitespaceOption lCurlyArrayInit     = WhitespaceOption.BEFORE_AND_AFTER;
    private WhitespaceOption preDec              = WhitespaceOption.NOT_AFTER;

    private boolean allowEmptyAnonClass = true;
    private boolean allowEmptyArrayInit = true;

    // BEGIN CONFIGURATION SETTERS
    // CHECKSTYLE JavadocMethod:OFF
    // CHECKSTYLE LineLength:OFF
    public void setArithmeticOperators(String value) { this.arithmeticOperators = toEnum(value, WhitespaceOption.class); }
    public void setAssignments(String value)         { this.assignments         = toEnum(value, WhitespaceOption.class); }
    public void setAt(String value)                  { this.at                  = toEnum(value, WhitespaceOption.class); }
    public void setBitwiseOperators(String value)    { this.bitwiseOperators    = toEnum(value, WhitespaceOption.class); }
    public void setBitwiseComplement(String value)   { this.bitwiseComplement   = toEnum(value, WhitespaceOption.class); }
    public void setColonDefault(String value)        { this.colonDefault        = toEnum(value, WhitespaceOption.class); }
    public void setColonCase(String value)           { this.colonCase           = toEnum(value, WhitespaceOption.class); }
    public void setColonEnhancedFor(String value)    { this.colonEnhancedFor    = toEnum(value, WhitespaceOption.class); }
    public void setColonTernary(String value)        { this.colonTernary        = toEnum(value, WhitespaceOption.class); }
    public void setComma(String value)               { this.comma               = toEnum(value, WhitespaceOption.class); }
    public void setDoWhile(String value)             { this.doWhile             = toEnum(value, WhitespaceOption.class); }
    public void setDot(String value)                 { this.dot                 = toEnum(value, WhitespaceOption.class); }
    public void setEmptyStat(String value)           { this.emptyStat           = toEnum(value, WhitespaceOption.class); }
    public void setEqualities(String value)          { this.equalities          = toEnum(value, WhitespaceOption.class); }
    public void setGenericEnd(String value)          { this.genericEnd          = toEnum(value, WhitespaceOption.class); }
    public void setGenericStart(String value)        { this.genericStart        = toEnum(value, WhitespaceOption.class); }
    public void setLCurlyAnonClass(String value)     { this.lCurlyAnonClass     = toEnum(value, WhitespaceOption.class); }
    public void setLCurlyArrayInit(String value)     { this.lCurlyArrayInit     = toEnum(value, WhitespaceOption.class); }
    public void setPreDec(String value)              { this.preDec              = toEnum(value, WhitespaceOption.class); }

    public void setAllowEmptyAnonClass(boolean value) { this.allowEmptyAnonClass = value; }
    public void setAllowEmptyArrayInit(boolean value) { this.allowEmptyArrayInit = value; }
    // CHECKSTYLE LineLength:ON
    // CHECKSTYLE JavadocMethod:ON
    // END CONFIGURATION SETTERS

    private <E extends Enum<E>> E
    toEnum(String value, Class<E> enumClass) {
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new ConversionException("Unable to parse " + value, iae);
        }
    }

    @Override public void
    visitToken(DetailAST ast) {

        final int type, parentType, grandparentType;
        {
            type = ast.getType();
            DetailAST parent = ast.getParent();
            if (parent == null) {
                parentType      = -1;
                grandparentType = -1;
            } else {
                parentType = parent.getType();
                DetailAST grandparent = parent.getParent();
                grandparentType = grandparent == null ? -1 : grandparent.getType();
            }
        }

        WhitespaceOption whitespaceOption = null;
        switch (ast.getType()) {
        case TokenTypes.ABSTRACT:
        case TokenTypes.ANNOTATION:
        case TokenTypes.ANNOTATION_ARRAY_INIT:
        case TokenTypes.ANNOTATION_DEF:
        case TokenTypes.ANNOTATION_FIELD_DEF:
        case TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR:
        case TokenTypes.ANNOTATIONS:
        case TokenTypes.ARRAY_DECLARATOR:
        case TokenTypes.ARRAY_INIT:
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
            whitespaceOption = this.assignments;
            break;
        case TokenTypes.AT:
            whitespaceOption = this.at;
            break;
        case TokenTypes.BAND:
        case TokenTypes.BOR:
        case TokenTypes.BSR:
        case TokenTypes.BXOR:
            whitespaceOption = this.bitwiseOperators;
            break;
        case TokenTypes.BNOT:
            whitespaceOption = this.bitwiseComplement;
            break;
        case TokenTypes.CASE_GROUP:
        case TokenTypes.CHAR_LITERAL:
        case TokenTypes.CLASS_DEF:
            break;
        case TokenTypes.COLON:
            if (parentType == TokenTypes.LITERAL_DEFAULT) { // 'default:'
                whitespaceOption = this.colonDefault;
            } else
            if (parentType == TokenTypes.LITERAL_CASE) {    // 'case 77:'
                whitespaceOption = this.colonCase;
            } else
            if (parentType == TokenTypes.FOR_EACH_CLAUSE) { // 'for (Object o : list) {'
                whitespaceOption = this.colonEnhancedFor;
            } else
            {                                               // 'a ? b : c'
                whitespaceOption = this.colonTernary;
            }
            break;
        case TokenTypes.COMMA:
            whitespaceOption = this.comma;
            break;
        case TokenTypes.CTOR_CALL:
        case TokenTypes.CTOR_DEF:
            break;
        case TokenTypes.DEC:
            whitespaceOption = this.preDec; // '--x'
            break;
        case TokenTypes.DIV:  // 'a / b'
        case TokenTypes.PLUS: // 'a + b'
        case TokenTypes.MOD:  // 'a % b'
            whitespaceOption = this.arithmeticOperators;
            break;
        case TokenTypes.DO_WHILE: // '... } while (...);'
            whitespaceOption = this.doWhile;
            break;
        case TokenTypes.DOT: // 'a.b'
            whitespaceOption = this.dot;
            break;
        case TokenTypes.ELIST:
            break;
        case TokenTypes.ELLIPSIS: // 'meth(int x, ...) {'
            break;
        case TokenTypes.EMPTY_STAT: // ';'
            whitespaceOption = this.emptyStat;
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
            whitespaceOption = this.equalities;
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
            whitespaceOption = this.genericEnd;
            break;
        case TokenTypes.GENERIC_START:
            whitespaceOption = this.genericStart;
            break;
        case TokenTypes.IDENT:
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
            {
                boolean allowEmpty;
                if (parentType == TokenTypes.OBJBLOCK && (
                    grandparentType == TokenTypes.CLASS_DEF         // 'class MyClass() {...}'
                    || grandparentType == TokenTypes.INTERFACE_DEF  // 'interface MyInterface() {...}'
                    || grandparentType == TokenTypes.LITERAL_NEW    // 'new MyClass() {...}'
                    || grandparentType == TokenTypes.ANNOTATION_DEF // 'new @MyAnnotation {...}'
                )) {
                    whitespaceOption = this.lCurlyAnonClass;
                    allowEmpty       = this.allowEmptyAnonClass;
                } else
                if (parentType == TokenTypes.ARRAY_INIT) { // 'int[] ia = {...}', 'new int[] {...}'
                    whitespaceOption = this.lCurlyArrayInit;
                    allowEmpty       = this.allowEmptyArrayInit;
                } else
                {
                    break;
                }

                if (allowEmpty && nextSiblingTypeIs(ast, TokenTypes.RCURLY)) {
                    switch (whitespaceOption) {
                    case NOT_BEFORE_BUT_AFTER: whitespaceOption = WhitespaceOption.NOT_BEFORE; break;
                    case BEFORE_AND_AFTER:     whitespaceOption = WhitespaceOption.BEFORE;     break;
                    case AFTER:                whitespaceOption = WhitespaceOption.ANY;        break;
                    default: break;
                    }
                }
                break;
            }

        case TokenTypes.LITERAL_ASSERT:
            break;
//        case TokenTypes.LITERAL_BOOLEAN:
//        case TokenTypes.LITERAL_BREAK:
//        case TokenTypes.LITERAL_BYTE:
//        case TokenTypes.LITERAL_CASE:
        case TokenTypes.LITERAL_CATCH:
            break;
//        case TokenTypes.LITERAL_CHAR:
//        case TokenTypes.LITERAL_CLASS:
//        case TokenTypes.LITERAL_CONTINUE:
//        case TokenTypes.LITERAL_DEFAULT:
        case TokenTypes.LITERAL_DO:
            break;
//        case TokenTypes.LITERAL_DOUBLE:
        case TokenTypes.LITERAL_ELSE:
            break;
//        case TokenTypes.LITERAL_FALSE:
        case TokenTypes.LITERAL_FINALLY:
            break;
//        case TokenTypes.LITERAL_FLOAT:
        case TokenTypes.LITERAL_FOR:
            break;
        case TokenTypes.LITERAL_IF:
            break;
//        case TokenTypes.LITERAL_INSTANCEOF:
//        case TokenTypes.LITERAL_INT:
//        case TokenTypes.LITERAL_INTERFACE:
//        case TokenTypes.LITERAL_LONG:
//        case TokenTypes.LITERAL_NATIVE:
//        case TokenTypes.LITERAL_NEW:
//        case TokenTypes.LITERAL_NULL:
//        case TokenTypes.LITERAL_PRIVATE:
//        case TokenTypes.LITERAL_PROTECTED:
//        case TokenTypes.LITERAL_PUBLIC:
        case TokenTypes.LITERAL_RETURN:
            if (ast.getFirstChild().getType() == TokenTypes.SEMI) { // 'return;'
                break;
            } else { // 'return x;'
                break;
            }

//        case TokenTypes.LITERAL_SHORT:
//        case TokenTypes.LITERAL_STATIC:
//        case TokenTypes.LITERAL_SUPER:
//        case TokenTypes.LITERAL_SWITCH:
        case TokenTypes.LITERAL_SYNCHRONIZED:
            break;
//        case TokenTypes.LITERAL_THIS:
//        case TokenTypes.LITERAL_THROW:
//        case TokenTypes.LITERAL_THROWS:
//        case TokenTypes.LITERAL_TRANSIENT:
//        case TokenTypes.LITERAL_TRUE:
        case TokenTypes.LITERAL_TRY:
            break;
//        case TokenTypes.LITERAL_VOID:
//        case TokenTypes.LITERAL_VOLATILE:
        case TokenTypes.LITERAL_WHILE:
            break;
//        case TokenTypes.LNOT:
        case TokenTypes.LOR:
            break;
//        case TokenTypes.LPAREN:
//        case TokenTypes.METHOD_CALL:
//        case TokenTypes.METHOD_DEF:
        case TokenTypes.MINUS:
            break;
//        case TokenTypes.MODIFIERS:
//        case TokenTypes.NUM_DOUBLE:
//        case TokenTypes.NUM_FLOAT:
//        case TokenTypes.NUM_INT:
//        case TokenTypes.NUM_LONG:
//        case TokenTypes.OBJBLOCK:
//        case TokenTypes.PACKAGE_DEF:
//        case TokenTypes.PARAMETER_DEF:
//        case TokenTypes.PARAMETERS:
//        case TokenTypes.POST_DEC:
//        case TokenTypes.POST_INC:
        case TokenTypes.QUESTION:
            break;
//        case TokenTypes.RBRACK:
        case TokenTypes.RCURLY:
            
            if ( // 'new MyClass() {...}'
                type == TokenTypes.OBJBLOCK
                && grandparentType == TokenTypes.LITERAL_NEW
            ) {
                break;
            } else

            if ( // 'catch (Exception e) {...}'
                type == TokenTypes.SLIST
                && grandparentType == TokenTypes.LITERAL_CATCH
            ) {
                break;
            } else

            if ( // 'class MyClass {...}', 'interface MyInterface {...}', '@interface MyAnnotation {...}'
                type == TokenTypes.OBJBLOCK
                && (
                    grandparentType == TokenTypes.CLASS_DEF
                    || grandparentType == TokenTypes.INTERFACE_DEF
                    || grandparentType == TokenTypes.ANNOTATION_DEF
                )
            ) {
                if (previousSiblingTypeIs(ast, TokenTypes.LCURLY)) break;
                break;
            } else

            if ( // 'public MyClass(...) {...}', 'public method(...) {...}'
                type == TokenTypes.SLIST
                && (grandparentType == TokenTypes.CTOR_DEF || grandparentType == TokenTypes.METHOD_DEF)
            ) {
                if (ast.getPreviousSibling() == null) { // Empty method body
                    break;
                }
                break;
            } else
            
            if (type == TokenTypes.ARRAY_INIT) { // 'Object[] oa = {...}', 'new Object[] {...}'
                if (ast.getPreviousSibling() == null) { // Empty initializer
                    break;
                }
                break;
            } else
                
            {
                break;
            }
//        case TokenTypes.RESOURCE:
//        case TokenTypes.RESOURCE_SPECIFICATION:
//        case TokenTypes.RESOURCES:
//        case TokenTypes.RPAREN:
//        case TokenTypes.SEMI:
        case TokenTypes.SL:
            break;
        case TokenTypes.SLIST:
            break;
        case TokenTypes.SR:
            break;
        case TokenTypes.STAR:
            if (type == TokenTypes.DOT) { // 'import pkg.pkg.*;'
                break;
            } else {
                break;
            }
//        case TokenTypes.STATIC_IMPORT:
//        case TokenTypes.STATIC_INIT:
//        case TokenTypes.STRICTFP:
//        case TokenTypes.STRING_LITERAL:
//        case TokenTypes.SUPER_CTOR_CALL:
//        case TokenTypes.TYPE:
//        case TokenTypes.TYPE_ARGUMENT:
//        case TokenTypes.TYPE_ARGUMENTS:
        case TokenTypes.TYPE_EXTENSION_AND:
            break;
//        case TokenTypes.TYPE_LOWER_BOUNDS:
//        case TokenTypes.TYPE_PARAMETER:
//        case TokenTypes.TYPE_PARAMETERS:
//        case TokenTypes.TYPE_UPPER_BOUNDS:
//        case TokenTypes.TYPECAST:
//        case TokenTypes.UNARY_MINUS:
//        case TokenTypes.UNARY_PLUS:
//        case TokenTypes.VARIABLE_DEF:
//        case TokenTypes.WILDCARD_TYPE:

        default:
            assert false;
        }

        if (whitespaceOption == null) return;

        // Short-circuit.
        if (whitespaceOption == WhitespaceOption.ANY) return; 

        WhitespaceOption2 before, after;
        switch (whitespaceOption) {
        case NOT_BEFORE_AND_NOT_AFTER: before = WhitespaceOption2.MUST_NOT; after = WhitespaceOption2.MUST_NOT; break;
        case NOT_BEFORE_BUT_AFTER:     before = WhitespaceOption2.MUST_NOT; after = WhitespaceOption2.MUST;     break;
        case NOT_BEFORE:               before = WhitespaceOption2.MUST_NOT; after = WhitespaceOption2.MAY;      break;
        case BEFORE_BUT_NOT_AFTER:     before = WhitespaceOption2.MUST;     after = WhitespaceOption2.MUST_NOT; break;
        case BEFORE_AND_AFTER:         before = WhitespaceOption2.MUST;     after = WhitespaceOption2.MUST;     break;
        case BEFORE:                   before = WhitespaceOption2.MUST;     after = WhitespaceOption2.MAY;      break;
        case NOT_AFTER:                before = WhitespaceOption2.MAY;      after = WhitespaceOption2.MUST_NOT; break;
        case AFTER:                    before = WhitespaceOption2.MAY;      after = WhitespaceOption2.MUST;     break;
        case ANY:                      before = WhitespaceOption2.MAY;      after = WhitespaceOption2.MAY;      break;
        default: throw new AssertionError();
        }
        
        final String[] lines = getLines();
        final String   line  = lines[ast.getLineNo() - 1];

        if (before != WhitespaceOption2.MAY) {
            int before2 = ast.getColumnNo() - 1;

            if (
                before2 >= 0
                && (Character.isWhitespace(line.charAt(before2)) ^ before == WhitespaceOption2.MUST)
            ) log(ast.getLineNo(), ast.getColumnNo(), "ws.notPreceded", ast.getText());
        }
        if (after != WhitespaceOption2.MAY) {
            final int after2 = ast.getColumnNo() + ast.getText().length();

            if (
                after2 < line.length()
                && (Character.isWhitespace(line.charAt(after2)) ^ after == WhitespaceOption2.MUST)
            ) log(ast.getLineNo(), ast.getColumnNo() + ast.getText().length(), "ws.notFollowed", ast.getText());
        }
    }

    /** Whether or not empty constructor bodies are allowed. */
    private boolean mAllowEmptyCtors;
    /** Whether or not empty method bodies are allowed. */
    private boolean mAllowEmptyMethods;
    /** whether or not to ignore a colon in a enhanced for loop */
    private boolean mIgnoreEnhancedForColon = true;

    @Override public int[]
    getDefaultTokens() {
        return new int[] {
//            TokenTypes.ABSTRACT,
//            TokenTypes.ANNOTATION,
//            TokenTypes.ANNOTATION_ARRAY_INIT,
//            TokenTypes.ANNOTATION_DEF,
//            TokenTypes.ANNOTATION_FIELD_DEF,
//            TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR,
//            TokenTypes.ANNOTATIONS,
//            TokenTypes.ARRAY_DECLARATOR,
//            TokenTypes.ARRAY_INIT,
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
//            TokenTypes.CASE_GROUP,
//            TokenTypes.CHAR_LITERAL,
//            TokenTypes.CLASS_DEF,
            TokenTypes.COLON,
            TokenTypes.COMMA,
//            TokenTypes.CTOR_CALL,
//            TokenTypes.CTOR_DEF,
//            TokenTypes.DEC,
            TokenTypes.DIV,
            TokenTypes.DIV_ASSIGN,
//            TokenTypes.DO_WHILE,
//            TokenTypes.DOT,
//            TokenTypes.ELIST,
//            TokenTypes.ELLIPSIS,
//            TokenTypes.EMPTY_STAT,
//            TokenTypes.ENUM,
//            TokenTypes.ENUM_CONSTANT_DEF,
//            TokenTypes.ENUM_DEF,
//            TokenTypes.EOF,
            TokenTypes.EQUAL,
//            TokenTypes.EXPR,
//            TokenTypes.EXTENDS_CLAUSE,
//            TokenTypes.FINAL,
//            TokenTypes.FOR_CONDITION,
//            TokenTypes.FOR_EACH_CLAUSE,
//            TokenTypes.FOR_INIT,
//            TokenTypes.FOR_ITERATOR,
            TokenTypes.GE,
//            TokenTypes.GENERIC_END,
//            TokenTypes.GENERIC_START,
            TokenTypes.GT,
//            TokenTypes.IDENT,
//            TokenTypes.IMPLEMENTS_CLAUSE,
//            TokenTypes.IMPORT,
//            TokenTypes.INC,
//            TokenTypes.INDEX_OP,
//            TokenTypes.INSTANCE_INIT,
//            TokenTypes.INTERFACE_DEF,
//            TokenTypes.LABELED_STAT,
            TokenTypes.LAND,
            TokenTypes.LCURLY,
            TokenTypes.LE,
            TokenTypes.LITERAL_ASSERT,
//            TokenTypes.LITERAL_BOOLEAN,
//            TokenTypes.LITERAL_BREAK,
//            TokenTypes.LITERAL_BYTE,
//            TokenTypes.LITERAL_CASE,
            TokenTypes.LITERAL_CATCH,
//            TokenTypes.LITERAL_CHAR,
//            TokenTypes.LITERAL_CLASS,
//            TokenTypes.LITERAL_CONTINUE,
//            TokenTypes.LITERAL_DEFAULT,
            TokenTypes.LITERAL_DO,
//            TokenTypes.LITERAL_DOUBLE,
            TokenTypes.LITERAL_ELSE,
//            TokenTypes.LITERAL_FALSE,
            TokenTypes.LITERAL_FINALLY,
//            TokenTypes.LITERAL_FLOAT,
            TokenTypes.LITERAL_FOR,
            TokenTypes.LITERAL_IF,
//            TokenTypes.LITERAL_INSTANCEOF,
//            TokenTypes.LITERAL_INT,
//            TokenTypes.LITERAL_INTERFACE,
//            TokenTypes.LITERAL_LONG,
//            TokenTypes.LITERAL_NATIVE,
//            TokenTypes.LITERAL_NEW,
//            TokenTypes.LITERAL_NULL,
//            TokenTypes.LITERAL_PRIVATE,
//            TokenTypes.LITERAL_PROTECTED,
//            TokenTypes.LITERAL_PUBLIC,
            TokenTypes.LITERAL_RETURN,
//            TokenTypes.LITERAL_SHORT,
//            TokenTypes.LITERAL_STATIC,
//            TokenTypes.LITERAL_SUPER,
//            TokenTypes.LITERAL_SWITCH,
            TokenTypes.LITERAL_SYNCHRONIZED,
//            TokenTypes.LITERAL_THIS,
//            TokenTypes.LITERAL_THROW,
//            TokenTypes.LITERAL_THROWS,
//            TokenTypes.LITERAL_TRANSIENT,
//            TokenTypes.LITERAL_TRUE,
            TokenTypes.LITERAL_TRY,
//            TokenTypes.LITERAL_VOID,
//            TokenTypes.LITERAL_VOLATILE,
            TokenTypes.LITERAL_WHILE,
//            TokenTypes.LNOT,
            TokenTypes.LOR,
//            TokenTypes.LPAREN,
            TokenTypes.LT,
//            TokenTypes.METHOD_CALL,
//            TokenTypes.METHOD_DEF,
            TokenTypes.MINUS,
            TokenTypes.MINUS_ASSIGN,
            TokenTypes.MOD,
            TokenTypes.MOD_ASSIGN,
//            TokenTypes.MODIFIERS,
            TokenTypes.NOT_EQUAL,
//            TokenTypes.NUM_DOUBLE,
//            TokenTypes.NUM_FLOAT,
//            TokenTypes.NUM_INT,
//            TokenTypes.NUM_LONG,
//            TokenTypes.OBJBLOCK,
//            TokenTypes.PACKAGE_DEF,
//            TokenTypes.PARAMETER_DEF,
//            TokenTypes.PARAMETERS,
            TokenTypes.PLUS,
            TokenTypes.PLUS_ASSIGN,
//            TokenTypes.POST_DEC,
//            TokenTypes.POST_INC,
            TokenTypes.QUESTION,
//            TokenTypes.RBRACK,
            TokenTypes.RCURLY,
//            TokenTypes.RESOURCE,
//            TokenTypes.RESOURCE_SPECIFICATION,
//            TokenTypes.RESOURCES,
//            TokenTypes.RPAREN,
//            TokenTypes.SEMI,
            TokenTypes.SL,
            TokenTypes.SL_ASSIGN,
            TokenTypes.SLIST,
            TokenTypes.SR,
            TokenTypes.SR_ASSIGN,
            TokenTypes.STAR,
            TokenTypes.STAR_ASSIGN,
//            TokenTypes.STATIC_IMPORT,
//            TokenTypes.STATIC_INIT,
//            TokenTypes.STRICTFP,
//            TokenTypes.STRING_LITERAL,
//            TokenTypes.SUPER_CTOR_CALL,
//            TokenTypes.TYPE,
//            TokenTypes.TYPE_ARGUMENT,
//            TokenTypes.TYPE_ARGUMENTS,
            TokenTypes.TYPE_EXTENSION_AND,
//            TokenTypes.TYPE_LOWER_BOUNDS,
//            TokenTypes.TYPE_PARAMETER,
//            TokenTypes.TYPE_PARAMETERS,
//            TokenTypes.TYPE_UPPER_BOUNDS,
//            TokenTypes.TYPECAST,
//            TokenTypes.UNARY_MINUS,
//            TokenTypes.UNARY_PLUS,
//            TokenTypes.VARIABLE_DEF,
//            TokenTypes.WILDCARD_TYPE,
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


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

import static de.unkrig.cscontrib.util.JavaElement.*;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import de.unkrig.commons.nullanalysis.Nullable;

/**
 * Utility methods related to CHECKSTYLE's DetailAST model.
 */
public final
class AstUtil {

    private
    AstUtil() {}

    /**
     * @return Whether the {@code ast}'s grandparent's type is one of {@code types}
     */
    public static boolean
    grandparentTypeIs(DetailAST ast, int... types) {
        int grandparentType = ast.getParent().getParent().getType();
        for (int type : types) {
            if (grandparentType == type) return true;
        }
        return false;
    }

    /**
     * @return Whether the {@code ast}'s parent's type is {@code type}
     */
    public static boolean
    parentTypeIs(DetailAST ast, int type) {
        DetailAST parent = ast.getParent();

        return parent.getType() == type;
    }

    /**
     * @return Whether the {@code ast}'s next sibling's type is {@code type}
     */
    public static boolean
    nextSiblingTypeIs(DetailAST ast, int type) {
        DetailAST nextSibling = ast.getNextSibling();

        return nextSibling != null && nextSibling.getType() == type;
    }

    /**
     * @return Whether the {@code ast}'s first child's type is {@code type}
     */
    public static boolean
    firstChildTypeIs(DetailAST ast, int type) {
        DetailAST firstChild = ast.getFirstChild();

        return firstChild != null && firstChild.getType() == type;
    }

    /**
     * @return Whether the {@code ast}'s previous sibling's type is {@code type}
     */
    public static boolean
    previousSiblingTypeIs(DetailAST ast, int type) {
        DetailAST previousSibling = ast.getPreviousSibling();

        return previousSibling != null && previousSibling.getType() == type;
    }

    /**
     * Converts the given {@link DetailAST} into a {@link JavaElement}. In some cases, this is a one-to-one mapping,
     * but in others one {@link DetailAST} can repsresent one of <i>several</i> {@link JavaElement}s. E.g. the
     * colon can appear in a SWITCH-CASE, a SWITCH-DEFAULT, in an enhanced FOR statement and in a ternary
     * expression ({@code a ? b : c}).
     */
    @Nullable public static JavaElement
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
                    getAncestorWithTypeNot(ast, TokenTypes.TYPE_ARGUMENTS, TokenTypes.DOT) == TokenTypes.TYPE
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
                    getAncestorWithTypeNot(ast, TokenTypes.TYPE_ARGUMENTS, TokenTypes.DOT) == TokenTypes.TYPE
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
}


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

import de.unkrig.commons.nullanalysis.Nullable;
import de.unkrig.cscontrib.LocalTokenType;

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
    grandParentTypeIs(DetailAST ast, LocalTokenType... types) {
        LocalTokenType grandparentType = LocalTokenType.localize(ast.getParent().getParent().getType());
        for (LocalTokenType type : types) {
            if (grandparentType == type) return true;
        }
        return false;
    }

    /**
     * @return Whether the {@code ast}'s parent's type is {@code type}
     */
    public static boolean
    parentTypeIs(DetailAST ast, LocalTokenType type) {
        DetailAST parent = ast.getParent();

        return LocalTokenType.localize(parent.getType()) == type;
    }

    /**
     * @return Whether the {@code ast}'s next sibling's type is {@code type}
     */
    public static boolean
    nextSiblingTypeIs(DetailAST ast, LocalTokenType type) {
        DetailAST nextSibling = ast.getNextSibling();

        return nextSibling != null && LocalTokenType.localize(nextSibling.getType()) == type;
    }

    /**
     * @return Whether the {@code ast}'s first child's type is {@code type}
     */
    public static boolean
    firstChildTypeIs(DetailAST ast, LocalTokenType type) {
        DetailAST firstChild = ast.getFirstChild();

        return firstChild != null && LocalTokenType.localize(firstChild.getType()) == type;
    }

    /**
     * @return Whether the {@code ast}'s previous sibling's type is {@code type}
     */
    public static boolean
    previousSiblingTypeIs(DetailAST ast, LocalTokenType type) {
        DetailAST previousSibling = ast.getPreviousSibling();

        return previousSibling != null && LocalTokenType.localize(previousSibling.getType()) == type;
    }

    /**
     * Converts the given {@link DetailAST} into a {@link JavaElement}. In some cases, this is a one-to-one mapping,
     * but in others one {@link DetailAST} can repsresent one of <i>several</i> {@link JavaElement}s. E.g. the
     * colon can appear in a SWITCH-CASE, a SWITCH-DEFAULT, in an enhanced FOR statement and in a ternary
     * expression ({@code a ? b : c}).
     */
    @Nullable public static JavaElement
    toJavaElement(final DetailAST ast) {

        final LocalTokenType type, parentType, grandParentType, previousSiblingType, nextSiblingType, firstChildType;
        {
            type = LocalTokenType.localize(ast.getType());
            DetailAST parent = ast.getParent();
            if (parent == null) {
                parentType      = null;
                grandParentType = null;
            } else {
                parentType = LocalTokenType.localize(parent.getType());
                DetailAST grandparent = parent.getParent();
                grandParentType = grandparent == null ? null : LocalTokenType.localize(grandparent.getType());
            }

            DetailAST previousSibling = ast.getPreviousSibling();
            previousSiblingType = previousSibling == null ? null : LocalTokenType.localize(previousSibling.getType());

            DetailAST nextSibling = ast.getNextSibling();
            nextSiblingType = nextSibling == null ? null : LocalTokenType.localize(nextSibling.getType());

            DetailAST firstChild = ast.getFirstChild();
            firstChildType = firstChild == null ? null : LocalTokenType.localize(firstChild.getType());
        }

        // Find out how this token is to be checked.
        switch (type) {

        // Tokens that appear in only one context, and thus map one-to-one to a Java element.
        case ABSTRACT:           return ABSTRACT;
        case ARRAY_DECLARATOR:   return L_BRACK__ARRAY_DECL;
        case BAND:               return AND__EXPR;
        case BAND_ASSIGN:        return AND_ASSIGN;
        case BNOT:               return BITWISE_COMPLEMENT;
        case BOR:                return OR;
        case BOR_ASSIGN:         return OR_ASSIGN;
        case BSR:                return UNSIGNED_RIGHT_SHIFT;
        case BSR_ASSIGN:         return UNSIGNED_RIGHT_SHIFT_ASSIGN;
        case BXOR:               return XOR;
        case BXOR_ASSIGN:        return XOR_ASSIGN;
        case CHAR_LITERAL:       return CHAR_LITERAL;
        case COMMA:              return COMMA;
        case CTOR_CALL:          return THIS__CTOR_CALL;
        case DEC:                return PRE_DECR;
        case DIV:                return DIVIDE;
        case DIV_ASSIGN:         return DIVIDE_ASSIGN;
        case DO_WHILE:           return WHILE__DO;
        case ELLIPSIS:           return ELLIPSIS;
        case EMPTY_STAT:         return SEMI__EMPTY_STAT;
        case ENUM:               return ENUM;
        case EQUAL:              return EQUAL;
        case EXTENDS_CLAUSE:     return EXTENDS__TYPE;
        case FINAL:              return FINAL;
        case GE:                 return GREATER_EQUAL;
        case GT:                 return GREATER;
        case IMPLEMENTS_CLAUSE:  return IMPLEMENTS;
        case IMPORT:             return IMPORT;
        case INC:                return PRE_INCR;
        case INDEX_OP:           return L_BRACK__INDEX;
        case LABELED_STAT:       return COLON__LABELED_STAT;
        case LAND:               return CONDITIONAL_AND;
        case LE:                 return LESS_EQUAL;
        case LITERAL_ASSERT:     return ASSERT;
        case LITERAL_BOOLEAN:    return BOOLEAN;
        case LITERAL_BREAK:      return BREAK;
        case LITERAL_BYTE:       return BYTE;
        case LITERAL_CASE:       return CASE;
        case LITERAL_CATCH:      return CATCH;
        case LITERAL_CONTINUE:   return CONTINUE;
        case LITERAL_CHAR:       return CHAR;
        case LITERAL_DO:         return DO;
        case LITERAL_DOUBLE:     return DOUBLE;
        case LITERAL_ELSE:       return ELSE;
        case LITERAL_FALSE:      return FALSE;
        case LITERAL_FINALLY:    return FINALLY;
        case LITERAL_FLOAT:      return FLOAT;
        case LITERAL_FOR:        return FOR;
        case LITERAL_IF:         return IF;
        case LITERAL_INSTANCEOF: return INSTANCEOF;
        case LITERAL_INT:        return INT;
        case LITERAL_INTERFACE:  return INTERFACE;
        case LITERAL_LONG:       return LONG;
        case LITERAL_NATIVE:     return NATIVE;
        case LITERAL_NEW:        return NEW;
        case LITERAL_NULL:       return NULL;
        case LITERAL_PRIVATE:    return PRIVATE;
        case LITERAL_PROTECTED:  return PROTECTED;
        case LITERAL_PUBLIC:     return PUBLIC;
        case LITERAL_SHORT:      return SHORT;
        case LITERAL_SUPER:      return SUPER__EXPR;
        case LITERAL_SWITCH:     return SWITCH;
        case LITERAL_THIS:       return THIS__EXPR;
        case LITERAL_THROW:      return THROW;
        case LITERAL_THROWS:     return THROWS;
        case LITERAL_TRANSIENT:  return TRANSIENT;
        case LITERAL_TRUE:       return TRUE;
        case LITERAL_TRY:        return TRY;
        case LITERAL_VOID:       return VOID;
        case LITERAL_VOLATILE:   return VOLATILE;
        case LITERAL_WHILE:      return WHILE__WHILE;
        case LNOT:               return LOGICAL_COMPLEMENT;
        case LOR:                return CONDITIONAL_OR;
        case LT:                 return LESS;
        case METHOD_CALL:        return L_PAREN__METH_INVOCATION;
        case MINUS:              return MINUS__ADDITIVE;
        case MINUS_ASSIGN:       return MINUS_ASSIGN;
        case MOD:                return MODULO;
        case MOD_ASSIGN:         return MODULO_ASSIGN;
        case NOT_EQUAL:          return NOT_EQUAL;
        case NUM_DOUBLE:         return DOUBLE_LITERAL;
        case NUM_FLOAT:          return FLOAT_LITERAL;
        case NUM_INT:            return INT_LITERAL;
        case NUM_LONG:           return LONG_LITERAL;
        case PACKAGE_DEF:        return PACKAGE;
        case PLUS:               return PLUS__ADDITIVE;
        case PLUS_ASSIGN:        return PLUS_ASSIGN;
        case POST_DEC:           return POST_DECR;
        case POST_INC:           return POST_INCR;
        case QUESTION:           return QUESTION__TERNARY;
        case SL:                 return LEFT_SHIFT;
        case SL_ASSIGN:          return LEFT_SHIFT_ASSIGN;
        case SR:                 return RIGHT_SHIFT;
        case SR_ASSIGN:          return RIGHT_SHIFT_ASSIGN;
        case STAR_ASSIGN:        return MULTIPLY_ASSIGN;
        case STATIC_IMPORT:      return IMPORT__STATIC_IMPORT;
        case STRING_LITERAL:     return STRING_LITERAL;
        case SUPER_CTOR_CALL:    return SUPER__CTOR_CALL;
        case TYPE_EXTENSION_AND: return AND__TYPE_BOUND;
        case TYPE_LOWER_BOUNDS:  return SUPER__TYPE_BOUND;
        case TYPE_UPPER_BOUNDS:  return EXTENDS__TYPE_BOUND;
        case TYPECAST:           return L_PAREN__CAST;
        case UNARY_PLUS:         return PLUS__UNARY;
        case UNARY_MINUS:        return MINUS__UNARY;
        case WILDCARD_TYPE:      return QUESTION__WILDCARD_TYPE;

        case ARRAY_INIT:
            return firstChildType == LocalTokenType.RCURLY ? L_CURLY__EMPTY_ARRAY_INIT : L_CURLY__ARRAY_INIT;

        case ASSIGN:
            return parentType == LocalTokenType.VARIABLE_DEF ? ASSIGN__VAR_DECL : ASSIGN__ASSIGNMENT;

        case AT:
            assert parentType != null;
            switch (parentType) {

            case ANNOTATION:     return AT__ANNO;
            case ANNOTATION_DEF: return AT__ANNO_DECL;
            default:             assert false : parentType;
            }
            break;

        case COLON:
            assert parentType != null;
            switch (parentType) {

            case LITERAL_DEFAULT: return COLON__DEFAULT;
            case LITERAL_CASE:    return COLON__CASE;
            case FOR_EACH_CLAUSE: return COLON__ENHANCED_FOR;
            default:              return COLON__TERNARY;
            }

        case STAR: return parentType == LocalTokenType.DOT ? STAR__TYPE_IMPORT_ON_DEMAND : MULTIPLY;

        case DOT:
            if (AstUtil.getAncestorWithTypeNot(ast, LocalTokenType.DOT) == LocalTokenType.PACKAGE_DEF) {
                return DOT__PACKAGE_DECL;
            }
            if (AstUtil.getAncestorWithTypeNot(ast, LocalTokenType.DOT) == LocalTokenType.IMPORT) {
                return DOT__IMPORT;
            }
            if (AstUtil.getAncestorWithTypeNot(
                ast,
                LocalTokenType.ARRAY_DECLARATOR,
                LocalTokenType.DOT
            ) == LocalTokenType.TYPE) return DOT__QUALIFIED_TYPE;
            return DOT__SELECTOR;

        case GENERIC_END:
            assert parentType != null;
            switch (parentType) {

            case TYPE_PARAMETERS:
                return (
                    grandParentType == LocalTokenType.METHOD_DEF
                    || grandParentType == LocalTokenType.CTOR_DEF
                ) ? R_ANGLE__METH_DECL_TYPE_PARAMS : R_ANGLE__TYPE_ARGS;

            case TYPE_ARGUMENTS:
                {
                    LocalTokenType tt = AstUtil.getAncestorWithTypeNot(
                        ast,
                        LocalTokenType.TYPE_ARGUMENTS,
                        LocalTokenType.DOT
                    );
                    return (
                        tt == LocalTokenType.TYPE
                        || tt == LocalTokenType.LITERAL_NEW
                        || tt == LocalTokenType.EXTENDS_CLAUSE
                        || tt == LocalTokenType.IMPLEMENTS_CLAUSE
                    ) ? R_ANGLE__TYPE_ARGS : R_ANGLE__METH_INVOCATION_TYPE_ARGS;
                }

            default:
                assert false : parentType;
            }
            break;

        case GENERIC_START:
            assert parentType != null;
            switch (parentType) {

            case TYPE_PARAMETERS:
                return (
                    grandParentType == LocalTokenType.METHOD_DEF
                    || grandParentType == LocalTokenType.CTOR_DEF
                ) ? L_ANGLE__METH_DECL_TYPE_PARAMS : L_ANGLE__TYPE_ARGS;

            case TYPE_ARGUMENTS:
                return (
                    AstUtil.getAncestorWithTypeNot(
                        ast,
                        LocalTokenType.TYPE_ARGUMENTS,
                        LocalTokenType.DOT
                    ) == LocalTokenType.TYPE
                    || grandParentType == LocalTokenType.LITERAL_NEW
                    || grandParentType == LocalTokenType.EXTENDS_CLAUSE
                    || grandParentType == LocalTokenType.IMPLEMENTS_CLAUSE
                ) ? L_ANGLE__TYPE_ARGS : L_ANGLE__METH_INVOCATION_TYPE_ARGS;

            default:
                assert false : parentType;
            }
            break;

        case IDENT:
            assert parentType != null;
            switch (parentType) {

            case ANNOTATION:                   return NAME__ANNO;
            case ANNOTATION_FIELD_DEF:         return NAME__ANNO_ELEM_DECL;
            case VARIABLE_DEF:                 return NAME__LOCAL_VAR_DECL;
            case CTOR_DEF:                     return NAME__CTOR_DECL;
            case METHOD_DEF:                   return NAME__METH_DECL;
            case ANNOTATION_MEMBER_VALUE_PAIR: return NAME__ANNO_MEMBER;
            case PARAMETER_DEF:                return NAME__PARAM;

            case CLASS_DEF:
            case INTERFACE_DEF:
            case ANNOTATION_DEF:
            case ENUM_DEF:
                return NAME__TYPE_DECL;

            default:
                if (AstUtil.getAncestorWithTypeNot(ast, LocalTokenType.DOT) == LocalTokenType.PACKAGE_DEF) {
                    return NAME__PACKAGE_DECL;
                }

                if (AstUtil.getAncestorWithTypeNot(ast, LocalTokenType.DOT) == LocalTokenType.IMPORT) {
                    return ast.getNextSibling() == null ? NAME__IMPORT_TYPE : NAME__IMPORT_COMPONENT;
                }

                {
                    LocalTokenType a = AstUtil.getAncestorWithTypeNot(ast, LocalTokenType.ARRAY_DECLARATOR);
                    if (a == LocalTokenType.TYPE || a == LocalTokenType.LITERAL_NEW) return NAME__SIMPLE_TYPE;
                }

                if (AstUtil.getAncestorWithTypeNot(
                    ast,
                    LocalTokenType.ARRAY_DECLARATOR,
                    LocalTokenType.DOT
                ) == LocalTokenType.TYPE) return NAME__QUALIFIED_TYPE;

                return NAME__AMBIGUOUS;
            }

        case LCURLY:
            assert parentType != null;
            switch (parentType) {

            case LITERAL_SWITCH: return L_CURLY__SWITCH;

            case OBJBLOCK:
                assert grandParentType != null;
                switch (grandParentType) {

                case ENUM_CONSTANT_DEF: return L_CURLY__ENUM_CONST;

                case CLASS_DEF:
                case INTERFACE_DEF:
                case ANNOTATION_DEF:
                case ENUM_DEF:
                    return nextSiblingType == LocalTokenType.RCURLY ? L_CURLY__EMPTY_TYPE_DECL : L_CURLY__TYPE_DECL;

                case LITERAL_NEW:
                    return nextSiblingType == LocalTokenType.RCURLY ? L_CURLY__EMPTY_ANON_CLASS : L_CURLY__ANON_CLASS;

                default:
                    assert false : grandParentType;
                }
                break;

            case ARRAY_INIT:
                return nextSiblingType == LocalTokenType.RCURLY ? L_CURLY__EMPTY_ARRAY_INIT : L_CURLY__ARRAY_INIT;

            default:
                assert false : parentType;
            }
            break;

        case ANNOTATION_ARRAY_INIT:
            return (
                firstChildType == LocalTokenType.RCURLY
                ? L_CURLY__EMPTY_ANNO_ARRAY_INIT
                : L_CURLY__ANNO_ARRAY_INIT
            );

        case LITERAL_RETURN:
            return firstChildType == LocalTokenType.SEMI ? RETURN__NO_EXPR : RETURN__EXPR;

        case LITERAL_CLASS:
            return parentType == LocalTokenType.CLASS_DEF ? CLASS__CLASS_DECL : CLASS__CLASS_LITERAL;

        case LITERAL_DEFAULT:
            return (
                parentType == LocalTokenType.ANNOTATION_MEMBER_VALUE_PAIR
                || parentType == LocalTokenType.ANNOTATION_FIELD_DEF
            ) ? DEFAULT__ANNO_ELEM : DEFAULT__SWITCH;

        case LITERAL_STATIC:
            return parentType == LocalTokenType.STATIC_IMPORT ? STATIC__STATIC_IMPORT : STATIC__MOD;

        case LITERAL_SYNCHRONIZED:
            return parentType == LocalTokenType.SLIST ? SYNCHRONIZED__SYNCHRONIZED : SYNCHRONIZED__MOD;

        case STATIC_INIT:
            ast.setText("static");
            return STATIC__STATIC_INIT;

        case LPAREN:
            assert parentType != null;
            switch (parentType) {

            case ANNOTATION:           return L_PAREN__ANNO;
            case ANNOTATION_FIELD_DEF: return L_PAREN__ANNO_ELEM_DECL;
            case LITERAL_DO:           return L_PAREN__DO_WHILE;
            case LITERAL_IF:           return L_PAREN__IF;
            case LITERAL_CATCH:        return L_PAREN__CATCH;

            case SUPER_CTOR_CALL:
            case LITERAL_NEW:
                return L_PAREN__METH_INVOCATION;

            case LITERAL_FOR:
                return ast.getNextSibling().getFirstChild() == null ? L_PAREN__FOR_NO_INIT : L_PAREN__FOR;

            default:
                if (nextSiblingType == LocalTokenType.PARAMETERS) return L_PAREN__PARAMS;
                return L_PAREN__PARENTHESIZED;
            }

        case RBRACK:
            assert parentType != null;
            switch (parentType) {

            case ARRAY_DECLARATOR: return R_BRACK__ARRAY_DECL;
            case INDEX_OP:         return R_BRACK__INDEX;
            default:               assert false : parentType;
            }
            break;

        case RCURLY:
            assert parentType != null;
            switch (parentType) {

            case LITERAL_SWITCH: return R_CURLY__SWITCH;

            case ANNOTATION_ARRAY_INIT:
                return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_ANNO_ARRAY_INIT : R_CURLY__ANNO_ARRAY_INIT;

            case ARRAY_INIT:
                return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_ARRAY_INIT : R_CURLY__ARRAY_INIT;

            case OBJBLOCK:
                assert grandParentType != null;
                switch (grandParentType) {

                case ENUM_CONSTANT_DEF: return R_CURLY__ENUM_CONST_DECL;

                case CLASS_DEF:
                case INTERFACE_DEF:
                case ANNOTATION_DEF:
                case ENUM_DEF:
                    return previousSiblingType == LocalTokenType.LCURLY ? R_CURLY__EMPTY_TYPE_DECL : R_CURLY__TYPE_DECL;

                case LITERAL_NEW:
                    return (
                        previousSiblingType == LocalTokenType.LCURLY
                        ? R_CURLY__EMPTY_ANON_CLASS
                        : R_CURLY__ANON_CLASS
                    );

                default:
                    assert false : grandParentType;
                }
                break;

            case SLIST:
                assert grandParentType != null;
                switch (grandParentType) {

                case INSTANCE_INIT:        return R_CURLY__INSTANCE_INIT;
                case LABELED_STAT:         return R_CURLY__LABELED_STAT;
                case LITERAL_DO:           return R_CURLY__DO;
                case LITERAL_ELSE:         return R_CURLY__IF;
                case LITERAL_FINALLY:      return R_CURLY__FINALLY;
                case LITERAL_FOR:          return R_CURLY__FOR;
                case LITERAL_IF:           return R_CURLY__IF;
                case LITERAL_SYNCHRONIZED: return R_CURLY__SYNCHRONIZED;
                case LITERAL_TRY:          return R_CURLY__TRY;
                case LITERAL_WHILE:        return R_CURLY__WHILE;
                case SLIST:                return R_CURLY__BLOCK;
                case STATIC_INIT:          return R_CURLY__STATIC_INIT;

                case CTOR_DEF:
                case METHOD_DEF:
                    return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_METH_DECL : R_CURLY__METH_DECL;

                case ARRAY_INIT:
                    return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_ARRAY_INIT : R_CURLY__ARRAY_INIT;

                case LITERAL_CATCH:
                    return ast.getPreviousSibling() == null ? R_CURLY__EMPTY_CATCH : R_CURLY__CATCH;

                default:
                    assert false : grandParentType;
                }
                break;

            default:
                assert false : parentType;
            }
            break;

        case RPAREN:
            assert parentType != null;
            switch (parentType) {

            case ANNOTATION:           return R_PAREN__ANNO;
            case ANNOTATION_FIELD_DEF: return R_PAREN__ANNO_ELEM_DECL;
            case LITERAL_CATCH:        return R_PAREN__CATCH;
            case LITERAL_DO:           return R_PAREN__DO_WHILE;
            case LITERAL_IF:           return R_PAREN__IF;

            case CTOR_DEF:
            case METHOD_DEF:
                return R_PAREN__PARAMS;

            case SUPER_CTOR_CALL:
            case LITERAL_NEW:
            case METHOD_CALL:
                return R_PAREN__METH_INVOCATION;

            case LITERAL_FOR:
                return ast.getPreviousSibling().getFirstChild() == null ? R_PAREN__FOR_NO_UPDATE : R_PAREN__FOR;

            default:
                if (previousSiblingType == LocalTokenType.TYPE) return R_PAREN__CAST;
                return R_PAREN__PARENTHESIZED;
            }

        case SEMI:
            assert parentType != null;
            switch (parentType) {

            case PACKAGE_DEF:          return SEMI__PACKAGE_DECL;
            case IMPORT:               return SEMI__IMPORT;
            case STATIC_IMPORT:        return SEMI__STATIC_IMPORT;
            case METHOD_DEF:           return SEMI__ABSTRACT_METH_DECL;
            case ANNOTATION_FIELD_DEF: return SEMI__ANNO_ELEM_DECL;

            case OBJBLOCK:
                return previousSiblingType == LocalTokenType.ENUM_CONSTANT_DEF ? SEMI__ENUM_DECL :  SEMI__TYPE_DECL;

            case SLIST:
            case SUPER_CTOR_CALL:
            case CTOR_CALL:
            case LITERAL_DO:
            case LITERAL_RETURN:
            case LITERAL_BREAK:
            case LITERAL_CONTINUE:
            case LITERAL_IF:
            case LITERAL_WHILE:
            case LITERAL_ASSERT:
            case LITERAL_THROW:
                return SEMI__STATEMENT;

            case LITERAL_FOR:
                if (nextSiblingType == null) return SEMI__STATEMENT;
                if (previousSiblingType == LocalTokenType.FOR_INIT) {
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
                if (previousSiblingType == LocalTokenType.FOR_CONDITION) {
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

            case VARIABLE_DEF:
                if (grandParentType == LocalTokenType.OBJBLOCK) return SEMI__FIELD_DECL;
                break;

            default:
                assert false : parentType;
            }
            break;

        case SLIST:
            assert parentType != null;
            switch (parentType) {

            case STATIC_INIT:          return L_CURLY__STATIC_INIT;
            case INSTANCE_INIT:        return L_CURLY__INSTANCE_INIT;
            case LITERAL_IF:           return L_CURLY__IF;
            case LITERAL_ELSE:         return R_CURLY__ELSE;
            case LITERAL_DO:           return L_CURLY__DO;
            case LITERAL_WHILE:        return L_CURLY__WHILE;
            case LITERAL_FOR:          return L_CURLY__FOR;
            case LITERAL_TRY:          return L_CURLY__TRY;
            case LITERAL_FINALLY:      return L_CURLY__FINALLY;
            case LITERAL_SYNCHRONIZED: return L_CURLY__SYNCHRONIZED;
            case LABELED_STAT:         return L_CURLY__LABELED_STAT;
            case SLIST:                return L_CURLY__BLOCK;

            case LITERAL_CATCH:
                return firstChildType == LocalTokenType.RCURLY ? L_CURLY__EMPTY_CATCH : L_CURLY__CATCH;

            case CTOR_DEF:
            case METHOD_DEF:
                return (
                    firstChildType == LocalTokenType.RCURLY
                    ? JavaElement.L_CURLY__EMPTY_METH_DECL
                    : JavaElement.L_CURLY__METH_DECL
                );

            default:
                return null; // Not a 'physical' token.
            }

        case ANNOTATION:
        case ANNOTATION_DEF:
        case ANNOTATION_FIELD_DEF:
        case ANNOTATION_MEMBER_VALUE_PAIR:
        case ANNOTATIONS:
        case CASE_GROUP:
        case CLASS_DEF:
        case CTOR_DEF:
        case ELIST:
        case ENUM_DEF:
        case ENUM_CONSTANT_DEF:
        case EXPR:
        case FOR_EACH_CLAUSE:
        case FOR_INIT:
        case FOR_CONDITION:
        case FOR_ITERATOR:
        case INTERFACE_DEF:
        case INSTANCE_INIT:
        case METHOD_DEF:
        case MODIFIERS:
        case OBJBLOCK:
        case PARAMETER_DEF:
        case PARAMETERS:
        case RESOURCE:
        case RESOURCE_SPECIFICATION:
        case RESOURCES:
        case STRICTFP:
        case TYPE:
        case TYPE_ARGUMENT:
        case TYPE_ARGUMENTS:
        case TYPE_PARAMETER:
        case TYPE_PARAMETERS:
        case VARIABLE_DEF:
            // These are the 'virtual' tokens, i.e. token which are not uniquely related to a physical token.
            return null;

        case EOF:
            assert false : "Unexpected token '" + ast + "' (" + type + ')';
            return null;
        }

        assert false : (
            "'"
            + ast
            + "' (type '"
            + type
            + "') has unexpected parent type '"
            + parentType
            + "' and/or grandparent type '"
            + grandParentType
            + "'"
        );

        return null;

    }

    /**
     * @return The type of the closest ancestor who's type is no the given {@code tokenType}, or -1
     */
    @Nullable private static LocalTokenType
    getAncestorWithTypeNot(DetailAST ast, LocalTokenType tokenType) {
        for (DetailAST a = ast.getParent();; a = a.getParent()) {

            if (a == null) return null;

            int t = a.getType();
            if (t != tokenType.delocalize()) return LocalTokenType.localize(t);
        }
    }
    /**
     * @return The type of the closest ancestor who's type is not {@code tokenType1} or {@code tokenType2}, or -1
     */
    @Nullable private static LocalTokenType
    getAncestorWithTypeNot(DetailAST ast, LocalTokenType tokenType1, LocalTokenType tokenType2) {
        for (DetailAST a = ast.getParent();; a = a.getParent()) {

            if (a == null) return null;

            LocalTokenType t = LocalTokenType.localize(a.getType());
            if (t != tokenType1 && t != tokenType2) return t;
        }
    }

    /** @return Whether the type of the {@code ast} equals the given {@code ltt} */
    public static boolean
    typeIs(DetailAST ast, LocalTokenType ltt) { return LocalTokenType.localize(ast.getType()) == ltt; }

    /**
     * @return Whether the {@code ast} has a parent, and that has a preceeding sibling, and the type of that equals the
     *         given {@code ltt}
     */
    public static boolean
    previousUncleTypeIs(DetailAST ast, LocalTokenType ltt) {
        DetailAST parent = ast.getParent();
        if (parent != null) {
            DetailAST previousUncle = parent.getPreviousSibling();
            if (previousUncle != null) {
                return LocalTokenType.localize(previousUncle.getType()) == ltt;
            }
        }
        return false;
    }

    /**
     * @return Whether the {@code ast} has a parent, and that has a parent, and the type of that equals the given
     *         {@code ltt}
     */
    public static boolean
    grandGrandParentTypeIs(DetailAST ast, LocalTokenType ltt) {
        DetailAST parent = ast.getParent();
        if (parent != null) {
            DetailAST grandParent = parent.getParent();
            if (grandParent != null) {
                DetailAST grandGrandParent = grandParent.getParent();
                if (grandGrandParent != null) {
                    return LocalTokenType.localize(grandGrandParent.getType()) == ltt;
                }
            }
        }
        return false;
    }
}

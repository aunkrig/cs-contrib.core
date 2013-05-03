
package de.unkrig.cscontrib.checks;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.Utils;

/**
 * Statements must be uniformly wrapped and indented.
 */
public
class WrapAndIndent extends Check {

    /** How many spaces to use for new indentation level. */
    private int basicOffset = 4;

    /** May be ORed to the {@link TokenTypes}. */
    private static final int OPTIONAL           = 0x80000000;
    private static final int INDENT_IF_CHILDREN = 0x40000000;
    private static final int INDENT             = 0x20000000;
    private static final int UNINDENT           = 0x10000000;
    private static final int WRAP               = 0x08000000;
    private static final int MUST_WRAP          = 0x04000000;
    private static final int MASK               = 0x03ffffff;

    private static final int ANY     = 999;
    private static final int FORK    = 800;
    private static final int BRANCH  = 900;
    private static final int END     = 996;

    public void
    setBasicOffset(int basicOffset) { this.basicOffset = basicOffset; }

    public int
    getBasicOffset() { return basicOffset; }

    public int[]
    getDefaultTokens() {
        return new int[] {
//            ABSTRACT,
            ANNOTATION,
            ANNOTATIONS,
            ANNOTATION_ARRAY_INIT,
            ANNOTATION_DEF,
            ANNOTATION_FIELD_DEF,
            ANNOTATION_MEMBER_VALUE_PAIR,
            ARRAY_DECLARATOR,
            ARRAY_INIT,
//            ASSIGN,
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
            INDEX_OP,
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
        };
    }

    public void
    visitToken(DetailAST ast) {

        @SuppressWarnings("unused") ASTDumper dumper = new ASTDumper(ast); // For debugging

        switch (ast.getType()) {

        case ANNOTATION:
            checkChildren(
                ast,

                AT,
                FORK + 4,
                DOT,
                BRANCH + 5,

/* 4 */         IDENT,

/* 5 */         FORK + 7,
                END,

/* 7 */         LPAREN,
                BRANCH + 10,

/* 9 */         COMMA,

/* 10 */        FORK + 13,
                INDENT | ANNOTATION_MEMBER_VALUE_PAIR,
                BRANCH + 20,

/* 13 */        FORK + 16,
                INDENT | ANNOTATION,
                BRANCH + 20,

/* 16 */        FORK + 19,
                INDENT | EXPR,
                BRANCH + 20,

/* 19 */        INDENT | ANNOTATION_ARRAY_INIT,

/* 20 */        FORK + 9,
                UNINDENT | RPAREN,
                END
            );
            break;

        case ANNOTATION_ARRAY_INIT:
            checkChildren(
                ast,

                FORK + 5,

/* 1 */         INDENT | EXPR,
                FORK + 5,
                COMMA,
                FORK + 1,

/* 5 */         UNINDENT | RCURLY,
                END
            );
            break;

        case ANNOTATION_DEF:
            checkChildren(
                ast,
                
                MODIFIERS,
                MUST_WRAP | AT,
                LITERAL_INTERFACE,
                IDENT,
                OBJBLOCK,
                END
            );
            break;

        case ARRAY_INIT:
            checkChildren(
                ast,
                
/* 0 */         FORK + 5,
                INDENT | ANY,
                FORK + 5,
                COMMA,
                BRANCH + 0,
/* 5 */         UNINDENT | RCURLY,
                END
            );
            break;

        case CASE_GROUP:
            checkChildren(
                ast,

                FORK + 6,
                LITERAL_CASE,                              // case 1: case 2:
/* 2 */         FORK + 5,
                WRAP | LITERAL_CASE,
                BRANCH + 2,

/* 5 */         FORK + 7,
/* 6 */         WRAP | LITERAL_DEFAULT,

/* 7 */         INDENT_IF_CHILDREN | SLIST,
                END
            );
            break;
            
        case CLASS_DEF:
            if (isSingleLine(ast)) break;
            checkChildren(
                ast,
                
                MODIFIERS,
                LITERAL_CLASS | MUST_WRAP,
                IDENT,

                FORK + 5,
                TYPE_PARAMETERS,

/* 5 */         FORK + 7,
                WRAP | EXTENDS_CLAUSE,

/* 7 */         FORK + 9,
                WRAP | IMPLEMENTS_CLAUSE,

/* 9 */         OBJBLOCK,
                END
            );
            break;

        case CTOR_CALL:
            checkChildren(
                ast,

                LPAREN,
                INDENT_IF_CHILDREN | ELIST,
                UNINDENT | RPAREN,
                SEMI,
                END
            );
            break;
          
        case CTOR_DEF:
            if (isSingleLine(ast)) break;
            checkChildren(
                ast,

                MODIFIERS,
                IDENT | MUST_WRAP,

                LPAREN,
                INDENT_IF_CHILDREN | PARAMETERS,
                UNINDENT | RPAREN,

                FORK + 7,
                WRAP | LITERAL_THROWS,

/* 7 */         SLIST,
                END
            );
            break;

        case ELIST:
            checkChildren(
                ast,
                
                FORK + 5,
/* 1 */         INDENT | EXPR,
                FORK + 5,
/* 3 */         COMMA,
                BRANCH + 1,
/* 6 */         END
            );
            break;

        case EXPR:
            {
                DetailAST child = ast.getFirstChild();
                if (child.getType() == LPAREN) {
                    child = checkParenthesizedExpression(child, false);
                    assert child == null;
                } else {
                    boolean inline;
                    switch (ast.getParent().getType()) {

                    case ANNOTATION:        // @SuppressWarnings(#)
                    case ASSIGN:            // a = #
                    case FOR_CONDITION:     // for (; #;)
                    case FOR_EACH_CLAUSE:   // for (Object o : #)
                    case LITERAL_ASSERT:    // assert #
                    case LITERAL_CASE:      // case #:
                    case LITERAL_ELSE:      // else #;
                    case LITERAL_FOR:       // for (...; ...; ...) #;
                    case LITERAL_RETURN:    // return #
                    case LITERAL_THROW:     // throw #
                    case SLIST:             // #;
                        inline = true;
                        break;

                    case ARRAY_DECLARATOR:     // new String[#]
                    case ARRAY_INIT:           // int[] a = { # }
                    case INDEX_OP:             // a[#]
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
                            getFileContents().getFilename()
                            + ":"
                            + ast.getLineNo()
                            + ": EXPR has unexpected parent "
                            + TokenTypes.getTokenName(ast.getParent().getType())
                        );
                        inline = false;
                        break;
                    }
                    checkExpression(child, inline);
                }
            }
            break;
            
        case FOR_EACH_CLAUSE:
            checkChildren(
                ast,
                
                VARIABLE_DEF,
                WRAP | COLON,
                EXPR,
                END
            );
            break;

        case INTERFACE_DEF:
            if (isSingleLine(ast)) break;
            checkChildren(
                ast,
                
                MODIFIERS,
                LITERAL_INTERFACE | MUST_WRAP,
                IDENT,
                FORK + 5,
                TYPE_PARAMETERS,

/* 5 */         FORK + 7,
                WRAP | EXTENDS_CLAUSE,

/* 7 */         OBJBLOCK,
                END
            );
            break;

        case LABELED_STAT:
            checkChildren(
                ast,

                IDENT,
                WRAP | ANY,
                END
            );
            break;
            
        case INDEX_OP:
            checkChildren(
                ast,

                ANY,
                INDENT | EXPR,
                UNINDENT | RBRACK,
                END
            );
            break;
            
        case LITERAL_DO:
            checkChildren(
                ast,

                SLIST,
                DO_WHILE,
                LPAREN,
                INDENT | EXPR,
                UNINDENT | RPAREN,
                SEMI,
                END
            );
            break;
            
        case LITERAL_FOR:
            checkChildren(
                ast,
                
                LPAREN,
                FORK + 8,
                INDENT | FOR_INIT,
                SEMI,
                INDENT | FOR_CONDITION,
                SEMI,
                INDENT_IF_CHILDREN | FOR_ITERATOR,
                FORK + 9,

/* 8 */         INDENT | FOR_EACH_CLAUSE,
/* 9 */         UNINDENT | RPAREN,

                FORK + 14,
                EXPR,
                SEMI,
                END,

/* 14 */        ANY,
                END
            );
            break;

        case LITERAL_IF:
            checkChildren(
                ast,

                LPAREN,
                INDENT | EXPR,
                UNINDENT | RPAREN,
                FORK + 7,
                EXPR,
                SEMI,
                END,

/* 7 */         ANY,
                FORK + 10,
                LITERAL_ELSE,

/* 10 */        END
            );
            break;
            
        case LITERAL_NEW:
            checkChildren(
                ast,

/* 0 */         ANY,
                FORK + 3,
                TYPE_ARGUMENTS,
/* 3 */         FORK + 8,
                ARRAY_DECLARATOR,
                FORK + 7,
                ARRAY_INIT,
/* 7 */         END,
/* 8 */         LPAREN,
                INDENT_IF_CHILDREN | ELIST,
                UNINDENT | RPAREN,
                OBJBLOCK | OPTIONAL,
                END
            );
            break;

        case LITERAL_SWITCH:
            checkChildren(
                ast,

                LPAREN,
                INDENT | EXPR,
                UNINDENT | RPAREN,
                LCURLY,
                FORK + 7,
/* 5 */         INDENT | CASE_GROUP,
                FORK + 5,
/* 7 */         UNINDENT | RCURLY,
                END
            );
            break;

        case METHOD_DEF:
            if (isSingleLine(ast)) break;
            checkChildren(
                ast,
                
/* 0 */         MODIFIERS,
                FORK + 3,
                TYPE_PARAMETERS,

/* 3 */         TYPE,

                IDENT | MUST_WRAP,

                LPAREN,
                INDENT_IF_CHILDREN | PARAMETERS,
                UNINDENT | RPAREN,

                FORK + 10,
                WRAP | LITERAL_THROWS,

/* 10 */        FORK + 13,
                SLIST,
                END,

/* 13 */        SEMI,
                END
            );
            break;

        case LITERAL_WHILE:
            checkChildren(
                ast,

                LPAREN,
                INDENT | EXPR,
                UNINDENT | RPAREN,
                FORK + 7,

                EXPR,
                SEMI,
                END,

/* 7 */         ANY,
                END
            );
            break;

        case MODIFIERS:
            checkChildren(
                ast,

                FORK + 3,

/* 1 */         ANY,
                FORK + 1,

/* 3 */         END
            );
            break;

        case OBJBLOCK:
            checkChildren(
                ast,

/* 0 */         LCURLY,

                FORK + 8,
/* 2 */         INDENT | ENUM_CONSTANT_DEF,
                FORK + 6,
                COMMA,
                FORK + 2,
/* 6 */         FORK + 8,
                INDENT | SEMI,

/* 8 */         FORK + 14,
                INDENT | VARIABLE_DEF,
/* 10 */        FORK + 8,
                COMMA,
                VARIABLE_DEF,
                BRANCH + 10,

/* 14 */        FORK + 17,
/* 15 */        UNINDENT | RCURLY,
                END,

/* 17 */        INDENT | ANY,
                BRANCH + 6
            );
            break;

        case PARAMETERS:
            checkChildren(
                ast,
                
                FORK + 5,
/* 1 */         INDENT | PARAMETER_DEF,
                FORK + 5,
/* 3 */         COMMA,
                BRANCH + 1,
/* 5 */         END
            );
            break;
            
        case SLIST:
            // Single-line case group?
            if (
                ast.getParent().getType() == TokenTypes.CASE_GROUP
                && isSingleLine(ast)
                && ast.getParent().getLineNo() == ast.getLineNo()
            ) return;

            checkChildren(
                ast,
                
/* 0 */         FORK + 4,
                INDENT | EXPR,
                SEMI,
                BRANCH + 0,
                
/* 4 */         FORK + 12,
                INDENT | VARIABLE_DEF,
/* 6 */         FORK + 10,
                COMMA,
                VARIABLE_DEF,
                BRANCH + 6,
/* 10 */        SEMI,
                BRANCH + 0,

                // SLIST in CASE_GROUP ends _without_ an RCURLY!
/* 12 */        FORK + 14,
                END,

/* 14 */        FORK + 17,
                UNINDENT | RCURLY,
                END,

/* 17 */        INDENT | ANY,
                BRANCH + 0
            );
            break;

        case SUPER_CTOR_CALL:
            checkChildren(
                ast,
                
                LPAREN,
                INDENT_IF_CHILDREN | ELIST,
                UNINDENT | RPAREN,
                SEMI,
                END
            );
            break;

        case VARIABLE_DEF:
            checkChildren(
                ast,

                MODIFIERS,
                TYPE,
                WRAP | IDENT,
                FORK + 5,
                ASSIGN,
/* 5 */         FORK + 7,
                SEMI, // Field declarations DO have a SEMI, local variable declarations DON'T!?
/* 7 */         END
            );
            break;

        // Those which were not registered for.
        case ABSTRACT:
        case ASSIGN:
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
            System.currentTimeMillis();
        case ANNOTATION_FIELD_DEF:
        case ANNOTATION_MEMBER_VALUE_PAIR:
        case ANNOTATIONS:
        case ARRAY_DECLARATOR:
        case ENUM_CONSTANT_DEF:
        case ENUM_DEF:
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
        case LITERAL_TRY:
        case PACKAGE_DEF:
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
            checkChildren(
                ast,

                FORK + 3,
/* 1 */         ANY,
                FORK + 1,
/* 3 */         END
            );
            break;

        default:
            throw new AssertionError("Unknown token type '" + ast.getType() + "'");
        }
    }

    private boolean
    isSingleLine(DetailAST ast) {
        return getLeftmostDescendant(ast).getLineNo() == getRightmostDescendant(ast).getLineNo();
    }

    /**
     * @param inline Iff {@code true}, then the entire expression must appear on one line.
     */
    private void
    checkExpression(DetailAST expression, boolean inline) {
        switch (expression.getType()) {

        // Ternary operation
        case QUESTION:
            {
                DetailAST c = checkParenthesizedExpression(expression.getFirstChild(), inline);
                c = checkParenthesizedExpression(c, inline);
                assert c.getType() == COLON;
                c = c.getNextSibling();
                c = checkParenthesizedExpression(c, inline);
                assert c == null;
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
                DetailAST c = checkParenthesizedExpression(expression.getFirstChild(), inline);
                if (c != null && c.getType() == TYPE_ARGUMENTS) {

                    // TYPE_ARGUMENTS checked by "visitToken()".
                    ;
                    c = c.getNextSibling();
                }
                assert c != null : (
                    getFileContents().getFilename()
                    + ":"
                    + expression.getLineNo()
                    + ": Second operand for '"
                    + TokenTypes.getTokenName(expression.getType())
                    + "' missing"
                );
                c = checkParenthesizedExpression(c, inline);
                assert c == null : (
                    getFileContents().getFilename()
                    + ":"
                    + expression.getLineNo()
                    + ": Unexpected third operand "
                    + TokenTypes.getTokenName(c.getType())
                    + "/'"
                    + c.getText()
                    + "' for '"
                    + TokenTypes.getTokenName(expression.getType())
                    + "'"
                );
            }
            break;

        // Unary operations
        case BNOT:
        case DEC:
        case INC:
        case LNOT:
        case POST_DEC:
        case POST_INC:
        case UNARY_MINUS:
        case UNARY_PLUS:
            {
                DetailAST c = checkParenthesizedExpression(expression.getFirstChild(), inline);
                assert c == null;
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
                checkExpression(method, inline);
                checkSameLine(method, expression);

                DetailAST arguments = method.getNextSibling();
                DetailAST rparen    = arguments.getNextSibling();

                assert rparen.getType() == RPAREN;
                assert rparen.getNextSibling() == null;

                DetailAST firstArgument = arguments.getFirstChild();
                if (
                    firstArgument == null
                    || getLeftmostDescendant(firstArgument).getLineNo() == expression.getLineNo()
                ) {
                    checkSameLine(getRightmostDescendant(arguments), rparen);
                } else {
                    checkAligned(getLeftmostDescendant(expression), rparen);
                }
            }
            break;

        case LITERAL_NEW:
        case ARRAY_INIT:
        case TYPECAST:
        case INDEX_OP:

            // Checked by "visitToken()".
            ;
            break;

        default:
            log(
                expression,
                "Uncheckable: " + TokenTypes.getTokenName(expression.getType()) + " / " + expression.toString()
            );
        }
    }

    private static DetailAST
    getLeftmostDescendant(DetailAST ast) {
        for (;;) {
            DetailAST tmp = ast.getFirstChild();
            if (tmp == null && ast.getType() == MODIFIERS) tmp = ast.getNextSibling(); 
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
        if (previous.getType() != LPAREN) {
            checkExpression(previous, inline);
            return previous.getNextSibling();
        }

        @SuppressWarnings("unused") ASTDumper dumper = new ASTDumper(previous); // For debugging

        int       parenthesisCount = 1;
        DetailAST next             = previous.getNextSibling();
        for (;;) {
            if (next.getType() != LPAREN) {
                break;
            }
            checkSameLine(previous, next);
            previous = next;
            next     = next.getNextSibling();
        }

        if (previous.getLineNo() == getLeftmostDescendant(next).getLineNo()) {
            checkExpression(next, true);
            previous = next;
            next     = next.getNextSibling();
            checkSameLine(getRightmostDescendant(previous), next);
        } else {
            checkIndented(previous, getLeftmostDescendant(next));
            checkExpression(next, false);
            previous = next;
            next     = next.getNextSibling();
            checkUnindented(getRightmostDescendant(previous), next);
        }

        previous = next;
        for (int i = 1; i < parenthesisCount; ++i) {
            assert previous.getType() == RPAREN;
            next = next.getNextSibling();
            checkSameLine(previous, next);
            previous = next;
        }
        assert next.getType() == RPAREN;
        return next.getNextSibling();
    }

    /**
     * Verifies that the children of the given {@code ast} are positioned as specified.
     */
    private void
    checkChildren(DetailAST ast, Integer... args) {
        int       idx   = 0;
        DetailAST child = ast.getFirstChild();

        // Determine the "indentation parent".
        if (ast.getType() == ELIST) {
            ast = ast.getParent();
        } else
        if (ast.getType() == SLIST && ast.getParent().getType() == CASE_GROUP) {
            ast = ast.getParent().getParent();
        } else
        if (ast.getType() == PARAMETERS) {
            ast = ast.getParent().findFirstToken(IDENT);
        } else
        if (ast.getType() == DOT) {
            ast = getLeftmostDescendant(ast);
        }

        DetailAST previousAst = ast;
        int       indentation = calculateIndentation(previousAst);
        int       mode        = 0;
        for (;;) {
            int tokenType = args[idx++];

            // Handle END.
            if ((tokenType & MASK) == END) {
                if (child == null) return;
                log(child, "Unexpected extra token ''{0}''", child.getText());
                return;
            }

            // Handle OPTIONAL.
            if ((tokenType & OPTIONAL) != 0) {
                if (child != null && ((tokenType & MASK) == ANY || child.getType() == (tokenType & MASK))) {
                    previousAst = child;
                    child       = child.getNextSibling();
                }
                continue;
            }

            // Handle FORK.
            if (tokenType >= FORK && tokenType <= FORK + 90) {

                int     destination = tokenType - FORK;
                Integer da          = args[destination] & MASK;
                if (
                    child == null ? args[idx] != END && da >= FORK
                    : (
                        da == ANY
                        || (da >= FORK && da <= FORK + 90)
                        || (da >= BRANCH && da <= BRANCH + 90)
                    ) ? (args[idx] & MASK) != child.getType()
                    : da == child.getType()
                ) idx = destination;
                continue;
            }

            // Handle BRANCH.
            if (tokenType >= BRANCH && tokenType <= BRANCH + 90) {
                idx = tokenType - BRANCH;
                continue;
            }
            
            if (child == null) {
                log(
                    previousAst,
                    "Expected ''{0}'' after ''{1}''",
                    ((tokenType & MASK) == ANY ? "ANY" : TokenTypes.getTokenName(tokenType & MASK)),
                    previousAst.getText()
                );
                return;
            }

            if (
                (tokenType & MASK) != ANY
                && child.getType() != (tokenType & MASK)
            ) {
                log(
                    child,
                    "Expected ''{0}'' instead of ''{1}''",
                    TokenTypes.getTokenName(tokenType & MASK),
                    child.getText() + "'"
                );
                return;
            }

            if ((tokenType & INDENT_IF_CHILDREN) != 0 && child.getFirstChild() == null) {
                ;
            } else if ((tokenType & (INDENT | INDENT_IF_CHILDREN)) != 0) {
                switch (mode) {

                case 0:
                    {
                        DetailAST c = getLeftmostDescendant(child);
                        if (c.getLineNo() == previousAst.getLineNo()) {
                            mode = 1;
                        } else {
                            mode = 2;
                            checkAlignment(
                                c,
                                child.getType() == CASE_GROUP ? indentation : indentation + this.basicOffset
                            );
                        }
                    }
                    break;

                case 1:
                    checkSameLine(previousAst, child);
                    break;

                case 2:
                    {
                        DetailAST l = getLeftmostDescendant(child);
                        if (l.getLineNo() == previousAst.getLineNo()) {
                            if (
                                ast.getType() == TokenTypes.ARRAY_INIT
                                || ast.getType() == TokenTypes.METHOD_CALL
                                || ast.getParent().getType() == TokenTypes.ENUM_DEF
                            ) {

                                // Allow multiple children in the same line.
                                ;
                            } else {
                                log(l, "Must wrap line before ''{0}''", l.getText());
                            }
                        } else {
                            checkAlignment(
                                l,
                                child.getType() == CASE_GROUP ? indentation : indentation + this.basicOffset
                            );
                        }
                    }
                    break;
                }
            } else if ((tokenType & UNINDENT) != 0) {
                switch (mode) {

                case 0:
                    if (previousAst.getLineNo() != child.getLineNo()) {
                        checkAligned(ast, child);
                    }
                    break;

                case 1:
                    checkSameLine(previousAst, child);
                    break;

                case 2:
                    checkAligned(ast, child);
                    break;
                }
                mode = 0;
            } else if ((tokenType & WRAP) != 0) {
                assert mode == 0;
                if (child.getLineNo() != previousAst.getLineNo()) {
                    checkAlignment(child, indentation);
                }
            } else if ((tokenType & MUST_WRAP) != 0) {
                assert mode == 0;
                if (previousAst.getType() == MODIFIERS) {
                    ;
                } else
                if (child.getLineNo() == previousAst.getLineNo()) {
                    log(child, "Must wrap line before ''{0}''", child.getText());
                } else
                {
                    checkAlignment(child, indentation);
                }
            } else {
                checkSameLine(previousAst, getLeftmostDescendant(child));
            }
            previousAst = getRightmostDescendant(child);
            child       = child.getNextSibling();
        }
    }

    /**
     * Checks that the line where {@code next} occurs is indented by {@link #DEFAULT_INDENTATION}, compared to the line
     * where {@code previous} occurs.
     */
    private void
    checkIndented(DetailAST previous, DetailAST next) {
        if (next.getLineNo() == previous.getLineNo()) {
            log(next, "Must wrap line before ''{0}''", next.getText());
        } else {
            checkAlignment(next, calculateIndentation(previous) + this.basicOffset);
        }
    }

    /**
     * Checks that the line where {@code next} occurs is unindented by {@link #DEFAULT_INDENTATION}, compared to the
     * line where {@code previous} occurs.
     */
    private void
    checkUnindented(DetailAST previous, DetailAST next) {
        if (next.getLineNo() == previous.getLineNo()) {
            log(next, "Must wrap line before ''{0}''", next.getText());
        } else {
            checkAlignment(next, calculateIndentation(previous) - this.basicOffset);
        }
    }
    
    /**
     * Checks that the line where {@code next} occurs is indented exactly as the line where {@code previous} occurs.
     */
    private void
    checkAligned(DetailAST previous, DetailAST next) {
        if (next.getLineNo() == previous.getLineNo()) {
            log(next, "Must wrap line before ''{0}''", next.getText());
        } else {
            checkAlignment(next, calculateIndentation(previous));
        }
    }

    private void
    checkSameLine(DetailAST left, DetailAST right) {
        if (left.getLineNo() != right.getLineNo()) {
            log(
                right,
                "''{0}'' must appear on same line as ''{1}''",
                right.getText(),
                left.getText()
            );
        }
    }
    
    /**
     * Logs a problem iff the given {@code ast} is not vertically positioned at the given {@code targetColumnNo}.
     *
     * @param targetColumnNo Counting from zero
     */
    private void
    checkAlignment(DetailAST ast, int targetColumnNo) {
        int actualColumnNo = Utils.lengthExpandedTabs(
            getLines()[ast.getLineNo() - 1],
            ast.getColumnNo(),
            this.getTabWidth()
        );
        if (actualColumnNo != targetColumnNo) {
            log(
                ast,
                "''{0}'' must appear in column {1}, not {2}",
                ast.getText(),
                targetColumnNo + 1,
                actualColumnNo + 1
            );
        }
    }

    /**
     * Calculate the indentation of the line of the given {@code ast}, honoring TAB characters. Notice that the
     * {@code ast} need not be the FIRST element in that line.
     */
    private int
    calculateIndentation(DetailAST ast) {
        String line = getLines()[ast.getLineNo() - 1];

        int result = 0;
        for (int i = 0; i < line.length(); ++i) {
            switch (line.charAt(i)) {

            case ' ':
                ++result;
                break;

            case '\t':
                {
                    int tabWidth = getTabWidth();
                    result += tabWidth - (result % tabWidth);
                }
                break;

            default:
                return result;
            }
        }
        return 0;
    }
}

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
            ABSTRACT,
            ANNOTATION,
            ANNOTATIONS,
            ANNOTATION_ARRAY_INIT,
            ANNOTATION_DEF,
            ANNOTATION_FIELD_DEF,
            ANNOTATION_MEMBER_VALUE_PAIR,
            ARRAY_DECLARATOR,
            ARRAY_INIT,
//            ASSIGN,
            AT,
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
//            DOT,
            DO_WHILE,
            ELIST,
//            ELLIPSIS,
            EMPTY_STAT,
            ENUM,
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
            GENERIC_END,
            GENERIC_START,
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
            LCURLY,
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
            LITERAL_INTERFACE,
//            LITERAL_LONG,
//            LITERAL_NATIVE,
            LITERAL_NEW,
//            LITERAL_NULL,
//            LITERAL_PRIVATE,
//            LITERAL_PROTECTED,
//            LITERAL_PUBLIC,
            LITERAL_RETURN,
//            LITERAL_SHORT,
            LITERAL_STATIC,
//            LITERAL_SUPER,
            LITERAL_SWITCH,
            LITERAL_SYNCHRONIZED,
            LITERAL_THIS,
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
            LPAREN,
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
            RBRACK,
            RCURLY,
//            RPAREN,
            SEMI,
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
            TYPE_EXTENSION_AND,
            TYPE_LOWER_BOUNDS,
            TYPE_PARAMETER,
            TYPE_PARAMETERS,
            TYPE_UPPER_BOUNDS,
//            UNARY_MINUS,
//            UNARY_PLUS,
            VARIABLE_DEF,
            WILDCARD_TYPE,
        };
    }

    public void
    visitToken(DetailAST ast) {
        switch (ast.getType()) {

        case ARRAY_INIT:
            checkChildren(
                ast,
                
                FORK + 5,          // 0
                ANY | INDENT,
                FORK + 5,
                COMMA,
                BRANCH + 0,
                RCURLY | UNINDENT, // 5
                END
            );
            break;

        case CASE_GROUP:
            checkChildren(
                ast,

                FORK + 6,
                LITERAL_CASE,
                FORK + 5,                   // 2
                LITERAL_CASE | WRAP,
                BRANCH + 2,

                FORK + 7,                   // 5
                LITERAL_DEFAULT | WRAP,     // 6

                SLIST | INDENT_IF_CHILDREN, // 7
                END
            );
            break;
            
        case CLASS_DEF:
            checkChildren(
                ast,
                
                MODIFIERS,
                LITERAL_CLASS | MUST_WRAP,
                IDENT,

                FORK + 5,
                TYPE_PARAMETERS,

                FORK + 7,            // 5
                EXTENDS_CLAUSE | WRAP,

                FORK + 9,            // 7
                IMPLEMENTS_CLAUSE | WRAP,

                OBJBLOCK,            // 9
                END
            );
            break;

        case CTOR_CALL:
            checkChildren(
                ast,

                LPAREN,
                ELIST | INDENT_IF_CHILDREN,
                RPAREN | UNINDENT,
                SEMI,
                END
            );
            break;
          
        case CTOR_DEF:
            checkChildren(
                ast,

                MODIFIERS,
                IDENT | MUST_WRAP,

                LPAREN,
                PARAMETERS | INDENT_IF_CHILDREN,
                RPAREN | UNINDENT,

                FORK + 7,
                LITERAL_THROWS | WRAP,

                SLIST,      // 7
                END
            );
            break;

        case ELIST:
            checkChildren(
                ast,
                
                FORK + 5,
                EXPR | INDENT, // 1
                FORK + 5,
                COMMA,         // 3
                BRANCH + 1,
                END            // 6
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
                COLON | WRAP,
                EXPR,
                END
            );
            break;

        case INTERFACE_DEF:
            checkChildren(
                ast,
                
                MODIFIERS,
                LITERAL_INTERFACE | MUST_WRAP,
                IDENT,
                FORK + 5,
                TYPE_PARAMETERS,

                FORK + 7,            // 5
                EXTENDS_CLAUSE | WRAP,

                OBJBLOCK,            // 7
                END
            );
            break;

        case LABELED_STAT:
            checkChildren(
                ast,

                IDENT,
                ANY | WRAP,
                END
            );
            break;
            
        case INDEX_OP:
            checkChildren(
                ast,

                ANY,
                EXPR | INDENT,
                RBRACK | UNINDENT,
                END
            );
            break;
            
        case LITERAL_DO:
            checkChildren(
                ast,

                SLIST,
                DO_WHILE,
                LPAREN,
                EXPR | INDENT,
                RPAREN | UNINDENT,
                SEMI,
                END
            );
            break;
            
        case LITERAL_FOR:
            checkChildren(
                ast,
                
                LPAREN,
                FORK + 8,
                FOR_INIT | INDENT,
                SEMI,
                FOR_CONDITION | INDENT,
                SEMI,
                FOR_ITERATOR | INDENT_IF_CHILDREN,
                FORK + 9,

                FOR_EACH_CLAUSE | INDENT, // 8
                RPAREN | UNINDENT,        // 9

                FORK + 14,
                EXPR,
                SEMI,
                END,

                ANY,                      // 14
                END
            );
            break;

        case LITERAL_IF:
            checkChildren(
                ast,

                LPAREN,
                EXPR | INDENT,
                RPAREN | UNINDENT,
                FORK + 7,
                EXPR,
                SEMI,
                END,

                ANY,       // 7
                FORK + 10,
                LITERAL_ELSE,

                END        // 10
            );
            break;
            
        case LITERAL_NEW:
            checkChildren(
                ast,

                ANY,           // 0  Identifier or primitive
                FORK + 3,
                TYPE_ARGUMENTS,
                FORK + 8,        // 3
                ARRAY_DECLARATOR,
                FORK + 7,
                ARRAY_INIT,
                END,             // 7
                LPAREN,          // 8
                ELIST | INDENT_IF_CHILDREN,
                RPAREN | UNINDENT,
                OBJBLOCK | OPTIONAL,
                END
            );
            break;

        case LITERAL_SWITCH:
            checkChildren(
                ast,

                LPAREN,
                EXPR | INDENT,
                RPAREN | UNINDENT,
                LCURLY,
                FORK + 7,
                CASE_GROUP | INDENT, // 5
                FORK + 5,
                RCURLY | UNINDENT,   // 7
                END
            );
            break;

        case METHOD_DEF:
            checkChildren(
                ast,
                
                MODIFIERS,    // 0
                FORK + 3,
                TYPE_PARAMETERS,

                TYPE,         // 3

                IDENT | MUST_WRAP,

                LPAREN,
                PARAMETERS | INDENT_IF_CHILDREN,
                RPAREN | UNINDENT,

                FORK + 10,
                LITERAL_THROWS | WRAP,

                FORK + 13, // 10
                SLIST,
                END,

                SEMI,     // 13
                END
            );
            break;

        case LITERAL_WHILE:
            checkChildren(
                ast,

                LPAREN,
                EXPR | INDENT,
                RPAREN | UNINDENT,
                FORK + 7,

                EXPR,
                SEMI,
                END,

                ANY,       // 7
                END
            );
            break;

        case MODIFIERS:
            checkChildren(
                ast,

                FORK + 3,
                ANNOTATION | WRAP, // 1
                FORK + 1,

                FORK + 6,          // 3
                ANY | WRAP,        // 4
                FORK + 4,
                END                // 6
            );
            break;

        case OBJBLOCK:
            checkChildren(
                ast,

                LCURLY,                     // 0

                FORK + 8,
                ENUM_CONSTANT_DEF | INDENT, // 2
                FORK + 6,
                COMMA,
                FORK + 2,
                FORK + 8,                   // 6
                SEMI | WRAP,

                FORK + 14,                  // 8
                VARIABLE_DEF | INDENT,
                FORK + 8,                   // 10
                COMMA,
                VARIABLE_DEF,
                BRANCH + 10,

                FORK + 17,                  // 14
                RCURLY | UNINDENT,          // 15
                END,

                ANY | INDENT,               // 17
                BRANCH + 6
            );
            break;

        case PARAMETERS:
            checkChildren(
                ast,
                
                FORK + 5,
                PARAMETER_DEF | INDENT, // 1
                FORK + 5,
                COMMA,                  // 3
                BRANCH + 1,
                END                     // 5
            );
            break;
            
        case SLIST:
            checkChildren(
                ast,
                
                FORK + 4,     // 0
                EXPR | INDENT,
                SEMI,
                BRANCH + 0,
                
                FORK + 12,    // 4
                VARIABLE_DEF | INDENT,
                FORK + 10,    // 6
                COMMA,
                VARIABLE_DEF,
                BRANCH + 6,
                SEMI,         // 10
                BRANCH + 0,

                // SLIST in CASE_GROUP ends _without_ an RCURLY!
                FORK + 14,    // 12
                END,

                FORK + 17,    // 14
                RCURLY | UNINDENT,
                END,

                ANY | INDENT, // 17
                BRANCH + 0
            );
            break;

        case SUPER_CTOR_CALL:
            checkChildren(
                ast,
                
                LPAREN,
                ELIST | INDENT_IF_CHILDREN,
                RPAREN | UNINDENT,
                SEMI,
                END
            );
            break;

        case VARIABLE_DEF:
            checkChildren(
                ast,

                MODIFIERS,
                TYPE,
                IDENT | WRAP,
                FORK + 5,
                ASSIGN,
                FORK + 7, // 5
                SEMI, // Field declarations DO have a SEMI, local variable declarations DON'T!?
                END       // 7
            );
            break;

        default:
            checkChildren(
                ast,

                FORK + 3,
                ANY,        // 1
                FORK + 1,
                END         // 3
            );
            break;
        }
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

        @SuppressWarnings("unused") Dumper dumper = new Dumper(previous); // For debugging

        int       parenthesisCount = 1;
        DetailAST next             = previous.getNextSibling();
        for (;;) {
            if (next.getType() != LPAREN) {
                break;
            }
            checkSameLine(previous, next);
            previous = next;
            next = next.getNextSibling();
        }

        if (previous.getLineNo() == getLeftmostDescendant(next).getLineNo()) {
            checkExpression(next, true);
            previous = next;
            next = next.getNextSibling();
            checkSameLine(getRightmostDescendant(previous), next);
        } else {
            checkIndented(previous, getLeftmostDescendant(next));
            checkExpression(next, false);
            previous = next;
            next = next.getNextSibling();
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
        } else if (ast.getType() == SLIST && ast.getParent().getType() == CASE_GROUP) {
            ast = ast.getParent().getParent();
        } else if (ast.getType() == PARAMETERS) {
            ast = ast.getParent().findFirstToken(IDENT);
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
                    child = child.getNextSibling();
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
                            if (ast.getType() == TokenTypes.ARRAY_INIT || ast.getType() == TokenTypes.METHOD_CALL) {

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
            child = child.getNextSibling();
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

    static class Dumper {
        private DetailAST ast;

        Dumper(DetailAST ast) {
            this.ast = ast;
        }

        @Override public String
        toString() {
            StringBuilder sb = new StringBuilder();
            dumpSiblings("", this.ast, sb);
            return sb.toString();
        }

        private static void
        dumpSiblings(String prefix, DetailAST sibling, StringBuilder sb) {
            for (; sibling != null; sibling = sibling.getNextSibling()) {
                sb.append(prefix).append(sibling).append('\n');
                dumpSiblings(prefix + "  ", sibling.getFirstChild(), sb);
            }
        }
    }
}
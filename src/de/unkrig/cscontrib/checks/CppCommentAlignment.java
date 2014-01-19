
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.ConversionException;

import com.google.common.collect.ImmutableMap;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.AbstractFormatCheck;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

/**
 * Verifies that C++ comments are correctly aligned.
 * <p>
 * C++ comments must appear on the same column iff
 * <ul>
 *   <li>They designate AST siblings, and
 *   <li>They appear in immediately consecutive lines
 * </ul>
 * Examples for AST siblings are the arguments in an invocation, or the operands of an arithmetic expression.
 */
@NotNullByDefault(false) public
class CppCommentAlignment extends AbstractFormatCheck {

    private ImmutableMap<Integer /*lineNumber*/, TextBlock> cppComments;

    public
    CppCommentAlignment() throws ConversionException { super("^[\\s\\}\\);]*$"); }

    @Override public int[]
    getDefaultTokens() {
        return new int[] {

            // Binary operators.
            TokenTypes.ASSIGN,
            TokenTypes.BAND,
            TokenTypes.BAND_ASSIGN,
            TokenTypes.BOR,
            TokenTypes.BOR_ASSIGN,
            TokenTypes.BSR,
            TokenTypes.BSR_ASSIGN,
            TokenTypes.BXOR,
            TokenTypes.BXOR_ASSIGN,
            TokenTypes.DIV,
            TokenTypes.DIV_ASSIGN,
            TokenTypes.DOT,
            TokenTypes.EQUAL,
            TokenTypes.GE,
            TokenTypes.GT,
            TokenTypes.INDEX_OP,
            TokenTypes.LAND,
            TokenTypes.LE,
            TokenTypes.LITERAL_INSTANCEOF,
            TokenTypes.LOR,
            TokenTypes.LT,
            TokenTypes.MINUS,
            TokenTypes.MINUS_ASSIGN,
            TokenTypes.MOD,
            TokenTypes.MOD_ASSIGN,
            TokenTypes.NOT_EQUAL,
            TokenTypes.PLUS,
            TokenTypes.PLUS_ASSIGN,
            TokenTypes.SL,
            TokenTypes.SL_ASSIGN,
            TokenTypes.SR,
            TokenTypes.SR_ASSIGN,
            TokenTypes.STAR,
            TokenTypes.STAR_ASSIGN,

            // Unary operators.
            TokenTypes.BNOT,
            TokenTypes.LNOT,
//          TokenTypes.UNARY_MINUS,
//          TokenTypes.UNARY_PLUS,

            // Other tokens.
//            TokenTypes.ABSTRACT,
            TokenTypes.ANNOTATION,
            TokenTypes.ANNOTATION_ARRAY_INIT,
            TokenTypes.ANNOTATION_DEF,
            TokenTypes.ANNOTATION_FIELD_DEF,
//            TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR,
            TokenTypes.ANNOTATIONS,
            TokenTypes.ARRAY_DECLARATOR,
            TokenTypes.ARRAY_INIT,
//            TokenTypes.AT,
            TokenTypes.CASE_GROUP,
//            TokenTypes.CHAR_LITERAL,
            TokenTypes.CLASS_DEF,
//            TokenTypes.COLON,
//            TokenTypes.COMMA,
            TokenTypes.CTOR_CALL,
            TokenTypes.CTOR_DEF,
//            TokenTypes.DEC,
//            TokenTypes.DO_WHILE,
            TokenTypes.ELIST,
//            TokenTypes.ELLIPSIS,
//            TokenTypes.EMPTY_STAT,
//            TokenTypes.ENUM,
            TokenTypes.ENUM_CONSTANT_DEF,
            TokenTypes.ENUM_DEF,
//            TokenTypes.EOF,
//            TokenTypes.EXPR, // Pseudo token, e.g. EXPR { '(' '+' ')' }
            TokenTypes.EXTENDS_CLAUSE,
//            TokenTypes.FINAL,
            TokenTypes.FOR_CONDITION,
            TokenTypes.FOR_EACH_CLAUSE,
            TokenTypes.FOR_INIT,
            TokenTypes.FOR_ITERATOR,
//            TokenTypes.GENERIC_END,
//            TokenTypes.GENERIC_START,
//            TokenTypes.IDENT,
            TokenTypes.IMPLEMENTS_CLAUSE,
//            TokenTypes.IMPORT,
//            TokenTypes.INC,
//            TokenTypes.INSTANCE_INIT,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.LABELED_STAT,
//            TokenTypes.LCURLY,
            TokenTypes.LITERAL_ASSERT,
//            TokenTypes.LITERAL_BOOLEAN,
            TokenTypes.LITERAL_BREAK,
//            TokenTypes.LITERAL_BYTE,
            TokenTypes.LITERAL_CASE,
            TokenTypes.LITERAL_CATCH,
//            TokenTypes.LITERAL_CHAR,
//            TokenTypes.LITERAL_CLASS,
            TokenTypes.LITERAL_CONTINUE,
//            TokenTypes.LITERAL_DEFAULT,
            TokenTypes.LITERAL_DO,
//            TokenTypes.LITERAL_DOUBLE,
            TokenTypes.LITERAL_ELSE,
//            TokenTypes.LITERAL_FALSE,
//            TokenTypes.LITERAL_FINALLY,
//            TokenTypes.LITERAL_FLOAT,
            TokenTypes.LITERAL_FOR,
            TokenTypes.LITERAL_IF,
//            TokenTypes.LITERAL_INT,
//            TokenTypes.LITERAL_INTERFACE,
//            TokenTypes.LITERAL_LONG,
//            TokenTypes.LITERAL_NATIVE,
//            TokenTypes.LITERAL_NEW, // Irrelevant: "new { FormalParameter'(' ELIST ')' }
//            TokenTypes.LITERAL_NULL,
//            TokenTypes.LITERAL_PRIVATE,
//            TokenTypes.LITERAL_PROTECTED,
//            TokenTypes.LITERAL_PUBLIC,
            TokenTypes.LITERAL_RETURN,
//            TokenTypes.LITERAL_SHORT,
//            TokenTypes.LITERAL_STATIC,
//            TokenTypes.LITERAL_SUPER,
            TokenTypes.LITERAL_SWITCH,
            TokenTypes.LITERAL_SYNCHRONIZED,
//            TokenTypes.LITERAL_THIS,
            TokenTypes.LITERAL_THROW,
            TokenTypes.LITERAL_THROWS,
//            TokenTypes.LITERAL_TRANSIENT,
//            TokenTypes.LITERAL_TRUE,
            TokenTypes.LITERAL_TRY,
//            TokenTypes.LITERAL_VOID,
//            TokenTypes.LITERAL_VOLATILE,
            TokenTypes.LITERAL_WHILE,
//            TokenTypes.LPAREN,
//            TokenTypes.METHOD_CALL  METHOD_CALL { meth ELIST ')' }
            TokenTypes.METHOD_DEF,
            TokenTypes.MODIFIERS,
//            TokenTypes.NUM_DOUBLE,
//            TokenTypes.NUM_FLOAT,
//            TokenTypes.NUM_INT,
//            TokenTypes.NUM_LONG,
            TokenTypes.OBJBLOCK,
            TokenTypes.PACKAGE_DEF,
            TokenTypes.PARAMETER_DEF,
            TokenTypes.PARAMETERS,
//            TokenTypes.POST_DEC,
//            TokenTypes.POST_INC,
            TokenTypes.QUESTION,
//            TokenTypes.RBRACK,
//            TokenTypes.RCURLY,
//            TokenTypes.RESOURCE,
//            TokenTypes.RESOURCE_SPECIFICATION,
//            TokenTypes.RESOURCES,
//            TokenTypes.RPAREN,
//            TokenTypes.SEMI,
            TokenTypes.SLIST,
            TokenTypes.STATIC_IMPORT,
//            TokenTypes.STATIC_INIT,
//            TokenTypes.STRICTFP,
//            TokenTypes.STRING_LITERAL,
            TokenTypes.SUPER_CTOR_CALL,
            TokenTypes.TYPE,
            TokenTypes.TYPECAST,
            TokenTypes.TYPE_ARGUMENT,
            TokenTypes.TYPE_ARGUMENTS,
            TokenTypes.TYPE_PARAMETER,
//            TokenTypes.TYPE_EXTENSION_AND,
//            TokenTypes.TYPE_LOWER_BOUNDS,
            TokenTypes.TYPE_PARAMETERS,
            TokenTypes.TYPE_UPPER_BOUNDS,
//            TokenTypes.VARIABLE_DEF,  // Pseudo token, e.g. VARIABLE_DEF { MODIFIERS TYPE name init }
//            TokenTypes.WILDCARD_TYPE,
        };
    }

    @Override public void
    beginTree(DetailAST ast) {
        this.cppComments = getFileContents().getCppComments();
    }

    @Override public void
    visitToken(DetailAST ast) {
        this.debug("ast=" + ast);
        if (ast.getChildCount() <= 1) return;
        
        List<DetailAST> children = this.getChildren(ast);

        Map<Integer /*lineNo*/, Integer /*colNo*/> commentCoordinates = new HashMap<Integer, Integer>();

        int prevLineNo = Integer.MAX_VALUE;
        for (DetailAST child : children) {
            this.debug("lchild=" + child);

            int lineNo = child.getLineNo();

            // Special case "CASE_GROUP { 'case' 'case' SLIST }".
            if (child.getType() == TokenTypes.SLIST) continue;

            TextBlock tb = this.cppComments.get(lineNo);
            if (tb == null || tb.getStartColNo() == 1) continue;

            if (tb.getStartLineNo() - 1 > prevLineNo) {
                analyze(commentCoordinates);
                commentCoordinates.clear();
                prevLineNo = Integer.MAX_VALUE;
                continue;
            }

            if (commentCoordinates.containsKey(lineNo)) continue;

            commentCoordinates.put(lineNo, tb.getStartColNo());
            prevLineNo = lineNo;
        }
        analyze(commentCoordinates);
    }
    
    private void
    analyze(Map<Integer /*lineNo*/, Integer /*colNo*/> commentCoordinates) {

        if (commentCoordinates.size() <= 1) return;

        int maxCommentColNo = 0;
        for (Integer commentColNo : commentCoordinates.values()) {
            if (commentColNo > maxCommentColNo) maxCommentColNo = commentColNo;
        }

        for (Entry<Integer /*lineNo*/, Integer /*colNo*/> e : commentCoordinates.entrySet()) {
            Integer commentLineNo = e.getKey();
            Integer commentColNo  = e.getValue();
            if (commentColNo != maxCommentColNo) {
                log(
                    commentLineNo,
                    commentColNo,
                    "C++ comment must appear on column {0}, not {1}",
                    maxCommentColNo + 1,
                    commentColNo  + 1
                );
            }
        }
    }

    /**
     * Returns the children of the given node, but with somtimes flattened, e.g. "(a + b) + (c + d)" is
     * "a + b + c + d".
     */
    private List<DetailAST>
    getChildren(DetailAST ast) {

        {
            DetailAST parent = ast.getParent();
            if (parent != null && parent.getText().equals(ast.getText())) return Collections.emptyList();
        }

        List<DetailAST> result = new ArrayList<DetailAST>();
        getChildren2(ast, result);
        return result;
    }
    
    private void
    getChildren2(DetailAST ast, List<DetailAST> result) {

        for (DetailAST child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
            this.debug("child=" + child);
            
            if (child.getText().equals(ast.getText())) {
                this.getChildren2(child, result);
                continue;
            }
            
            result.add(child);
        }
    }

    private void
    debug(String text) {
        ;
    }
}

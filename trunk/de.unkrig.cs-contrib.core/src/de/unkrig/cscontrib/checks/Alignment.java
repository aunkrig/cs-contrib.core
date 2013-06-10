
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

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static de.unkrig.cscontrib.util.AstUtil.grandparentTypeIs;
import static de.unkrig.cscontrib.util.AstUtil.parentTypeIs;
import static de.unkrig.cscontrib.util.AstUtil.previousSiblingTypeIs;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

/**
 * Checks that field/parameter/variable names are aligned in one-per-line declarations.
 */
public
class Alignment extends Check {

    private boolean applyToFieldName                = true;
    private boolean applyToFieldInitializer         = true;
    private boolean applyToLocalVariableName        = true;
    private boolean applyToLocalVariableInitializer = true;
    private boolean applyToParameterName            = true;
    private boolean applyToMethodName               = true;
    private boolean applyToMethodBody               = true;
    private boolean applyToCaseGroupStatements      = true;
    private boolean applyToAssignments              = true;

    // CONFIGURATION SETTERS -- CHECKSTYLE MethodCheck:OFF

    public void
    setApplyToFieldName(boolean applyToFieldName) {
        this.applyToFieldName = applyToFieldName;
    }

    public void
    setApplyToFieldInitializer(boolean applyToFieldInitializer) {
        this.applyToFieldInitializer = applyToFieldInitializer;
    }

    public void
    setApplyToLocalVariableName(boolean applyToLocalVariableName) {
        this.applyToLocalVariableName = applyToLocalVariableName;
    }

    public void
    setApplyToLocalVariableInitializer(boolean applyToLocalVariableInitializer) {
        this.applyToLocalVariableInitializer = applyToLocalVariableInitializer;
    }

    public void
    setApplyToParameterName(boolean applyToParameterName) {
        this.applyToParameterName = applyToParameterName;
    }

    public void
    setApplyToMethodName(boolean applyToMethodName) {
        this.applyToMethodName = applyToMethodName;
    }

    public void
    setApplyToMethodBody(boolean applyToMethodBody) {
        this.applyToMethodBody = applyToMethodBody;
    }

    public void
    setApplyToCaseGroupStatements(boolean applyToCaseGroupStatements) {
        this.applyToCaseGroupStatements = applyToCaseGroupStatements;
    }

    public void
    setApplyToAssignments(boolean applyToAssignments) {
        this.applyToAssignments = applyToAssignments;
    }

    // END CONFIGURATION SETTERS -- CHECKSTYLE MethodCheck:ON

    @Override public int[]
    getDefaultTokens() {
        return new int[] { VARIABLE_DEF, PARAMETER_DEF, METHOD_DEF, CTOR_DEF, CASE_GROUP, EXPR };
    }

    private DetailAST previousFieldDeclaration         = null;
    private DetailAST previousParameterDeclaration     = null;
    private DetailAST previousLocalVariableDeclaration = null;
    private DetailAST previousMethodDeclaration        = null;
    private DetailAST previousCaseGroup                = null;
    private DetailAST previousAssignment               = null;

    @Override public void
    visitToken(DetailAST ast) {

        @SuppressWarnings("unused") AstDumper ad = new AstDumper(ast);

        switch (ast.getType()) {

        case VARIABLE_DEF:
            if (
                !previousSiblingTypeIs(ast, COMMA)
                && grandparentTypeIs(ast, CLASS_DEF, INTERFACE_DEF, ENUM_DEF)
            ) {

                // First declarator in a field declaration.
                checkDeclarationAlignment(
                    this.previousFieldDeclaration,
                    ast,
                    this.applyToFieldName,
                    this.applyToFieldInitializer
                );
                this.previousFieldDeclaration = ast;
                return;
            }

            if (
                !previousSiblingTypeIs(ast, COMMA)
                && parentTypeIs(ast, SLIST)
            ) {

                // First declarator in a local variable declaration in block (not in a FOR initializer).
                checkDeclarationAlignment(
                    this.previousLocalVariableDeclaration,
                    ast,
                    this.applyToLocalVariableName,
                    this.applyToLocalVariableInitializer
                );
                this.previousLocalVariableDeclaration = ast;
                return;
            }
            break;

        case PARAMETER_DEF:
            // Parameter declaration.
            checkDeclarationAlignment(this.previousParameterDeclaration, ast, this.applyToParameterName, false);
            this.previousParameterDeclaration = ast;
            break;

        case METHOD_DEF:
        case CTOR_DEF:
            // Method or constructor declaration.
            checkMethodDefinitionAlignment(this.previousMethodDeclaration, ast);
            this.previousMethodDeclaration = ast;
            break;

        case CASE_GROUP:
            if (this.applyToCaseGroupStatements) {
                checkCaseGroupLignment(this.previousCaseGroup, ast);
                this.previousCaseGroup = ast;
            }
            break;

        case EXPR:
            if (this.applyToAssignments && ast.getParent().getType() == SLIST) {
                DetailAST ass = ast.getFirstChild();
                if (ass.getType() >= ASSIGN && ass.getType() <= BOR_ASSIGN) {
                    checkTokenAlignment(this.previousAssignment, ass);
                    this.previousAssignment = ass;
                }
            }
        }
    }

    private void
    checkCaseGroupLignment(DetailAST previous, DetailAST current) {
        if (previous == null) return;

        DetailAST casE = current.getFirstChild();
        if (casE.getType() != LITERAL_CASE) return;
        DetailAST slist = casE.getNextSibling();
        if (slist.getType() != SLIST) return;
        if (slist.getChildCount() == 0) return;

        checkTokenAlignment(
            getLeftmostDescendant(previous.getFirstChild().getNextSibling()),
            getLeftmostDescendant(slist.getFirstChild())
        );
    }

    @Override public void
    beginTree(DetailAST aRootAst) {
        this.previousFieldDeclaration         = null;
        this.previousParameterDeclaration     = null;
        this.previousLocalVariableDeclaration = null;
        this.previousMethodDeclaration        = null;
        this.previousCaseGroup                = null;
        this.previousAssignment               = null;
    }

    /**
     * Logs a problem iff the names of the first declarators of the two declarations are not vertically aligned.
     * <p>
     * Does nothing if {@code previousDeclaration} is {@code null}.
     */
    private void
    checkDeclarationAlignment(
        DetailAST previousDeclaration,
        DetailAST currentDeclaration,
        boolean   applyToName,
        boolean   applyToInitializer
    ) {
        if (previousDeclaration == null) return;

        if (currentDeclaration.getParent() != previousDeclaration.getParent()) return;

        // Apply alignment check only to first declarator of a declaration, e.g. "int a = 3, b = 7".
        if (previousDeclaration.getLineNo() == currentDeclaration.getLineNo()) return;

        // Check vertical alignment of names.
        if (applyToName) {
            checkTokenAlignment(
                previousDeclaration.findFirstToken(IDENT),
                currentDeclaration.findFirstToken(IDENT)
            );
        }

        // Check vertical alignment of initializers.
        if (applyToInitializer) {
            checkTokenAlignment(
                previousDeclaration.findFirstToken(ASSIGN),
                currentDeclaration.findFirstToken(ASSIGN)
            );
        }
    }

    /**
     * Logs problems iff the names or the bodies of the two method declarations are not vertically aligned.
     * <p>
     * Does nothing if {@code firstDefinition} is {@code null}.
     */
    private void
    checkMethodDefinitionAlignment(DetailAST previousDefinition, DetailAST currentDefinition) {
        if (previousDefinition == null) return;

        // Check vertical alignment of names.
        if (this.applyToMethodName) {
            checkTokenAlignment(
                previousDefinition.findFirstToken(IDENT),
                currentDefinition.findFirstToken(IDENT)
            );
        }

        // Check vertical alignment of initializers.
        if (this.applyToMethodBody) {
            checkTokenAlignment(
                previousDefinition.findFirstToken(SLIST),
                currentDefinition.findFirstToken(SLIST)
            );
        }
    }

    private void
    checkTokenAlignment(DetailAST previousToken, DetailAST currentToken) {
        if (previousToken == null || currentToken == null) return;

        if (
            previousToken.getLineNo() + 1 == currentToken.getLineNo()
            && previousToken.getColumnNo() != currentToken.getColumnNo()
        ) {

            // The name in the current declaration is not vertically aligned with the name in the declaration in the
            // preceding line.
            log(
                currentToken,
                "''{0}'' should be aligned with ''{1}'' in line {2,number,#}",
                currentToken.getText(),
                previousToken.getText(),
                previousToken.getLineNo()
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

//    private static DetailAST
//    getRightmostDescendant(DetailAST ast) {
//        for (;;) {
//            DetailAST tmp = ast.getLastChild();
//            if (
//                tmp == null
//                || tmp.getLineNo() < ast.getLineNo()
//                || (tmp.getLineNo() == ast.getLineNo() && tmp.getColumnNo() < ast.getColumnNo())
//            ) return ast;
//            ast = tmp;
//        }
//    }
}

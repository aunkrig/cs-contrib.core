
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


import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
import de.unkrig.cscontrib.util.AstUtil;

// SUPPRESS CHECKSTYLE LineLength:33
/**
 * Verifies that Java elements are vertically aligned in immediately consecutive lines (and only there!):
 * <pre>
 * public class Main {
 *
 *     int    <font color="red">x</font> = 7;
 *     double <font color="red">xxx</font> = 7.0;              // Aligned field names
 *
 *     int y      <font color="red">=</font> 7;
 *     double yyy <font color="red">=</font> 7.0;              // Aligned field initializers
 *
 *     public static void meth1(
 *         String[] <font color="red">p1</font>,
 *         int      <font color="red">p2</font>                // Aligned parameter names
 *     ) {
 *
 *         int    <font color="red">x</font> = 7;
 *         double <font color="red">xxx</font> = 7.0;          // Aligned local variable names
 *
 *         int y      <font color="red">=</font> 7;
 *         double yyy <font color="red">=</font> 7.0;          // Aligned local variable initializers
 *
 *         y   <font color="red">=</font> 8;
 *         yyy <font color="red">=</font> 8.0;                 // Aligned assignments
 *
 *         switch (x) {
 *         case 1:  <font color="red">break;</font>
 *         default: <font color="red">x++;</font> return;      // Aligned case groups statements
 *         }
 *     }
 *
 *     public static void <font color="red">meth2</font>() {}
 *     public void        <font color="red">meth33</font>() {} // Aligned method names
 *
 *     public static void meth4()  <font color="red">{}</font>
 *     public void meth5()         <font color="red">{}</font> // Aligned method bodies
 * }</pre>
 *
 * @cs-rule-group %Whitespace.group
 * @cs-rule-name  de.unkrig: Alignment
 * @cs-rule-parent TreeWalker
 */
@NotNullByDefault(false) public
class Alignment extends Check {

    /** @cs-message ''{0}'' should be aligned with ''{1}'' in line {2,number,#} */
    public static final String MESSAGE_KEY_MISALIGNED = "Alignment.misaligned";

    // CONFIGURATION SETTERS -- CHECKSTYLE MethodCheck:OFF

    /**
     * Check alignment of first name in field declarations.
     *
     * @cs-property-name          applyToFieldName
     * @cs-property-default-value {@value #DEFAULT_APPLY_TO_FIELD_NAME}
     */
    public void
    setApplyToFieldName(boolean applyToFieldName) {
        this.applyToFieldName = applyToFieldName;
    }
    private boolean              applyToFieldName            = Alignment.DEFAULT_APPLY_TO_FIELD_NAME;
    private static final boolean DEFAULT_APPLY_TO_FIELD_NAME = true;

    /**
     * Check alignment of first '=' in field declarations.
     *
     * @cs-property-name          applyToFieldInitializer
     * @cs-property-default-value {@value #DEFAULT_APPLY_TO_FIELD_INITIALIZER}
     */
    public void
    setApplyToFieldInitializer(boolean applyToFieldInitializer) {
        this.applyToFieldInitializer = applyToFieldInitializer;
    }
    private boolean              applyToFieldInitializer            = Alignment.DEFAULT_APPLY_TO_FIELD_INITIALIZER;
    private static final boolean DEFAULT_APPLY_TO_FIELD_INITIALIZER = true;

    /**
     * Check alignment of method (and constructor) parameter names.
     *
     * @cs-property-name          applyToParameterName
     * @cs-property-default-value {@value #DEFAULT_APPLY_TO_PARAMETER_NAME}
     */
    public void
    setApplyToParameterName(boolean applyToParameterName) {
        this.applyToParameterName = applyToParameterName;
    }
    private boolean              applyToParameterName            = Alignment.DEFAULT_APPLY_TO_PARAMETER_NAME;
    private static final boolean DEFAULT_APPLY_TO_PARAMETER_NAME = true;

    /**
     * Check alignment of first name in local variable declarations.
     *
     * @cs-property-name          applyToLocalVariableName
     * @cs-property-default-value {@value #DEFAULT_APPLY_TO_LOCAL_VARIABLE_NAME}
     */
    public void
    setApplyToLocalVariableName(boolean applyToLocalVariableName) {
        this.applyToLocalVariableName = applyToLocalVariableName;
    }
    private boolean              applyToLocalVariableName             = Alignment.DEFAULT_APPLY_TO_LOCAL_VARIABLE_NAME;
    private static final boolean DEFAULT_APPLY_TO_LOCAL_VARIABLE_NAME = true;

    /**
     * Check alignment of first '=' in local variable declarations.
     *
     * @cs-property-name          applyToLocalVariableInitializer
     * @cs-property-default-value {@value #DEFAULT_APPLY_TO_LOCAL_VARIABLE_INITIALIZER}
     */
    public void
    setApplyToLocalVariableInitializer(boolean applyToLocalVariableInitializer) {
        this.applyToLocalVariableInitializer = applyToLocalVariableInitializer;
    }
    private boolean              applyToLocalVariableInitializer             = Alignment.DEFAULT_APPLY_TO_LOCAL_VARIABLE_INITIALIZER; // SUPPRESS CHECKSTYLE LineLength
    private static final boolean DEFAULT_APPLY_TO_LOCAL_VARIABLE_INITIALIZER = true;

    /**
     * Check alignment of '=' in assignments.
     *
     * @cs-property-name          applyToAssignments
     * @cs-property-default-value {@value #DEFAULT_APPLY_TO_ASSIGNMENTS}
     */
    public void
    setApplyToAssignments(boolean applyToAssignments) {
        this.applyToAssignments = applyToAssignments;
    }
    private boolean              applyToAssignments           = Alignment.DEFAULT_APPLY_TO_ASSIGNMENTS;
    private static final boolean DEFAULT_APPLY_TO_ASSIGNMENTS = true;

    /**
     * Check alignment of first statement in case groups.
     *
     * @cs-property-name          applyToCaseGroupStatements
     * @cs-property-default-value {@value #DEFAULT_APPLY_TO_CASE_GROUP_STATEMENTS}
     */
    public void
    setApplyToCaseGroupStatements(boolean applyToCaseGroupStatements) {
        this.applyToCaseGroupStatements = applyToCaseGroupStatements;
    }
    private boolean              applyToCaseGroupStatements             = Alignment.DEFAULT_APPLY_TO_CASE_GROUP_STATEMENTS; // SUPPRESS CHECKSTYLE LineLength
    private static final boolean DEFAULT_APPLY_TO_CASE_GROUP_STATEMENTS = true;

    /**
     * Check alignment of method (and constructor) names in declarations.
     *
     * @cs-property-name          applyToMethodName
     * @cs-property-default-value {@value #DEFAULT_APPLY_TO_METHOD_NAME}
     */
    public void
    setApplyToMethodName(boolean applyToMethodName) {
        this.applyToMethodName = applyToMethodName;
    }
    private boolean              applyToMethodName            = Alignment.DEFAULT_APPLY_TO_METHOD_NAME;
    private static final boolean DEFAULT_APPLY_TO_METHOD_NAME = true;

    /**
     * Check alignment of '{' in method (and constructor) declarations.
     *
     * @cs-property-name          applyToMethodBody
     * @cs-property-default-value {@value #DEFAULT_APPLY_TO_METHOD_BODY}
     */
    public void
    setApplyToMethodBody(boolean applyToMethodBody) {
        this.applyToMethodBody = applyToMethodBody;
    }
    private boolean              applyToMethodBody            = Alignment.DEFAULT_APPLY_TO_METHOD_BODY;
    private static final boolean DEFAULT_APPLY_TO_METHOD_BODY = true;

    // END CONFIGURATION SETTERS -- CHECKSTYLE MethodCheck:ON

    @Override public int[]
    getDefaultTokens() {
        return new int[] {
            LocalTokenType.CASE_GROUP.delocalize(),
            LocalTokenType.CTOR_DEF.delocalize(),
            LocalTokenType.EXPR.delocalize(),
            LocalTokenType.METHOD_DEF.delocalize(),
            LocalTokenType.PARAMETER_DEF.delocalize(),
            LocalTokenType.VARIABLE_DEF.delocalize(),
        };
    }

    private DetailAST previousFieldDeclaration;
    private DetailAST previousParameterDeclaration;
    private DetailAST previousLocalVariableDeclaration;
    private DetailAST previousMethodDeclaration;
    private DetailAST previousCaseGroup;
    private DetailAST previousAssignment;

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        @SuppressWarnings("unused") AstDumper ad = new AstDumper(ast);

        switch (LocalTokenType.localize(ast.getType())) {

        case VARIABLE_DEF:
            if (
                !AstUtil.previousSiblingTypeIs(ast, LocalTokenType.COMMA)
                && AstUtil.grandParentTypeIs(
                    ast,
                    LocalTokenType.CLASS_DEF,
                    LocalTokenType.INTERFACE_DEF,
                    LocalTokenType.ENUM_DEF
                )
            ) {

                // First declarator in a field declaration.
                this.checkDeclarationAlignment(
                    this.previousFieldDeclaration,
                    ast,
                    this.applyToFieldName,
                    this.applyToFieldInitializer
                );
                this.previousFieldDeclaration = ast;
                return;
            }

            if (
                !AstUtil.previousSiblingTypeIs(ast, LocalTokenType.COMMA)
                && AstUtil.parentTypeIs(ast, LocalTokenType.SLIST)
            ) {

                // First declarator in a local variable declaration in block (not in a FOR initializer).
                this.checkDeclarationAlignment(
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
            this.checkDeclarationAlignment(this.previousParameterDeclaration, ast, this.applyToParameterName, false);
            this.previousParameterDeclaration = ast;
            break;

        case METHOD_DEF:
        case CTOR_DEF:
            // Method or constructor declaration.
            this.checkMethodDefinitionAlignment(this.previousMethodDeclaration, ast);
            this.previousMethodDeclaration = ast;
            break;

        case CASE_GROUP:
            if (this.applyToCaseGroupStatements) {
                this.checkCaseGroupLignment(this.previousCaseGroup, ast);
                this.previousCaseGroup = ast;
            }
            break;

        case EXPR:
            if (this.applyToAssignments && AstUtil.parentTypeIs(ast, LocalTokenType.SLIST)) {
                DetailAST      ass   = ast.getFirstChild();
                LocalTokenType fcltt = LocalTokenType.localize(ass.getType());
                if (
                    fcltt == LocalTokenType.ASSIGN
                    || fcltt == LocalTokenType.PLUS_ASSIGN
                    || fcltt == LocalTokenType.MINUS_ASSIGN
                    || fcltt == LocalTokenType.STAR_ASSIGN
                    || fcltt == LocalTokenType.DIV_ASSIGN
                    || fcltt == LocalTokenType.MOD_ASSIGN
                    || fcltt == LocalTokenType.SR_ASSIGN
                    || fcltt == LocalTokenType.BSR_ASSIGN
                    || fcltt == LocalTokenType.SL_ASSIGN
                    || fcltt == LocalTokenType.BAND_ASSIGN
                    || fcltt == LocalTokenType.BXOR_ASSIGN
                    || fcltt == LocalTokenType.BOR_ASSIGN
                ) {
                    this.checkTokenAlignment(this.previousAssignment, ass);
                    this.previousAssignment = ass;
                }
            }
            break;

        default:
            throw new IllegalStateException(ast.toString());
        }
    }

    private void
    checkCaseGroupLignment(DetailAST previous, DetailAST current) {
        if (previous == null) return;

        DetailAST casE = current.getFirstChild();
        if (LocalTokenType.localize(casE.getType()) != LocalTokenType.LITERAL_CASE) return;
        DetailAST slist = casE.getNextSibling();
        if (LocalTokenType.localize(slist.getType()) != LocalTokenType.SLIST) return;
        if (slist.getChildCount() == 0) return;

        this.checkTokenAlignment(
            Alignment.getLeftmostDescendant(previous.getFirstChild().getNextSibling()),
            Alignment.getLeftmostDescendant(slist.getFirstChild())
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
            this.checkTokenAlignment(
                previousDeclaration.findFirstToken(LocalTokenType.IDENT.delocalize()),
                currentDeclaration.findFirstToken(LocalTokenType.IDENT.delocalize())
            );
        }

        // Check vertical alignment of initializers.
        if (applyToInitializer) {
            this.checkTokenAlignment(
                previousDeclaration.findFirstToken(LocalTokenType.ASSIGN.delocalize()),
                currentDeclaration.findFirstToken(LocalTokenType.ASSIGN.delocalize())
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
            this.checkTokenAlignment(
                previousDefinition.findFirstToken(LocalTokenType.IDENT.delocalize()),
                currentDefinition.findFirstToken(LocalTokenType.IDENT.delocalize())
            );
        }

        // Check vertical alignment of initializers.
        if (this.applyToMethodBody) {
            this.checkTokenAlignment(
                previousDefinition.findFirstToken(LocalTokenType.SLIST.delocalize()),
                currentDefinition.findFirstToken(LocalTokenType.SLIST.delocalize())
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
            this.log(
                currentToken,
                Alignment.MESSAGE_KEY_MISALIGNED,
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
            if (
                tmp == null
                && LocalTokenType.localize(ast.getType()) == LocalTokenType.MODIFIERS
            ) tmp = ast.getNextSibling();

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

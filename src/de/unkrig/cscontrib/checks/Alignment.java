
package de.unkrig.cscontrib.checks;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static de.unkrig.cscontrib.util.AST.grandparentTypeIs;
import static de.unkrig.cscontrib.util.AST.parentTypeIs;
import static de.unkrig.cscontrib.util.AST.previousSiblingTypeIs;

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

    public int[]
    getDefaultTokens() {
        return new int[] { VARIABLE_DEF, PARAMETER_DEF, METHOD_DEF, CTOR_DEF, CASE_GROUP, EXPR };
    }

    DetailAST previousFieldDeclaration         = null;
    DetailAST previousParameterDeclaration     = null;
    DetailAST previousLocalVariableDeclaration = null;
    DetailAST previousMethodDeclaration        = null;
    DetailAST previousCaseGroup                = null;
    DetailAST previousAssignment               = null;

    public void
    visitToken(DetailAST ast) {

        @SuppressWarnings("unused") ASTDumper ad = new ASTDumper(ast);

        switch (ast.getType()) {

        case VARIABLE_DEF:
            if (
                !previousSiblingTypeIs(ast, COMMA)
                && grandparentTypeIs(ast, CLASS_DEF, INTERFACE_DEF, ENUM_DEF)
            ) {

                // First declarator in a field declaration.
                checkDeclarationAlignment(this.previousFieldDeclaration, ast, this.applyToFieldName, this.applyToFieldInitializer);
                this.previousFieldDeclaration = ast;
                return;
            }

            if (
                !previousSiblingTypeIs(ast, COMMA)
                && parentTypeIs(ast, SLIST)
            ) {

                // First declarator in a local variable declaration in block (not in a FOR initializer).
                checkDeclarationAlignment(this.previousLocalVariableDeclaration, ast, this.applyToLocalVariableName, this.applyToLocalVariableInitializer);
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
                    checkTokenAlignment(previousAssignment, ass);
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
    beginTree(DetailAST aRootAST) {
        this.previousFieldDeclaration         = null;
        this.previousParameterDeclaration     = null;
        this.previousLocalVariableDeclaration = null;
        this.previousMethodDeclaration        = null;
        this.previousCaseGroup                = null;
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
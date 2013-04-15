
package de.unkrig.cscontrib.checks;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import static de.unkrig.cscontrib.util.AST.*;

/**
 * Checks that field/parameter/variable names are aligned in one-per-line declarations.
 */
public class Alignment extends Check {

    private boolean applyToFieldName         = true;
    private boolean applyToParameterName     = true;
    private boolean applyToLocalVariableName = true;
    private boolean applyToInitializer       = true;

    public void
    setApplyToFieldName(boolean applyToFieldName) {
        this.applyToFieldName = applyToFieldName;
    }

    public void
    setApplyToParameterName(boolean applyToParameterName) {
        this.applyToParameterName = applyToParameterName;
    }

    public void
    setApplyToLocalVariableName(boolean applyToLocalVariableName) {
        this.applyToLocalVariableName = applyToLocalVariableName;
    }

    public void
    setApplyToInitializer(boolean applyToInitializer) {
        this.applyToInitializer = applyToInitializer;
    }

    public int[]
	getDefaultTokens() {
        return new int[] { VARIABLE_DEF, PARAMETER_DEF };
    }

    DetailAST previousFieldDeclaration = null;
    DetailAST firstFormalParameter = null;
    DetailAST previousVariableDeclaration = null;

    public void
    visitToken(DetailAST ast) {

        switch (ast.getType()) {

        case VARIABLE_DEF:
            if (
                this.applyToFieldName
                && !previousSiblingTypeIs(ast, COMMA)
                && grandparentTypeIs(ast, CLASS_DEF, INTERFACE_DEF, ENUM_DEF)
            ) {

                // First declarator in a field declaration.
                checkDeclarationAlignment(this.previousFieldDeclaration, ast);
                this.previousFieldDeclaration = ast;
                return;
            }

            if (
                this.applyToLocalVariableName
                && !previousSiblingTypeIs(ast, COMMA)
                && parentTypeIs(ast, SLIST)
            ) {

                // First declarator in a local variable declaration in block (not in a FOR initializer).
                checkDeclarationAlignment(this.previousVariableDeclaration, ast);
                this.previousVariableDeclaration = ast;
                return;
            }
            break;

        case PARAMETER_DEF:
            if (this.applyToParameterName) {
                if (ast.getPreviousSibling() == null) {
                    this.firstFormalParameter = ast;
                    return;
                }
                    
                // Non-first parameter declaration.
                checkDeclarationAlignment(this.firstFormalParameter, ast);
                return;
            }
            break;
        }
    }

    @Override public void
    beginTree(DetailAST aRootAST) {
        this.previousFieldDeclaration = null;
        this.firstFormalParameter = null;
        this.previousVariableDeclaration = null;
    }

    /**
     * Logs a problem iff the names of the first declarators of the two declarations are not vertically aligned.
     * <p>
     * Does nothing if {@code previousDeclaration} is {@code null}.
     */
    private void
    checkDeclarationAlignment(
        DetailAST previousDeclaration,
        DetailAST currentDeclaration
    ) {
        if (previousDeclaration == null) return;

        // Apply alignment check only to first declarator of a declaration, e.g. "int a = 3, b = 7".
        if (previousDeclaration.getLineNo() == currentDeclaration.getLineNo()) return;

        // Check vertical alignment of names.
        checkTokenAlignment(
            previousDeclaration.findFirstToken(IDENT),
            currentDeclaration.findFirstToken(IDENT)
        );

        // Check vertical alignment of initializers.
        if (this.applyToInitializer) {
            checkTokenAlignment(
                previousDeclaration.findFirstToken(ASSIGN),
                currentDeclaration.findFirstToken(ASSIGN)
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
}
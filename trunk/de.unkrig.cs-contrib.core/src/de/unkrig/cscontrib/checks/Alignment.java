
package de.unkrig.cscontrib.checks;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.ASSIGN;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.CLASS_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.COMMA;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.ENUM_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.IDENT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.INTERFACE_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.METHOD_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.PARAMETER_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.SLIST;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.VARIABLE_DEF;
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

    private boolean applyToFieldName         = true;
    private boolean applyToParameterName     = true;
    private boolean applyToLocalVariableName = true;
    private boolean applyToInitializer       = true;
    private boolean applyToMethodName        = true;
    private boolean applyToMethodBody        = true;

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
    
    public void
    setApplyToMethodName(boolean applyToMethodName) {
        this.applyToMethodName = applyToMethodName;
    }
    
    public void
    setApplyToMethodBody(boolean applyToMethodBody) {
        this.applyToMethodBody = applyToMethodBody;
    }

    public int[]
    getDefaultTokens() {
        return new int[] { VARIABLE_DEF, PARAMETER_DEF, METHOD_DEF };
    }

    DetailAST previousFieldDeclaration    = null;
    DetailAST previousFormalParameter     = null;
    DetailAST previousVariableDeclaration = null;
    DetailAST previousMethodDefinition    = null;

    public void
    visitToken(DetailAST ast) {

        @SuppressWarnings("unused") ASTDumper ad = new ASTDumper(ast);

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
                    
                // Non-first parameter declaration.
                checkDeclarationAlignment(this.previousFormalParameter, ast);
                this.previousFormalParameter = ast;
                return;
            }
            break;

        case METHOD_DEF:
            if (this.applyToMethodName) {
                checkMethodDefinitionAlignment(this.previousMethodDefinition, ast);
                this.previousMethodDefinition = ast;
            }
            break;
        }
    }

    @Override public void
    beginTree(DetailAST aRootAST) {
        this.previousFieldDeclaration = null;
        this.previousFormalParameter = null;
        this.previousVariableDeclaration = null;
    }

    /**
     * Logs a problem iff the names of the first declarators of the two declarations are not vertically aligned.
     * <p>
     * Does nothing if {@code previousDeclaration} is {@code null}.
     */
    private void
    checkDeclarationAlignment(DetailAST previousDeclaration, DetailAST currentDeclaration) {
        if (previousDeclaration == null) return;

        if (currentDeclaration.getParent() != previousDeclaration.getParent()) return;

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
    
    /**
     * Logs problems iff the names or the bodies of the two method declarations are not vertically aligned.
     * <p>
     * Does nothing if {@code firstDefinition} is {@code null}.
     */
    private void
    checkMethodDefinitionAlignment(DetailAST previousDefinition, DetailAST currentDefinition) {
        if (previousDefinition == null) return;

        @SuppressWarnings("unused") ASTDumper d = new ASTDumper(currentDefinition);

        // Check vertical alignment of names.
        checkTokenAlignment(
            previousDefinition.findFirstToken(IDENT),
            currentDefinition.findFirstToken(IDENT)
        );
        
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
}
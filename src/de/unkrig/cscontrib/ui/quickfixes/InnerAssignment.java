
package de.unkrig.cscontrib.ui.quickfixes;

import java.util.List;

import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import de.unkrig.cscontrib.ui.PluginImages;

/***/
public class InnerAssignment extends AbstractASTResolution {

    /**
     * {@inheritDoc}
     */
    protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo, final int markerStartOffset) {

        return new ASTVisitor() {
            
            @Override
            public void endVisit(Assignment node) {
                int lhsEnd   = node.getLeftHandSide().getStartPosition() + node.getLeftHandSide().getLength();
                int rhsStart = node.getRightHandSide().getStartPosition();
                if (markerStartOffset >= lhsEnd && markerStartOffset < rhsStart) {

                    // Marker begins BETWEEN the LHS and the RHS... THIS is the assignment to parenthesize!
                    replace(node, parenthesize(node));
                }
            }

            /**
             * Changes the relation between the {@code oldNode} and its parent to the {@code newNode}.
             */
            private void replace(ASTNode oldNode, ASTNode newNode) {
                ASTNode parent = oldNode.getParent();
                StructuralPropertyDescriptor location = oldNode.getLocationInParent();
                if (location.isChildProperty()) {
                    parent.setStructuralProperty(location, newNode);
                } else if (location.isChildListProperty()) {
                    @SuppressWarnings("unchecked") List<ASTNode> childList = (
                        (List<ASTNode>) parent.getStructuralProperty(location)
                    );
                    childList.set(childList.indexOf(oldNode), newNode);
                } else {
                    assert false;
                }
            }

            /** @return A parenthesized copy of the {@code expression} */
            private Expression parenthesize(Expression expression) {
                AST ast = expression.getAST();
                ParenthesizedExpression pe = ast.newParenthesizedExpression();
                pe.setExpression((Expression) ASTNode.copySubtree(ast, expression));
                return pe;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return Messages.InnerAssignmentQuickfix_description;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel() {
        return Messages.InnerAssignmentQuickfix_label;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return PluginImages.getImage(PluginImages.CORRECTION_ADD);
    }
}

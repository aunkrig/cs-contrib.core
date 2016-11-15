
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
 *    3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.unkrig.cscontrib.ui.quickfixes;

import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.ui.PluginImages;

/**
 * Parenthesizes the assignment to indicate that it is intentional.
 *
 * @cs-label Parenthesize assignment
 */
@NotNullByDefault(false) public
class InnerAssignment extends AbstractASTResolution {

    /**
     * {@inheritDoc}
     */
    @Override protected ASTVisitor
    handleGetCorrectingASTVisitor(final IRegion lineInfo, final int markerStartOffset) {

        return new ASTVisitor() {

            @Override public void
            endVisit(Assignment node) {
                int lhsEnd   = node.getLeftHandSide().getStartPosition() + node.getLeftHandSide().getLength();
                int rhsStart = node.getRightHandSide().getStartPosition();
                if (markerStartOffset >= lhsEnd && markerStartOffset < rhsStart) {

                    // Marker begins BETWEEN the LHS and the RHS... THIS is the assignment to parenthesize!
                    InnerAssignment.this.replace(node, this.parenthesize(node));
                }
            }

//            /**
//             * Changes the relation between the {@code oldNode} and its parent to the {@code newNode}.
//             */
//            private void
//            replace(ASTNode oldNode, ASTNode newNode) {
//                ASTNode                      parent   = oldNode.getParent();
//                StructuralPropertyDescriptor location = oldNode.getLocationInParent();
//
//                if (location.isChildProperty()) {
//                    parent.setStructuralProperty(location, newNode);
//                } else if (location.isChildListProperty()) {
//                    @SuppressWarnings("unchecked") List<ASTNode> childList = (
//                        (List<ASTNode>) parent.getStructuralProperty(location)
//                    );
//                    childList.set(childList.indexOf(oldNode), newNode);
//                } else {
//                    assert false;
//                }
//            }

            /** @return A parenthesized copy of the {@code expression} */
            private Expression
            parenthesize(Expression expression) {
                AST                     ast = expression.getAST();
                ParenthesizedExpression pe  = ast.newParenthesizedExpression();

                pe.setExpression((Expression) ASTNode.copySubtree(ast, expression));
                return pe;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override public String
    getDescription() { return Messages.InnerAssignmentQuickfix_description; }

    /**
     * {@inheritDoc}
     */
    @Override public String
    getLabel() { return Messages.InnerAssignmentQuickfix_label; }

    /**
     * {@inheritDoc}
     */
    @Override public Image
    getImage() { return PluginImages.getImage(PluginImages.CORRECTION_ADD); }
}

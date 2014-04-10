
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

package de.unkrig.cscontrib.ui.quickfixes;

import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.ui.PluginImages;

/***/
@NotNullByDefault(false) public
class ZeroParameterSuperconstructorInvocation extends AbstractASTResolution {

    @Override protected ASTVisitor
    handleGetCorrectingASTVisitor(final IRegion lineInfo, final int markerStartOffset) {

        return new ASTVisitor() {

            @Override public void
            endVisit(SuperConstructorInvocation node) {
//                int lhsEnd   = node.getLeftHandSide().getStartPosition() + node.getLeftHandSide().getLength();
//                int rhsStart = node.getRightHandSide().getStartPosition();
//                if (markerStartOffset >= lhsEnd && markerStartOffset < rhsStart) {
//
//                    // Marker begins BETWEEN the LHS and the RHS... THIS is the assignment to parenthesize!
//                    ZeroParameterSuperconstructorInvocation.this.replace(node, parenthesize(node));
//                }
                if (
                    ZeroParameterSuperconstructorInvocation.this.containsPosition(lineInfo, node.getStartPosition())
                    && node.arguments().isEmpty()
                ) {
                    node.delete();
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override public String
    getDescription() { return Messages.ZeroParameterSuperconstructorInvocation_description; }

    /**
     * {@inheritDoc}
     */
    @Override public String
    getLabel() { return Messages.ZeroParameterSuperconstructorInvocation_label; }

    /**
     * {@inheritDoc}
     */
    @Override public Image
    getImage() { return PluginImages.getImage(PluginImages.CORRECTION_REMOVE); }
}

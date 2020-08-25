
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import de.unkrig.commons.nullanalysis.NotNull;
import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.Activator;
import de.unkrig.cscontrib.checks.AbstractWrapCheck;

/**
 * Corrects the indentation of this line.
 *
 * @cs-label Correct indentation
 */
@NotNullByDefault(false) public
class WrapAndIndent3 extends AbstractJavaResolution {

    @Override protected boolean
    canFixMessageKey(String messageKey) {
        return AbstractWrapCheck.MESSAGE_KEY_WRONG_COLUMN.equals(messageKey);
    }

    @Override protected void
    resolve(String messageKey, Object[] arguments, @NotNull IDocument document, int markerStart, IResource resource)
    throws CoreException {

        assert AbstractWrapCheck.MESSAGE_KEY_WRONG_COLUMN.equals(messageKey);

        final String text                = (String) arguments[0];
        final int    correctColumnNumber = Integer.parseInt((String) arguments[1]) - 1;
//        int          wrongColumnNumber   = Integer.parseInt((String) arguments[2]) - 1;

        try {

            IRegion lineInformation = document.getLineInformationOfOffset(markerStart);
            String  line            = document.get(lineInformation.getOffset(), lineInformation.getLength());
            int     tokenIndex      = markerStart - lineInformation.getOffset();

            {
                String actualText = line.substring(tokenIndex, tokenIndex + text.length());
                if (!text.equals(actualText)) {
                    throw Activator.coreException("Actual text is '" + actualText + "' instead of '" + text + "'");
                }
            }

            IJavaProject javaProject = JavaCore.create(resource.getProject());

            int preceedingSpace; // Index of the whitespace before the token.
            for (preceedingSpace = tokenIndex; preceedingSpace > 0; preceedingSpace--) {
                if (!Character.isWhitespace(line.charAt(preceedingSpace - 1))) break;
            }

            String s = this.space(line.substring(0, preceedingSpace), correctColumnNumber, javaProject);
            if (s == null) {
                s = document.getLineDelimiter(0) + this.space("", correctColumnNumber, javaProject);
            }
            document.replace(
                lineInformation.getOffset() + preceedingSpace,
                tokenIndex - preceedingSpace,
                s
            );
        } catch (BadLocationException ble) {
            throw Activator.coreException(ble);
        }

    }

    @Override public String
    getDescription() { return Messages.WrapAndIndentQuickfix3_description; }

    @Override public String
    getLabel() { return Messages.WrapAndIndentQuickfix3_label; }

    @Override public Image
    getImage() { return /*PluginImages.getImage(PluginImages.CORRECTION_ADD)*/null; }
}

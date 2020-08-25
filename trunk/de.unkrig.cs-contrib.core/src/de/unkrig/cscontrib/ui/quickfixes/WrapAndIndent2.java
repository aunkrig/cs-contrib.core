
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

import net.sf.eclipsecs.core.util.CheckstyleLog;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

import de.unkrig.commons.nullanalysis.NotNull;
import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.checks.AbstractWrapCheck;

/**
 * Appends this line to the previous.
 *
 * @cs-label Join lines
 */
@NotNullByDefault(false) public
class WrapAndIndent2 extends AbstractDocumentResolution {

    @Override protected boolean
    canFixMessageKey(String messageKey) {
        return AbstractWrapCheck.MESSAGE_KEY_MUST_JOIN.equals(messageKey);
    }

    @Override protected void
    resolve(String messageKey, Object[] arguments, @NotNull IDocument document, int markerStart, IResource resource) {
        try {
            char c = 0;
            int  from;
            for (from = markerStart; from > 0; from--) {
                c = document.getChar(from - 1);
                if (!Character.isWhitespace(c)) break;
            }
            char c2 = document.getChar(markerStart);
            document.replace(
                from,
                markerStart - from,
                (
                    c == '@' || c == '(' || c == '[' || c == '.'
                    || c2 == ')' || c2 == ',' || c2 == ';' || c2 == '[' || c2 == ']' || c2 == '.'
                ) ? "" : " "
            );
        } catch (BadLocationException ble) {
            CheckstyleLog.log(ble);
        }
    }

    @Override public String
    getDescription() { return Messages.WrapAndIndentQuickfix2_description; }

    @Override public String
    getLabel() { return Messages.WrapAndIndentQuickfix2_label; }

    @Override public Image
    getImage() { return /*PluginImages.getImage(PluginImages.CORRECTION_ADD)*/null; }
}

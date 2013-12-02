
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

import de.unkrig.commons.nullanalysis.NotNull;
import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.Activator;
import de.unkrig.cscontrib.checks.WrapAndIndent;

/**
 * Quickfix for {@link WrapAndIndent#MESSAGE_KEY__0_MUST_APPEAR_IN_COLUMN_1_NOT_2}.
 */
@NotNullByDefault(false) public
class WrapAndIndent3 extends AbstractDocumentResolution {

    @Override protected boolean
    canFixMessageKey(String messageKey) {
        return WrapAndIndent.MESSAGE_KEY__0_MUST_APPEAR_IN_COLUMN_1_NOT_2.equals(messageKey);
    }

    @Override protected void
    resolve(String messageKey, Object[] arguments, @NotNull IDocument document, int markerStart) throws CoreException {

        assert WrapAndIndent.MESSAGE_KEY__0_MUST_APPEAR_IN_COLUMN_1_NOT_2.equals(messageKey);

        String text                = (String) arguments[0];
        int    correctColumnNumber = Integer.parseInt((String) arguments[1]) - 1;
//        int    wrongColumnNumber   = Integer.parseInt((String) arguments[2]) - 1;

        try {
            {
                String actualText = document.get(markerStart, text.length());
                if (!text.equals(actualText)) {
                    throw Activator.coreException("Actual text is '" + actualText + "' instead of '" + text + "'");
                }
            }

             // Zero-based

            int offset, indentation = 0;
            for (
                offset = document.getLineInformation(document.getLineOfOffset(markerStart)).getOffset();
                offset < markerStart;
                offset++
            ) {
                char c = document.getChar(offset);
                if (c == ' ') {
                    if (indentation == correctColumnNumber) break;
                    indentation++;
                } else
                if (c == '\t') {
                    int newIndentation = indentation - (indentation % 4) + 4;
                    if (newIndentation > correctColumnNumber) break;
                    indentation = newIndentation;
                } else
                {
                    break;
                }
            }

            String s;
            if (indentation == correctColumnNumber) {
                s = "";
            } else {
                StringBuilder sb = new StringBuilder();
                for (; indentation < correctColumnNumber; indentation++) sb.append(' ');
                s = sb.toString();
            }

            document.replace(offset, markerStart - offset, s);
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

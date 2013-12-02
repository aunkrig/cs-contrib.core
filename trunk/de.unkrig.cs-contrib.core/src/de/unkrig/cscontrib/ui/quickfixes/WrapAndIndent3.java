
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

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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

            IRegion lineInformation = document.getLineInformationOfOffset(markerStart);
            String  line            = document.get(lineInformation.getOffset(), lineInformation.getLength());
            int     tokenIndex      = markerStart - lineInformation.getOffset();

            {
                String actualText = line.substring(tokenIndex, tokenIndex + text.length());
                if (!text.equals(actualText)) {
                    throw Activator.coreException("Actual text is '" + actualText + "' instead of '" + text + "'");
                }
            }

            int     indentation        = 0;
            boolean tokenIsFirstInLine = true;
            int     i;
            for (i = 0; i < tokenIndex; i++) {
                char c = line.charAt(i);
                if (c == ' ') {
                    indentation++;
                } else
                if (c == '\t') {
                    indentation = indentation - (indentation % 4) + 4;
                } else
                {
                    tokenIsFirstInLine = false;
                    break;
                }
            }

            if (tokenIsFirstInLine) {
                document.replace(lineInformation.getOffset(), i, string(' ', correctColumnNumber));
            } else {
                document.replace(markerStart, 0, document.getLineDelimiter(0) + string(' ', correctColumnNumber));
            }
        } catch (BadLocationException ble) {
            throw Activator.coreException(ble);
        }

    }

    private static String
    string(char c, int n) {
        if (n < 80) {
            return "                                                                                ".substring(0, n);
        }
        char[] ca = new char[n];
        Arrays.fill(ca, c);
        return new String(ca);
    }

    @Override public String
    getDescription() { return Messages.WrapAndIndentQuickfix3_description; }

    @Override public String
    getLabel() { return Messages.WrapAndIndentQuickfix3_label; }

    @Override public Image
    getImage() { return /*PluginImages.getImage(PluginImages.CORRECTION_ADD)*/null; }
}

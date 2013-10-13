
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

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Arrays;

import net.sf.eclipsecs.core.util.CheckstyleLog;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import de.unkrig.commons.nullanalysis.NotNull;
import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.checks.WrapAndIndent;

/**
 * Quickfix for {@link WrapAndIndent#MESSAGE_KEY__0_MUST_APPEAR_IN_COLUMN_1_NOT_2}.
 */
@NotNullByDefault(false) public
class WrapAndIndent3 extends AbstractDocumentResolution {

    private static final MessageFormat
    MF = new MessageFormat(WrapAndIndent.MESSAGE_KEY__0_MUST_APPEAR_IN_COLUMN_1_NOT_2);

    @Override protected boolean
    canFixMessageKey(String messageKey) {
        return WrapAndIndent.MESSAGE_KEY__0_MUST_APPEAR_IN_COLUMN_1_NOT_2.equals(messageKey);
    }

    @Override protected void
    resolve(String messageKey, @NotNull IDocument document, int markerStart) {
        System.err.println("messageKey=" + messageKey);
        try {
            Object[] result = MF.parse(messageKey);
            System.err.println("result=" + Arrays.toString(result));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // TODO: This code always WRAPS, and never INDENTS or UNINDENTS.
        try {
            int lineNumber = document.getLineOfOffset(markerStart); // Zero-based
            System.err.println("lineNumber=" + lineNumber);
            if (lineNumber >= 1) {
                IRegion li1 = document.getLineInformation(lineNumber - 1);
                System.err.println("li1=" + li1);
                IRegion li2 = document.getLineInformation(lineNumber);
                System.err.println("li2=" + li2);

                int offset1;
                for (offset1 = li1.getOffset(); offset1 < li1.getOffset() + li1.getLength(); offset1++) {
                    if (!Character.isWhitespace(document.getChar(offset1))) break;
                }
                
                int offset2;
                for (offset2 = li2.getOffset(); offset2 < li2.getOffset() + li2.getLength(); offset2++) {
                    if (!Character.isWhitespace(document.getChar(offset2))) break;
                }

                document.replace(
                    li2.getOffset(),
                    offset2 - li2.getOffset(),
                    document.get(li1.getOffset(), offset1 - li1.getOffset())
                );
            }
        } catch (BadLocationException ble) {
            CheckstyleLog.log(ble);
        }

    }

    @Override public String
    getDescription() { return Messages.WrapAndIndentQuickfix3_description; }

    @Override public String
    getLabel() { return Messages.WrapAndIndentQuickfix3_label; }

    @Override public Image
    getImage() { return /*PluginImages.getImage(PluginImages.CORRECTION_ADD)*/null; }
}

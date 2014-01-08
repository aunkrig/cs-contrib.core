
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

package de.unkrig.cscontrib.checks;

import org.apache.commons.beanutils.ConversionException;

import com.google.common.collect.ImmutableMap;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.checks.AbstractFormatCheck;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

@NotNullByDefault(false) public
class CppCommentAlignment extends AbstractFormatCheck {

    public
    CppCommentAlignment() throws ConversionException { super("^[\\s\\}\\);]*$"); }

    @Override public int[]
    getDefaultTokens() { return new int[0]; }

    @Override public void
    visitToken(DetailAST ast) { throw new IllegalStateException("visitToken() shouldn't be called."); }

    @Override public void
    beginTree(DetailAST ast) {
        String[]                         lines       = getFileContents().getLines();
        ImmutableMap<Integer, TextBlock> cppComments = getFileContents().getCppComments();

        int previousLineCppCommentColNo = -1;
        for (int lineNo = 0; lineNo < lines.length; lineNo++) {
            TextBlock cppComment = cppComments.get(lineNo);
            if (cppComment == null) {
                previousLineCppCommentColNo = -1;
            } else {
                int cppCommentColNo = cppComment.getStartColNo();
                if (
                    cppCommentColNo != 0
                    && previousLineCppCommentColNo != -1
                    && cppCommentColNo != previousLineCppCommentColNo
                ) {
                    log(lineNo, cppCommentColNo, "C++ comment is misaligned");
                }
                previousLineCppCommentColNo = cppCommentColNo;
            }
        }
    }
}

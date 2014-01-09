
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConversionException;

import com.google.common.collect.ImmutableMap;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.checks.AbstractFormatCheck;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

/**
 * Checks whether C++ comments are correctly aligned.
 * <p>
 * C++ comments must appear on the same column iff
 * <ul>
 *   <li>They appear in immediately consecutive lines
 *   <li>All of these lines are of the same {@link LineKind kind}
 * </ul>
 */
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

        LINES:
        for (int lineNo = 0;;) {
            int           maxCppCommentColNo;
            LineKind      lineKind;
            List<Integer> cppCommentColNos = new ArrayList<Integer>();

            // Find next C++ comment.
            for (;; lineNo++) {

                if (lineNo >= lines.length) break LINES;

                TextBlock cppComment = cppComments.get(lineNo + 1);
                if (cppComment != null) {
                    maxCppCommentColNo = cppComment.getStartColNo();
                    lineKind           = getLineKind(lines[lineNo]);
                    cppCommentColNos.add(maxCppCommentColNo);
                    break;
                }
            }

            // Find C++ comments in consecutive lines of same kind.
            for (lineNo++; lineNo < lines.length; lineNo++) {

                TextBlock cppComment = cppComments.get(lineNo + 1);
                if (cppComment == null) break;

                if (getLineKind(lines[lineNo]) != lineKind) break;

                int cppCommentColNo = cppComment.getStartColNo();
                if (cppCommentColNo > maxCppCommentColNo) maxCppCommentColNo = cppCommentColNo;
                cppCommentColNos.add(cppCommentColNo);
            }

            // Log misaligned C++ comments.
            for (int i = 0; i < cppCommentColNos.size(); i++) {
                int cppCommentColNo = cppCommentColNos.get(i);
                if (cppCommentColNo != maxCppCommentColNo) {
                    log(
                        lineNo - cppCommentColNos.size() + i + 1,
                        cppCommentColNo,
                        "C++ comment must appear on column {0}, not {1}",
                        maxCppCommentColNo + 1,
                        cppCommentColNo + 1
                    );
                }
            }
        }
    }

    /** @see {@link #BARE_CPP_COMMENT}, {@link #SWITCH_LABEL_AND_CPP_COMMENT}, {@link #OTHER} */
    enum LineKind {

        /** A line which contains only a C++ comment. */
        BARE_CPP_COMMENT,

        /** A line which contains only a switch label ('case x:' or 'default:') and a C++ comment. */
        SWITCH_LABEL_AND_CPP_COMMENT,

        /** Any other line. */
        OTHER,
    }

    private LineKind
    getLineKind(String line) {
        return (
            BARE_CPP_COMMENT_LINE_PATTERN.matcher(line).matches()             ? LineKind.BARE_CPP_COMMENT :
            SWITCH_LABEL_AND_CPP_COMMENT_LINE_PATTERN.matcher(line).matches() ? LineKind.SWITCH_LABEL_AND_CPP_COMMENT :
            LineKind.OTHER
        );
    }
    private static final Pattern
    BARE_CPP_COMMENT_LINE_PATTERN = Pattern.compile("\\s*//.*");
    private static final Pattern
    SWITCH_LABEL_AND_CPP_COMMENT_LINE_PATTERN = Pattern.compile("\\s*(?:case\\b[^:]*|default\\s*):\\s*//.*");
}

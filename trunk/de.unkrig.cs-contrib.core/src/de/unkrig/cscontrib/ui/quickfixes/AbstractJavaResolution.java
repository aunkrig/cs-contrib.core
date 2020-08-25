
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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.commons.nullanalysis.Nullable;

/**
 * Abstract base class for marker resolutions for JAVA sources.
 */
@NotNullByDefault(false) public abstract
class AbstractJavaResolution extends AbstractDocumentResolution {

    /**
     * Computes a string consisting of TABs and/or SPACEs (depending on the {@code javaProject}'s FORMATTER_TAB_CHAR
     * option) such that the {@code prefix} plus the string reaches the {@code correctColumnNumber}.
     * <p>
     * Example (assuming the tab size is 4, FORMATTER_TAB_CHAR is MIXED, and {@code correctColumnNumber} is 9):
     * <p>
     * Prefix "ab" + string "\t\t " reaches column number 9
     *
     * @return {@code null} iff the {@code prefix} is "too long".
     */
    @Nullable public String
    space(String prefix, int correctColumnNumber, IJavaProject javaProject) {

        if (correctColumnNumber < prefix.length()) return null;

        int tabWidth = AbstractJavaResolution.getCoreOption(
            javaProject,
            DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE,
            4
        );

        int cn = 0;
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (c == '\t') {
                cn = cn - (cn % tabWidth) + tabWidth;
            } else {
                cn++;
            }
            if (cn > correctColumnNumber) return null;
        }

        String tabChar = AbstractJavaResolution.getCoreOption(
            javaProject,
            DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR
        );

        StringBuilder sb = new StringBuilder();
        if (JavaCore.SPACE.equals(tabChar)) {
            for (; cn < correctColumnNumber; cn++) sb.append(' ');
        } else
        if (JavaCore.TAB.equals(tabChar)) {
            for (; cn < correctColumnNumber; cn = cn - (cn % tabWidth) + tabWidth) sb.append('\t');
        } else
        if (DefaultCodeFormatterConstants.MIXED.equals(tabChar)) {
            while (cn < correctColumnNumber) {
                int newCn = cn - (cn % tabWidth) + tabWidth;
                if (newCn > correctColumnNumber) break;
                sb.append('\t');
                cn = newCn;
            }
            for (; cn < correctColumnNumber; cn++) sb.append(' ');
        } else
        {
            for (; cn < correctColumnNumber; cn++) sb.append(' ');
        }

        return sb.toString();
    }

    /** @return The (possibly {@code javaProject}-specific) core preference defined under {@code key} */
    private static String
    getCoreOption(IJavaProject javaProject, String key) {
        return javaProject == null ? JavaCore.getOption(key) : javaProject.getOption(key, true);
    }

    /**
     * @return The (possibly {@code javaProject}-specific) core preference defined under {@code key}, or {@code
     *         defaulT} if the value is not a integer
     */
    private static int
    getCoreOption(IJavaProject javaProject, String key, int defaulT) {
        try {
            return Integer.parseInt(AbstractJavaResolution.getCoreOption(javaProject, key));
        } catch (NumberFormatException e) {
            return defaulT;
        }
    }
}

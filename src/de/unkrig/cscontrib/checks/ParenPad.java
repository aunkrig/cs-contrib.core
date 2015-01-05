
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

import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConversionException;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.checks.whitespace.PadOption;
import com.puppycrawl.tools.checkstyle.checks.whitespace.ParenPadCheck;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

/**
 * Enhanced version of "ParenPad": NOSPACE now allows "{@code ( // ...}".
 * <p>
 *   <span style="color: red"><b>This check is superseded by {@code de.unkrig.Whitespace}.</b></span>
 * </p>
 *
 * @cs-rule-group  %Whitespace.group
 * @cs-rule-name   de.unkrig: Parenthesis padding
 * @cs-rule-parent TreeWalker
 */
@NotNullByDefault(false) public
class ParenPad extends ParenPadCheck {

    /** @cs-message ''{0}'' is followed by whitespace */
    public static final String
    MESSAGE_KEY_FOLLOWED_BY_WHITESPACE = "ParenPad.followedByWhitespace";

    /** @cs-message ''{0}'' is not followed by whitespace */
    public static final String
    MESSAGE_KEY_NOT_FOLLOWED_BY_WHITESPACE = "ParenPad.notFollowedByWhitespace";

    private static final Pattern NOSPACE_PATTERN = Pattern.compile(
        "[^\\s].*"   // '(' + non-space
        + "|"
        + "\\s*//.*" // '(' + space + end-of-line-comment
        + "|"
        + ""         // '(' + line-break
    );
    private static final Pattern SPACE_PATTERN = Pattern.compile(
        "\\s.*"    // '(' + Space
        + "|"
        + "\\).*"  // '(' + ')'
        + "|"
        + ""       // '(' + line-break
    );


    /**
     * Whether space is required or forbidden.
     *
     * @cs-property-name            option
     * @cs-property-default-value   nospace
     * @cs-property-option-provider com.puppycrawl.tools.checkstyle.checks.whitespace.PadOption
     */
    @Override public void
    setOption(String option) throws ConversionException { super.setOption(option); }

    @Override protected void
    processLeft(DetailAST ast) {
        final String line  = this.getLines()[ast.getLineNo() - 1];
        final int    after = ast.getColumnNo() + 1;

        String rest = line.substring(after);
        if (
            this.getAbstractOption() == PadOption.NOSPACE
            && !ParenPad.NOSPACE_PATTERN.matcher(rest).matches()
        ) {
            this.log(ast.getLineNo(), after, ParenPad.MESSAGE_KEY_FOLLOWED_BY_WHITESPACE, "(");
        } else if (
            this.getAbstractOption() == PadOption.SPACE
            && !ParenPad.SPACE_PATTERN.matcher(rest).matches()
        ) {
            this.log(ast.getLineNo(), after, ParenPad.MESSAGE_KEY_NOT_FOLLOWED_BY_WHITESPACE, "(");
        }
    }
}

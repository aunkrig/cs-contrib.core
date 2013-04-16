
package de.unkrig.cscontrib.checks;

import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.checks.whitespace.*;

/**
 * Enhanced version of "ParenPad": NOSPACE now allows '( // ...'.
 */
public class ParenPad extends ParenPadCheck {

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

    @Override
    protected void processLeft(DetailAST ast) {
        final String line  = getLines()[ast.getLineNo() - 1];
        final int    after = ast.getColumnNo() + 1;

        String rest = line.substring(after);
        if (
            getAbstractOption() == PadOption.NOSPACE
            && !NOSPACE_PATTERN.matcher(rest).matches()
        ) {
            log(ast.getLineNo(), after, "''{0}'' is followed by whitespace", "(");
        } else if (
            getAbstractOption() == PadOption.SPACE
            && !SPACE_PATTERN.matcher(rest).matches()
        ) {
            log(ast.getLineNo(), after, "''{0}'' is not followed by whitespace", "(");
        }
    }
}

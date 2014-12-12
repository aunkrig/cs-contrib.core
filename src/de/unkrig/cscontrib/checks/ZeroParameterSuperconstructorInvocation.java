
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

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;

/**
 * Checks for redundant zero-parameter superconstructor invocations:
 * <pre>
 * class Foo extends Bar {
 *     Foo(int a, int b) {
 *         super(); // <===
 *     }
 * }</pre>
 */
@NotNullByDefault(false) public
class ZeroParameterSuperconstructorInvocation extends Check {

    @Override public int[]
    getDefaultTokens() { return new int[] { LocalTokenType.CTOR_DEF.delocalize() }; }

    @Override public void
    visitToken(DetailAST ast) {
        assert ast != null;

        @SuppressWarnings("unused") AstDumper dumper = new AstDumper(ast);

        // Find the constructor body.
        DetailAST statementList = ast.findFirstToken(LocalTokenType.SLIST.delocalize());

        // Find the superconstructor call.
        DetailAST superconstructorCall = statementList.findFirstToken(LocalTokenType.SUPER_CTOR_CALL.delocalize());
        if (superconstructorCall == null) return;

        // Check whether this is a qualified SUPER call.
        DetailAST lparen = superconstructorCall.getFirstChild();
        if (lparen.getType() != LocalTokenType.LPAREN.delocalize()) return;

        // Find the argument list.
        DetailAST arguments = lparen.getNextSibling();

        // Determine the argument count.
        int argumentCount = arguments.getChildCount(LocalTokenType.EXPR.delocalize());

        // Complain about redundant zero-parameter superconstructor invocation.
        if (argumentCount == 0) {
            this.log(superconstructorCall, "Redundant invocation of zero-parameter superconstructor");
        }
    }
}

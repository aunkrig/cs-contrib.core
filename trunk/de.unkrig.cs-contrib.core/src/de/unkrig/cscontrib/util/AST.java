
/*
 * cs-contrib - Additional checks, filters and quickfixes for CheckStyle and Eclipse-CS
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

package de.unkrig.cscontrib.util;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

/**
 * Utility methods related to CHECKSTYLE's DetailAST model.
 */
public
class AST {

    private
    AST() {}

    public static boolean
    grandparentTypeIs(DetailAST ast, int... types) {
        int grandparentType = ast.getParent().getParent().getType();
        for (int type : types) {
            if (grandparentType == type) return true;
        }
        return false;
    }

    public static boolean
    parentTypeIs(DetailAST ast, int type) {
        DetailAST parent = ast.getParent();

        return parent.getType() == type;
    }

    public static boolean
    nextSiblingTypeIs(DetailAST ast, int type) {
        DetailAST nextSibling = ast.getNextSibling();

        return nextSibling != null && nextSibling.getType() == type;
    }

    public static boolean
    firstChildTypeIs(DetailAST ast, int type) {
        DetailAST firstChild = ast.getFirstChild();

        return firstChild != null && firstChild.getType() == type;
    }

    public static boolean
    previousSiblingTypeIs(DetailAST ast, int type) {
        DetailAST previousSibling = ast.getPreviousSibling();

        return previousSibling != null && previousSibling.getType() == type;
    }
}

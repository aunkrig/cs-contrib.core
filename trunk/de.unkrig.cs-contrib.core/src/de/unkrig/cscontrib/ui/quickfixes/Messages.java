
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

import org.eclipse.osgi.util.NLS;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

/** Message bundle for this package. */
@NotNullByDefault(false) public final
class Messages extends NLS {

    private
    Messages() {}

    private static final String BUNDLE_NAME = "de.unkrig.cscontrib.ui.quickfixes.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(Messages.BUNDLE_NAME, Messages.class);
    }

    // CHECKSTYLE StaticVariableNameCheck:OFF
    // CHECKSTYLE VariableCheck:OFF
    public static String InnerAssignmentQuickfix_description;
    public static String InnerAssignmentQuickfix_label;

    public static String WrapAndIndentQuickfix1_description;
    public static String WrapAndIndentQuickfix1_label;
    public static String WrapAndIndentQuickfix2_description;
    public static String WrapAndIndentQuickfix2_label;
    public static String WrapAndIndentQuickfix3_description;
    public static String WrapAndIndentQuickfix3_label;

    public static String ZeroParameterSuperconstructorInvocation_description;
    public static String ZeroParameterSuperconstructorInvocation_label;
    // CHECKSTYLE StaticVariableNameCheck:ON
    // CHECKSTYLE VariableCheck:ON
}

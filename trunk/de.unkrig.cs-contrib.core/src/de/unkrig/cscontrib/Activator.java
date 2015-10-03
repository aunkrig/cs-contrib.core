
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

package de.unkrig.cscontrib;

import java.util.Dictionary;

import net.sf.eclipsecs.ui.CheckstyleUIPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

/**
 * 'Bundle-Activator' of this plugin.
 */
@NotNullByDefault(false) public
class Activator extends AbstractUIPlugin {

    /**
     * The unique ID of this plug-in.
     */
    public static final String PLUGIN_ID = "de.unkrig.cs-contrib.core";

    @Override public void
    start(BundleContext context) throws Exception {
        super.start(context);

        // ECLIPSECS quickfixes are broken, because the 'net.sf.eclipsecs.ui' plugin lacks two specific entries in its
        // manifest. Effectively, 'net.sf.eclipsecs.ui' is unable to load OTHER plugins' quickfixes
        // (ClassNotFoundException), and produces ugly messages in the error log ('Checkstyle-Plugin:
        // de.unkrig.cscontrib.ui.quickfixes.InnerAssignment'), because 'net.sf.eclipsecs.ui' fails to allow other
        // plugin to register themselves as buddies, and also fails to export some Java packages to these buddies.
        //
        // This problem was diagnosed in ECLIPSECS 5.1.0, and is fixed in ECLIPSECS 5.3.0.
        //
        // Check these conditions here and issue a warning message that motivates users to patch their
        // 'net.sf.eclipsecs.ui' plugin's manifest appropriately.
        Dictionary<?, ?> headers = Platform.getBundle(CheckstyleUIPlugin.PLUGIN_ID).getHeaders();
        if (headers.get("Export-Package") == null || !"registered".equals(headers.get("Eclipse-BuddyPolicy"))) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override public void
                run() {
                    MessageDialog.openWarning(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Activator.PLUGIN_ID,
                        (
                            "The 'net.sf.eclipsecs.ui' plugin has a bug that thwarts the quickfixes of "
                            + "'de.unkrig.cscontrib' from functioning. The manifest of the 'net.sf.eclipsecs.ui' "
                            + "plugin must be patched to contain the following lines:\n"
                            + "\n"
                            + "  Export-Package: net.sf.eclipsecs.ui,\n"
                            + "    net.sf.eclipsecs.ui.properties.filter,\n"
                            + "    net.sf.eclipsecs.ui.quickfixes\n"
                            + "  Eclipse-BuddyPolicy: registered"
                        )
                    );
                }
            });
        }
    }

    /** @return A {@link CoreException} repreenting the {@code message} */
    public static CoreException
    coreException(String message) {
        return new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
    }

    /** @return A {@link CoreException} repreenting the {@link Throwable} */
    public static CoreException
    coreException(Throwable t) {
        return new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, t));
    }

    /** @return A {@link CoreException} repreenting the {@code message} and {@link Throwable} */
    public static CoreException
    coreException(String message, Throwable t) {
        return new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, t));
    }
}

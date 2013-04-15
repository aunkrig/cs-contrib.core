
package de.unkrig.cscontrib;

import java.util.*;

import net.sf.eclipsecs.ui.CheckstyleUIPlugin;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.*;

/**
 * 'Bundle-Activator' of this plugin.
 */
public class Activator extends AbstractUIPlugin {

	/** xyz. @copyright abc */
    enum xFoo { BAR }

    public static final String PLUGIN_ID = "de.unkrig.cs-contrib.core";

    @Override
    public void start(BundleContext context) throws Exception {
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
                
                public void run() {
                    MessageDialog.openWarning(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        PLUGIN_ID,
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
}

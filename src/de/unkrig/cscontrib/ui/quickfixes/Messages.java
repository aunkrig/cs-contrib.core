
package de.unkrig.cscontrib.ui.quickfixes;

import org.eclipse.osgi.util.NLS;

/** Message bundle for this package. */
public class Messages extends NLS {
    private Messages() {}
    
    private static final String BUNDLE_NAME = "de.unkrig.cscontrib.ui.quickfixes.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

// CHECKSTYLE xNameSpelling:OFF
    public static String InnerAssignmentQuickfix_description;
    public static String InnerAssignmentQuickfix_label;
// CHECKSTYLE NameSpelling:ON
}

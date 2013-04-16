
package de.unkrig.cscontrib.ui;

import java.util.HashMap;
import java.util.Map;

import net.sf.eclipsecs.ui.CheckstyleUIPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Utility class that manages this plugin's images.
 */
public abstract class PluginImages {

    private
    PluginImages() {}

    private static final Map<ImageDescriptor, Image> CACHED_IMAGES = new HashMap<ImageDescriptor, Image>();

    public static final ImageDescriptor CORRECTION_ADD;

    static {
        CORRECTION_ADD = AbstractUIPlugin.imageDescriptorFromPlugin(
            CheckstyleUIPlugin.PLUGIN_ID,
            "icons/add_correction.gif"
        );
    }

    /**
     * Gets an image from a given descriptor.
     * 
     * @param descriptor the descriptor
     * @return the image
     */
    public static Image
    getImage(ImageDescriptor descriptor) {

        Image image = CACHED_IMAGES.get(descriptor);
        if (image == null) {
            image = descriptor.createImage();
            CACHED_IMAGES.put(descriptor, image);
        }
        return image;
    }

    /**
     * Disposes the cached images and clears the cache.
     */
    public static void
    clearCachedImages() {
        for (Image image : CACHED_IMAGES.values()) image.dispose();

        CACHED_IMAGES.clear();
    }
}
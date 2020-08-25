
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
public abstract
class PluginImages {

    private
    PluginImages() {}

    private static final Map<ImageDescriptor, Image> CACHED_IMAGES = new HashMap<ImageDescriptor, Image>();

    /**
     * An icon that visualized the addition of an element.
     */
    public static final ImageDescriptor CORRECTION_ADD = PluginImages.getImageDescriptor("icons/add_correction.gif");

    /**
     * An icon that visualized the removal of an element.
     */
    public static final ImageDescriptor
    CORRECTION_REMOVE = PluginImages.getImageDescriptor("icons/remove_correction.gif");

    private static ImageDescriptor
    getImageDescriptor(String name) {
        ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
            CheckstyleUIPlugin.PLUGIN_ID,
            name
        );
        assert imageDescriptor != null : "Image '" + name + "' could not be found";
        return imageDescriptor;
    }

    /**
     * Gets an image from a given descriptor.
     *
     * @param descriptor the descriptor
     * @return the image
     */
    public static Image
    getImage(ImageDescriptor descriptor) {

        Image image = PluginImages.CACHED_IMAGES.get(descriptor);
        if (image == null) {
            image = descriptor.createImage();
            assert image != null : "Image could not be created from descriptor '" + descriptor + "'";
            PluginImages.CACHED_IMAGES.put(descriptor, image);
        }
        return image;
    }

    /**
     * Disposes the cached images and clears the cache.
     */
    public static void
    clearCachedImages() {
        for (Image image : PluginImages.CACHED_IMAGES.values()) image.dispose();

        PluginImages.CACHED_IMAGES.clear();
    }
}

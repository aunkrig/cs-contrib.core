
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

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.quickfixes.ICheckstyleMarkerResolution;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

import de.unkrig.commons.nullanalysis.NotNull;
import de.unkrig.commons.nullanalysis.NotNullByDefault;

/**
 * Abstract base class for marker resolutions through document modification.
 */
@NotNullByDefault(false) public abstract
class AbstractDocumentResolution extends WorkbenchMarkerResolution implements ICheckstyleMarkerResolution {

    private boolean      autoCommit;
    private RuleMetadata metadata;

    // ICheckstyleMarkerResolution declares this method AFTER version 5.6.0.
    // SUPPRESS CHECKSTYLE JavadocMethod
    @Override @SuppressWarnings("all") /**@Override*/ public void
    setRuleMetaData(RuleMetadata metadata) {
        this.metadata = metadata;
    }

    @Override public void
    setAutoCommitChanges(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    @Override public boolean
    canFix(IMarker marker) {

        try {
            if (!CheckstyleMarker.MARKER_ID.equals(marker.getType())) return false;
        } catch (CoreException e) {
            throw new IllegalStateException(e);
        }

        String moduleName = marker.getAttribute(CheckstyleMarker.MODULE_NAME, null);
        return (
            (
                this.metadata == null // Only ECLIPSE-CS > 5.6.0 sets the metadata!
                || this.metadata.getInternalName().equals(moduleName)
                || this.metadata.getAlternativeNames().contains(moduleName)
            )
            && this.canFixMessageKey(marker.getAttribute(CheckstyleMarker.MESSAGE_KEY, null))
        );
    }

    /**
     * If the checks produces different markers and the quickfix is applicable to only part of them, then the
     * quickfix must override this method.
     *
     * @param messageKey As specified in 'checkstyle-metadata.xml' in element 'message-key'
     * @return           Whether this quickfix is applicable or not
     */
    protected boolean
    canFixMessageKey(String messageKey) {
        return true;
    }

    @Override public Image
    getImage() { return null; }

    @Override public IMarker[]
    findOtherMarkers(IMarker[] markers) {

        Set<IMarker> candidates = new HashSet<IMarker>();

        for (IMarker m : markers) {

            if (this.canFix(m)) {
                candidates.add(m);
            }
        }

        return candidates.toArray(new IMarker[candidates.size()]);
    }

    @Override public void
    run(IMarker marker) {

        IPath path;
        {
            IResource resource = marker.getResource();
            if (!(resource instanceof IFile)) return;
            path = resource.getLocation();
        }

        ITextFileBufferManager bufferManager = null;
        try {
            bufferManager = FileBuffers.getTextFileBufferManager();
            bufferManager.connect(path, LocationKind.NORMALIZE, null);

            ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.NORMALIZE);

            IAnnotationModel annotationModel = textFileBuffer.getAnnotationModel();
            assert annotationModel != null : "Text file buffer is disconnected";

            MarkerAnnotation annotation = this.getMarkerAnnotation(annotationModel, marker);

            if (annotation == null) return;

            String   messageKey;
            Object[] arguments;
            {
                messageKey = marker.getAttribute(CheckstyleMarker.MESSAGE_KEY, null);

                String message;
                {
                    Object o = marker.getAttribute("message");
                    if (!(o instanceof String)) return;
                    message = (String) o;
                }

                MessageFormat messageFormat = new MessageFormat(messageKey);
                arguments = messageFormat.parse(message, new ParsePosition(0));
                if (arguments == null) {
                    int idx = message.indexOf(": ");
                    if (idx != -1) {
                        arguments = messageFormat.parse(message.substring(idx + 2), new ParsePosition(0));
                    }
                }
            }

            // Invoke the quickfix.
            IDocument document = textFileBuffer.getDocument();
            assert document != null;
            this.resolve(
                messageKey,
                arguments,
                document,
                annotationModel.getPosition(annotation).getOffset(),
                marker.getResource()
            );

            annotation.markDeleted(true);

            // commit changes to underlying file
            if (this.autoCommit) textFileBuffer.commit(new NullProgressMonitor(), false);
        } catch (CoreException e) {
            CheckstyleLog.log(e, Messages.AbstractASTResolution_msgErrorQuickfix);
        } catch (MalformedTreeException e) {
            CheckstyleLog.log(e, Messages.AbstractASTResolution_msgErrorQuickfix);
        } finally {

            if (bufferManager != null) {
                try {
                    bufferManager.disconnect(path, LocationKind.NORMALIZE, null);
                } catch (CoreException e) {
                    CheckstyleLog.log(e, "Error processing quickfix"); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * @param messageKey  Identifies the event that needs to be fixed
     * @param arguments   The argument values in the message
     * @param document    The document that needs to be modified by the quickfix
     * @param markerStart The offset within the document where the event occurred
     * @param resource    The underlying resource
     */
    protected abstract void
    resolve(String messageKey, Object[] arguments, @NotNull IDocument document, int markerStart, IResource resource)
    throws CoreException;

    /**
     * @return The annotation related to the given {@code marker}, or {@code null}
     */
    private MarkerAnnotation
    getMarkerAnnotation(@NotNull IAnnotationModel annotationModel, @NotNull IMarker marker) {

        for (
            @SuppressWarnings("unchecked") Iterator<Annotation> it = annotationModel.getAnnotationIterator();
            it.hasNext();
        ) {
            Annotation annotation = it.next();

            if (annotation instanceof MarkerAnnotation) {
                MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
                if (markerAnnotation.getMarker().equals(marker)) return markerAnnotation;
            }
        }
        return null;
    }
}

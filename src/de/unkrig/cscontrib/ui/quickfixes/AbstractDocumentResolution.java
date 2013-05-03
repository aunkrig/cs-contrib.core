//============================================================================
//
// Copyright (C) 2002-2012  David Schneider, Lars K�dderitzsch
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package de.unkrig.cscontrib.ui.quickfixes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

/**
 * Abstract base class for marker resolutions through document modification.
 */
public abstract
class AbstractDocumentResolution extends WorkbenchMarkerResolution implements ICheckstyleMarkerResolution {

    private boolean      autoCommit;
    private RuleMetadata metadata;

    public void
    setRuleMetaData(RuleMetadata metadata) {
        this.metadata = metadata;
    }

    public void
    setAutoCommitChanges(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public boolean
    canFix(IMarker marker) {

        String moduleName = marker.getAttribute(CheckstyleMarker.MODULE_NAME, null);
        try {
            return (
                CheckstyleMarker.MARKER_ID.equals(marker.getType())
                && (metadata.getInternalName().equals(moduleName) || metadata.getAlternativeNames().contains(moduleName))
                && this.canFixMessageKey(marker.getAttribute(CheckstyleMarker.MESSAGE_KEY, null))
            );
        } catch (CoreException e) {
            throw new IllegalStateException(e);
        }
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

    public Image
    getImage() { return null; }

    @Override public IMarker[]
    findOtherMarkers(IMarker[] markers) {

        Set<IMarker> candidates = new HashSet<IMarker>();

        for (IMarker m : markers) {

            if (canFix(m)) {
                candidates.add(m);
            }
        }

        return candidates.toArray(new IMarker[candidates.size()]);
    }

    public void
    run(IMarker marker) {

        IPath path;
        {
            IResource resource = marker.getResource();
            if (!(resource instanceof IFile)) return;
            path = resource.getLocation();
        }

        ITextFileBufferManager bufferManager = null;
        try {
            @SuppressWarnings("unused") Map<String, Object> attributes = marker.getAttributes(); // TODO: DEBUG

            IProgressMonitor monitor = new NullProgressMonitor();

            bufferManager = FileBuffers.getTextFileBufferManager();
            bufferManager.connect(path, LocationKind.NORMALIZE, null);

            ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.NORMALIZE);

            IDocument        document        = textFileBuffer.getDocument();
            IAnnotationModel annotationModel = textFileBuffer.getAnnotationModel();

            MarkerAnnotation annotation = getMarkerAnnotation(annotationModel, marker);

            if (annotation == null) return;

            // Invoke the quickfix.
            this.resolve(
                marker.getAttribute(CheckstyleMarker.MESSAGE_KEY, null),
                document,
                annotationModel.getPosition(annotation).getOffset()
            );

            annotation.markDeleted(true);

            // commit changes to underlying file
            if (this.autoCommit) textFileBuffer.commit(monitor, false);
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

    protected abstract void
    resolve(String messageKey, IDocument document, int markerStart);

    private MarkerAnnotation
    getMarkerAnnotation(IAnnotationModel annotationModel, IMarker marker) {

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

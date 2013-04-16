
package de.unkrig.cscontrib.filters;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.beanutils.ConversionException;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.api.*;
import com.puppycrawl.tools.checkstyle.checks.FileContentsHolder;

/***/
public class SuppressionLine extends AutomaticBean implements Filter {

    /**
     * A Tag holds a magic line and its location, and determines whether the suppression turns CHECKSTYLE reporting on
     * or off.
     */
    public class Tag implements Comparable<Tag> {

        /** The text of the tag. */
        private final String mText;

        /** The line number of the tag. */
        private final int mLineNo;

        /** Determines whether the suppression turns checkstyle reporting on. */
        private final boolean on;

        /** The parsed check regex, expanded for the text of this tag. */
        private Pattern checkNameRegex;

        /** The parsed message regex, expanded for the text of this tag. */
        private Pattern messageRegex;

        /** The parsed module id regex, expanded for the text of this tag. */
        private Pattern moduleIdRegex;

        /**
         * Constructs a tag.
         *
         * @param aLineNo                the line number.
         * @param aText                the text of the suppression.
         * @param aOn                  <code>true</code> if the tag turns checkstyle reporting.
         * @throws ConversionException if unable to parse expanded aText.
         * on.
         */
        public
        Tag(int aLineNo, String aText, boolean aOn) throws ConversionException {
            mLineNo = aLineNo;
            mText = aText;
            on = aOn;

            // Expand regex for check and message
            // Does not intern Patterns with Utils.getPattern()
            try {
                Pattern regex = aOn ? onRegex : offRegex;
                if (checkNameFormat != null) {
                    this.checkNameRegex = Pattern.compile(expandFromLine(aText, checkNameFormat, regex));
                }
                if (messageFormat != null) {
                    this.messageRegex = Pattern.compile(expandFromLine(aText, messageFormat, regex));
                }
                if (moduleIdFormat != null) {
                    this.moduleIdRegex = Pattern.compile(expandFromLine(aText, moduleIdFormat, regex));
                }
            } catch (final PatternSyntaxException e) {
                throw new ConversionException("unable to parse expanded line " + e.getPattern(), e);
            }
        }

        /** @return the text of the tag. */
        public String
        getText() { return mText; }

        /** @return the line number of the tag in the source file. */
        public int
        getLine() { return mLineNo; }

        /**
         * Determines whether the suppression turns checkstyle reporting on or
         * off.
         * @return <code>true</code>if the suppression turns reporting on.
         */
        public boolean
        isOn() { return on; }

        /**
         * Compares the position of this tag in the file
         * with the position of another tag.
         * @param that the tag to compare with this one.
         * @return a negative number if this tag is before the other tag,
         * 0 if they are at the same position, and a positive number if this
         * tag is after the other tag.
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int
        compareTo(Tag that) { return mLineNo - that.mLineNo; }

        /**
         * Determines whether the source of an audit event
         * matches the text of this tag.
         * @param aEvent the <code>AuditEvent</code> to check.
         * @return true if the source of aEvent matches the text of this tag.
         */
        public boolean
        isMatch(AuditEvent aEvent) {
            if (
                checkNameRegex != null
                && checkNameRegex.matcher(aEvent.getSourceName()).find()
            ) return true;
            if (
                messageRegex != null
                && messageRegex.matcher(aEvent.getMessage()).find()
            ) return true;
            if (
                moduleIdRegex != null
                && aEvent.getModuleId() != null
                && moduleIdRegex.matcher(aEvent.getModuleId()).find()
            ) return true;
            return false;
        }

        /**
         * Expand based on a matching line.
         *
         * @param replacement the string to expand.
         * @param regex the parsed expander.
         *
         * @return the expanded {@code replacement}
         */
        private String
        expandFromLine(String line, String replacement, Pattern regex) {
            final Matcher matcher = regex.matcher(line);

            if (!matcher.find()) return replacement;

            String result = replacement;
            for (int i = 0; i <= matcher.groupCount(); i++) {
                // $n expands line match like in Pattern.subst().
                result = result.replaceAll("\\$" + i, matcher.group(i));
            }
            return result;
        }

        @Override public final String
        toString() { return "Tag[line=" + getLine() + "; on=" + isOn() + "; text='" + getText() + "']"; }
    }

    /** Control all checks */

    /** Parsed line regex that turns checkstyle reporting off. */
    private Pattern offRegex;

    /** Parsed line regex that turns checkstyle reporting on. */
    private Pattern onRegex;

    /** The pattern agains which the check name is matched. */
    private String checkNameFormat;

    /** The message format to suppress. */
    private String messageFormat;

    private String moduleIdFormat;

    private final List<Tag> magicLines = Lists.newArrayList();

    /**
     * References the current FileContents for this filter.
     * Since this is a weak reference to the FileContents, the FileContents
     * can be reclaimed as soon as the strong references in TreeWalker
     * and FileContentsHolder are reassigned to the next FileContents,
     * at which time filtering for the current FileContents is finished.
     */
    private WeakReference<FileContents> mFileContentsReference = new WeakReference<FileContents>(null);

    public
    SuppressionLine() {}

    public void
    setOffFormat(String offFormat) throws ConversionException {
        try {
            this.offRegex = Utils.getPattern(offFormat);
        } catch (final PatternSyntaxException e) {
            throw new ConversionException("unable to parse " + offFormat, e);
        }
    }

    public void
    setOnFormat(String onFormat) throws ConversionException {
        try {
            this.onRegex = Utils.getPattern(onFormat);
        } catch (final PatternSyntaxException e) {
            throw new ConversionException("unable to parse " + onFormat, e);
        }
    }

    /** @return the FileContents for this filter. */
    public FileContents
    getFileContents() { return mFileContentsReference.get(); }

    /**
     * Set the FileContents for this filter.
     * @param aFileContents the FileContents for this filter.
     */
    public void
    setFileContents(FileContents aFileContents) {
        mFileContentsReference = new WeakReference<FileContents>(aFileContents);
    }

    public void
    setCheckNameFormat(String checkNameFormat) throws ConversionException {
        try {
            Utils.getPattern(checkNameFormat);
        } catch (final PatternSyntaxException e) {
            throw new ConversionException("unable to parse " + checkNameFormat, e);
        }
        this.checkNameFormat = checkNameFormat;
    }

    public void
    setMessageFormat(String messageFormat) throws ConversionException {
        // check that aFormat parses
        try {
            Utils.getPattern(messageFormat);
        } catch (final PatternSyntaxException e) {
            throw new ConversionException("unable to parse " + messageFormat, e);
        }
        this.messageFormat = messageFormat;
    }

    public void
    setModuleIdFormat(String moduleIdFormat) throws ConversionException {
        try {
            Utils.getPattern(moduleIdFormat);
        } catch (final PatternSyntaxException e) {
            throw new ConversionException("unable to parse " + moduleIdFormat, e);
        }
        this.moduleIdFormat = moduleIdFormat;
    }
    
    public boolean
    accept(AuditEvent event) {

        if (event.getLocalizedMessage() == null) return true;        // A special event.

        // Lazy update. If the first event for the current file, update file
        // contents and tag suppressions
        final FileContents currentContents = FileContentsHolder.getContents();
        if (currentContents == null) {
            // we have no contents, so we can not filter.
            // TODO: perhaps we should notify user somehow?
            return true;
        }
        if (getFileContents() != currentContents) {
            setFileContents(currentContents);
            processMagicLines();
        }
        final Tag matchTag = findNearestMatch(event);
        if ((matchTag != null) && !matchTag.isOn()) {
            return false;
        }
        return true;
    }

    /**
     * Finds the nearest tag that matches an audit event. The nearest tag is before the line and column of the event.
     *
     * @param aEvent The <code>AuditEvent</code> to match.
     * @return       The <code>Tag</code> nearest aEvent.
     */
    private Tag
    findNearestMatch(AuditEvent aEvent) {
        Tag result = null;
        // TODO: try binary search if sequential search becomes a performance
        // problem.
        for (Tag tag : magicLines) {
            if (tag.getLine() + 1 > aEvent.getLine()) break;
            if (tag.isMatch(aEvent)) result = tag;
        }
        return result;
    }

    /**
     * Collects all the suppression tags for all magic lines into a list and sorts the list.
     */
    private void
    processMagicLines() {
        magicLines.clear();
        String[] lines = getFileContents().getLines();
        for (int lineNo = 0; lineNo < lines.length; ++lineNo) {
            checkMagicness(lines[lineNo], lineNo);
        }
    }

    /**
     * Tags a string if it matches the format for turning
     * checkstyle reporting on or the format for turning reporting off.
     * @param aText the string to tag.
     * @param aLine the line number of aText.
     */
    private void
    checkMagicness(String aText, int aLine) {
        if (offRegex != null) {
            final Matcher offMatcher = offRegex.matcher(aText);
            if (offMatcher.find()) {
                addTag(offMatcher.group(0), aLine, false);
            }
        }
        if (onRegex != null) {
            final Matcher onMatcher = onRegex.matcher(aText);
            if (onMatcher.find()) {
                addTag(onMatcher.group(0), aLine, true);
            }
        }
    }

    /**
     * Adds a <code>Tag</code> to the list of all tags.
     * @param aText the text of the tag.
     * @param aLine the line number of the tag.
     * @param aOn <code>true</code> if the tag turns checkstyle reporting on.
     */
    private void
    addTag(String aText, int aLine, boolean aOn) {
        final Tag tag = new Tag(aLine, aText, aOn);
        magicLines.add(tag);
    }
}

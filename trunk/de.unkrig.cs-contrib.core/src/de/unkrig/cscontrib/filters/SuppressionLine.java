
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

package de.unkrig.cscontrib.filters;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.beanutils.ConversionException;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.api.*;
import com.puppycrawl.tools.checkstyle.checks.FileContentsHolder;

/***/
public
class SuppressionLine extends AutomaticBean implements Filter {

    /**
     * A Tag holds a magic line and its location, and determines whether the suppression turns CHECKSTYLE reporting on
     * or off.
     */
    public
    class Tag implements Comparable<Tag> {

        /** The text of the tag. */
        private final String text;

        /** The line number of the tag. */
        private final int lineNo;

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
         * @param lineNo               the line number.
         * @param text                 the text of the suppression.
         * @param on                   <code>true</code> if the tag turns checkstyle reporting.
         * @throws ConversionException if unable to parse {@code text}.
         * on.
         */
        public
        Tag(int lineNo, String text, boolean on) throws ConversionException {
            this.lineNo = lineNo;
            this.text   = text;
            this.on     = on;

            // Expand regex for check and message
            // Does not intern Patterns with Utils.getPattern()
            try {
                Pattern regex = on ? onRegex : offRegex;
                if (checkNameFormat != null) {
                    this.checkNameRegex = Pattern.compile(expandFromLine(text, checkNameFormat, regex));
                }
                if (messageFormat != null) {
                    this.messageRegex = Pattern.compile(expandFromLine(text, messageFormat, regex));
                }
                if (moduleIdFormat != null) {
                    this.moduleIdRegex = Pattern.compile(expandFromLine(text, moduleIdFormat, regex));
                }
            } catch (final PatternSyntaxException e) {
                throw new ConversionException("unable to parse expanded line " + e.getPattern(), e);
            }
        }

        /** @return the text of the tag. */
        public String
        getText() { return this.text; }

        /** @return the line number of the tag in the source file. */
        public int
        getLine() { return this.lineNo; }

        /**
         * Determines whether the suppression turns checkstyle reporting on or
         * off.
         * @return <code>true</code>if the suppression turns reporting on.
         */
        public boolean
        isOn() { return this.on; }

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
        compareTo(Tag that) { return this.lineNo - that.lineNo; }

        /**
         * Determines whether the source of an audit event
         * matches the text of this tag.
         *
         * @param event the <code>AuditEvent</code> to check.
         * @return      {@code true} if the source of {@code event} matches the text of this tag.
         */
        public boolean
        isMatch(AuditEvent event) {
            if (
                this.checkNameRegex != null
                && this.checkNameRegex.matcher(event.getSourceName()).find()
            ) return true;
            if (
                this.messageRegex != null
                && this.messageRegex.matcher(event.getMessage()).find()
            ) return true;
            if (
                this.moduleIdRegex != null
                && event.getModuleId() != null
                && this.moduleIdRegex.matcher(event.getModuleId()).find()
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
        toString() { return "Tag[line=" + this.getLine() + "; on=" + this.isOn() + "; text='" + this.getText() + "']"; }
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
    private WeakReference<FileContents> fileContentsReference = new WeakReference<FileContents>(null);

    public
    SuppressionLine() {}

    // CONFIGURATION SETTERS AND GETTERS -- CHECKSTYLE MethodCheck:OFF

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
        // check that format parses
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

    // END CONFIGURATION SETTERS AND GETTERS -- CHECKSTYLE MethodCheck:ON

    /** @return the FileContents for this filter. */
    public FileContents
    getFileContents() { return this.fileContentsReference.get(); }

    /**
     * Set the FileContents for this filter.
     *
     * @param fileContents the FileContents for this filter.
     */
    public void
    setFileContents(FileContents fileContents) {
        this.fileContentsReference = new WeakReference<FileContents>(fileContents);
    }
    
    @Override public boolean
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
        if (this.getFileContents() != currentContents) {
            this.setFileContents(currentContents);
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
     * @param event The {@code AuditEvent} to match.
     * @return      The {@code Tag} nearest {@code event}.
     */
    private Tag
    findNearestMatch(AuditEvent event) {
        Tag result = null;
        // TODO: try binary search if sequential search becomes a performance
        // problem.
        for (Tag tag : this.magicLines) {
            if (tag.getLine() + 1 > event.getLine()) break;
            if (tag.isMatch(event)) result = tag;
        }
        return result;
    }

    /**
     * Collects all the suppression tags for all magic lines into a list and sorts the list.
     */
    private void
    processMagicLines() {
        this.magicLines.clear();
        String[] lines = this.getFileContents().getLines();
        for (int lineNo = 0; lineNo < lines.length; ++lineNo) {
            checkMagicness(lines[lineNo], lineNo);
        }
    }

    /**
     * Tags a string if it matches the format for turning
     * checkstyle reporting on or the format for turning reporting off.
     *
     * @param text the string to tag.
     * @param line the line number of {@code text}.
     */
    private void
    checkMagicness(String text, int line) {
        if (this.offRegex != null) {
            final Matcher offMatcher = this.offRegex.matcher(text);
            if (offMatcher.find()) {
                addTag(offMatcher.group(0), line, false);
            }
        }
        if (this.onRegex != null) {
            final Matcher onMatcher = this.onRegex.matcher(text);
            if (onMatcher.find()) {
                addTag(onMatcher.group(0), line, true);
            }
        }
    }

    /**
     * Adds a <code>Tag</code> to the list of all tags.
     *
     * @param text the text of the tag.
     * @param line the line number of the tag.
     * @param on   {@code true} if the tag turns checkstyle reporting on.
     */
    private void
    addTag(String text, int line, boolean on) {
        final Tag tag = new Tag(line, text, on);
        this.magicLines.add(tag);
    }
}

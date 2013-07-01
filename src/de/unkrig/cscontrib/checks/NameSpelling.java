
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

package de.unkrig.cscontrib.checks;

import java.util.*;

import org.apache.commons.beanutils.ConversionException;

import com.puppycrawl.tools.checkstyle.api.*;
import com.puppycrawl.tools.checkstyle.checks.AbstractFormatCheck;

import de.unkrig.commons.nullanalysis.NotNullByDefault;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Checks that particular Java elements are declared with a name that matches or does not match a configurable REGEX.
 */
@NotNullByDefault(false) public
class NameSpelling extends AbstractFormatCheck {

    /**
     * Elements to apply this check to
     */
    private final EnumSet<Elements> elements = EnumSet.noneOf(Elements.class);

    /**
     * Apply only to declarations which have these modifiers
     */
    private final Set<Integer> requiredModifiers = new HashSet<Integer>();

    /**
     * Apply only to declarations which do not have these modifiers
     */
    private final Set<Integer> missingModifiers = new HashSet<Integer>();

    /**
     * Whether to REQUIRE or FORBID that names match
     */
    private Options option;

    public
    NameSpelling() { super(""); }

    /**
     * All elements that can be declared in the JAVA programming language.
     */
    public enum Elements {
        // CHECKSTYLE VariableCheck:OFF
        ANNOTATION("Annotation"),
        ANNOTATION_FIELD("Annotation field"),
        CATCH_PARAMETER("Catch parameter"),
        CLASS("Class"),
        ENUM("Enum"),
        ENUM_CONSTANT("Enum constant"),
        FOR_VARIABLE("For variable"),
        FOREACH_VARIABLE("Foreach variable"),
        FIELD("Field"),
        FORMAL_PARAMETER("Formal parameter"),
        INTERFACE("Interface"),
        LOCAL_VARIABLE("Local variable"),
        METHOD("Method"),
        PACKAGE("Package"),
        TYPE_PARAMETER("Type parameter");
        // CHECKSTYLE VariableCheck:ON

        private final String name;

        private
        Elements(String name) { this.name = name; }

        @Override public String
        toString() { return this.name; }
    }

    // CONFIGURATION SETTERS -- CHECKSTYLE MethodCheck:OFF

    public final void
    setElements(String[] elements) {
        for (final String element : elements) {
            this.elements.add(Enum.valueOf(Elements.class, element.toUpperCase()));
        }
    }

    /**
     * All modifiers of the JAVA programming language.
     */
    public enum Modifiers {
        // SUPPRESS CHECKSTYLE JavadocVariable
        PUBLIC, PROTECTED, PRIVATE, STATIC, FINAL, VOLATILE, STRICTFP
    }

    public final void
    setRequiredModifiers(String[] modifiers) {
        for (final String modifier : modifiers) {
            this.requiredModifiers.add(TokenTypes.getTokenId(modifier.toUpperCase()));
        }
    }
    
    public final void
    setMissingModifiers(String[] modifiers) {
        for (final String modifier : modifiers) {
            this.missingModifiers.add(TokenTypes.getTokenId(modifier.toUpperCase()));
        }
    }

    /**
     * Whether a name MUST match, or MUST NOT match.
     */
    public enum Options {
        // SUPPRESS CHECKSTYLE VariableCheck
        REQUIRE, FORBID
    }

    public final void
    setOption(String option) throws ConversionException {
        try {
            this.option = Enum.valueOf(Options.class, option.trim().toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new ConversionException(option, iae);
        }
    }

    // END CONFIGURATION SETTERS -- CHECKSTYLE MethodCheck:ON

    @Override public int[]
    getDefaultTokens() {

        // Calculate the minimal set of tokens required to perform the check.
        int[] tokenIds = new int[20];
        int   idx      = 0;

        if (this.elements.contains(Elements.ANNOTATION))       tokenIds[idx++] = ANNOTATION_DEF;
        if (this.elements.contains(Elements.ANNOTATION_FIELD)) tokenIds[idx++] = ANNOTATION_FIELD_DEF;
        if (this.elements.contains(Elements.CLASS))            tokenIds[idx++] = CLASS_DEF;
        if (this.elements.contains(Elements.ENUM))             tokenIds[idx++] = ENUM_DEF;
        if (this.elements.contains(Elements.ENUM_CONSTANT))    tokenIds[idx++] = ENUM_CONSTANT_DEF;
        if (this.elements.contains(Elements.INTERFACE))        tokenIds[idx++] = INTERFACE_DEF;
        if (this.elements.contains(Elements.METHOD))           tokenIds[idx++] = METHOD_DEF;
        if (this.elements.contains(Elements.PACKAGE))          tokenIds[idx++] = PACKAGE_DEF;
        if (this.elements.contains(Elements.TYPE_PARAMETER))   tokenIds[idx++] = TYPE_PARAMETER;
        if (
            this.elements.contains(Elements.CATCH_PARAMETER)
            || this.elements.contains(Elements.FORMAL_PARAMETER)
        ) tokenIds[idx++] = PARAMETER_DEF;
        if (
            this.elements.contains(Elements.LOCAL_VARIABLE)
            || this.elements.contains(Elements.FOR_VARIABLE)
            || this.elements.contains(Elements.FOREACH_VARIABLE)
            || this.elements.contains(Elements.FIELD)
        ) tokenIds[idx++] = VARIABLE_DEF;

        int[] result = new int[idx];
        System.arraycopy(tokenIds, 0, result, 0, idx);
        return result;
    }

    @Override public void
    visitToken(DetailAST ast) {
        try {

            // Determine the element type from the given AST.
            Elements element;
            switch (ast.getType()) {

            case ANNOTATION_DEF:
                element = Elements.ANNOTATION;
                break;

            case ANNOTATION_FIELD_DEF:
                element = Elements.ANNOTATION_FIELD;
                break;

            case CLASS_DEF:
                element = Elements.CLASS;
                break;

            case ENUM_DEF:
                element = Elements.ENUM;
                break;

            case ENUM_CONSTANT_DEF:
                element = Elements.ENUM_CONSTANT;
                break;

            case INTERFACE_DEF:
                element = Elements.INTERFACE;
                break;

            case METHOD_DEF:
                element = Elements.METHOD;
                break;

            case PACKAGE_DEF:
                element = Elements.PACKAGE;
                break;

            case TYPE_PARAMETER:
                element = Elements.TYPE_PARAMETER;
                break;

            case PARAMETER_DEF:
                {
                    int parentType = ast.getParent().getType();
                    element = (
                        parentType == PARAMETERS      ? Elements.FORMAL_PARAMETER
                        : parentType == LITERAL_CATCH ? Elements.CATCH_PARAMETER
                        : null
                    );
                }
                break;

            case VARIABLE_DEF:
                {
                    int parentType = ast.getParent().getType();
                    element = (
                        parentType == SLIST             ? Elements.LOCAL_VARIABLE
                        : parentType == FOR_INIT        ? Elements.FOR_VARIABLE
                        : parentType == FOR_EACH_CLAUSE ? Elements.FOREACH_VARIABLE
                        : parentType == OBJBLOCK        ? Elements.FIELD
                        : null
                    );
                }
                break;

            default:
                throw new IllegalStateException(Integer.toString(ast.getType()));
            }
            if (element == null) throw new IllegalStateException(Integer.toString(ast.getType()));

            // Verify that this element should be checked.
            if (!this.elements.contains(element)) return;

            // Now determine the modifiers and the name.
            DetailAST modifiersAst, nameAst;
            switch (element) {

            case ANNOTATION:
            case ANNOTATION_FIELD:
            case CATCH_PARAMETER:
            case CLASS:
            case ENUM:
            case ENUM_CONSTANT:
            case FOR_VARIABLE:
            case FOREACH_VARIABLE:
            case FIELD:
            case FORMAL_PARAMETER:
            case INTERFACE:
            case LOCAL_VARIABLE:
            case METHOD:
            case TYPE_PARAMETER:
                modifiersAst = ast.findFirstToken(MODIFIERS);
                nameAst      = ast.findFirstToken(IDENT);
                break;

            case PACKAGE:
                modifiersAst = null;
                nameAst      = ast.getLastChild().getPreviousSibling();
                break;

            default:
                throw new IllegalStateException(element.toString());
            }

            // Check if the modifiers match the configuration.
            if (modifiersAst == null) {
                assert this.requiredModifiers.isEmpty() : "Must not set 'requiredModifiers' for element 'package'";
                assert this.missingModifiers.isEmpty() : "Must not set 'missingModifiers' for element 'package'";
            } else {
                for (Integer modifier : this.requiredModifiers) {
                    if (modifiersAst.findFirstToken(modifier) == null) return;
                }
                for (Integer modifier : this.missingModifiers) {
                    if (modifiersAst.findFirstToken(modifier) != null) return;
                }
            }

            // Eventually check the element name.
            FullIdent fullName = FullIdent.createFullIdent(nameAst);
            switch (this.option) {

            case REQUIRE:
                if (!getRegexp().matcher(fullName.getText()).find()) {
                    log(
                        fullName.getLineNo(),
                        fullName.getColumnNo(),
                        "{0} ''{1}'' does not comply with ''{2}''",
                        element.toString(),
                        fullName.getText(),
                        getFormat()
                    );
                }
                break;

            case FORBID:
                if (getRegexp().matcher(fullName.getText()).find()) {
                    log(
                        fullName.getLineNo(),
                        fullName.getColumnNo(),
                        "{0} ''{1}'' must not match ''{2}''",
                        element.toString(),
                        fullName.getText(),
                        getFormat()
                    );
                }
                break;
            }
        } catch (RuntimeException rte) {
            throw new RuntimeException(
                getFileContents().getFilename() + ":" + ast.getLineNo() + "x" + ast.getColumnNo(),
                rte
            );
        }
    }
}


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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.ConversionException;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.checks.AbstractFormatCheck;

import de.unkrig.commons.nullanalysis.NotNullByDefault;
import de.unkrig.cscontrib.LocalTokenType;
import de.unkrig.cscontrib.util.AstUtil;

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
    private final Set<LocalTokenType> requiredModifiers = new HashSet<LocalTokenType>();

    /**
     * Apply only to declarations which do not have these modifiers
     */
    private final Set<LocalTokenType> missingModifiers = new HashSet<LocalTokenType>();

    /**
     * Whether to REQUIRE or FORBID that names match
     */
    private Options option;

    public
    NameSpelling() { super(""); }

    /**
     * All elements that can be declared in the JAVA programming language.
     */
    public
    enum Elements {

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
     * The 'option-provider' for the 'requiredModifiers' and 'missingModifiers' properties.
     */
    public
    enum Modifier {
        PUBLIC, PROTECTED, PRIVATE, STATIC, FINAL, VOLATILE, STRICTFP // SUPPRESS CHECKSTYLE JavadocVariable
    }

    public final void
    setRequiredModifiers(String[] modifiers) {
        for (final String modifier : modifiers) {
            this.requiredModifiers.add(LocalTokenType.valueOf("LITERAL_" + modifier.toUpperCase()));
        }
    }

    public final void
    setMissingModifiers(String[] modifiers) {
        for (final String modifier : modifiers) {
            this.missingModifiers.add(LocalTokenType.valueOf("LITERAL_" + modifier.toUpperCase()));
        }
    }

    /**
     * Whether a name MUST match, or MUST NOT match.
     */
    public enum Options { REQUIRE, FORBID } // SUPPRESS CHECKSTYLE JavadocVariable

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
        List<LocalTokenType> tokens = new ArrayList<LocalTokenType>();

        if (this.elements.contains(Elements.ANNOTATION))       tokens.add(LocalTokenType.ANNOTATION_DEF);
        if (this.elements.contains(Elements.ANNOTATION_FIELD)) tokens.add(LocalTokenType.ANNOTATION_FIELD_DEF);
        if (this.elements.contains(Elements.CLASS))            tokens.add(LocalTokenType.CLASS_DEF);
        if (this.elements.contains(Elements.ENUM))             tokens.add(LocalTokenType.ENUM_DEF);
        if (this.elements.contains(Elements.ENUM_CONSTANT))    tokens.add(LocalTokenType.ENUM_CONSTANT_DEF);
        if (this.elements.contains(Elements.INTERFACE))        tokens.add(LocalTokenType.INTERFACE_DEF);
        if (this.elements.contains(Elements.METHOD))           tokens.add(LocalTokenType.METHOD_DEF);
        if (this.elements.contains(Elements.PACKAGE))          tokens.add(LocalTokenType.PACKAGE_DEF);
        if (this.elements.contains(Elements.TYPE_PARAMETER))   tokens.add(LocalTokenType.TYPE_PARAMETER);

        if (
            this.elements.contains(Elements.CATCH_PARAMETER)
            || this.elements.contains(Elements.FORMAL_PARAMETER)
        ) tokens.add(LocalTokenType.PARAMETER_DEF);

        if (
            this.elements.contains(Elements.LOCAL_VARIABLE)
            || this.elements.contains(Elements.FOR_VARIABLE)
            || this.elements.contains(Elements.FOREACH_VARIABLE)
            || this.elements.contains(Elements.FIELD)
        ) tokens.add(LocalTokenType.VARIABLE_DEF);

        LocalTokenType[] tokensArray = tokens.toArray(new LocalTokenType[tokens.size()]);
        assert tokensArray != null;
        return LocalTokenType.delocalize(tokensArray);
    }

    @Override public void
    visitToken(DetailAST ast) {
        try {

            // Determine the element type from the given AST.
            Elements element;
            switch (LocalTokenType.localize(ast.getType())) {

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
                element = (
                    AstUtil.parentTypeIs(ast, LocalTokenType.PARAMETERS)      ? Elements.FORMAL_PARAMETER
                    : AstUtil.parentTypeIs(ast, LocalTokenType.LITERAL_CATCH) ? Elements.CATCH_PARAMETER
                    : null
                );
                break;

            case VARIABLE_DEF:
                element = (
                    AstUtil.parentTypeIs(ast, LocalTokenType.SLIST)             ? Elements.LOCAL_VARIABLE
                    : AstUtil.parentTypeIs(ast, LocalTokenType.FOR_INIT)        ? Elements.FOR_VARIABLE
                    : AstUtil.parentTypeIs(ast, LocalTokenType.FOR_EACH_CLAUSE) ? Elements.FOREACH_VARIABLE
                    : AstUtil.parentTypeIs(ast, LocalTokenType.OBJBLOCK)        ? Elements.FIELD
                    : null
                );
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
                modifiersAst = ast.findFirstToken(LocalTokenType.MODIFIERS.delocalize());
                nameAst      = ast.findFirstToken(LocalTokenType.IDENT.delocalize());
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
                for (LocalTokenType modifier : this.requiredModifiers) {
                    if (modifiersAst.findFirstToken(modifier.delocalize()) == null) return;
                }
                for (LocalTokenType modifier : this.missingModifiers) {
                    if (modifiersAst.findFirstToken(modifier.delocalize()) != null) return;
                }
            }

            // Eventually check the element name.
            FullIdent fullName = FullIdent.createFullIdent(nameAst);
            switch (this.option) {

            case REQUIRE:
                if (!this.getRegexp().matcher(fullName.getText()).find()) {
                    this.log(
                        fullName.getLineNo(),
                        fullName.getColumnNo(),
                        "{0} ''{1}'' does not comply with ''{2}''",
                        element.toString(),
                        fullName.getText(),
                        this.getFormat()
                    );
                }
                break;

            case FORBID:
                if (this.getRegexp().matcher(fullName.getText()).find()) {
                    this.log(
                        fullName.getLineNo(),
                        fullName.getColumnNo(),
                        "{0} ''{1}'' must not match ''{2}''",
                        element.toString(),
                        fullName.getText(),
                        this.getFormat()
                    );
                }
                break;
            }
        } catch (RuntimeException rte) {
            throw new RuntimeException(
                this.getFileContents().getFilename() + ":" + ast.getLineNo() + "x" + ast.getColumnNo(),
                rte
            );
        }
    }
}

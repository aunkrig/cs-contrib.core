
/*
 * de.unkrig.cs-contrib - Additional checks, filters and quickfixes for CheckStyle and Eclipse-CS
 *
 * Copyright (c) 2015, Arno Unkrig
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

package de.unkrig.csdoclet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Derives a boolean rule from a class.
 */
@Target(ElementType.TYPE) @Retention(RetentionPolicy.SOURCE) public
@interface Rule  {

    String group();

    /**
     * CheckStyle defines the following 'standard' groups:
     * <pre>
     * Annotation.group =    Annotations
     * Javadoc.group =       Javadoc Comments
     * Naming.group =        Naming Conventions
     * Headers.group =       Headers
     * Imports.group =       Imports
     * Sizes.group =         Size Violations
     * Whitespace.group =    Whitespace
     * Regexp.group =        Regexp
     * Modifiers.group =     Modifiers
     * Blocks.group =        Blocks
     * Coding.group =        Coding Problems
     * Design.group =        Class Design
     * Duplicates.group =    Duplicates
     * Metrics.group =       Metrics
     * Miscellaneous.group = Miscellaneous
     * Other.group =         Other
     * Filters.group =       Filters
     * </pre>
     * If you put a check into a 'standard group', then you should choose the group name identically with the standard
     * group, otherwise you'll end up with TWO groups.
     */
    String     groupName();
    String     name();
    String     parent();
    Class<?>[] quickfixes() default {};
    boolean    hasSeverity() default true;
}

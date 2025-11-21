/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered;

import net.octyl.levelheadered.rewriter.FileHeaderRewriter;
import org.gradle.api.file.ContentFilterable;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.resources.TextResource;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.util.PatternSet;

/**
 * Shared configuration interface between the extension and tasks.
 *
 * <p>
 * Not to be implemented by users.
 * </p>
 */
public interface LevelHeaderedConfig {
    /**
     * {@return the rewriters to use for different file types} This map is keyed primarily by file extensions
     * <em>with</em> the leading dot (e.g. {@code .java}, {@code .xml}), but may also be keyed by full file names
     * for those that do not have extensions (e.g. {@code Makefile}).
     */
    @Nested
    MapProperty<String, FileHeaderRewriter> getRewriters();

    /**
     * {@return the header template to apply to source files}
     * This is expanded using {@link groovy.text.SimpleTemplateEngine}.
     */
    @Nested
    Property<TextResource> getHeaderTemplate();

    /**
     * {@return the template variables to use when expanding the header template}
     */
    @Input
    MapProperty<String, String> getTemplateVariables();

    /**
     * {@return the patterns to match source files to apply headers to}
     *
     * <p>
     * These are used in combination with the filters on the source files of the task.
     * By default, this is an empty pattern set, meaning all source files are considered.
     * </p>
     */
    @Internal
    Property<PatternSet> getSourceMatchPatterns();
}

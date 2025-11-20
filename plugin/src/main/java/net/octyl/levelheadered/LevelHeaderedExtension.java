/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package net.octyl.levelheadered;

import org.gradle.api.Action;
import org.gradle.api.resources.TextResourceFactory;
import org.gradle.api.tasks.util.PatternSet;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Extension class for configuring the LevelHeadered plugin.
 *
 * <p>
 * Only {@code abstract} for Gradle, not to be implemented by users.
 * </p>
 */
public abstract class LevelHeaderedExtension implements LevelHeaderedConfig {
    private final TextResourceFactory textResourceFactory;

    @SuppressWarnings("doclint:missing")
    @Inject
    public LevelHeaderedExtension(TextResourceFactory textResourceFactory) {
        this.textResourceFactory = textResourceFactory;
    }

    /**
     * Sets the header template from the given string.
     *
     * @param template the template string
     * @see #getHeaderTemplate()
     */
    public final void headerTemplate(String template) {
        getHeaderTemplate().set(textResourceFactory.fromString(template));
    }

    /**
     * Sets the header template from the given file. UTF-8 encoding is used.
     *
     * @param templateFile the template file
     * @see #getHeaderTemplate()
     */
    public final void headerTemplate(File templateFile) {
        getHeaderTemplate().set(textResourceFactory.fromFile(templateFile, StandardCharsets.UTF_8.name()));
    }

    /**
     * Configures the current source match patterns using the given action.
     *
     * @param action the action to configure the pattern set
     * @see #getSourceMatchPatterns()
     */
    public final void sourceMatchPatterns(Action<? super PatternSet> action) {
        action.execute(getSourceMatchPatterns().get());
    }
}

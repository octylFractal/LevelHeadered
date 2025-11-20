/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package net.octyl.levelheadered;

import net.octyl.levelheadered.rewriter.StandardFileHeaderRewriter;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.util.PatternSet;

import javax.inject.Inject;
import java.util.Map;

/**
 * Base plugin that applies common conventions to all LevelHeadered tasks.
 *
 * <p>
 * Only {@code abstract} for Gradle, not to be implemented by users.
 * </p>
 */
public abstract class LevelHeaderedBasePlugin implements Plugin<Project> {
    static void applyConventions(LevelHeaderedConfig config, ProviderFactory providers) {
        config.getHeaderTemplate().convention(providers.provider(() -> {
            throw new InvalidUserDataException("No header template configured for LevelHeadered plugin. " +
                "Please configure the 'levelHeadered.headerTemplate' property.");
        }));
        // Keep alphabetized for better merging:
        config.getRewriters().convention(Map.of(
            ".css", StandardFileHeaderRewriter.SLASH_STAR_COMMENT,
            ".gradle", StandardFileHeaderRewriter.SLASH_STAR_COMMENT,
            ".groovy", StandardFileHeaderRewriter.SLASH_STAR_COMMENT,
            ".java", StandardFileHeaderRewriter.SLASH_STAR_COMMENT,
            ".js", StandardFileHeaderRewriter.SLASH_STAR_COMMENT,
            ".kt", StandardFileHeaderRewriter.SLASH_STAR_COMMENT,
            ".kts", StandardFileHeaderRewriter.SLASH_STAR_COMMENT,
            ".scala", StandardFileHeaderRewriter.SLASH_STAR_COMMENT
        ));
        config.getSourceMatchPatterns().convention(new PatternSet());
    }

    static void copyViaConvention(LevelHeaderedConfig from, LevelHeaderedConfig to) {
        to.getHeaderTemplate().convention(from.getHeaderTemplate());
        to.getTemplateVariables().convention(from.getTemplateVariables());
        to.getRewriters().convention(from.getRewriters());
        to.getSourceMatchPatterns().convention(from.getSourceMatchPatterns());
    }

    @SuppressWarnings("doclint:missing")
    public LevelHeaderedBasePlugin() {
    }

    @SuppressWarnings("doclint:missing")
    @Inject
    protected abstract ProviderFactory getProviders();

    public void apply(Project project) {
        project.getTasks().withType(HeaderWorkerSourceTask.class).configureEach(config ->
            applyConventions(config, getProviders())
        );
    }
}

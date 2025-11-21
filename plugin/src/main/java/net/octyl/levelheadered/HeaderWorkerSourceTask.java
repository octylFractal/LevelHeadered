/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered;

import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import net.octyl.levelheadered.internal.HeaderWorkParameters;
import net.octyl.levelheadered.rewriter.FileHeaderRewriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileType;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.ChangeType;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for tasks that perform actions with headers on source files.
 *
 * @param <W> the work action type
 * @param <P> the work parameters type
 */
public abstract sealed class HeaderWorkerSourceTask<W extends WorkAction<P>, P extends HeaderWorkParameters>
    extends DefaultTask implements LevelHeaderedConfig permits HeaderApplyTask, HeaderVerifyTask {
    private static String getExtensionsOrName(Path file) {
        String name = file.getFileName().toString();
        int firstDot = name.indexOf('.');
        if (firstDot == -1) {
            return name;
        }
        return name.substring(firstDot);
    }

    @SuppressWarnings("doclint:missing")
    protected HeaderWorkerSourceTask() {
    }

    @SuppressWarnings("doclint:missing")
    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @SuppressWarnings("doclint:missing")
    @Inject
    protected abstract ProviderFactory getProviderFactory();

    /**
     * {@return Source files to process} Will be filtered by {@link #getSourceMatchPatterns()}.
     */
    @Internal
    public abstract ConfigurableFileCollection getSource();

    @SuppressWarnings("doclint:missing")
    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    protected abstract ConfigurableFileCollection getSourcesToProcess();

    {
        // Wire up sources to process
        getSourcesToProcess().from(
            getProviderFactory().provider(() -> getSource().getAsFileTree().matching(getSourceMatchPatterns().get()))
        );
        getSourcesToProcess().builtBy(getSource());
    }

    /**
     * Runs the work action on changed files.
     *
     * @param inputChanges the input changes from Gradle
     * @throws IOException if an I/O error occurs
     */
    @TaskAction
    public void runWork(InputChanges inputChanges) throws IOException {
        WorkQueue workQueue = getWorkerExecutor().noIsolation();
        String headerText = expandHeaderTemplate();
        Map<String, FileHeaderRewriter> rewriters = getRewriters().get();
        inputChanges.getFileChanges(getSourcesToProcess()).forEach(change -> {
            if (change.getChangeType() != ChangeType.REMOVED && change.getFileType() == FileType.FILE) {
                Path file = change.getFile().toPath();
                String rewriterKey = getExtensionsOrName(file);
                FileHeaderRewriter rewriter = rewriters.get(rewriterKey);
                if (rewriter == null) {
                    getLogger().info(
                        "No rewriter configured for key '{}', skipping file {}", rewriterKey, file.toAbsolutePath()
                    );
                    return;
                }
                getLogger().info("Processing {} file: {}", change.getChangeType(), file.toAbsolutePath());
                workQueue.submit(getWorkActionClass(), p -> {
                    p.getHeaderText().set(headerText);
                    p.getRewriter().set(rewriter);
                    p.getSourceFile().set(change.getFile());
                    configureParameters(p);
                });
            }
        });
    }

    @Internal
    abstract Class<W> getWorkActionClass();

    void configureParameters(P parameters) {
    }

    private String expandHeaderTemplate() throws IOException {
        Writable result = new SimpleTemplateEngine()
            .createTemplate(getHeaderTemplate().get().asReader())
            // Must copy variables into a new map, as the SimpleTemplateEngine may modify the map during expansion
            .make(new HashMap<>(getTemplateVariables().get()));
        StringWriter writer = new StringWriter();
        result.writeTo(writer);
        return writer.toString();
    }
}

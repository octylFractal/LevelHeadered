/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered;

import net.octyl.levelheadered.internal.HeaderWorkParameters;
import net.octyl.levelheadered.rewriter.FileHeaderRewriter;
import net.octyl.levelheadered.rewriter.ModificationWriter;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.VerificationException;
import org.gradle.workers.WorkAction;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class HeaderVerifyWorkAction implements WorkAction<HeaderVerifyWorkAction.Parameters> {
    public interface Parameters extends HeaderWorkParameters {
        Property<String> getHeaderApplyTaskPath();
    }

    @Inject
    public HeaderVerifyWorkAction() {
    }

    @Override
    public void execute() {
        String headerText = getParameters().getHeaderText().get();
        FileHeaderRewriter rewriter = getParameters().getRewriter().get();
        Path sourceFilePath = getParameters().getSourceFile().get().getAsFile().toPath();

        try {
            String fileContent = Files.readString(sourceFilePath);
            ModificationWriter modificationWriter = rewriter.rewriteHeader(fileContent, headerText);
            if (modificationWriter == null) {
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify header of file: " + sourceFilePath, e);
        }
        String adviceText = getParameters().getHeaderApplyTaskPath()
            .map(path -> "Run the " + path + " task to fix this.")
            .getOrElse("Apply the correct header to this file to fix this.");
        throw new VerificationException(
            "Header verification failed for file: " + sourceFilePath + ". " + adviceText
        );
    }
}

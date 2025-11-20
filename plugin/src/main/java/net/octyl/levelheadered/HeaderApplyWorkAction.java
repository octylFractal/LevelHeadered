/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package net.octyl.levelheadered;

import net.octyl.levelheadered.internal.HeaderWorkParameters;
import net.octyl.levelheadered.rewriter.FileHeaderRewriter;
import net.octyl.levelheadered.rewriter.ModificationWriter;
import org.gradle.workers.WorkAction;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class HeaderApplyWorkAction implements WorkAction<HeaderApplyWorkAction.Parameters> {
    public interface Parameters extends HeaderWorkParameters {
    }

    @Inject
    public HeaderApplyWorkAction() {
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
            try (var writer = Files.newBufferedWriter(sourceFilePath)) {
                modificationWriter.writeTo(writer);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply header to file: " + sourceFilePath, e);
        }
    }
}

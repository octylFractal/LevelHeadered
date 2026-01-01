/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered;

import net.octyl.levelheadered.internal.Constants;
import net.octyl.levelheadered.internal.HeaderWorkParameters;
import net.octyl.levelheadered.rewriter.FileHeaderRewriter;
import net.octyl.levelheadered.rewriter.ModificationWriter;
import org.gradle.api.problems.ProblemGroup;
import org.gradle.api.problems.ProblemId;
import org.gradle.api.problems.Problems;
import org.gradle.api.problems.Severity;
import org.gradle.workers.WorkAction;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class HeaderApplyWorkAction implements WorkAction<HeaderApplyWorkAction.Parameters> {
    public interface Parameters extends HeaderWorkParameters {
    }

    private static final ProblemGroup PROBLEM_GROUP = ProblemGroup.create(
        "header-application", "Header Application Problems", Constants.PROBLEM_GROUP
    );
    private static final ProblemId FAILED_TO_APPLY_HEADER = ProblemId.create(
        "failed-to-apply-header", "Failed to apply header", PROBLEM_GROUP
    );

    @Inject
    public HeaderApplyWorkAction() {
    }

    @Inject
    protected abstract Problems getProblems();

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
            throw getProblems().getReporter().throwing(
                e,
                FAILED_TO_APPLY_HEADER,
                spec -> spec
                    .details("File: " + sourceFilePath)
                    .severity(Severity.ERROR)
                    .withException(e)
            );
        }
    }
}

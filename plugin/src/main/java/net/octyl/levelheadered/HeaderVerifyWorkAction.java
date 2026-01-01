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

    private static final ProblemGroup PROBLEM_GROUP = ProblemGroup.create(
        "header-verification", "Header Verification Problems", Constants.PROBLEM_GROUP
    );
    private static final ProblemId HEADER_VERIFICATION_FAILURE = ProblemId.create(
        "failure", "Header Verification Failure", PROBLEM_GROUP
    );
    private static final ProblemId FAILED_TO_RUN_HEADER_VERIFICATION = ProblemId.create(
        "failed-to-run", "Failed to run header verification", PROBLEM_GROUP
    );

    @Inject
    public HeaderVerifyWorkAction() {
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
        } catch (Exception e) {
            throw getProblems().getReporter().throwing(
                e,
                FAILED_TO_RUN_HEADER_VERIFICATION,
                spec -> spec
                    .details("File: " + sourceFilePath)
                    .severity(Severity.ERROR)
                    .withException(e)
            );
        }
        String adviceText = getParameters().getHeaderApplyTaskPath()
            .map(path -> "Run the " + path + " task to fix this.")
            .getOrElse("Apply the correct header to this file to fix this.");
        throw getProblems().getReporter().throwing(
            new VerificationException(
                "Header verification failed for file: " + sourceFilePath + "."
            ),
            HEADER_VERIFICATION_FAILURE,
            spec -> spec
                .solution(adviceText)
                .severity(Severity.ERROR)
                .fileLocation(sourceFilePath.toString())
        );
    }
}

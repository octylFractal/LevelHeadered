/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered.rewriter;

import org.jspecify.annotations.Nullable;

/**
 * Represents a way to change the file header to a specific text.
 *
 * <p>
 * All implementations should be {@link java.io.Serializable} or a valid {@link org.gradle.api.tasks.Nested @Nested}
 * property value, as they will be used in task inputs.
 * </p>
 */
public interface FileHeaderRewriter {
    /**
     * Detects the line ending used in the given content, from either LF or CRLF.
     * If multiple line endings are used, an error is thrown.
     * If no line endings are found, {@link System#lineSeparator()} is returned.
     *
     * @param content the content to analyze
     * @return the detected line ending
     * @throws IllegalArgumentException if multiple line endings are detected
     */
    static String detectLineEnding(String content) {
        enum LineEnding {
            UNKNOWN,
            LF,
            CRLF
        }

        var lineEnding = LineEnding.UNKNOWN;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\n') {
                if (i > 0 && content.charAt(i - 1) == '\r') {
                    // CRLF
                    if (lineEnding == LineEnding.LF) {
                        throw new IllegalArgumentException("Multiple line endings detected in content");
                    }
                    lineEnding = LineEnding.CRLF;
                } else {
                    // LF
                    if (lineEnding == LineEnding.CRLF) {
                        throw new IllegalArgumentException("Multiple line endings detected in content");
                    }
                    lineEnding = LineEnding.LF;
                }
            }
        }
        return switch (lineEnding) {
            case UNKNOWN -> System.lineSeparator();
            case LF -> "\n";
            case CRLF -> "\r\n";
        };
    }

    /**
     * Rewrites the header of the given file content to the specified header text, if needed.
     *
     * @param fileContent the content of the file
     * @param headerText the header text to apply
     * @return a function that writes the modified content to an {@link Appendable} if the content needs to be modified,
     * or {@code null} otherwise
     */
    @Nullable
    ModificationWriter rewriteHeader(String fileContent, String headerText);
}

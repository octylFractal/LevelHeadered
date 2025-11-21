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

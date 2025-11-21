/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered.internal;

import net.octyl.levelheadered.rewriter.FileHeaderRewriter;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

/**
 * Parameters for header work actions.
 *
 * <p>
 * Not to be implemented by users.
 * </p>
 */
public interface HeaderWorkParameters extends WorkParameters {
    /**
     * {@return the header text to apply to the source file} It is not yet in the comment format.
     */
    Property<String> getHeaderText();

    /**
     * {@return the rewriter to use to apply the header text to the source file}
     */
    Property<FileHeaderRewriter> getRewriter();

    /**
     * {@return the source file to apply the header to}
     */
    RegularFileProperty getSourceFile();
}

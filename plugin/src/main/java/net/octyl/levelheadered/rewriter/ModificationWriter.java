/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered.rewriter;

import java.io.IOException;

/**
 * A function to write modifications to an {@link Appendable}.
 */
@FunctionalInterface
public interface ModificationWriter {
    /**
     * Writes the new content to the given appendable.
     *
     * @param appendable the appendable to write to
     * @throws IOException if an I/O error occurs
     */
    void writeTo(Appendable appendable) throws IOException;
}

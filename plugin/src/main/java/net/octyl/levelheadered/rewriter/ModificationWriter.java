/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: AGPL-3.0-or-later
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

/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered.rewriter;

import java.io.Serializable;

/**
 * The standard file header rewriter implementations.
 */
public sealed interface StandardFileHeaderRewriter extends FileHeaderRewriter, Serializable
    permits SlashStarCommentRewriter {
    /**
     * Writes file headers as slash-star comments (<code>/* ... *{@literal /}</code>).
     */
    StandardFileHeaderRewriter SLASH_STAR_COMMENT = new SlashStarCommentRewriter();
}

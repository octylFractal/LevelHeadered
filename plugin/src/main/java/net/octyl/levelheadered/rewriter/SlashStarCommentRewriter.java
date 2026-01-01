/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered.rewriter;

import org.jspecify.annotations.Nullable;

final class SlashStarCommentRewriter implements StandardFileHeaderRewriter {
    private static String makeSlashStarComment(String newline, String headerText) {
        StringBuilder commentBuilder = new StringBuilder();
        commentBuilder.append("/*").append(newline);
        // The header text is not split by `newline` as it comes normalized from the user input.
        String[] lines = headerText.strip().split("\n", -1);
        for (String line : lines) {
            if (line.contains("*/")) {
                throw new IllegalArgumentException("Header text cannot contain '*/' sequence");
            }
            if (line.isBlank()) {
                commentBuilder.append(" *").append(newline);
                continue;
            }
            commentBuilder.append(" * ").append(line.stripTrailing()).append(newline);
        }
        commentBuilder.append(" */");
        return commentBuilder.toString();
    }

    @Override
    @Nullable
    public ModificationWriter rewriteHeader(String fileContent, String headerText) {
        String newline = FileHeaderRewriter.detectLineEnding(fileContent);
        String headerTextAsComment = makeSlashStarComment(newline, headerText);
        int indexOfFirstNonWhitespace = RewriteUtil.getIndexOfFirstNonWhitespaceChar(fileContent);
        if (indexOfFirstNonWhitespace == -1) {
            // empty file, just insert the header at the start
            return appendable -> {
                appendable.append(headerTextAsComment);
                appendable.append(fileContent);
            };
        }
        if (fileContent.substring(indexOfFirstNonWhitespace).startsWith(headerTextAsComment)) {
            // The header is already present.
            return null;
        }
        // Delete any existing comment header
        if (fileContent.substring(indexOfFirstNonWhitespace).startsWith("/*")) {
            // 3: '/**' potential, if +2 is '*'
            if (indexOfFirstNonWhitespace + 3 < fileContent.length()
                && fileContent.charAt(indexOfFirstNonWhitespace + 2) == '*') {
                // Insert the new header before the existing javadoc comment, with newline inbetween
                return appendable -> {
                    appendable.append(fileContent, 0, indexOfFirstNonWhitespace);
                    appendable.append(headerTextAsComment).append(newline).append(newline);
                    appendable.append(fileContent, indexOfFirstNonWhitespace, fileContent.length());
                };
            }
            int indexOfExistingCommentEnd = fileContent.indexOf("*/", indexOfFirstNonWhitespace);
            if (indexOfExistingCommentEnd != -1) {
                // Replace the existing comment
                return appendable -> {
                    appendable.append(fileContent, 0, indexOfFirstNonWhitespace);
                    appendable.append(headerTextAsComment);
                    appendable.append(fileContent, indexOfExistingCommentEnd + 2, fileContent.length());
                };
            } else {
                // Unclosed comment, just insert the new header before it, with newline inbetween
                return appendable -> {
                    appendable.append(fileContent, 0, indexOfFirstNonWhitespace);
                    appendable.append(headerTextAsComment).append(newline).append(newline);
                    appendable.append(fileContent, indexOfFirstNonWhitespace, fileContent.length());
                };
            }
        }
        // No existing comment, just insert the new header at the start of the file
        // Add a blank line if there is none in the whitespace
        boolean addBlankLine = true;
        for (int i = 0; i < indexOfFirstNonWhitespace; i++) {
            if (fileContent.startsWith(newline, i)) {
                addBlankLine = false;
                break;
            }
        }
        String newlines = addBlankLine ? newline + newline : newline;
        return appendable -> {
            appendable.append(headerTextAsComment).append(newlines);
            appendable.append(fileContent);
        };
    }
}

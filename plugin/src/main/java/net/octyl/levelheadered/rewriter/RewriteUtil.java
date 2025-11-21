/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered.rewriter;

class RewriteUtil {
    static int getIndexOfFirstNonWhitespaceChar(String content) {
        for (int i = 0; i < content.length(); i++) {
            if (!Character.isWhitespace(content.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}

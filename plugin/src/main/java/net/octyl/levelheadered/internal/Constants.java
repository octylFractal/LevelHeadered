/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered.internal;

import org.gradle.api.problems.ProblemGroup;

/**
 * A utility class to hold constants for internal use.
 */
public class Constants {
    /**
     * The problem group for all LevelHeadered plugin related problems.
     */
    public static final ProblemGroup PROBLEM_GROUP = ProblemGroup.create(
        "net.octyl.level-headered", "LevelHeadered Plugin Problems"
    );

    private Constants() {
    }
}

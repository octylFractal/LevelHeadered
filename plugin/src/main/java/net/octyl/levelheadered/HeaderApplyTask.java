/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package net.octyl.levelheadered;

import org.gradle.work.DisableCachingByDefault;

/**
 * Task that applies headers to source files in place.
 *
 * <p>
 * Only {@code abstract} for Gradle, not to be implemented by users.
 * </p>
 */
@DisableCachingByDefault(because = "Writes to the source files directly")
public abstract non-sealed class HeaderApplyTask
    extends HeaderWorkerSourceTask<HeaderApplyWorkAction, HeaderApplyWorkAction.Parameters>
    implements LevelHeaderedConfig {
    @SuppressWarnings("doclint:missing")
    public HeaderApplyTask() {
        // Our outputs are always up-to-date, given that our inputs haven't changed.
        getOutputs().upToDateWhen(t -> true);
    }

    @Override
    Class<HeaderApplyWorkAction> getWorkActionClass() {
        return HeaderApplyWorkAction.class;
    }
}

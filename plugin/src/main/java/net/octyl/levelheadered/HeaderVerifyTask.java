/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package net.octyl.levelheadered;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.work.DisableCachingByDefault;

/**
 * Task that verifies headers on source files.
 *
 * <p>
 * Only {@code abstract} for Gradle, not to be implemented by users.
 * </p>
 */
@DisableCachingByDefault(because = "Produces no useful output")
public abstract non-sealed class HeaderVerifyTask
    extends HeaderWorkerSourceTask<HeaderVerifyWorkAction, HeaderVerifyWorkAction.Parameters>
    implements LevelHeaderedConfig {
    @SuppressWarnings("doclint:missing")
    public HeaderVerifyTask() {
        // Our outputs are always up-to-date, given that our inputs haven't changed.
        getOutputs().upToDateWhen(t -> true);
    }

    /**
     * {@return the path of the corresponding header apply task} This is the task that can be run to fix
     * any header violations found by this task.
     *
     * <p>
     * If not set, a generic message will be shown instead.
     * </p>
     */
    @Console
    public abstract Property<String> getHeaderApplyTaskPath();

    @Override
    Class<HeaderVerifyWorkAction> getWorkActionClass() {
        return HeaderVerifyWorkAction.class;
    }

    @Override
    void configureParameters(HeaderVerifyWorkAction.Parameters parameters) {
        parameters.getHeaderApplyTaskPath().set(getHeaderApplyTaskPath());
    }
}

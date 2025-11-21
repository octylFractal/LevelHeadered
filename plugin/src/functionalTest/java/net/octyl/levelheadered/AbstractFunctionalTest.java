/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered;

import org.gradle.testkit.runner.GradleRunner;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractFunctionalTest {
    @TempDir
    Path projectDir;

    protected Path getSettingsFile() {
        return projectDir.resolve("settings.gradle.kts");
    }

    protected Path getBuildFile() {
        return projectDir.resolve("build.gradle.kts");
    }

    protected static void writeFile(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    protected void settingsFile(@Language("kotlin") String content) throws IOException {
        writeFile(getSettingsFile(), content);
    }

    protected void buildFile(@Language("kotlin") String content) throws IOException {
        writeFile(getBuildFile(), content);
    }

    protected GradleRunner createGradleRunner(String... args) {
        return GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments(args)
            .withPluginClasspath();
    }
}

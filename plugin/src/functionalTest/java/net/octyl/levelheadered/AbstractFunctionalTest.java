/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered;

import org.gradle.testkit.runner.GradleRunner;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public abstract class AbstractFunctionalTest {
    @TempDir
    Path projectDir;

    protected Path getSettingsFile() {
        return projectDir.resolve("settings.gradle.kts");
    }

    protected Path getBuildFile() {
        return projectDir.resolve("build.gradle.kts");
    }

    protected static String quotedFilePath(String path) {
        return Pattern.quote(path.replace('/', File.separatorChar));
    }

    protected static String withLocalLineSep(String content) {
        return content.replace("\n", System.lineSeparator());
    }

    protected static String withoutLocalLineSep(String content) {
        return content.replace(System.lineSeparator(), "\n");
    }

    protected static void writeFile(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, withLocalLineSep(content));
    }

    protected void settingsFile(@Language("kotlin") String content) throws IOException {
        writeFile(getSettingsFile(), content);
    }

    protected void buildFile(@Language("kotlin") String content) throws IOException {
        writeFile(getBuildFile(), content);
    }

    protected GradleRunner createGradleRunner(String... args) {
        return GradleRunner.create()
            .forwardOutput()
            .withProjectDir(projectDir.toFile())
            .withArguments(args)
            .withPluginClasspath();
    }
}

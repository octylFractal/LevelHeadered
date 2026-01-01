/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for regressions of issues with {@code HeaderApplyTask}.
 */
class HeaderApplyTaskRegressionFunctionalTest extends AbstractFunctionalTest {
    @Test
    void trimsHeaderText() throws IOException {
        settingsFile("");
        buildFile(
            """
            plugins {
                java
                id("net.octyl.level-headered")
            }
            
            levelHeadered.headerTemplate("\\nMy custom header with surrounding whitespace\\n")
            """
        );

        writeFile(
            projectDir.resolve("src/main/java/App.java"),
            """
            public class App {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """
        );

        BuildResult result = createGradleRunner("applyHeader").build();
        BuildTask task = result.task(":applyHeader");
        assertThat(task).isNotNull();
        assertThat(task.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(Files.readString(projectDir.resolve("src/main/java/App.java"))).isEqualTo(withLocalLineSep(
            """
            /*
             * My custom header with surrounding whitespace
             */
            
            public class App {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """
        ));
    }

    @Test
    void noTrailingWhitespace() throws IOException {
        settingsFile("");
        buildFile(
            """
            plugins {
                java
                id("net.octyl.level-headered")
            }
            
            levelHeadered.headerTemplate("My custom header with trailing whitespace     \\n\\nAnd more text")
            """
        );

        writeFile(
            projectDir.resolve("src/main/java/App.java"),
            """
            public class App {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """
        );

        BuildResult result = createGradleRunner("applyHeader").build();
        BuildTask task = result.task(":applyHeader");
        assertThat(task).isNotNull();
        assertThat(task.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(Files.readString(projectDir.resolve("src/main/java/App.java"))).isEqualTo(withLocalLineSep(
            """
            /*
             * My custom header with trailing whitespace
             *
             * And more text
             */
            
            public class App {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """
        ));
    }

    @Test
    void doesntMangleCommentsPastFileStart() throws IOException {
        settingsFile("");
        buildFile(
            """
            plugins {
                java
                id("net.octyl.level-headered")
            }
            
            levelHeadered.headerTemplate("A header")
            """
        );

        writeFile(
            projectDir.resolve("src/main/java/App.java"),
            """
            public class App {
                /*
                 * This main function is really something, isn't it?
                 */
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """
        );

        BuildResult result = createGradleRunner("applyHeader").build();
        BuildTask task = result.task(":applyHeader");
        assertThat(task).isNotNull();
        assertThat(task.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(Files.readString(projectDir.resolve("src/main/java/App.java"))).isEqualTo(withLocalLineSep(
            """
            /*
             * A header
             */
            
            public class App {
                /*
                 * This main function is really something, isn't it?
                 */
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """
        ));
    }

    @Test
    void failsOnMixedLineEndings() throws IOException {
        settingsFile("");
        buildFile(
            """
            plugins {
                java
                id("net.octyl.level-headered")
            }
            
            levelHeadered.headerTemplate("A header")
            """
        );

        Files.createDirectories(projectDir.resolve("src/main/java"));
        Files.writeString(
            projectDir.resolve("src/main/java/App.java"),
            """
            public class App {\r
                // No CR here ->
            }\r
            """
        );
        Files.writeString(
            projectDir.resolve("src/main/java/AppSwapped.java"),
            """
            public class App {
                // Only CR here ->\r
            }
            """
        );

        BuildResult result = createGradleRunner("applyHeader").buildAndFail();
        BuildTask task = result.task(":applyHeader");
        assertThat(task).isNotNull();
        assertThat(task.getOutcome()).isEqualTo(TaskOutcome.FAILED);
        assertThat(withoutLocalLineSep(result.getOutput())).containsMatch(
            """
               > A failure occurred while executing net\\.octyl\\.levelheadered\\.HeaderApplyWorkAction
                  > Failed to apply header
                      Multiple line endings detected in content
                        File: .+%s
            """.formatted(quotedFilePath("src/main/java/App.java"))
        );
        assertThat(withoutLocalLineSep(result.getOutput())).containsMatch(
            """
               > A failure occurred while executing net\\.octyl\\.levelheadered\\.HeaderApplyWorkAction
                  > Failed to apply header
                      Multiple line endings detected in content
                        File: .+%s
            """.formatted(quotedFilePath("src/main/java/AppSwapped.java"))
        );
    }
}

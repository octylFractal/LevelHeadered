/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package net.octyl.levelheadered;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;

/**
 * Verifies functionality of the Level Headered plugin when applied to a Java project.
 */
class WithJavaPluginFunctionalTest extends AbstractFunctionalTest {
    private static final String HEADER_TEXT_AS_EMBEDDABLE_STRING =
        "My custom header\\\\nWith other text too.";
    private static final String BASE_CONTENT =
        """
        public class App {
            public static void main(String[] args) {
                System.out.println("Hello, World!");
            }
        }
        """;
    private static final String WITH_HEADER_CONTENT =
        """
        /*
         * My custom header
         * With other text too.
         */
        
        public class App {
            public static void main(String[] args) {
                System.out.println("Hello, World!");
            }
        }
        """;
    private static final String WITH_JAVADOC_CONTENT =
        """
        /**
         * This is an app that says hello to the world.
         */
        """ + BASE_CONTENT;
    private static final String WITH_JAVADOC_AND_HEADER_CONTENT =
        """
        /*
         * My custom header
         * With other text too.
         */
        
        /**
         * This is an app that says hello to the world.
         */
        """ + BASE_CONTENT;
    private static final String FILE_PATH = "src/main/java/App.java";
    private static final String FILE_PATH_TEST = "src/test/java/App.java";

    enum TestCase {
        BASE(BASE_CONTENT, WITH_HEADER_CONTENT),
        JAVADOC(WITH_JAVADOC_CONTENT, WITH_JAVADOC_AND_HEADER_CONTENT);

        final String withoutHeader;
        final String withHeader;

        TestCase(String withoutHeader, String withHeader) {
            this.withoutHeader = withoutHeader;
            this.withHeader = withHeader;
        }
    }

    @ParameterizedTest
    @EnumSource(TestCase.class)
    void appliesHeaderToJavaFile(TestCase testCase) throws IOException {
        settingsFile("");
        buildFile(
            """
            plugins {
                java
                id("net.octyl.level-headered")
            }
            
            levelHeadered.headerTemplate("%s")
            """.formatted(HEADER_TEXT_AS_EMBEDDABLE_STRING)
        );
        writeFile(projectDir.resolve(FILE_PATH), testCase.withoutHeader);
        writeFile(projectDir.resolve(FILE_PATH_TEST), testCase.withoutHeader);

        BuildResult result = createGradleRunner("applyHeader", "-iS").build();
        assertThat(result.getOutput()).containsMatch(
            "Processing ADDED file: .*/" + Pattern.quote(FILE_PATH)
        );

        String modifiedContent = Files.readString(projectDir.resolve(FILE_PATH));
        assertThat(modifiedContent).isEqualTo(testCase.withHeader);

        // It takes one extra cycle to get to UP_TO_DATE, as we change the file (which is an input) in the first run.
        result = createGradleRunner("applyHeader", "-iS").build();
        assertThat(result.task(":applyHeader").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        result = createGradleRunner("applyHeader", "-iS").build();
        assertThat(result.task(":applyHeader").getOutcome()).isEqualTo(TaskOutcome.UP_TO_DATE);
        // Verify content is unchanged
        assertThat(modifiedContent).isEqualTo(testCase.withHeader);
        // Verify test file is not yet modified
        assertThat(Files.readString(projectDir.resolve(FILE_PATH_TEST))).isEqualTo(testCase.withoutHeader);

        // Now apply header to test sources
        result = createGradleRunner("applyTestHeader", "-iS").build();
        assertThat(result.getOutput()).containsMatch(
            "Processing ADDED file: .*/" + Pattern.quote(FILE_PATH_TEST)
        );
        modifiedContent = Files.readString(projectDir.resolve(FILE_PATH_TEST));
        assertThat(modifiedContent).isEqualTo(testCase.withHeader);
    }

    @ParameterizedTest
    @EnumSource(TestCase.class)
    void verifiesValidHeaderInJavaFile(TestCase testCase) throws IOException {
        settingsFile("");
        buildFile(
            """
            plugins {
                java
                id("net.octyl.level-headered")
            }
            
            levelHeadered.headerTemplate("%s")
            """.formatted(HEADER_TEXT_AS_EMBEDDABLE_STRING)
        );
        writeFile(projectDir.resolve(FILE_PATH), testCase.withHeader);

        BuildResult result = createGradleRunner("verifyHeader", "-iS").build();
        assertThat(result.task(":verifyHeader").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput()).containsMatch(
            "Processing ADDED file: .*/" + Pattern.quote(FILE_PATH)
        );

        result = createGradleRunner("verifyHeader", "-iS").build();
        assertThat(result.task(":verifyHeader").getOutcome()).isEqualTo(TaskOutcome.UP_TO_DATE);
    }

    @ParameterizedTest
    @EnumSource(TestCase.class)
    void throwsOnInvalidHeaderInJavaFile(TestCase testCase) throws IOException {
        settingsFile("");
        buildFile(
            """
            plugins {
                java
                id("net.octyl.level-headered")
            }
            
            levelHeadered.headerTemplate("%s")
            """.formatted(HEADER_TEXT_AS_EMBEDDABLE_STRING)
        );
        writeFile(projectDir.resolve(FILE_PATH), testCase.withoutHeader);

        BuildResult result = createGradleRunner("verifyHeader", "-iS").buildAndFail();
        assertThat(result.task(":verifyHeader").getOutcome()).isEqualTo(TaskOutcome.FAILED);
        assertThat(result.getOutput()).containsMatch(
            "Header verification failed for file: .*/" + Pattern.quote(FILE_PATH) +
                "\\. Run the :applyHeader task to fix this\\."
        );
    }

    @ParameterizedTest
    @EnumSource(TestCase.class)
    void onlyVerifiesInApplicableSourceSet(TestCase testCase) throws IOException {
        settingsFile("");
        buildFile(
            """
            plugins {
                java
                id("net.octyl.level-headered")
            }
            
            levelHeadered.headerTemplate("%s")
            """.formatted(HEADER_TEXT_AS_EMBEDDABLE_STRING)
        );
        writeFile(projectDir.resolve(FILE_PATH), testCase.withHeader);
        writeFile(projectDir.resolve(FILE_PATH_TEST), testCase.withoutHeader);

        BuildResult result = createGradleRunner("verifyHeader", "-iS").build();
        assertThat(result.task(":verifyHeader").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput()).containsMatch(
            "Processing ADDED file: .*/" + Pattern.quote(FILE_PATH)
        );
        assertThat(result.getOutput()).doesNotContain(
            "Processing ADDED file: .*/" + Pattern.quote(FILE_PATH_TEST)
        );
    }


    @ParameterizedTest
    @EnumSource(TestCase.class)
    void onlyAppliesToApplicableFiles(TestCase testCase) throws IOException {
        settingsFile("");
        buildFile(
            """
            plugins {
                java
                id("net.octyl.level-headered")
            }
            
            levelHeadered.headerTemplate("%s")
            levelHeadered.sourceMatchPatterns {
                exclude("**/CustomHeaderedFile.java")
            }
            """.formatted(HEADER_TEXT_AS_EMBEDDABLE_STRING)
        );
        writeFile(projectDir.resolve(FILE_PATH), testCase.withoutHeader);
        writeFile(projectDir.resolve("src/main/java/CustomHeaderedFile.java"), testCase.withoutHeader);

        BuildResult result = createGradleRunner("applyHeader", "-iS").build();
        assertThat(result.task(":applyHeader").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput()).containsMatch(
            "Processing ADDED file: .*/" + Pattern.quote(FILE_PATH)
        );
        assertThat(result.getOutput()).doesNotContain(
            "Processing ADDED file: .*/" + Pattern.quote("src/main/java/CustomHeaderedFile.java")
        );

        assertThat(Files.readString(projectDir.resolve(FILE_PATH))).isEqualTo(testCase.withHeader);
        assertThat(Files.readString(projectDir.resolve("src/main/java/CustomHeaderedFile.java")))
            .isEqualTo(testCase.withoutHeader);
    }
    @ParameterizedTest
    @EnumSource(TestCase.class)
    void onlyVerifiesApplicableFiles(TestCase testCase) throws IOException {
        settingsFile("");
        buildFile(
            """
            plugins {
                java
                id("net.octyl.level-headered")
            }
            
            levelHeadered.headerTemplate("%s")
            levelHeadered.sourceMatchPatterns {
                exclude("**/CustomHeaderedFile.java")
            }
            """.formatted(HEADER_TEXT_AS_EMBEDDABLE_STRING)
        );
        writeFile(projectDir.resolve(FILE_PATH), testCase.withHeader);
        writeFile(projectDir.resolve("src/main/java/CustomHeaderedFile.java"), testCase.withoutHeader);

        BuildResult result = createGradleRunner("verifyHeader", "-iS").build();
        assertThat(result.task(":verifyHeader").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput()).containsMatch(
            "Processing ADDED file: .*/" + Pattern.quote(FILE_PATH)
        );
        assertThat(result.getOutput()).doesNotContain(
            "Processing ADDED file: .*/" + Pattern.quote("src/main/java/CustomHeaderedFile.java")
        );
    }
}

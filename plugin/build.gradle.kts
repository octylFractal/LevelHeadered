/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "2.0.0"
    id("net.octyl.level-headered") version "0.1.0-SNAPSHOT"
}

levelHeadered {
    headerTemplate(rootProject.file("HEADER.txt"))
}

repositories {
    mavenCentral()
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.intellij.annotations)
    testImplementation(libs.truth)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    components {
        withModule("com.google.truth:truth") {
            withVariant("compile") {
                withDependencies {
                    // junit is actually a runtime-only dependency
                    // See https://github.com/google/truth/issues/333
                    removeAll { it.group == "junit" }
                }
            }
        }
    }
}

gradlePlugin {
    website = "https://github.com/octylFractal/LevelHeadered"
    vcsUrl = "https://github.com/octylFractal/LevelHeadered"

    plugins {
        create("levelHeaderedBase") {
            id = "net.octyl.level-headered.base"
            implementationClass = "net.octyl.levelheadered.LevelHeaderedBasePlugin"
            displayName = "LevelHeadered Base Plugin"
            description = "Base plugin for LevelHeadered, a file header management plugin," +
                    " configures tasks with conventions"
            tags = listOf("header", "license")
        }
        create("levelHeadered") {
            id = "net.octyl.level-headered"
            implementationClass = "net.octyl.levelheadered.LevelHeaderedPlugin"
            displayName = "LevelHeadered Plugin"
            description = "Simple interface for LevelHeadered, a file header management plugin," +
                    " activates applying and verifying headers when certain plugins are present"
            tags = listOf("header", "license")
        }
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.check {
    dependsOn(functionalTest)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters", "-Werror"))
}

tasks.javadoc {
    val options = options as StandardJavadocDocletOptions
    options.encoding = "UTF-8"
    options.addBooleanOption("Werror", true)

    // Allow @SuppressWarnings with doclint
    javadocTool = javaToolchains.javadocToolFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

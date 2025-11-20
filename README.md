<!--
SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
SPDX-License-Identifier: CC-BY-NC-SA-4.0
-->

LevelHeadered
=============
Keep a level head while maintaining your file headers.

A file header management Gradle plugin. Primarily designed to apply license headers to source code files.

# Usage

See how to apply the plugin on the Gradle Plugin Portal: [
![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/net.octyl.level-headered)
](https://plugins.gradle.org/plugin/net.octyl.level-headered)

Then, for Java projects, just configure the header template and any other necessary options:
```kotlin
levelHeadered {
    // This is the only required property. All others come with sensible defaults.
    headerTemplate("My header")
    // or: headerTemplate(file("path/to/header.txt"))
    // or: set any TextResource on getHeaderTemplate()

    // Variables replaced in the header template, using Groovy's SimpleTemplateEngine.
    templateVariables.put("version", "${project.version}")

    // Customize which files get headers applied/verified.
    // Defaults to any files that come from source directories and match a rewriter.
    sourceMatchPatterns {
        include("**/*.java")
        include("**/*.kt")
        exclude("**/generated/**")
    }

    // Configure file header rewriters as needed, defaults can be found in LevelHeaderedBasePlugin
    // Feel free to PR more defaults if they're obvious.
    rewriters.put(".myownfile", StandardFileHeaderRewriter.SLASH_STAR_COMMENT)
}
```

Headers will then be verified as part of `check` (or individually via `verifyHeader`, `verifyTestHeader`, etc.),
and can be applied via `applyHeaderToAll` (or individually via `applyHeader`, `applyTestHeader`, etc.).

You can also apply the [base plugin](https://plugins.gradle.org/plugin/net.octyl.level-headered.base) and then create
your own `HeaderApplyTask` or `HeaderVerifyTask` if you need more customization.

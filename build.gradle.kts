/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

plugins {
    id("net.researchgate.release") version "3.1.0"
}

release {
    tagTemplate = $$"v${version}"
    git {
        requireBranch = "master"
        signTag = true
    }
}

tasks.afterReleaseBuild {
    dependsOn(":plugin:publishPlugins")
}

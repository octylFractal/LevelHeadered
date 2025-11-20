/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: AGPL-3.0-or-later
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

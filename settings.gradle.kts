/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal {
            mavenContent {
                includeGroupAndSubgroups("net.octyl.level-headered")
            }
        }
    }
}

rootProject.name = "LevelHeadered"
include("plugin")

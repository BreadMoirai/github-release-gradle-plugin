/*
 *    Copyright 2017 - 2018 BreadMoirai (Ton Ly)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.breadmoirai

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

class GithubReleasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def ext = project.extensions.create('githubRelease', GithubReleaseExtension, project)

        project.tasks.create('githubRelease', GithubReleaseTask) {
            it.with {
                setToken ext.tokenProvider
                setOwner ext.ownerProvider
                setRepo ext.repoProvider
                setTagName ext.tagNameProvider
                setTargetCommitish ext.targetCommitishProvider
                setReleaseName ext.releaseNameProvider
                setBody ext.bodyProvider
                setDraft ext.draftProvider
                setPrerelease ext.prereleaseProvider
                setReleaseAssets ext.releaseAssets
            }
        }

        project.afterEvaluate {
            def self = project.plugins.findPlugin(GithubReleasePlugin)
            if (self) {
                GithubReleaseExtension e = project.extensions.getByType(GithubReleaseExtension)
                setOrElse(e.owner) {
                    def group = project.group.toString()
                    group.substring(group.lastIndexOf('.') + 1)
                }
                setOrElse(e.repo) {
                    project.name ?: project.rootProject?.name ?: project.rootProject?.rootProject?.name
                }
                setOrElse(e.tagName) { "v${project.version}" }
                setOrElse(e.targetCommitish) { 'master' }
                setOrElse(e.releaseName) { "${project.version}" }
                setOrElse(e.body) { "" }
                setOrElse(e.draft) { false }
                setOrElse(e.prerelease) { false }
                setOrElse(e.token) { "" }
            }
        }
    }

    private static <P, V extends P> void setOrElse(Property<P> prop, Closure<V> value) {
        if (!prop.isPresent()) prop.set(value())
    }
}
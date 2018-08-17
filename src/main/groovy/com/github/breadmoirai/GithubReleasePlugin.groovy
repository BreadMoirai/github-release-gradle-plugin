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
import org.gradle.api.provider.Provider

import java.util.concurrent.Callable

class GithubReleasePlugin implements Plugin<Project> {

    public static boolean infoEnabled = false

    @Override
    void apply(Project project) {
        infoEnabled = project.logger.infoEnabled

        def ext = project.extensions.create('githubRelease', GithubReleaseExtension, project)

        project.tasks.create('githubRelease', GithubReleaseTask) {
            it.with {
                setAuthorization ext.authorizationProvider
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
                setOrElse(e.owner, CharSequence.class) {
                    def group = project.group.toString()
                    group.substring(group.lastIndexOf('.') + 1)
                }
                setOrElse(e.repo, CharSequence.class) {
                    project.name ?: project.rootProject?.name ?: project.rootProject?.rootProject?.name
                }
                setOrElse(e.tagName, CharSequence.class) { "v${project.version}" }
                setOrElse(e.targetCommitish, CharSequence.class) { 'master' }
                setOrElse(e.releaseName, CharSequence.class) { "v${project.version}" }
                setOrElse(e.body, CharSequence.class, new ChangeLogProvider())
                setOrElse(e.draft, Boolean.class) { false }
                setOrElse(e.prerelease, Boolean.class) { false }
                setOrElse(e.authorization, CharSequence.class) {
                    GithubLoginApp.start()
                    GithubLoginApp.waitForResult().map{
                        "Basic $it"
                    }.orElse(null)
                }
            }

        }
    }

    static def <T> void setOrElse(Property<T> prop, Class<T> type, Callable<T> value) {
        if (!prop.isPresent()) {
            prop.set(new TypedDefaultProvider<T>(type, value))
        }
    }

    static def <T> void setOrElse(Property<T> prop, Class<T> type, Provider<T> value) {
        if (!prop.isPresent()) {
            prop.set(new TypedDefaultProvider<T>(type, value))
        }
    }
}
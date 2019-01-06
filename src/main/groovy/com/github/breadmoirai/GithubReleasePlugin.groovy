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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Callable

class GithubReleasePlugin implements Plugin<Project> {

    private final static Logger log = LoggerFactory.getLogger(GithubReleasePlugin.class)
    public static boolean infoEnabled = false
    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        infoEnabled = project.logger.infoEnabled

        log.debug("Creating Extension githubRelease")
        def ext = project.extensions.create('githubRelease', GithubReleaseExtension, project)

        project.tasks.create('githubRelease', GithubReleaseTask) {
            log.debug("Creating Task githubRelease")
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
                setOverwrite ext.overwriteProvider
                setAllowUploadToExisting ext.allowUploadToExistingProvider
            }
        }

        project.afterEvaluate {
            def self = project.plugins.findPlugin(GithubReleasePlugin)

            if (self) {
                log.debug("Assigning default values for GithubReleasePlugin")
                GithubReleaseExtension e = project.extensions.getByType(GithubReleaseExtension)
                setOrElse("owner", e.owner, CharSequence.class) {
                    def group = project.group.toString()
                    group.substring(group.lastIndexOf('.') + 1)
                }
                setOrElse("repo", e.repo, CharSequence.class) {
                    project.name ?: project.rootProject?.name ?: project.rootProject?.rootProject?.name
                }
                setOrElse("tagName", e.tagName, CharSequence.class) { "v${project.version}" }
                setOrElse("targetCommitish", e.targetCommitish, CharSequence.class) { 'master' }
                setOrElse("releaseName", e.releaseName, CharSequence.class) {
                    e.tagName.get() }
                setOrElse("draft", e.draft, Boolean.class) { false }
                setOrElse("prerelease", e.prerelease, Boolean.class) { false }
                setOrElse("authorization", e.authorization, CharSequence.class) {
                    //new GithubLoginApp().awaitResult().map{result -> "Basic $result"}.get()
                    return null
                }
                setOrElse("body", e.body, CharSequence.class, new ChangeLogSupplier(e, project))
                setOrElse("overwrite", e.overwrite, Boolean.class) { false }
                setOrElse("allowUploadToExisting", e.allowUploadToExisting, Boolean.class) { false }
            }

        }
    }

    def <T> void setOrElse(String name, Property<T> prop, Class<T> type, Callable<T> value) {
        if (!prop.isPresent()) {
            prop.set(project.provider(value))
        }
    }

}

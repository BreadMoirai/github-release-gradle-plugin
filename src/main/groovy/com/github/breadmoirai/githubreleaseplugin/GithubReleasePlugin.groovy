/*
 * Copyright (c) 2017 - 2022 BreadMoirai (Ton Ly)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.breadmoirai.githubreleaseplugin


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GithubReleasePlugin implements Plugin<Project> {

    private Project project
    private GithubReleaseTask task

    @Override
    void apply(Project project) {
        this.project = project

        def ext = project.extensions.create('githubRelease', GithubReleaseExtension, project)

        task = project.tasks.register('githubRelease', GithubReleaseTask) {
            it.with {
                setAuthorization ext.authorizationProvider
                setOwner ext.ownerProvider
                setRepo ext.repoProvider
                setTagName ext.tagNameProvider
                setTargetCommitish ext.targetCommitishProvider
                setReleaseName ext.releaseNameProvider
                setGenerateReleaseNotes ext.generateReleaseNotesProvider
                setBody ext.bodyProvider
                setDraft ext.draftProvider
                setPrerelease ext.prereleaseProvider
                setReleaseAssets ext.releaseAssets
                setOverwrite ext.overwriteProvider
                setAllowUploadToExisting ext.allowUploadToExistingProvider
                setDryRun ext.dryRunProvider
                setApiEndpoint ext.apiEndpointProvider
            }
        } as GithubReleaseTask
    }

}

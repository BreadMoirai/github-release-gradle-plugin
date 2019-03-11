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

package com.github.breadmoirai.githubreleaseplugin

import com.github.breadmoirai.githubreleaseplugin.ast.ExtensionClass
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets

@ExtensionClass
class GithubReleaseTask extends DefaultTask {

    @Input
    final Property<CharSequence> owner
    @Input
    final Property<CharSequence> repo
    @Input
    final Property<CharSequence> authorization
    @Input
    final Property<CharSequence> tagName
    @Input
    final Property<CharSequence> targetCommitish
    @Input
    final Property<CharSequence> releaseName
    @Input
    final Property<CharSequence> body
    @Input
    final Property<Boolean> draft
    @Input
    final Property<Boolean> prerelease
    @InputFiles
    final ConfigurableFileCollection releaseAssets
    @Input
    final Property<Boolean> overwrite
    @Input
    final Property<Boolean> allowUploadToExisting

    final Project project

    GithubReleaseTask() {
        this.project = super.project
        this.setGroup('publishing')
        final ObjectFactory objectFactory = project.objects
        owner = objectFactory.property(CharSequence)
        repo = objectFactory.property(CharSequence)
        authorization = objectFactory.property(CharSequence)
        tagName = objectFactory.property(CharSequence)
        targetCommitish = objectFactory.property(CharSequence)
        releaseName = objectFactory.property(CharSequence)
        body = objectFactory.property(CharSequence)
        draft = objectFactory.property(Boolean)
        prerelease = objectFactory.property(Boolean)
        releaseAssets = project.files()
        overwrite = objectFactory.property(Boolean)
        allowUploadToExisting = objectFactory.property(Boolean)
    }

    void setReleaseAssets(Object... assets) {
        this.releaseAssets.setFrom(assets)
    }

    @TaskAction
    void publishRelease() {
        final CharSequence authValue = authorization.get()
        final GithubApi api = new GithubApi(authValue)
        final CharSequence ownerValue = owner.get()
        final CharSequence repoValue = repo.get()
        final CharSequence tagValue = tagName.get()

        def previousRelease = api.findReleaseByTag ownerValue, repoValue, tagValue
        switch (previousRelease.code) {
            case 200:
                println ":githubRelease EXISTING RELEASE FOUND ${previousRelease.body.html_url}"
                if (this.overwrite.get()) {
                    deleteRelease(api, previousRelease)
                    createRelease(api, ownerValue, repoValue, tagValue)
                } else if (this.allowUploadToExisting.get() && (releaseAssets.size() > 0)) {
                    println ':githubRelease UPLOADING ASSETS TO EXISTING RELEASE'
                    uploadAssetsToUrl api, previousRelease.body.upload_url as String
                } else {
                    throw new Error(':githubRelease FAILED RELEASE ALREADY EXISTS')
                }
                break
            case 404:
                createRelease(api, ownerValue, repoValue, tagValue)
                break
            default:
                throw new Error(":githubRelease FAILED $previousRelease.code $previousRelease.message\n$previousRelease.body")
        }
    }

    private static void deleteRelease(GithubApi api, GithubApi.Response previousRelease) throws Error {
        def response = api.deleteReleaseByUrl previousRelease.body.url as String
        switch (response.code) {
            case 404:
                throw new Error("404 Repository at ${previousRelease.body.url} was not found")
            case 204:
                break
            default:
                throw new Error("Couldn't delete old release: $response.code $response.message\n$response.body")
        }
    }

    private void createRelease(GithubApi api, CharSequence ownerValue, CharSequence repoValue, CharSequence tagValue) {
        def response = api.postRelease ownerValue.toString(), repoValue.toString(), [
                tag_name        : tagValue,
                target_commitish: targetCommitish.get(),
                name            : releaseName.get(),
                body            : body.get(),
                draft           : draft.get(),
                prerelease      : prerelease.get()
        ]
        if (response.code != 201) {
            if (response.code == 404) {
                throw new Error("404 Repository with Owner: '${ownerValue}' and Name: '${repoValue}' was not found")
            }
            throw new Error("Could not create release: $response.code $response.message\n$response.body")
        } else {
            println ":githubRelease STATUS ${response.message.toUpperCase()}"
            println ":githubRelease $response.body.html_url"
            if (releaseAssets.size() > 0) {
                println ':githubRelease UPLOADING ASSETS'
                uploadAssetsToUrl api, response.body.upload_url as String
            }
        }
    }

    private void uploadAssetsToUrl(GithubApi api, String url) {
        releaseAssets.files.each { asset ->
            url = url.replace '{?name,label}', "?name=${URLEncoder.encode(asset.name, StandardCharsets.UTF_8.displayName())}"
            def response = api.uploadFileToUrl url, asset
            if (response.code != 201) {
                System.err.println ":githubRelease FAILED TO UPLOAD $asset.name\n$response.code $response.message\n$response.body"
            }
        }
    }

}

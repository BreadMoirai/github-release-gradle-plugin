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
    final Property<String> owner
    @Input
    final Property<String> repo
    @Input
    final Property<String> authorization
    @Input
    final Property<String> tagName
    @Input
    final Property<String> targetCommitish
    @Input
    final Property<String> releaseName
    @Input
    final Property<String> body
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

    GithubReleaseTask() {
        this.setGroup('publishing')
        final ObjectFactory objectFactory = project.objects
        owner = objectFactory.property(String)
        repo = objectFactory.property(String)
        authorization = objectFactory.property(String)
        tagName = objectFactory.property(String)
        targetCommitish = objectFactory.property(String)
        releaseName = objectFactory.property(String)
        body = objectFactory.property(String)
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
        def api = new GithubApi(authorization.getOrThrow())
        def ownerValue = owner.getOrThrow()
        def repoValue = repo.getOrThrow()
        def tagValue = tagName.getOrThrow()
        def previousRelease = api.findGithubReleaseByTag ownerValue, repoValue, tagValue
        if (previousRelease.code == 200) {
            println ":githubRelease EXISTING RELEASE FOUND ${previousRelease.body.html_url}"
            if (this.overwrite.getOrThrow()) {
                def response = api.deleteGithubReleaseByUrl previousRelease.body.url as String
                if (response.code != 204) {
                    if (response.code == 404) {
                        throw new Error("404 Repository at ${previousRelease.body.url} was not found")
                    }
                    throw new Error("Couldn't delete old release: $response.code $response.message\n$response.body")
                }
            } else if (this.allowUploadToExisting.getOrThrow() && (releaseAssets.size() > 0)) {
                println ':githubRelease UPLOADING ASSETS TO EXISTING RELEASE'
                uploadAssetsToUrl api, previousRelease.body.upload_url as String
            } else {
                throw new Error(':githubRelease FAILED RELEASE ALREADY EXISTS')
            }
        } else if (previousRelease.code == 404) {
            def response = api.postRelease ownerValue as String, repoValue as String, [
                    tag_name        : tagValue,
                    target_commitish: targetCommitish.getOrThrow(),
                    name            : releaseName.getOrThrow(),
                    body            : body.getOrThrow(),
                    draft           : draft.getOrThrow(),
                    prerelease      : prerelease.getOrThrow()
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
        } else {
            throw new Error(":githubRelease FAILED $previousRelease.code $previousRelease.message\n$previousRelease.body")
        }
    }

    void uploadAssetsToUrl(GithubApi api, String url) {
        releaseAssets.files.each { asset ->
            url = url.replace '{?name,label}', "?name=${URLEncoder.encode(asset.name, StandardCharsets.UTF_8.displayName())}"
            def response = api.uploadFileToUrl url, asset
            if (response.code != 201) {
                System.err.println ":githubRelease FAILED TO UPLOAD $asset.name\n$response.code $response.message\n$response.body"
            }
        }
    }

}

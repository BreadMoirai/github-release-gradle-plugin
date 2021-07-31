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
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable

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
    @Input
    final Property<Boolean> dryRun
    @Input
    final Property<CharSequence> apiEndpoint

    private final ChangeLogSupplier changeLogSupplier
    @Internal
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
        dryRun = objectFactory.property(Boolean)
        apiEndpoint = objectFactory.property(CharSequence)

        owner {
            def group = project.group.toString()
            return group.substring(group.lastIndexOf('.') + 1)
        }
        repo {
            project.name ?: project.rootProject?.name ?: project.rootProject?.rootProject?.name
        }
        tagName { "v${project.version}" }
        targetCommitish { 'master' }
        releaseName { "v${project.version}" }
        draft { true }
        prerelease { false }
        // authorization has no default value
        body { "" }
        overwrite { false }
        allowUploadToExisting { false }
        apiEndpoint { GithubApi.endpoint }
        dryRun { false }
        changeLogSupplier = new ChangeLogSupplier(project, owner, repo, authorization, tagName, dryRun)
    }

    private void log(String message) {
        if (dryRun.get())
            println ":githubRelease [$message]"
        else
            println ":githubRelease $message"
    }

    void setReleaseAssets(Object... assets) {
        this.releaseAssets.setFrom(assets)
    }

    void releaseAssets(Object... assets) {
        this.releaseAssets.setFrom(assets)
    }

    void setToken(CharSequence token) {
        this.authorization.set("Token $token")
    }

    void token(CharSequence token) {
        this.authorization.set("Token $token")
    }

    void setToken(Provider<? extends CharSequence> token) {
        this.authorization.set(token.map { "Token $it" })
    }

    void token(Provider<? extends CharSequence> token) {
        this.authorization.set(token.map { "Token $it" })
    }

    void setToken(Callable<? extends CharSequence> token) {
        this.authorization.set(project.provider(token).map { "Token $it" })
    }

    void token(Callable<? extends CharSequence> token) {
        this.authorization.set(project.provider(token).map { "Token $it" })
    }

    Callable<String> changelog(@DelegatesTo(ChangeLogSupplier) final Closure closure) {
        changeLogSupplier.with closure
        return changeLogSupplier
    }

    Callable<String> changelog() {
        return changeLogSupplier
    }

    @TaskAction
    void publishRelease() {
        try {
            if (dryRun.get()) {
                log "This task is a dry run. All API calls that would modify the repo are disabled. API calls that access the repo information are not disabled. Use this to show what actions would be executed."
            }
            GithubApi.endpoint = apiEndpoint.get()
            final CharSequence authValue = authorization.get()
            final GithubApi api = new GithubApi(authValue)
            final CharSequence ownerValue = owner.get()
            final CharSequence repoValue = repo.get()
            final CharSequence tagValue = tagName.get()
            log 'CHECKING FOR PREVIOUS RELEASE'
            def previousRelease = api.findReleaseByTag ownerValue, repoValue, tagValue
            switch (previousRelease.code) {
                case 200:
                    log "EXISTING RELEASE FOUND ${previousRelease.body.html_url}"
                    if (this.overwrite.get()) {
                        deleteRelease(api, previousRelease)
                        createRelease(api, ownerValue, repoValue, tagValue)
                    } else if (this.allowUploadToExisting.get() && (releaseAssets.size() > 0)) {
                        log 'UPLOADING ASSETS TO EXISTING RELEASE'
                        uploadAssetsToUrl api, previousRelease.body.upload_url as String
                        if (!draft.get() && !previousRelease.body.draft) {
                            log "PUBLISHING RELEASE"

                            def publishResponse = api.publishRelease(previousRelease.body.url as String)
                            if (publishResponse.code != 200) {
                                throw new Error("Could not update Release with draft=false: " +
                                        "$publishResponse.code $publishResponse.message/n$publishResponse.body")
                            }
                        }
                    } else if (previousRelease.body.draft && !draft.get()) {
                        log "PUBLISHING RELEASE"
                        def publishResponse = api.publishRelease(previousRelease.body.url as String)
                        if (publishResponse.code != 200) {
                            throw new Error("Could not update Release with draft=false: " +
                                    "$publishResponse.code $publishResponse.message/n$publishResponse.body")
                        }
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
        catch (Exception e) {
            e.printStackTrace()
        }
    }

    private void deleteRelease(GithubApi api, GithubApi.Response previousRelease) throws Error {
        log "DELETING RELEASE ${previousRelease.body.name}"
        if (dryRun.get())
            return

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
        def values = [
                tag_name        : tagValue,
                target_commitish: targetCommitish.get(),
                name            : releaseName.get(),
                body            : body.get(),
                draft           : true,
                prerelease      : prerelease.get()
        ]
        log """CREATING NEW RELEASE 
{
    tag_name         = ${tagValue}
    target_commitish = ${targetCommitish.get()}
    name             = ${releaseName.get()}
    body             = 
        ${body.get().replace('\n': '\n\t\t')}
    draft            = ${draft.get()}
    prerelease       = ${prerelease.get()}
}"""
        if (dryRun.get()) {
            uploadAssetsToUrl null, null
            return
        }

        def response = api.postRelease ownerValue.toString(), repoValue.toString(), values
        if (response.code != 201) {
            if (response.code == 404) {
                throw new Error("404 Repository with Owner: '${ownerValue}' and Name: '${repoValue}' was not found")
            }
            throw new Error("Could not create release: $response.code $response.message\n$response.body")
        } else {
            log "STATUS ${response.message.toUpperCase()}"
            log "$response.body.html_url"
            if (releaseAssets.size() > 0) {
                log 'UPLOADING ASSETS'
                uploadAssetsToUrl api, response.body.upload_url as String
            }
            if (!draft.get()) {
                log "PUBLISHING RELEASE"
                def publishResponse = api.publishRelease(response.body.url as String)
                if (publishResponse.code != 200) {
                    throw new Error("Could not update Release with draft=false: " +
                        "$publishResponse.code $publishResponse.message/n$publishResponse.body")
                }
            }
        }
    }

    private void uploadAssetsToUrl(GithubApi api, String url) {
        for (asset in releaseAssets.files) {
            log 'UPLOADING ' + project.projectDir.toPath().resolve(asset.toPath())
            if (asset.size() == 0) {
                log "CANNOT UPLOAD ${asset.name} with file size 0"
                continue
            }
            if (dryRun.get()) continue
            def encodedUrl = url.replace '{?name,label}', "?name=${URLEncoder.encode(asset.name, StandardCharsets.UTF_8.displayName())}"
            def response = api.uploadFileToUrl encodedUrl, asset
            if (response.code != 201) {
                System.err.println ":githubRelease FAILED TO UPLOAD $asset.name\n$response.code $response.message\n$response.body"
            }
        }
    }
}


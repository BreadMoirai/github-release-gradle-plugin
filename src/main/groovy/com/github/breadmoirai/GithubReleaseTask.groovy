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

import com.j256.simplemagic.ContentInfo
import com.j256.simplemagic.ContentInfoUtil
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject

class GithubReleaseTask extends DefaultTask {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8")

    @Input final Provider<String> owner
    @Input final Provider<String> repo
    @Input final Provider<String> token
    @Input final Provider<String> tagName
    @Input final Provider<String> targetCommitish
    @Input final Provider<String> releaseName
    @Input final Provider<String> body
    @Input final Provider<Boolean> draft
    @Input final Provider<Boolean> prerelease
    @InputFiles final ConfigurableFileCollection releaseAssets

    GithubReleaseTask() {
        print("wow")
        this.setGroup('publishing')
        final ObjectFactory objectFactory = project.objects
        owner = objectFactory.property(String)
        repo = objectFactory.property(String)
        token = objectFactory.property(String)
        tagName = objectFactory.property(String)
        targetCommitish = objectFactory.property(String)
        releaseName = objectFactory.property(String)
        body = objectFactory.property(String)
        draft = objectFactory.property(Boolean)
        prerelease = objectFactory.property(Boolean)
        releaseAssets = project.files()
    }

    @TaskAction
    void publishRelease() {
        OkHttpClient client = new OkHttpClient()

        def (
            String tag,
            String tar,
            String rel,
            String bod,
            String own,
            String rep,
            boolean dra,
            boolean pre,
            String tok
        ) = getVariables()
        FileCollection releaseAssets = releaseAssets

        Response response1 = checkForPreviousRelease(tok, own, rep, tag, client)
        if (response1.code() == 200) {
            println ':githubRelease PREVIOUS RELEASE EXISTS'
            deletePreviousRelease(response1, tok, own, rep, client)
        }

        Response response3 = createRelease(tag, tar, rel, bod, dra, pre, tok, own, rep, client)
        uploadAssets(response3, tok, own, rep, releaseAssets, client)
    }

    private List getVariables() {
        String tag = tagName.getOrElse("v$project.version")
        String tar = targetCommitish.getOrElse('master')
        String rel = releaseName.getOrElse(tag)
        String bod = this.body.getOrElse('')
        String group = project.group.toString()
        String own = this.owner.getOrElse(group.substring(group.lastIndexOf('.') + 1))
        String rep = this.repo.getOrElse(project.name) ?: project.rootProject?.name ?: project.rootProject?.rootProject?.name
        boolean dra = draft.getOrElse(false)
        boolean pre = prerelease.getOrElse(false)
        String tok = this.token.getOrNull()
        if (tok == null) throw new MissingPropertyException("Field 'token' is not set for githubRelease", 'token', String)
        return [tag, tar, rel, bod, own, rep, dra, pre, tok]
    }

    private static Response checkForPreviousRelease(String tok, String own, String rep, String tag, OkHttpClient client) {
        String releaseUrl = "https://api.github.com/repos/${own}/${rep}/releases/tags/${tag}"
        println ':githubRelease CHECKING FOR PREVIOUS RELEASE ' + releaseUrl
        Request request = createRequestWithHeaders(tok, own, rep)
                .url(releaseUrl)
                .get()
                .build()
        Response response = client
                .newCall(request)
                .execute()
        return response
    }

    private static Response deletePreviousRelease(Response previous, String tok, String own, String rep, OkHttpClient client) {
        JSONObject responseJson = new JSONObject(previous.body().string())
        def prevReleaseUrl = responseJson.getString("url")

        println ':githubRelease DELETING PREVIOUS RELEASE ' + prevReleaseUrl
        Request request = createRequestWithHeaders(tok, own, rep)
                .url(prevReleaseUrl)
                .delete()
                .build()
        Response response = client.newCall(request).execute()
        int status = response.code()
        if (status != 204) {
            if (status == 404) {
                throw new Error("404 Repository with Owner: '${own}' and Name: '${rep}' was not found")
            }
            throw new Error("Couldn't delete old release: $status\n$response")
        }
        return response
    }

    private static Response createRelease(String tag, String tar, String rel, String bod, boolean dra, boolean pre, String tok, String own, String rep, OkHttpClient client) {
        println ':githubRelease CREATING RELEASE'
        JSONObject jsonObject = new JSONObject()
        jsonObject.with {
            put('tag_name', tag)
            put('target_commitish', tar)
            put('name', rel)
            put('body', bod)
            put('draft', dra)
            put('prerelease', pre)
        }

        def requestBody = RequestBody.create(JSON, jsonObject.toString())

        Request request = createRequestWithHeaders(tok, own, rep)
                .url("https://api.github.com/repos/${own}/${rep}/releases")
                .post(requestBody)
                .build()
        Response response = client.newCall(request).execute()
        int status = response.code()
        if (status != 201) {
            if (status == 404) {
                throw new Error("404 Repository with Owner: '${own}' and Name: '${rep}' was not found")
            }
            throw new Error("Could not create release: $status\n$response")
        }
        println ":githubRelease STATUS ${response.header("Status").toUpperCase()}"
        return response
    }

    private static List<Response> uploadAssets(Response response, String tok, String own, String rep, FileCollection releaseAssets, OkHttpClient client) {
        println ':githubRelease UPLOADING ASSETS'
        JSONObject responseJson = new JSONObject(response.body().string())

        ContentInfoUtil util = new ContentInfoUtil()
        if (releaseAssets.isEmpty()) {
            println ':githubRelease NO ASSETS FOUND'
            return Collections.emptyList()
        }
        return releaseAssets.files.stream().collect { asset ->
            println ':githubRelease UPLOADING ' + asset.name
            ContentInfo info = util.findMatch(asset) ?: ContentInfo.EMPTY_INFO
            MediaType type = MediaType.parse(info.mimeType)
            if (type == null)
                println ':githubRelease WARNING Mime Type could not be determined'
            String uploadUrl = responseJson.getString("upload_url")
            RequestBody assetBody = RequestBody.create(type, asset)

            Request assetPost = createRequestWithHeaders(tok, own, rep)
                    .url(uploadUrl.replace('{?name,label}', "?name=$asset.name"))
                    .post(assetBody)
                    .build()

            return client.newCall(assetPost).execute()
        }
    }

    private static Request.Builder createRequestWithHeaders(String tok, String own, String rep) {
        return new Request.Builder()
                .addHeader('Authorization', "token ${tok}")
                .addHeader('User-Agent', "${own}.${rep}")
                .addHeader('Accept', 'application/vnd.github.v3+json')
                .addHeader('Content-Type', 'application/json')
    }

    void setOwner(Provider<String> owner) {
        this.owner.set(owner)
    }

    void setOwner(String owner) {
        this.owner.set(owner)
    }

    void setRepo(Provider<String> repo) {
        this.repo.set(repo)
    }

    void setRepo(String repo) {
        this.repo.set(repo)
    }

    void setToken(Provider<String> token) {
        this.token.set(token)
    }

    void setToken(String repo) {
        this.token.set(repo)
    }

    void setTagName(Provider<String> tagName) {
        this.tagName.set(tagName)
    }

    void setTagName(String tagName) {
        this.tagName.set(tagName)
    }

    void setTargetCommitish(Provider<String> targetCommitish) {
        this.targetCommitish.set(targetCommitish)
    }

    void setTargetCommitish(String targetCommitish) {
        this.targetCommitish.set(targetCommitish)
    }

    void setReleaseName(Provider<String> releaseName) {
        this.releaseName.set(releaseName)
    }

    void setReleaseName(String releaseName) {
        this.releaseName.set(releaseName)
    }

    void setBody(Provider<String> body) {
        this.body.set(body)
    }

    void setBody(String body) {
        this.body.set(body)
    }

    void setDraft(Provider<Boolean> draft) {
        this.draft.set(draft)
    }

    void setDraft(Boolean draft) {
        this.draft.set(draft)
    }

    void setPrerelease(Provider<Boolean> prerelease) {
        this.prerelease.set(prerelease)
    }

    void setPrerelease(Boolean prerelease) {
        this.prerelease.set(prerelease)
    }

    void setReleaseAssets(Object... releaseAssets) {
        this.releaseAssets.setFrom(releaseAssets)
    }

}

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
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GithubRelease implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(GithubRelease)

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8")

    final CharSequence owner
    final CharSequence repo
    final CharSequence authorization
    final CharSequence tagName
    final CharSequence targetCommitish
    final CharSequence releaseName
    final CharSequence body
    final boolean draft
    final boolean prerelease
    final FileCollection releaseAssets
    final Provider<Boolean> overwrite
    final Provider<Boolean> allowUploadToExisting
    final OkHttpClient client
    final JsonSlurper slurper

    GithubRelease(CharSequence owner, CharSequence repo, CharSequence authorization, CharSequence tagName, CharSequence targetCommitish, CharSequence releaseName, CharSequence body, boolean draft, boolean prerelease, FileCollection releaseAssets, Provider<Boolean> overwrite, Provider<Boolean> allowUploadToExisting) {
        this.owner = owner
        this.repo = repo
        this.authorization = authorization
        this.tagName = tagName
        this.targetCommitish = targetCommitish
        this.releaseName = releaseName
        this.body = body
        this.draft = draft
        this.prerelease = prerelease
        this.releaseAssets = releaseAssets
        this.overwrite = overwrite
        this.allowUploadToExisting = allowUploadToExisting
        client = new OkHttpClient()
        slurper = new JsonSlurper()
    }

    @Override
    void run() {
        Response previousReleaseResponse = checkForPreviousRelease()
        final def code = previousReleaseResponse.code()
        if (code == 200) {
            println ':githubRelease EXISTING RELEASE FOUND'
            Boolean ovr = this.overwrite.getOrNull()
            if (ovr == null) {
                throw new PropertyNotSetException('overwrite')
            }
            if (ovr) {
                logger.info ':githubRelease EXISTING RELEASE DELETED'
                deletePreviousRelease(previousReleaseResponse)
                Response createReleaseResponse = createRelease()
                uploadAssets(createReleaseResponse)
            } else if (allowUploadToExisting.getOrElse(false)) {
                logger.info ':githubRelease Assets will added to existing release'
                uploadAssets(previousReleaseResponse)
            } else {
                def s = ':githubRelease FAILED RELEASE ALREADY EXISTS\n\tSet property[\'overwrite\'] to true to replace existing releases'
                logger.error s
                throw new Error(s)
            }
        } else if (code == 404) {
            Response createReleaseResponse = createRelease()
            uploadAssets(createReleaseResponse)
        } else {
            def s = ':githubRelease FAILED ERROR CODE ' + code
            logger.error s
            throw new Error("$s\n${previousReleaseResponse.body().string()}")
        }
    }


    Response checkForPreviousRelease() {
        String releaseUrl = "https://api.github.com/repos/$owner/$repo/releases/tags/$tagName"
        println ':githubRelease CHECKING FOR PREVIOUS RELEASE ' + releaseUrl
        Request request = createRequestWithHeaders(authorization)
                .url(releaseUrl)
                .get()
                .build()
        Response response = client
                .newCall(request)
                .execute()
        return response
    }

    Response deletePreviousRelease(Response previous) {
        def responseJson = slurper.parseText(previous.body().string())
        String prevReleaseUrl = responseJson.url

        println ":githubRelease DELETING PREVIOUS RELEASE $prevReleaseUrl"
        Request request = createRequestWithHeaders(authorization)
                .url(prevReleaseUrl)
                .delete()
                .build()
        Response response = client.newCall(request).execute()
        int status = response.code()
        if (status != 204) {
            if (status == 404) {
                throw new Error("404 Repository with Owner: '$owner' and Name: '$repo' was not found")
            }
            throw new Error("Couldn't delete old release: $status\n$response")
        }
        return response
    }

    Response createRelease() {
        println ':githubRelease CREATING RELEASE'
        String json = JsonOutput.toJson([
                tag_name        : tagName,
                target_commitish: targetCommitish,
                name            : releaseName,
                body            : body,
                draft           : draft,
                prerelease      : prerelease
        ])
        RequestBody requestBody = RequestBody.create(JSON, json)
        Request request = createRequestWithHeaders(authorization)
                .url("https://api.github.com/repos/$owner/$repo/releases")
                .post(requestBody)
                .build()

        Response response = client.newCall(request).execute()
        int status = response.code()
        if (status != 201) {
            def body = response.body().string()
            if (status == 404) {
                throw new Error("404 Repository with Owner: '$owner' and Name: '$repo' was not found")
            }
            throw new Error("Could not create release: $status ${response.message()}\n$body")
        }
        println ":githubRelease STATUS ${response.header("Status").toUpperCase()}"
        return response
    }

    /**
     * The responses returned is automatically closed for convenience. This behavior may change in the future if required.
     * @param response this response should reference the release that the assets will be uploaded to
     * @return a list of responses from uploaded each asset
     */
    List<Response> uploadAssets(Response response) {
        println ':githubRelease UPLOADING ASSETS'
        def responseJson = slurper.parseText(response.body().string())

        ContentInfoUtil util = new ContentInfoUtil()
        if (releaseAssets.isEmpty()) {
            println ':githubRelease NO ASSETS FOUND'
            return Collections.emptyList()
        }
        def assetResponses = releaseAssets.files.stream().collect { asset ->
            println ":githubRelease UPLOADING $asset.name"
            ContentInfo info = util.findMatch(asset) ?: ContentInfo.EMPTY_INFO
            MediaType type = MediaType.parse(info.mimeType)
            if (type == null)
                println ':githubRelease WARNING Mime Type could not be determined'
            String uploadUrl = responseJson.upload_url
            RequestBody assetBody = RequestBody.create(type, asset)

            Request assetPost = createRequestWithHeaders(authorization)
                    .url(uploadUrl.replace('{?name,label}', "?name=$asset.name"))
                    .post(assetBody)
                    .build()

            return client.newCall(assetPost).execute()
        }
        assetResponses.forEach{ it.close() }
        return assetResponses
    }

    static Request.Builder createRequestWithHeaders(CharSequence authorization) {
        return new Request.Builder()
                .addHeader('Authorization', authorization.toString())
                .addHeader('User-Agent', "breadmoirai github-release-gradle-plugin")
                .addHeader('Accept', 'application/vnd.github.v3+json')
                .addHeader('Content-Type', 'application/json')
    }
}

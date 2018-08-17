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
    final OkHttpClient client
    final JsonSlurper slurper

    List<Object> releases

    GithubRelease(CharSequence owner, CharSequence repo, CharSequence authorization, CharSequence tagName, CharSequence targetCommitish, CharSequence releaseName, CharSequence body, boolean draft, boolean prerelease, FileCollection releaseAssets) {
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
        client = new OkHttpClient()
        slurper = new JsonSlurper()
        releases = getReleases()

    }

    @Override
    void run() {

        Response previousReleaseResponse = checkForPreviousRelease()
        if (previousReleaseResponse.code() == 200) {
            logger.info ':githubRelease PREVIOUS RELEASE EXISTS'
            deletePreviousRelease(previousReleaseResponse)
        } else if (previousReleaseResponse.code() == 404) {
            logger.error ':githubRelease FAILED REPOSITORY NOT FOUND'
        }


        Response createReleaseResponse = createRelease()
        uploadAssets(createReleaseResponse)
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

        logger.info ":githubRelease DELETING PREVIOUS RELEASE $prevReleaseUrl"
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
        logger.info ':githubRelease CREATING RELEASE'
        def json = JsonOutput.toJson([
            tag_name: tagName,
            target_commitish: targetCommitish,
            name: releaseName,
            body: body,
            draft: draft,
            prerelease: prerelease
        ])

        def requestBody = RequestBody.create(JSON, json)

        Request request = createRequestWithHeaders(authorization)
                .url("https://api.github.com/repos/$owner/$repo/releases")
                .post(requestBody)
                .build()
        Response response = client.newCall(request).execute()
        int status = response.code()
        if (status != 201) {
            if (status == 404) {
                throw new Error("404 Repository with Owner: '$owner' and Name: '$repo' was not found")
            }
            throw new Error("Could not create release: $status\n$response")
        }
        logger.info ":githubRelease STATUS ${response.header("Status").toUpperCase()}"
        return response
    }

    List<Response> uploadAssets(Response response) {
        logger.info ':githubRelease UPLOADING ASSETS'
        def responseJson = slurper.parseText(response.body().string())

        ContentInfoUtil util = new ContentInfoUtil()
        if (releaseAssets.isEmpty()) {
            logger.info ':githubRelease NO ASSETS FOUND'
            return Collections.emptyList()
        }
        return releaseAssets.files.stream().collect { asset ->
            logger.info ":githubRelease UPLOADING $asset.name"
            ContentInfo info = util.findMatch(asset) ?: ContentInfo.EMPTY_INFO
            MediaType type = MediaType.parse(info.mimeType)
            if (type == null)
                logger.info ':githubRelease WARNING Mime Type could not be determined'
            String uploadUrl = responseJson.upload_url
            RequestBody assetBody = RequestBody.create(type, asset)

            Request assetPost = createRequestWithHeaders(authorization)
                    .url(uploadUrl.replace('{?name,label}', "?name=$asset.name"))
                    .post(assetBody)
                    .build()

            return client.newCall(assetPost).execute()
        }
    }

    static Request.Builder createRequestWithHeaders(CharSequence authorization) {
        return new Request.Builder()
                .addHeader('Authorization', authorization.toString())
                .addHeader('User-Agent', "breadmoirai github-release-gradle-plugin")
                .addHeader('Accept', 'application/vnd.github.v3+json')
                .addHeader('Content-Type', 'application/json')
    }
}

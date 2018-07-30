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
import org.gradle.api.file.FileCollection
import org.json.JSONObject

class GithubRelease implements Runnable {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8")

    final String owner
    final String repo
    final String token
    final String tagName
    final String targetCommitish
    final String releaseName
    final String body
    final Boolean draft
    final Boolean prerelease
    final FileCollection releaseAssets
    final OkHttpClient client

    GithubRelease(String owner, String repo, String token, String tagName, String targetCommitish, String releaseName, String body, Boolean draft, Boolean prerelease, FileCollection releaseAssets) {
        this.owner = owner
        this.repo = repo
        this.token = token
        this.tagName = tagName
        this.targetCommitish = targetCommitish
        this.releaseName = releaseName
        this.body = body
        this.draft = draft
        this.prerelease = prerelease
        this.releaseAssets = releaseAssets
        client = new OkHttpClient()
    }

    @Override
    void run() {
        Response previousReleaseResponse = checkForPreviousRelease()
        if (previousReleaseResponse.code() == 200) {
            println ':githubRelease PREVIOUS RELEASE EXISTS'
            deletePreviousRelease(previousReleaseResponse)
        }

        Response createReleaseResponse = createRelease()
        uploadAssets(createReleaseResponse)
    }


    Response checkForPreviousRelease() {
        String releaseUrl = "https://api.github.com/repos/$owner/$repo/releases/tags/$tagName"
        println ':githubRelease CHECKING FOR PREVIOUS RELEASE ' + releaseUrl
        Request request = createRequestWithHeaders()
                .url(releaseUrl)
                .get()
                .build()
        Response response = client
                .newCall(request)
                .execute()
        return response
    }

    Response deletePreviousRelease(Response previous) {
        JSONObject responseJson = new JSONObject(previous.body().string())
        def prevReleaseUrl = responseJson.getString("url")

        println ":githubRelease DELETING PREVIOUS RELEASE $prevReleaseUrl"
        Request request = createRequestWithHeaders()
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
        JSONObject jsonObject = new JSONObject()
        jsonObject.with {
            put('tag_name', tagName)
            put('target_commitish', targetCommitish)
            put('name', releaseName)
            put('body', body)
            put('draft', draft)
            put('prerelease', prerelease)
        }

        def requestBody = RequestBody.create(JSON, jsonObject.toString())

        Request request = createRequestWithHeaders()
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
        println ":githubRelease STATUS ${response.header("Status").toUpperCase()}"
        return response
    }

    List<Response> uploadAssets(Response response) {
        println ':githubRelease UPLOADING ASSETS'
        JSONObject responseJson = new JSONObject(response.body().string())

        ContentInfoUtil util = new ContentInfoUtil()
        if (releaseAssets.isEmpty()) {
            println ':githubRelease NO ASSETS FOUND'
            return Collections.emptyList()
        }
        return releaseAssets.files.stream().collect { asset ->
            println ":githubRelease UPLOADING $asset.name"
            ContentInfo info = util.findMatch(asset) ?: ContentInfo.EMPTY_INFO
            MediaType type = MediaType.parse(info.mimeType)
            if (type == null)
                println ':githubRelease WARNING Mime Type could not be determined'
            String uploadUrl = responseJson.getString("upload_url")
            RequestBody assetBody = RequestBody.create(type, asset)

            Request assetPost = createRequestWithHeaders()
                    .url(uploadUrl.replace('{?name,label}', "?name=$asset.name"))
                    .post(assetBody)
                    .build()

            return client.newCall(assetPost).execute()
        }
    }

    Request.Builder createRequestWithHeaders() {
        return new Request.Builder()
                .addHeader('Authorization', "token ${token}")
                .addHeader('User-Agent', "breadmoirai:github-release-gradle-plugin")
                .addHeader('Accept', 'application/vnd.github.v3+json')
                .addHeader('Content-Type', 'application/json')
    }
}

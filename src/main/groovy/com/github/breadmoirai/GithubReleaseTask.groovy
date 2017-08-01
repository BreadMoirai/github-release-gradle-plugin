package com.github.breadmoirai

import com.j256.simplemagic.ContentInfoUtil
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.com.google.gson.JsonParser

class GithubReleaseTask extends DefaultTask {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8")

    @Input
    final Provider<String> owner = project.property(String),
                           repo = project.property(String),
                           token = project.token(String),
                           tagName = project.property(String),
                           targetCommitish = project.property(String),
                           releaseName = project.property(String),
                           body = project.property(String)
    @Input
    final Provider<Boolean> draft = project.property(Boolean), prerelease = project.property(Boolean)

    final ConfigurableFileCollection releaseAssets = project.files()

    @TaskAction
    void publishRelease() {
        println ':githubRelease CREATING RELEASE'
        def client = new OkHttpClient()
        def tag = tagName.getOrNull() ?: "v$project.version"
        def tar = targetCommitish.getOrNull() ?: 'master'
        def rel = releaseName.getOrNull() ?: tag
        def bod = body.getOrNull() ?: ''
        boolean dra = draft.getOrNull() ?: false
        boolean pre = prerelease.getOrNull() ?: false

        def json = $/
                    {
                      "tag_name": $tag,
                      "target_commitish": $tar,
                      "name": $rel,
                      "body": $bod,
                      "draft": $dra,
                      "prerelease": $pre
                    }
                    /$
        def requestBody = RequestBody.create(JSON, json)

        Request request = new Request.Builder()
                .addHeader('Authorization', "token ${this.token.get()}")
                .addHeader('User-Agent', "${this.owner.get()}.${this.repo.get()}")
                .url("https://api.github.com/repos/${this.owner.get()}/${this.repo.get()}/releases")
                .post(requestBody)
                .build()

        def execute = client.newCall(request).execute()
        println ':githubRelease ' + execute.headers()
        def status = execute.headers().get("Status")
        if (!status.startsWith('201')) {
            throw new Error(status)
        }
        def responseJson = new JsonParser().parse(execute.body().charStream()).getAsJsonObject()
        def releaseId = responseJson.get("id").asInt
        println ":githubRelease URL ${responseJson.get("html_url")}"
        println ':githubRelease UPLOADING ASSETS'
        def util = new ContentInfoUtil()
        this.releaseAssets.forEach { asset ->
            def info = util.findMatch(asset)
            def type = MediaType.parse(info.getMimeType())
            if (type == null)
                println ':githubRelease UPLOAD FAILED\n\tMime Type could not be determined'
            def uploadUrl = responseJson.get("upload_url").asString
            println uploadUrl
            def assetBody = RequestBody.create(type, asset)

            Request assetPost = new Request.Builder()
                    .addHeader('Authorization', "token ${this.token.get()}")
                    .addHeader('User-Agent', "${this.owner.get()}.${this.repo.get()}")
                    .url(uploadUrl.replace('{?name,label}', "?name=$asset.name"))
                    .post(assetBody)
                    .build()

            def assetResponse = client.newCall(assetPost).execute()
            println assetResponse.headers().get('Status')
        }

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

    void setReleaseAssets(FileCollection releaseAssets) {
        this.releaseAssets.setFrom(releaseAssets)
    }

}

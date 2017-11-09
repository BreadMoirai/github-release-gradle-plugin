package com.github.breadmoirai

import com.j256.simplemagic.ContentInfoUtil
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject

class GithubReleaseTask extends DefaultTask {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8")

    final Provider<String> owner = project.property(String),
                           repo = project.property(String),
                           token = project.property(String),
                           tagName = project.property(String),
                           targetCommitish = project.property(String),
                           releaseName = project.property(String),
                           body = project.property(String)

    final Provider<Boolean> draft = project.property(Boolean), prerelease = project.property(Boolean)

    final ConfigurableFileCollection releaseAssets = project.files()

    GithubReleaseTask() {
        this.setGroup('publishing')
    }

    @TaskAction
    void publishRelease() {
        println ':githubRelease CREATING RELEASE'
        def client = new OkHttpClient()
        def tag = tagName.getOrNull() ?: "v$project.version"
        def tar = targetCommitish.getOrNull() ?: 'master'
        def rel = releaseName.getOrNull() ?: tag
        def bod = body.getOrNull() ?: ''
        def group = project.group.toString()
        def own = this.owner.getOrNull() ?:
                group.substring(group.lastIndexOf('.') + 1)
        def rep = this.repo.getOrNull() ?: project.name ?: project.rootProject?.name ?: project.rootProject?.rootProject?.name
        boolean dra = draft.getOrNull() ?: false
        boolean pre = prerelease.getOrNull() ?: false
        def tok = this.token.getOrNull()
        if (tok == null) throw new MissingPropertyException("Field 'token' is not set for githubRelease", 'token', String)
        def jsonObject = new JSONObject()
        jsonObject.with {
            put('tag_name', tag)
            put('target_commitish', tar)
            put('name', rel)
            put('body', bod)
            put('draft', dra)
            put('prerelease', pre)
        }

        def requestBody = RequestBody.create(JSON, jsonObject.toString())

        Request request = new Request.Builder()
                .addHeader('Authorization', "token ${tok}")
                .addHeader('User-Agent', "${own}.${rep}")
                .addHeader('Accept', 'application/vnd.github.v3+json')
                .url("https://api.github.com/repos/${own}/${rep}/releases")
                .post(requestBody)
                .build()
        def execute = client.newCall(request).execute()
        def headers = execute.headers()
        def status = headers.get("Status")
        if (!status.startsWith('201')) {
            if (status.startsWith('404')) {
                throw new Error("404 Repository with Owner: '${own}' and Name: '${rep}' was not found")
            }
            throw new Error(status + '\n' + execute.toString())
        }
        println ":githubRelease STATUS " + status.toUpperCase()
        def responseJson = new JSONObject(execute.body().string())
        println ":githubRelease URL ${responseJson.getString("html_url")}"
        println ':githubRelease UPLOADING ASSETS'
        def util = new ContentInfoUtil()
        this.releaseAssets.files.forEach { asset ->
            println ':githubRelease UPLOADING ' + asset.name
            def info = util.findMatch(asset)
            def type = MediaType.parse(info.mimeType)
            if (type == null)
                println ':githubRelease UPLOAD FAILED\n\tMime Type could not be determined'
            def uploadUrl = responseJson.getString("upload_url")
            def assetBody = RequestBody.create(type, asset)

            Request assetPost = new Request.Builder()
                    .addHeader('Authorization', "token ${tok}")
                    .addHeader('User-Agent', "${own}.${rep}")
                    .addHeader('Accept', 'application/vnd.github.v3+json')
                    .url(uploadUrl.replace('{?name,label}', "?name=$asset.name"))
                    .post(assetBody)
                    .build()

            def assetResponse = client.newCall(assetPost).execute()
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

    void setReleaseAssets(Object... releaseAssets) {
        this.releaseAssets.setFrom(releaseAssets)
    }

}

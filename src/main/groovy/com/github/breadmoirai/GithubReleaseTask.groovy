package com.github.breadmoirai

import com.j256.simplemagic.ContentInfo
import com.j256.simplemagic.ContentInfoUtil
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject

class GithubReleaseTask extends DefaultTask {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8")

    final Provider<String> owner
    final Provider<String> repo
    final Provider<String> token
    final Provider<String> tagName
    final Provider<String> targetCommitish
    final Provider<String> releaseName
    final Provider<String> body
    final Provider<Boolean> draft
    final Provider<Boolean> prerelease
    final ConfigurableFileCollection releaseAssets

    GithubReleaseTask() {
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
        def client = new OkHttpClient()
        def tag = tagName.getOrNull() ?: "v$project.version"
        def tar = targetCommitish.getOrNull() ?: 'master'
        def rel = releaseName.getOrNull() ?: tag
        def bod = this.body.getOrNull() ?: ''
        def group = project.group.toString()
        def own = this.owner.getOrNull() ?:
                group.substring(group.lastIndexOf('.') + 1)
        def rep = this.repo.getOrNull() ?: project.name ?: project.rootProject?.name ?: project.rootProject?.rootProject?.name
        boolean dra = draft.getOrNull() ?: false
        boolean pre = prerelease.getOrNull() ?: false
        def tok = this.token.getOrNull()
        if (tok == null) throw new MissingPropertyException("Field 'token' is not set for githubRelease", 'token', String)
        def releaseUrl = "https://api.github.com/repos/${own}/${rep}/releases/tags/${tag}"

        println ':githubRelease CHECKING PREVIOUS RELEASE ' + releaseUrl
        Request request = new Request.Builder()
                .addHeader('Authorization', "token ${tok}")
                .addHeader('User-Agent', "${own}.${rep}")
                .addHeader('Accept', 'application/vnd.github.v3+json')
                .addHeader('Content-Type', 'application/json')
                .url(releaseUrl)
                .get()
                .build()
        def execute = client.newCall(request).execute()
        def headers = execute.headers()
        def status = headers.get("Status")
        if (status.startsWith('200')) {
            println ':githubRelease PREVIOUS RELEASE EXISTS'
            def body = execute.body()
            def responseJson = new JSONObject(body.string())
            body.close()
            def prevReleaseUrl = responseJson.getString("url")

            println ':githubRelease DELETING PREVIOUS RELEASE ' + prevReleaseUrl
            request = new Request.Builder()
                    .addHeader('Authorization', "token ${tok}")
                    .addHeader('User-Agent', "${own}.${rep}")
                    .addHeader('Accept', 'application/vnd.github.v3+json')
                    .addHeader('Content-Type', 'application/json')
                    .url(prevReleaseUrl)
                    .delete()
                    .build()
            execute = client.newCall(request).execute()
            headers = execute.headers()
            status = headers.get("Status")
            if (!status.startsWith('204')) {
                if (status.startsWith('404')) {
                    throw new Error("404 Repository with Owner: '${own}' and Name: '${rep}' was not found")
                }
                def buffer = new Buffer()
                request.newBuilder().build().body().writeTo(buffer)
                throw new Error('Couldnt delete old release: ' + status.toString() + '\n' + execute.toString() + '\n' + buffer.readUtf8())
            }
        }

        println ':githubRelease CREATING RELEASE'
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

        request = new Request.Builder()
                .addHeader('Authorization', "token ${tok}")
                .addHeader('User-Agent', "${own}.${rep}")
                .addHeader('Accept', 'application/vnd.github.v3+json')
                .addHeader('Content-Type', 'application/json')
                .url("https://api.github.com/repos/${own}/${rep}/releases")
                .post(requestBody)
                .build()
        execute = client.newCall(request).execute()
        headers = execute.headers()
        status = headers.get("Status")
        if (!status.startsWith('201')) {
            if (status.startsWith('404')) {
                throw new Error("404 Repository with Owner: '${own}' and Name: '${rep}' was not found")
            }
            def buffer = new Buffer()
            request.newBuilder().build().body().writeTo(buffer)
            throw new Error(status + '\n' + execute.toString() + '\n' + buffer.readUtf8())
        }
        println ":githubRelease STATUS " + status.toUpperCase()
        def body = execute.body()
        def responseJson = new JSONObject(body.string())
        body.close()
        println ":githubRelease URL ${responseJson.getString("html_url")}"
        println ':githubRelease UPLOADING ASSETS'
        def util = new ContentInfoUtil()
        if (this.releaseAssets.isEmpty())
            println ':githubRelease NO ASSETS FOUND'
        else
            this.releaseAssets.files.forEach { asset ->
                println ':githubRelease UPLOADING ' + asset.name
                def info = util.findMatch(asset)
                if(info == null)
                    info = ContentInfo.EMPTY_INFO
                def type = MediaType.parse(info.mimeType)
                if (type == null)
                    println ':githubRelease UPLOAD FAILED\n\tMime Type could not be determined'
                def uploadUrl = responseJson.getString("upload_url")
                def assetBody = RequestBody.create(type, asset)

                Request assetPost = new Request.Builder()
                        .addHeader('Authorization', "token ${tok}")
                        .addHeader('User-Agent', "${own}.${rep}")
                        .addHeader('Accept', 'application/vnd.github.v3+json')
                        .addHeader('Content-Type', 'application/json')
                        .url(uploadUrl.replace('{?name,label}', "?name=$asset.name"))
                        .post(assetBody)
                        .build()

                def assetResponse = client.newCall(assetPost).execute().close()
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

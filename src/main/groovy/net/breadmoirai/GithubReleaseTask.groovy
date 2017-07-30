package net.breadmoirai

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.PropertyState
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.com.google.gson.JsonParser

class GithubReleaseTask extends DefaultTask {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8")

    final PropertyState<String> owner = project.property(String),
                                repo = project.property(String),
                                token = project.token(String),
                                tagName = project.property(String),
                                targetCommitish = project.property(String),
                                releaseName = project.property(String),
                                body = project.property(String)

    final PropertyState<Boolean> draft = project.property(Boolean), prerelease = project.property(Boolean)

    final ConfigurableFileCollection releaseAssets = project.files()

    @TaskAction
    void publishRelease() {
        println 'GithubRelease: Creating Release'
        def client = new OkHttpClient()
        def tag = tagName.getOrNull()
        if (tag == null) tag = "v$project.version"
        def tar = targetCommitish.getOrNull()
        if (tar == null) tar = 'master'
        def rel = releaseName.getOrNull()
        if (rel == null) rel = tag
        def bod = body.getOrNull()
        if (bod == null) bod = ''
        boolean dra = draft.getOrNull()
        if (dra == null) dra = false
        boolean pre = prerelease.getOrNull()
        if (pre == null) pre = false

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
        println execute.headers()
        if (execute.headers().get("Status").startsWith('201')) {
            def responseJson = new JsonParser().parse(execute.body().charStream()).getAsJsonObject()
            def releaseId = responseJson.get("id").asInt
            println "Url: ${responseJson.get("html_url")}"
            println 'GithubRelease: Uploading Assets'
        }

    }

    void setOwner(PropertyState<String> owner) {
        this.owner.set(owner)
    }

    void setOwner(String owner) {
        this.owner.set(owner)
    }

    void setRepo(PropertyState<String> repo) {
        this.repo.set(repo)
    }

    void setRepo(String repo) {
        this.repo.set(repo)
    }

    void setToken(PropertyState<String> token) {
        this.token.set(token)
    }

    void setToken(String repo) {
        this.token.set(repo)
    }

    void setTagName(PropertyState<String> tagName) {
        this.tagName.set(tagName)
    }

    void setTagName(String tagName) {
        this.tagName.set(tagName)
    }

    void setTargetCommitish(PropertyState<String> targetCommitish) {
        this.targetCommitish.set(targetCommitish)
    }

    void setReleaseName(String releaseName) {
        this.releaseName.set(releaseName)
    }

    void setBody(PropertyState<String> body) {
        this.body.set(body)
    }

    void setBody(String body) {
        this.body.set(body)
    }

    void setDraft(PropertyState<Boolean> draft) {
        this.draft.set(draft)
    }

    void setDraft(Boolean draft) {
        this.draft.set(draft)
    }

    void setPrerelease(PropertyState<Boolean> prerelease) {
        this.prerelease.set(prerelease)
    }

    void setPrerelease(Boolean prerelease) {
        this.prerelease.set(prerelease)
    }

    void setReleaseAssets(ConfigurableFileCollection releaseAssets) {
        this.releaseAssets.setFrom(releaseAssets)
    }
}

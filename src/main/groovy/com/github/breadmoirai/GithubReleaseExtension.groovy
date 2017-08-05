package com.github.breadmoirai

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider

class GithubReleaseExtension {

    final PropertyState<String> owner, repo, token, tagName, targetCommitish, name, body;
    final PropertyState<Boolean> draft, prerelease

    final ConfigurableFileCollection releaseAssets

    GithubReleaseExtension(Project project) {
        owner = project.property(String)
        repo = project.property(String)
        token = project.property(String)
        tagName = project.property(String)
        targetCommitish = project.property(String)
        name = project.property(String)
        body = project.property(String)
        draft = project.property(Boolean)
        prerelease = project.property(Boolean)
        releaseAssets = project.files()
    }

    Provider<String> getOwnerProvider() {
        return owner
    }

    Provider<String> getRepoProvider() {
        return repo
    }

    Provider<String> getTokenProvider() {
        return token
    }

    Provider<String> getTagNameProvider() {
        return tagName
    }

    Provider<String> getTargetCommitishProvider() {
        return targetCommitish
    }

    Provider<String> getNameProvider() {
        return name
    }

    Provider<String> getBodyProvider() {
        return body
    }

    Provider<Boolean> getDraftProvider() {
        return draft
    }

    Provider<Boolean> getPrereleaseProvider() {
        return prerelease
    }

    def getReleaseAssets() {
        return releaseAssets
    }

    void setOwner(String owner) {
        this.owner.set(owner)
    }

    void setRepo(String repo) {
        this.repo.set(repo)
    }

    void setToken(String token) {
        this.token.set(token)
    }

    void setTagName(String tagName) {
        this.tagName.set(tagName)
    }

    void setTargetCommitish(String targetCommitish) {
        this.targetCommitish.set(targetCommitish)
    }

    void setName(String name) {
        this.name.set(name)
    }

    void setBody(String body) {
        this.body.set(body)
    }

    void setDraft(boolean draft) {
        this.draft.set(draft)
    }

    void setPrerelease(boolean prerelease) {
        this.prerelease.set(prerelease)
    }

    void setReleaseAssets(Object... assets) {
        releaseAssets.setFrom(assets)
    }
}
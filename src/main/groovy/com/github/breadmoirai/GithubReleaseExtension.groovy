package com.github.breadmoirai

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider

class GithubReleaseExtension {

    final PropertyState<String> owner, repo, token, tagName, targetCommitish, name, body;
    final PropertyState<Boolean> draft, prerelease

    final ConfigurableFileCollection releaseAssets

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

    ConfigurableFileCollection getReleaseAssets() {
        return releaseAssets
    }
}
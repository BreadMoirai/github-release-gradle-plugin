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

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider


class GithubReleaseExtension {

    final Property<String> owner
    final Property<String> repo
    final Property<String> token
    final Property<String> tagName
    final Property<String> targetCommitish
    final Property<String> releaseName
    final Property<String> body
    final Property<Boolean> draft
    final Property<Boolean> prerelease

    final ConfigurableFileCollection releaseAssets

    GithubReleaseExtension(Project project) {
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

    Provider<String> getReleaseNameProvider() {
        return releaseName
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

    void setReleaseName(String releaseName) {
        this.releaseName.set(releaseName)
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

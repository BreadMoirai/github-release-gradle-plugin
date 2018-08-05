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

    final Property<CharSequence> owner
    final Property<CharSequence> repo
    final Property<CharSequence> token
    final Property<CharSequence> tagName
    final Property<CharSequence> targetCommitish
    final Property<CharSequence> releaseName
    final Property<CharSequence> body
    final Property<Boolean> draft
    final Property<Boolean> prerelease

    final ConfigurableFileCollection releaseAssets

    GithubReleaseExtension(Project project) {
        final ObjectFactory objectFactory = project.objects
        owner = objectFactory.property(CharSequence)
        repo = objectFactory.property(CharSequence)
        token = objectFactory.property(CharSequence)
        tagName = objectFactory.property(CharSequence)
        targetCommitish = objectFactory.property(CharSequence)
        releaseName = objectFactory.property(CharSequence)
        body = objectFactory.property(CharSequence)
        draft = objectFactory.property(Boolean)
        prerelease = objectFactory.property(Boolean)
        releaseAssets = project.files()
    }

    Provider<CharSequence> getOwnerProvider() {
        return owner
    }

    Provider<CharSequence> getRepoProvider() {
        return repo
    }

    Provider<CharSequence> getTokenProvider() {
        return token
    }

    Provider<CharSequence> getTagNameProvider() {
        return tagName
    }

    Provider<CharSequence> getTargetCommitishProvider() {
        return targetCommitish
    }

    Provider<CharSequence> getReleaseNameProvider() {
        return releaseName
    }

    Provider<CharSequence> getBodyProvider() {
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

    void setOwner(CharSequence owner) {
        this.owner.set(owner)
    }

    void setRepo(CharSequence repo) {
        this.repo.set(repo)
    }

    void setToken(CharSequence token) {
        this.token.set(token)
    }

    void setTagName(CharSequence tagName) {
        this.tagName.set(tagName)
    }

    void setTargetCommitish(CharSequence targetCommitish) {
        this.targetCommitish.set(targetCommitish)
    }

    void setReleaseName(CharSequence releaseName) {
        this.releaseName.set(releaseName)
    }

    void setBody(CharSequence body) {
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

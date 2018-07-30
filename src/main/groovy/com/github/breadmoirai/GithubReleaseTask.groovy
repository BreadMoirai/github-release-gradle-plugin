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

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

class GithubReleaseTask extends DefaultTask {

    @Input final Provider<String> owner
    @Input final Provider<String> repo
    @Input final Provider<String> token
    @Input final Provider<String> tagName
    @Input final Provider<String> targetCommitish
    @Input final Provider<String> releaseName
    @Input final Provider<String> body
    @Input final Provider<Boolean> draft
    @Input final Provider<Boolean> prerelease
    @InputFiles final ConfigurableFileCollection releaseAssets

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
        String tag = tagName.getOrElse("v$project.version")
        String tar = targetCommitish.getOrElse('master')
        String rel = releaseName.getOrElse(tag)
        String bod = this.body.getOrElse('')
        String group = project.group.toString()
        String own = this.owner.getOrElse(group.substring(group.lastIndexOf('.') + 1))
        String rep = this.repo.getOrElse(project.name) ?: project.rootProject?.name ?: project.rootProject?.rootProject?.name
        boolean dra = draft.getOrElse(false)
        boolean pre = prerelease.getOrElse(false)
        String tok = this.token.getOrNull()
        FileCollection releaseAssets = this.releaseAssets

        new GithubRelease(own, rep, tok, tag, tar, rel, bod, dra, pre, releaseAssets).run()
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

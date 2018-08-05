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

    @Input final Provider<CharSequence> owner
    @Input final Provider<CharSequence> repo
    @Input final Provider<CharSequence> token
    @Input final Provider<CharSequence> tagName
    @Input final Provider<CharSequence> targetCommitish
    @Input final Provider<CharSequence> releaseName
    @Input final Provider<CharSequence> body
    @Input final Provider<Boolean> draft
    @Input final Provider<Boolean> prerelease
    @InputFiles final ConfigurableFileCollection releaseAssets

    GithubReleaseTask() {
        this.setGroup('publishing')
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

    @TaskAction
    void publishRelease() {
        CharSequence tag = this.tagName.get()
        CharSequence tar = this.targetCommitish.get()
        CharSequence rel = this.releaseName.get()
        CharSequence bod = this.body.get()
        CharSequence own = this.owner.get()
        CharSequence rep = this.repo.get()
        boolean dra = this.draft.get()
        boolean pre = this.prerelease.get()
        CharSequence tok = this.token.get()
        CharSequence auth
        if (tok.length() != 0) {
            auth = "Token $tok"
        } else {
            GithubLoginApp.start()
            Optional<CharSequence> wait = GithubLoginApp.getApp().waitForResult()
            if (!wait.isPresent()) {
                println "githubRelease: TASK CANCELLED"
                return
            } else {
                auth = "Basic ${wait.get()}"
            }
        }
        FileCollection releaseAssets = this.releaseAssets

        new GithubRelease(own, rep, auth, tag, tar, rel, bod, dra, pre, releaseAssets).run()
    }

    void setOwner(Provider<CharSequence> owner) {
        this.owner.set(owner)
    }

    void setOwner(CharSequence owner) {
        this.owner.set(owner)
    }

    void setRepo(Provider<CharSequence> repo) {
        this.repo.set(repo)
    }

    void setRepo(CharSequence repo) {
        this.repo.set(repo)
    }

    void setToken(Provider<CharSequence> token) {
        this.token.set(token)
    }

    void setToken(CharSequence repo) {
        this.token.set(repo)
    }

    void setTagName(Provider<CharSequence> tagName) {
        this.tagName.set(tagName)
    }

    void setTagName(CharSequence tagName) {
        this.tagName.set(tagName)
    }

    void setTargetCommitish(Provider<CharSequence> targetCommitish) {
        this.targetCommitish.set(targetCommitish)
    }

    void setTargetCommitish(CharSequence targetCommitish) {
        this.targetCommitish.set(targetCommitish)
    }

    void setReleaseName(Provider<CharSequence> releaseName) {
        this.releaseName.set(releaseName)
    }

    void setReleaseName(CharSequence releaseName) {
        this.releaseName.set(releaseName)
    }

    void setBody(Provider<CharSequence> body) {
        this.body.set(body)
    }

    void setBody(CharSequence body) {
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

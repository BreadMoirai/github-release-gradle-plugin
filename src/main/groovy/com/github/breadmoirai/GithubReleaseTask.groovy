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

import java.util.concurrent.Callable

class GithubReleaseTask extends DefaultTask {

    @Input final Provider<CharSequence> owner
    @Input final Provider<CharSequence> repo
    @Input final Provider<CharSequence> authorization
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
        authorization = objectFactory.property(CharSequence)
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
        CharSequence auth = this.authorization.get()
        FileCollection releaseAssets = this.releaseAssets

        new GithubRelease(own, rep, auth, tag, tar, rel, bod, dra, pre, releaseAssets).run()
    }

    void setOwner(CharSequence owner) {
        this.owner.set(owner)
    }

    void setOwner(Provider<CharSequence> owner) {
        this.owner.set(owner)
    }

    void setOwner(Callable<CharSequence> owner) {
        this.owner.set(new TypedDefaultProvider<>(CharSequence.class, owner))
    }

    void setRepo(CharSequence repo) {
        this.repo.set(repo)
    }

    void setRepo(Provider<CharSequence> repo) {
        this.repo.set(repo)
    }

    void setRepo(Callable<CharSequence> repo) {
        this.repo.set(new TypedDefaultProvider<>(CharSequence.class, repo))
    }

    void setToken(CharSequence token) {
        this.authorization.set("Token $token")
    }

    void setToken(Provider<CharSequence> token) {
        this.authorization.set(token.map { "Token $it" })
    }

    void setToken(Callable<CharSequence> token) {
        this.authorization.set(new TypedDefaultProvider(CharSequence.class, token).map { "Token $it" })
    }

    void setAuthorization(CharSequence authorization) {
        this.authorization.set(authorization)
    }

    void setAuthorization(Provider<CharSequence> authorization) {
        this.authorization.set(authorization)
    }

    void setAuthorization(Callable<CharSequence> authorization) {
        this.authorization.set(new TypedDefaultProvider<>(CharSequence.class, authorization))
    }

    void setTagName(CharSequence tagName) {
        this.tagName.set(tagName)
    }

    void setTagName(Provider<CharSequence> tagName) {
        this.tagName.set(tagName)
    }

    void setTagName(Callable<CharSequence> tagName) {
        this.tagName.set(new TypedDefaultProvider<>(CharSequence.class, tagName))
    }

    void setTargetCommitish(CharSequence targetCommitish) {
        this.targetCommitish.set(targetCommitish)
    }

    void setTargetCommitish(Provider<CharSequence> targetCommitish) {
        this.targetCommitish.set(targetCommitish)
    }

    void setTargetCommitish(Callable<CharSequence> targetCommitish) {
        this.targetCommitish.set(new TypedDefaultProvider<>(CharSequence.class, targetCommitish))
    }

    void setReleaseName(CharSequence releaseName) {
        this.releaseName.set(releaseName)
    }

    void setReleaseName(Provider<CharSequence> releaseName) {
        this.releaseName.set(releaseName)
    }

    void setReleaseName(Callable<CharSequence> releaseName) {
        this.releaseName.set(new TypedDefaultProvider<>(CharSequence.class, releaseName))
    }

    void setBody(CharSequence body) {
        this.body.set(body)
    }

    void setBody(Provider<CharSequence> body) {
        this.body.set(body)
    }

    void setBody(Callable<CharSequence> body) {
        this.body.set(new TypedDefaultProvider<>(CharSequence.class, body))
    }

    void setDraft(boolean draft) {
        this.draft.set(draft)
    }

    void setDraft(Provider<Boolean> draft) {
        this.draft.set(draft)
    }

    void setDraft(Callable<Boolean> draft) {
        this.draft.set(new TypedDefaultProvider<>(Boolean.class, draft))
    }

    void setPrerelease(boolean prerelease) {
        this.prerelease.set(prerelease)
    }

    void setPrerelease(Provider<Boolean> prerelease) {
        this.prerelease.set(prerelease)
    }

    void setPrerelease(Callable<Boolean> prerelease) {
        this.prerelease.set(new TypedDefaultProvider<>(Boolean.class, prerelease))
    }

    void setReleaseAssets(Object... assets) {
        this.releaseAssets.setFrom(assets)
    }

}

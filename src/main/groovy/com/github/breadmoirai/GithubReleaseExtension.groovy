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

import java.util.concurrent.Callable

/**
 * An extension for the {@link com.github.breadmoirai.GithubReleasePlugin}
 * <p> See Default Values below </p>
 * <p>
 *     <table>
 *         <tr>
 *             <th>Property</th>
 *             <th>Default Value</th>
 *         </tr>
 *         <tr>
 *             <td>owner</td>
 *             <td>project.group<br />
 *                 part after last period</td>
 *         </tr>
 *         <tr>
 *             <td>repo</td>
 *             <td>project.name ||<br />
 *                 rootProject.name</td>
 *         </tr>
 *         <tr>
 *             <td>tagName</td>
 *             <td>'v' + project.version</td>
 *         </tr>
 *         <tr>
 *             <td>targetCommitish</td>
 *             <td>'master'</td>
 *         </tr>
 *         <tr>
 *             <td>releaseName</td>
 *             <td>'v' + project.version</td>
 *         </tr>
 *         <tr>
 *             <td>body</td>
 *             <td>list of commits since last release</td>
 *         </tr>
 *         <tr>
 *             <td>draft</td>
 *             <td>false</td>
 *         </tr>
 *         <tr>
 *             <td>prerelease</td>
 *             <td>false</td>
 *         </tr>
 *         <tr>
 *             <td>token</td>
 *             <td>show login prompt</td>
 *         </tr>
 *     </table>
 * </p>
 *
 */
class GithubReleaseExtension {

    final Property<CharSequence> owner
    final Property<CharSequence> repo
    final Property<CharSequence> authorization
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
        authorization = objectFactory.property(CharSequence)
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

    Provider<CharSequence> getAuthorizationProvider() {
        return authorization
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

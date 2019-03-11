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

package com.github.breadmoirai.githubreleaseplugin


import com.github.breadmoirai.githubreleaseplugin.ast.ExtensionClass
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import java.util.concurrent.Callable

/**
 * An extension for the {@link com.github.breadmoirai.githubreleaseplugin.GithubReleasePlugin}
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
 *             <td>authorization</td>
 *             <td>N/A</td>
 *         </tr>
 *         <tr>
 *             <td>overwrite</td>
 *             <td>false</td>
 *         </tr>
 *         <tr>
 *              <td>allowUploadToExisting</td>
 *              <td>false</td>
 *          </tr>
 *     </table>
 * </p>
 *
 */
@ExtensionClass
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
    final Property<Boolean> overwrite
    final Property<Boolean> allowUploadToExisting

    final ConfigurableFileCollection releaseAssets

    final Project project

    GithubReleaseExtension(Project project) {
        this.project = project
        final ObjectFactory objectFactory = project.objects
        owner = objectFactory.namedProperty("owner", CharSequence)
        repo = objectFactory.namedProperty("repo", CharSequence)
        authorization = objectFactory.namedProperty("authorization", CharSequence)
        tagName = objectFactory.namedProperty("tagName", CharSequence)
        targetCommitish = objectFactory.namedProperty("targetCommitish", CharSequence)
        releaseName = objectFactory.namedProperty("releaseName", CharSequence)
        body = objectFactory.namedProperty("body", CharSequence)
        draft = objectFactory.namedProperty("draft", Boolean)
        prerelease = objectFactory.namedProperty("prerelease", Boolean)
        releaseAssets = project.files()
        overwrite = objectFactory.namedProperty("overwrite", Boolean)
        allowUploadToExisting = objectFactory.namedProperty("allowUploadToExisting", Boolean)
    }

    Callable<String> changelog(@DelegatesTo(ChangeLogSupplier) final Closure closure) {
        def c = new ChangeLogSupplier(this, project)
        c.with closure
        return c
    }

    Callable<String> changelog() {
        return new ChangeLogSupplier(this, project)
    }

    ConfigurableFileCollection getReleaseAssets() {
        return releaseAssets
    }

    void setReleaseAssets(Object... assets) {
        this.releaseAssets.setFrom(assets)
    }

    void releaseAssets(Object... assets) {
        this.releaseAssets.setFrom(assets)
    }


    void setToken(CharSequence token) {
        this.authorization.set("Token $token")
    }

    void token(CharSequence token) {
        this.authorization.set("Token $token")
    }

    void setToken(Provider<? extends CharSequence> token) {
        this.authorization.set(token.map { "Token $it" })
    }

    void token(Provider<? extends CharSequence> token) {
        this.authorization.set(token.map { "Token $it" })
    }

    void setToken(Callable<? extends CharSequence> token) {
        this.authorization.set(project.provider(token).map { "Token $it" })
    }

    void token(Callable<? extends CharSequence> token) {
        this.authorization.set(project.provider(token).map { "Token $it" })
    }

}

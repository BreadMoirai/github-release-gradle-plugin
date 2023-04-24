/*
 * Copyright (c) 2017 - 2022 BreadMoirai (Ton Ly)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.breadmoirai.githubreleaseplugin

import com.github.breadmoirai.githubreleaseplugin.ast.ExtensionClass
import okhttp3.OkHttpClient
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
 *             <td>'main'</td>
 *         </tr>
 *         <tr>
 *             <td>releaseName</td>
 *             <td>'v' + project.version</td>
 *         </tr>
 *         <tr>
 *             <td>generateReleaseNotes</td>
 *             <td>false</td>
 *         </tr>
 *         <tr>
 *             <td>body</td>
 *             <td>""</td>
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
 *             <td>dryRun</td>
 *             <td>false</td>
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


    final Property<String> owner
    final Property<String> repo
    final Property<String> authorization
    final Property<String> tagName
    final Property<String> targetCommitish
    final Property<String> releaseName
    final Property<Boolean> generateReleaseNotes
    final Property<String> body
    final Property<Boolean> draft
    final Property<Boolean> prerelease
    final Property<Boolean> overwrite
    final Property<Boolean> allowUploadToExisting
    final Property<Boolean> dryRun
    final Property<String> apiEndpoint

    final ConfigurableFileCollection releaseAssets

    final Project project
    private final ChangeLogSupplier changeLogSupplier

    public OkHttpClient client = GithubApi.client

    @SuppressWarnings("GroovyAssignabilityCheck")
    GithubReleaseExtension(Project project) {
        this.project = project
        final ObjectFactory objectFactory = project.objects
        owner = objectFactory.property(String)
        repo = objectFactory.property(String)
        authorization = objectFactory.property(String)
        tagName = objectFactory.property(String)
        targetCommitish = objectFactory.property(String)
        releaseName = objectFactory.property(String)
        generateReleaseNotes = objectFactory.property(Boolean)
        body = objectFactory.property(String)
        draft = objectFactory.property(Boolean)
        prerelease = objectFactory.property(Boolean)
        releaseAssets = project.files()
        overwrite = objectFactory.property(Boolean)
        allowUploadToExisting = objectFactory.property(Boolean)
        dryRun = objectFactory.property(Boolean)
        apiEndpoint = objectFactory.property(String)

        owner {
            return project.group.substring(project.group.lastIndexOf('.') + 1)
        }
        repo {
            project.name ?: project.rootProject?.name ?: project.rootProject?.rootProject?.name
        }
        tagName { "v${project.version}" }
        targetCommitish { 'main' }
        releaseName { "v${project.version}" }
        draft { false }
        prerelease { false }
        // authorization has no default value
        generateReleaseNotes { false }
        body { "" }
        overwrite { false }
        allowUploadToExisting { false }
        apiEndpoint { GithubApi.endpoint }
        dryRun { false }
        changeLogSupplier = new ChangeLogSupplier(project, owner, repo, authorization, tagName, dryRun)
    }

    Callable<String> changelog(@DelegatesTo(ChangeLogSupplier) final Closure closure) {
        changeLogSupplier.with closure
        return changeLogSupplier
    }

    Callable<String> changelog() {
        return changeLogSupplier
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

    void setToken(String token) {
        this.authorization.set("Token $token")
    }

    void token(String token) {
        this.authorization.set("Token $token")
    }

    void setToken(Provider<String> token) {
        this.authorization.set(token.map { "Token $it" })
    }

    void token(Provider<String> token) {
        this.authorization.set(token.map { "Token $it" })
    }

    void setToken(Callable<String> token) {
        this.authorization.set(project.provider(token).map { "Token $it" })
    }

    void token(Callable<String> token) {
        this.authorization.set(project.provider(token).map { "Token $it" })
    }

    OkHttpClient getClient() {
        return client
    }

    void setClient(OkHttpClient client) {
        this.client = client
        GithubApi.client = client
    }

    void client(OkHttpClient client) {
        this.client = client
        GithubApi.client = client
    }
}

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
import com.github.breadmoirai.githubreleaseplugin.exceptions.PropertyNotSetException
import groovy.json.JsonSlurper
import groovy.transform.Memoized
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.zeroturnaround.exec.ProcessExecutor

import java.util.concurrent.Callable

@ExtensionClass
class ChangeLogSupplier implements Callable<String> {

    private final Project project

    private final Provider<CharSequence> owner
    private final Provider<CharSequence> repo
    private final Provider<CharSequence> authorization
    private final Provider<CharSequence> tag

    final Property<CharSequence> executable
    final Property<CharSequence> currentCommit
    final Property<CharSequence> lastCommit
    final Property<List> options
    private final OkHttpClient client = GithubApi.client

    ChangeLogSupplier(GithubReleaseExtension extension, Project project) {
        this.project = project
        def objects = project.objects
        this.owner = extension.ownerProvider
        this.repo = extension.repoProvider
        this.authorization = extension.authorizationProvider
        this.tag = extension.tagNameProvider
        this.executable = objects.property(CharSequence)
        this.currentCommit = objects.property(CharSequence)
        this.lastCommit = objects.property(CharSequence)
        this.options = objects.property(List)
        setExecutable 'git'
        setCurrentCommit "HEAD"
        setLastCommit { this.getLastReleaseCommit() }
        setOptions(["--format=oneline", "--abbrev-commit", "--max-count=50"])
    }

    /**
     * Looks for the previous release's targetCommitish
     * @return
     */
    private CharSequence getLastReleaseCommit() {
        CharSequence owner = this.owner.getOrNull()
        if (owner == null) {
            throw new PropertyNotSetException("owner")
        }
        CharSequence repo = this.repo.getOrNull()
        if (repo == null) {
            throw new PropertyNotSetException("repo")
        }
        CharSequence auth = authorization.getOrNull()
        if (auth == null) {
            throw new PropertyNotSetException("auth")
        }
        CharSequence tag = this.tag.getOrNull()
        if (tag == null) {
            throw new PropertyNotSetException("tag")
        }

        // query the github api for releases
        String releaseUrl = "${GithubApi.endpoint}/repos/$owner/$repo/releases"
        Response response = client.newCall(createRequestWithHeaders(auth)
                .url(releaseUrl)
                .get()
                .build()
        ).execute()
        if (response.code() != 200) {
            return ""
        }
        List releases = new JsonSlurper().parse(response.body().bytes()) as List
        // find current release if exists
        int index = releases.findIndexOf { release -> (release.tag_name == tag) }
        if (releases.isEmpty() || (releases.size() == 1 && index == 0)) {
            CharSequence exe = this.executable.getOrNull()
            if (exe == null) {
                throw new PropertyNotSetException("exe")
            }
            List<String> cmd = [exe, "rev-list", "--max-parents=0", "--max-count=1", "HEAD"]*.toString()
            return new ProcessExecutor()
                    .command(cmd)
                    .readOutput(true)
                    .exitValueNormal()
                    .execute()
                    .outputUTF8()
                    .trim()
        } else {
            // get the next release before the current release
            // if current release does not exist, then gets the most recent release
            Object lastRelease = releases.get(index + 1)
            String lastTag = lastRelease.tag_name
            String tagUrl = "${GithubApi.endpoint}/repos/$owner/$repo/git/refs/tags/$lastTag"
            Response tagResponse = client
                    .newCall(createRequestWithHeaders(auth)
                    .url(tagUrl)
                    .get()
                    .build()
            ).execute()

            // retrieves the sha1 commit from the response
            def bodyS = tagResponse.body().bytes()
            tagResponse.body().close()
            return new JsonSlurper().parse(bodyS).object.sha
        }

    }

    @Override
    @Memoized
    String call() {
        println ':githubRelease Generating Release Body with Commit History'
        CharSequence current = currentCommit.get()
        CharSequence last = lastCommit.get()
        List<String> opts = options.get()*.toString()
        CharSequence get = executable.getOrNull()
        if (get == null) {
            throw new PropertyNotSetException('get')
        }
        List<String> cmds = [get, 'rev-list', *opts, last + '..' + current, '--']
        try {
            return new ProcessExecutor()
                    .command(cmds)
                    .readOutput(true)
                    .exitValueNormal()
                    .execute()
                    .outputUTF8()
        } catch (IOException e) {
            if (e.cause != null && e.cause.message.contains('CreateProcess error=2')) {
                throw new Error('Failed to run git executable to find commit history. ' +
                        'Please specify the path to the git executable.\n')
            }
            else throw e
        }
    }

    static Request.Builder createRequestWithHeaders(CharSequence authorization) {
        return new Request.Builder()
                .addHeader('Authorization', authorization.toString())
                .addHeader('User-Agent', "breadmoirai github-release-gradle-plugin")
                .addHeader('Accept', 'application/vnd.github.v3+json')
                .addHeader('Content-Type', 'application/json')
    }

    @Override
    public String toString() {
        return call()
    }
}
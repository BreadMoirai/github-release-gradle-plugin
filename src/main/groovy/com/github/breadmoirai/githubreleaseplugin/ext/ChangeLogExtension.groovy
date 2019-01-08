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

package com.github.breadmoirai.githubreleaseplugin.ext

import com.github.breadmoirai.githubreleaseplugin.GithubRelease
import com.github.breadmoirai.githubreleaseplugin.exceptions.PropertyNotSetException
import com.github.breadmoirai.githubreleaseplugin.ast.ExtensionProperty
import groovy.json.JsonSlurper
import okhttp3.OkHttpClient
import okhttp3.Response
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor

import java.util.concurrent.Callable

class ChangeLogExtension implements Callable<CharSequence> {

    private static final Logger log = LoggerFactory.getLogger(ChangeLogExtension.class)

    private final Provider<CharSequence> owner
    private final Provider<CharSequence> repo
    private final Provider<CharSequence> authorization
    private final Provider<CharSequence> tag

    @ExtensionProperty
    private final Property<CharSequence> executable
    @ExtensionProperty
    private final Property<CharSequence> currentCommit
    @ExtensionProperty
    private final Property<CharSequence> lastCommit
    @ExtensionProperty
    private final Property<List> options

    private final Project project

    ChangeLogExtension(GithubReleaseExtension extension, Project project) {
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
        executable.set 'git'
        currentCommit.set "HEAD"
        lastCommit.set project.provider({ this.getLastReleaseCommit() })
        options.set(["--format=oneline", "--abbrev-commit", "--max-count=50"])
    }

    /**
     * Looks for the previous release's targetCommitish
     * @return
     */
    private CharSequence getLastReleaseCommit() {
        CharSequence owner = this.owner.getOrThrow()
        CharSequence repo = this.repo.getOrThrow()
        CharSequence auth = this.authorization.getOrThrow()
        CharSequence tag = this.tag.getOrThrow()

        // query the github api for releases
        String releaseUrl = "https://api.github.com/repos/$owner/$repo/releases"
        Response response = new OkHttpClient().newCall(GithubRelease.createRequestWithHeaders(auth)
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
        if (releases.isEmpty()) {
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
            // if current release does not ezist, then gets the most recent release
            Object lastRelease = releases.get(index + 1)
            String lastTag = lastRelease.tag_name
            String tagUrl = "https://api.github.com/repos/$owner/$repo/git/refs/tags/$lastTag"
            Response tagResponse = new OkHttpClient()
                    .newCall(GithubRelease.createRequestWithHeaders(auth)
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
    String call() {
        log.info ':githubRelease Generating Release Body with Commit History'
        CharSequence current = currentCommit.getOrThrow()
        CharSequence last = lastCommit.getOrThrow()
        List<String> opts = options.getOrThrow()*.toString()
        CharSequence git = executable.getOrThrow()
        List<String> cmds = [git, 'rev-list', *opts, last + '..' + current, '--']
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

}
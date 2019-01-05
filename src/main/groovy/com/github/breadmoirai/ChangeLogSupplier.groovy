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

import groovy.json.JsonSlurper
import okhttp3.OkHttpClient
import okhttp3.Response
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import java.util.concurrent.Callable

class ChangeLogSupplier implements Callable<String> {

    private static final Logger log = LoggerFactory.getLogger(ChangeLogSupplier.class)

    private final Project project

    private final Provider<CharSequence> owner
    private final Provider<CharSequence> repo
    private final Provider<CharSequence> authorization
    private final Provider<CharSequence> tag

    private final Property<CharSequence> executable
    private final Property<CharSequence> currentCommit
    private final Property<CharSequence> lastCommit
    private final Property<List> options

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
        }
    }

    public void setCurrentCommit(Provider<? extends CharSequence> currentCommit) {
        this.currentCommit.set(currentCommit)
    }

    public void currentCommit(Provider<? extends CharSequence> currentCommit) {
        this.currentCommit.set(currentCommit)
    }

    public void setLastCommit(Provider<? extends CharSequence> lastCommit) {
        this.lastCommit.set(lastCommit)
    }

    public void lastCommit(Provider<? extends CharSequence> lastCommit) {
        this.lastCommit.set(lastCommit)
    }

    public void setOptions(Provider<List> options) {
        this.options.set(options)
    }

    public void options(Provider<List> options) {
        this.options.set(options)
    }

    public void setExecutable(Provider<CharSequence> gitExecutable) {
        this.executable.set(gitExecutable)
    }

    public void executable(Provider<CharSequence> gitExecutable) {
        this.executable.set(gitExecutable)
    }

    public void setCurrentCommit(Callable<? extends CharSequence> currentCommit) {
        setCurrentCommit project.provider(currentCommit)
    }

    public void currentCommit(Callable<? extends CharSequence> currentCommit) {
        setCurrentCommit project.provider(currentCommit)
    }

    public void setLastCommit(Callable<? extends CharSequence> lastCommit) {
        setLastCommit project.provider(lastCommit)
    }

    public void lastCommit(Callable<? extends CharSequence> lastCommit) {
        setLastCommit project.provider(lastCommit)
    }

    public void setOptions(Callable<List> options) {
        setOptions project.provider(options)
    }

    public void options(Callable<List> options) {
        setOptions project.provider(options)
    }

    public void setExecutable(Callable<CharSequence> gitExecutable) {
        setExecutable project.provider(gitExecutable)
    }

    public void executable(Callable<CharSequence> gitExecutable) {
        setExecutable project.provider(gitExecutable)
    }

    public void setCurrentCommit(CharSequence currentCommit) {
        setCurrentCommit { currentCommit }
    }

    public void currentCommit(CharSequence currentCommit) {
        setCurrentCommit { currentCommit }
    }

    public void setLastCommit(CharSequence lastCommit) {
        setLastCommit { lastCommit }
    }

    public void lastCommit(CharSequence lastCommit) {
        setLastCommit { lastCommit }
    }

    public void setOptions(List options) {
        setOptions { options }
    }

    public void options(List options) {
        setOptions { options }
    }

    public void setExecutable(CharSequence gitExecutable) {
        setExecutable { gitExecutable }
    }

    public void executable(CharSequence gitExecutable) {
        setExecutable { gitExecutable }
    }
}
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

import com.github.breadmoirai.githubreleaseplugin.GithubApi
import com.github.breadmoirai.githubreleaseplugin.ast.ExtensionClass
import com.github.breadmoirai.githubreleaseplugin.exceptions.PropertyNotSetException
import groovy.transform.Memoized
import okhttp3.OkHttpClient
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.zeroturnaround.exec.ProcessExecutor

import java.time.Instant
import java.util.concurrent.Callable

@ExtensionClass
class ChangeLogSupplier implements Callable<String> {

    private final Project project

    private final Provider<String> owner
    private final Provider<String> repo
    private final Provider<String> authorization
    private final Provider<String> tag
    private final Provider<Boolean> dryRun

    final Property<String> executable
    final Property<String> currentCommit
    final Property<String> lastCommit
    final ListProperty<String> options
    private final OkHttpClient client = GithubApi.client

    ChangeLogSupplier(Project project,
                      Provider<String> ownerProvider,
                      Provider<String> repoProvider,
                      Provider<String> authorizationProvider,
                      Provider<String> tagProvider,
                      Provider<Boolean> dryRunProvider) {
        this.project = project
        def objects = project.objects
        this.owner = ownerProvider
        this.repo = repoProvider
        this.authorization = authorizationProvider
        this.tag = tagProvider
        this.dryRun = dryRunProvider
        this.executable = objects.property(String)
        this.currentCommit = objects.property(String)
        this.lastCommit = objects.property(String)
        this.options = objects.listProperty(String)
        setExecutable 'git'
        setCurrentCommit "HEAD"
        setLastCommit { this.getLastReleaseCommit() }
        setOptions("--format=oneline", "--abbrev-commit", "--max-count=50")
    }

    /**
     * Looks for the previous release's targetCommitish
     * @return
     */
    private String getLastReleaseCommit() {
        log 'Searching for previous release on Github'
        String owner = this.owner.getOrNull()
        if (owner == null) {
            throw new PropertyNotSetException("owner")
        }
        String repo = this.repo.getOrNull()
        if (repo == null) {
            throw new PropertyNotSetException("repo")
        }
        String auth = authorization.getOrNull()
        if (auth == null) {
            throw new PropertyNotSetException("auth")
        }
        String tag = this.tag.getOrNull()
        if (tag == null) {
            throw new PropertyNotSetException("tag")
        }

        // query the github api for releases
        def api = new GithubApi(auth)
        log 'RETRIEVING RELEASES'
        GithubApi.Response response = api.getReleases(owner, repo)
        if (response.code != 200) {
            // i should do something here?
            return ""
        }
        List releases = response.body as List
        releases.sort(Comparator.comparing { release -> Instant.parse(release.created_at) }.reversed())
        // find current release if exists
        int index = releases.findIndexOf { release -> (release.tag_name == tag) }
        for (int i = 0; i < releases.size(); i++) {
            log "$i : ${releases[i].tag_name}"
        }
        if (releases.isEmpty() || (releases.size() == 1 && index == 0) || index + 1 == releases.size()) {
            String exe = this.executable.getOrNull()
            if (exe == null) {
                throw new PropertyNotSetException("exe")
            }
            log "Previous release not found"
            log "Searching for earliest commit"
            List<String> cmd = [exe, "rev-list", "--max-parents=0", "--max-count=1", "HEAD"]
            log "Running `${cmd.join(' ')}`"
            def result = new ProcessExecutor()
                    .command(cmd)
                    .readOutput(true)
                    .exitValueNormal()
                    .execute()
                    .outputUTF8()
                    .trim()
            log "Found $result"
            return result
        } else {
            // get the next release before the current release
            // if current release does not exist, then gets the most recent release
            Object lastRelease = releases.get(index + 1)
            String lastTag = lastRelease.tag_name
            String tagUrl = "${GithubApi.endpoint}/repos/$owner/$repo/git/refs/tags/$lastTag"
            def previousRelease = api.findTagByName(owner, repo, lastTag)
            def commit = previousRelease.body.object.sha
            log("Found previous release with tag $lastTag at commit $commit")
            // retrieves the sha1 commit from the response
            return commit
        }
    }


    @Override
    @Memoized
    String call() {
        log 'Creating...'
        String current = currentCommit.get()
        String last = lastCommit.get()
        List<String> opts = options.get()
        String get = executable.getOrNull()
        if (get == null) {
            throw new PropertyNotSetException('get')
        }
        List<String> cmds = [get, 'rev-list', *opts, last + '..' + current, '--']
        log "Running `${cmds.join(' ')}`"
        try {
            def reslt = new ProcessExecutor()
                    .command(cmds)
                    .readOutput(true)
                    .exitValueNormal()
                    .execute()
                    .outputUTF8()
            log '\n\t\t' + reslt.replace('\n', '\n\t\t')
            return reslt
        } catch (IOException e) {
            if (e.cause != null && e.cause.message.contains('CreateProcess error=2')) {
                throw new Error('Failed to run git executable to find commit history. ' +
                        'Please specify the path to the git executable.\n')
            } else throw e
        }
    }

    @Override
    public String toString() {
        return call()
    }

    private void log(String message) {
        if (dryRun.get())
            println ":githubRelease CHANGELOG [$message]"
    }

    public void setOptions(String... options) {
        this.options.set(options.toList())
    }

    public void setOptions(Iterable<String> options) {
        this.options.set options
    }

    public void options(String... options) {
        this.options.set(options.toList())
    }

    public void options(Iterable<String> options) {
        this.options.set options
    }

    public void addOption(String option) {
        this.options.add(option)
    }

}

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
import org.gradle.api.internal.provider.AbstractProvider
import org.gradle.api.provider.Provider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor

import java.util.concurrent.Callable

class ChangeLogProvider extends AbstractProvider<CharSequence> {

    private static final Logger log = LoggerFactory.getLogger(ChangeLogProvider.class)

    private final Provider<CharSequence> owner
    private final Provider<CharSequence> repo
    private final Provider<CharSequence> authorization
    private final Provider<CharSequence> tag

    private Provider<CharSequence> currentReleaseCommit
    private Provider<CharSequence> lastReleaseCommit
    private Provider<List<CharSequence>> options

    ChangeLogProvider(GithubReleaseTask task) {
        this.owner = owner
        this.repo = repo
        this.authorization = authorization
        this.tag = tag
        setCurrentReleaseCommit "HEAD"
        setLastReleaseCommit this.&getLastReleaseCommit
        setOptions(["--format=oneline", "--abbrev-commit", "--max-count=50"])
    }

    private CharSequence getLastReleaseCommit() {
        String releaseUrl = "https://api.github.com/repos/${this.owner.get()}/${this.repo.get()}/releases"
        Response response = new OkHttpClient().newCall(GithubRelease.createRequestWithHeaders(authorization.get())
                .url(releaseUrl)
                .get()
                .build()
        ).execute()
        if (response.code() != 200) {
            return ""
        }
        List releases = new JsonSlurper().parse(response.body().bytes()) as List
        // find current release if exists
        String lastCommit
        int index = releases.findIndexOf { release -> release.tag_name == tag.get() }
        if (releases.isEmpty() || releases.size() > index) {
            lastCommit = new ProcessExecutor().command("git", "rev-list", "--max-parents=0", "--max-count=1", "HEAD")
                    .readOutput(true)
                    .exitValueNormal()
                    .execute()
                    .outputUTF8()
                    .trim()
        } else {
            Object lastRelease = releases.get(index + 1)
            String lastTag = lastRelease.tag_name
            lastCommit = new ProcessExecutor().command("git", "rev-parse", "--verify", lastTag + "^{commit}")
                    .readOutput(true)
                    .exitValueNormal()
                    .execute()
                    .outputUTF8()
                    .trim()
        }
        return lastCommit
    }

    @Override
    Class<CharSequence> getType() {
        return CharSequence.class
    }

    @Override
    CharSequence getOrNull() {
        log.info ':githubRelease Generating Release Body with Commit History'
        CharSequence current = currentReleaseCommit.get()
        CharSequence last = lastReleaseCommit.get()
        List<String> opts = options.get()*.toString()
        List<String> cmds = ["git", "rev-list", *opts, last + ".." + current]
        return new ProcessExecutor()
                .command(cmds)
                .readOutput(true)
                .exitValueNormal()
                .execute()
                .outputUTF8()
    }

    void setCurrentReleaseCommit(Provider<CharSequence> currentReleaseCommit) {
        this.currentReleaseCommit = currentReleaseCommit
    }

    void setLastReleaseCommit(Provider<CharSequence> lastReleaseCommit) {
        this.lastReleaseCommit = lastReleaseCommit
    }

    void setOptions(Provider<List<CharSequence>> options) {
        this.options = options
    }

    void setCurrentReleaseCommit(Callable<CharSequence> currentReleaseCommit) {
        setCurrentReleaseCommit new TypedDefaultProvider<>(CharSequence.class, currentReleaseCommit)
    }

    void setLastReleaseCommit(Callable<CharSequence> lastReleaseCommit) {
        setLastReleaseCommit new TypedDefaultProvider<>(CharSequence.class, lastReleaseCommit)
    }

    void setOptions(Callable<List<CharSequence>> options) {
        setOptions new TypedDefaultProvider<>(CharSequence.class, options)
    }
    void setCurrentReleaseCommit(CharSequence currentReleaseCommit) {
        setCurrentReleaseCommit {currentReleaseCommit}
    }

    void setLastReleaseCommit(CharSequence lastReleaseCommit) {
        setLastReleaseCommit {lastReleaseCommit}
    }

    void setOptions(List<CharSequence> options) {
        setOptions {options}
    }
}
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

import org.gradle.api.file.FileCollection
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GithubReleaseFunctionalTest extends Specification {
    static final String owner = "breadmoirai"
    static final String repo = "github-release-gradle-plugin"
    String tagName
    static final String targetCommitish = "master"
    static final String releaseName = "test"
    String body = ""
    boolean draft = false
    boolean prerelease = false
    FileCollection releaseAssets


    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile
    List<File> pluginClasspath

    def setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle")

        def pluginClasspathResource = this.class.classLoader.getResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    def
    "manual test"() {
        given:
        buildFile << """
        plugins {
            id 'com.github.breadmoirai.github-release'
        } 
        
        group = 'com.github.breadmoirai'
        version = '0.0.0'
        
        githubRelease {
            repo = 'test'
        }
        
        """.stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('githubRelease')
            .withPluginClasspath(pluginClasspath)
            .build()

        then:
        result.task(":githubRelease").outcome == SUCCESS
        println "result.output = $result.output"

    }

}
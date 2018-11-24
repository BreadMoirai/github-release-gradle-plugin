///*
// *    Copyright 2017 - 2018 BreadMoirai (Ton Ly)
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the License is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the License for the specific language governing permissions and
// *    limitations under the License.
// */
//
//package com.github.breadmoirai
//
//import org.gradle.testkit.runner.GradleRunner
//import org.zeroturnaround.exec.ProcessExecutor
//import spock.lang.Ignore
//import spock.lang.Specification
//
//import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
//
//class GithubReleaseUnitTest extends Specification {
//
//    def setup() throws IOException {
//
//    }
//
//    @Ignore
//    def "Test1"() {
//        when:
//        def c = new ChangeLogSupplier(new GithubReleaseExtension())
//        c.lastCommit = {
//
//            String lastTag = "test"
//            String lastCommit = new ProcessExecutor().command("git", "rev-parse", "--verify", lastTag + "^{commit}")
//                    .readOutput(true)
//                    .exitValueNormal()
//                    .execute()
//                    .outputUTF8()
//                    .trim()
//            return lastCommit
//        }
//        c.options = {
//            ["--format=oneline", "--abbrev-commit", "--max-count=50", "--graph"]
//        }
//        then:
//        String get = c.get()
//        println "get = $get"
//
//
//    }
//
//    @Ignore
//    def
//    "delete release"() {
//        given: "A release with a tag"
//        def app = new GithubRelease("BreadMoirai", "github-release-gradle-plugin",)
//
//        when:
//        def result = GradleRunner.create()
//                .withProjectDir(testProjectDir.root)
//                .withArguments('githubRelease')
//                .withPluginClasspath(pluginClasspath)
//                .build()
//
//        then:
//        result.task(":githubRelease").outcome == SUCCESS
//        println "result.output = $result.output"
//
//    }
//
//}
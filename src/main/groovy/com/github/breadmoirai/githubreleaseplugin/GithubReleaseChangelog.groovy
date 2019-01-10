//package com.github.breadmoirai.githubreleaseplugin
//
//import com.github.breadmoirai.githubreleaseplugin.exceptions.PropertyNotSetException
//import com.github.breadmoirai.githubreleaseplugin.ext.ChangeLogExtension
//import groovy.json.JsonSlurper
//import okhttp3.OkHttpClient
//import okhttp3.Response
//import org.gradle.api.Project
//import org.gradle.api.provider.Provider
//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
//import org.zeroturnaround.exec.ProcessExecutor
//
//class GithubReleaseChangelog {
//    private static final Logger log = LoggerFactory.getLogger(ChangeLogExtension.class)
//
//    private final Provider<CharSequence> owner
//    private final Provider<CharSequence> repo
//    private final Provider<CharSequence> authorization
//    private final Provider<CharSequence> tag
//    private final Provider<CharSequence> executable
//    private final Provider<CharSequence> currentCommit
//    private final Provider<CharSequence> lastCommit
//    private final Provider<List> options
//
//    private final Project project
//
//    GithubReleaseChangelog(Provider<CharSequence> owner, Provider<CharSequence> repo, Provider<CharSequence> authorization, Provider<CharSequence> tag, Provider<CharSequence> executable, Provider<CharSequence> currentCommit, Provider<CharSequence> lastCommit, Provider<List> options, Project project) {
//        this.owner = owner
//        this.repo = repo
//        this.authorization = authorization
//        this.tag = tag
//        this.executable = executable
//        this.currentCommit = currentCommit
//        this.lastCommit = lastCommit
//        this.options = options
//        this.project = project
//    }
//
//    /**
//     * Looks for the previous release's targetCommitish
//     * @return
//     */
//    private CharSequence getLastReleaseCommit() {
//        CharSequence owner = this.owner.getOrThrow()
//        CharSequence repo = this.repo.getOrThrow()
//        CharSequence auth = this.authorization.getOrThrow()
//        CharSequence tag = this.tag.getOrThrow()
//
//        // query the github api for releases
//        String releaseUrl = "https://api.github.com/repos/$owner/$repo/releases"
//        Response response = new OkHttpClient().newCall(GithubRelease.createRequestWithHeaders(auth)
//                                                               .url(releaseUrl)
//                                                               .get()
//                                                               .build()
//                                                      ).execute()
//        if (response.code() != 200) {
//            return ""
//        }
//        List releases = new JsonSlurper().parse(response.body().bytes()) as List
//        // find current release if exists
//        int index = releases.findIndexOf { release -> (release.tag_name == tag) }
//        if (releases.isEmpty()) {
//            CharSequence exe = this.executable.getOrNull()
//            if (exe == null) {
//                throw new PropertyNotSetException("exe")
//            }
//            List<String> cmd = [exe, "rev-list", "--max-parents=0", "--max-count=1", "HEAD"]*.toString()
//            return new ProcessExecutor()
//                    .command(cmd)
//                    .readOutput(true)
//                    .exitValueNormal()
//                    .execute()
//                    .outputUTF8()
//                    .trim()
//        } else {
//            // get the next release before the current release
//            // if current release does not ezist, then gets the most recent release
//            Object lastRelease = releases.get(index + 1)
//            String lastTag = lastRelease.tag_name
//            String tagUrl = "https://api.github.com/repos/$owner/$repo/git/refs/tags/$lastTag"
//            Response tagResponse = new OkHttpClient()
//                    .newCall(GithubRelease.createRequestWithHeaders(auth)
//                                     .url(tagUrl)
//                                     .get()
//                                     .build()
//                            ).execute()
//
//            // retrieves the sha1 commit from the response
//            def bodyS = tagResponse.body().bytes()
//            tagResponse.body().close()
//            return new JsonSlurper().parse(bodyS).object.sha
//        }
//
//    }
//
//    @Override
//    String call() {
//        log.info ':githubRelease Generating Release Body with Commit History'
//        CharSequence current = currentCommit.getOrThrow()
//        CharSequence last = lastCommit.getOrThrow()
//        List<String> opts = options.getOrThrow()*.toString()
//        CharSequence git = executable.getOrThrow()
//        List<String> cmds = [git, 'rev-list', *opts, last + '..' + current, '--']
//        try {
//            return new ProcessExecutor()
//                    .command(cmds)
//                    .readOutput(true)
//                    .exitValueNormal()
//                    .execute()
//                    .outputUTF8()
//        } catch (IOException e) {
//            if (e.cause != null && e.cause.message.contains('CreateProcess error=2')) {
//                throw new Error('Failed to run git executable to find commit history. ' +
//                                        'Please specify the path to the git executable.\n')
//            }
//            else throw e
//        }
//    }
//}

# github-release
[![Gradle Plugin Portal](https://img.shields.io/badge/version-2.5.1-blue.svg)](https://plugins.gradle.org/plugin/com.github.breadmoirai.github-release/2.5.1)

[p63]: https://github.com/BreadMoirai/github-release-gradle-plugin/pull/63
[p62]: https://github.com/BreadMoirai/github-release-gradle-plugin/pull/62
[p61]: https://github.com/BreadMoirai/github-release-gradle-plugin/pull/61
[p60]: https://github.com/BreadMoirai/github-release-gradle-plugin/pull/60
[i51]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/53
[i51]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/51
[p42]: https://github.com/BreadMoirai/github-release-gradle-plugin/pull/42
[p41]: https://github.com/BreadMoirai/github-release-gradle-plugin/pull/41
[p38]: https://github.com/BreadMoirai/github-release-gradle-plugin/pull/38
[i40]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/40
[i39]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/39
[i37]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/37
[i36]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/36
[i32]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/32
[i31]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/31
[i28]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/28
[i27]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/27
[i23]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/23
[i22]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/22
[i21]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/21
[i20]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/20
[i19]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/19
[i17]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/17
[i16]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/16
[i14]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/14
[i11]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/11
[i5]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/5
[i4]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/4


A Gradle Plugin to send Releases to Github

This plugin is not endorsed by Github.

This plugin uses [OkHttp](http://square.github.io/okhttp/) to send a POST requests to the github api that creates a release and uploads specified assets.

## Known Issues
If you are using multiple GithubRelease tasks to compose a single release, the release must be published `draft=false` with the first task. Currently, the plugin cannot find an existing release if it is a draft.  
Some version numbers are skipped because of issues with the gradle plugin portal.

## Changelog
2.5.1
- Fixed classpath [error](https://github.com/BreadMoirai/github-release-gradle-plugin/pull/62#discussion_r1354355485)

2.5
- Merged [#60][p60], [#61][p61], [#62][p62], [#63][p63] (Thanks for contributing!)
- Uses of CharSequence have been replaced with String 

2.4.1
- Add support for GitHub generated release notes with `generateReleaseNotes: true`. If a name is not provided one will be generated. If a body is provided it will be pre-pended to the auto generated notes. Fixes [#51][i51].

2.3.7
- Update dependencies to latest version
  - OkHttp 4.*, etc; Addressing [#36][i36]
- Update to Gradle 7; Addressing [#40][i40]
- Setting draft to `false` and running the task with an existing release will now publish the release if it is a draft. If already published, will error with `RELEASE ALREADY EXISTS`
- If draft is set to `false`, task will now first create the release with draft set to `true`, upload assets, and then update the release with draft set to `false`, publishing it. Addressing [#28][i28]. Does not work if steps are split up into separate tasks
- Added recipes for configuring tasks addressing [#39][i39]
- Merged [#38][p38], [#41][p41], [#42][p42]

2.2.12
- Address [#32][i32] Exposed the OkHttpClient with `client`.

2.2.11
- Address [#31][i31] Added `dryRun` property. You can set this to `true` to show run the task without actually modifying anything.
- Changed internal implementation of changelog generation. Please open an issue if the behavior is unexpected.
- Note that Username and Password authentication with be disabled on July 1st, 2020 for the Github API.
 
2.2.10
- Address [#27][i27]. Http 307 Redirects are now respected.

2.2.9
- Address [#21][i21] & [#23][i23]. Multiple assets were being mistakenly uploaded with the same name.
- Address [#22][i22]. Remove deprecated gradle api call for list property.
 
2.2.8
- Address [#20][i20]. Change minimum supported version for Gradle to 4.10+ down from 5.x

2.2.7
- Address [#19][i19]. Replaced mime type detector with Apache Tika.

2.2.6
- Address [#14][i14] with new property [`apiEndpoint`](https://github.com/BreadMoirai/github-release-gradle-plugin/wiki#apiEndpoint)

2.2.5
- Removed unnecessary urls in logging
- Address [#17][i17] by setting default values elsewhere
- Address [#16][i16] by applying changes as suggested 

2.2.4
- Changed `implementation` to `compile` as per [#11][i11]

2.2.3
- Updated for Gradle 5.x

2.2.2
- new option [`allowUploadToExisting`](https://github.com/BreadMoirai/github-release-gradle-plugin/wiki#allowUploadToExisting)

2.2.1
- Address [#5][i5]
- Address [#4][i4] build with Java 8 

2.2.0
- Added more detailed information in [wiki](https://github.com/BreadMoirai/github-release-gradle-plugin/wiki)
- This will no longer delete existing releases by default and must be specified with `overwrite = true`
- Added ability to use username and password as shown [here](https://github.com/BreadMoirai/github-release-gradle-plugin/wiki#authorization)

### Adding as a dependency

[Gradle Plugin Page](https://plugins.gradle.org/plugin/com.github.breadmoirai.github-release)


Using the plugins DSL:

```groovy
plugins {
  id "com.github.breadmoirai.github-release" version "2.4.1"
}
```
Using legacy plugin application:

```groovy
buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath "com.github.breadmoirai:github-release:2.4.1"
  }
}

apply plugin: "com.github.breadmoirai.github-release"
```



## Using this plugin

```groovy
githubRelease {
    token "<your token>" // This is your personal access token with Repo permissions
                         // You get this from your user settings > developer settings > Personal Access Tokens
    owner "breadmoirai" // default is the last part of your group. Eg group: "com.github.breadmoirai" => owner: "breadmoirai"
    repo "github-release" // by default this is set to your project name
    tagName "v1.0.0" // by default this is set to "v${project.version}"
    targetCommitish "main" // by default this is set to "main"
    releaseName "v1.0.0" // Release title, by default this is the same as the tagName
    generateReleaseNotes false // Generate release notes automatically, if true and body is present, body will be prepended, if name is not given, one will be generated by the tag
    body "" // by default this is empty
    draft true // by default this is true
    prerelease false // by default this is false
    releaseAssets jar.destinationDir.listFiles // this points to which files you want to upload as assets with your release, by default this is empty
    allowUploadToExisting.set false // Setting this to true will allow this plugin to upload artifacts to a release if it found an existing one. If overwrite is set to true, this option is ignored.  
    overwrite false // by default false; if set to true, will delete an existing release with the same tag and name
    dryRun false // by default false; you can use this to see what actions would be taken without making a release
    apiEndpoint "https://api.github.com" // should only change for github enterprise users
    client // This is the okhttp client used for http requests
}
```
View more information on each field at the [WIKI](https://github.com/BreadMoirai/github-release-gradle-plugin/wiki)

For additional info on these fields please see the [Github API specification](https://developer.github.com/v3/repos/releases/#create-a-release).


### Additional Tips:

You can use a provider with a closure to defer evaluation.
```groovy
body provider({
    //do something intensive
    return "wow"
})
```
#### [Body Changelog](https://github.com/BreadMoirai/github-release-gradle-plugin/wiki#changelog)
This plugin also provides a way to retrieve a list of commits since your last release. This uses the commandline to call git 
```groovy
body provider(changelog())
// or
body provider { """\
## CHANGELOG
${changelog().call()}
""" }
```
The changelog can be modified as follows
```groovy
body provider(changelog {
    currentCommit "HEAD"
    lastCommit "HEAD~10"
    options(["--format=oneline", "--abbrev-commit", "--max-count=50", "graph"])
})
```
You can also apply string operations to the result.
```groovy
body provider({ """\
# Info
...

## ChangeLog
${changelog().call().replace('\n', '\n* ')}
""" })
```


#### Token
You can store your token in a gradle.properties located in either `USER/.gradle` or in the project directory and then retrieve it with `getProperty('github.token')`

#### Release Assets
You can avoid removing irrelevant files from your selected directory each time you publish a release by using a filter. 
For Example 
```groovy
FilenameFilter filter = { dir, filename -> filename.contains(project.version) }
releaseAssets jar.destinationDir.asFileTree.listFiles filter
// or
releaseAssets jar.archiveFile
```

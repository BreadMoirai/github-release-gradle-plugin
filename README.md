# github-release
[![Gradle Plugin Portal](https://img.shields.io/badge/version-2.2.12-blue.svg)](https://plugins.gradle.org/plugin/com.github.breadmoirai.github-release/2.2.12)

[i32]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/32
[i31]: https://github.com/BreadMoirai/github-release-gradle-plugin/issues/31
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

## Changelog
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
  id "com.github.breadmoirai.github-release" version "2.2.12"
}
```
Using legacy plugin application:

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "com.github.breadmoirai:github-release:2.2.12"
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
    targetCommitish "master" // by default this is set to "master"
    releaseName "v1.0.0" // Release title, by default this is the same as the tagName
    body "" // by default this is empty
    draft false // by default this is false
    prerelease false // by default this is false
    releaseAssets jar.destinationDir.listFiles // this points to which files you want to upload as assets with your release

    overwrite false // by default false; if set to true, will delete an existing release with the same tag and name
    dryRun false // by default false; you can use this to see what actions would be taken without making a release
    apiEndpoint "https://api.github.com" // should only change for github enterprise users
    client // This is the okhttp client used for http requests
}
```
View more information on each field at the [WIKI](https://github.com/BreadMoirai/github-release-gradle-plugin/wiki)

For additional info on these fields please see the [Github API specification](https://developer.github.com/v3/repos/releases/#create-a-release).


### Additional Tips:

All properties except releaseAssets support using a closure to defer evaluation.
```groovy
body {
    //do something intensive
    return "wow"
}
```
#### [Body Changelog](https://github.com/BreadMoirai/github-release-gradle-plugin/wiki#changelog)
This plugin also provides a way to retrieve a list of commits since your last release. This uses the commandline to call git 
```groovy
body changelog()
// or
body """\
## CHANGELOG
${changelog().call()}
"""
```
The changelog can be modified as follows
```groovy
body changelog {
    currentCommit "HEAD"
    lastCommit "HEAD~10"
    options(["--format=oneline", "--abbrev-commit", "--max-count=50", "graph"])
}
```
You can also apply string operations to the result.
```groovy
body { """\
# Info
...

## ChangeLog
${changelog().call().replace('\n', '\n* ')}
""" }
```


#### Token
You can store your token in a gradle.properties located in either `USER/.gradle` or in the project directory and then retrieve it with `getProperty('github.token')`

#### Release Assets
You can avoid removing irrelevant files from your selected directory each time you publish a release by using a filter. 
For Example 
```groovy
FilenameFilter filter = { dir, filename -> filename.contains(project.version) }
releaseAssets = jar.destinationDir.listFiles filter
```

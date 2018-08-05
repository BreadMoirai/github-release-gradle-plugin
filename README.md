# github-release
A Gradle Plugin to send Releases to Github

This plugin is not endorsed by Github.

This plugin uses [OkHttp](http://square.github.io/okhttp/) to send a POST requests to the github api that creates a release and uploads specified assets.
If a duplicate release already exists on the Github repo, that release is overridden by this plugin.

### Adding as a dependency
[Gradle Plugin Page](https://plugins.gradle.org/plugin/com.github.breadmoirai.github-release)

Build script snippet for use in all Gradle versions:
```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.github.breadmoirai:github-release:2.0.2"
  }
}

apply plugin: "com.github.breadmoirai.github-release"
```

Build script snippet for new, incubating, plugin mechanism introduced in Gradle 2.1:
```groovy
plugins {
  id "com.github.breadmoirai.github-release" version "2.0.2"
}
```

## Using this plugin

```groovy
githubRelease {
    token = "your token"// This is your personal access token with Repo permissions
                        // You get this from your user settings > developer settings
                        // If this value is not set, a login dialog will prompt you for your credentials.
                        // 2fa is not supported
    owner = "breadmoirai" // default is the last part of your group. Eg group: "com.github.breadmoirai" => owner: "breadmoirai"
    repo = "github-release" // by default this is set to your project name
    tagName = "v1.0.0" // by default this is set to "v${project.version}"
    targetCommitish = "master" // by default this is set to "master"
    releaseName = "v1.0.0" // Release title, by default this is the same as the tagName
    body = "Wham, bam! Thank you clam!" // by default this is empty
    draft = false // by default this is false
    prerelease = false // by default this is false
    releaseAssets = jar.destinationDir.listFiles // this points to which files you want to upload as assets with your release
}
```
For additional info on these fields please see the [Github API specification](https://developer.github.com/v3/repos/releases/#create-a-release).


#### Additional Tips:

You can avoid removing irrelevant files from your selected directory each time you publish a release by using a filter. 
For Example 
```groovy
FilenameFilter filter = { dir, filename -> filename.contains(project.version) }
releaseAssets = jar.destinationDir.listFiles filter
```

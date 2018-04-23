package com.github.breadmoirai

import org.gradle.api.Plugin
import org.gradle.api.Project

class GithubReleasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def ext = project.extensions.create('githubRelease', GithubReleaseExtension, project)

        project.tasks.create('githubRelease', GithubReleaseTask) {
            it.with {
                setToken ext.tokenProvider
                setOwner ext.ownerProvider
                setRepo ext.repoProvider
                setTagName ext.tagNameProvider
                setTargetCommitish ext.targetCommitishProvider
                setReleaseName ext.releaseNameProvider
                setBody ext.bodyProvider
                setDraft ext.draftProvider
                setPrerelease ext.prereleaseProvider
                setReleaseAssets ext.releaseAssets
            }
        }
    }
}
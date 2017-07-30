package net.breadmoirai

import org.gradle.api.Plugin
import org.gradle.api.Project

class GithubReleasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def ext = project.extensions.create('githubRelease', GithubReleaseExtension, project)

        project.tasks.create('publishRelease', GithubReleaseTask) { t ->
            t.token.set ext.tokenProvider
            t.owner.set ext.ownerProvider
            t.repo.set ext.repoProvider
            t.tagName.set ext.tagNameProvider
            t.targetCommitish.set ext.targetCommitishProvider
            t.releaseName.set ext.nameProvider
            t.body.set ext.bodyProvider
            t.draft.set ext.draftProvider
            t.prerelease.set ext.prereleaseProvider
            t.releaseAssets.set ext.releaseAssets
        }
    }
}
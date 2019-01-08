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

package com.github.breadmoirai.githubreleaseplugin

import com.github.breadmoirai.githubreleaseplugin.ast.ExtensionProperty
import com.github.breadmoirai.githubreleaseplugin.exceptions.PropertyNotSetException
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

class GithubReleaseTask extends DefaultTask {

    @Input
    @ExtensionProperty
    final Property<CharSequence> owner
    @Input
    @ExtensionProperty
    final Property<CharSequence> repo
    @Input
    @ExtensionProperty
    final Property<CharSequence> authorization
    @Input
    @ExtensionProperty
    final Property<CharSequence> tagName
    @Input
    @ExtensionProperty
    final Property<CharSequence> targetCommitish
    @Input
    @ExtensionProperty
    final Property<CharSequence> releaseName
    @Input
    @ExtensionProperty
    final Property<CharSequence> body
    @Input
    @ExtensionProperty
    final Property<Boolean> draft
    @Input
    @ExtensionProperty
    final Property<Boolean> prerelease
    @InputFiles
    final ConfigurableFileCollection releaseAssets
    @Input
    @ExtensionProperty
    final Property<Boolean> overwrite
    @Input
    @ExtensionProperty
    final Property<Boolean> allowUploadToExisting

    GithubReleaseTask() {
        this.setGroup('publishing')
        final ObjectFactory objectFactory = project.objects
        owner = objectFactory.property(CharSequence)
        repo = objectFactory.property(CharSequence)
        authorization = objectFactory.property(CharSequence)
        tagName = objectFactory.property(CharSequence)
        targetCommitish = objectFactory.property(CharSequence)
        releaseName = objectFactory.property(CharSequence)
        body = objectFactory.property(CharSequence)
        draft = objectFactory.property(Boolean)
        prerelease = objectFactory.property(Boolean)

        releaseAssets = project.files()

        overwrite = objectFactory.property(Boolean)
        allowUploadToExisting = objectFactory.property(Boolean)
    }

    @TaskAction
    void publishRelease() {
        CharSequence tag = this.tagName.getOrThrow()
        CharSequence tar = this.targetCommitish.getOrThrow()
        CharSequence rel = this.releaseName.getOrThrow()
        CharSequence bod = this.body.getOrThrow()
        CharSequence own = this.owner.getOrThrow()
        CharSequence rep = this.repo.getOrThrow()
        Boolean dra = this.draft.getOrThrow()
        Boolean pre = this.prerelease.getOrThrow()
        CharSequence auth = this.authorization.getOrThrow()
        FileCollection releaseAssets = this.releaseAssets
        new GithubRelease(own, rep, auth, tag, tar, rel, bod, dra, pre, releaseAssets, this.overwrite, this.allowUploadToExisting).run()
    }

    void setReleaseAssets(Object... assets) {
        this.releaseAssets.setFrom(assets)
    }

}

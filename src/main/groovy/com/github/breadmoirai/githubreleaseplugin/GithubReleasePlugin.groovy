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

import com.github.breadmoirai.githubreleaseplugin.exceptions.PropertyNotSetException
import com.github.breadmoirai.githubreleaseplugin.ext.GithubReleaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Callable

class GithubReleasePlugin implements Plugin<Project> {

    private final static Logger log = LoggerFactory.getLogger(GithubReleasePlugin.class)
    public static boolean infoEnabled = false
    private Project project

    static {
        Provider.metaClass.name = "Undefined"
        Provider.metaClass.getOrThrow << {
            def val = delegate.getOrNull()
            if (val == null) {
                String name = delegate.name
                throw new PropertyNotSetException(name)
            }
            return val
        }
        ObjectFactory.metaClass.namedProperty << { String name, Class<?> valueType ->
            def provider = delegate.property(valueType)
            provider.name = name
            return provider
        }
    }

    @Override
    void apply(Project project) {
        this.project = project
        infoEnabled = project.logger.infoEnabled

        log.debug("Creating Extension GithubRelease")
        def ext = project.extensions.create('githubRelease', GithubReleaseExtension, project)

        project.tasks.create('githubRelease', GithubReleaseTask) {
            log.debug("Creating Task githubRelease")
            it.with {
                setAuthorization ext.authorizationProvider
                setOwner ext.ownerProvider
                setRepo ext.repoProvider
                setTagName ext.tagNameProvider
                setTargetCommitish ext.targetCommitishProvider
                setReleaseName ext.releaseNameProvider
                setBody ext.bodyProvider
                setDraft ext.draftProvider
                setPrerelease ext.prereleaseProvider
                setReleaseAssets ext.releaseAssets
                setOverwrite ext.overwriteProvider
                setAllowUploadToExisting ext.allowUploadToExistingProvider
            }
        }

        project.afterEvaluate {
            def self = project.plugins.findPlugin(GithubReleasePlugin)

            if (self) {
                log.debug "Assigning default values for GithubReleasePlugin"
                GithubReleaseExtension e = project.extensions.getByType GithubReleaseExtension
                setOrElse e.owner, {
                    def group = project.group.toString()
                    group.substring(group.lastIndexOf('.') + 1)
                }
                setOrElse e.repo, {
                    project.name ?: project.rootProject?.name ?: project.rootProject?.rootProject?.name
                }
                setOrElse e.tagName, { "v${project.version}" }
                setOrElse e.targetCommitish, { 'master' }
                setOrElse e.releaseName, {
                    e.tagName.get()
                }
                setOrElse e.draft, { false }
                setOrElse e.prerelease, { false }
                // authorization has no default value
                setOrElse e.body, { "" }
                setOrElse e.overwrite, { false }
                setOrElse e.allowUploadToExisting, { false }
            }
        }
    }

    private <T> void setOrElse(Property<T> prop, Callable<T> value) {
        if (!prop.isPresent()) {
            prop.set project.provider(value)
        }
    }

}

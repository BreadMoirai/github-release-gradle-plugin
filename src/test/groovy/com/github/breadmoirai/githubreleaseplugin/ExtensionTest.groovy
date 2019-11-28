package com.github.breadmoirai.githubreleaseplugin
//package com.github.breadmoirai
//
//import ExtensionProperty
//import groovy.mock.interceptor.MockFor
//import org.gradle.api.Project
//import org.gradle.api.model.ObjectFactory
//import org.gradle.api.provider.Property
//import spock.lang.Specification
//
//class ExtensionTest extends Specification {
//
//
//
//    def "test provider getters"() {
//        given:
//            def projectMock = new MockFor(Project)
//            def objectFactoryMock = new MockFor(ObjectFactory)
//            projectMock.demand.getObjects { objectFactoryMock }
//            objectFactoryMock.demand.property { it ->
//                return new MockFor(Property)
//            }
//
//        when:
//            projectMock.use {
//                new GithubReleaseExtension()
//            }
//
//        then:
//            1
//    }
//}

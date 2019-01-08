package com.github.breadmoirai.githubreleaseplugin.ast

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.FIELD])
@GroovyASTTransformationClass(["com.github.breadmoirai.githubreleaseplugin.ast.ExtensionPropertyASTTransformation"])
@interface ExtensionProperty {
}
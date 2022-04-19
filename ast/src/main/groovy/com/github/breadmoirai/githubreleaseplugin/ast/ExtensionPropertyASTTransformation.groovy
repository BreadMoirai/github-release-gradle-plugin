/*
 * Copyright (c) 2017 - 2022 BreadMoirai (Ton Ly)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.breadmoirai.githubreleaseplugin.ast

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.ast.tools.GenericsUtils
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.gradle.api.Project
import org.gradle.api.jvm.ModularitySpec
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input

import java.util.concurrent.Callable

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ExtensionPropertyASTTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit _) {
        if (astNodes.length == 1) return
        FieldNode fieldNode = astNodes[1] as FieldNode
        ClassNode classNode = fieldNode.declaringClass
        // assertions to make sure it is applied correctly
        /**
         * Requirements
         * is applied to type: Property
         * has accessible field: Project project
         */
        if (fieldNode.type.typeClass.name != 'org.gradle.api.provider.Property') {
            throw new ExtensionPropertyException("The ExtensionProperty annotation can only be applied to fields of the type ${Property.name}. This annotation has been applied to the field '${fieldNode.name}' of type '${fieldNode.type.typeClass.name}'")
        }
        FieldNode projectField = classNode.getField('project')
        if (projectField == null || projectField.type.typeClass.name != 'org.gradle.api.Project') {
            throw new ExtensionPropertyException("The ExtensionProperty annotation can only be applied to fields with an accompanying field named `project` of the type ${Project.name}")
        }

        String fieldName = fieldNode.name
        String fieldNameCap = fieldName.capitalize()
        GenericsType genericType = fieldNode.type.getGenericsTypes()[0]


        FieldExpression fieldVar = GeneralUtils.fieldX(fieldNode)

        // create getter method
        // getPropertyProvider
        final providerClassNode = createParameterizedNode(Provider, genericType)
        classNode.addMethod new MethodNode(
                "get${fieldNameCap}Provider",
                ACC_PROTECTED,
                providerClassNode,
                [] as Parameter[],
                [] as ClassNode[],
                new ReturnStatement(fieldVar)
        ).tap {
            addAnnotation new AnnotationNode(new ClassNode(Input))
        }

        //create setter methods
        // setProperty(T value)
        final type = genericType.type
        final paramValue = new Parameter(type, fieldName)
        classNode.addMethod(new MethodNode(
                "set${fieldNameCap}",
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                [paramValue] as Parameter[],
                [] as ClassNode[],
                new ExpressionStatement(new MethodCallExpression(new FieldExpression(fieldNode), "set", new VariableExpression(paramValue)))
        ))

        // prop(T value)
        classNode.addMethod(new MethodNode(
                fieldName,
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                [paramValue] as Parameter[],
                [] as ClassNode[],
                new ExpressionStatement(new MethodCallExpression(new FieldExpression(fieldNode), "set", new VariableExpression(paramValue)))
        ))

        // setProp(Provider<? extends T> value)
        final providerWildClassNode = createParameterizedNode(Provider, GenericsUtils.buildWildcardType(type))
        final paramProvider = new Parameter(providerWildClassNode, fieldName)
        classNode.addMethod(new MethodNode(
                "set${fieldNameCap}",
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                [paramProvider] as Parameter[],
                [] as ClassNode[],
                new ExpressionStatement(new MethodCallExpression(new FieldExpression(fieldNode), "set", new VariableExpression(paramProvider)))
        ))

        // prop(Provider<? extends T> value)
        classNode.addMethod(new MethodNode(
                fieldName,
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                [paramProvider] as Parameter[],
                [] as ClassNode[],
                new ExpressionStatement(new MethodCallExpression(new FieldExpression(fieldNode), "set", new VariableExpression(paramProvider)))
        ))

        // setProp(Callable<? extends T> callable)
        final paramCallable = new Parameter(createParameterizedNode(Callable, GenericsUtils.buildWildcardType(type)), fieldName)
        classNode.addMethod(new MethodNode(
                "set${fieldNameCap}",
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                [paramCallable] as Parameter[],
                [] as ClassNode[],
                new ExpressionStatement(
                        new MethodCallExpression(
                                new FieldExpression(fieldNode),
                                "set",
                                new MethodCallExpression(
                                        new FieldExpression(projectField),
                                        "provider",
                                        new VariableExpression(paramCallable)
                                )
                        )
                )
        ))
        // prop(Callable<? extends T> callable)
        classNode.addMethod(new MethodNode(
                fieldName,
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                [paramCallable] as Parameter[],
                [] as ClassNode[],
                new ExpressionStatement(
                        new MethodCallExpression(
                                new FieldExpression(fieldNode),
                                "set",
                                new MethodCallExpression(
                                        new FieldExpression(projectField),
                                        "provider",
                                        new VariableExpression(paramCallable)
                                )
                        )
                )
        ))

        if (type.typeClass == CharSequence) {
            final type1 = new ClassNode(String)
            final paramValue1 = new Parameter(type1, fieldName)
            classNode.addMethod(new MethodNode(
                    "set${fieldNameCap}",
                    ACC_PUBLIC,
                    ClassHelper.VOID_TYPE,
                    [paramValue1] as Parameter[],
                    [] as ClassNode[],
                    new ExpressionStatement(new MethodCallExpression(new FieldExpression(fieldNode), "set", new VariableExpression(paramValue1)))
            ))

            // prop(T value)
            classNode.addMethod(new MethodNode(
                    fieldName,
                    ACC_PUBLIC,
                    ClassHelper.VOID_TYPE,
                    [paramValue1] as Parameter[],
                    [] as ClassNode[],
                    new ExpressionStatement(new MethodCallExpression(new FieldExpression(fieldNode), "set", new VariableExpression(paramValue1)))
            ))
        }
    }

    private static ClassNode createParameterizedNode(Class returnType, GenericsType genericType) {
        GenericsType[] generics = [genericType]
        ClassNode redirect = new ClassNode(returnType)
        redirect.usingGenerics = true
        redirect.genericsTypes = generics
        ClassNode returnNode = new ClassNode(returnType)
        returnNode.setRedirect(redirect)
        returnNode.usingGenerics = true
        returnNode.genericsTypes = generics
        return returnNode

    }
}

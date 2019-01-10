package com.github.breadmoirai.githubreleaseplugin.ast

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.ast.tools.GenericsUtils
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.macro.methods.MacroGroovyMethods
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input

import java.util.concurrent.Callable

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ExtensionPropertyASTTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit _) {
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


        String fieldName = fieldNode.name
        String fieldNameCap = fieldName.capitalize()
        GenericsType genericType = fieldNode.type.getGenericsTypes()[0]


        FieldExpression fieldVar = GeneralUtils.fieldX(fieldNode)

        // create getter method
        // getPropertyProvider
        def providerClassNode = createParameterizedNode(Provider, genericType)
        classNode.addMethod new MethodNode(
                "get${fieldNameCap}Provider",
                ACC_PROTECTED,
                providerClassNode,
                [] as Parameter[],
                [] as ClassNode[],
                new ReturnStatement(fieldVar)).tap {
            it.addAnnotation new AnnotationNode(new ClassNode(Input))
        }

        //create setter methods
        // setProperty(T value)
        def paramValue = new Parameter(genericType.type, fieldName)
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
        def providerWildClassNode = createParameterizedNode(Provider, GenericsUtils.buildWildcardType(genericType.type))
        def paramProvider = new Parameter(providerWildClassNode, fieldName)
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
        def paramCallable = new Parameter(createParameterizedNode(Callable, GenericsUtils.buildWildcardType(genericType.type)), fieldName)
        classNode.addMethod(new MethodNode(
                "set${fieldNameCap}",
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                [paramCallable] as Parameter[],
                [] as ClassNode[],
                new ExpressionStatement(new MethodCallExpression(new FieldExpression(fieldNode), "set", new MethodCallExpression(new ClassExpression(new ClassNode(ProviderFactory)), "provider", new VariableExpression(paramCallable))))
        ))
        // prop(Callable<? extends T> callable)
        classNode.addMethod(new MethodNode(
                fieldName,
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                [paramCallable] as Parameter[],
                [] as ClassNode[],
                new ExpressionStatement(new MethodCallExpression(new FieldExpression(fieldNode), "set", new MethodCallExpression(new ClassExpression(new ClassNode(ProviderFactory)), "provider", new VariableExpression(paramCallable))))
        ))
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

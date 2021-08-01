package com.github.breadmoirai.githubreleaseplugin.ast

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ExtensionClassASTTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        def transformation = new ExtensionPropertyASTTransformation()
        ClassNode node = astNodes[1] as ClassNode
        node.fields.each {
            if (it.type.getPlainNodeReference().name == Property.name) {
                if (!it.annotations.any {
                    it.classNode.getPlainNodeReference().name == Internal.name
                })
                    transformation.visit([null, it] as ASTNode[], null)
            }
        }
    }
}

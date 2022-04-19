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
        if (astNodes.size() == 1) return
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

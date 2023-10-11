package com.github.breadmoirai.githubreleaseplugin

class ASTTest extends GroovyTestCase {

    void testTransformation() {
        assertScript '''
            import com.github.breadmoirai.githubreleaseplugin.ast.ExtensionProperty
            import org.gradle.api.provider.Property
            import org.gradle.api.Project

            class TestClass {
                @ExtensionProperty
                Property<java.lang.String> testP
                
                Project project
                
                public TestClass() {
                    println 'test do it'
                }
            }
            
            for (def method in TestClass.class.declaredMethods) {
                if (method.name.toLowerCase().contains('testp')) {
                    println method
                    println method.parameters
                }
            }
        '''
    }
}

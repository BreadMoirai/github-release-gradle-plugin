///*
// *    Copyright 2017 - 2018 BreadMoirai (Ton Ly)
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the License is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the License for the specific language governing permissions and
// *    limitations under the License.
// */
//
//package com.github.breadmoirai
//
//
//import org.gradle.testkit.runner.GradleRunner
//import org.junit.Rule
//import org.junit.rules.TemporaryFolder
//import spock.lang.Specification
//
//import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
//
//class GithubReleaseFunctionalTest extends Specification {
//
//    @Rule
//    TemporaryFolder testProjectDir = new TemporaryFolder()
//
//    File buildFile
//    List<File> pluginClasspath
//
//    def setup() throws IOException {
//
//        buildFile = testProjectDir.newFile("build.gradle")
//
//        def pluginClasspathResource = this.class.classLoader.getResource("plugin-classpath.txt")
//        if (pluginClasspathResource == null) {
//            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
//        }
//
//        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
//    }
//
//    def "self test"() {
//
//    }
//
//    def "manual test"() {
//        given:
////        buildFile << """
////        plugins {
////            id 'com.github.breadmoirai.github-release'
////        }
////
////        group = 'com.github.breadmoirai'
////        version = 'test'
////
////        githubRelease {
////            repo 'github-release-gradle-plugin'
////            body changelog {}
////        }
////
////        """.stripIndent()
//
//        when:
//        def result = GradleRunner.create()
//                .withProjectDir(new File('C:\\Users\\TonTL\\Desktop\\Git\\BreadBotFramework'))
//                .withArguments('githubRelease', '--info', '--stacktrace')
//                .withPluginClasspath(pluginClasspath)
//                .build()
//
//        then:
//        result.task(":githubRelease").outcome == SUCCESS
//        println "result.output = $result.output"
//    }
//
//    def "manual login test"() {
//
//        when:
//        //new GithubLoginApp().awaitResult()
//
//                /*
//                GridPane grid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new Insets(25, 25, 25, 25));
//
//        Text scenetitle = new Text("Login to Github");
//        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
//        grid.add(scenetitle, 0, 0, 1, 1);
//
//        Label usernameLabel = new Label("User Name:");
//        grid.add(usernameLabel, 0, 1);
//
//        TextField usernameField = new TextField();
//        usernameField.setId("field-username");
//        grid.add(usernameField, 1, 1);
//
//        Label passwordLabel = new Label("Password:");
//        grid.add(passwordLabel, 0, 2);
//
//        PasswordField passwordField = new PasswordField();
//        passwordField.setId("field-password");
//        grid.add(passwordField, 1, 2);
//
//        Button loginButton = new Button("Sign in");
//        loginButton.setId("button-login");
//        loginButton.setDisable(true);
//        loginButton.setDefaultButton(true);
//        Button cancelButton = new Button("Cancel");
//        cancelButton.setId("button-cancel");
//        HBox buttonBox = new HBox(15);
//        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
//        buttonBox.getChildren().addAll(cancelButton, loginButton);
//        grid.add(buttonBox, 0, 4, 2, 1);
//
//
//        usernameField.textProperty().addListener(getValidationListener(passwordField.textProperty(), loginButton));
//        passwordField.textProperty().addListener(getValidationListener(usernameField.textProperty(), loginButton));
//
//        loginButton.setOnAction(e -> {
//            final String username = usernameField.getText();
//            final String password = passwordField.getText();
//            final String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
//            future.complete(Optional.of(credentials));
//            primaryStage.close();
//        });
//
//        cancelButton.setOnAction(e -> {
//            future.complete(Optional.empty());
//            primaryStage.close();
//        });
//
//        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
//            if (KeyCode.ESCAPE == event.getCode()) {
//                future.complete(Optional.empty());
//                primaryStage.close();
//            }
//        });
//
//        Scene scene = new Scene(grid, 300, 275);
//        primaryStage.setScene(scene);
//        primaryStage.show();
//        primaryStage.toFront();
//                 */
//
//
//        then:
//        true
//    }
//
//
//}
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
package com.github.breadmoirai;

import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GithubLoginApp extends Application {

    private static GithubLoginApp app;
    private final CompletableFuture<Optional<String>> future = new CompletableFuture<>();

    public static void start() {
        Application.launch();
    }

    public static GithubLoginApp getApp() {
        return app;
    }

    public GithubLoginApp() {
        app = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Github Login");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("Login to Github");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 1, 1);

        Label usernameLabel = new Label("User Name:");
        grid.add(usernameLabel, 0, 1);

        TextField usernameField = new TextField();
        usernameField.setId("field-username");
        grid.add(usernameField, 1, 1);

        Label passwordLabel = new Label("Password:");
        grid.add(passwordLabel, 0, 2);

        PasswordField passwordField = new PasswordField();
        passwordField.setId("field-password");
        grid.add(passwordField, 1, 2);

        Button loginButton = new Button("Sign in");
        loginButton.setId("button-login");
        loginButton.setDisable(true);
        loginButton.setDefaultButton(true);
        Button cancelButton = new Button("Cancel");
        cancelButton.setId("button-cancel");
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, loginButton);
        grid.add(buttonBox, 0, 4, 2, 1);


        usernameField.textProperty().addListener(getValidationListener(passwordField.textProperty(), loginButton));
        passwordField.textProperty().addListener(getValidationListener(usernameField.textProperty(), loginButton));

        loginButton.setOnAction(e -> {
            final String username = usernameField.getText();
            final String password = passwordField.getText();
            final String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            future.complete(Optional.of(credentials));
            primaryStage.close();
        });

        cancelButton.setOnAction(e -> {
            future.complete(Optional.empty());
            primaryStage.close();
        });

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (KeyCode.ESCAPE == event.getCode()) {
                future.complete(Optional.empty());
                primaryStage.close();
            }
        });

        Scene scene = new Scene(grid, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Optional<String> waitForResult() throws ExecutionException, InterruptedException {
        return future.get();
    }

    CompletableFuture<Optional<String>> getFuture() {
        return future;
    }

    public ChangeListener<? super String> getValidationListener(StringProperty other, Button button) {
        return (observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty() || other.getValueSafe().isEmpty()) {
                button.setDisable(true);
            } else {
                button.setDisable(false);
            }
        };
    }
}

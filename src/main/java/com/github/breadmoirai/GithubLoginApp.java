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
//package com.github.breadmoirai;
//
//import javafx.beans.property.StringProperty;
//import javafx.beans.value.ChangeListener;
//import javafx.embed.swing.JFXPanel;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.PasswordField;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
//import javafx.scene.text.Text;
//
//import javax.swing.*;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//import java.util.Optional;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//
//public class GithubLoginApp {
//
//    private final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
//
//    public Optional<String> awaitResult() throws ExecutionException, InterruptedException {
//        return future.get();
//    }
//
//    public GithubLoginApp() {
//        final JFrame parent = new JFrame();
//        parent.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        final JFXPanel panel = new JFXPanel();
//        parent.setTitle("Github Login");
//        GridPane grid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
//        grid.setHgap(40);
//        grid.setVgap(20);
//        grid.setPadding(new Insets(25, 25, 25, 25));
//
//        Text scenetitle = new Text("Login to Github");
//        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 40));
//        grid.add(scenetitle, 0, 0, 2, 2);
//
//        Label usernameLabel = new Label("User Name:");
//        grid.add(usernameLabel, 0, 2);
//
//        TextField usernameField = new TextField();
//        usernameField.setId("field-username");
//        grid.add(usernameField, 1, 2);
//
//        Label passwordLabel = new Label("Password:");
//        grid.add(passwordLabel, 0, 3);
//
//        PasswordField passwordField = new PasswordField();
//        passwordField.setId("field-password");
//        grid.add(passwordField, 1, 3);
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
//            parent.dispose();
//        });
//
//        cancelButton.setOnAction(e -> {
//            future.complete(Optional.empty());
//            parent.dispose();
//        });
//
//        parent.addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {
//                if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
//                    future.complete(Optional.empty());
//                    parent.dispose();
//                }
//            }
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//
//            }
//
//            @Override
//            public void keyReleased(KeyEvent e) {
//
//            }
//        });
//
//        Scene scene = new Scene(grid);
//        panel.setScene(scene);
//        parent.setContentPane(panel);
//        parent.setLocationRelativeTo(null);
//        parent.setAlwaysOnTop(true);
//        parent.setAutoRequestFocus(true);
//        parent.pack();
//        parent.setVisible(true);
//    }
//
//    private ChangeListener<? super String> getValidationListener(StringProperty other, Button button) {
//        return (observable, oldValue, newValue) -> {
//            if (newValue == null || newValue.isEmpty() || other.getValueSafe().isEmpty()) {
//                button.setDisable(true);
//            } else {
//                button.setDisable(false);
//            }
//        };
//    }
//}

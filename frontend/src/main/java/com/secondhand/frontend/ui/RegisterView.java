package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.RegisterRequest;
import com.secondhand.frontend.service.AuthService;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class RegisterView {

    public static Parent build() {
        Label title = new Label("ثبت‌نام در سامانه");
        title.setStyle("-fx-text-fill: #ec1c24; -fx-font-size: 22px; -fx-font-weight: bold;");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("نام و نام خانوادگی");
        fullNameField.setMaxWidth(300);

        TextField usernameField = new TextField();
        usernameField.setPromptText("نام کاربری");
        usernameField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("رمز عبور");
        passwordField.setMaxWidth(300);

        TextField phoneField = new TextField();
        phoneField.setPromptText("شماره تماس");
        phoneField.setMaxWidth(300);

        Button registerButton = new Button("ثبت‌نام");
        registerButton.setStyle("-fx-background-color: #ec1c24; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        registerButton.setMaxWidth(300);

        Button backButton = new Button("قبلاً حساب دارید؟ ورود");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2255aa;");

        registerButton.setOnAction(e -> {
            if (fullNameField.getText().isBlank() || usernameField.getText().isBlank()
                    || passwordField.getText().isBlank() || phoneField.getText().isBlank()) {
                AlertUtil.showError("لطفاً همه فیلدها را پر کنید.");
                return;
            }

            RegisterRequest req = new RegisterRequest();
            req.fullName = fullNameField.getText().trim();
            req.username = usernameField.getText().trim();
            req.password = passwordField.getText();
            req.phoneNumber = phoneField.getText().trim();

            registerButton.setDisable(true);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    AuthService.register(req);
                    return null;
                }
            };
            task.setOnSucceeded(ev -> {
                registerButton.setDisable(false);
                AlertUtil.showInfo("ثبت‌نام با موفقیت انجام شد. حالا وارد شوید.");
                SceneManager.show(LoginView.build(), "ورود");
            });
            task.setOnFailed(ev -> {
                registerButton.setDisable(false);
                AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
            });
            new Thread(task).start();
        });

        backButton.setOnAction(e -> SceneManager.show(LoginView.build(), "ورود"));

        VBox box = new VBox(12, title, fullNameField, usernameField, passwordField, phoneField, registerButton, backButton);
        box.setStyle("-fx-background-color: #1c1c1e;");
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        return box;
    }
}
package com.secondhand.frontend.ui;

import com.secondhand.frontend.service.AuthService;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginView {

    public static Parent build() {
        Label title = new Label("ورود به سامانه");
        title.setStyle(Theme.APP_TITLE);

        TextField usernameField = new TextField();
        usernameField.setPromptText("نام کاربری");
        usernameField.setMaxWidth(280);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("رمز عبور");
        passwordField.setMaxWidth(280);

        Button loginButton = Theme.primaryButton("ورود");
        loginButton.setMaxWidth(280);
        loginButton.setDefaultButton(true);

        Button goRegisterButton = Theme.linkButton("حساب ندارید؟ ثبت‌نام کنید");

        Button guestButton = Theme.linkButton("ورود به عنوان مهمان (مشاهده آگهی‌ها)");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                AlertUtil.showError("نام کاربری و رمز عبور را وارد کنید.");
                return;
            }

            loginButton.setDisable(true);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    AuthService.login(username, password);
                    return null;
                }
            };
            task.setOnSucceeded(ev -> {
                loginButton.setDisable(false);
                SceneManager.show(AdListView.build(), "لیست آگهی‌ها");
            });
            task.setOnFailed(ev -> {
                loginButton.setDisable(false);
                AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
            });
            new Thread(task).start();
        });

        goRegisterButton.setOnAction(e -> SceneManager.show(RegisterView.build(), "ثبت‌نام"));
        guestButton.setOnAction(e -> SceneManager.show(AdListView.build(), "لیست آگهی‌ها"));

        VBox box = new VBox(14, title, usernameField, passwordField, loginButton, goRegisterButton, guestButton);
        box.setStyle(Theme.BG_DARK);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        return box;
    }
}
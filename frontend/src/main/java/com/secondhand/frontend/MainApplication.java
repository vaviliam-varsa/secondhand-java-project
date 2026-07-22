package com.secondhand.frontend;

import com.secondhand.frontend.ui.LoginView;
import com.secondhand.frontend.ui.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) {
        // 1. تنظیم Stage اصلی در SceneManager (تا بتوانیم بین صفحات جابه‌جا شویم)
        SceneManager.init(stage);

        // 2. اولین صفحه‌ای که به کاربر نشان داده می‌شود، صفحه‌ی ورود است
        SceneManager.show(LoginView.build(), "ورود به سامانه");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
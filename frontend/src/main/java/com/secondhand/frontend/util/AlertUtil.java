package com.secondhand.frontend.util;

import com.secondhand.frontend.http.ApiException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertUtil {

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("خطا");
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static String extractMessage(Throwable ex) {
        if (ex instanceof ApiException apiEx) {
            return apiEx.getMessage();
        }
        if (ex != null && ex.getCause() instanceof ApiException apiEx) {
            return apiEx.getMessage();
        }
        return "خطای غیرمنتظره: " + (ex != null ? ex.getMessage() : "");
    }
}
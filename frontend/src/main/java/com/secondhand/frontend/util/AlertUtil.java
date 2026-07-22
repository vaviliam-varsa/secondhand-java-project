package com.secondhand.frontend.util;

import com.secondhand.frontend.http.ApiException;
import javafx.scene.control.Alert;

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

    public static String extractMessage(Throwable ex) {
        if (ex instanceof ApiException apiEx) {
            return apiEx.getMessage();
        }
        if (ex.getCause() instanceof ApiException apiEx) {
            return apiEx.getMessage();
        }
        return "خطای غیرمنتظره: " + ex.getMessage();
    }
}
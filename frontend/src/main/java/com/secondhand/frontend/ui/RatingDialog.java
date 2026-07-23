package com.secondhand.frontend.ui;

import com.secondhand.frontend.service.RatingService;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class RatingDialog {

    public static void showAndSubmit(long advertisementId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("امتیازدهی به فروشنده");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle(Theme.BG_DARK);

        ComboBox<Integer> scoreBox = new ComboBox<>();
        scoreBox.getItems().addAll(1, 2, 3, 4, 5);
        scoreBox.setPromptText("امتیاز (۱ تا ۵)");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("نظر شما (اختیاری)");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);

        Label scoreLabel = new Label("امتیاز:");
        scoreLabel.setStyle(Theme.TEXT_LIGHT);
        Label commentLabel = new Label("نظر:");
        commentLabel.setStyle(Theme.TEXT_LIGHT);

        VBox box = new VBox(10, scoreLabel, scoreBox, commentLabel, commentArea);
        box.setStyle(Theme.BG_DARK);
        box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (scoreBox.getValue() == null) {
                AlertUtil.showError("لطفاً یک امتیاز بین ۱ تا ۵ انتخاب کنید.");
                return;
            }
            int score = scoreBox.getValue();
            String comment = commentArea.getText().trim();

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    RatingService.submit(advertisementId, score, comment);
                    return null;
                }
            };
            task.setOnSucceeded(e -> AlertUtil.showInfo("امتیاز شما ثبت شد."));
            task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
            new Thread(task).start();
        }
    }
}
package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.RatingComment;
import com.secondhand.frontend.model.RatingCommentsPage;
import com.secondhand.frontend.service.RatingService;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class RatingCommentsDialog {

    public static void show(long sellerId, String sellerName) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("نظرات درباره‌ی " + sellerName);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle(Theme.BG_DARK);

        VBox container = new VBox(10);
        container.setStyle(Theme.BG_DARK);
        container.setPadding(new Insets(10));

        Label loadingLabel = new Label("در حال بارگذاری نظرات...");
        loadingLabel.setStyle(Theme.TEXT_LIGHT);
        container.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(420, 400);
        scrollPane.setStyle(Theme.BG_DARK);

        dialog.getDialogPane().setContent(scrollPane);

        loadComments(sellerId, container, loadingLabel);

        dialog.showAndWait();
    }

    private static void loadComments(long sellerId, VBox container, Label loadingLabel) {
        Task<RatingCommentsPage> task = new Task<>() {
            @Override
            protected RatingCommentsPage call() throws Exception {
                return RatingService.getSellerComments(sellerId, null);
            }
        };
        task.setOnSucceeded(e -> {
            container.getChildren().remove(loadingLabel);
            RatingCommentsPage page = task.getValue();
            if (page.comments == null || page.comments.isEmpty()) {
                Label empty = new Label("هنوز نظری برای این فروشنده ثبت نشده است.");
                empty.setStyle(Theme.TEXT_MUTED);
                container.getChildren().add(empty);
            } else {
                for (RatingComment c : page.comments) {
                    container.getChildren().add(buildCommentRow(c));
                }
            }
        });
        task.setOnFailed(e -> {
            container.getChildren().remove(loadingLabel);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });
        new Thread(task).start();
    }

    private static VBox buildCommentRow(RatingComment c) {
        VBox row = new VBox(4);
        row.setStyle(Theme.CARD_BG + "-fx-padding: 10;");

        Label header = new Label(c.raterName + "   ★ " + c.score + "/5");
        header.setStyle(Theme.CARD_TITLE);

        Label commentText = new Label(c.comment);
        commentText.setStyle(Theme.TEXT_LIGHT);
        commentText.setWrapText(true);

        Label date = new Label(c.createdAt != null ? c.createdAt : "");
        date.setStyle(Theme.TEXT_MUTED);

        row.getChildren().addAll(header, commentText, date);
        return row;
    }
}
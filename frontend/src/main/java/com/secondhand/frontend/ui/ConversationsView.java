package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ConversationsView {

    public static Parent build() {
        VBox root = new VBox(12);
        root.setStyle(Theme.BG_DARK);
        root.setPadding(new Insets(20));

        Button backButton = Theme.secondaryButton("بازگشت به لیست آگهی‌ها");
        backButton.setOnAction(e -> SceneManager.show(AdListView.build(), "لیست آگهی‌ها"));

        Label title = new Label("گفت‌وگوهای من");
        title.setStyle(Theme.SECTION_TITLE);

        VBox itemsBox = new VBox(8);
        Label loadingLabel = new Label("در حال بارگذاری...");
        loadingLabel.setStyle(Theme.TEXT_LIGHT);
        itemsBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(Theme.BG_DARK);

        root.getChildren().addAll(backButton, title, scrollPane);

        loadConversations(itemsBox, loadingLabel);

        return root;
    }

    private static void loadConversations(VBox itemsBox, Label loadingLabel) {
        Task<List<Conversation>> task = new Task<>() {
            @Override
            protected List<Conversation> call() throws Exception {
                return ChatService.listConversations();
            }
        };
        task.setOnSucceeded(e -> {
            itemsBox.getChildren().remove(loadingLabel);
            List<Conversation> conversations = task.getValue();
            if (conversations.isEmpty()) {
                Label empty = new Label("هنوز هیچ گفت‌وگویی ندارید.");
                empty.setStyle(Theme.TEXT_MUTED);
                itemsBox.getChildren().add(empty);
            } else {
                for (Conversation c : conversations) {
                    itemsBox.getChildren().add(buildRow(c));
                }
            }
        });
        task.setOnFailed(e -> {
            itemsBox.getChildren().remove(loadingLabel);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });
        new Thread(task).start();
    }

    private static VBox buildRow(Conversation c) {
        VBox row = new VBox(4);
        row.setStyle(Theme.CARD_BG + "-fx-padding: 12; -fx-cursor: hand;");

        String adTitle = c.advertisement != null ? c.advertisement.title : "-";
        String otherName = c.otherUser != null ? c.otherUser.fullName : "-";
        String last = c.lastMessage != null ? c.lastMessage : "(پیامی ثبت نشده)";

        Label header = new Label(adTitle + "  —  با: " + otherName);
        header.setStyle(Theme.CARD_TITLE);

        Label lastMsg = new Label(last);
        lastMsg.setStyle(Theme.TEXT_MUTED);

        row.getChildren().addAll(header, lastMsg);
        row.setOnMouseClicked(e -> SceneManager.show(ChatView.build(c.id, otherName), "گفت‌وگو با " + otherName));

        return row;
    }
}
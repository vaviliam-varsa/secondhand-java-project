package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.ChatMessage;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.session.SessionManager;
import com.secondhand.frontend.util.AlertUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChatView {

    public static Parent build(long conversationId, String otherUserName) {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.BG_DARK);
        root.setPadding(new Insets(16));

        Button backButton = Theme.secondaryButton("بازگشت به گفت‌وگوها");
        backButton.setOnAction(e -> SceneManager.show(ConversationsView.build(), "گفت‌وگوهای من"));

        Label title = new Label("گفت‌وگو با " + otherUserName);
        title.setStyle(Theme.SECTION_TITLE);

        VBox top = new VBox(10, backButton, title);
        top.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(top);

        VBox messagesBox = new VBox(8);
        messagesBox.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(Theme.BG_DARK);
        root.setCenter(scrollPane);

        TextField messageField = new TextField();
        messageField.setPromptText("پیام خود را بنویسید...");
        HBox.setHgrow(messageField, Priority.ALWAYS);

        Button sendButton = Theme.primaryButton("ارسال");
        sendButton.setDefaultButton(true);

        HBox sendBar = new HBox(8, messageField, sendButton);
        sendBar.setPadding(new Insets(12, 0, 0, 0));
        root.setBottom(sendBar);

        Runnable reload = () -> loadMessages(messagesBox, scrollPane, conversationId, otherUserName);

        sendButton.setOnAction(e -> {
            String content = messageField.getText().trim();
            if (content.isEmpty()) return;

            sendButton.setDisable(true);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ChatService.sendMessage(conversationId, content);
                    return null;
                }
            };
            task.setOnSucceeded(ev -> {
                sendButton.setDisable(false);
                messageField.clear();
                reload.run();
            });
            task.setOnFailed(ev -> {
                sendButton.setDisable(false);
                AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
            });
            new Thread(task).start();
        });

        reload.run();

        return root;
    }

    private static void loadMessages(VBox messagesBox, ScrollPane scrollPane, long conversationId, String otherUserName) {
        Task<List<ChatMessage>> task = new Task<>() {
            @Override
            protected List<ChatMessage> call() throws Exception {
                return ChatService.listMessages(conversationId);
            }
        };
        task.setOnSucceeded(e -> {
            messagesBox.getChildren().clear();
            for (ChatMessage m : task.getValue()) {
                messagesBox.getChildren().add(buildBubbleRow(m, otherUserName));
            }
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        });
        task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
        new Thread(task).start();
    }

    private static HBox buildBubbleRow(ChatMessage m, String otherUserName) {
        boolean isMine = m.senderId != null && m.senderId.equals(SessionManager.getInstance().getUserId());

        VBox bubble = new VBox(4);
        bubble.setMaxWidth(360);
        bubble.setStyle(isMine
                ? "-fx-background-color: #ec1c24; -fx-background-radius: 12; -fx-padding: 10 14 10 14;"
                : "-fx-background-color: #2e2e30; -fx-background-radius: 12; -fx-padding: 10 14 10 14;");

        Label senderLabel = new Label(isMine ? "من" : otherUserName);
        senderLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: "
                + (isMine ? "rgba(255,255,255,0.85);" : "#bbbbbb;"));

        Label contentLabel = new Label(m.content);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: white;");

        Label timeLabel = new Label(m.sentAt != null ? m.sentAt : "");
        timeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: "
                + (isMine ? "rgba(255,255,255,0.7);" : "#999999;"));

        bubble.getChildren().addAll(senderLabel, contentLabel, timeLabel);

        HBox row = new HBox(bubble);
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return row;
    }
}
package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.ChatMessage;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.session.SessionManager;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChatView {

    public static Parent build(long conversationId, String otherUserName) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        Button backButton = new Button("بازگشت به گفت‌وگوها");
        backButton.setOnAction(e -> SceneManager.show(ConversationsView.build(), "گفت‌وگوهای من"));

        Label title = new Label("گفت‌وگو با " + otherUserName);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox top = new VBox(8, backButton, title);
        root.setTop(top);

        ListView<ChatMessage> messagesView = new ListView<>();
        messagesView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessage m, boolean empty) {
                super.updateItem(m, empty);
                if (empty || m == null) {
                    setText(null);
                } else {
                    boolean isMine = m.senderId != null
                            && m.senderId.equals(SessionManager.getInstance().getUserId());
                    String who = isMine ? "من" : otherUserName;
                    setText("[" + m.sentAt + "] " + who + ": " + m.content);
                }
            }
        });
        root.setCenter(messagesView);

        TextField messageField = new TextField();
        messageField.setPromptText("پیام خود را بنویسید...");
        HBox.setHgrow(messageField, Priority.ALWAYS);

        Button sendButton = new Button("ارسال");

        HBox sendBar = new HBox(8, messageField, sendButton);
        sendBar.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(sendBar);

        Runnable reload = () -> loadMessages(messagesView, conversationId);

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

    private static void loadMessages(ListView<ChatMessage> messagesView, long conversationId) {
        Task<List<ChatMessage>> task = new Task<>() {
            @Override
            protected List<ChatMessage> call() throws Exception {
                return ChatService.listMessages(conversationId);
            }
        };
        task.setOnSucceeded(e -> messagesView.getItems().setAll(task.getValue()));
        task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
        new Thread(task).start();
    }
}
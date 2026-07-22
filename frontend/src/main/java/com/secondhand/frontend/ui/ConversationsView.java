package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.List;

public class ConversationsView {

    public static Parent build() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Button backButton = new Button("بازگشت به لیست آگهی‌ها");
        backButton.setOnAction(e -> SceneManager.show(AdListView.build(), "لیست آگهی‌ها"));

        Label title = new Label("گفت‌وگوهای من");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        ListView<Conversation> listView = new ListView<>();
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                } else {
                    String adTitle = c.advertisement != null ? c.advertisement.title : "-";
                    String otherName = c.otherUser != null ? c.otherUser.fullName : "-";
                    String last = c.lastMessage != null ? c.lastMessage : "(پیامی ثبت نشده)";
                    setText(adTitle + "  —  با: " + otherName + "\n" + last);
                }
            }
        });

        listView.setOnMouseClicked(e -> {
            Conversation selected = listView.getSelectionModel().getSelectedItem();
            if (e.getClickCount() == 2 && selected != null) {
                String otherName = selected.otherUser != null ? selected.otherUser.fullName : "کاربر";
                SceneManager.show(ChatView.build(selected.id, otherName), "گفت‌وگو با " + otherName);
            }
        });

        root.getChildren().addAll(backButton, title, listView);

        loadConversations(listView);

        return root;
    }

    private static void loadConversations(ListView<Conversation> listView) {
        Task<List<Conversation>> task = new Task<>() {
            @Override
            protected List<Conversation> call() throws Exception {
                return ChatService.listConversations();
            }
        };
        task.setOnSucceeded(e -> listView.getItems().setAll(task.getValue()));
        task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
        new Thread(task).start();
    }
}
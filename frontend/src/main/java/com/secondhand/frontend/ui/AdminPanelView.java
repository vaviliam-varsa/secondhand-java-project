package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.AdminPendingAd;
import com.secondhand.frontend.model.AdminUser;
import com.secondhand.frontend.service.AdminService;
import com.secondhand.frontend.service.AuthService;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class AdminPanelView {

    public static Parent build() {
        VBox root = new VBox(12);
        root.setStyle(Theme.BG_DARK);
        root.setPadding(new Insets(20));

        Button logoutButton = Theme.secondaryButton("خروج از حساب");
        logoutButton.setOnAction(e -> {
            AuthService.logout();
            SceneManager.show(LoginView.build(), "ورود");
        });

        Label title = new Label("پنل مدیریت");
        title.setStyle(Theme.SECTION_TITLE);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle(Theme.BG_DARK);

        Tab pendingTab = new Tab("آگهی‌های در انتظار بررسی");
        pendingTab.setContent(buildPendingAdsPane());

        Tab usersTab = new Tab("مدیریت کاربران");
        usersTab.setContent(buildUsersPane());

        tabPane.getTabs().addAll(pendingTab, usersTab);

        root.getChildren().addAll(logoutButton, title, tabPane);
        return root;
    }

    private static VBox buildPendingAdsPane() {
        VBox box = new VBox(8);
        box.setStyle(Theme.BG_DARK);
        box.setPadding(new Insets(10));

        VBox itemsBox = new VBox(8);
        Label loadingLabel = new Label("در حال بارگذاری...");
        loadingLabel.setStyle(Theme.TEXT_LIGHT);
        itemsBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(Theme.BG_DARK);

        box.getChildren().add(scrollPane);

        loadPendingAds(itemsBox, loadingLabel);

        return box;
    }

    private static void loadPendingAds(VBox itemsBox, Label loadingLabel) {
        Task<List<AdminPendingAd>> task = new Task<>() {
            @Override
            protected List<AdminPendingAd> call() throws Exception {
                return AdminService.pendingAds();
            }
        };
        task.setOnSucceeded(e -> {
            itemsBox.getChildren().remove(loadingLabel);
            List<AdminPendingAd> ads = task.getValue();
            if (ads.isEmpty()) {
                Label empty = new Label("آگهی‌ای در انتظار بررسی نیست.");
                empty.setStyle(Theme.TEXT_MUTED);
                itemsBox.getChildren().add(empty);
            } else {
                for (AdminPendingAd ad : ads) {
                    itemsBox.getChildren().add(buildPendingAdRow(ad, itemsBox));
                }
            }
        });
        task.setOnFailed(e -> {
            itemsBox.getChildren().remove(loadingLabel);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });
        new Thread(task).start();
    }

    private static HBox buildPendingAdRow(AdminPendingAd ad, VBox itemsBox) {
        String ownerName = ad.owner != null ? ad.owner.fullName : "-";
        Label label = new Label(ad.title + "   |   فروشنده: " + ownerName);
        label.setStyle(Theme.TEXT_LIGHT);
        label.setMaxWidth(500);

        Button reviewButton = Theme.primaryButton("مشاهده و بررسی");
        reviewButton.setOnAction(e -> SceneManager.show(AdminAdDetailView.build(ad.id), "بررسی آگهی"));

        return new HBox(10, label, reviewButton);
    }

    private static VBox buildUsersPane() {
        VBox box = new VBox(8);
        box.setStyle(Theme.BG_DARK);
        box.setPadding(new Insets(10));

        VBox itemsBox = new VBox(8);
        Label loadingLabel = new Label("در حال بارگذاری...");
        loadingLabel.setStyle(Theme.TEXT_LIGHT);
        itemsBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(Theme.BG_DARK);

        box.getChildren().add(scrollPane);

        loadUsers(itemsBox, loadingLabel);

        return box;
    }

    private static void loadUsers(VBox itemsBox, Label loadingLabel) {
        Task<List<AdminUser>> task = new Task<>() {
            @Override
            protected List<AdminUser> call() throws Exception {
                return AdminService.users();
            }
        };
        task.setOnSucceeded(e -> {
            itemsBox.getChildren().remove(loadingLabel);
            List<AdminUser> users = task.getValue();
            if (users.isEmpty()) {
                Label empty = new Label("کاربری یافت نشد.");
                empty.setStyle(Theme.TEXT_MUTED);
                itemsBox.getChildren().add(empty);
            } else {
                for (AdminUser u : users) {
                    itemsBox.getChildren().add(buildUserRow(u));
                }
            }
        });
        task.setOnFailed(e -> {
            itemsBox.getChildren().remove(loadingLabel);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });
        new Thread(task).start();
    }

    private static HBox buildUserRow(AdminUser u) {
        Label label = new Label(u.fullName + " (@" + u.username + ")   |   وضعیت: " + u.status);
        label.setStyle(Theme.TEXT_LIGHT);
        label.setMaxWidth(500);

        Button blockButton = Theme.secondaryButton("مسدود کردن");
        blockButton.setDisable("BLOCKED".equals(u.status));

        Button unblockButton = Theme.secondaryButton("رفع مسدودی");
        unblockButton.setDisable(!"BLOCKED".equals(u.status));

        HBox row = new HBox(10, label, blockButton, unblockButton);
        row.setStyle(Theme.CARD_BG + "-fx-padding: 10;");

        blockButton.setOnAction(e -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    AdminService.block(u.id);
                    return null;
                }
            };
            task.setOnSucceeded(ev -> {
                u.status = "BLOCKED";
                label.setText(u.fullName + " (@" + u.username + ")   |   وضعیت: " + u.status);
                blockButton.setDisable(true);
                unblockButton.setDisable(false);
            });
            task.setOnFailed(ev -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
            new Thread(task).start();
        });

        unblockButton.setOnAction(e -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    AdminService.unblock(u.id);
                    return null;
                }
            };
            task.setOnSucceeded(ev -> {
                u.status = "ACTIVE";
                label.setText(u.fullName + " (@" + u.username + ")   |   وضعیت: " + u.status);
                blockButton.setDisable(false);
                unblockButton.setDisable(true);
            });
            task.setOnFailed(ev -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
            new Thread(task).start();
        });

        return row;
    }
}
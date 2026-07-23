package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.AdminPendingAd;
import com.secondhand.frontend.model.AdminUser;
import com.secondhand.frontend.model.Category;
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

        Tab categoriesTab = new Tab("مدیریت دسته‌بندی‌ها");
        categoriesTab.setContent(buildCategoriesPane());

        tabPane.getTabs().addAll(pendingTab, usersTab, categoriesTab);

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

    // ---------- مدیریت دسته‌بندی‌ها ----------

    private static VBox buildCategoriesPane() {
        VBox box = new VBox(12);
        box.setStyle(Theme.BG_DARK);
        box.setPadding(new Insets(10));

        Label formLabel = new Label("افزودن دسته‌بندی جدید:");
        formLabel.setStyle(Theme.TEXT_LIGHT);

        TextField nameField = new TextField();
        nameField.setPromptText("نام دسته‌بندی");
        nameField.setPrefWidth(250);

        Button addButton = Theme.primaryButton("افزودن");

        HBox formRow = new HBox(10, nameField, addButton);

        VBox itemsBox = new VBox(8);
        Label loadingLabel = new Label("در حال بارگذاری...");
        loadingLabel.setStyle(Theme.TEXT_LIGHT);
        itemsBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(Theme.BG_DARK);

        addButton.setOnAction(e -> {
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            if (name.isEmpty()) {
                AlertUtil.showError("نام دسته‌بندی نمی‌تواند خالی باشد.");
                return;
            }
            addButton.setDisable(true);
            Task<Category> task = new Task<>() {
                @Override
                protected Category call() throws Exception {
                    return AdminService.createCategory(name);
                }
            };
            task.setOnSucceeded(ev -> {
                addButton.setDisable(false);
                Category created = task.getValue();
                itemsBox.getChildren().add(buildCategoryRow(created, itemsBox));
                nameField.clear();
            });
            task.setOnFailed(ev -> {
                addButton.setDisable(false);
                AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
            });
            new Thread(task).start();
        });

        box.getChildren().addAll(formLabel, formRow, scrollPane);

        loadCategories(itemsBox, loadingLabel);

        return box;
    }

    private static void loadCategories(VBox itemsBox, Label loadingLabel) {
        Task<List<Category>> task = new Task<>() {
            @Override
            protected List<Category> call() throws Exception {
                return AdminService.categories();
            }
        };
        task.setOnSucceeded(e -> {
            itemsBox.getChildren().remove(loadingLabel);
            List<Category> categories = task.getValue();
            if (categories.isEmpty()) {
                Label empty = new Label("دسته‌بندی‌ای ثبت نشده است.");
                empty.setStyle(Theme.TEXT_MUTED);
                itemsBox.getChildren().add(empty);
            } else {
                for (Category c : categories) {
                    itemsBox.getChildren().add(buildCategoryRow(c, itemsBox));
                }
            }
        });
        task.setOnFailed(e -> {
            itemsBox.getChildren().remove(loadingLabel);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });
        new Thread(task).start();
    }

    private static HBox buildCategoryRow(Category category, VBox itemsBox) {
        Label label = new Label(category.name);
        label.setStyle(Theme.TEXT_LIGHT);
        label.setMaxWidth(400);
        label.setMinWidth(200);

        Button editButton = Theme.secondaryButton("ویرایش");
        Button deleteButton = Theme.secondaryButton("حذف");

        HBox row = new HBox(10, label, editButton, deleteButton);
        row.setStyle(Theme.CARD_BG + "-fx-padding: 10;");

        editButton.setOnAction(e -> showEditCategoryDialog(category, label));

        deleteButton.setOnAction(e -> {
            boolean confirmed = AlertUtil.confirm("آیا از حذف دسته‌بندی «" + category.name + "» مطمئن هستید؟");
            if (!confirmed) return;

            deleteButton.setDisable(true);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    AdminService.deleteCategory(category.id);
                    return null;
                }
            };
            task.setOnSucceeded(ev -> itemsBox.getChildren().remove(row));
            task.setOnFailed(ev -> {
                deleteButton.setDisable(false);
                AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
            });
            new Thread(task).start();
        });

        return row;
    }

    private static void showEditCategoryDialog(Category category, Label label) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("ویرایش دسته‌بندی");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle(Theme.BG_DARK);

        Label nameLabel = new Label("نام دسته‌بندی:");
        nameLabel.setStyle(Theme.TEXT_LIGHT);

        TextField nameField = new TextField(category.name);

        VBox content = new VBox(10, nameLabel, nameField);
        content.setStyle(Theme.BG_DARK);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newName = nameField.getText() == null ? "" : nameField.getText().trim();
            if (newName.isEmpty()) {
                AlertUtil.showError("نام دسته‌بندی نمی‌تواند خالی باشد.");
                return;
            }

            Task<Category> task = new Task<>() {
                @Override
                protected Category call() throws Exception {
                    return AdminService.updateCategory(category.id, newName);
                }
            };
            task.setOnSucceeded(e -> {
                Category updated = task.getValue();
                category.name = updated.name;
                label.setText(category.name);
            });
            task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
            new Thread(task).start();
        }
    }
}
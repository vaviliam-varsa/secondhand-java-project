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
import com.secondhand.frontend.model.Category;

import java.util.List;
import java.util.Optional;

public class AdminPanelView {

    public static Parent build() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Button logoutButton = new Button("خروج از حساب");
        logoutButton.setOnAction(e -> {
            AuthService.logout();
            SceneManager.show(LoginView.build(), "ورود");
        });

        Label title = new Label("پنل مدیریت");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

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
        box.setPadding(new Insets(10));

        VBox itemsBox = new VBox(8);
        Label loadingLabel = new Label("در حال بارگذاری...");
        itemsBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);

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
                itemsBox.getChildren().add(new Label("آگهی‌ای در انتظار بررسی نیست."));
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
        label.setMaxWidth(500);

        Button approveButton = new Button("تایید");
        Button rejectButton = new Button("رد کردن");

        HBox row = new HBox(10, label, approveButton, rejectButton);

        approveButton.setOnAction(e -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    AdminService.approve(ad.id);
                    return null;
                }
            };
            task.setOnSucceeded(ev -> {
                AlertUtil.showInfo("آگهی تایید شد.");
                itemsBox.getChildren().remove(row);
            });
            task.setOnFailed(ev -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
            new Thread(task).start();
        });

        rejectButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("رد آگهی");
            dialog.setHeaderText(null);
            dialog.setContentText("دلیل رد آگهی:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(reason -> {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        AdminService.reject(ad.id, reason);
                        return null;
                    }
                };
                task.setOnSucceeded(ev -> {
                    AlertUtil.showInfo("آگهی رد شد.");
                    itemsBox.getChildren().remove(row);
                });
                task.setOnFailed(ev -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
                new Thread(task).start();
            });
        });

        return row;
    }

    private static VBox buildUsersPane() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));

        VBox itemsBox = new VBox(8);
        Label loadingLabel = new Label("در حال بارگذاری...");
        itemsBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);

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
                itemsBox.getChildren().add(new Label("کاربری یافت نشد."));
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
        label.setMaxWidth(500);

        Button blockButton = new Button("مسدود کردن");
        blockButton.setDisable("BLOCKED".equals(u.status));

        Button unblockButton = new Button("رفع مسدودی");
        unblockButton.setDisable(!"BLOCKED".equals(u.status));

        HBox row = new HBox(10, label, blockButton, unblockButton);

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

    private static VBox buildCategoriesPane() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));

        Button addButton = new Button("افزودن دسته‌بندی جدید");

        VBox itemsBox = new VBox(8);
        Label loadingLabel = new Label("در حال بارگذاری...");
        itemsBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);

        addButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("دسته‌بندی جدید");
            dialog.setHeaderText(null);
            dialog.setContentText("نام دسته‌بندی:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                Task<com.secondhand.frontend.model.Category> task = new Task<>() {
                    @Override
                    protected com.secondhand.frontend.model.Category call() throws Exception {
                        return AdminService.createCategory(name);
                    }
                };
                task.setOnSucceeded(ev -> {
                    AlertUtil.showInfo("دسته‌بندی با موفقیت اضافه شد.");
                    loadCategories(itemsBox, null);
                });
                task.setOnFailed(ev -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
                new Thread(task).start();
            });
        });

        box.getChildren().addAll(addButton, scrollPane);

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
            itemsBox.getChildren().clear();
            List<Category> categories = task.getValue();
            if (categories.isEmpty()) {
                itemsBox.getChildren().add(new Label("دسته‌بندی‌ای ثبت نشده است."));
            } else {
                for (Category c : categories) {
                    itemsBox.getChildren().add(buildCategoryRow(c, itemsBox));
                }
            }
        });
        task.setOnFailed(e -> {
            itemsBox.getChildren().clear();
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });
        new Thread(task).start();
    }

    private static HBox buildCategoryRow(Category category, VBox itemsBox) {
        Label label = new Label(category.name);
        label.setMaxWidth(400);

        Button editButton = new Button("ویرایش");
        Button deleteButton = new Button("حذف");

        HBox row = new HBox(10, label, editButton, deleteButton);

        editButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(category.name);
            dialog.setTitle("ویرایش دسته‌بندی");
            dialog.setHeaderText(null);
            dialog.setContentText("نام جدید:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                Task<Category> task = new Task<>() {
                    @Override
                    protected Category call() throws Exception {
                        return AdminService.updateCategory(category.id, newName);
                    }
                };
                task.setOnSucceeded(ev -> {
                    category.name = task.getValue().name;
                    label.setText(category.name);
                    AlertUtil.showInfo("دسته‌بندی ویرایش شد.");
                });
                task.setOnFailed(ev -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
                new Thread(task).start();
            });
        });

        deleteButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "آیا از حذف دسته‌بندی «" + category.name + "» مطمئن هستید؟");
            confirm.setHeaderText(null);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        AdminService.deleteCategory(category.id);
                        return null;
                    }
                };
                task.setOnSucceeded(ev -> {
                    AlertUtil.showInfo("دسته‌بندی حذف شد.");
                    itemsBox.getChildren().remove(row);
                });
                task.setOnFailed(ev -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
                new Thread(task).start();
            }
        });

        return row;
    }
}
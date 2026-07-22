package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.AdvertisementDetail;
import com.secondhand.frontend.service.AdvertisementService;
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.session.SessionManager;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdDetailView {

    public static Parent build(long adId) {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Button backButton = new Button("بازگشت به لیست");
        backButton.setOnAction(e -> SceneManager.show(AdListView.build(), "لیست آگهی‌ها"));

        Label loadingLabel = new Label("در حال بارگذاری جزئیات آگهی...");
        root.getChildren().addAll(backButton, loadingLabel);

        loadDetail(root, loadingLabel, adId);

        return root;
    }

    private static void loadDetail(VBox root, Label loadingLabel, long adId) {
        Task<AdvertisementDetail> task = new Task<>() {
            @Override
            protected AdvertisementDetail call() throws Exception {
                return AdvertisementService.getDetail(adId);
            }
        };

        task.setOnSucceeded(e -> {
            AdvertisementDetail ad = task.getValue();
            root.getChildren().remove(loadingLabel);
            renderDetail(root, ad);
        });

        task.setOnFailed(e -> {
            root.getChildren().remove(loadingLabel);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });

        new Thread(task).start();
    }

    private static void renderDetail(VBox root, AdvertisementDetail ad) {
        Label title = new Label(ad.title);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label price = new Label(ad.price != null ? String.format("%,d تومان", ad.price) : "-");
        price.setStyle("-fx-font-size: 16px; -fx-text-fill: #2a7d2a;");

        Label meta = new Label("شهر: " + ad.city + "   |   دسته‌بندی: " + ad.category + "   |   وضعیت: " + ad.status);

        Label descTitle = new Label("توضیحات:");
        descTitle.setStyle("-fx-font-weight: bold;");
        Label desc = new Label(ad.description != null ? ad.description : "-");
        desc.setWrapText(true);

        String ownerName = ad.owner != null ? ad.owner.fullName : "-";
        Label owner = new Label("فروشنده: " + ownerName);

        int imageCount = ad.images != null ? ad.images.size() : 0;
        Label images = new Label("تعداد تصاویر: " + imageCount + " (نمایش گالری در فاز بعدی اضافه می‌شود)");

        root.getChildren().addAll(title, price, meta, descTitle, desc, owner, images);

        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        boolean isOwner = loggedIn && ad.owner != null && ad.owner.id != null
                && ad.owner.id.equals(SessionManager.getInstance().getUserId());

        HBox actions = new HBox(10);

        if (isOwner) {
            Button editButton = new Button("ویرایش آگهی");
            editButton.setOnAction(e -> SceneManager.show(AdFormView.buildEdit(ad.id), "ویرایش آگهی"));

            Button deleteButton = new Button("حذف آگهی");
            deleteButton.setOnAction(e -> handleDelete(ad.id));

            Button soldButton = new Button("علامت‌گذاری به‌عنوان فروخته‌شده");
            soldButton.setDisable("SOLD".equals(ad.status));
            soldButton.setOnAction(e -> handleMarkSold(ad.id));

            actions.getChildren().addAll(editButton, deleteButton, soldButton);
        } else if (loggedIn) {
            Button favoriteButton = new Button("افزودن به علاقه‌مندی‌ها");
            favoriteButton.setOnAction(e -> handleAddFavorite(ad.id, favoriteButton));

            Button chatButton = new Button("گفت‌وگو با فروشنده (به‌زودی)");
            chatButton.setDisable(true);

            Button ratingButton = new Button("امتیازدهی به فروشنده (به‌زودی)");
            ratingButton.setDisable(true);

            actions.getChildren().addAll(favoriteButton, chatButton, ratingButton);
        } else {
            Label guestHint = new Label("برای افزودن به علاقه‌مندی‌ها یا گفت‌وگو با فروشنده، ابتدا وارد شوید.");
            guestHint.setStyle("-fx-text-fill: #888;");
            actions.getChildren().add(guestHint);
        }

        root.getChildren().add(actions);
    }

    private static void handleDelete(Long adId) {
        boolean confirmed = AlertUtil.confirm("از حذف این آگهی مطمئن هستید؟ این عملیات قابل بازگشت نیست.");
        if (!confirmed) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                AdvertisementService.delete(adId);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            AlertUtil.showInfo("آگهی با موفقیت حذف شد.");
            SceneManager.show(AdListView.build(), "لیست آگهی‌ها");
        });
        task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
        new Thread(task).start();
    }

    private static void handleMarkSold(Long adId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                AdvertisementService.markSold(adId);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            AlertUtil.showInfo("وضعیت آگهی به فروخته‌شده تغییر کرد.");
            SceneManager.show(AdDetailView.build(adId), "جزئیات آگهی");
        });
        task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
        new Thread(task).start();
    }

    private static void handleAddFavorite(Long adId, Button favoriteButton) {
        favoriteButton.setDisable(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                FavoriteService.add(adId);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            AlertUtil.showInfo("آگهی به علاقه‌مندی‌ها اضافه شد.");
        });
        task.setOnFailed(e -> {
            favoriteButton.setDisable(false);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });
        new Thread(task).start();
    }
}
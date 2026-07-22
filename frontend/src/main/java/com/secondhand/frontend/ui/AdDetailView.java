package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.AdvertisementDetail;
import com.secondhand.frontend.model.SellerRatingsSummary;
import com.secondhand.frontend.service.AdvertisementService;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.service.RatingService;
import com.secondhand.frontend.session.SessionManager;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;

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

        Label ratingLabel = new Label("امتیاز فروشنده: در حال بارگذاری...");
        if (ad.owner != null && ad.owner.id != null) {
            loadSellerRating(ad.owner.id, ratingLabel);
        } else {
            ratingLabel.setText("امتیاز فروشنده: نامشخص");
        }

        int imageCount = ad.images != null ? ad.images.size() : 0;
        Label images = new Label("تعداد تصاویر: " + imageCount + " (نمایش گالری تصاویر پیاده‌سازی نشده است)");

        root.getChildren().addAll(title, price, meta, descTitle, desc, owner, ratingLabel, images);

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

            Button chatButton = new Button("گفت‌وگو با فروشنده");
            chatButton.setOnAction(e -> handleStartChat(ad.id, ownerName));

            Button ratingButton = new Button("امتیازدهی به فروشنده");
            ratingButton.setOnAction(e -> RatingDialog.showAndSubmit(ad.id));

            actions.getChildren().addAll(favoriteButton, chatButton, ratingButton);
        } else {
            Label guestHint = new Label("برای افزودن به علاقه‌مندی‌ها، گفت‌وگو یا امتیازدهی، ابتدا وارد شوید.");
            guestHint.setStyle("-fx-text-fill: #888;");
            actions.getChildren().add(guestHint);
        }

        root.getChildren().add(actions);
    }

    private static void loadSellerRating(long ownerId, Label ratingLabel) {
        Task<SellerRatingsSummary> task = new Task<>() {
            @Override
            protected SellerRatingsSummary call() throws Exception {
                return RatingService.getSellerRatings(ownerId);
            }
        };
        task.setOnSucceeded(e -> {
            SellerRatingsSummary summary = task.getValue();
            if (summary.totalRatings != null && summary.totalRatings > 0) {
                ratingLabel.setText(String.format("امتیاز فروشنده: %.1f از ۵ (%d رأی)",
                        summary.averageScore, summary.totalRatings));
            } else {
                ratingLabel.setText("امتیاز فروشنده: هنوز امتیازی ثبت نشده است.");
            }
        });
        task.setOnFailed(e -> ratingLabel.setText("امتیاز فروشنده: دریافت نشد."));
        new Thread(task).start();
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
        task.setOnSucceeded(e -> AlertUtil.showInfo("آگهی به علاقه‌مندی‌ها اضافه شد."));
        task.setOnFailed(e -> {
            favoriteButton.setDisable(false);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });
        new Thread(task).start();
    }

    private static void handleStartChat(Long adId, String ownerName) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("شروع گفت‌وگو");
        dialog.setHeaderText(null);
        dialog.setContentText("پیام خود به فروشنده:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(content -> {
            if (content.isBlank()) {
                AlertUtil.showError("متن پیام نمی‌تواند خالی باشد.");
                return;
            }
            Task<Long> task = new Task<>() {
                @Override
                protected Long call() throws Exception {
                    return ChatService.startConversation(adId, content);
                }
            };
            task.setOnSucceeded(e -> {
                Long conversationId = task.getValue();
                SceneManager.show(ChatView.build(conversationId, ownerName), "گفت‌وگو با " + ownerName);
            });
            task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
            new Thread(task).start();
        });
    }
}
package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.AdvertisementDetail;
import com.secondhand.frontend.model.SellerRatingsSummary;
import com.secondhand.frontend.service.AdvertisementService;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.service.RatingService;
import com.secondhand.frontend.session.SessionManager;
import com.secondhand.frontend.ui.components.ImagePickerView;
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
        root.setStyle(Theme.BG_DARK);
        root.setPadding(new Insets(20));

        Button backButton = Theme.secondaryButton("بازگشت به لیست");
        backButton.setOnAction(e -> SceneManager.show(AdListView.build(), "لیست آگهی‌ها"));

        Label loadingLabel = new Label("در حال بارگذاری جزئیات آگهی...");
        loadingLabel.setStyle(Theme.TEXT_LIGHT);
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
        title.setStyle(Theme.SECTION_TITLE);

        Label price = new Label(ad.price != null ? String.format("%,d تومان", ad.price) : "-");
        price.setStyle(Theme.CARD_PRICE + "-fx-font-size: 16px;");

        Label meta = new Label("شهر: " + ad.city + "   |   دسته‌بندی: " + ad.category + "   |   وضعیت: " + ad.status);
        meta.setStyle(Theme.TEXT_LIGHT);

        root.getChildren().addAll(title, price, meta);

        // بنر دلیل رد آگهی (اگه رد شده باشه)
        if ("REJECTED".equals(ad.status) && ad.rejectionReason != null && !ad.rejectionReason.isBlank()) {
            Label rejectionBanner = new Label("این آگهی توسط مدیر رد شده است. دلیل: " + ad.rejectionReason);
            rejectionBanner.setStyle(Theme.DANGER_BANNER);
            rejectionBanner.setWrapText(true);
            root.getChildren().add(rejectionBanner);
        }

        Label descTitle = new Label("توضیحات:");
        descTitle.setStyle(Theme.SECTION_TITLE + "-fx-font-size: 14px;");
        Label desc = new Label(ad.description != null ? ad.description : "-");
        desc.setStyle(Theme.TEXT_LIGHT);
        desc.setWrapText(true);

        String ownerName = ad.owner != null ? ad.owner.fullName : "-";
        Label owner = new Label("فروشنده: " + ownerName);
        owner.setStyle(Theme.TEXT_LIGHT);

        Label ratingLabel = new Label("امتیاز فروشنده: در حال بارگذاری...");
        ratingLabel.setStyle(Theme.TEXT_LIGHT);

        Button showCommentsButton = Theme.linkButton("نمایش نظرات");
        showCommentsButton.setVisible(false);
        if (ad.owner != null && ad.owner.id != null) {
            long ownerId = ad.owner.id;
            loadSellerRating(ownerId, ratingLabel);
            showCommentsButton.setVisible(true);
            showCommentsButton.setOnAction(e -> RatingCommentsDialog.show(ownerId, ownerName));
        } else {
            ratingLabel.setText("امتیاز فروشنده: نامشخص");
        }

        HBox ratingRow = new HBox(10, ratingLabel, showCommentsButton);

        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        boolean isOwner = loggedIn && ad.owner != null && ad.owner.id != null
                && ad.owner.id.equals(SessionManager.getInstance().getUserId());

        Label imagesTitle = new Label("تصاویر آگهی:");
        imagesTitle.setStyle(Theme.SECTION_TITLE + "-fx-font-size: 14px;");
        ImagePickerView gallery = new ImagePickerView(ad.id, ad.images, isOwner);

        root.getChildren().addAll(descTitle, desc, owner, ratingRow, imagesTitle, gallery.getNode());

        HBox actions = new HBox(10);

        if (isOwner) {
            Button editButton = Theme.secondaryButton("ویرایش آگهی");
            editButton.setOnAction(e -> SceneManager.show(AdFormView.buildEdit(ad.id), "ویرایش آگهی"));

            Button deleteButton = Theme.secondaryButton("حذف آگهی");
            deleteButton.setOnAction(e -> handleDelete(ad.id));

            Button soldButton = Theme.secondaryButton("علامت‌گذاری به‌عنوان فروخته‌شده");
            soldButton.setDisable("SOLD".equals(ad.status));
            soldButton.setOnAction(e -> handleMarkSold(ad.id));

            actions.getChildren().addAll(editButton, deleteButton, soldButton);
        } else if (loggedIn) {
            Button favoriteButton = Theme.secondaryButton("افزودن به علاقه‌مندی‌ها");
            favoriteButton.setOnAction(e -> handleAddFavorite(ad.id, favoriteButton));

            Button chatButton = Theme.primaryButton("گفت‌وگو با فروشنده");
            chatButton.setOnAction(e -> handleStartChat(ad.id, ownerName));

            Button ratingButton = Theme.secondaryButton("امتیازدهی به فروشنده");
            ratingButton.setOnAction(e -> RatingDialog.showAndSubmit(ad.id));

            actions.getChildren().addAll(favoriteButton, chatButton, ratingButton);
        } else {
            Label guestHint = new Label("برای افزودن به علاقه‌مندی‌ها، گفت‌وگو یا امتیازدهی، ابتدا وارد شوید.");
            guestHint.setStyle(Theme.TEXT_MUTED);
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
package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.AdvertisementDetail;
import com.secondhand.frontend.service.AdvertisementService;
import com.secondhand.frontend.service.AdminService;
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

/**
 * Full detail view an admin sees before approving/rejecting a pending
 * advertisement — shows everything the seller filled in (including edits
 * made after a previous rejection), not just title + seller name.
 */
public class AdminAdDetailView {

    public static Parent build(long adId) {
        VBox root = new VBox(12);
        root.setStyle(Theme.BG_DARK);
        root.setPadding(new Insets(20));

        Button backButton = Theme.secondaryButton("بازگشت به پنل مدیریت");
        backButton.setOnAction(e -> SceneManager.show(AdminPanelView.build(), "پنل مدیریت"));

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
            root.getChildren().remove(loadingLabel);
            renderDetail(root, task.getValue());
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

        // اگه قبلاً رد شده بوده (مثلاً فروشنده ویرایش کرده و دوباره اومده برای بررسی)، دلیل قبلی رو هم نشون بده
        if ("REJECTED".equals(ad.status) && ad.rejectionReason != null && !ad.rejectionReason.isBlank()) {
            Label rejectionBanner = new Label("این آگهی قبلاً رد شده بود. دلیل: " + ad.rejectionReason);
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

        Label imagesTitle = new Label("تصاویر آگهی:");
        imagesTitle.setStyle(Theme.SECTION_TITLE + "-fx-font-size: 14px;");
        ImagePickerView gallery = new ImagePickerView(ad.id, ad.images, false);

        root.getChildren().addAll(descTitle, desc, owner, imagesTitle, gallery.getNode());

        if ("PENDING".equals(ad.status)) {
            Button approveButton = Theme.successButton("✓ تایید آگهی");
            approveButton.setOnAction(e -> handleApprove(ad.id));

            Button rejectButton = Theme.dangerButton("✕ رد آگهی");
            rejectButton.setOnAction(e -> handleReject(ad.id));

            HBox actions = new HBox(10, approveButton, rejectButton);
            root.getChildren().add(actions);
        }
    }

    private static void handleApprove(long adId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                AdminService.approve(adId);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            AlertUtil.showInfo("آگهی تایید شد.");
            SceneManager.show(AdminPanelView.build(), "پنل مدیریت");
        });
        task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
        new Thread(task).start();
    }

    private static void handleReject(long adId) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("رد آگهی");
        dialog.setHeaderText(null);
        dialog.setContentText("دلیل رد آگهی:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(reason -> {
            if (reason.isBlank()) {
                AlertUtil.showError("لطفاً دلیل رد آگهی را وارد کنید.");
                return;
            }
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    AdminService.reject(adId, reason);
                    return null;
                }
            };
            task.setOnSucceeded(e -> {
                AlertUtil.showInfo("آگهی رد شد.");
                SceneManager.show(AdminPanelView.build(), "پنل مدیریت");
            });
            task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
            new Thread(task).start();
        });
    }
}
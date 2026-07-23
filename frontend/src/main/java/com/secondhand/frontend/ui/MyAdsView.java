package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.AdvertisementDetail;
import com.secondhand.frontend.service.AdvertisementService;
import com.secondhand.frontend.session.SessionManager;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class MyAdsView {

    public static Parent build() {
        VBox root = new VBox(12);
        root.setStyle(Theme.BG_DARK);
        root.setPadding(new Insets(20));

        Button backButton = Theme.secondaryButton("بازگشت به لیست آگهی‌ها");
        backButton.setOnAction(e -> SceneManager.show(AdListView.build(), "لیست آگهی‌ها"));

        Label title = new Label("آگهی‌های من");
        title.setStyle(Theme.SECTION_TITLE);

        VBox itemsBox = new VBox(8);

        List<Long> myAdIds = SessionManager.getInstance().getMyAdIds();

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(Theme.BG_DARK);

        root.getChildren().addAll(backButton, title, scrollPane);

        loadMyAds(itemsBox);

        return root;
    }

    private static void loadMyAds(VBox itemsBox) {
        Task<List<com.secondhand.frontend.model.Advertisement>> task = new Task<>() {
            @Override
            protected List<com.secondhand.frontend.model.Advertisement> call() throws Exception {
                return AdvertisementService.listMine();
            }
        };
        task.setOnSucceeded(e -> {
            List<com.secondhand.frontend.model.Advertisement> ads = task.getValue();
            if (ads.isEmpty()) {
                Label empty = new Label("هنوز آگهی‌ای ثبت نکرده‌اید.");
                empty.setStyle(Theme.TEXT_MUTED);
                itemsBox.getChildren().add(empty);
            } else {
                for (com.secondhand.frontend.model.Advertisement ad : ads) {
                    itemsBox.getChildren().add(buildRow(ad));
                }
            }
        });
        task.setOnFailed(e -> {
            Label errorLabel = new Label("خطا در بارگذاری آگهی‌ها.");
            errorLabel.setStyle(Theme.TEXT_MUTED);
            itemsBox.getChildren().add(errorLabel);
        });
        new Thread(task).start();
    }

    private static VBox buildRow(com.secondhand.frontend.model.Advertisement ad) {
        VBox row = new VBox(4);
        row.setStyle(Theme.CARD_BG + "-fx-padding: 12; -fx-cursor: hand;");

        String priceText = ad.price != null ? String.format("%,d تومان", ad.price) : "-";
        Label headerLabel = new Label(ad.title + "   |   " + priceText + "   |   وضعیت: " + ad.status);
        headerLabel.setStyle(Theme.CARD_TITLE);

        row.getChildren().add(headerLabel);

        // اگه رد شده، دلیلش رو هم می‌گیریم و نشون می‌دیم
        if ("REJECTED".equals(ad.status)) {
            loadRejectionReason(ad.id, row);
        }

        row.setOnMouseClicked(e -> SceneManager.show(AdDetailView.build(ad.id), "جزئیات آگهی"));
        return row;
    }

    private static void loadRejectionReason(Long adId, VBox row) {
        Task<AdvertisementDetail> task = new Task<>() {
            @Override
            protected AdvertisementDetail call() throws Exception {
                return AdvertisementService.getDetail(adId);
            }
        };
        task.setOnSucceeded(e -> {
            AdvertisementDetail detail = task.getValue();
            if (detail.rejectionReason != null && !detail.rejectionReason.isBlank()) {
                Label reasonLabel = new Label("دلیل رد: " + detail.rejectionReason);
                reasonLabel.setStyle("-fx-text-fill: #ff8a80; -fx-font-size: 11px;");
                reasonLabel.setWrapText(true);
                row.getChildren().add(reasonLabel);
            }
        });
        task.setOnFailed(e -> { });
        new Thread(task).start();
    }
}
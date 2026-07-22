package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.Advertisement;
import com.secondhand.frontend.service.AdvertisementService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Shows all advertisements owned by the current user (any status except DELETED),
 * fetched from GET /api/advertisements/mine. This includes PENDING and REJECTED ads
 * that don't appear in the public list. Backed by a real endpoint, so it survives
 * logout/login and works across devices/sessions.
 */
public class MyAdsView {

    public static Parent build() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Button backButton = new Button("بازگشت به لیست آگهی‌ها");
        backButton.setOnAction(e -> SceneManager.show(AdListView.build(), "لیست آگهی‌ها"));

        Label title = new Label("آگهی‌های من");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox itemsBox = new VBox(8);
        Label statusLabel = new Label("در حال بارگذاری...");
        itemsBox.getChildren().add(statusLabel);

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);

        root.getChildren().addAll(backButton, title, scrollPane);

        loadMyAds(itemsBox, statusLabel);

        return root;
    }

    private static void loadMyAds(VBox itemsBox, Label statusLabel) {
        Task<List<Advertisement>> task = new Task<>() {
            @Override
            protected List<Advertisement> call() throws Exception {
                return AdvertisementService.getMine();
            }
        };

        task.setOnSucceeded(e -> {
            itemsBox.getChildren().clear();
            List<Advertisement> ads = task.getValue();
            if (ads.isEmpty()) {
                itemsBox.getChildren().add(new Label("هنوز آگهی‌ای ثبت نکرده‌اید."));
            } else {
                for (Advertisement ad : ads) {
                    itemsBox.getChildren().add(buildRow(ad));
                }
            }
        });

        task.setOnFailed(e -> {
            itemsBox.getChildren().clear();
            itemsBox.getChildren().add(new Label("بارگذاری آگهی‌های شما ناموفق بود. دوباره تلاش کنید."));
        });

        new Thread(task).start();
    }

    private static HBox buildRow(Advertisement ad) {
        String priceText = ad.price != null ? String.format("%,d تومان", ad.price) : "-";
        Label label = new Label(ad.title + "   |   " + priceText + "   |   وضعیت: " + ad.status);

        Button viewButton = new Button("مشاهده / مدیریت");
        viewButton.setOnAction(e -> SceneManager.show(AdDetailView.build(ad.id), "جزئیات آگهی"));

        return new HBox(10, label, viewButton);
    }
}
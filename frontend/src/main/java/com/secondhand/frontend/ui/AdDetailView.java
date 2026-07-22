package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.AdvertisementDetail;
import com.secondhand.frontend.service.AdvertisementService;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

public class AdDetailView {

    public static Parent build(long adId) {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Button backButton = new Button("بازگشت به لیست");
        backButton.setOnAction(e -> SceneManager.show(AdListView.build(), "لیست آگهی‌ها"));

        Label loadingLabel = new Label("در حال بارگذاری جزئیات آگهی...");
        root.getChildren().addAll(backButton, loadingLabel);

        Task<AdvertisementDetail> task = new Task<>() {
            @Override
            protected AdvertisementDetail call() throws Exception {
                return AdvertisementService.getDetail(adId);
            }
        };

        task.setOnSucceeded(e -> {
            AdvertisementDetail ad = task.getValue();
            root.getChildren().remove(loadingLabel);

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
            Label images = new Label("تعداد تصاویر: " + imageCount + " (نمایش گالری در نسخه بعدی اضافه می‌شود)");

            root.getChildren().addAll(title, price, meta, descTitle, desc, owner, images);

            // دکمه‌های چت / علاقه‌مندی / امتیازدهی در فازهای بعدی این پروژه اضافه خواهند شد
            Button placeholderButton = new Button("گفت‌وگو با فروشنده / افزودن به علاقه‌مندی‌ها (به‌زودی)");
            placeholderButton.setDisable(true);
            root.getChildren().add(placeholderButton);
        });

        task.setOnFailed(e -> {
            root.getChildren().remove(loadingLabel);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });

        new Thread(task).start();

        return root;
    }
}
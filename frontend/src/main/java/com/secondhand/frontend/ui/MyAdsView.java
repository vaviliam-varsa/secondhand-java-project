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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Shows advertisements the current user created during this session.
 *
 * The API contract has no "get my advertisements" endpoint, so there is no way to
 * reliably list a user's ads (especially PENDING ones, which don't appear in the
 * public list). As a practical workaround, SessionManager remembers the id of every
 * ad the user creates in this session, and this screen fetches their current details.
 * Ads created in a previous session (before the app was restarted) will not appear here.
 */
public class MyAdsView {

    public static Parent build() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Button backButton = new Button("بازگشت به لیست آگهی‌ها");
        backButton.setOnAction(e -> SceneManager.show(AdListView.build(), "لیست آگهی‌ها"));

        Label title = new Label("آگهی‌های من (این نشست)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label hint = new Label("توجه: این لیست فقط آگهی‌هایی را نشان می‌دهد که در همین اجرای برنامه ثبت کرده‌اید،"
                + " چون API فعلی مسیر «دریافت آگهی‌های من» را ندارد.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        VBox itemsBox = new VBox(8);

        List<Long> myAdIds = SessionManager.getInstance().getMyAdIds();
        if (myAdIds.isEmpty()) {
            itemsBox.getChildren().add(new Label("هنوز در این نشست آگهی‌ای ثبت نکرده‌اید."));
        } else {
            for (Long adId : myAdIds) {
                itemsBox.getChildren().add(buildLoadingRow(adId));
            }
        }

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);

        root.getChildren().addAll(backButton, title, hint, scrollPane);
        return root;
    }

    private static HBox buildLoadingRow(Long adId) {
        Label label = new Label("آگهی #" + adId + " — در حال بارگذاری...");
        HBox row = new HBox(10, label);

        Task<AdvertisementDetail> task = new Task<>() {
            @Override
            protected AdvertisementDetail call() throws Exception {
                return AdvertisementService.getDetail(adId);
            }
        };
        task.setOnSucceeded(e -> {
            AdvertisementDetail ad = task.getValue();
            String priceText = ad.price != null ? String.format("%,d تومان", ad.price) : "-";
            label.setText(ad.title + "   |   " + priceText + "   |   وضعیت: " + ad.status);

            Button viewButton = new Button("مشاهده / مدیریت");
            viewButton.setOnAction(e2 -> SceneManager.show(AdDetailView.build(ad.id), "جزئیات آگهی"));
            row.getChildren().add(viewButton);
        });
        task.setOnFailed(e -> label.setText("آگهی #" + adId + " — بارگذاری ناموفق بود."));
        new Thread(task).start();

        return row;
    }
}
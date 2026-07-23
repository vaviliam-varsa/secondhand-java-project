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

    private static final String PRIMARY_BUTTON_STYLE =
            "-fx-background-color: #ec1c24; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18 8 18;";
    private static final String SECONDARY_BUTTON_STYLE =
            "-fx-background-color: #2e2e30; -fx-text-fill: #cfcfcf; -fx-background-radius: 6; -fx-padding: 8 14 8 14;";

    public static Parent build(long adId) {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Button backButton = new Button("بازگشت به پنل مدیریت");
        backButton.setStyle(SECONDARY_BUTTON_STYLE);
        backButton.setOnAction(e -> SceneManager.show(AdminPanelView.build(), "پنل مدیریت"));

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
        title.setStyle("-fx-text-fill: #f2f2f2; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label price = new Label(ad.price != null ? String.format("%,d تومان", ad.price) : "-");
        price.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label meta = new Label("شهر: " + ad.city + "   |   دسته‌بندی: " + ad.category + "   |   وضعیت: " + ad.status);
        meta.setStyle("-fx-text-fill: #cfcfcf;");

        Label descTitle = new Label("توضیحات:");
        descTitle.setStyle("-fx-text-fill: #f2f2f2; -fx-font-weight: bold;");
        Label desc = new Label(ad.description != null ? ad.description : "-");
        desc.setStyle("-fx-text-fill: #cfcfcf;");
        desc.setWrapText(true);

        String ownerName = ad.owner != null ? ad.owner.fullName : "-";
        Label owner = new Label("فروشنده: " + ownerName);
        owner.setStyle("-fx-text-fill: #cfcfcf;");

        Label imagesTitle = new Label("تصاویر آگهی:");
        imagesTitle.setStyle("-fx-text-fill: #f2f2f2; -fx-font-weight: bold;");
        ImagePickerView gallery = new ImagePickerView(ad.id, ad.images, false);

        root.getChildren().addAll(title, price, meta, descTitle, desc, owner, imagesTitle, gallery.getNode());

        if ("PENDING".equals(ad.status)) {
            Button approveButton = new Button("تایید آگهی");
            approveButton.setStyle(PRIMARY_BUTTON_STYLE);
            approveButton.setOnAction(e -> handleApprove(ad.id));

            Button rejectButton = new Button("رد آگهی");
            rejectButton.setStyle(SECONDARY_BUTTON_STYLE);
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
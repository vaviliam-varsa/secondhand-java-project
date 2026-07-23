package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.FavoriteItem;
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class FavoritesView {

    public static Parent build() {
        VBox root = new VBox(12);
        root.setStyle(Theme.BG_DARK);
        root.setPadding(new Insets(20));

        Button backButton = Theme.secondaryButton("بازگشت به لیست آگهی‌ها");
        backButton.setOnAction(e -> SceneManager.show(AdListView.build(), "لیست آگهی‌ها"));

        Label title = new Label("علاقه‌مندی‌های من");
        title.setStyle(Theme.SECTION_TITLE);

        VBox itemsBox = new VBox(8);
        Label loadingLabel = new Label("در حال بارگذاری...");
        loadingLabel.setStyle(Theme.TEXT_LIGHT);
        itemsBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(Theme.BG_DARK);

        root.getChildren().addAll(backButton, title, scrollPane);

        loadFavorites(itemsBox, loadingLabel);

        return root;
    }

    private static void loadFavorites(VBox itemsBox, Label loadingLabel) {
        Task<List<FavoriteItem>> task = new Task<>() {
            @Override
            protected List<FavoriteItem> call() throws Exception {
                return FavoriteService.list();
            }
        };
        task.setOnSucceeded(e -> {
            itemsBox.getChildren().remove(loadingLabel);
            List<FavoriteItem> favorites = task.getValue();
            if (favorites.isEmpty()) {
                Label empty = new Label("هنوز آگهی‌ای به علاقه‌مندی‌ها اضافه نکرده‌اید.");
                empty.setStyle(Theme.TEXT_MUTED);
                itemsBox.getChildren().add(empty);
            } else {
                for (FavoriteItem fav : favorites) {
                    itemsBox.getChildren().add(buildRow(fav, itemsBox));
                }
            }
        });
        task.setOnFailed(e -> {
            itemsBox.getChildren().remove(loadingLabel);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });
        new Thread(task).start();
    }

    private static HBox buildRow(FavoriteItem fav, VBox itemsBox) {
        String titleText = fav.advertisement != null ? fav.advertisement.title : "(آگهی حذف شده)";
        String priceText = fav.advertisement != null && fav.advertisement.price != null
                ? String.format("%,d تومان", fav.advertisement.price)
                : "-";

        Label label = new Label(titleText + "   |   " + priceText);
        label.setStyle(Theme.TEXT_LIGHT);
        label.setMaxWidth(500);

        Button viewButton = Theme.secondaryButton("مشاهده آگهی");
        viewButton.setDisable(fav.advertisement == null);

        HBox row = new HBox(10, label, viewButton);
        row.setStyle(Theme.CARD_BG + "-fx-padding: 10;");

        if (fav.advertisement != null) {
            Long adId = fav.advertisement.id;
            viewButton.setOnAction(e -> SceneManager.show(AdDetailView.build(adId), "جزئیات آگهی"));
        }

        Button removeButton = Theme.secondaryButton("حذف از علاقه‌مندی‌ها");
        removeButton.setOnAction(e -> handleRemove(fav.id, itemsBox, row));
        row.getChildren().add(removeButton);

        return row;
    }

    private static void handleRemove(Long favoriteId, VBox itemsBox, HBox row) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                FavoriteService.remove(favoriteId);
                return null;
            }
        };
        task.setOnSucceeded(e -> itemsBox.getChildren().remove(row));
        task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
        new Thread(task).start();
    }
}
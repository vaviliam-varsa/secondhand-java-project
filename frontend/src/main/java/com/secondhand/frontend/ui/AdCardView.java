package com.secondhand.frontend.ui.components;

import com.secondhand.frontend.config.ApiConfig;
import com.secondhand.frontend.model.Advertisement;
import com.secondhand.frontend.model.AdvertisementDetail;
import com.secondhand.frontend.service.AdvertisementService;
import com.secondhand.frontend.ui.Theme;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AdCardView {

    private static final double CARD_WIDTH = 180;
    private static final double IMAGE_SIZE = 160;

    // Session-lifetime cache: adId -> first image path (or "" if none/failed).
    // Needed because GET /api/advertisements (the list endpoint) has no image field;
    // this fetches each ad's detail once (per app run) just to grab its first image.
    // Proper long-term fix: add a thumbnail field to the list DTO on the backend.
    private static final Map<Long, String> thumbnailCache = new ConcurrentHashMap<>();

    public static Node build(Advertisement ad, Consumer<Advertisement> onClick) {
        VBox card = new VBox(6);
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(Theme.CARD_BG + "-fx-padding: 10; -fx-cursor: hand;");

        StackPane imageBox = new StackPane();
        imageBox.setPrefSize(IMAGE_SIZE, IMAGE_SIZE);
        imageBox.setMaxSize(IMAGE_SIZE, IMAGE_SIZE);
        imageBox.setStyle("-fx-background-color: #3a3a3d; -fx-background-radius: 8;");

        Label placeholderIcon = new Label("🖼");
        placeholderIcon.setStyle("-fx-font-size: 40px; -fx-text-fill: #6b6b6e;");
        imageBox.getChildren().add(placeholderIcon);

        loadThumbnail(ad.id, imageBox, placeholderIcon);

        Label title = new Label(ad.title);
        title.setStyle(Theme.CARD_TITLE);
        title.setWrapText(true);
        title.setMaxWidth(IMAGE_SIZE);
        title.setAlignment(Pos.CENTER);

        String priceText = ad.price != null ? String.format("%,d تومان", ad.price) : "-";
        Label price = new Label(priceText);
        price.setStyle(Theme.CARD_PRICE);

        String metaText = (ad.city != null ? ad.city : "-") + " · " + (ad.category != null ? ad.category : "-");
        Label meta = new Label(metaText);
        meta.setStyle(Theme.CARD_META);
        meta.setWrapText(true);
        meta.setMaxWidth(IMAGE_SIZE);
        meta.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageBox, title, price, meta);
        card.setOnMouseClicked(e -> onClick.accept(ad));

        return card;
    }

    private static void loadThumbnail(Long adId, StackPane imageBox, Label placeholderIcon) {
        if (adId == null) return;

        String cached = thumbnailCache.get(adId);
        if (cached != null) {
            if (!cached.isEmpty()) {
                applyImage(imageBox, placeholderIcon, cached);
            }
            return;
        }

        Task<AdvertisementDetail> task = new Task<>() {
            @Override
            protected AdvertisementDetail call() throws Exception {
                return AdvertisementService.getDetail(adId);
            }
        };
        task.setOnSucceeded(e -> {
            AdvertisementDetail detail = task.getValue();
            String firstImage = (detail.images != null && !detail.images.isEmpty()) ? detail.images.get(0) : "";
            thumbnailCache.put(adId, firstImage);
            if (!firstImage.isEmpty()) {
                applyImage(imageBox, placeholderIcon, firstImage);
            }
        });
        task.setOnFailed(e -> thumbnailCache.put(adId, ""));
        new Thread(task).start();
    }

    private static void applyImage(StackPane imageBox, Label placeholderIcon, String relativePath) {
        String url = relativePath.startsWith("http") ? relativePath : ApiConfig.BASE_URL + "/" + relativePath;
        try {
            Image img = new Image(url, IMAGE_SIZE, IMAGE_SIZE, true, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(IMAGE_SIZE);
            iv.setFitHeight(IMAGE_SIZE);
            imageBox.getChildren().remove(placeholderIcon);
            imageBox.getChildren().add(iv);
        } catch (Exception ignored) {
            // در صورت خطا، همون آیکون placeholder می‌ماند
        }
    }
}
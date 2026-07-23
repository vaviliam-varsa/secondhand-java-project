package com.secondhand.frontend.ui.components;

import com.secondhand.frontend.model.Advertisement;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * A square-ish card for an advertisement, used in the grid on AdListView.
 * NOTE: the list API doesn't return an image, so this shows a placeholder icon.
 * Real thumbnails would need a small backend addition (image field on the list DTO).
 */
public class AdCardView {

    private static final double CARD_WIDTH = 180;
    private static final double IMAGE_SIZE = 160;

    public static Node build(Advertisement ad, Consumer<Advertisement> onClick) {
        VBox card = new VBox(6);
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: #2a2a2c; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");

        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.setPrefSize(IMAGE_SIZE, IMAGE_SIZE);
        imagePlaceholder.setMaxSize(IMAGE_SIZE, IMAGE_SIZE);
        imagePlaceholder.setStyle("-fx-background-color: #3a3a3d; -fx-background-radius: 8;");

        Label icon = new Label("🖼");
        icon.setStyle("-fx-font-size: 40px; -fx-text-fill: #6b6b6e;");
        imagePlaceholder.getChildren().add(icon);

        Label title = new Label(ad.title);
        title.setStyle("-fx-text-fill: #f2f2f2; -fx-font-size: 13px; -fx-font-weight: bold;");
        title.setWrapText(true);
        title.setMaxWidth(IMAGE_SIZE);
        title.setAlignment(Pos.CENTER);

        String priceText = ad.price != null ? String.format("%,d تومان", ad.price) : "-";
        Label price = new Label(priceText);
        price.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px; -fx-font-weight: bold;");

        String metaText = (ad.city != null ? ad.city : "-") + " · " + (ad.category != null ? ad.category : "-");
        Label meta = new Label(metaText);
        meta.setStyle("-fx-text-fill: #9a9a9a; -fx-font-size: 10px;");
        meta.setWrapText(true);
        meta.setMaxWidth(IMAGE_SIZE);
        meta.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imagePlaceholder, title, price, meta);
        card.setOnMouseClicked(e -> onClick.accept(ad));

        return card;
    }
}
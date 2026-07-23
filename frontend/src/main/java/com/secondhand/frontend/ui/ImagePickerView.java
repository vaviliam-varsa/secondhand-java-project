package com.secondhand.frontend.ui.components;

import com.secondhand.frontend.config.ApiConfig;
import com.secondhand.frontend.model.UploadImageResponse;
import com.secondhand.frontend.service.ImageService;
import com.secondhand.frontend.util.AlertUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagePickerView {

    private static final double THUMB_SIZE = 90;

    private final FlowPane pane = new FlowPane(10, 10);
    private final List<File> pendingFiles = new ArrayList<>();
    private Long advertisementId;
    private final boolean allowUpload;

    public ImagePickerView(Long advertisementId, List<String> existingImageUrls, boolean allowUpload) {
        this.advertisementId = advertisementId;
        this.allowUpload = allowUpload;

        if (existingImageUrls != null) {
            for (String url : existingImageUrls) {
                pane.getChildren().add(buildExistingThumbnail(url));
            }
        }
        if (allowUpload) {
            pane.getChildren().add(buildAddTile());
        }
    }

    public Node getNode() {
        return pane;
    }

    private Node buildAddTile() {
        Button addButton = new Button("+ افزودن عکس");
        addButton.setPrefSize(THUMB_SIZE, THUMB_SIZE);
        addButton.setWrapText(true);
        addButton.setStyle("-fx-background-color: #2e2e30; -fx-text-fill: #cfcfcf; -fx-background-radius: 6;");
        addButton.setOnAction(e -> pickFiles(addButton));
        return addButton;
    }

    private void pickFiles(Button addButton) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("انتخاب عکس آگهی");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("تصاویر", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.gif"));

        List<File> files = chooser.showOpenMultipleDialog(addButton.getScene().getWindow());
        if (files == null || files.isEmpty()) return;

        for (File file : files) {
            if (advertisementId != null) {
                uploadImmediately(file);
            } else {
                pendingFiles.add(file);
                int addTileIndex = pane.getChildren().size() - 1;
                pane.getChildren().add(addTileIndex, buildLocalThumbnail(file, "در انتظار ثبت آگهی"));
            }
        }
    }

    private void uploadImmediately(File file) {
        Node placeholder = buildLocalThumbnail(file, "در حال آپلود...");
        int addTileIndex = pane.getChildren().size() - 1;
        pane.getChildren().add(addTileIndex, placeholder);

        Task<UploadImageResponse> task = new Task<>() {
            @Override
            protected UploadImageResponse call() throws Exception {
                return ImageService.upload(advertisementId, file);
            }
        };
        task.setOnSucceeded(e -> {
            int idx = pane.getChildren().indexOf(placeholder);
            pane.getChildren().remove(placeholder);
            pane.getChildren().add(idx, buildExistingThumbnail(task.getValue().filePath));
        });
        task.setOnFailed(e -> {
            pane.getChildren().remove(placeholder);
            AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
        });
        new Thread(task).start();
    }

    public void uploadPendingTo(long newAdvertisementId, Runnable onAllDone) {
        this.advertisementId = newAdvertisementId;
        if (pendingFiles.isEmpty()) {
            onAllDone.run();
            return;
        }
        uploadSequentially(new ArrayList<>(pendingFiles), 0, onAllDone);
    }

    private void uploadSequentially(List<File> files, int index, Runnable onAllDone) {
        if (index >= files.size()) {
            onAllDone.run();
            return;
        }
        Task<UploadImageResponse> task = new Task<>() {
            @Override
            protected UploadImageResponse call() throws Exception {
                return ImageService.upload(advertisementId, files.get(index));
            }
        };
        task.setOnSucceeded(e -> uploadSequentially(files, index + 1, onAllDone));
        task.setOnFailed(e -> {
            AlertUtil.showError("آپلود عکس شماره " + (index + 1) + " ناموفق بود: "
                    + AlertUtil.extractMessage(task.getException()));
            uploadSequentially(files, index + 1, onAllDone);
        });
        new Thread(task).start();
    }

    private Node buildLocalThumbnail(File file, String badgeText) {
        StackPane box = new StackPane();
        box.setPrefSize(THUMB_SIZE, THUMB_SIZE);
        box.setStyle("-fx-background-color: #3a3a3d; -fx-background-radius: 6;");

        try {
            Image img = new Image(file.toURI().toString(), THUMB_SIZE, THUMB_SIZE, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(THUMB_SIZE);
            iv.setFitHeight(THUMB_SIZE);
            box.getChildren().add(iv);
        } catch (Exception ex) {
            box.getChildren().add(new Label("عکس"));
        }

        Label badge = new Label(badgeText);
        badge.setStyle("-fx-background-color: rgba(0,0,0,0.65); -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 2 4;");
        StackPane.setAlignment(badge, Pos.BOTTOM_CENTER);
        box.getChildren().add(badge);

        return box;
    }

    private Node buildExistingThumbnail(String relativeOrFullPath) {
        StackPane box = new StackPane();
        box.setPrefSize(THUMB_SIZE, THUMB_SIZE);
        box.setStyle("-fx-background-color: #3a3a3d; -fx-background-radius: 6; -fx-cursor: hand;");

        String url = relativeOrFullPath.startsWith("http")
                ? relativeOrFullPath
                : ApiConfig.BASE_URL + "/" + relativeOrFullPath;

        try {
            Image img = new Image(url, THUMB_SIZE, THUMB_SIZE, true, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(THUMB_SIZE);
            iv.setFitHeight(THUMB_SIZE);
            box.getChildren().add(iv);
            box.setOnMouseClicked(e -> openLightbox(box, url));
        } catch (Exception ex) {
            box.getChildren().add(new Label("خطا در بارگذاری"));
        }

        return box;
    }

    /** Opens the image full-size in an in-app dimmed overlay (like Divar's lightbox), not a separate OS window. */
    private static void openLightbox(Node ownerNode, String url) {
        Platform.runLater(() -> {
            Window ownerWindow = ownerNode.getScene() != null ? ownerNode.getScene().getWindow() : null;

            Stage lightbox = new Stage(StageStyle.TRANSPARENT);
            if (ownerWindow != null) {
                lightbox.initOwner(ownerWindow);
                lightbox.initModality(Modality.WINDOW_MODAL);
            }

            ImageView iv = new ImageView(new Image(url, 800, 800, true, true, true));
            iv.setOnMouseClicked(javafx.event.Event::consume);

            Label hint = new Label("برای بستن، بیرون از عکس کلیک کنید یا Esc را بزنید");
            hint.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
            StackPane.setAlignment(hint, Pos.BOTTOM_CENTER);

            StackPane overlay = new StackPane(iv, hint);
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
            overlay.setOnMouseClicked(e -> lightbox.close());

            Scene scene = new Scene(overlay);
            scene.setFill(Color.TRANSPARENT);
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) lightbox.close();
            });
            lightbox.setScene(scene);

            if (ownerWindow != null) {
                lightbox.setX(ownerWindow.getX());
                lightbox.setY(ownerWindow.getY());
                lightbox.setWidth(ownerWindow.getWidth());
                lightbox.setHeight(ownerWindow.getHeight());
            } else {
                lightbox.setWidth(800);
                lightbox.setHeight(800);
            }

            lightbox.show();
        });
    }
}
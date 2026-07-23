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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable image gallery + picker.
 *
 * - CREATE mode (advertisementId == null): picks are staged locally (nothing is uploaded yet).
 *   Call uploadPendingTo(newAdId, callback) right after the advertisement is created.
 * - EDIT / VIEW mode (advertisementId != null): existing images are shown, and if allowUpload
 *   is true, newly picked files are uploaded immediately (the ad already has an id).
 *
 * Clicking any thumbnail opens it full-size in a new window.
 */
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

    /** Call this right after a new advertisement is created, to upload files picked before it had an id. */
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
            uploadSequentially(files, index + 1, onAllDone); // با بقیه‌ی عکس‌ها ادامه بده
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
            box.setOnMouseClicked(e -> openFullImage(url));
        } catch (Exception ex) {
            box.getChildren().add(new Label("خطا در بارگذاری"));
        }

        return box;
    }

    private static void openFullImage(String url) {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            ImageView iv = new ImageView(new Image(url, 700, 700, true, true, true));
            StackPane root = new StackPane(iv);
            root.setStyle("-fx-background-color: black;");
            stage.setScene(new Scene(root, 720, 720));
            stage.setTitle("مشاهده عکس");
            stage.show();
        });
    }
}
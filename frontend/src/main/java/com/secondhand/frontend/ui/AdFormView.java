package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.AdvertisementDetail;
import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.model.CreateAdvertisementRequest;
import com.secondhand.frontend.model.UpdateAdvertisementRequest;
import com.secondhand.frontend.service.AdvertisementService;
import com.secondhand.frontend.service.CategoryService;
import com.secondhand.frontend.service.CityService;
import com.secondhand.frontend.session.SessionManager;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AdFormView {

    /** Build the "create new advertisement" form. */
    public static Parent buildCreate() {
        return build(null);
    }

    /** Build the "edit advertisement" form, prefilled with the current values. */
    public static Parent buildEdit(long adId) {
        return build(adId);
    }

    private static Parent build(Long adId) {
        boolean editMode = adId != null;

        Label title = new Label(editMode ? "ویرایش آگهی" : "ثبت آگهی جدید");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField titleField = new TextField();
        titleField.setPromptText("عنوان آگهی");
        titleField.setMaxWidth(400);

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("توضیحات");
        descriptionField.setMaxWidth(400);
        descriptionField.setPrefRowCount(5);
        descriptionField.setWrapText(true);

        TextField priceField = new TextField();
        priceField.setPromptText("قیمت (تومان)");
        priceField.setMaxWidth(400);

        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("دسته‌بندی");
        categoryBox.setMaxWidth(400);

        ComboBox<City> cityBox = new ComboBox<>();
        cityBox.setPromptText("شهر");
        cityBox.setMaxWidth(400);

        Label statusLabel = new Label();

        VBox box = new VBox(12, title, titleField, descriptionField, priceField);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));

        if (!editMode) {
            // فقط در حالت ثبت آگهی جدید، دسته‌بندی و شهر قابل انتخاب هستند.
            // طبق مستند API، ویرایش آگهی (PUT) فقط title/description/price را می‌پذیرد.
            loadCategories(categoryBox);
            loadCities(cityBox);
            box.getChildren().addAll(categoryBox, cityBox);
        } else {
            Label editHint = new Label("توجه: در حالت ویرایش، دسته‌بندی و شهر آگهی قابل تغییر نیستند.");
            editHint.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
            box.getChildren().add(editHint);

            loadExistingAd(adId, titleField, descriptionField, priceField);
        }

        Button submitButton = new Button(editMode ? "ذخیره تغییرات" : "ثبت آگهی");
        submitButton.setMaxWidth(400);

        Button cancelButton = new Button("انصراف");
        cancelButton.setMaxWidth(400);
        cancelButton.setOnAction(e -> {
            if (editMode) {
                SceneManager.show(AdDetailView.build(adId), "جزئیات آگهی");
            } else {
                SceneManager.show(AdListView.build(), "لیست آگهی‌ها");
            }
        });

        submitButton.setOnAction(e -> {
            String titleText = titleField.getText().trim();
            String descText = descriptionField.getText().trim();
            String priceText = priceField.getText().trim();

            if (titleText.isEmpty() || descText.isEmpty() || priceText.isEmpty()) {
                AlertUtil.showError("عنوان، توضیحات و قیمت را وارد کنید.");
                return;
            }

            Long price;
            try {
                price = Long.parseLong(priceText);
            } catch (NumberFormatException ex) {
                AlertUtil.showError("قیمت باید یک عدد معتبر باشد.");
                return;
            }

            if (!editMode && (categoryBox.getValue() == null || cityBox.getValue() == null)) {
                AlertUtil.showError("دسته‌بندی و شهر را انتخاب کنید.");
                return;
            }

            submitButton.setDisable(true);

            if (editMode) {
                UpdateAdvertisementRequest req = new UpdateAdvertisementRequest();
                req.title = titleText;
                req.description = descText;
                req.price = price;

                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        AdvertisementService.update(adId, req);
                        return null;
                    }
                };
                task.setOnSucceeded(ev -> {
                    submitButton.setDisable(false);
                    AlertUtil.showInfo("آگهی با موفقیت به‌روزرسانی شد.");
                    SceneManager.show(AdDetailView.build(adId), "جزئیات آگهی");
                });
                task.setOnFailed(ev -> {
                    submitButton.setDisable(false);
                    AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
                });
                new Thread(task).start();

            } else {
                CreateAdvertisementRequest req = new CreateAdvertisementRequest();
                req.title = titleText;
                req.description = descText;
                req.price = price;
                req.categoryId = categoryBox.getValue().id;
                req.cityId = cityBox.getValue().id;

                Task<Long> task = new Task<>() {
                    @Override
                    protected Long call() throws Exception {
                        return AdvertisementService.create(req);
                    }
                };
                task.setOnSucceeded(ev -> {
                    submitButton.setDisable(false);
                    Long newId = task.getValue();
                    SessionManager.getInstance().rememberCreatedAd(newId);
                    AlertUtil.showInfo("آگهی ثبت شد و در انتظار بررسی و تایید مدیر است.");
                    SceneManager.show(AdListView.build(), "لیست آگهی‌ها");
                });
                task.setOnFailed(ev -> {
                    submitButton.setDisable(false);
                    AlertUtil.showError(AlertUtil.extractMessage(task.getException()));
                });
                new Thread(task).start();
            }
        });

        box.getChildren().addAll(submitButton, cancelButton, statusLabel);
        return box;
    }

    private static void loadExistingAd(long adId, TextField titleField, TextArea descriptionField, TextField priceField) {
        Task<AdvertisementDetail> task = new Task<>() {
            @Override
            protected AdvertisementDetail call() throws Exception {
                return AdvertisementService.getDetail(adId);
            }
        };
        task.setOnSucceeded(e -> {
            AdvertisementDetail ad = task.getValue();
            titleField.setText(ad.title != null ? ad.title : "");
            descriptionField.setText(ad.description != null ? ad.description : "");
            priceField.setText(ad.price != null ? String.valueOf(ad.price) : "");
        });
        task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
        new Thread(task).start();
    }

    private static void loadCategories(ComboBox<Category> box) {
        Task<java.util.List<Category>> task = new Task<>() {
            @Override
            protected java.util.List<Category> call() throws Exception {
                return CategoryService.list();
            }
        };
        task.setOnSucceeded(e -> box.getItems().setAll(task.getValue()));
        task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
        new Thread(task).start();
    }

    private static void loadCities(ComboBox<City> box) {
        Task<java.util.List<City>> task = new Task<>() {
            @Override
            protected java.util.List<City> call() throws Exception {
                return CityService.list();
            }
        };
        task.setOnSucceeded(e -> box.getItems().setAll(task.getValue()));
        task.setOnFailed(e -> AlertUtil.showError(AlertUtil.extractMessage(task.getException())));
        new Thread(task).start();
    }
}
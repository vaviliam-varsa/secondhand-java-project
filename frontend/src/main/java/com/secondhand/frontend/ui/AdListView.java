package com.secondhand.frontend.ui;

import com.secondhand.frontend.model.Advertisement;
import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.service.AdvertisementService;
import com.secondhand.frontend.service.AuthService;
import com.secondhand.frontend.service.CategoryService;
import com.secondhand.frontend.service.CityService;
import com.secondhand.frontend.session.SessionManager;
import com.secondhand.frontend.ui.components.AdCardView;
import com.secondhand.frontend.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class AdListView {

    private static final String PRIMARY_BUTTON_STYLE =
            "-fx-background-color: #ec1c24; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18 8 18;";
    private static final String SECONDARY_BUTTON_STYLE =
            "-fx-background-color: #2e2e30; -fx-text-fill: #cfcfcf; -fx-background-radius: 6; -fx-padding: 8 14 8 14;";

    public static Parent build() {
        // Admins never see the normal browsing screen — if an admin session
        // somehow ends up here, send them straight to the admin panel.
        if (SessionManager.getInstance().isAdmin()) {
            return AdminPanelView.build();
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1c1c1e;");
        root.setPadding(new Insets(10));

        boolean loggedIn = SessionManager.getInstance().isLoggedIn();

        Label appTitle = new Label("دیوار۲ (نمونه)");
        appTitle.setStyle("-fx-text-fill: #ec1c24; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label userLabel = new Label(loggedIn
                ? "کاربر: " + SessionManager.getInstance().getUsername()
                : "مهمان (وارد نشده‌اید)");
        userLabel.setStyle("-fx-text-fill: #cfcfcf;");

        Button authButton = new Button(loggedIn ? "خروج" : "ورود");
        authButton.setStyle(SECONDARY_BUTTON_STYLE);
        authButton.setOnAction(e -> {
            if (SessionManager.getInstance().isLoggedIn()) {
                AuthService.logout();
            }
            SceneManager.show(LoginView.build(), "ورود");
        });

        HBox topBar = new HBox(14, appTitle, userLabel, authButton);
        topBar.setPadding(new Insets(0, 0, 6, 0));

        HBox actionsBar = new HBox(8);
        if (loggedIn) {
            Button createAdButton = new Button("+ ثبت آگهی جدید");
            createAdButton.setStyle(PRIMARY_BUTTON_STYLE);
            createAdButton.setOnAction(e -> SceneManager.show(AdFormView.buildCreate(), "ثبت آگهی جدید"));

            Button favoritesButton = new Button("علاقه‌مندی‌های من");
            favoritesButton.setStyle(SECONDARY_BUTTON_STYLE);
            favoritesButton.setOnAction(e -> SceneManager.show(FavoritesView.build(), "علاقه‌مندی‌های من"));

            Button myAdsButton = new Button("آگهی‌های من");
            myAdsButton.setStyle(SECONDARY_BUTTON_STYLE);
            myAdsButton.setOnAction(e -> SceneManager.show(MyAdsView.build(), "آگهی‌های من"));

            Button conversationsButton = new Button("گفت‌وگوهای من");
            conversationsButton.setStyle(SECONDARY_BUTTON_STYLE);
            conversationsButton.setOnAction(e -> SceneManager.show(ConversationsView.build(), "گفت‌وگوهای من"));

            actionsBar.getChildren().addAll(createAdButton, favoritesButton, myAdsButton, conversationsButton);

        }
        actionsBar.setPadding(new Insets(0, 0, 10, 0));

        TextField keywordField = new TextField();
        keywordField.setPromptText("جست‌وجو در عنوان/توضیحات");
        keywordField.setPrefWidth(260);
        keywordField.setStyle("-fx-background-color: #2a2a2c; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 6 14 6 14;");

        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("دسته‌بندی");

        ComboBox<City> cityBox = new ComboBox<>();
        cityBox.setPromptText("شهر");

        TextField minPriceField = new TextField();
        minPriceField.setPromptText("حداقل قیمت");
        minPriceField.setMaxWidth(110);

        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("حداکثر قیمت");
        maxPriceField.setMaxWidth(110);

        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("newest", "price_asc", "price_desc");
        sortBox.setPromptText("مرتب‌سازی");

        Button searchButton = new Button("جست‌وجو");
        searchButton.setStyle(PRIMARY_BUTTON_STYLE);

        HBox filterBar = new HBox(8, keywordField, categoryBox, cityBox, minPriceField, maxPriceField, sortBox, searchButton);
        filterBar.setPadding(new Insets(0, 0, 10, 0));

        FlowPane grid = new FlowPane(16, 16);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #1c1c1e;");

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1c1c1e; -fx-background: #1c1c1e;");

        VBox top = new VBox(6, topBar, actionsBar, filterBar);
        root.setTop(top);
        root.setCenter(scrollPane);

        loadCategories(categoryBox);
        loadCities(cityBox);

        Runnable doSearch = () -> {
            Map<String, String> filters = new HashMap<>();
            filters.put("keyword", keywordField.getText());
            if (categoryBox.getValue() != null) filters.put("categoryId", String.valueOf(categoryBox.getValue().id));
            if (cityBox.getValue() != null) filters.put("cityId", String.valueOf(cityBox.getValue().id));
            filters.put("minPrice", minPriceField.getText());
            filters.put("maxPrice", maxPriceField.getText());
            if (sortBox.getValue() != null) filters.put("sort", sortBox.getValue());
            loadAds(grid, filters);
        };

        searchButton.setOnAction(e -> doSearch.run());

        loadAds(grid, new HashMap<>());

        return root;
    }

    private static void loadAds(FlowPane grid, Map<String, String> filters) {
        Task<java.util.List<Advertisement>> task = new Task<>() {
            @Override
            protected java.util.List<Advertisement> call() throws Exception {
                return AdvertisementService.list(filters);
            }
        };
        task.setOnSucceeded(e -> {
            grid.getChildren().clear();
            for (Advertisement ad : task.getValue()) {
                grid.getChildren().add(AdCardView.build(ad,
                        selected -> SceneManager.show(AdDetailView.build(selected.id), "جزئیات آگهی")));
            }
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
        task.setOnFailed(e -> { /* در صورت خطا فیلتر دسته‌بندی صرفاً خالی می‌ماند */ });
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
        task.setOnFailed(e -> { /* در صورت خطا فیلتر شهر صرفاً خالی می‌ماند */ });
        new Thread(task).start();
    }
}
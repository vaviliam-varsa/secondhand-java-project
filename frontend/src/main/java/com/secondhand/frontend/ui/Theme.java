package com.secondhand.frontend.ui;

import javafx.scene.control.Button;

/** Centralized style constants so every screen looks consistent. Plain Java, no CSS file. */
public class Theme {

    public static final String BG_DARK = "-fx-background-color: #1c1c1e;";
    public static final String TEXT_LIGHT = "-fx-text-fill: #e6e6e6;";
    public static final String TEXT_MUTED = "-fx-text-fill: #9a9a9a;";
    public static final String APP_TITLE = "-fx-text-fill: #ec1c24; -fx-font-size: 22px; -fx-font-weight: bold;";
    public static final String SECTION_TITLE = "-fx-text-fill: #f2f2f2; -fx-font-size: 18px; -fx-font-weight: bold;";
    public static final String DANGER_BANNER =
            "-fx-background-color: #3a1f1f; -fx-text-fill: #ff8a80; -fx-padding: 12; -fx-background-radius: 8;";

    public static final String CARD_BG = "-fx-background-color: #2a2a2c; -fx-background-radius: 10;";
    public static final String CARD_TITLE = "-fx-text-fill: #f2f2f2; -fx-font-size: 13px; -fx-font-weight: bold;";
    public static final String CARD_PRICE = "-fx-text-fill: #4caf50; -fx-font-size: 12px; -fx-font-weight: bold;";
    public static final String CARD_META = "-fx-text-fill: #9a9a9a; -fx-font-size: 10px;";

    private static final String PRIMARY_BUTTON_STYLE =
            "-fx-background-color: #ec1c24; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18 8 18; -fx-cursor: hand;";
    private static final String SECONDARY_BUTTON_STYLE =
            "-fx-background-color: #2e2e30; -fx-text-fill: #cfcfcf; -fx-background-radius: 6; -fx-padding: 8 14 8 14; -fx-cursor: hand;";
    private static final String LINK_BUTTON_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #6ea8fe; -fx-cursor: hand;";

    public static Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle(PRIMARY_BUTTON_STYLE);
        return b;
    }

    public static Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setStyle(SECONDARY_BUTTON_STYLE);
        return b;
    }

    public static Button linkButton(String text) {
        Button b = new Button(text);
        b.setStyle(LINK_BUTTON_STYLE);
        return b;
    }
}
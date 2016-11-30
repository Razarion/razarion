package com.btxtech.client;

/**
 * Created by Beat
 * 30.11.2016.
 */
public class StaticResourcePath {
    private static final String IMAGES_PATH = "images/";
    private static final String CURSORS_PATH = IMAGES_PATH + "cursors/";
    private static final String CURSORS_POSTFIX = ".cur";

    public static String getCursorPath(String cursorName) {
        return CURSORS_PATH + cursorName + CURSORS_POSTFIX;
    }
}

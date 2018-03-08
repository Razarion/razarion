package com.btxtech.client;

/**
 * Created by Beat
 * 30.11.2016.
 */
public class StaticResourcePath {
    public static final String IMG_NAME_TICK = "Tick.png";
    public static final String IMG_NAME_EXCLAMATION = "Exclamation.png";
    public static final String IMG_NAME_PAUSE = "Pause.png";

    private static final String IMAGES_PATH = "images/";
    private static final String CURSORS_PATH = IMAGES_PATH + "cursors/";
    private static final String CURSORS_POSTFIX = ".cur";

    public static String getCursorPath(String cursorName) {
        return CURSORS_PATH + cursorName + CURSORS_POSTFIX;
    }

    public static String getImagePath(String imageName) {
        return IMAGES_PATH + "/" + imageName;
    }
}

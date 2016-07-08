package com.btxtech.shared;

/**
 * Created by Beat
 * 16.06.2016.
 */
public class RestUrl {

    public static final String APPLICATION_PATH = "rest";
    public static final String IMAGE_SERVICE_PATH = "image";
    public static final String STORYBOARD_SERVICE_PATH = "storyboard";
    public static final String PLANET_EDITOR_SERVICE_PATH = "planeteditor";
    public static final String TERRAIN_ELEMENT_SERVICE_PATH = "terrainelement";

    public static String getImageServiceUrl(int id) {
        return APPLICATION_PATH + "/" + IMAGE_SERVICE_PATH + "/" + Integer.toString(id) + "?t=" + System.currentTimeMillis();
    }

}

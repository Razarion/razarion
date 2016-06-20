package com.btxtech.shared;

/**
 * Created by Beat
 * 16.06.2016.
 */
public class RestUrl {

    public static final String APPLICATION_PATH = "rest";
    public static final String IMAGE_SERVICE_PATH = "image";

    public static String getImageServiceUrl(int id) {
        return APPLICATION_PATH + "/" + IMAGE_SERVICE_PATH + "/" + Integer.toString(id) + "?t=" + System.currentTimeMillis();
    }

}

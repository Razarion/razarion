package com.btxtech.shared.rest;

/**
 * Created by Beat
 * 16.06.2016.
 */
public class RestUrl {

    public static final String APPLICATION_PATH = "rest";
    public static final String IMAGE_SERVICE_PATH = "image";
    public static final String AUDIO_SERVICE_PATH = "audio";
    public static final String STORYBOARD_SERVICE_PATH = "storyboard";
    public static final String PLANET_EDITOR_SERVICE_PATH = "planeteditor";
    public static final String TERRAIN_ELEMENT_SERVICE_PATH = "terrainelement";
    public static final String SHAPE_3D_PROVIDER = "shape3dprovider";
    public static final String ITEM_TYPE_PROVIDER = "itemtypeprovider";
    public static final String CLIP_PROVIDER = "clipprovider";

    public static String getImageServiceUrl(int id) {
        return APPLICATION_PATH + "/" + IMAGE_SERVICE_PATH + "/" + Integer.toString(id) + "?t=" + System.currentTimeMillis();
    }

    public static String getImageServiceUrlSafe(Integer id) {
        if (id != null) {
            return getImageServiceUrl(id);
        } else {
            return "";
        }
    }

    public static String getAudioServiceUrl(int id) {
        return APPLICATION_PATH + "/" + AUDIO_SERVICE_PATH + "/" + Integer.toString(id) + "?t=" + System.currentTimeMillis();
    }

}

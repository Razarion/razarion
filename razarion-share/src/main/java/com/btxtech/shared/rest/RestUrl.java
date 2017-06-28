package com.btxtech.shared.rest;

/**
 * Created by Beat
 * 16.06.2016.
 */
public class RestUrl {
    public static final String GAME_CONNECTION_WEB_SOCKET_ENDPOINT = "/gameconnection";
    public static final String SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT = "/systemconnection";
    public static final String APPLICATION_PATH = "rest";
    public static final String G_ZIPPED = "gz"; // Must be configured on the webserver. (Wildfly standalone.xml set gzipFilter)
    public static final String REMOTE_LOGGING = "remote_logging";
    public static final String LOGGING_SIMPLE = "simple";
    public static final String LOGGING_JSON = "json";
    public static final String IMAGE_SERVICE_PATH = "image";
    public static final String AUDIO_SERVICE_PATH = "audio";
    public static final String SCENE_EDITOR_PATH = "sceneeditor";
    public static final String GAME_UI_CONTROL_PATH = G_ZIPPED + "/" + "gameuicontrol";
    public static final String COLD = "cold";
    public static final String WARM = "warm";
    public static final String PLANET_EDITOR_SERVICE_PATH = "planeteditor";
    public static final String TERRAIN_ELEMENT_SERVICE_PATH = "terrainelement";
    public static final String SHAPE_3D_PROVIDER = G_ZIPPED + "/" + "shape3dprovider";
    public static final String TERRAIN_SHAPE_PROVIDER = G_ZIPPED + "/" + "terrainshape";
    public static final String SHAPE_3D_PROVIDER_GET_VERTEX_BUFFER = "getshape3dvertexbuffer";
    public static final String ITEM_TYPE_PROVIDER = "itemtypeprovider";
    public static final String TRACKER_PATH = "tracker";
    public static final String TRACKER_BACKEND_PATH = "trackerbackend";
    public static final String MARKETING = "marketing";
    public static final String FB_CLICK_TRACKING_TAGS_RECEIVER = "clicktrackerreceiver";

    public static String getImageServiceUrl(int id) {
        return APPLICATION_PATH + "/" + IMAGE_SERVICE_PATH + "/" + Integer.toString(id)/* + "?t=" + System.currentTimeMillis()*/; // TODO image cache
    }

    public static String getSimpleLoggingUrl() {
        // DOMAIN is needed in the worker. Worker adds always worker JS dir to the path
        return "/" + APPLICATION_PATH + "/" + REMOTE_LOGGING + "/" + LOGGING_SIMPLE;
    }

    public static String getWorkerApplicationRoot() {
        // DOMAIN is needed in the worker. Worker adds always worker JS dir to the path
        return "/" + APPLICATION_PATH;
    }

    public static String loadShape3dBufferUrl() {
        return "/" + APPLICATION_PATH + "/" + SHAPE_3D_PROVIDER + "/" + SHAPE_3D_PROVIDER_GET_VERTEX_BUFFER;
    }

    public static String terrainShapeProvider(int planetId) {
        return "/" + APPLICATION_PATH + "/" + TERRAIN_SHAPE_PROVIDER + "/" + planetId;
    }

    public static String fbClickTrackingReceiver() {
        return "https://www.razarion.com/" + APPLICATION_PATH + "/" + MARKETING + "/" + FB_CLICK_TRACKING_TAGS_RECEIVER;
    }

    public static String getImageServiceUrlSafe(Integer id) {
        if (id != null) {
            return getImageServiceUrl(id);
        } else {
            return "";
        }
    }

    public static String getAudioServiceUrl(int id) {
        return APPLICATION_PATH + "/" + AUDIO_SERVICE_PATH + "/" + Integer.toString(id)/* + "?t=" + System.currentTimeMillis()*/;// TODO image cache
    }
}

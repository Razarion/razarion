package com.btxtech.shared;

/**
 * Created by Beat
 * 16.06.2016.
 */
public class CommonUrl {
    public static final String RAZARION_URL = "https://www.razarion.com";
    // Angular
    public static final String EMAIL_VERIFICATION = "/verify-email";
    public static final String FORGOT_PASSWORD_CHANGE = "/change-password";
    // GWT code path
    public static final String CLIENT_WORKER_PATH = "/com.btxtech.worker.RazarionClientWorker";
    public static final String CLIENT_WORKER_SCRIPT = CLIENT_WORKER_PATH + "/com.btxtech.worker.RazarionClientWorker.nocache.js";
    // Web socket
    public static final String GAME_CONNECTION_WEB_SOCKET_ENDPOINT = "/gameconnection";
    public static final String SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT = "/systemconnection";
    // Rest
    public static final String APPLICATION_PATH = "/rest";
    public static final String G_ZIPPED = "gz"; // Must be configured on the webserver. (Wildfly standalone.xml set gzipFilter)
    public static final String REMOTE_LOGGING = "remote_logging";
    public static final String LOGGING_SIMPLE = "simple";
    public static final String LOGGING_JSON = "json";
    public static final String IMAGE_SERVICE_PATH = "image";
    public static final String PLANET_MINI_MAP_PATH = "minimap";
    public static final String AUDIO_SERVICE_PATH = "audio";
    public static final String ALARM_SERVICE_PATH = "alarm-service";
    public static final String GAME_UI_CONTEXT_CONTROL_PATH = G_ZIPPED + "/" + "game-ui-context-control";
    public static final String COLD = "cold";
    public static final String WARM = "warm";
    public static final String PLANET_EDITOR_SERVICE_PATH = "planeteditor";
    public static final String PLANET_EDITOR_PATH = "editor/planet";
    public static final String BASE_ITEM_TYPE_EDITOR_PATH = "editor/base_item_type";
    public static final String GENERIC_PROPERTY_EDITOR_PATH = "editor/generic-property";
    public static final String GAME_UI_CONTEXT_EDITOR_PATH = "editor/game-ui-context";
    public static final String MODEL_3D_CONTROLLER = "editor/model-3d";
    public static final String INVENTORY_ITEM_EDITOR_PATH = "editor/inventory-item";
    public static final String TERRAIN_SHAPE_CONTROLLER = G_ZIPPED + "/" + "terrainshape";
    public static final String TERRAIN_HEIGHT_MAP_CONTROLLER = "terrainHeightMap";
    public static final String TRACKER_PATH = "tracker";
    public static final String SERVER_MGMT = "servermgmt";
    public static final String MARKETING = "marketing";
    public static final String FB_CLICK_TRACKING_TAGS_RECEIVER = "clicktrackerreceiver";
    public static final String COMMON_EDITOR_PROVIDER_PATH = "commoneditorprovider";
    public static final String USER_SERVICE_PROVIDER_PATH = "userserviceprovider";


    public static String getImageServiceUrl(int id) {
        return APPLICATION_PATH + "/" + IMAGE_SERVICE_PATH + "/" + id/* + "?t=" + System.currentTimeMillis()*/; // TODO image cache
    }

    public static String getMiniMapPlanetUrl(int planetId) {
        return APPLICATION_PATH + "/" + IMAGE_SERVICE_PATH + "/" + PLANET_MINI_MAP_PATH + "/" + planetId + "?t=" + System.currentTimeMillis();
    }

    public static String getSimpleLoggingUrl() {
        // DOMAIN is needed in the worker. Worker adds always worker JS dir to the path
        return APPLICATION_PATH + "/" + REMOTE_LOGGING + "/" + LOGGING_SIMPLE;
    }

    public static String getWorkerApplicationRoot() {
        // DOMAIN is needed in the worker. Worker adds always worker JS dir to the path
        return APPLICATION_PATH;
    }

    public static String terrainShapeController(int planetId) {
        return APPLICATION_PATH + "/" + TERRAIN_SHAPE_CONTROLLER + "/" + planetId;
    }

    public static String terrainHeightMapController(int planetId) {
        return APPLICATION_PATH + "/" + TERRAIN_HEIGHT_MAP_CONTROLLER + "/" + planetId;
    }

    public static String fbClickTrackingReceiver() {
        return RAZARION_URL + APPLICATION_PATH + "/" + MARKETING + "/" + FB_CLICK_TRACKING_TAGS_RECEIVER;
    }

    public static String getWorkerScriptUrl() {
        return CLIENT_WORKER_SCRIPT + "?t=" + System.currentTimeMillis();
    }

    public static String getImageServiceUrlSafe(Integer id) {
        if (id != null) {
            return getImageServiceUrl(id);
        } else {
            return "";
        }
    }

    public static String getAudioServiceUrl(int id) {
        return APPLICATION_PATH + "/" + AUDIO_SERVICE_PATH + "/" + id/* + "?t=" + System.currentTimeMillis()*/;// TODO image cache
    }

    public static String generateVerificationLink(String verificationId) {
        return RAZARION_URL + EMAIL_VERIFICATION + "/" + verificationId;
    }

    public static String generateForgotPasswordLink(String uuid) {
        return RAZARION_URL + FORGOT_PASSWORD_CHANGE + "/" + uuid;
    }

}

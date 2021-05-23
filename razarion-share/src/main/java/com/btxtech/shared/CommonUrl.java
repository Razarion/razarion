package com.btxtech.shared;

/**
 * Created by Beat
 * 16.06.2016.
 */
public class CommonUrl {
    public static final String RAZARION_URL = "https://www.razarion.com";
    // Angular
    public static final String ANGULAR_BACKEND_PATH = "/backend/";
    public static final String BACKEND_ANGULAR_HTML_FILE = ANGULAR_BACKEND_PATH + "index.html";
    public static final String FRONTEND_ANGULAR_HTML_FILE = "/index.html";
    public static final String EMAIL_VERIFICATION = "/verify-email";
    public static final String FORGOT_PASSWORD_CHANGE = "/change-password";
    public static final String LOGOUT_PAGE = "/logout";
    // GWT code path
    public static final String CLIENT_PATH = "/razarion_client";
    public static final String CLIENT_WORKER_PATH = "/razarion_client_worker";
    public static final String CLIENT_WORKER_SCRIPT = CLIENT_WORKER_PATH + "/razarion_client_worker.nocache.js";
    // Web socket
    public static final String GAME_CONNECTION_WEB_SOCKET_ENDPOINT = "/gameconnection";
    public static final String SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT = "/systemconnection";
    // Rest
    public static final String APPLICATION_PATH = "/rest";
    public static final String G_ZIPPED = "gz"; // Must be configured on the webserver. (Wildfly standalone.xml set gzipFilter)
    public static final String REMOTE_LOGGING = "remote_logging";
    public static final String LOGGING_SIMPLE = "simple";
    public static final String LOGGING_JSON = "json";
    public static final String LOGGING_JSON_DEBUG_DB = "debugdb";
    public static final String IMAGE_SERVICE_PATH = "image";
    public static final String PLANET_MINI_MAP_PATH = "minimap";
    public static final String AUDIO_SERVICE_PATH = "audio";
    public static final String ALARM_SERVICE_PATH = "alarm-service";
    public static final String SERVER_GAME_ENGINE_EDITOR_PROVIDER_PATH = "servergameengineditorprovider";
    public static final String QUEST_PROVIDER_PATH = "questprovider";
    public static final String SERVER_GAME_ENGINE_PATH = "server-game-engine";
    public static final String GAME_UI_CONTEXT_CONTROL_PATH = G_ZIPPED + "/" + "game-ui-context-control";
    public static final String COLD = "cold";
    public static final String WARM = "warm";
    public static final String PLANET_EDITOR_SERVICE_PATH = "planeteditor";
    public static final String TERRAIN_OBJECT_EDITOR_PATH = "editor/terrain-object";
    public static final String GROUND_EDITOR_PATH = "editor/ground";
    public static final String SLOPE_EDITOR_PATH = "editor/slope";
    public static final String DRIVEWAY_EDITOR_PATH = "editor/driveway";
    public static final String WATER_EDITOR_PATH = "editor/water";
    public static final String PLANET_EDITOR_PATH = "editor/planet";
    public static final String LEVEL_EDITOR_PATH = "editor/level";
    public static final String SHAPE_3D_EDITOR_PATH = "editor/shape-3d";
    public static final String BASE_ITEM_TYPE_EDITOR_PATH = "editor/base_item_type";
    public static final String RESOURCE_ITEM_TYPE_EDITOR_PATH = "editor/resource_item_type";
    public static final String GENERIC_PROPERTY_EDITOR_PATH = "editor/generic-property";
    public static final String GAME_UI_CONTEXT_EDITOR_PATH = "editor/game-ui-context";
    public static final String SERVER_GAME_ENGINE_EDITOR_PATH = "editor/server-game-engine";
    public static final String PARTICLE_EMITTER_SEQUENCE_EDITOR_PATH = "editor/particle-emitter-sequence-editor";
    public static final String PARTICLE_SHAPE_EDITOR_PATH = "editor/particle-shape-editor";
    public static final String SHAPE_3D_CONTROLLER = G_ZIPPED + "/" + "shape3d-controller";
    public static final String TERRAIN_SHAPE_CONTROLLER = G_ZIPPED + "/" + "terrainshape";
    public static final String SHAPE_3D_PROVIDER_GET_VERTEX_BUFFER = "getshape3dvertexbuffer";
    @Deprecated
    public static final String ITEM_TYPE_PROVIDER = "itemtypeprovider";
    public static final String TRACKER_PATH = "tracker";
    public static final String BACKEND_PATH = "backend";
    public static final String SERVER_MGMT = "servermgmt";
    public static final String MARKETING = "marketing";
    public static final String FB_CLICK_TRACKING_TAGS_RECEIVER = "clicktrackerreceiver";
    public static final String SERVER_GAME_ENGINE_MGMT_PATH = "servergameenginemgmt";
    public static final String COMMON_EDITOR_PROVIDER_PATH = "commoneditorprovider";
    public static final String INVENTORY_PROVIDER_PATH = "inventoryprovider";
    public static final String INVENTORY_EDITOR_PROVIDER_PATH = "inventoryeditorprovider";
    public static final String UNLOCK_PROVIDER_PATH = "unlockprovider";
    public static final String USER_SERVICE_PROVIDER_PATH = "userserviceprovider";
    public static final String FRONTEND_PATH = "frontend";
    public static final String SERVER_TEST_HELPER = "servertesthelper";
    // Cookies
    public static final String LOGIN_COOKIE_NAME = "LoginToken";
    public static final String RAZARION_COOKIE_NAME = "RazarionToken";
    // OpenApi schema types
    public static final String IMAGE_ID_TYPE = "imageId";
    public static final String COLLADA_STRING_TYPE = "colladaString";


    public static String getImageServiceUrl(int id) {
        return APPLICATION_PATH + "/" + IMAGE_SERVICE_PATH + "/" + Integer.toString(id)/* + "?t=" + System.currentTimeMillis()*/; // TODO image cache
    }

    public static String getMiniMapPlanetUrl(int planetId) {
        return APPLICATION_PATH + "/" + IMAGE_SERVICE_PATH + "/" + PLANET_MINI_MAP_PATH + "/" + Integer.toString(planetId) + "?t=" + System.currentTimeMillis();
    }

    public static String getSimpleLoggingUrl() {
        // DOMAIN is needed in the worker. Worker adds always worker JS dir to the path
        return APPLICATION_PATH + "/" + REMOTE_LOGGING + "/" + LOGGING_SIMPLE;
    }

    public static String getWorkerApplicationRoot() {
        // DOMAIN is needed in the worker. Worker adds always worker JS dir to the path
        return APPLICATION_PATH;
    }

    public static String loadShape3dBufferUrl() {
        return APPLICATION_PATH + "/" + SHAPE_3D_CONTROLLER + "/" + SHAPE_3D_PROVIDER_GET_VERTEX_BUFFER;
    }

    public static String terrainShapeController(int planetId) {
        return APPLICATION_PATH + "/" + TERRAIN_SHAPE_CONTROLLER + "/" + planetId;
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
        return APPLICATION_PATH + "/" + AUDIO_SERVICE_PATH + "/" + Integer.toString(id)/* + "?t=" + System.currentTimeMillis()*/;// TODO image cache
    }

    public static String generateVerificationLink(String verificationId) {
        return RAZARION_URL + EMAIL_VERIFICATION + "/" + verificationId;
    }

    public static String generateForgotPasswordLink(String uuid) {
        return RAZARION_URL + FORGOT_PASSWORD_CHANGE + "/" + uuid;
    }

}

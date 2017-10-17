package com.btxtech.persistence;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainEditorLoad;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.webglemulator.razarion.HttpConnectionEmu;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Singleton
public class JsonProviderEmulator {
    private static final String RAZARION_TMP_DIR = "C:\\dev\\projects\\razarion\\code\\tmp";
    private static final String FILE_NAME_TUTORIAL = "GameUiControlConfigTutorial.json";
    private static final String FILE_NAME_MULTI_PLAYER = "GameUiControlConfigMultiplayer.json";
    private static final String VERTEX_CONTAINER_BUFFERS_FILE_NAME = "VertexContainerBuffers.json";
    private static final String TMP_FILE_NAME = "TmpGameUiControlConfig.json";
    public static final String HTTP_LOCALHOST_8080 = "http://localhost:8080";
    private static final String PLANET_EDITOR_READ_SLOPS = HTTP_LOCALHOST_8080 + "/" + RestUrl.APPLICATION_PATH + "/" + RestUrl.PLANET_EDITOR_SERVICE_PATH + "/readTerrainSlopePositions";
    private static final String URL_GAME_UI_CONTROL = HTTP_LOCALHOST_8080 + "/" + RestUrl.APPLICATION_PATH + "/" + RestUrl.GAME_UI_CONTROL_PATH + "/" + RestUrl.COLD;
    private static final String URL_LOGIN = HTTP_LOCALHOST_8080;
    private static final String URL_TERRAIN_SHAPE = HTTP_LOCALHOST_8080 + "/" + RestUrl.APPLICATION_PATH + "/" + RestUrl.TERRAIN_SHAPE_PROVIDER + "/";
    private static final String URL_SLOPES_PROVIDER = HTTP_LOCALHOST_8080 + "/" + RestUrl.APPLICATION_PATH + "/" + RestUrl.PLANET_EDITOR_SERVICE_PATH + "/" + "readTerrainSlopePositions/";
    private static final String URL_VERTEX_CONTAINER_BUFFERS_FILE_NAME = HTTP_LOCALHOST_8080 + "/" + RestUrl.APPLICATION_PATH + "/" + RestUrl.SHAPE_3D_PROVIDER + "/" + RestUrl.SHAPE_3D_PROVIDER_GET_VERTEX_BUFFER;
    private static final String GAME_UI_CONTROL_INPUT = "{\"playbackGameSessionUuid\": null, \"playbackSessionUuid\": null}";
    private static final String FB_USER_ID_TEST = "100003634094139";

    public ColdGameUiControlConfig readFromServer() {
        return ClientBuilder.newClient().target(URL_GAME_UI_CONTROL).request(MediaType.APPLICATION_JSON).get(ColdGameUiControlConfig.class);
    }

    public ColdGameUiControlConfig readFromFile(boolean tutorial) {
        try {
            String string = new String(Files.readAllBytes(getFile(tutorial ? FILE_NAME_TUTORIAL : FILE_NAME_MULTI_PLAYER).toPath()));
            return new ObjectMapper().readValue(string, ColdGameUiControlConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<VertexContainerBuffer> readVertexContainerBuffersFromFile() {
        try {
            String string = new String(Files.readAllBytes(getFile(VERTEX_CONTAINER_BUFFERS_FILE_NAME).toPath()));
            return new ObjectMapper().readValue(string, new TypeReference<List<VertexContainerBuffer>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public StaticGameConfig readGameEngineConfigFromFile(String filename) {
        try {
            String string = new String(Files.readAllBytes(new File(filename).toPath()));
            return new ObjectMapper().readValue(string, StaticGameConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fromServerToFilePost(String fileName, String url, String fbLogin) {
        try {
            Client client = ClientBuilder.newClient();
            String text = client.target(url).request(MediaType.APPLICATION_JSON).post(Entity.entity(fbLogin, MediaType.APPLICATION_JSON_TYPE), String.class);
            Files.write(getFile(fileName).toPath(), text.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpConnectionEmu fromServer() {
        try {
            HttpConnectionEmu emu = new HttpConnectionEmu();
            Client client = ClientBuilder.newClient();
            client.register((ClientResponseFilter) (requestContext, responseContext) -> {
                for (Map.Entry<String, NewCookie> entry : responseContext.getCookies().entrySet()) {
                    if (entry.getKey().equals(HttpConnectionEmu.SESSION_KEY)) {
                        if (emu.getSessionCookie() == null) {
                            emu.setSessionCookie(entry.getValue());
                        }
                    }
                }
            });
            // client.register(new LoggingFilter(Logger.getLogger(JsonProviderEmulator.class.getName()), true));
            // Execute Get Page to read JSF form javax.faces.ViewState
            String homePage = client.target(URL_LOGIN).request(MediaType.TEXT_HTML).get(String.class);
            Document doc = Jsoup.parse(homePage);
            Element formFiled = doc.getElementById("j_id1:javax.faces.ViewState:0");
            String FormValueViewState = formFiled.attr("value");
            // Execute Login with jsession cookie and javax.faces.ViewState from above
            Form formData = new Form();
            formData.param("helperForm", "helperForm");
            formData.param("helperForm:fbUserIdField", FB_USER_ID_TEST);
            formData.param("helperForm:fbResponseFormButton", "Submit");
            formData.param("javax.faces.ViewState", FormValueViewState);
            client.target(URL_LOGIN).request(MediaType.TEXT_HTML).cookie(emu.getSessionCookie()).post(Entity.entity(formData, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
            // Execute Get with jsession cookie and GAME_UI_CONTROL
            String string = client.target(URL_GAME_UI_CONTROL).request(MediaType.APPLICATION_JSON).cookie(emu.getSessionCookie()).post(Entity.entity(GAME_UI_CONTROL_INPUT, MediaType.APPLICATION_JSON_TYPE), String.class);
            emu.setColdGameUiControlConfig(new ObjectMapper().readValue(string, ColdGameUiControlConfig.class));
            return emu;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NativeTerrainShape nativeTerrainShapeServer(int planetId) {
        try {
            Client client = ClientBuilder.newClient();
            String string = client.target(URL_TERRAIN_SHAPE + planetId).request(MediaType.APPLICATION_JSON).get(String.class);
            return new ObjectMapper().readValue(string, NativeTerrainShape.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TerrainSlopePosition> readSlopes(int planetId) {
        try {
            Client client = ClientBuilder.newClient();
            String string = client.target(URL_SLOPES_PROVIDER + planetId).request(MediaType.APPLICATION_JSON).get(String.class);
            return new ObjectMapper().readValue(string, new TypeReference<List<TerrainSlopePosition>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fromServerToFileGet(String fileName, String url) {
        try {
            Client client = ClientBuilder.newClient();
            String text = client.target(url).request(MediaType.APPLICATION_JSON).get(String.class);
            Files.write(getFile(fileName).toPath(), text.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void toFile(String fileName, Object object) {
        try {
            File file = getFile(fileName);
            new ObjectMapper().writeValue(file, object);
            System.out.println("Written config to: " + file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameUiControlConfigToTmpFile(Object object) {
        toFile(TMP_FILE_NAME, object);
    }

    public void fromServerToFile() {
        fromServerToFilePost(FILE_NAME_MULTI_PLAYER, URL_GAME_UI_CONTROL, GAME_UI_CONTROL_INPUT);
        fromServerToFilePost(FILE_NAME_TUTORIAL, URL_GAME_UI_CONTROL, GAME_UI_CONTROL_INPUT);
    }

    public void fromServerToFileVertexContainerBuffer() {
        fromServerToFileGet(VERTEX_CONTAINER_BUFFERS_FILE_NAME, URL_VERTEX_CONTAINER_BUFFERS_FILE_NAME);
    }

    private File getFile(String fileName) {
        return new File(RAZARION_TMP_DIR, fileName);
    }

    public static void dumpSlope(int planetId, DecimalPosition containingPosition) throws IOException {
        Client client = ClientBuilder.newClient();
        // Get slope corners
        TerrainEditorLoad terrainEditorLoad = client.target(PLANET_EDITOR_READ_SLOPS + "/" + planetId).request(MediaType.APPLICATION_JSON).get(TerrainEditorLoad.class);
        TerrainSlopePosition terrainSlopePosition = terrainEditorLoad.getSlopes().stream().filter(slope -> {
            Polygon2D polygon2D = new Polygon2D(slope.getPolygon().stream().map(TerrainSlopeCorner::getPosition).collect(Collectors.toList()));
            return polygon2D.isInside(containingPosition);
        }).findFirst().orElseThrow(() -> new IllegalArgumentException("Containing position not found in slopes: " + containingPosition));
        // Get slope shape
        String string = client.target(URL_GAME_UI_CONTROL).request(MediaType.APPLICATION_JSON).post(Entity.entity(GAME_UI_CONTROL_INPUT, MediaType.APPLICATION_JSON_TYPE), String.class);
        ColdGameUiControlConfig coldGameUiControlConfig = new ObjectMapper().readValue(string, ColdGameUiControlConfig.class);
        SlopeSkeletonConfig slopeSkeletonConfig = coldGameUiControlConfig.getStaticGameConfig().getSlopeSkeletonConfigs().stream().filter(config -> config.getId() == terrainSlopePosition.getSlopeConfigId()).findFirst().orElseThrow(() -> new IllegalArgumentException("SlopeSkeletonConfig not found for id: " + terrainSlopePosition.getSlopeConfigId()));
        ;
        // Dump
        System.out.println("// SlopeId: " + terrainSlopePosition.getId());
        System.out.println("// SlopeConfigId: " + terrainSlopePosition.getSlopeConfigId());
        System.out.println("// ---------------------------Slope shape---------------------------");
        System.out.println("List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();");
        System.out.println("SlopeSkeletonConfig skeletonConfig = new SlopeSkeletonConfig();");
        System.out.println("skeletonConfig.setId(" + slopeSkeletonConfig.getId() + ").setType(SlopeSkeletonConfig.Type." + slopeSkeletonConfig.getType() + ");");
        System.out.println("skeletonConfig.setRows(" + slopeSkeletonConfig.getRows() + ").setSegments(" + slopeSkeletonConfig.getSegments() + ").setWidth(" + slopeSkeletonConfig.getWidth() + ").setVerticalSpace(" + slopeSkeletonConfig.getVerticalSpace() + ").setHeight(" + slopeSkeletonConfig.getHeight() + ");");
        System.out.println("SlopeNode[][] slopeNodes = new SlopeNode[][]{");
        for (int x = 0; x < slopeSkeletonConfig.getSegments(); x++) {
            System.out.print("{");
            for (int y = 0; y < slopeSkeletonConfig.getRows(); y++) {
                SlopeNode slopeNode = slopeSkeletonConfig.getSlopeNode(x, y);
                System.out.print("createSlopeNode(" + slopeNode.getPosition().getX() + ", " + slopeNode.getPosition().getZ() + ", " + slopeNode.getSlopeFactor() + ")");
                if (y + 1 < slopeSkeletonConfig.getRows()) {
                    System.out.print(", ");
                }
            }
            System.out.print("}");
            if (x + 1 < slopeSkeletonConfig.getSegments()) {
                System.out.println(",");
            } else {
                System.out.println();
            }
        }
        System.out.println("};");
        System.out.println("skeletonConfig.setSlopeNodes(slopeNodes);");
        System.out.println("skeletonConfig.setInnerLineTerrainType(" + slopeSkeletonConfig.getInnerLineTerrainType() + ").setCoastDelimiterLineTerrainType(" + slopeSkeletonConfig.getCoastDelimiterLineTerrainType() + ").setOuterLineTerrainType(" + slopeSkeletonConfig.getOuterLineTerrainType() + ");");
        System.out.println("slopeSkeletonConfigs.add(skeletonConfig);");
        System.out.println("// ---------------------------Slope corners---------------------------");
        System.out.print("TerrainSlopeCorner[] terrainSlopeCorners = {");
        List<TerrainSlopeCorner> polygon = terrainSlopePosition.getPolygon();
        for (int i = 0; i < polygon.size(); i++) {
            TerrainSlopeCorner terrainSlopeCorner = polygon.get(i);
            System.out.print("createTerrainSlopeCorner(" + terrainSlopeCorner.getPosition().getX() + ", " + terrainSlopeCorner.getPosition().getY() + ", " + terrainSlopeCorner.getSlopeDrivewayId() + ")");
            if (i + 1 < polygon.size()) {
                System.out.print(", ");
            }
        }
        System.out.println("};");
        System.out.println("List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();");
        System.out.println("TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();");
        System.out.println("terrainSlopePosition.setId(" + terrainSlopePosition.getId() + ");");
        System.out.println("terrainSlopePosition.setSlopeConfigId(" + terrainSlopePosition.getSlopeConfigId() + ");");
        System.out.println("terrainSlopePosition.setPolygon(Arrays.asList(terrainSlopeCorners));");
        System.out.println("terrainSlopePositions.add(terrainSlopePosition);");
        System.out.println("// -----------------------------------------------------");
    }

    public static void main(String[] args) {
        try {
            dumpSlope(2, new DecimalPosition(360, 96));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

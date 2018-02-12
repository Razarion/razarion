package com.btxtech.persistence;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainEditorLoad;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.rest.FrontendProvider;
import com.btxtech.shared.rest.GameUiControlProvider;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.shared.rest.Shape3DProvider;
import com.btxtech.shared.rest.TerrainShapeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Singleton
public class JsonProviderEmulator {
    public static final String HOST_PORT = "192.168.99.100:32778/test";
    // public static final String HOST_PORT = "localhost:8080";
    public static final String HTTP_LOCALHOST_8080 = "http://" + HOST_PORT;
    public static final String REST = HTTP_LOCALHOST_8080 + "/rest/";
    private static final String PLANET_EDITOR_READ_SLOPS = HTTP_LOCALHOST_8080 + CommonUrl.APPLICATION_PATH + "/" + CommonUrl.PLANET_EDITOR_SERVICE_PATH + "/readTerrainSlopePositions";
    private static final String URL_GAME_UI_CONTROL = HTTP_LOCALHOST_8080 + CommonUrl.APPLICATION_PATH + "/" + CommonUrl.GAME_UI_CONTROL_PATH + "/" + CommonUrl.COLD;
    private static final String GAME_UI_CONTROL_INPUT = "{\"playbackGameSessionUuid\": null, \"playbackSessionUuid\": null}";
    private static final String FB_USER_ID_TEST = "100003634094139";
    private NewCookie sessionCookie;
    private Shape3DProvider shape3DProvider;
    private GameUiControlProvider coldGameUiControlConfig;
    private TerrainShapeProvider terrainShapeProvider;
    private PlanetEditorProvider planetEditorProvider;

    public JsonProviderEmulator() {
        Client client = ClientBuilder.newClient();
        ResteasyWebTarget target = (ResteasyWebTarget) client.target(REST);
        client.register((ClientResponseFilter) (requestContext, responseContext) -> {
            if (responseContext.getCookies().containsKey("JSESSIONID")) {
                sessionCookie = responseContext.getCookies().get("JSESSIONID");
            }
//            System.out.println("Request----------------------");
//            System.out.println("URI: " + requestContext.getUri());
//            System.out.println("Cookies: " + requestContext.getCookies());
//            System.out.println("Method: " + requestContext.getMethod());
//            System.out.println("Headers: " + requestContext.getStringHeaders());
//            System.out.println("Response----------------------");
//            System.out.println("Status: " + responseContext.getStatus());
//            System.out.println("----------------------");
        });
        client.register((ClientRequestFilter) (requestContext) -> {
            if (sessionCookie != null) {
                requestContext.getCookies().put("JSESSIONID", sessionCookie);
            }
        });
        target.proxy(FrontendProvider.class).loginUser("test", "test", false);
        shape3DProvider = target.proxy(Shape3DProvider.class);
        coldGameUiControlConfig = target.proxy(GameUiControlProvider.class);
        terrainShapeProvider = target.proxy(TerrainShapeProvider.class);
        planetEditorProvider = target.proxy(PlanetEditorProvider.class);
    }

    public List<VertexContainerBuffer> readVertexContainerBuffers() {
        return shape3DProvider.getVertexBuffer();
    }

    public ColdGameUiControlConfig readColdGameUiControlConfig() {
        return coldGameUiControlConfig.loadGameUiControlConfig(new GameUiControlInput());
    }

    public NativeTerrainShape nativeTerrainShapeServer(int planetId) {
        return terrainShapeProvider.getTerrainShape(planetId);
    }

    public List<TerrainSlopePosition> readSlopes(int planetId) {
        return planetEditorProvider.readTerrainEditorLoad(planetId).getSlopes();
    }

    public NewCookie getSessionCookie() {
        return sessionCookie;
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
                System.out.print("GameTestHelper.createSlopeNode(" + slopeNode.getPosition().getX() + ", " + slopeNode.getPosition().getZ() + ", " + slopeNode.getSlopeFactor() + ")");
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
        System.out.println("skeletonConfig.setInnerLineGameEngine(" + slopeSkeletonConfig.getInnerLineGameEngine() + ").setCoastDelimiterLineGameEngine(" + slopeSkeletonConfig.getCoastDelimiterLineGameEngine() + ").setOuterLineGameEngine(" + slopeSkeletonConfig.getOuterLineGameEngine() + ");");
        System.out.println("slopeSkeletonConfigs.add(skeletonConfig);");
        System.out.println("// ---------------------------Slope corners---------------------------");
        System.out.print("TerrainSlopeCorner[] terrainSlopeCorners = {");
        List<TerrainSlopeCorner> polygon = terrainSlopePosition.getPolygon();
        for (int i = 0; i < polygon.size(); i++) {
            TerrainSlopeCorner terrainSlopeCorner = polygon.get(i);
            System.out.print("GameTestHelper.createTerrainSlopeCorner(" + terrainSlopeCorner.getPosition().getX() + ", " + terrainSlopeCorner.getPosition().getY() + ", " + terrainSlopeCorner.getSlopeDrivewayId() + ")");
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
            dumpSlope(2, new DecimalPosition(595, 1016));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

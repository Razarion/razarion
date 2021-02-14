package com.btxtech.shared;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.TerrainEditorLoad;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.gui.userobject.InstanceStringGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Singleton
public class RestClientHelper {
    // public static final String HOST_PORT = "192.168.99.100:32778/test";
    // public static final String HOST_PORT = "www.razarion.com";
    public static final String HOST_PORT = "localhost:32778";
    public static final String HTTP_LOCALHOST_8080 = "http://" + HOST_PORT;
    public static final String REST = HTTP_LOCALHOST_8080 + "/rest/";
    private static final String PLANET_EDITOR_READ_SLOPS = HTTP_LOCALHOST_8080 + CommonUrl.APPLICATION_PATH + "/" + CommonUrl.PLANET_EDITOR_SERVICE_PATH + "/readTerrainSlopePositions";
    private static final String URL_GAME_UI_CONTROL = HTTP_LOCALHOST_8080 + CommonUrl.APPLICATION_PATH + "/" + CommonUrl.GAME_UI_CONTEXT_CONTROL_PATH + "/" + CommonUrl.COLD;
    private static final String GAME_UI_CONTROL_INPUT = "{\"playbackGameSessionUuid\": null, \"playbackSessionUuid\": null}";
    private static final String FB_USER_ID_TEST = "100003634094139";


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
        ColdGameUiContext coldGameUiContext = new ObjectMapper().readValue(string, ColdGameUiContext.class);
        SlopeConfig slopeConfig = coldGameUiContext.getStaticGameConfig().getSlopeConfigs().stream().filter(config -> config.getId() == terrainSlopePosition.getSlopeConfigId()).findFirst().orElseThrow(() -> new IllegalArgumentException("SlopeConfig not found for id: " + terrainSlopePosition.getSlopeConfigId()));
        // Dump
        System.out.println("// SlopeId: " + terrainSlopePosition.getId());
        System.out.println("// SlopeConfigId: " + terrainSlopePosition.getSlopeConfigId());
        System.out.println("// ---------------------------SlopeConfig---------------------------");
        System.out.println("List<SlopeConfig> slopeConfigs = Collections.singletonList(new SlopeConfig()");
        System.out.println("         .id(" + slopeConfig.getId() + ")");
        System.out.println("         .horizontalSpace(" + slopeConfig.getHorizontalSpace() + ")");
        System.out.println("         .groundConfigId(" + slopeConfig.getGroundConfigId() + ")");
        System.out.println("         .waterConfigId(" + slopeConfig.getWaterConfigId() + ")");
        System.out.println("         .outerLineGameEngine(" + slopeConfig.getOuterLineGameEngine() + ")");
        System.out.println("         .innerLineGameEngine(" + slopeConfig.getInnerLineGameEngine() + ")");
        System.out.println("         .slopeShapes(Arrays.asList(");
        System.out.println(slopeConfig.getSlopeShapes().stream().map(slopeShape -> {
            String result = "                 new SlopeShape()";
            if (slopeShape.getPosition() != null) {
                result += ".position(" + InstanceStringGenerator.generate(slopeShape.getPosition()) + ")";
            }
            result += ".slopeFactor(" + slopeShape.getSlopeFactor() + ")";
            return result;
        }).collect(Collectors.joining(",\n")) + ")));");


        System.out.println("// ---------------------------DrivewayConfig---------------------------");
        System.out.println("List<DrivewayConfig> drivewayConfigs = Arrays.asList(");
        System.out.println(coldGameUiContext.getStaticGameConfig().getDrivewayConfigs().stream().map(driveway -> "    new DrivewayConfig().id(" + driveway.getId() + ").angle(" + driveway.getAngle() + ")").collect(Collectors.joining(",\n")) + ");");
        System.out.println("// ---------------------------Slope corners---------------------------");
        System.out.println("List<TerrainSlopePosition> terrainSlopePositions = Collections.singletonList(new TerrainSlopePosition()");
        System.out.println("        .id(" + terrainSlopePosition.getId() + ")");
        System.out.println("        .slopeConfigId(" + terrainSlopePosition.getSlopeConfigId() + ")");
        System.out.println("        .polygon(Arrays.asList(");
        System.out.println(terrainSlopePosition
                .getPolygon()
                .stream()
                .map(terrainSlopeCorner -> "                 GameTestHelper.createTerrainSlopeCorner(" + terrainSlopeCorner.getPosition().getX() + ", " + terrainSlopeCorner.getPosition().getY() + ", " + terrainSlopeCorner.getSlopeDrivewayId() + ")")
                .collect(Collectors.joining(",\n")) + ")));");
        System.out.println("setupTerrainTypeService(slopeConfigs, drivewayConfigs, null, null, null, terrainSlopePositions, null, null);");
        System.out.println("showDisplay();");
    }

    public static void main(String[] args) {
        try {
            dumpSlope(117, new DecimalPosition(283, 409));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.btxtech.shared.dto.FallbackConfig.GROUND_CONFIG_ID;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class TerrainObjectServiceTest extends DaggerTerrainServiceTestBase {

    @Test
    @Ignore
    public void testTerrainObjectTileGeneration4Tiles() {
        List<GroundConfig> groundConfig = Collections.singletonList(
                new GroundConfig().id(GROUND_CONFIG_ID));

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).internalName("banana_plant").radius(1).model3DId(105));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).internalName("Rock1C").radius(1).model3DId(86));
        // terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5).threeJsModelPackConfigId(2));
        // terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10).threeJsModelPackConfigId(3));

        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().id(1).terrainObjectConfigId(1).position(new DecimalPosition(48, 40)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(Math.toRadians(0), 0, 0))
//                new TerrainObjectPosition().id(2).terrainObjectConfigId(1).position(new DecimalPosition(3, 11)).scale(new Vertex(1.3, 1.3, 1.3)).rotation(new Vertex(Math.toRadians(0), 0, 0)),
//                new TerrainObjectPosition().id(3).terrainObjectConfigId(1).position(new DecimalPosition(200, 10)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, Math.toRadians(90))),
//                new TerrainObjectPosition().id(4).terrainObjectConfigId(2).position(new DecimalPosition(8, 8)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, Math.toRadians(0))),
//                new TerrainObjectPosition().id(5).terrainObjectConfigId(3).position(new DecimalPosition(50, 10)).scale(new Vertex(0.5, 0.5, 0.5)).rotation(new Vertex(Math.toRadians(90), Math.toRadians(90), 0)),
//                new TerrainObjectPosition().id(6).terrainObjectConfigId(3).position(new DecimalPosition(60, 10)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, Math.toRadians(90), Math.toRadians(90))),
//                new TerrainObjectPosition().id(7).terrainObjectConfigId(3).position(new DecimalPosition(70, 10)).scale(new Vertex(0.9, 0.9, 0.9)).rotation(new Vertex(Math.toRadians(90), 0, Math.toRadians(90))),
//                new TerrainObjectPosition().id(8).terrainObjectConfigId(2).position(new DecimalPosition(200, 160)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, 0))
        );

        setupTerrainTypeService(null, terrainObjectConfigs, null, terrainObjectPositions, groundConfig);
        // showDisplay();

        exportTriangles("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-share\\src\\test\\resources\\com\\btxtech\\shared\\gameengine\\planet\\terrain",
                new Index(0, 0), new Index(1, 0), new Index(0, 1), new Index(1, 1));

        // AssertTerrainShape.assertTerrainShape(getClass(), "testTerrainObjectTileGeneration4TilesShape1.json", getTerrainShape());
        // AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testTerrainObjectTileGeneration4TilesHNT1.json");
        // AssertTerrainTile.assertTerrainTile(getClass(), "testTerrainObjectTileGeneration4Tiles1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }
}

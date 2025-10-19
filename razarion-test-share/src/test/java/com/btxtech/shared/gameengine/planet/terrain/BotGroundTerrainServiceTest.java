package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
import org.junit.Test;

import java.util.List;

/**
 * Created by Beat
 * 29.03.2017.
 */
public class BotGroundTerrainServiceTest extends DaggerTerrainServiceTestBase {
    @Test
    public void testGroundTileGeneration1() {
        BotGroundSlopeBox westSlopeBox = new BotGroundSlopeBox();
        westSlopeBox.height = 1.501573626336352;
        westSlopeBox.yRot = 0;
        westSlopeBox.zRot = 0.3839724354387525;
        westSlopeBox.xPos = 7.29126458173285;
        westSlopeBox.yPos = 15;

        BotGroundSlopeBox southSlopeBox = new BotGroundSlopeBox();
        southSlopeBox.height = 1.501573626336352;
        southSlopeBox.yRot = Math.toRadians(270);
        southSlopeBox.zRot = 0.3839724354387525;
        southSlopeBox.xPos = 15;
        southSlopeBox.yPos = 7.29126458173285;

        BotGroundSlopeBox eastSlopeBox = new BotGroundSlopeBox();
        eastSlopeBox.height = 1.501573626336352;
        eastSlopeBox.yRot = Math.toRadians(180);
        eastSlopeBox.zRot = 0.3839724354387525;
        eastSlopeBox.xPos = 22.70873541826715;
        eastSlopeBox.yPos = 15;

        BotGroundSlopeBox nordSlopeBox = new BotGroundSlopeBox();
        nordSlopeBox.height = 1.501573626336352;
        nordSlopeBox.yRot = Math.toRadians(90);
        nordSlopeBox.zRot = 0.3839724354387525;
        nordSlopeBox.xPos = 15;
        nordSlopeBox.yPos = 22.70873541826715;


        List<BotConfig> botConfigs = List.of(new BotConfig()
                .groundBoxHeight(3.0)
                .groundBoxModel3DEntityId(1)
                .groundBoxPositions(List.of(new DecimalPosition(15, 15)))
                .botGroundSlopeBoxes(List.of(westSlopeBox, eastSlopeBox, southSlopeBox, nordSlopeBox))
        );

        setupTerrainTypeService(null, null, null, null, null, botConfigs);

        showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testGroundShapeGeneration1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testGroundShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testGroundTileGeneration1.json", generateTerrainTiles(new Index(0, 0)));

    }
}
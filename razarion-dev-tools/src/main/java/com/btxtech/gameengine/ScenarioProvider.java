package com.btxtech.gameengine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 19.09.2016.
 */
public class ScenarioProvider {
    private BaseItemService baseItemService;
    private ResourceService resourceService;
    private PlayerBase playerBase;
    private int slopeId = 1;

    // Override in subclasses
    protected void createSyncItems() {

    }

    // Override in subclasses
    public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {

    }

    // Override in subclasses
    public void setupBots(Collection<BotConfig> botConfigs) {

    }

    // Override in subclasses
    public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {

    }

    public void setupSyncItems(BaseItemService baseItemService, PlayerBase playerBase, ResourceService resourceService) {
        this.baseItemService = baseItemService;
        this.playerBase = playerBase;
        this.resourceService = resourceService;
        createSyncItems();
    }

    protected void createSyncBaseItem(BaseItemType baseItemType, DecimalPosition position, DecimalPosition destination) {
        try {
            SyncBaseItem syncBaseItem = baseItemService.spawnSyncBaseItem(baseItemType, position, playerBase, true);
            if (syncBaseItem.getSyncPhysicalArea().canMove()) {
                ((SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea()).setDestination(destination);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void createSyncResourceItem(ResourceItemType resourceItemType, DecimalPosition position) {
        try {
            resourceService.createResources(Collections.singletonList(new ResourceItemPosition().setResourceItemTypeId(resourceItemType.getId()).setPosition(position)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected TerrainSlopePosition createRectangleSlope(int slopeSkeletonId, int x, int y, int width, int height) {
        return new TerrainSlopePosition().setId(slopeId++).setSlopeId(slopeSkeletonId).setPolygon(Arrays.asList(new Index(x, y), new Index(x + width, y), new Index(x + width, y + height), new Index(x, y + height)));
    }

}

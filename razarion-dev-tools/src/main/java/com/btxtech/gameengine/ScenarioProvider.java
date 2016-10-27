package com.btxtech.gameengine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.utils.CollectionUtils;

import java.util.ArrayList;
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
    private BoxService boxService;
    private PlayerBase playerBase;
    private int slopeId = 1;
    private List<SyncBaseItem> createdSyncBaseItems = new ArrayList<>();
    private List<SyncBoxItem> createdSyncBoxItems = new ArrayList<>();
    private BotService botService;

    // Override in subclasses
    protected void createSyncItems() {

    }

    // Override in subclasses
    public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {

    }

    // Override in subclasses
    protected void setupBots(Collection<BotConfig> botConfigs) {

    }

    // Override in subclasses
    public void executeCommands(CommandService commandService) {

    }

    // Override in subclasses
    public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {

    }

    public void setupSyncItems(BaseItemService baseItemService, PlayerBase playerBase, ResourceService resourceService, BoxService boxService) {
        this.baseItemService = baseItemService;
        this.playerBase = playerBase;
        this.resourceService = resourceService;
        this.boxService = boxService;
        createSyncItems();
    }

    protected void createSyncBaseItem(BaseItemType baseItemType, DecimalPosition position, DecimalPosition destination) {
        try {
            SyncBaseItem syncBaseItem = baseItemService.spawnSyncBaseItem(baseItemType, position, playerBase, true);
            if (syncBaseItem.getSyncPhysicalArea().canMove() && destination != null) {
                ((SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea()).setDestination(destination);
            }
            createdSyncBaseItems.add(syncBaseItem);
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

    protected void createBoxItem(BoxItemType boxItemType, DecimalPosition position) {
        try {
            createdSyncBoxItems.add(boxService.dropBox(boxItemType.getId(), position, 0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setupBots(BotService botService) {
        this.botService = botService;
        Collection<BotConfig> botConfigs = new ArrayList<>();
        setupBots(botConfigs);
        botService.startBots(botConfigs);
    }

    protected Collection<SyncBaseItem> getBotItem(int botId) {
        return botService.getBotRunner(botId).getBase().getItems();
    }

    protected SyncBaseItem getFirstBotItem(int botId) {
        return CollectionUtils.getFirst(getBotItem(botId));
    }

    protected TerrainSlopePosition createRectangleSlope(int slopeSkeletonId, double x, double y, double width, double height) {
        return new TerrainSlopePosition().setId(slopeId++).setSlopeId(slopeSkeletonId).setPolygon(Arrays.asList(new DecimalPosition(x, y), new DecimalPosition(x + width, y), new DecimalPosition(x + width, y + height), new DecimalPosition(x, y + height)));
    }

    protected SyncBaseItem getCreatedSyncBaseItems(int index) {
        return createdSyncBaseItems.get(index);
    }

    protected SyncBaseItem getFirstCreatedSyncBaseItem() {
        return getCreatedSyncBaseItems(0);
    }

    protected SyncBoxItem getCreatedSyncBoxItems(int index) {
        return createdSyncBoxItems.get(index);
    }

    protected SyncBoxItem getFirstCreatedSyncBoxItem() {
        return getCreatedSyncBoxItems(0);
    }
}

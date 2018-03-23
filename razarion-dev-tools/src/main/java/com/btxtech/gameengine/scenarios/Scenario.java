package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
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
public class Scenario {
    private String name;
    private BaseItemService baseItemService;
    private ResourceService resourceService;
    private BoxService boxService;
    private SyncItemContainerService syncItemContainerService;
    private PathingService pathingService;
    private PlayerBaseFull playerBase;
    private int slopeId = 1;
    private List<SyncBaseItem> createdSyncBaseItems = new ArrayList<>();
    private List<SyncResourceItem> createdSyncResourceItems = new ArrayList<>();
    private List<SyncBoxItem> createdSyncBoxItems = new ArrayList<>();
    private BotService botService;
    private ScenarioSuite scenarioSuite;

    public Scenario(String name) {
        this.name = name;
    }

    public void setScenarioSuite(ScenarioSuite scenarioSuite) {
        this.scenarioSuite = scenarioSuite;
    }

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

    // Override in subclasses
    public void setupResourceRegionConfig(List<ResourceRegionConfig> resourceRegionConfigs) {

    }

    // Override in subclasses
    public QuestConfig setupQuest() {
        return null;
    }

    // Override in subclasses
    public StaticGameConfig setupGameEngineConfig() {
        return null;
    }

    // Override in subclasses
    public void stop() {

    }

    // Override in subclasses
    public boolean isStart() {
        return false;
    }

    public PlayerBaseFull getPlayerBase() {
        return playerBase;
    }

    public void setupSyncItems(BaseItemService baseItemService, PlayerBaseFull playerBase, ResourceService resourceService, BoxService boxService, PathingService pathingService, SyncItemContainerService syncItemContainerService) {
        this.baseItemService = baseItemService;
        this.playerBase = playerBase;
        this.resourceService = resourceService;
        this.boxService = boxService;
        this.pathingService = pathingService;
        this.syncItemContainerService = syncItemContainerService;
        createSyncItems();
    }

    protected SyncBaseItem createSyncBaseItem(BaseItemType baseItemType, DecimalPosition position, double angle, DecimalPosition destination) {
        try {
            SyncBaseItem syncBaseItem = baseItemService.spawnSyncBaseItem(baseItemType, position, angle, playerBase, true);
            if (syncBaseItem.getSyncPhysicalArea().canMove() && destination != null) {
                SimplePath path = new SimplePath();
                path.setWayPositions(Collections.singletonList(destination));
                ((SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea()).setPath(path);
            }
            createdSyncBaseItems.add(syncBaseItem);
            return syncBaseItem;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SyncBaseItem createSyncBaseItemAStar(BaseItemType baseItemType, DecimalPosition position, double angle, DecimalPosition destination) {
        try {
            SyncBaseItem syncBaseItem = baseItemService.spawnSyncBaseItem(baseItemType, position, angle, playerBase, true);
            if (syncBaseItem.getSyncPhysicalArea().canMove() && destination != null) {
                SimplePath path = pathingService.setupPathToDestination(syncBaseItem, destination);
                ((SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea()).setPath(path);
            }
            createdSyncBaseItems.add(syncBaseItem);
            return syncBaseItem;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void createSyncResourceItem(ResourceItemType resourceItemType, DecimalPosition position) {
        try {
            createdSyncResourceItems.add(resourceService.createResource(resourceItemType.getId(), position, 0));
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
        botService.startBots(botConfigs, null);
    }

    protected Collection<SyncBaseItem> getBotItem(int botId) {
        return botService.getBotRunner(botId).getBase().getItems();
    }

    protected SyncBaseItem getFirstBotItem(int botId) {
        return CollectionUtils.getFirst(getBotItem(botId));
    }

    protected TerrainSlopePosition createRectangleSlope(int slopeSkeletonId, double x, double y, double width, double height) {
        return new TerrainSlopePosition().setId(slopeId++).setSlopeConfigId(slopeSkeletonId).setPolygon(Arrays.asList(
                new TerrainSlopeCorner().setPosition(new DecimalPosition(x, y)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(x + width, y)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(x + width, y + height)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(x, y + height))));
    }

    protected SyncBaseItem getCreatedSyncBaseItem(int index) {
        return createdSyncBaseItems.get(index);
    }

    protected SyncBaseItem getFirstCreatedSyncBaseItem() {
        return getCreatedSyncBaseItem(0);
    }

    protected SyncBaseItem getSecondCreatedSyncBaseItem() {
        return getCreatedSyncBaseItem(1);
    }

    protected SyncBoxItem getCreatedSyncBoxItem(int index) {
        return createdSyncBoxItems.get(index);
    }

    protected SyncBoxItem getFirstCreatedSyncBoxItem() {
        return getCreatedSyncBoxItem(0);
    }

    protected SyncResourceItem getCreatedSyncResourceItem(int index) {
        return createdSyncResourceItems.get(index);
    }

    protected SyncResourceItem getFirstCreatedSyncResourceItem() {
        return getCreatedSyncResourceItem(0);
    }

    protected SyncResourceItem getSyncResourceItem(int resourceItemTypeId) {
        return CollectionUtils.getFirst(syncItemContainerService.findResourceItemWithPlace(resourceItemTypeId, new PlaceConfig() {
            @Override
            public boolean checkInside(SyncItem syncItem) {
                return true;
            }
        }));
    }

    @Override
    public String toString() {
        return scenarioSuite.getName() + "->" + name;
    }
}

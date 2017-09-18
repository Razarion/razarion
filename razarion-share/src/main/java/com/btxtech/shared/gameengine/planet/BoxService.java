package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CommonUtils;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 16.07.2016.
 */
@Singleton
public class BoxService {
    private static final long TICK_TO_SLEEP_MS = 10 * PlanetService.TICKS_PER_SECONDS;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private InventoryTypeService inventoryTypeService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private GameEngineMode gameEngineMode;
    private final Map<Integer, SyncBoxItem> boxes = new HashMap<>();
    private Collection<BoxRegion> boxRegion;
    private long ticksSinceLastCheck;

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        switch (planetActivationEvent.getType()) {
            case INITIALIZE:
                setup(planetActivationEvent);
                break;
            case STOP:
                stop();
                break;
            default:
                throw new IllegalArgumentException("BoxService.onPlanetActivation() can not handle: " + planetActivationEvent.getType());
        }
    }

    private void setup(PlanetActivationEvent planetActivationEvent) {
        synchronized (boxes) {
            boxes.clear();
        }
        gameEngineMode = planetActivationEvent.getGameEngineMode();
        if (planetActivationEvent.getSlaveSyncItemInfo() != null && planetActivationEvent.getSlaveSyncItemInfo().getSyncBoxItemInfos() != null) {
            planetActivationEvent.getSlaveSyncItemInfo().getSyncBoxItemInfos().forEach(this::createSyncBoxItemSlave);
        }
    }

    private void stop() {
        synchronized (boxes) {
            boxes.clear();
        }
        boxRegion = null;
    }

    public void startBoxRegions(Collection<BoxRegionConfig> boxRegionConfigs) {
        boxRegion = boxRegionConfigs.stream().map(BoxRegion::new).collect(Collectors.toList());
        ticksSinceLastCheck = 0;
    }

    public void dropBoxes(List<BoxItemPosition> boxItemPositions) {
        for (BoxItemPosition boxItemPosition : boxItemPositions) {
            dropBox(boxItemPosition.getBoxItemTypeId(), boxItemPosition.getPosition(), boxItemPosition.getRotationZ());
        }
    }

    public SyncBoxItem dropBox(Integer boxItemTypeId, DecimalPosition position2d, double zRotation) {
        BoxItemType boxItemType = itemTypeService.getBoxItemType(boxItemTypeId);
        SyncBoxItem syncBoxItem = syncItemContainerService.createSyncBoxItem(boxItemType, position2d, zRotation);
        syncBoxItem.setup(CommonUtils.valueOrDefault(boxItemType.getTtl(), Integer.MAX_VALUE) * PlanetService.TICKS_PER_SECONDS);
        synchronized (boxes) {
            boxes.put(syncBoxItem.getId(), syncBoxItem);
        }
        gameLogicService.onBoxCreated(syncBoxItem);
        return syncBoxItem;
    }

    public void onSyncBoxItemPicked(SyncBoxItem box, SyncBaseItem picker) {
        if (gameEngineMode != GameEngineMode.MASTER) {
            return;
        }

        removeSyncBox(box);
        if (picker.getBase().isAbandoned()) {
            gameLogicService.onBoxDeletedSlave(box);
            return;
        }

        BoxContent boxContent = new BoxContent();
        box.getBoxItemType().getBoxItemTypePossibilities().stream().filter(boxItemTypePossibility -> MathHelper.isRandomPossibility(boxItemTypePossibility.getPossibility())).forEach(boxItemTypePossibility -> setupBoxContent(boxItemTypePossibility, boxContent));

        gameLogicService.onBoxPicked(box, picker, boxContent);
    }

    public void onSlaveSyncBoxItemChanged(SyncBoxItemInfo syncBoxItemInfo) {
        SyncBoxItem syncBoxItem = boxes.get(syncBoxItemInfo.getId());
        if (syncBoxItem == null) {
            createSyncBoxItemSlave(syncBoxItemInfo);
        } else {
            throw new IllegalArgumentException("BoxService.onSlaveSyncBoxItemChanged() SyncBoxItem already exists: " + syncBoxItemInfo);
        }
    }

    private void createSyncBoxItemSlave(SyncBoxItemInfo syncBoxItemInfo) {
        BoxItemType boxItemType = itemTypeService.getBoxItemType(syncBoxItemInfo.getBoxItemTypeId());
        SyncBoxItem syncBoxItem = syncItemContainerService.createSyncBoxItemSlave(boxItemType, syncBoxItemInfo.getId(), syncBoxItemInfo.getSyncPhysicalAreaInfo().getPosition(), syncBoxItemInfo.getSyncPhysicalAreaInfo().getAngle());
        synchronized (boxes) {
            boxes.put(syncBoxItem.getId(), syncBoxItem);
        }
        gameLogicService.onBoxCreated(syncBoxItem);
    }

    public void removeSyncBoxSlave(SyncBoxItem box) {
        removeSyncBox(box);
        gameLogicService.onBoxDeletedSlave(box);
    }


    private void removeSyncBox(SyncBoxItem box) {
        box.kill();
        syncItemContainerService.destroySyncItem(box);
        synchronized (boxes) {
            boxes.remove(box.getId());
        }
    }

    private void setupBoxContent(BoxItemTypePossibility boxItemTypePossibility, BoxContent boxContent) {
        if (boxItemTypePossibility.getInventoryItemId() != null) {
            InventoryItem inventoryItem = inventoryTypeService.getInventoryItem(boxItemTypePossibility.getInventoryItemId());
            boxContent.addInventoryItem(inventoryItem);
//            gameLogicService.onInventoryItemFromBox(userContext, syncBoxItem, boxItemTypePossibility);
//        } else if (boxItemTypePossibility.getDbInventoryArtifact() != null) {
//            userContext.addInventoryArtifact(boxItemTypePossibility.getDbInventoryArtifact().getId());
//            serverConditionService.onArtifactItemAdded(userContext, true, boxItemTypePossibility.getDbInventoryArtifact().getId());
//            historyService.addInventoryArtifactFromBox(userContext, boxItemTypePossibility.getDbInventoryArtifact().getName());
//            builder.append("Artifact: ").append(boxItemTypePossibility.getDbInventoryArtifact().getName());
        } else if (boxItemTypePossibility.getCrystals() != null) {
            boxContent.addCrystals(boxItemTypePossibility.getCrystals());
        } else {
            throw new IllegalArgumentException("Can not handle boxItemTypePossibility: " + boxItemTypePossibility);
        }
    }

    public SyncBoxItem getSyncBoxItem(int id) {
        SyncBoxItem syncBoxItem = boxes.get(id);
        if (syncBoxItem == null) {
            throw new ItemDoesNotExistException(id);
        }
        return syncBoxItem;
    }

    public void tick() {
        if (boxRegion == null) {
            return;
        }
        ticksSinceLastCheck++;
        if (ticksSinceLastCheck < TICK_TO_SLEEP_MS) {
            return;
        }
        ticksSinceLastCheck = 0;
        Collection<SyncBoxItem> boxesToRemove;
        synchronized (boxes) {
            boxesToRemove = boxes.values().stream().filter(syncBoxItem -> !syncBoxItem.tickTtl((int) TICK_TO_SLEEP_MS)).collect(Collectors.toList());
        }
        boxesToRemove.forEach(syncBoxItem -> {
            gameLogicService.onBoxDeleted(syncBoxItem);
            removeSyncBox(syncBoxItem);
        });
        boxRegion.forEach(this::handleBoxRegion);
    }

    private void handleBoxRegion(BoxRegion boxRegion) {
        if (boxRegion.tick(TICK_TO_SLEEP_MS)) {
            try {
                dropRegionBoxes(boxRegion);
                boxRegion.setupNextDropTime();
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        }
    }

    private void dropRegionBoxes(BoxRegion boxRegion) {
        BoxItemType boxItemType = itemTypeService.getBoxItemType(boxRegion.getBoxRegionConfig().getBoxItemTypeId());
        for (int i = 0; i < boxRegion.getBoxRegionConfig().getCount(); i++) {
            DecimalPosition position = syncItemContainerService.getFreeRandomPosition(boxItemType.getRadius() + boxRegion.getBoxRegionConfig().getMinDistanceToItems(), boxRegion.getBoxRegionConfig().getRegion());
            dropBox(boxItemType.getId(), position, MathHelper.getRandomAngle());
        }
    }

    public List<SyncBoxItemInfo> getSyncBoxItemInfos() {
        List<SyncBoxItemInfo> syncBoxItemInfos = new ArrayList<>();
        synchronized (boxes) {
            for (SyncBoxItem syncBoxItem : boxes.values()) {
                syncBoxItemInfos.add(syncBoxItem.getSyncInfo());
            }
        }
        return syncBoxItemInfos;
    }
}

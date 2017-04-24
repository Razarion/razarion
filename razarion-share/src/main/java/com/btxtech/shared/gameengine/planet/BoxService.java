package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.gameengine.InventoryService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 16.07.2016.
 */
@Singleton
public class BoxService {
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private InventoryService inventoryService;
    private final Map<Integer, SyncBoxItem> boxes = new HashMap<>();

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        synchronized (boxes) {
            boxes.clear();
        }
    }

    public void dropBoxes(List<BoxItemPosition> boxItemPositions) {
        for (BoxItemPosition boxItemPosition : boxItemPositions) {
            dropBox(boxItemPosition.getBoxItemTypeId(), boxItemPosition.getPosition(), boxItemPosition.getRotationZ());
        }
    }

    public SyncBoxItem dropBox(int boxItemTypeId, DecimalPosition position2d, double zRotation) {
        BoxItemType boxItemType = itemTypeService.getBoxItemType(boxItemTypeId);
        SyncBoxItem syncBoxItem = syncItemContainerService.createSyncBoxItem(boxItemType, position2d, zRotation);
        synchronized (boxes) {
            boxes.put(syncBoxItem.getId(), syncBoxItem);
        }
        syncBoxItem.setup();
        gameLogicService.onBoxCreated(syncBoxItem);
        return syncBoxItem;
    }

    public void onSyncBoxItemPicked(SyncBoxItem box, SyncBaseItem picker) {
        removeSyncBox(box);
        if (picker.getBase().isAbandoned()) {
            gameLogicService.onBoxDeletedSlave(box);
            return;
        }

        BoxContent boxContent = new BoxContent();
        box.getBoxItemType().getBoxItemTypePossibilities().stream().filter(boxItemTypePossibility -> MathHelper.isRandomPossibility(boxItemTypePossibility.getPossibility())).forEach(boxItemTypePossibility -> setupBoxContent(boxItemTypePossibility, boxContent));

        gameLogicService.onBoxPicket(box, picker, boxContent);
    }

    public void removeSyncBoxSlave(SyncBoxItem box) {
        removeSyncBox(box);
        gameLogicService.onBoxDeletedSlave(box);
    }


    private void removeSyncBox(SyncBoxItem box) {
        syncItemContainerService.destroySyncItem(box);
        synchronized (boxes) {
            boxes.remove(box.getId());
        }
    }

    private void setupBoxContent(BoxItemTypePossibility boxItemTypePossibility, BoxContent boxContent) {
        if (boxItemTypePossibility.getInventoryItemId() != null) {
            InventoryItem inventoryItem = inventoryService.getInventoryItem(boxItemTypePossibility.getInventoryItemId());
            boxContent.addInventoryItem(inventoryItem);
//            gameLogicService.onInventoryItemFromBox(userContext, syncBoxItem, boxItemTypePossibility);
//        } else if (boxItemTypePossibility.getDbInventoryArtifact() != null) {
//            userContext.addInventoryArtifact(boxItemTypePossibility.getDbInventoryArtifact().getId());
//            serverConditionService.onArtifactItemAdded(userContext, true, boxItemTypePossibility.getDbInventoryArtifact().getId());
//            historyService.addInventoryArtifactFromBox(userContext, boxItemTypePossibility.getDbInventoryArtifact().getName());
//            builder.append("Artifact: ").append(boxItemTypePossibility.getDbInventoryArtifact().getName());
//        } else if (boxItemTypePossibility.getCrystals() != null) {
//            userContext.addCrystals(boxItemTypePossibility.getCrystals());
//            historyService.addCrystalsFromBox(userContext, boxItemTypePossibility.getCrystals());
//            serverConditionService.onCrystalsIncreased(userContext, true, boxItemTypePossibility.getCrystals());
//            builder.append("Crystals: ").append(boxItemTypePossibility.getCrystals());
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
        // TODO check TTL
    }
}

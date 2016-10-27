package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
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
import java.util.ArrayList;
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
    private ActivityService activityService;
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

    public SyncBoxItem dropBox(int boxItemTypeId, DecimalPosition position, double zRotation) {
        BoxItemType boxItemType = itemTypeService.getBoxItemType(boxItemTypeId);
        Vertex vertex = terrainService.calculatePositionGroundMesh(position);
        SyncBoxItem syncBoxItem = syncItemContainerService.createSyncBoxItem(boxItemType, vertex, zRotation);
        synchronized (boxes) {
            boxes.put(syncBoxItem.getId(), syncBoxItem);
        }
        syncBoxItem.setup();
        activityService.onBoxCreated(syncBoxItem);
        return syncBoxItem;
    }

    public void onSyncBoxItemPicked(SyncBoxItem box, SyncBaseItem picker) {
        syncItemContainerService.destroySyncItem(box);
        synchronized (boxes) {
            boxes.remove(box.getId());
        }
        if (picker.getBase().isAbandoned()) {
            return;
        }

        BoxContent boxContent = new BoxContent();
        box.getBoxItemType().getBoxItemTypePossibilities().stream().filter(boxItemTypePossibility -> MathHelper.isRandomPossibility(boxItemTypePossibility.getPossibility())).forEach(boxItemTypePossibility -> {
            addBoxContentToUser(boxItemTypePossibility, picker.getBase().getUserContext(), boxContent);
        });

        activityService.onBoxPicket(box, picker, boxContent);
    }

    private void addBoxContentToUser(BoxItemTypePossibility boxItemTypePossibility, UserContext userContext, BoxContent boxContent) {
        if (boxItemTypePossibility.getInventoryItemId() != null) {
            InventoryItem inventoryItem = inventoryService.getInventoryItem(boxItemTypePossibility.getInventoryItemId());
            userContext.addInventoryItem(inventoryItem.getId());
            boxContent.addInventoryItem(inventoryItem);
//            activityService.onInventoryItemFromBox(userContext, syncBoxItem, boxItemTypePossibility);
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

    public List<ModelMatrices> provideModelMatrices(BoxItemType boxItemType) {
        List<ModelMatrices> modelMatrices = new ArrayList<>();
        synchronized (boxes) {
            for (SyncBoxItem syncBoxItem : boxes.values()) {
                if (!syncBoxItem.getItemType().equals(boxItemType)) {
                    continue;
                }
                modelMatrices.add(syncBoxItem.createModelMatrices());
            }
        }
        return modelMatrices;
    }
}

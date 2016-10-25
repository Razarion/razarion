package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

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
    private final Map<Integer, SyncBoxItem> boxes = new HashMap<>();

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        synchronized (boxes) {
            boxes.clear();
        }
    }

    public void dropBox(BoxItemPosition boxItemPosition) {
        BoxItemType boxItemType = itemTypeService.getBoxItemType(boxItemPosition.getBoxItemTypeId());
        Vertex position = terrainService.calculatePositionGroundMesh(boxItemPosition.getPosition());
        SyncBoxItem syncBoxItem = syncItemContainerService.createSyncBoxItem(boxItemType, position, boxItemPosition.getRotationZ());
        synchronized (boxes) {
            boxes.put(syncBoxItem.getId(), syncBoxItem);
        }
        activityService.onBoxCreated(syncBoxItem);
    }

    public void onSyncBoxItemPicked(SyncBoxItem box, SyncBaseItem picker) {
        // TODO
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

package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.SpawnItemType;
import com.btxtech.shared.gameengine.datatypes.syncobject.SyncSpawnItem;
import com.btxtech.shared.gameengine.planet.SpawnItemService;
import com.btxtech.uiservice.renderer.PreRenderEvent;
import com.btxtech.uiservice.renderer.RenderServiceInitEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 26.07.2016.
 */
@Singleton
public class SpawnItemUiService {
    @Inject
    private SpawnItemService spawnItemService;
    @Inject
    private ItemTypeService itemTypeService;
    private Map<Integer, VertexContainer> vertexContainers;
    private Map<Integer, Collection<ModelMatrices>> spawnItemTypeIdModelMatrices;

    public void onRenderServiceInitEvent(@Observes RenderServiceInitEvent renderServiceInitEvent) {
        // Setup render data
        vertexContainers = new HashMap<>();
        for (SpawnItemType spawnItemType : itemTypeService.getItemTypes(SpawnItemType.class)) {
            vertexContainers.put(spawnItemType.getId(), spawnItemType.getVertexContainer());
        }
    }

    public void onPreRenderEvent(@Observes PreRenderEvent preRenderEvent) {
        // Setup model matrices
        spawnItemTypeIdModelMatrices = new HashMap<>();
        for (SyncSpawnItem syncSpawnItem : spawnItemService.getSyncSpawnItems()) {
            Collection<ModelMatrices> itemTypeMatrices = spawnItemTypeIdModelMatrices.get(syncSpawnItem.getItemType().getId());
            if (itemTypeMatrices == null) {
                itemTypeMatrices = new ArrayList<>();
                spawnItemTypeIdModelMatrices.put(syncSpawnItem.getItemType().getId(), itemTypeMatrices);
            }
            itemTypeMatrices.add(syncSpawnItem.createModelMatrices());
        }
    }

    public Collection<SpawnItemType> getSpawnItemType() {
        return itemTypeService.getItemTypes(SpawnItemType.class);
    }

    public Collection<ModelMatrices> provideModelMatrices(SpawnItemType spawnItemType) {
        return spawnItemTypeIdModelMatrices.get(spawnItemType.getId());
    }

    public VertexContainer getSpawnItemTypeVertexContainer(int id) {
        return vertexContainers.get(id);
    }
}

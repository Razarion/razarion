package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.ItemLifecycle;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.uiservice.ImageDescriptor;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 28.12.2015.
 * *
 */
@Singleton
public class BaseItemUiService {
    // private Logger logger = Logger.getLogger(BaseItemUiService.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private ResourceService resourceService;
    private ImageDescriptor imageDescriptor = ImageDescriptor.UNIT_TEXTURE_O1;
    @Deprecated
    private double specularIntensity = 0.1;
    @Deprecated
    private double specularHardness = 10;

    @Deprecated
    public VertexContainer getItemTypeVertexContainer(int id) {
        throw new UnsupportedOperationException();
        // return vertexContainers.get(id);
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public double getSpecularIntensity() {
        return specularIntensity;
    }

    public void setSpecularIntensity(double specularIntensity) {
        this.specularIntensity = specularIntensity;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public void setSpecularHardness(double specularHardness) {
        this.specularHardness = specularHardness;
    }


    public Collection<BaseItemType> getBaseItemTypes() {
        return itemTypeService.getBaseItemTypes();
    }

    public List<ModelMatrices> provideSpawnModelMatrices(BaseItemType baseItemType) {
        return baseItemService.getItemLifecycleBaseItems(ItemLifecycle.SPAWN).stream().filter(syncBaseItem -> syncBaseItem.getBaseItemType().equals(baseItemType)).map(syncBaseItem -> syncBaseItem.createModelMatrices().setProgress(syncBaseItem.getSpawnProgress())).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<ModelMatrices> provideAliveModelMatrices(BaseItemType baseItemType) {
        return baseItemService.getItemLifecycleBaseItems(ItemLifecycle.ALIVE).stream().filter(syncBaseItem -> syncBaseItem.getBaseItemType().equals(baseItemType)).map(SyncItem::createModelMatrices).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<ModelMatrices> provideHarvestAnimationModelMatrices(BaseItemType baseItemType) {
        Collection<SyncBaseItem> harvesters = syncItemContainerService.getSyncBaseItems4BaseItemType(baseItemType);
        List<ModelMatrices> modelMatrices = new ArrayList<>();
        for (SyncBaseItem harvester : harvesters) {
            if (!harvester.getSyncHarvester().isHarvesting()) {
                continue;
            }
            Vertex origin = harvester.getSyncPhysicalArea().createModelMatrices().getModel().multiply(baseItemType.getHarvesterType().getAnimationOrigin(), 1.0);
            SyncResourceItem syncResourceItem = resourceService.getSyncResourceItem(harvester.getSyncHarvester().getTarget());
            Vertex direction = syncResourceItem.getSyncPhysicalArea().getPosition().sub(origin).normalize(1.0);
            modelMatrices.add(ModelMatrices.createFromPositionAndDirection(origin, direction));
        }
        return modelMatrices;
    }
}

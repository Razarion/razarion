package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.uiservice.ImageDescriptor;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;

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
    private ImageDescriptor imageDescriptor = ImageDescriptor.UNIT_TEXTURE_O1;
    private double specularIntensity = 0.1;
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
        return itemTypeService.getItemTypes(BaseItemType.class);
    }

    public Collection<ModelMatrices> provideSpawnModelMatrices() {
        Collection<ModelMatrices> modelMatrices = new ArrayList<>();
        for (SyncBaseItem syncBaseItem : baseItemService.getBeamingSyncBaseItems()) {
            modelMatrices.add(syncBaseItem.createModelMatrices());
        }
        return modelMatrices;
    }

    public Collection<ModelMatrices> provideAliveModelMatrices() {
        Collection<ModelMatrices> modelMatrices = new ArrayList<>();
        for (SyncBaseItem syncBaseItem : baseItemService.getAliveSyncBaseItems()) {
            modelMatrices.add(syncBaseItem.createModelMatrices());
        }
        return modelMatrices;
    }
}

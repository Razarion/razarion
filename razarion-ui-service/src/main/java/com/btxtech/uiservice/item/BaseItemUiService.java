package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.syncobject.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.uiservice.ImageDescriptor;
import com.btxtech.uiservice.renderer.ModelMatricesProvider;
import com.btxtech.uiservice.renderer.PreRenderEvent;
import com.btxtech.uiservice.renderer.RenderServiceInitEvent;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 28.12.2015.
 * *
 */
@Singleton
public class BaseItemUiService implements ModelMatricesProvider {
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
    private Map<Integer, VertexContainer> vertexContainers;
    private Map<Integer, Collection<ModelMatrices>> baseItemIdModelMatrices;

    public void onRenderServiceInitEvent(@Observes RenderServiceInitEvent renderServiceInitEvent) {
        // Setup render data
        vertexContainers = new HashMap<>();
        for (ItemType itemType : itemTypeService.getItemTypes(BaseItemType.class)) {
            vertexContainers.put(itemType.getId(), itemType.getVertexContainer());
        }
    }

    public void onPreRenderEvent(@Observes PreRenderEvent preRenderEvent) {
        // Setup model matrices
        baseItemIdModelMatrices = new HashMap<>();
        for (SyncBaseItem syncBaseItem : baseItemService.getSyncBaseItems()) {
            Collection<ModelMatrices> baseItemTypeMatrices = baseItemIdModelMatrices.get(syncBaseItem.getItemType().getId());
            if (baseItemTypeMatrices == null) {
                baseItemTypeMatrices = new ArrayList<>();
                baseItemIdModelMatrices.put(syncBaseItem.getItemType().getId(), baseItemTypeMatrices);
            }
            baseItemTypeMatrices.add(syncBaseItem.createModelMatrices());
        }
//            InterpolatedTerrainTriangle interpolatedTerrainTriangle = terrainUiService.getInterpolatedTerrainTriangle(unit.getPosition());
//            Vertex direction = new Vertex(DecimalPosition.createVector(unit.getAngle(), 1.0), 0);
//            double yRotation = direction.unsignedAngle(interpolatedTerrainTriangle.getNorm()) - MathHelper.QUARTER_RADIANT;
//            Matrix4 rotation = Matrix4.createZRotation(unit.getAngle()).multiply(Matrix4.createYRotation(-yRotation));
//            Matrix4 translation = Matrix4.createTranslation(unit.getPosition().getX(), unit.getPosition().getY(), interpolatedTerrainTriangle.getHeight()).multiply(rotation);
//            itemMatrices.add(new ModelMatrices(translation, rotation));
    }

    public Collection<Integer> getBaseItemTypeIds() {
        return vertexContainers.keySet();
    }

    public VertexContainer getItemTypeVertexContainer(int id) {
        return vertexContainers.get(id);
    }

    @Override
    public Collection<ModelMatrices> provideModelMatrices(int id) {
        return baseItemIdModelMatrices.get(id);
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
}

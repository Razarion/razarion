package com.btxtech.client.units;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.game.jsre.client.common.CollectionUtils;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.dto.ItemType;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.primitives.Matrix4;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 28.12.2015.
 */
@Singleton
public class ItemService {
    private static final long PERIOD_DURATION = 16000;
    // private Logger logger = Logger.getLogger(ItemService.class.getName());
    private ImageDescriptor imageDescriptor = ImageDescriptor.UNIT_TEXTURE_O1;
    private double specularIntensity = 0.1;
    private double specularHardness = 10;
    private Collection<ItemType> itemTypes;
    private Map<Integer, VertexContainer> vertexContainers;
    private Map<Integer, Collection<ModelMatrices>> unitIdModelMatrices;
    private boolean moving = true;
    private double angle = 0;
    private long lastTimestamp = System.currentTimeMillis();

    public void setItemTypes(Collection<ItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public void init() {
        vertexContainers = new HashMap<>();
        for (ItemType itemType : itemTypes) {
            vertexContainers.put(itemType.getId(), itemType.getVertexContainer());
        }
    }

    public Collection<Integer> getItemTypeIds() {
        return vertexContainers.keySet();
    }

    public VertexContainer getItemTypeVertexContainer(int id) {
        return vertexContainers.get(id);
    }


    public Collection<ModelMatrices> getModelMatrices(int itemTypeId) {
        return unitIdModelMatrices.get(itemTypeId);
    }

    public void tick() {
        long current = System.currentTimeMillis();
        long delta = current - lastTimestamp;
        lastTimestamp = current;

        double deltaAngle = (double) delta / (double) PERIOD_DURATION * MathHelper.ONE_RADIANT;
        if (moving) {
            angle += deltaAngle;
        }

        Collection<ModelMatrices> itemMatrices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Matrix4 vertexTransformation = Matrix4.createTranslation(200, 200, 5).multiply(Matrix4.createZRotation(angle)).multiply(Matrix4.createTranslation(i * 50, 0, 0)).multiply(Matrix4.createZRotation(Math.toRadians(-90)));
            Matrix4 normTransformation = Matrix4.createZRotation(angle).multiply(Matrix4.createZRotation(Math.toRadians(-90)));
            itemMatrices.add(new ModelMatrices(vertexTransformation, normTransformation));
        }
        unitIdModelMatrices = new HashMap<>();
        int itemTypeId = CollectionUtils.getFirst(itemTypes).getId();
        unitIdModelMatrices.put(itemTypeId, itemMatrices);
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
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

package com.btxtech.client.units;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.dto.ItemType;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import com.btxtech.shared.gameengine.pathing.Obstacle;
import com.btxtech.shared.gameengine.pathing.Pathing;
import com.btxtech.shared.gameengine.pathing.Unit;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// import elemental.client.Browser;
// import elemental.dom.TimeoutHandler;

/**
 * Created by Beat
 * 28.12.2015.
 */
@Singleton
public class ItemService {
    private Logger logger = Logger.getLogger(ItemService.class.getName());
    @Inject
    private TerrainSurface terrainSurface;
    private ImageDescriptor imageDescriptor = ImageDescriptor.UNIT_TEXTURE_O1;
    private double specularIntensity = 0.1;
    private double specularHardness = 10;
    private Collection<ItemType> itemTypes;
    private Map<Integer, VertexContainer> vertexContainers;
    private Map<Integer, Collection<ModelMatrices>> unitIdModelMatrices;
    private Pathing pathing;

    public void setItemTypes(Collection<ItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public void init() {
        // Setup render data
        vertexContainers = new HashMap<>();
        for (ItemType itemType : itemTypes) {
            vertexContainers.put(itemType.getId(), itemType.getVertexContainer());
        }
        // Setup game engine data
        pathing = new Pathing();
        Collection<Obstacle> obstacles = terrainSurface.getAllObstacles();
        for (Obstacle obstacle : obstacles) {
            pathing.addObstacle(obstacle);
        }
        setupItems();
    }

    public void tick() {
        try {
            pathing.tick(Pathing.FACTOR);
            setupModelMetrices();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Game loop crashed: " + t.getMessage(), t);
        }
    }

    private void setupModelMetrices() {
        // Setup model matrices
        unitIdModelMatrices.clear();
        Collection<ModelMatrices> itemMatrices = new ArrayList<>();
        for (Unit unit : pathing.getUnits()) {
            Vertex norm = terrainSurface.getInterpolatedNorm(unit.getPosition());
            System.out.println("norm: " + norm);
            System.out.println("unit: " + unit);
            Vertex direction = new Vertex(DecimalPosition.createVector(unit.getAngle(), 1.0), 0);
            System.out.println("direction: " + direction);
            double yRotation = direction.unsignedAngle(norm) - MathHelper.QUARTER_RADIANT;
            System.out.println("yRotation: " + Math.toDegrees(yRotation));
            Matrix4 rotation = Matrix4.createZRotation(unit.getAngle()).multiply(Matrix4.createYRotation(-yRotation));
            double height = terrainSurface.getInterpolatedHeight(unit.getPosition());
            Matrix4 translation = Matrix4.createTranslation(unit.getPosition().getX(), unit.getPosition().getY(), height).multiply(rotation);
            itemMatrices.add(new ModelMatrices(translation, rotation));
        }
        unitIdModelMatrices.put(1, itemMatrices);
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

    public void setupItems() {
        // Setup items
        int syncItemId = 1;
        pathing.removeAllUnits();

        pathing.createUnit(syncItemId++, true, 10, new DecimalPosition(893, 32), new DecimalPosition(2000, 32), null);
//        DecimalPosition destination = new DecimalPosition(2700, 1700);
//        pathing.createUnit(syncItemId++, true, 10, new DecimalPosition(200, 200), destination, null);
//        for (int x = -2; x < 3; x++) {
//            for (int y = -2; y < 3; y++) {
//                pathing.createUnit(syncItemId++, true, 10, new DecimalPosition(20 * x, 20 * y).add(200, 200), destination, null);
//            }
//        }
        unitIdModelMatrices = new HashMap<>();
        setupModelMetrices();
    }
}

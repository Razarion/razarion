package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 26.07.2016.
 */
@Dependent
@Named(SyncItem.SYNC_PHYSICAL_AREA)
public class SyncPhysicalArea {
    @Inject
    private TerrainService terrainService;
    private SyncItem syncItem;
    private DecimalPosition position2d;
    private double angle;
    private Vertex position3d;
    private Vertex norm;
    private double radius;
    private boolean fixVerticalNorm;
    private TerrainType terrainType;
    private Matrix4 modelMatrices;

    public void init(SyncItem syncItem, double radius, boolean fixVerticalNorm, TerrainType terrainType, DecimalPosition position2d, double angle) {
        this.syncItem = syncItem;
        this.radius = radius;
        this.fixVerticalNorm = fixVerticalNorm;
        this.terrainType = terrainType;
        setPosition2d(position2d);
        this.angle = angle;
    }

    public double getRadius() {
        return radius;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public DecimalPosition getPosition2d() {
        return position2d;
    }

    public boolean hasPosition() {
        return position2d != null;
    }

    void setPosition2d(DecimalPosition position2d) {
        this.position2d = position2d;
        position3d = null;
    }

    public void addToPosition2d(DecimalPosition deltaXY) {
        setPosition2d(position2d.add(deltaXY));
    }

    public double getAngle() {
        return angle;
    }

    void setAngle(double angle) {
        this.angle = angle;
    }

    public void setupPosition3d() {
        if (position3d != null && norm != null) {
            return;
        }
        if (fixVerticalNorm) {
            position3d = new Vertex(position2d, terrainService.getSurfaceAccess().getHighestZInRegion(position2d, radius));
            norm = Vertex.Z_NORM;
        } else {
            position3d = new Vertex(position2d, terrainService.getSurfaceAccess().getInterpolatedZ(position2d));
            norm = terrainService.getSurfaceAccess().getInterpolatedNorm(position2d);
        }
        modelMatrices = null;
    }

    public Vertex getPosition3d() {
        if (position3d == null) {
            throw new IllegalStateException("Position3d is not set");
        }
        return position3d;
    }

    public Vertex getNorm() {
        if (norm == null) {
            throw new IllegalStateException("Norm is not set");
        }
        return norm;
    }

    public Matrix4 getModelMatrices() {
        if (modelMatrices == null) {
            modelMatrices = Matrix4.createTranslation(getPosition3d()).multiply(Matrix4.createFromNormAndYaw(getNorm(), angle));
        }
        return modelMatrices;
    }

    public boolean hasDestination() {
        return false;
    }

    public boolean overlap(DecimalPosition position) {
        return position2d.getDistance(position) < radius;
    }

    public boolean overlap(DecimalPosition position, double radius) {
        return position2d.getDistance(position) < this.radius + radius;
    }

    public boolean overlap(Rectangle2D rectangle) {
        return rectangle.adjoinsCircleExclusive(position2d, radius);
    }

    boolean isInRange(double range, SyncItem target) {
        return getDistance(target) < range;
    }

    boolean isInRange(double range, DecimalPosition position, BaseItemType baseItemType) {
        return getDistance(position, baseItemType.getPhysicalAreaConfig().getRadius()) < range;
    }

    public double getDistance(SyncPhysicalArea syncPhysicalArea) {
        return getDistance(syncPhysicalArea.position2d, syncPhysicalArea.getRadius());
    }

    public double getDistance(DecimalPosition position, double radius) {
        return position2d.getDistance(position) - this.radius - radius;
    }

    public double getDistance(SyncItem other) {
        return getDistance(other.getSyncPhysicalArea());
    }

    public SyncItem getSyncItem() {
        return syncItem;
    }

    public boolean canMove() {
        return false;
    }

    public void stop() {

    }

    private Collection<SyncPhysicalArea> getNeighbors(SyncItemContainerService syncItemContainerService) {
        java.util.Collection<SyncPhysicalArea> neighbors = new ArrayList<>();

        syncItemContainerService.iterateOverItems(false, false, getSyncItem(), null, syncItem -> {
            SyncPhysicalArea neighbor = syncItem.getSyncPhysicalArea();
            if (getDistance(neighbor) > PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE) {
                return null;
            }
            neighbors.add(neighbor);
            return null;
        });
        return neighbors;
    }

    boolean isDirectNeighborInDestination(SyncItemContainerService syncItemContainerService, DecimalPosition destination) {
        for (SyncPhysicalArea neighbor : getNeighbors(syncItemContainerService)) {
            if (neighbor.canMove() && neighbor.hasDestination()) {
                continue;
            }

            if (neighbor.position2d.getDistance(destination) < neighbor.getRadius() + PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE) {
                return true;
            }
        }
        return false;
    }

    boolean isIndirectNeighborInDestination(SyncItemContainerService syncItemContainerService, Collection<SyncPhysicalArea> expandedUnits, DecimalPosition destination) {
        Collection<SyncPhysicalArea> neighbors = getNeighbors(syncItemContainerService);
        for (SyncPhysicalArea neighbor : neighbors) {
            if (neighbor.isDirectNeighborInDestination(syncItemContainerService, destination)) {
                return true;
            }
        }
        expandedUnits.add(this);
        int count = 0;
        for (SyncPhysicalArea neighbor : neighbors) {
            if (expandedUnits.contains(neighbor)) {
                continue;
            }
            if (neighbor.isIndirectNeighborInDestination(syncItemContainerService, expandedUnits, destination)) {
                count++;
                if (count >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    public void synchronize(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        position2d = syncPhysicalAreaInfo.getPosition();
        angle = syncPhysicalAreaInfo.getAngle();
    }

    public SyncPhysicalAreaInfo getSyncPhysicalAreaInfo() {
        SyncPhysicalAreaInfo syncPhysicalAreaInfo = new SyncPhysicalAreaInfo();
        syncPhysicalAreaInfo.setAngle(angle);
        syncPhysicalAreaInfo.setPosition(position2d);
        return syncPhysicalAreaInfo;
    }

    @Override
    public String toString() {
        return "SyncPhysicalArea{" +
                "position3d=" + position3d +
                ", norm=" + norm +
                ", radius=" + radius +
                '}';
    }
}

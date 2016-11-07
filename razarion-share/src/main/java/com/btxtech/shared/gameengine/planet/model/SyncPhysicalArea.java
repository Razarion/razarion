package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 26.07.2016.
 */
public class SyncPhysicalArea {
    private SyncItem syncItem;
    private Vertex position;
    private double angle;
    private Vertex norm;
    private double radius;

    public SyncPhysicalArea(SyncItem syncItem, PhysicalAreaConfig physicalAreaConfig, Vertex position, Vertex norm, double angle) {
        this.syncItem = syncItem;
        this.angle = angle;
        this.radius = physicalAreaConfig.getRadius();
        this.position = position;
        this.norm = norm;
    }

    public SyncPhysicalArea(SyncItem syncItem, double radius, Vertex position, Vertex norm, double angle) {
        this.syncItem = syncItem;
        this.radius = radius;
        this.position = position;
        this.angle = angle;
        this.norm = norm;
    }

    public double getAngle() {
        return angle;
    }

    void setAngle(double angle) {
        this.angle = angle;
    }

    public ModelMatrices createModelMatrices() {
        Vertex direction = new Vertex(DecimalPosition.createVector(angle, 1.0), 0);
        double yRotation = direction.unsignedAngle(getNorm()) - MathHelper.QUARTER_RADIANT;
        Matrix4 rotation = Matrix4.createZRotation(angle).multiply(Matrix4.createYRotation(-yRotation));
        Matrix4 matrix = Matrix4.createTranslation(getPosition().getX(), getPosition().getY(), getPosition().getZ()).multiply(rotation);
        return new ModelMatrices().setModel(matrix).setNorm(matrix.normTransformation());
    }

    public Vertex getPosition() {
        return position;
    }

    public DecimalPosition getXYPosition() {
        return position.toXY();
    }

    public boolean hasDestination() {
        return false;
    }

    public void addToXYPosition(DecimalPosition deltaXY) {
        position = position.add(deltaXY.getX(), deltaXY.getY(), 0);
    }

    public void setXYPosition(DecimalPosition xyPosition) {
        position = new Vertex(xyPosition.getX(), xyPosition.getY(), position.getZ());
    }

    public Vertex getNorm() {
        return norm;
    }

    public double getRadius() {
        return radius;
    }

    public boolean hasPosition() {
        return position != null;
    }

    public boolean overlap(DecimalPosition position) {
        return this.position.toXY().getDistance(position) < radius;
    }

    public boolean overlap(DecimalPosition position, double radius) {
        return this.position.toXY().getDistance(position) < this.radius + radius;
    }

    public boolean overlap(Rectangle2D rectangle) {
        return rectangle.adjoinsCircleExclusive(position.toXY(), radius);
    }

    public boolean isInRange(double range, SyncItem target) {
        return getDistance(target) < range;
    }

    public boolean isInRange(double range, DecimalPosition position, BaseItemType baseItemType) {
        return getDistance(position, baseItemType.getPhysicalAreaConfig().getRadius()) < range;
    }

    public double getDistance(SyncPhysicalArea syncPhysicalArea) {
        return getDistance(syncPhysicalArea.getXYPosition(), syncPhysicalArea.getRadius());
    }

    public double getDistance(DecimalPosition position, double radius) {
        return getXYPosition().getDistance(position) - this.radius - radius;
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

    public boolean canTurn() {
        return false;
    }

    public void stop() {

    }

    protected Collection<SyncPhysicalArea> getNeighbors(SyncItemContainerService syncItemContainerService) {
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

    protected boolean isDirectNeighborInDestination(SyncItemContainerService syncItemContainerService, DecimalPosition destination) {
        for (SyncPhysicalArea neighbor : getNeighbors(syncItemContainerService)) {
            if (neighbor.canMove() && neighbor.hasDestination()) {
                continue;
            }

            if (neighbor.getXYPosition().getDistance(destination) < neighbor.getRadius() + PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE) {
                return true;
            }
        }
        return false;
    }

    protected boolean isIndirectNeighborInDestination(SyncItemContainerService syncItemContainerService, Collection<SyncPhysicalArea> expandedUnits, DecimalPosition destination) {
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

    @Override
    public String toString() {
        return "SyncPhysicalArea{" +
                "position=" + position +
                ", norm=" + norm +
                ", radius=" + radius +
                '}';
    }
}

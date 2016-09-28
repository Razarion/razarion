package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 26.07.2016.
 */
public class SyncPhysicalArea {
    private SyncItem syncItem;
    private Vertex position;
    private Vertex norm;
    private double radius;

    public SyncPhysicalArea(SyncItem syncItem, PhysicalAreaConfig physicalAreaConfig, Vertex position, Vertex norm) {
        this.syncItem = syncItem;
        this.radius = physicalAreaConfig.getRadius();
        this.position = position;
        this.norm = norm;
    }

    public ModelMatrices createModelMatrices(SyncBaseItem syncBaseItem, double scale) {
        throw new UnsupportedOperationException();
//        Vertex direction = new Vertex(DecimalPosition.createVector(angle, 1.0), 0);
//        double yRotation = direction.unsignedAngle(norm) - MathHelper.QUARTER_RADIANT;
//        Matrix4 rotation = Matrix4.createZRotation(angle).multiply(Matrix4.createYRotation(-yRotation));
//        Matrix4 matrix = Matrix4.createTranslation(position.startX(), position.startY(), position.getZ()).multiply(rotation).multiply(Matrix4.createScale(scale, scale, scale));
//        return new ModelMatrices().setSyncBaseItem(syncBaseItem).setModel(matrix).setNorm(matrix.normTransformation());
    }

    public Vertex getPosition() {
        return position;
    }

    public DecimalPosition getXYPosition() {
        return position.toXY();
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

    public boolean isInRange(double range, SyncBaseItem target) {
        return getXYPosition().getDistance(target.getSyncPhysicalArea().getXYPosition()) < radius + range;
    }

    public double getDistance(SyncPhysicalArea syncPhysicalArea) {
        return getXYPosition().getDistance(syncPhysicalArea.getXYPosition()) - radius - syncPhysicalArea.radius;
    }

    public SyncItem getSyncItem() {
        return syncItem;
    }

    public boolean canMove() {
        return false;
    }

    public void stop() {

    }

    protected Collection<SyncPhysicalArea> getNeighbors(SyncItemContainerService syncItemContainerService) {
        java.util.Collection<SyncPhysicalArea> neighbors = new ArrayList<>();

        syncItemContainerService.iterateOverItems(false, false, getSyncItem(), null, syncItem -> {
            SyncPhysicalArea neighbor = syncItem.getSyncPhysicalArea();
            if (getDistance(neighbor) > 1.0) {
                return null;
            }
            neighbors.add(neighbor);
            return null;
        });
        return neighbors;
    }

    protected boolean isDirectNeighborInDestination(SyncItemContainerService syncItemContainerService, DecimalPosition destination) {
        for (SyncPhysicalArea neighbor : getNeighbors(syncItemContainerService)) {
            if (neighbor.canMove() && ((SyncPhysicalMovable) neighbor).hasDestination()) {
                continue;
            }

            if (neighbor.getXYPosition().getDistance(destination) < neighbor.getRadius() + 1) {
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

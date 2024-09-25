package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 26.07.2016.
 */

@Named(SyncItem.SYNC_PHYSICAL_AREA)
public class SyncPhysicalArea {
    private Logger logger = Logger.getLogger(SyncPhysicalArea.class.getName());

    private SyncItemContainerServiceImpl syncItemContainerService;
    private SyncItem syncItem;
    private DecimalPosition position;
    private double angle;
    private double radius;
    private TerrainType terrainType;
    private Matrix4 modelMatrices;

    @Inject
    public SyncPhysicalArea(SyncItemContainerServiceImpl syncItemContainerService) {
        this.syncItemContainerService = syncItemContainerService;
    }

    public void init(SyncItem syncItem, double radius, boolean fixVerticalNorm, TerrainType terrainType, DecimalPosition position2d, double angle) {
        this.syncItem = syncItem;
        this.radius = radius;
        this.terrainType = terrainType;
        setPosition2d(position2d, false);
        this.angle = angle;
    }

    public double getRadius() {
        return radius;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public boolean hasPosition() {
        return position != null;
    }

    void setPosition2d(DecimalPosition position2d, boolean pathingService) {
        DecimalPosition oldPosition = this.position;
        this.position = position2d;
        syncItemContainerService.onPositionChanged(getSyncItem(), oldPosition, this.position, pathingService);
    }

    public double getAngle() {
        return angle;
    }

    void setAngle(double angle) {
        this.angle = angle;
    }

    public boolean hasDestination() {
        return false;
    }

    public boolean overlap(DecimalPosition position) {
        return this.position.getDistance(position) < radius;
    }

    public boolean overlap(DecimalPosition position, double radius) {
        return this.position.getDistance(position) < this.radius + radius;
    }

    public boolean overlap(Rectangle2D rectangle) {
        return rectangle.adjoinsCircleExclusive(position, radius);
    }

    boolean isInRange(double range, SyncItem target) {
        return getDistance(target) < range;
    }

    boolean isInRange(double range, DecimalPosition position, BaseItemType baseItemType) {
        return getDistance(position, baseItemType.getPhysicalAreaConfig().getRadius()) < range;
    }

    boolean isInRange(double range, DecimalPosition position) {
        return getDistance(position, 0) < range;
    }

    public double getDistance(SyncPhysicalArea syncPhysicalArea) {
        return getDistance(syncPhysicalArea.position, syncPhysicalArea.getRadius());
    }

    public double getDistance(DecimalPosition position, double radius) {
        return this.position.getDistance(position) - this.radius - radius;
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

    public void synchronize(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        // logger.severe("synchronize id: " + getSyncItem().getId() + ". " + syncPhysicalAreaInfo);
        DecimalPosition oldPosition2d = position;
        double oldAngle = angle;
        // System.out.println("synchronize: " + getSyncItem().getId() + "|" + syncPhysicalAreaInfo);
        if (position != null && syncPhysicalAreaInfo.getPosition() != null) {
            if (position.getDistance(syncPhysicalAreaInfo.getPosition()) > 0.5) {
                // System.out.println("TELEPORTING: " + getSyncItem().getId() + " p: " + position2d + ". new p: " + syncPhysicalAreaInfo.getPosition() + ". distance: " + position2d.getDistance(syncPhysicalAreaInfo.getPosition()));
                logger.severe("TELEPORTING: " + getSyncItem().getId() + " p: " + position + ". new p: " + syncPhysicalAreaInfo.getPosition() + ". distance: " + position.getDistance(syncPhysicalAreaInfo.getPosition()));
            }
        }
        position = syncPhysicalAreaInfo.getPosition();
        syncItemContainerService.onPositionChanged(getSyncItem(), oldPosition2d, position, false);
        angle = syncPhysicalAreaInfo.getAngle();
    }

    public SyncPhysicalAreaInfo getSyncPhysicalAreaInfo() {
        SyncPhysicalAreaInfo syncPhysicalAreaInfo = new SyncPhysicalAreaInfo();
        syncPhysicalAreaInfo.setAngle(angle);
        syncPhysicalAreaInfo.setPosition(position);
        return syncPhysicalAreaInfo;
    }

    @Override
    public String toString() {
        return "SyncPhysicalArea{" +
                "itemId=" + syncItem.getId() +
                "position2d=" + position +
                ", radius=" + radius +
                '}';
    }
}

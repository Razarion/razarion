package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.nativejs.NativeMatrixDto;
import com.btxtech.shared.nativejs.NativeMatrixFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * 26.07.2016.
 */
@Dependent
@Named(SyncItem.SYNC_PHYSICAL_AREA)
public class SyncPhysicalArea {
    @Inject
    private TerrainService terrainService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    private SyncItem syncItem;
    private DecimalPosition position2d;
    private double angle;
    private Vertex position3d;
    private Vertex norm;
    private double radius;
    private boolean fixVerticalNorm;
    private TerrainType terrainType;
    private Matrix4 modelMatrices;
    private NativeMatrixDto modelNativeMatrixDto;

    public void init(SyncItem syncItem, double radius, boolean fixVerticalNorm, TerrainType terrainType, DecimalPosition position2d, double angle) {
        this.syncItem = syncItem;
        this.radius = radius;
        this.fixVerticalNorm = fixVerticalNorm;
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

    public DecimalPosition getPosition2d() {
        return position2d;
    }

    public boolean hasPosition() {
        return position2d != null;
    }

    void setPosition2d(DecimalPosition position2d, boolean pathingService) {
        DecimalPosition oldPosition = this.position2d;
        this.position2d = position2d;
        syncItemContainerService.onPositionChanged(getSyncItem(), oldPosition, this.position2d, pathingService);
        position3d = null;
        norm = null;
    }

    public void addToPosition2d(DecimalPosition deltaXY) {
        setPosition2d(position2d.add(deltaXY), true);
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
        modelNativeMatrixDto = null;
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

    public NativeMatrixDto getModelNativeMatrixDto() {
        if (modelNativeMatrixDto == null) {
            modelNativeMatrixDto = nativeMatrixFactory.createNativeMatrixDtoColumnMajorArray(getModelMatrices().toWebGlArray());
        }
        return modelNativeMatrixDto;
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

    boolean isInRange(double range, DecimalPosition position) {
        return getDistance(position, 0) < range;
    }

    public double getDistance(SyncPhysicalArea syncPhysicalArea) {
        return getDistance(syncPhysicalArea.position2d, syncPhysicalArea.getRadius());
    }

    public boolean isDesiredTouching(SyncPhysicalArea other) {
        SyncPhysicalMovable thisMovable = null;
        SyncPhysicalMovable otherMovable = null;

        if (this instanceof SyncPhysicalMovable && ((SyncPhysicalMovable) this).isMoving()) {
            thisMovable = (SyncPhysicalMovable) this;
        }
        if (other instanceof SyncPhysicalMovable && ((SyncPhysicalMovable) other).isMoving()) {
            otherMovable = (SyncPhysicalMovable) other;
        }

        if (thisMovable == null && otherMovable == null) {
            return false;
        }

        if (thisMovable != null && otherMovable != null) {
            return thisMovable.getDesiredPosition().getDistance(otherMovable.getDesiredPosition()) < getRadius() + other.getRadius();
        } else if (thisMovable != null) {
            return thisMovable.getDesiredPosition().getDistance(other.getPosition2d()) < getRadius() + other.getRadius();
        } else {
            return otherMovable.getDesiredPosition().getDistance(getPosition2d()) < getRadius() + other.getRadius();
        }
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

    public void synchronize(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        DecimalPosition oldPosition2d = position2d;
        double oldAngle = angle;
        position2d = syncPhysicalAreaInfo.getPosition();
        syncItemContainerService.onPositionChanged(getSyncItem(), oldPosition2d, position2d, false);
        angle = syncPhysicalAreaInfo.getAngle();
        if (position2d != null) {
            if (oldPosition2d == null || !oldPosition2d.equals(position2d) || oldAngle != angle) {
                position3d = null;
                norm = null;
                setupPosition3d();
            }
        } else {
            position3d = null;
        }
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
                "itemId=" + syncItem.getId() +
                ", position3d=" + position3d +
                ", norm=" + norm +
                ", radius=" + radius +
                '}';
    }
}

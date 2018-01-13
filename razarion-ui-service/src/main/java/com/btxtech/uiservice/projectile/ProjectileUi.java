package com.btxtech.uiservice.projectile;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.datatypes.ModelMatrices;

/**
 * Created by Beat
 * 10.01.2017.
 */
public class ProjectileUi {
    private BaseItemType baseItemType;
    private Vertex start;
    private Vertex target;
    private double totalDistance;
    private double speed;
    private long startTime;
    private double distance;

    public ProjectileUi(BaseItemType baseItemType, Vertex start, Vertex target, double speed) {
        this.baseItemType = baseItemType;
        this.start = start;
        this.target = target;
        this.speed = speed;
        startTime = System.currentTimeMillis();
        totalDistance = start.distance(target);
    }

    BaseItemType getBaseItemType() {
        return baseItemType;
    }

    void setupDistance(long timeStamp) {
        distance = (double) (timeStamp - startTime) / 1000.0 * speed;
    }

    boolean destinationReached() {
        return distance >= totalDistance;
    }

    ModelMatrices createInterpolatedModelMatrices(NativeMatrixFactory nativeMatrixFactory) {
        Vertex position = start.interpolate(distance, target);
        return ModelMatrices.createFromPositionAndZRotation(NativeUtil.toNativeVertex(position), NativeUtil.toNativeVertex(target.sub(start)), nativeMatrixFactory);
    }


}

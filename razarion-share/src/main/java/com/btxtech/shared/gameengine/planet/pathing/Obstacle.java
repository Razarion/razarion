package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;

/**
 * Created by Beat
 * 16.05.2016.
 */
public abstract class Obstacle {

    public abstract boolean isPiercing(Line line);

    public abstract NativeObstacle toNativeObstacle();
}

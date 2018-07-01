package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;

/**
 * Created by Beat
 * 16.05.2016.
 */
public abstract class Obstacle {

    public abstract boolean isPiercing(Line line);

    public abstract boolean isIntersect(Circle2D circle2D);

    public abstract NativeObstacle toNativeObstacle();
}

package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

/**
 * Created by Beat
 * on 26.02.2018.
 */
public interface PathingServiceUpdateListener {
    void onPathingChanged(SyncPhysicalMovable syncPhysicalMovable);
}

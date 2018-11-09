package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

/**
 * Created by Beat
 * on 28.10.2018.
 */
public interface PathingServiceUtil {
    static void setupPushAwayVelocity(SyncPhysicalMovable pusher, SyncPhysicalMovable shifty) {
        DecimalPosition pushAwayDirection = shifty.getPosition2d().sub(pusher.getPosition2d()).normalize();
        shifty.setupForPushAway(pushAwayDirection.multiply(DecimalPosition.zeroIfNull(pusher.getVelocity()).dotProduct(pushAwayDirection)));
    }

}

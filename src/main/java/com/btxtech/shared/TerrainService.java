package com.btxtech.shared;

import com.btxtech.shared.SlopeConfigEntity;
import com.btxtech.shared.dto.GroundSkeleton;
import org.jboss.errai.bus.server.annotations.Remote;

import java.util.Collection;

/**
 * Created by Beat
 * 24.04.2016.
 */
@Remote
public interface TerrainService {
    Collection<SlopeSkeletonEntity> loadSlopeSkeleton();

    GroundSkeleton loadGroundSkeleton();
}

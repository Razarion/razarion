package com.btxtech.shared;

import com.btxtech.shared.dto.GroundSkeleton;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import org.jboss.errai.bus.server.annotations.Remote;

import javax.transaction.Transactional;
import java.util.Collection;

/**
 * Created by Beat
 * 24.04.2016.
 */
@Remote
public interface TerrainService {
    Collection<SlopeSkeleton> loadSlopeSkeletons();

    GroundSkeleton loadGroundSkeleton();

    Collection<TerrainSlopePosition> loadTerrainSlopePositions();

    Collection<TerrainObject> loadTerrainObjects();

    Collection<TerrainObjectPosition> loadTerrainObjectPositions();
}

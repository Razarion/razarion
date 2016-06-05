package com.btxtech.shared;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.SlopeConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import org.jboss.errai.bus.server.annotations.Remote;

import java.util.Collection;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Remote
public interface TerrainEditorService {
    Collection<ObjectNameId> getSlopeNameIds();

    SlopeConfig loadSlopeConfig(int id);

    SlopeConfig saveSlopeConfig(SlopeConfig slopeConfig);

    void deleteSlopeConfig(SlopeConfig slopeConfig);

    GroundConfig loadGroundConfig();

    GroundConfig saveGroundConfig(GroundConfig slopeConfig);

    void saveTerrainSlopePositions(Collection<TerrainSlopePosition> terrainSlopePositions);

    Collection<ObjectNameId> getTerrainObjectNameIds();

    void saveTerrainObjectPositions(Collection<TerrainObjectPosition> terrainObjectPositions);
}

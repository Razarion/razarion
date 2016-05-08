package com.btxtech.shared;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.SlopeNameId;
import org.jboss.errai.bus.server.annotations.Remote;

import java.util.Collection;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Remote
public interface TerrainEditorService {
    Collection<SlopeNameId> getSlopeNameIds();

    SlopeConfigEntity loadSlopeConfig(int id);

    SlopeConfigEntity saveSlopeConfig(SlopeConfigEntity slopeConfigEntity);

    void deleteSlopeConfig(SlopeConfigEntity slopeConfigEntity);

    GroundConfig loadGroundConfig();

    GroundConfig saveGroundConfig(GroundConfig slopeConfigEntity);
}

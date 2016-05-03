package com.btxtech.shared;

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

    GroundConfigEntity loadGroundConfig();

    GroundConfigEntity saveGroundConfig(GroundConfigEntity slopeConfigEntity);
}

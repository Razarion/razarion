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

    SlopeConfigEntity load(int id);

    SlopeConfigEntity save(SlopeConfigEntity slopeConfigEntity);

    void delete(SlopeConfigEntity slopeConfigEntity);
}

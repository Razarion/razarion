package com.btxtech.client;

import com.btxtech.shared.VertexList;
import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Remote
public interface TerrainEditorService {
    void save();
}

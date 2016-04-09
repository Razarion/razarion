package com.btxtech.shared;

import org.jboss.errai.bus.server.annotations.Remote;

import java.util.Collection;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Remote
public interface TerrainEditorService {
    SlopeConfigEntity read();

    void save(SlopeConfigEntity plateauConfigEntity);

    Collection<TerrainMeshVertex> readTerrainMeshVertices();

    void saveTerrainMeshVertices(Collection<TerrainMeshVertex> terrainMeshVertexes);

}

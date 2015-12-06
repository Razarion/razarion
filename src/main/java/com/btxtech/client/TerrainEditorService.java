package com.btxtech.client;

import com.btxtech.shared.TerrainMeshVertex;
import com.btxtech.shared.PlateauConfigEntity;
import org.jboss.errai.bus.server.annotations.Remote;

import java.util.Collection;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Remote
public interface TerrainEditorService {
    PlateauConfigEntity read();

    void save(PlateauConfigEntity plateauConfigEntity);

    Collection<TerrainMeshVertex> readTerrainMeshVertices();

    void saveTerrainMeshVertices(Collection<TerrainMeshVertex> terrainMeshVertexes);

}

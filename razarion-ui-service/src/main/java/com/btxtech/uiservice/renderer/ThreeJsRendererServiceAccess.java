package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface ThreeJsRendererServiceAccess {
    ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile, Integer defaultGroundConfigId);

    BabylonBaseItem createSyncBaseItem(int id, Integer threeJsModelPackConfigId, Integer meshContainerId, String internalName, Diplomacy diplomacy, double radius);

    void createProjectile(Vertex start, Vertex destination, double duration);

    void setViewFieldCenter(double x, double y);

    void initMeshContainers(MeshContainer[] meshContainers);
}

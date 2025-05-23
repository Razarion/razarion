package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 12.04.2017.
 */
@Singleton
public class TerrainTileFactory {

    // private Logger logger = Logger.getLogger(TerrainTileFactory.class.getName());
    private final Provider<TerrainTileBuilder> terrainTileBuilderInstance;

    private final ExceptionHandler exceptionHandler;

    @Inject
    public TerrainTileFactory(ExceptionHandler exceptionHandler, Provider<com.btxtech.shared.gameengine.planet.terrain.TerrainTileBuilder> terrainTileBuilderInstance) {
        this.exceptionHandler = exceptionHandler;
        this.terrainTileBuilderInstance = terrainTileBuilderInstance;
    }

    public TerrainTile generateTerrainTile(Index terrainTileIndex, TerrainShapeManager terrainShapeManager, PlanetConfig planetConfig) {
        TerrainShapeTile terrainShapeTile = terrainShapeManager.getTerrainShapeTile(terrainTileIndex);
        TerrainTileBuilder terrainTileBuilder = terrainTileBuilderInstance.get();
        terrainTileBuilder.init(terrainTileIndex);
        insertTerrainObjects(terrainTileBuilder, terrainShapeTile, terrainShapeManager);
        insertBabylonDecals(terrainTileBuilder, terrainShapeTile);
        return terrainTileBuilder.generate(planetConfig);
    }

    private void insertTerrainObjects(TerrainTileBuilder terrainTileBuilder, TerrainShapeTile terrainShapeTile, TerrainShapeManager terrainShapeManager) {
        if (terrainShapeTile == null) {
            return;
        }
        NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists = terrainShapeTile.getNativeTerrainShapeObjectLists();
        if (nativeTerrainShapeObjectLists == null) {
            return;
        }
        Arrays.stream(nativeTerrainShapeObjectLists).forEach(nativeTerrainShapeObjectList -> {
            if (nativeTerrainShapeObjectList.terrainShapeObjectPositions == null || nativeTerrainShapeObjectList.terrainShapeObjectPositions.length == 0) {
                return;
            }
            TerrainTileObjectList terrainTileObjectList = new TerrainTileObjectList();
            terrainTileObjectList.setTerrainObjectConfigId(nativeTerrainShapeObjectList.terrainObjectConfigId);
            List<TerrainObjectModel> terrainObjectModels = new ArrayList<>();
            Arrays.stream(nativeTerrainShapeObjectList.terrainShapeObjectPositions).forEach(nativeTerrainObjectPosition -> {
                try {
                    TerrainObjectModel terrainObjectModel = new TerrainObjectModel();
                    Index node = TerrainUtil.terrainPositionToNodeIndex(new DecimalPosition(nativeTerrainObjectPosition.x, nativeTerrainObjectPosition.y));
                    terrainObjectModel.position = new Vertex(nativeTerrainObjectPosition.x, nativeTerrainObjectPosition.y,  terrainShapeManager.getTerrainAnalyzer().getHeightNodeAt(node));
                    if (nativeTerrainObjectPosition.offset != null) {
                        terrainObjectModel.position = terrainObjectModel.position.add(
                                nativeTerrainObjectPosition.offset.x,
                                nativeTerrainObjectPosition.offset.y,
                                nativeTerrainObjectPosition.offset.z);
                    }
                    if (nativeTerrainObjectPosition.scale != null) {
                        terrainObjectModel.scale = new Vertex(
                                nativeTerrainObjectPosition.scale.x,
                                nativeTerrainObjectPosition.scale.y,
                                nativeTerrainObjectPosition.scale.z);
                    }
                    if (nativeTerrainObjectPosition.rotation != null) {
                        terrainObjectModel.rotation = new Vertex(
                                nativeTerrainObjectPosition.rotation.x,
                                nativeTerrainObjectPosition.rotation.y,
                                nativeTerrainObjectPosition.rotation.z);
                    }
                    terrainObjectModel.terrainObjectId = nativeTerrainObjectPosition.terrainObjectId;
                    terrainObjectModels.add(terrainObjectModel);
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            });
            terrainTileObjectList.setTerrainObjectModels(terrainObjectModels.toArray(new TerrainObjectModel[0]));
            terrainTileBuilder.addTerrainTileObjectList(terrainTileObjectList);
        });
    }

    private void insertBabylonDecals(TerrainTileBuilder terrainTileBuilder, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            return;
        }
        NativeBabylonDecal[] nativeBabylonDecals = terrainShapeTile.getNativeBabylonDecals();
        if (nativeBabylonDecals == null || nativeBabylonDecals.length == 0) {
            return;
        }

        BabylonDecal[] babylonDecals = new BabylonDecal[nativeBabylonDecals.length];
        for (int i = 0; i < nativeBabylonDecals.length; i++) {
            NativeBabylonDecal nativeBabylonDecal = nativeBabylonDecals[i];
            BabylonDecal babylonDecal = new BabylonDecal();
            babylonDecal.babylonMaterialId = nativeBabylonDecal.babylonMaterialId;
            babylonDecal.xPos = nativeBabylonDecal.xPos;
            babylonDecal.yPos = nativeBabylonDecal.yPos;
            babylonDecal.xSize = nativeBabylonDecal.xSize;
            babylonDecal.ySize = nativeBabylonDecal.ySize;
            babylonDecals[i] = babylonDecal;
        }
        terrainTileBuilder.setBabylonDecals(babylonDecals);
    }
}

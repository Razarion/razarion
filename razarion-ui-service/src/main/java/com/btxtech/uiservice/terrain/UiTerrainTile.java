package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.BabylonTerrainTile;

import jakarta.inject.Inject;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.terrainPositionToTileIndex;
import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.tileIndexToNodeIndex;

/**
 * Created by Beat
 * 31.03.2017.
 */

public class UiTerrainTile {
    // private Logger logger = Logger.getLogger(UiTerrainTile.class.getName());
    private final TerrainUiService terrainUiService;
    private final BabylonRendererService babylonRendererService;
    private final HeightMapConverter heightMapConverter;
    private TerrainTile terrainTile;
    private TerrainAnalyzer terrainAnalyzer;
    private BabylonTerrainTile babylonTerrainTile;
    private boolean active;

    @Inject
    public UiTerrainTile(BabylonRendererService babylonRendererService,
                         TerrainUiService terrainUiService,
                         HeightMapConverter heightMapConverter) {
        this.babylonRendererService = babylonRendererService;
        this.terrainUiService = terrainUiService;
        this.heightMapConverter = heightMapConverter;
    }

    public void init(Index index) {
        terrainUiService.requestTerrainTile(index, this::terrainTileReceived);
    }

    public void setActive(boolean active) {
        this.active = active;
        if (babylonTerrainTile != null) {
            if (active) {
                babylonTerrainTile.addToScene();
            } else {
                babylonTerrainTile.removeFromScene();
            }

        }
    }

    private void terrainTileReceived(TerrainTile terrainTile) {
        this.terrainTile = terrainTile;
        Uint16ArrayEmu heightMap = terrainTile.getGroundHeightMap();

        // Use platform-specific converter to safely convert heightMap to plain Java array.
        // This avoids WASM-GC illegal cast errors when JSObjects cross into pure Java code.
        int[] heightArray = heightMapConverter.convert(heightMap);

        terrainAnalyzer = new TerrainAnalyzer(new PlainArrayHeightMapAccess(heightArray), null);
        babylonTerrainTile = babylonRendererService.createTerrainTile(terrainTile);

        if (active) {
            babylonTerrainTile.addToScene();
        }
    }

    // Plain Java array accessor - no JSObject involvement
    private static class PlainArrayHeightMapAccess implements com.btxtech.shared.gameengine.planet.terrain.container.HeightMapAccess {
        private final int[] heightArray;

        public PlainArrayHeightMapAccess(int[] heightArray) {
            this.heightArray = heightArray;
        }

        @Override
        public int getUInt16HeightAt(int i) {
            if (i >= 0 && i < heightArray.length) {
                return heightArray[i];
            }
            return 0;
        }
    }

    public TerrainTile getTerrainTile() {
        return terrainTile;
    }

    public void setTerrainTypeOrdinals(int[] terrainTypeOrdinals) {
        if (terrainTile != null) {
            terrainTile.setTerrainTypeOrdinals(terrainTypeOrdinals);
        }
    }

    public void dispose() {
        if (babylonTerrainTile != null) {
            babylonTerrainTile.dispose();
            babylonTerrainTile = null;
        }
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, DecimalPosition position) {
        return TerrainType.isAllowed(terrainType, getTerrainType(position));
    }

    public TerrainType getTerrainType(DecimalPosition terrainPosition) {
        Index nodeIndex = TerrainUtil.terrainPositionToNodeIndex(terrainPosition);
        Index tileIndex = terrainPositionToTileIndex(terrainPosition);
        Index nodeTileIndex = tileIndexToNodeIndex(tileIndex);
        Index analyzeIndex = nodeIndex.sub(nodeTileIndex);

        // Prefer the worker-computed terrain types shipped with the tile: they account for blocking
        // terrain objects and read heights across tile borders (the local analyzer has neither the
        // TerrainShapeManager nor neighbour tiles, which makes it report BLOCKED along tile edges).
        int[] terrainTypeOrdinals = terrainTile != null ? terrainTile.getTerrainTypeOrdinals() : null;
        if (terrainTypeOrdinals != null
                && analyzeIndex.getX() >= 0 && analyzeIndex.getX() < TerrainUtil.NODE_X_COUNT
                && analyzeIndex.getY() >= 0 && analyzeIndex.getY() < TerrainUtil.NODE_Y_COUNT) {
            int ordinal = terrainTypeOrdinals[analyzeIndex.getY() * TerrainUtil.NODE_X_COUNT + analyzeIndex.getX()];
            return TerrainType.values()[ordinal];
        }

        if (terrainAnalyzer == null) {
            // The tile has been asked for and has not arrived. terrainAnalyzer is built in
            // terrainTileReceived, so between the request and the answer there is nothing here to
            // ask - and since the game deliberately starts before the tiles are in, that window is
            // seconds long on a phone and reliably hit by the base placer on its first check.
            //
            // BLOCKED is the honest answer: not "you may build here" and not a trap, but "not
            // here". The placer turns red, the player can move it, and the next check answers
            // properly the moment the tile lands.
            //
            // This is the null that cost every base on the planet from 2026-08-27 on. It read
            // "dereferencing a null pointer" with no stack, escaped the placer, the scene and the
            // worker dispatch, and took the tick stream with it - so the game rendered terrain,
            // moved its camera, and never showed a unit or a deploy dialog again.
            return TerrainType.BLOCKED;
        }
        return terrainAnalyzer.getTerrainType(analyzeIndex);
    }
}

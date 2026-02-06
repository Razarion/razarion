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

    public void dispose() {
        // TODO check for three.js resource which must be released
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, DecimalPosition position) {
        return TerrainType.isAllowed(terrainType, getTerrainType(position));
    }

    public TerrainType getTerrainType(DecimalPosition terrainPosition) {
        Index nodeIndex = TerrainUtil.terrainPositionToNodeIndex(terrainPosition);
        Index tileIndex = terrainPositionToTileIndex(terrainPosition);
        Index nodeTileIndex = tileIndexToNodeIndex(tileIndex);
        Index analyzeIndex = nodeIndex.sub(nodeTileIndex);

        return terrainAnalyzer.getTerrainType(analyzeIndex);
    }
}

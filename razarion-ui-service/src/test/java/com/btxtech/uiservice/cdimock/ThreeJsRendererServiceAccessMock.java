package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.renderer.BabylonBaseItem;
import com.btxtech.uiservice.renderer.BabylonBaseItemState;
import com.btxtech.uiservice.renderer.ThreeJsRendererServiceAccess;
import com.btxtech.uiservice.renderer.ThreeJsTerrainTile;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class ThreeJsRendererServiceAccessMock implements ThreeJsRendererServiceAccess {
    private final List<BabylonBaseItemMock> babylonBaseItemMocks = new ArrayList<>();

    private final Logger logger = Logger.getLogger(ThreeJsRendererServiceAccessMock.class.getName());

    @Override
    public ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile, Integer defaultGroundConfigId) {
        logger.warning("createTerrainTile()");
        return null;
    }

    @Override
    public BabylonBaseItem createBaseItem(int id) {
        BabylonBaseItemMock babylonBaseItemMock = new BabylonBaseItemMock(id);
        babylonBaseItemMocks.add(babylonBaseItemMock);
        return babylonBaseItemMock;
    }

    @Override
    public void setViewFieldCenter(double x, double y) {
        logger.warning("setViewFieldCenter()");
    }

    @Override
    public void initMeshContainers(MeshContainer[] meshContainers) {
        logger.warning("initMeshContainers()");
    }

    public List<BabylonBaseItemMock> getBabylonBaseItemMocks() {
        return babylonBaseItemMocks;
    }

    public void clear() {
        babylonBaseItemMocks.clear();
    }

    public static class BabylonBaseItemMock implements BabylonBaseItem {
        private final int id;
        private List<BabylonBaseItemState> babylonBaseItemStates = new ArrayList<>();
        private boolean removed;

        public BabylonBaseItemMock(int id) {
            this.id = id;
        }

        @Override
        public void updateState(BabylonBaseItemState state) {
            babylonBaseItemStates.add(state);
        }

        @Override
        public void remove() {
            removed = true;
        }

        public List<BabylonBaseItemState> getBabylonBaseItemStates() {
            return babylonBaseItemStates;
        }

        public boolean isRemoved() {
            return removed;
        }

        public int getId() {
            return id;
        }
    }
}

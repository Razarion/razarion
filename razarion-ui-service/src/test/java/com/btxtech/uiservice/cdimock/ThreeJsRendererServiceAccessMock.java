package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.nativejs.NativeVertexDto;
import com.btxtech.uiservice.Diplomacy;
import com.btxtech.uiservice.renderer.BabylonBaseItem;
import com.btxtech.uiservice.renderer.BabylonResourceItem;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.renderer.ThreeJsTerrainTile;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class ThreeJsRendererServiceAccessMock implements BabylonRenderServiceAccess {
    private final List<BabylonBaseItemMock> babylonBaseItemMocks = new ArrayList<>();

    private final Logger logger = Logger.getLogger(ThreeJsRendererServiceAccessMock.class.getName());

    @Override
    public ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile, Integer defaultGroundConfigId) {
        logger.warning("createTerrainTile()");
        return null;
    }

    @Override
    public BabylonBaseItem createBabylonBaseItem(int id, BaseItemType baseItemType, Diplomacy diplomacy) {
        BabylonBaseItemMock babylonBaseItemMock = new BabylonBaseItemMock(id, baseItemType, diplomacy);
        babylonBaseItemMocks.add(babylonBaseItemMock);
        return babylonBaseItemMock;
    }

    @Override
    public BabylonResourceItem createBabylonResourceItem(int id, ResourceItemType baseItemType) {
        throw new UnsupportedOperationException("...TODO...");
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
        private final BaseItemType baseItemType;
        private final Diplomacy diplomacy;
        private boolean disposed;
        private boolean select;
        private boolean hover;
        private Vertex position;
        private double angle;

        public BabylonBaseItemMock(int id, BaseItemType baseItemType, Diplomacy diplomacy) {
            this.id = id;
            this.baseItemType = baseItemType;
            this.diplomacy = diplomacy;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public void updatePosition() {

        }

        @Override
        public void updateAngle() {

        }

        @Override
        public void setHealth(double health) {

        }

        @Override
        public void select(boolean active) {
            this.select = active;
        }

        @Override
        public void hover(boolean active) {
            this.hover = active;
        }

        @Override
        public void setBuildingPosition(NativeVertexDto buildingPosition) {

        }

        @Override
        public void setHarvestingPosition(NativeVertexDto harvestingPosition) {

        }

        @Override
        public void setBuildup(double buildup) {

        }

        @Override
        public void setConstructing(double progress) {

        }

        @Override
        public Vertex getPosition() {
            return position;
        }

        @Override
        public void setPosition(Vertex position) {
            this.position = position;
        }

        public boolean isDisposed() {
            return disposed;
        }

        public Diplomacy getDiplomacy() {
            return diplomacy;
        }


        public double getAngle() {
            return angle;
        }

        @Override
        public void setAngle(double angle) {
            this.angle = angle;
        }

        public boolean isSelect() {
            return select;
        }

        public boolean isHover() {
            return hover;
        }

        @Override
        public void onProjectileFired(Vertex destination) {

        }

        @Override
        public void onExplode() {

        }

        @Override
        public BaseItemType getBaseItemType() {
            return baseItemType;
        }
    }
}

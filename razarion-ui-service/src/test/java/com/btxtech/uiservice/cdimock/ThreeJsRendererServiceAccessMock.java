package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;
import com.btxtech.uiservice.renderer.BabylonBaseItem;
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
    public BabylonBaseItem createBaseItem(int id, Diplomacy diplomacy, double radius) {
        BabylonBaseItemMock babylonBaseItemMock = new BabylonBaseItemMock(id, diplomacy, radius);
        babylonBaseItemMocks.add(babylonBaseItemMock);
        return babylonBaseItemMock;
    }

    @Override
    public void createProjectile(Vertex start, Vertex destination, double duration) {
        logger.warning("createProjectile()");
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
        private final Diplomacy diplomacy;
        private final double radius;
        private double x;
        private double y;
        private double z;
        private double angle;
        private boolean removed;
        private boolean select;
        private boolean hover;

        public BabylonBaseItemMock(int id, Diplomacy diplomacy, double radius) {
            this.id = id;
            this.diplomacy = diplomacy;
            this.radius = radius;
        }


        @Override
        public void updatePosition(double x, double y, double z, double angle) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.angle = angle;

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
        public void remove() {
            removed = true;
        }

        public boolean isRemoved() {
            return removed;
        }

        public Diplomacy getDiplomacy() {
            return diplomacy;
        }

        public double getRadius() {
            return radius;
        }

        public int getId() {
            return id;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public double getAngle() {
            return angle;
        }

        public boolean isSelect() {
            return select;
        }

        public boolean isHover() {
            return hover;
        }
    }
}

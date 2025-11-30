package com.btxtech.uiservice.mock;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;
import com.btxtech.uiservice.renderer.BabylonBaseItem;
import com.btxtech.uiservice.renderer.BabylonBoxItem;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.renderer.BabylonResourceItem;
import com.btxtech.uiservice.renderer.BabylonTerrainTile;
import com.btxtech.uiservice.renderer.MarkerConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Singleton
public class BabylonRenderServiceAccessMock implements BabylonRenderServiceAccess {
    private final Logger logger = Logger.getLogger(BabylonRenderServiceAccessMock.class.getName());
    private final List<BabylonTerrainTileMock> babylonTerrainTileMocks = new ArrayList<>();
    private final List<BabylonBaseItemMock> babylonBaseItemMocks = new ArrayList<>();
    private final List<BabylonResourceItemMock> babylonResourceItemMocks = new ArrayList<>();
    private final List<BabylonBoxItemMock> babylonBoxItemMocks = new ArrayList<>();

    private MarkerConfig showOutOfViewMarkerConfig;
    private double showOutOfViewAngle;

    @Inject
    public BabylonRenderServiceAccessMock() {
    }

    @Override
    public BabylonTerrainTile createTerrainTile(TerrainTile terrainTile) {
        BabylonTerrainTileMock babylonTerrainTileMock = new BabylonTerrainTileMock(terrainTile);
        babylonTerrainTileMocks.add(babylonTerrainTileMock);
        return babylonTerrainTileMock;
    }

    @Override
    public BabylonBaseItem createBabylonBaseItem(int id, BaseItemType baseItemType, int baseId, Diplomacy diplomacy, String userName) {
        BabylonBaseItemMock babylonBaseItemMock = new BabylonBaseItemMock(id, baseItemType, diplomacy, babylonBaseItemMocks::remove);
        babylonBaseItemMocks.add(babylonBaseItemMock);
        return babylonBaseItemMock;
    }

    @Override
    public void startSpawn(int particleSystemId, double x, double y, double z) {

    }

    @Override
    public BabylonResourceItem createBabylonResourceItem(int id, ResourceItemType baseItemType) {
        BabylonResourceItemMock babylonResourceItemMock = new BabylonResourceItemMock(id, baseItemType, babylonResourceItemMocks::remove);
        babylonResourceItemMocks.add(babylonResourceItemMock);
        return babylonResourceItemMock;
    }

    @Override
    public BabylonBoxItem createBabylonBoxItem(int id, BoxItemType boxItemType) {
        BabylonBoxItemMock babylonBoxItemMock = new BabylonBoxItemMock(id, boxItemType, babylonBoxItemMocks::remove);
        babylonBoxItemMocks.add(babylonBoxItemMock);
        return babylonBoxItemMock;
    }

    @Override
    public void setViewFieldCenter(double x, double y) {
        logger.warning("setViewFieldCenter()");
    }

    @Override
    public void runRenderer() {
        logger.warning("initMeshContainers()");
    }

    public List<BabylonTerrainTileMock> getBabylonTerrainTileMocks() {
        return babylonTerrainTileMocks;
    }

    public List<BabylonBaseItemMock> getBabylonBaseItemMocks() {
        return babylonBaseItemMocks;
    }

    public List<BabylonResourceItemMock> getBabylonResourceItemMocks() {
        return babylonResourceItemMocks;
    }

    public List<BabylonBoxItemMock> getBabylonBoxItemMocks() {
        return babylonBoxItemMocks;
    }

    public void clear() {
        babylonBaseItemMocks.clear();
        babylonResourceItemMocks.clear();
        babylonBoxItemMocks.clear();
    }

    @Override
    public void showOutOfViewMarker(MarkerConfig markerConfig, double angle) {
        this.showOutOfViewMarkerConfig = markerConfig;
        this.showOutOfViewAngle = angle;
    }

    @Override
    public void showPlaceMarker(PlaceConfig placeConfig, MarkerConfig markerConfig) {

    }

    public MarkerConfig getShowOutOfViewMarkerConfig() {
        return showOutOfViewMarkerConfig;
    }

    public double getShowOutOfViewAngle() {
        return showOutOfViewAngle;
    }

    public static class BabylonBaseItemMock implements BabylonBaseItem {
        private final int id;
        private final BaseItemType baseItemType;
        private final Diplomacy diplomacy;
        private final Consumer<BabylonBaseItemMock> onDisposeCallback;
        private boolean disposed;
        private boolean select;
        private boolean hover;
        private Vertex position;
        private double angle;
        private MarkerConfig markerConfig;

        public BabylonBaseItemMock(int id, BaseItemType baseItemType, Diplomacy diplomacy, Consumer<BabylonBaseItemMock> onDisposeCallback) {
            this.id = id;
            this.baseItemType = baseItemType;
            this.diplomacy = diplomacy;
            this.onDisposeCallback = onDisposeCallback;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void dispose() {
            disposed = true;
            onDisposeCallback.accept(this);
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
        public void mark(MarkerConfig markerConfig) {
            this.markerConfig = markerConfig;
        }

        public MarkerConfig getMarkerConfig() {
            return markerConfig;
        }

        @Override
        public void setBuildingPosition(DecimalPosition buildingPosition) {

        }

        @Override
        public void setHarvestingPosition(DecimalPosition harvestingPosition) {

        }

        @Override
        public void setBuildup(double buildup) {

        }

        @Override
        public void setConstructing(double progress) {

        }

        @Override
        public void setIdle(boolean idle) {

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

        @Override
        public boolean isEnemy() {
            return diplomacy == Diplomacy.ENEMY;
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
        public void onProjectileFired(int tagetSyncBaseItemId, DecimalPosition targetPosition) {

        }

        @Override
        public void onExplode() {

        }

        @Override
        public void updateUserName(String userName) {

        }

        @Override
        public int getBaseId() {
            return 0;
        }

        @Override
        public void setTurretAngle(double turretAngle) {

        }

        @Override
        public BaseItemType getBaseItemType() {
            return baseItemType;
        }
    }

    public static class BabylonResourceItemMock implements BabylonResourceItem {
        private final int id;
        private final ResourceItemType resourceItemType;
        private final Consumer<BabylonResourceItemMock> onDisposeCallback;
        private Vertex position;
        private MarkerConfig markerConfig;

        public BabylonResourceItemMock(int id, ResourceItemType resourceItemType, Consumer<BabylonResourceItemMock> onDisposeCallback) {
            this.id = id;
            this.resourceItemType = resourceItemType;
            this.onDisposeCallback = onDisposeCallback;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void dispose() {
            onDisposeCallback.accept(this);
        }

        @Override
        public double getAngle() {
            return 0;
        }

        @Override
        public void setAngle(double angle) {
        }

        @Override
        public void select(boolean active) {
        }

        @Override
        public void hover(boolean active) {
        }

        @Override
        public void mark(MarkerConfig markerConfig) {
            this.markerConfig = markerConfig;
        }

        @Override
        public Vertex getPosition() {
            return position;
        }

        @Override
        public void setPosition(Vertex position) {
            this.position = position;
        }

        public MarkerConfig getMarkerConfig() {
            return markerConfig;
        }
    }

    public static class BabylonBoxItemMock implements BabylonBoxItem {
        private final int id;
        private final BoxItemType boxItemType;
        private final Consumer<BabylonBoxItemMock> onDisposeCallback;
        private MarkerConfig markerConfig;
        private Vertex position;

        public BabylonBoxItemMock(int id, BoxItemType boxItemType, Consumer<BabylonBoxItemMock> onDisposeCallback) {
            this.id = id;
            this.boxItemType = boxItemType;
            this.onDisposeCallback = onDisposeCallback;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void dispose() {
            this.onDisposeCallback.accept(this);
        }

        @Override
        public Vertex getPosition() {
            return position;
        }

        @Override
        public void setPosition(Vertex position) {
            this.position = position;
        }

        @Override
        public double getAngle() {
            return 0;
        }

        @Override
        public void setAngle(double angle) {

        }

        @Override
        public void select(boolean active) {

        }

        @Override
        public void hover(boolean active) {

        }

        @Override
        public void mark(MarkerConfig markerConfig) {
            this.markerConfig = markerConfig;
        }

        public MarkerConfig getMarkerConfig() {
            return markerConfig;
        }
    }

}

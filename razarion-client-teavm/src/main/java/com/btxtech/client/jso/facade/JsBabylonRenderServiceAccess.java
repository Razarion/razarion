package com.btxtech.client.jso.facade;

import com.btxtech.client.bridge.DtoConverter;
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
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import java.util.logging.Logger;

import static com.btxtech.client.jso.facade.JsGwtAngularFacade.*;

public class JsBabylonRenderServiceAccess implements BabylonRenderServiceAccess {
    private final JSObject js;

    JsBabylonRenderServiceAccess(JSObject js) {
        this.js = js;
    }

    @JSBody(params = {"obj", "terrainTile"}, script = "return obj.createTerrainTile(terrainTile);")
    private static native JSObject callCreateTerrainTile(JSObject obj, JSObject terrainTile);

    @JSBody(params = {"obj", "id", "baseItemType", "baseId", "diplomacy", "userName"},
            script = "return obj.createBabylonBaseItem(id, baseItemType, baseId, diplomacy, userName);")
    private static native JSObject callCreateBabylonBaseItem(JSObject obj, int id, JSObject baseItemType, int baseId, String diplomacy, String userName);

    @JSBody(params = {"obj", "particleSystemId", "spawnAudioId", "x", "y", "z"}, script = "obj.startSpawn(particleSystemId, spawnAudioId, x, y, z);")
    private static native void callStartSpawn(JSObject obj, int particleSystemId, int spawnAudioId, double x, double y, double z);

    @JSBody(params = {"obj", "particleSystemId", "x", "y", "z"}, script = "obj.startSpawn(particleSystemId, null, x, y, z);")
    private static native void callStartSpawnParticleOnly(JSObject obj, int particleSystemId, double x, double y, double z);

    @JSBody(params = {"obj", "spawnAudioId", "x", "y", "z"}, script = "obj.startSpawn(null, spawnAudioId, x, y, z);")
    private static native void callStartSpawnAudioOnly(JSObject obj, int spawnAudioId, double x, double y, double z);

    @JSBody(params = {"obj", "id", "resourceItemType"}, script = "return obj.createBabylonResourceItem(id, resourceItemType);")
    private static native JSObject callCreateBabylonResourceItem(JSObject obj, int id, JSObject resourceItemType);

    @JSBody(params = {"obj", "id", "boxItemType"}, script = "return obj.createBabylonBoxItem(id, boxItemType);")
    private static native JSObject callCreateBabylonBoxItem(JSObject obj, int id, JSObject boxItemType);

    @JSBody(params = {"obj", "markerConfig", "angle"}, script = "obj.showOutOfViewMarker(markerConfig, angle);")
    private static native void callShowOutOfViewMarker(JSObject obj, Object markerConfig, double angle);

    @JSBody(params = {"obj", "placeConfig", "markerConfig"}, script = "obj.showPlaceMarker(placeConfig, markerConfig);")
    private static native void callShowPlaceMarker(JSObject obj, Object placeConfig, Object markerConfig);

    @JSBody(params = {"obj", "vertex"}, script = "obj.setPosition(vertex);")
    private static native void callSetPositionVertex(JSObject obj, JSObject vertex);

    @JSBody(params = {"obj", "x", "y"}, script = "obj.setBuildingPosition(x, y);")
    private static native void callSetBuildingPosition(JSObject obj, double x, double y);

    // --- Native bridge methods ---

    @JSBody(params = {"obj", "x", "y"}, script = "obj.setHarvestingPosition(x, y);")
    private static native void callSetHarvestingPosition(JSObject obj, double x, double y);

    @JSBody(params = {"obj", "targetId", "x", "y"}, script = "obj.onProjectileFired(targetId, x, y);")
    private static native void callOnProjectileFired(JSObject obj, int targetId, double x, double y);

    @JSBody(params = {"obj", "markerConfig"}, script = "obj.mark(markerConfig);")
    private static native void callMark(JSObject obj, Object markerConfig);

    @JSBody(params = {"obj", "prop"}, script = "return obj[prop];")
    private static native int getIntProp(JSObject obj, String prop);

    @JSBody(params = {"obj", "prop"}, script = "return obj[prop];")
    private static native double getDoubleProp(JSObject obj, String prop);

    @JSBody(params = {"obj", "method"}, script = "return obj[method]();")
    private static native double callMethod0ReturnD(JSObject obj, String method);

    @JSBody(params = {"obj", "prop"}, script = "return obj[prop];")
    private static native boolean getBooleanProp(JSObject obj, String prop);

    // --- Inner adapter classes for return types ---

    @Override
    public BabylonTerrainTile createTerrainTile(TerrainTile terrainTile) {
        JSObject terrainTileJs = DtoConverter.convertTerrainTile(terrainTile);
        JSObject result = callCreateTerrainTile(js, terrainTileJs);
        return new JsBabylonTerrainTile(result);
    }

    @Override
    public BabylonBaseItem createBabylonBaseItem(int id, BaseItemType baseItemType, int baseId, Diplomacy diplomacy, String userName) {
        JSObject baseItemTypeJs = DtoConverter.convertBaseItemType(baseItemType);
        JSObject result = callCreateBabylonBaseItem(js, id, baseItemTypeJs, baseId, diplomacy.name(), userName);
        return new JsBabylonBaseItem(result, baseItemType, baseId);
    }

    @Override
    public void startSpawn(Integer particleSystemId, Integer spawnAudioId, double x, double y, double z) {
        if (particleSystemId != null && spawnAudioId != null) {
            callStartSpawn(js, particleSystemId, spawnAudioId, x, y, z);
        } else if (particleSystemId != null) {
            callStartSpawnParticleOnly(js, particleSystemId, x, y, z);
        } else if (spawnAudioId != null) {
            callStartSpawnAudioOnly(js, spawnAudioId, x, y, z);
        }
    }

    @Override
    public BabylonResourceItem createBabylonResourceItem(int id, ResourceItemType resourceItemType) {
        JSObject resourceItemTypeJs = DtoConverter.convertResourceItemType(resourceItemType);
        JSObject result = callCreateBabylonResourceItem(js, id, resourceItemTypeJs);
        return new JsBabylonResourceItem(result);
    }

    @Override
    public BabylonBoxItem createBabylonBoxItem(int id, BoxItemType boxItemType) {
        JSObject boxItemTypeJs = DtoConverter.convertBoxItemType(boxItemType);
        JSObject result = callCreateBabylonBoxItem(js, id, boxItemTypeJs);
        return new JsBabylonBoxItem(result);
    }

    // --- Additional native helpers ---

    @Override
    public void setViewFieldCenter(double x, double y) {
        callMethod2D(js, "setViewFieldCenter", x, y);
    }

    @Override
    public void runRenderer() {
        callMethod0(js, "runRenderer");
    }

    @Override
    public void showOutOfViewMarker(MarkerConfig markerConfig, double angle) {
        // TODO: Convert MarkerConfig to JSObject via proxy factory
        callShowOutOfViewMarker(js, markerConfig, angle);
    }

    @Override
    public void showPlaceMarker(PlaceConfig placeConfig, MarkerConfig markerConfig) {
        // TODO: Convert PlaceConfig and MarkerConfig to JSObject via proxy factory
        callShowPlaceMarker(js, placeConfig, markerConfig);
    }

    private static class JsBabylonTerrainTile implements BabylonTerrainTile {
        private final JSObject js;

        JsBabylonTerrainTile(JSObject js) {
            this.js = js;
        }

        @Override
        public void addToScene() {
            callMethod0(js, "addToScene");
        }

        @Override
        public void removeFromScene() {
            callMethod0(js, "removeFromScene");
        }
    }

    private static class JsBabylonItemBase {
        protected final JSObject js;
        private final Logger logger = Logger.getLogger(JsBabylonItemBase.class.getName());

        JsBabylonItemBase(JSObject js) {
            this.js = js;
        }

        public int getId() {
            return getIntProp(js, "id");
        }

        public void dispose() {
            callMethod0(js, "dispose");
        }

        public Vertex getPosition() {
            JSObject pos = callMethod0Return(js, "getPosition");
            if (pos == null) {
                return null;
            }
            // Call the getter methods instead of reading properties directly
            // because Vertex objects use getX(), getY(), getZ() methods
            double x = callMethod0ReturnD(pos, "getX");
            double y = callMethod0ReturnD(pos, "getY");
            double z = callMethod0ReturnD(pos, "getZ");
            try {
                return new Vertex(x, y, z);
            } catch (Throwable t) {
                logger.warning("[JsBabylonItemBase] getPosition throwable: " + t.getMessage() + " x=" + x + " y=" + y + " z=" + z + " pos=" + pos);
                return new Vertex(0, 0, 0);
            }
        }

        public void setPosition(Vertex position) {
            JSObject vertexJs = DtoConverter.convertVertex(position);
            callSetPositionVertex(js, vertexJs);
        }

        public double getAngle() {
            return callMethod0ReturnD(js, "getAngle");
        }

        public void setAngle(double angle) {
            callMethod1D(js, "setAngle", angle);
        }

        public void select(boolean active) {
            callMethod1B(js, "select", active);
        }

        public void hover(boolean active) {
            callMethod1B(js, "hover", active);
        }

        public void mark(MarkerConfig markerConfig) {
            // TODO: Convert MarkerConfig to JSObject via proxy factory
            callMark(js, markerConfig);
        }
    }

    private static class JsBabylonBaseItem extends JsBabylonItemBase implements BabylonBaseItem {
        private final BaseItemType baseItemType;
        private final int baseId;

        JsBabylonBaseItem(JSObject js, BaseItemType baseItemType, int baseId) {
            super(js);
            this.baseItemType = baseItemType;
            this.baseId = baseId;
        }

        @Override
        public BaseItemType getBaseItemType() {
            return baseItemType;
        }

        @Override
        public boolean isEnemy() {
            return getBooleanProp(js, "isEnemy");
        }

        @Override
        public void setHealth(double health) {
            callMethod1D(js, "setHealth", health);
        }

        @Override
        public void setBuildingPosition(DecimalPosition buildingPosition) {
            if (buildingPosition != null) {
                callSetBuildingPosition(js, buildingPosition.getX(), buildingPosition.getY());
            } else {
                callMethod1(js, "setBuildingPosition", null);
            }
        }

        @Override
        public void setHarvestingPosition(DecimalPosition harvestingPosition) {
            if (harvestingPosition != null) {
                callSetHarvestingPosition(js, harvestingPosition.getX(), harvestingPosition.getY());
            } else {
                callMethod1(js, "setHarvestingPosition", null);
            }
        }

        @Override
        public void setBuildup(double buildup) {
            callMethod1D(js, "setBuildup", buildup);
        }

        @Override
        public void setConstructing(double progress) {
            callMethod1D(js, "setConstructing", progress);
        }

        @Override
        public void setIdle(boolean idle) {
            callMethod1B(js, "setIdle", idle);
        }

        @Override
        public void onProjectileFired(int targetSyncBaseItemId, DecimalPosition targetPosition) {
            if (targetPosition != null) {
                callOnProjectileFired(js, targetSyncBaseItemId, targetPosition.getX(), targetPosition.getY());
            }
        }

        @Override
        public void onExplode() {
            callMethod0(js, "onExplode");
        }

        @Override
        public void updateUserName(String userName) {
            callMethod1S(js, "updateUserName", userName);
        }

        @Override
        public int getBaseId() {
            return baseId;
        }

        @Override
        public void setTurretAngle(double turretAngle) {
            callMethod1D(js, "setTurretAngle", turretAngle);
        }
    }

    private static class JsBabylonResourceItem extends JsBabylonItemBase implements BabylonResourceItem {
        JsBabylonResourceItem(JSObject js) {
            super(js);
        }
    }

    private static class JsBabylonBoxItem extends JsBabylonItemBase implements BabylonBoxItem {
        JsBabylonBoxItem(JSObject js) {
            super(js);
        }
    }
}

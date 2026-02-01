package com.btxtech.worker;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeDecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBotGround;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBotGroundSlopeBox;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectPosition;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeVertex;
import com.btxtech.worker.jso.JsConsole;
import com.btxtech.worker.jso.JsFetch;
import com.btxtech.worker.jso.JsUtils;
import com.btxtech.worker.jso.dto.JsNativeBabylonDecal;
import com.btxtech.worker.jso.dto.JsNativeBotGround;
import com.btxtech.worker.jso.dto.JsNativeBotGroundSlopeBox;
import com.btxtech.worker.jso.dto.JsNativeDecimalPosition;
import com.btxtech.worker.jso.dto.JsNativeTerrainShape;
import com.btxtech.worker.jso.dto.JsNativeTerrainShapeObjectList;
import com.btxtech.worker.jso.dto.JsNativeTerrainShapeObjectPosition;
import com.btxtech.worker.jso.dto.JsNativeTerrainShapeTile;
import com.btxtech.worker.jso.dto.JsNativeVertex;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.typedarrays.Uint16Array;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.NODE_X_COUNT;
import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.NODE_Y_COUNT;
import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.TILE_NODE_SIZE;

/**
 * TeaVM implementation of NativeTerrainShapeAccess
 * Handles terrain data loading using Fetch API
 */
@Singleton
public class TeaVMNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    private final TerrainService terrainService;
    private NativeTerrainShape nativeTerrainShape;
    private Uint16Array terrainHeightMap;

    @Inject
    public TeaVMNativeTerrainShapeAccess(TerrainService terrainService) {
        this.terrainService = terrainService;
    }

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        nativeTerrainShape = null;
        terrainHeightMap = null;

        // Load terrain shape JSON
        loadTerrainShape(planetId, loadedCallback, failCallback);

        // Load terrain height map binary
        loadTerrainHeightMap(planetId, loadedCallback, failCallback);
    }

    private void loadTerrainShape(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        String url = CommonUrl.terrainShapeController(planetId);

        fetchJson(url, json -> {
            try {
                this.nativeTerrainShape = convertToNativeTerrainShape(json);
                checkBothLoaded(loadedCallback);
            } catch (Throwable t) {
                JsConsole.error("Error parsing terrain shape: " + t.getMessage());
                failCallback.accept(t.getMessage());
            }
        }, error -> {
            JsConsole.error("Failed to load terrain shape: " + error);
            failCallback.accept(error);
        });
    }

    private void loadTerrainHeightMap(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        String url = CommonUrl.terrainHeightMapController(planetId);

        fetchArrayBuffer(url, buffer -> {
            try {
                terrainHeightMap = JsFetch.createUint16Array(buffer);
            } catch (Throwable t) {
                JsConsole.warn("Error converting height map: " + t.getMessage());
                terrainHeightMap = createEmptyUint16Array();
            }
            checkBothLoaded(loadedCallback);
        }, error -> {
            JsConsole.warn("Failed to load terrain height map: " + error);
            terrainHeightMap = createEmptyUint16Array();
            checkBothLoaded(loadedCallback);
        });
    }

    private void checkBothLoaded(Consumer<NativeTerrainShape> loadedCallback) {
        if (nativeTerrainShape != null && terrainHeightMap != null) {
            loadedCallback.accept(nativeTerrainShape);
        }
    }

    @Override
    public Uint16ArrayEmu createTileGroundHeightMap(Index terrainTileIndex) {
        int tileHeightMapStart = getTileHeightMapStart(terrainTileIndex);
        int nextXTileHeightMapStart = getTileHeightMapStart(terrainTileIndex.add(1, 0));
        int nextYTileHeightMapStart = getTileHeightMapStart(terrainTileIndex.add(0, 1));
        int nextXYTileHeightMapStart = getTileHeightMapStart(terrainTileIndex.add(1, 1));

        Uint16Array resultArray = createUint16Array((NODE_X_COUNT + 1) * (NODE_Y_COUNT + 1));

        for (int i = 0; i < NODE_Y_COUNT; i++) {
            int sourceYOffset = i * NODE_X_COUNT;
            int sourceHeightMapStart = tileHeightMapStart + sourceYOffset;
            int sourceHeightMapEnd = sourceHeightMapStart + NODE_X_COUNT;
            int destHeightMapStart = i * (NODE_X_COUNT + 1);

            try {
                ArrayBufferView slice = sliceUint16Array(terrainHeightMap, sourceHeightMapStart, sourceHeightMapEnd);
                setUint16ArraySlice(resultArray, slice, destHeightMapStart);

                // Add from next X tile
                int sourceNextTileHeightMapStart;
                if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                    sourceNextTileHeightMapStart = nextXTileHeightMapStart + sourceYOffset;
                } else {
                    sourceNextTileHeightMapStart = sourceHeightMapEnd + 1;
                }
                ArrayBufferView sliceEast = sliceUint16Array(terrainHeightMap, sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
                setUint16ArraySlice(resultArray, sliceEast, destHeightMapStart + NODE_X_COUNT);

                // Add last north row
                if (i == NODE_Y_COUNT - 1) {
                    if (terrainTileIndex.getY() + 1 < terrainService.getTerrainShape().getTileYCount()) {
                        ArrayBufferView sliceNorth = sliceUint16Array(terrainHeightMap, nextYTileHeightMapStart, nextYTileHeightMapStart + NODE_X_COUNT);
                        setUint16ArraySlice(resultArray, sliceNorth, destHeightMapStart + NODE_X_COUNT + 1);

                        if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                            sourceNextTileHeightMapStart = nextXYTileHeightMapStart;
                        } else {
                            sourceNextTileHeightMapStart = nextYTileHeightMapStart + NODE_X_COUNT + 1;
                        }
                        ArrayBufferView sliceNorthEast = sliceUint16Array(terrainHeightMap, sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
                        setUint16ArraySlice(resultArray, sliceNorthEast, destHeightMapStart + NODE_X_COUNT + 1 + NODE_X_COUNT);
                    } else {
                        setUint16ArraySlice(resultArray, slice, destHeightMapStart + NODE_X_COUNT + 1);

                        if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                            sourceNextTileHeightMapStart = nextXTileHeightMapStart + sourceYOffset;
                        } else {
                            sourceNextTileHeightMapStart = sourceHeightMapEnd;
                        }
                        ArrayBufferView sliceNorthEast = sliceUint16Array(terrainHeightMap, sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
                        setUint16ArraySlice(resultArray, sliceNorthEast, destHeightMapStart + NODE_X_COUNT + 1 + NODE_X_COUNT);
                    }
                }
            } catch (Throwable t) {
                JsConsole.error("createTileGroundHeightMap error: " + t.getMessage());
            }
        }
        return asUint16ArrayEmu(resultArray);
    }

    private int getTileHeightMapStart(Index terrainTileIndex) {
        return terrainTileIndex.getY() * (terrainService.getTerrainShape().getTileXCount() * TILE_NODE_SIZE)
                + terrainTileIndex.getX() * TILE_NODE_SIZE;
    }

    @Override
    public int getGroundHeightAt(int index) {
        return getUint16ArrayValue(terrainHeightMap, index);
    }

    // Native JavaScript helpers via @JSBody

    @JSBody(params = {"length"}, script = "return new Uint16Array(length);")
    private static native Uint16Array createUint16Array(int length);

    @JSBody(script = "return new Uint16Array(0);")
    private static native Uint16Array createEmptyUint16Array();

    @JSBody(params = {"array", "start", "end"}, script = "return array.slice(start, end);")
    private static native ArrayBufferView sliceUint16Array(Uint16Array array, int start, int end);

    @JSBody(params = {"dest", "src", "offset"}, script = "dest.set(src, offset);")
    private static native void setUint16ArraySlice(Uint16Array dest, ArrayBufferView src, int offset);

    @JSBody(params = {"array", "index"}, script = "return array[index];")
    private static native int getUint16ArrayValue(Uint16Array array, int index);

    // Type conversion - convert from JSO interfaces to Java classes
    private static NativeTerrainShape convertToNativeTerrainShape(JSObject jsObj) {
        if (JsUtils.isNullOrUndefined(jsObj)) {
            return null;
        }

        JsNativeTerrainShape jsShape = (JsNativeTerrainShape) jsObj;
        NativeTerrainShape result = new NativeTerrainShape();

        int xLen = jsShape.getTilesXLength();

        if (xLen > 0 && xLen < 10000) { // Sanity check
            result.nativeTerrainShapeTiles = new NativeTerrainShapeTile[xLen][];
            for (int x = 0; x < xLen; x++) {
                int yLen = jsShape.getTilesYLength(x);
                if (yLen > 0 && yLen < 10000) { // Sanity check
                    result.nativeTerrainShapeTiles[x] = new NativeTerrainShapeTile[yLen];
                    for (int y = 0; y < yLen; y++) {
                        JsNativeTerrainShapeTile jsTile = jsShape.getTile(x, y);
                        result.nativeTerrainShapeTiles[x][y] = convertToNativeTerrainShapeTile(jsTile);
                    }
                }
            }
        }
        return result;
    }

    @JSBody(params = {"obj"}, script = "console.log('[TeaVM Debug] Object keys:', Object.keys(obj)); console.log('[TeaVM Debug] Object:', JSON.stringify(obj).substring(0, 500));")
    private static native void logObjectKeys(JSObject obj);

    private static NativeTerrainShapeTile convertToNativeTerrainShapeTile(JsNativeTerrainShapeTile jsTile) {
        if (jsTile == null || JsUtils.isNullOrUndefined((JSObject) jsTile)) {
            return null;
        }
        NativeTerrainShapeTile result = new NativeTerrainShapeTile();

        // Object lists - use direct JSBody accessors
        int objectListsLen = jsTile.getObjectListsLength();
        if (objectListsLen > 0) {
            result.nativeTerrainShapeObjectLists = new NativeTerrainShapeObjectList[objectListsLen];
            for (int i = 0; i < objectListsLen; i++) {
                JsNativeTerrainShapeObjectList jsList = jsTile.getObjectList(i);
                result.nativeTerrainShapeObjectLists[i] = convertToNativeTerrainShapeObjectList(jsList);
            }
        }

        // Babylon decals - use direct JSBody accessors
        int decalsLen = jsTile.getDecalsLength();
        if (decalsLen > 0) {
            result.nativeBabylonDecals = new NativeBabylonDecal[decalsLen];
            for (int i = 0; i < decalsLen; i++) {
                JsNativeBabylonDecal jsDecal = jsTile.getDecal(i);
                result.nativeBabylonDecals[i] = convertToNativeBabylonDecal(jsDecal);
            }
        }

        // Bot grounds - use direct JSBody accessors
        int botGroundsLen = jsTile.getBotGroundsLength();
        if (botGroundsLen > 0) {
            result.nativeBotGrounds = new NativeBotGround[botGroundsLen];
            for (int i = 0; i < botGroundsLen; i++) {
                JsNativeBotGround jsBotGround = jsTile.getBotGround(i);
                result.nativeBotGrounds[i] = convertToNativeBotGround(jsBotGround);
            }
        }

        return result;
    }

    private static NativeTerrainShapeObjectList convertToNativeTerrainShapeObjectList(JsNativeTerrainShapeObjectList jsList) {
        if (jsList == null || JsUtils.isNullOrUndefined((JSObject) jsList)) {
            return null;
        }
        NativeTerrainShapeObjectList result = new NativeTerrainShapeObjectList();
        result.terrainObjectConfigId = jsList.getTerrainObjectConfigId();

        // Use direct JSBody accessors
        int positionsLen = jsList.getPositionsLength();
        if (positionsLen > 0) {
            result.terrainShapeObjectPositions = new NativeTerrainShapeObjectPosition[positionsLen];
            for (int i = 0; i < positionsLen; i++) {
                JsNativeTerrainShapeObjectPosition jsPos = jsList.getPosition(i);
                result.terrainShapeObjectPositions[i] = convertToNativeTerrainShapeObjectPosition(jsPos);
            }
        }
        return result;
    }

    private static NativeTerrainShapeObjectPosition convertToNativeTerrainShapeObjectPosition(JsNativeTerrainShapeObjectPosition jsPos) {
        if (jsPos == null || JsUtils.isNullOrUndefined((JSObject) jsPos)) {
            return null;
        }
        NativeTerrainShapeObjectPosition result = new NativeTerrainShapeObjectPosition();
        result.terrainObjectId = jsPos.getTerrainObjectId();
        result.x = jsPos.getX();
        result.y = jsPos.getY();
        result.scale = convertToNativeVertex(jsPos.getScale());
        result.rotation = convertToNativeVertex(jsPos.getRotation());
        result.offset = convertToNativeVertex(jsPos.getOffset());
        return result;
    }

    private static NativeVertex convertToNativeVertex(JsNativeVertex jsVertex) {
        if (jsVertex == null || JsUtils.isNullOrUndefined((JSObject) jsVertex)) {
            return null;
        }
        NativeVertex result = new NativeVertex();
        result.x = jsVertex.getX();
        result.y = jsVertex.getY();
        result.z = jsVertex.getZ();
        return result;
    }

    private static NativeBabylonDecal convertToNativeBabylonDecal(JsNativeBabylonDecal jsDecal) {
        if (jsDecal == null || JsUtils.isNullOrUndefined((JSObject) jsDecal)) {
            return null;
        }
        NativeBabylonDecal result = new NativeBabylonDecal();
        result.babylonMaterialId = jsDecal.getBabylonMaterialId();
        result.xPos = jsDecal.getXPos();
        result.yPos = jsDecal.getYPos();
        result.xSize = jsDecal.getXSize();
        result.ySize = jsDecal.getYSize();
        return result;
    }

    private static NativeBotGround convertToNativeBotGround(JsNativeBotGround jsBotGround) {
        if (jsBotGround == null || JsUtils.isNullOrUndefined((JSObject) jsBotGround)) {
            return null;
        }
        NativeBotGround result = new NativeBotGround();
        result.model3DId = jsBotGround.getModel3DId();
        result.height = jsBotGround.getHeight();

        // Positions - use direct JSBody accessors, filter out invalid positions
        int positionsLen = jsBotGround.getPositionsLength();
        if (positionsLen > 0) {
            List<NativeDecimalPosition> validPositions = new ArrayList<>();
            for (int i = 0; i < positionsLen; i++) {
                JsNativeDecimalPosition jsPos = jsBotGround.getPosition(i);
                NativeDecimalPosition pos = convertToNativeDecimalPosition(jsPos);
                if (pos != null) {
                    validPositions.add(pos);
                }
            }
            if (!validPositions.isEmpty()) {
                result.positions = validPositions.toArray(new NativeDecimalPosition[0]);
            }
        }

        // Slope boxes - use direct JSBody accessors
        int slopeBoxesLen = jsBotGround.getSlopeBoxesLength();
        if (slopeBoxesLen > 0) {
            result.botGroundSlopeBoxes = new NativeBotGroundSlopeBox[slopeBoxesLen];
            for (int i = 0; i < slopeBoxesLen; i++) {
                JsNativeBotGroundSlopeBox jsBox = jsBotGround.getSlopeBox(i);
                result.botGroundSlopeBoxes[i] = convertToNativeBotGroundSlopeBox(jsBox);
            }
        }

        return result;
    }

    private static NativeDecimalPosition convertToNativeDecimalPosition(JsNativeDecimalPosition jsPos) {
        if (jsPos == null || JsUtils.isNullOrUndefined((JSObject) jsPos)) {
            return null;
        }
        // Check for valid position data
        if (!jsPos.isValid()) {
            JsConsole.warn("convertToNativeDecimalPosition: invalid position data, skipping");
            return null;
        }
        NativeDecimalPosition result = new NativeDecimalPosition();
        result.x = jsPos.getX();
        result.y = jsPos.getY();
        return result;
    }

    private static NativeBotGroundSlopeBox convertToNativeBotGroundSlopeBox(JsNativeBotGroundSlopeBox jsBox) {
        if (jsBox == null || JsUtils.isNullOrUndefined((JSObject) jsBox)) {
            return null;
        }
        NativeBotGroundSlopeBox result = new NativeBotGroundSlopeBox();
        result.xPos = jsBox.getXPos();
        result.yPos = jsBox.getYPos();
        result.height = jsBox.getHeight();
        result.yRot = jsBox.getYRot();
        result.zRot = jsBox.getZRot();
        return result;
    }

    // Array helper methods
    @JSBody(params = {"array"}, script = "return array === null || array === undefined;")
    private static native boolean isArrayNullOrUndefined(Object array);

    @JSBody(params = {"array"}, script = "return array ? array.length : 0;")
    private static native int getArrayLength(Object array);

    @JSBody(params = {"array2d", "index"}, script = "return array2d[index];")
    private static native JsNativeTerrainShapeTile[] getArray2D(JsNativeTerrainShapeTile[][] array2d, int index);

    @JSBody(params = {"array", "index"}, script = "return array[index];")
    private static native <T> T getArrayElement(T[] array, int index);

    @SuppressWarnings("unchecked")
    private static Uint16ArrayEmu asUint16ArrayEmu(Uint16Array array) {
        return (Uint16ArrayEmu) (Object) array;
    }

    // Fetch helpers with callbacks
    @JSBody(params = {"url", "successCallback", "errorCallback"}, script =
            "fetch(url)" +
            ".then(function(response) { " +
            "  if (!response.ok) throw new Error(response.status + ' ' + response.statusText);" +
            "  return response.json();" +
            "})" +
            ".then(function(json) { successCallback(json); })" +
            ".catch(function(error) { errorCallback(error.toString()); });")
    private static native void fetchJson(String url, JsonCallback successCallback, ErrorCallback errorCallback);

    @JSBody(params = {"url", "successCallback", "errorCallback"}, script =
            "fetch(url)" +
            ".then(function(response) { " +
            "  if (!response.ok) throw new Error(response.status + ' ' + response.statusText);" +
            "  return response.arrayBuffer();" +
            "})" +
            ".then(function(buffer) { successCallback(buffer); })" +
            ".catch(function(error) { errorCallback(error.toString()); });")
    private static native void fetchArrayBuffer(String url, ArrayBufferCallback successCallback, ErrorCallback errorCallback);

    @org.teavm.jso.JSFunctor
    public interface JsonCallback extends JSObject {
        void onSuccess(JSObject json);
    }

    @org.teavm.jso.JSFunctor
    public interface ArrayBufferCallback extends JSObject {
        void onSuccess(ArrayBuffer buffer);
    }

    @org.teavm.jso.JSFunctor
    public interface ErrorCallback extends JSObject {
        void onError(String error);
    }
}

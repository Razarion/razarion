package com.btxtech.worker;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemSpawnStart;
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.IdsDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.IntIntMap;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.BabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.BotGround;
import com.btxtech.shared.gameengine.planet.terrain.BotGroundSlopeBox;
import com.btxtech.shared.gameengine.planet.terrain.TerrainObjectModel;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.GeneratorType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HouseType;
import com.btxtech.shared.gameengine.datatypes.itemtype.SpecialType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeDecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.worker.jso.JsArray;
import com.btxtech.worker.jso.JsConsole;
import com.btxtech.worker.jso.JsJson;
import com.btxtech.worker.jso.JsObject;
import com.btxtech.worker.jso.JsUtils;
import com.btxtech.worker.jso.dto.JsNativeDecimalPosition;
import com.btxtech.worker.jso.dto.JsNativeTickInfo;
import com.btxtech.worker.jso.dto.JsNativeSyncBaseItemTickInfo;
import com.btxtech.worker.jso.dto.JsNativeSimpleSyncBaseItemTickInfo;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * TeaVM implementation of WorkerMarshaller
 * Uses native JavaScript JSON.stringify/parse for serialization
 */
public final class TeaVMWorkerMarshaller {
    private static final int COMMAND_OFFSET = 0;
    private static final int DATA_OFFSET_0 = 1;
    private static final int DATA_OFFSET_1 = 2;
    private static final int DATA_OFFSET_2 = 3;
    private static final int DATA_OFFSET_3 = 4;
    private static final int DATA_OFFSET_4 = 5;
    private static final int DATA_OFFSET_5 = 6;

    private TeaVMWorkerMarshaller() {
    }

    public static JsArray<Object> marshall(GameEngineControlPackage controlPackage) {
        JsArray<Object> array = JsArray.create();
        setArrayString(array, COMMAND_OFFSET, controlPackage.getCommand().name());

        switch (controlPackage.getCommand()) {
            // No data
            case LOADED:
            case STOP_REQUEST:
            case STOP_RESPONSE:
            case QUEST_PASSED:
            case PERFMON_REQUEST:
            case TICK_UPDATE_REQUEST:
            case INITIALIZED:
            case TICK_UPDATE_RESPONSE_FAIL:
            case CONNECTION_LOST:
            case INITIAL_SLAVE_SYNCHRONIZED_NO_BASE:
            case COMMAND_MOVE_ACK:
                break;

            // Single JSON data
            case START_BOTS:
            case EXECUTE_BOT_COMMANDS:
            case CREATE_RESOURCES:
            case RESOURCE_CREATED:
            case RESOURCE_DELETED:
            case CREATE_BOXES:
            case BOX_CREATED:
            case BOX_DELETED:
            case BOX_PICKED:
            case ACTIVATE_QUEST:
            case UPDATE_LEVEL:
            case PERFMON_RESPONSE:
            case INITIALISING_FAILED:
            case TERRAIN_TILE_REQUEST:
            case QUEST_PROGRESS:
            case SELL_ITEMS:
            case USE_INVENTORY_ITEM:
            case INITIAL_SLAVE_SYNCHRONIZED:
            case GET_TERRAIN_TYPE:
                setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                break;

            // Double JSON data
            case COMMAND_ATTACK:
            case COMMAND_FINALIZE_BUILD:
            case COMMAND_FABRICATE:
            case COMMAND_HARVEST:
            case COMMAND_MOVE:
            case COMMAND_PICK_BOX:
            case COMMAND_LOAD_CONTAINER:
            case COMMAND_UNLOAD_CONTAINER:
            case BASE_CREATED:
            case BASE_DELETED:
            case BASE_UPDATED:
            case PROJECTILE_DETONATION:
            case ENERGY_CHANGED:
            case GET_TERRAIN_TYPE_ANSWER:
                setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                setArrayString(array, DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                break;

            // Triple JSON data
            case PROJECTILE_FIRED:
            case COMMAND_BUILD:
                setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                setArrayString(array, DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                setArrayString(array, DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                break;

            case INITIALIZE_WARM:
                setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                setArrayString(array, DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                setArrayString(array, DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                setArrayString(array, DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                break;

            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                setArrayString(array, DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                setArrayString(array, DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                setArrayString(array, DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                setArrayString(array, DATA_OFFSET_4, toJson(controlPackage.getData(4)));
                break;

            // Multiple JSON data
            case INITIALIZE:
                setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                setArrayString(array, DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                setArrayString(array, DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                setArrayString(array, DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                setArrayString(array, DATA_OFFSET_4, toJson(controlPackage.getData(4)));
                break;

            // Native marshal terrain buffers
            case TERRAIN_TILE_RESPONSE:
                array.set(DATA_OFFSET_0, marshallTerrainTile((TerrainTile) controlPackage.getData(0)));
                break;

            // NativeTickInfo - convert to JavaScript object
            case TICK_UPDATE_RESPONSE:
                array.set(DATA_OFFSET_0, convertNativeTickInfoToJs((NativeTickInfo) controlPackage.getData(0)));
                break;

            // Single Structure clone
            case SYNC_ITEM_START_SPAWNED:
            case SYNC_ITEM_IDLE:
                array.set(DATA_OFFSET_0, (JSObject) controlPackage.getData(0));
                break;

            case START:
                setArrayString(array, DATA_OFFSET_0, (String) controlPackage.getData(0));
                break;

            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
        return array;
    }

    public static GameEngineControlPackage deMarshall(JSObject javaScriptObject) {
        JsArray<Object> array = (JsArray<Object>) javaScriptObject;
        String commandName = getArrayString(array, COMMAND_OFFSET);
        GameEngineControlPackage.Command command = GameEngineControlPackage.Command.valueOf(commandName);

        List<Object> data = new ArrayList<>();
        switch (command) {
            // No data
            case LOADED:
            case STOP_REQUEST:
            case STOP_RESPONSE:
            case QUEST_PASSED:
            case PERFMON_REQUEST:
            case TICK_UPDATE_REQUEST:
            case INITIALIZED:
            case TICK_UPDATE_RESPONSE_FAIL:
            case CONNECTION_LOST:
            case INITIAL_SLAVE_SYNCHRONIZED_NO_BASE:
            case COMMAND_MOVE_ACK:
                break;

            case INITIALIZE:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), StaticGameConfig.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), PlanetConfig.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_2), UserContext.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_3), GameEngineMode.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_4), String.class));
                break;

            case INITIALIZE_WARM:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), PlanetConfig.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), UserContext.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_2), GameEngineMode.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_3), String.class));
                break;

            case INITIALISING_FAILED:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), String.class));
                break;

            case START_BOTS:
            case EXECUTE_BOT_COMMANDS:
            case CREATE_RESOURCES:
            case CREATE_BOXES:
            case PERFMON_RESPONSE:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), List.class));
                break;

            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), Integer.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), IntIntMap.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_2), String.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_3), String.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_4), DecimalPosition.class));
                break;

            case TICK_UPDATE_RESPONSE:
            case SYNC_ITEM_START_SPAWNED:
            case SYNC_ITEM_IDLE:
                data.add(array.get(DATA_OFFSET_0));
                break;

            case COMMAND_ATTACK:
            case COMMAND_FINALIZE_BUILD:
            case COMMAND_PICK_BOX:
            case COMMAND_LOAD_CONTAINER:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), IdsDto.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), Integer.class));
                break;

            case COMMAND_BUILD:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), Integer.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), DecimalPosition.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_2), Integer.class));
                break;

            case COMMAND_FABRICATE:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), Integer.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), Integer.class));
                break;

            case COMMAND_HARVEST:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), IdsDto.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), Integer.class));
                break;

            case COMMAND_MOVE:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), IdsDto.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), DecimalPosition.class));
                break;

            case COMMAND_UNLOAD_CONTAINER:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), Integer.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), DecimalPosition.class));
                break;

            case RESOURCE_CREATED:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), SyncResourceItemSimpleDto.class));
                break;

            case RESOURCE_DELETED:
            case BOX_DELETED:
            case BASE_DELETED:
            case ACTIVATE_QUEST:
            case UPDATE_LEVEL:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), Integer.class));
                break;

            case ENERGY_CHANGED:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), Integer.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), Integer.class));
                break;

            case BASE_CREATED:
            case BASE_UPDATED:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), PlayerBaseDto.class));
                break;

            case USE_INVENTORY_ITEM:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), UseInventoryItem.class));
                break;

            case BOX_CREATED:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), SyncBoxItemSimpleDto.class));
                break;

            case BOX_PICKED:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), BoxContent.class));
                break;

            case PROJECTILE_FIRED:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), Integer.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), Integer.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_2), DecimalPosition.class));
                break;

            case PROJECTILE_DETONATION:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), Integer.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), DecimalPosition.class));
                break;

            case GET_TERRAIN_TYPE:
            case TERRAIN_TILE_REQUEST:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), Index.class));
                break;

            case GET_TERRAIN_TYPE_ANSWER:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), Index.class));
                data.add(fromJson(getArrayString(array, DATA_OFFSET_1), TerrainType.class));
                break;

            case TERRAIN_TILE_RESPONSE:
                data.add(demarshallTerrainTile(array.get(DATA_OFFSET_0)));
                break;

            case QUEST_PROGRESS:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), QuestProgressInfo.class));
                break;

            case SELL_ITEMS:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), IdsDto.class));
                break;

            case INITIAL_SLAVE_SYNCHRONIZED:
                data.add(fromJson(getArrayString(array, DATA_OFFSET_0), DecimalPosition.class));
                break;

            case START:
                data.add(getArrayString(array, DATA_OFFSET_0));
                break;

            default:
                throw new IllegalArgumentException("Unsupported command: " + command);
        }

        return new GameEngineControlPackage(command, data.toArray());
    }

    public static String toJson(Object object) {
        if (object == null) {
            return "null";
        }
        try {
            // For JSObjects, use native JSON.stringify
            if (object instanceof JSObject) {
                return JsJson.stringify((JSObject) object);
            }

            // Handle primitive types
            if (object instanceof String) {
                return "\"" + escapeJson((String) object) + "\"";
            }
            if (object instanceof Number) {
                return object.toString();
            }
            if (object instanceof Boolean) {
                return object.toString();
            }
            if (object instanceof Enum) {
                return "\"" + ((Enum<?>) object).name() + "\"";
            }

            // For complex Java objects, convert to JsObject manually
            JsObject jsObj = javaToJsObject(object);
            if (jsObj != null) {
                return JsJson.stringify(jsObj);
            }

            JsConsole.warn("TeaVMWorkerMarshaller.toJson(): Unsupported type: " + object.getClass().getName());
            return "null";
        } catch (Throwable t) {
            JsConsole.error("TeaVMWorkerMarshaller.toJson() error for: " + object.getClass().getName() + " - " + t.getMessage());
            return "null";
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json, Class<T> type) {
        if ("null".equals(json) || json == null || json.isEmpty()) {
            return null;
        }
        try {
            if (type == String.class) {
                // Remove surrounding quotes
                if (json.startsWith("\"") && json.endsWith("\"")) {
                    return (T) json.substring(1, json.length() - 1);
                }
                return (T) json;
            }
            if (type == Integer.class) {
                return (T) Integer.valueOf(json);
            }
            if (type == Double.class) {
                return (T) Double.valueOf(json);
            }
            if (type == Boolean.class) {
                return (T) Boolean.valueOf(json);
            }
            if (type == GameEngineMode.class) {
                String enumString = json.replace("\"", "");
                return (T) GameEngineMode.valueOf(enumString);
            }
            if (type == TerrainType.class) {
                String enumString = json.replace("\"", "");
                return (T) TerrainType.valueOf(enumString);
            }

            // For complex types, parse JSON and convert manually
            JSObject jsObj = JsJson.parse(json);
            return jsObjectToJava(jsObj, type);
        } catch (Throwable t) {
            JsConsole.error("TeaVMWorkerMarshaller.fromJson() error for type: " + type.getName() + " json: " + truncateForLog(json) + " - " + t.getMessage());
            return null;
        }
    }

    private static String truncateForLog(String s) {
        if (s == null) return "null";
        if (s.length() <= 100) return s;
        return s.substring(0, 100) + "...";
    }

    // Helper methods for array manipulation
    @JSBody(params = {"array", "index", "value"}, script = "array[index] = value;")
    private static native void setArrayString(JsArray<Object> array, int index, String value);

    @JSBody(params = {"array", "index"}, script = "return array[index];")
    private static native String getArrayString(JsArray<Object> array, int index);

    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Safe creation of DecimalPosition with NaN/Infinite validation
    // Uses fallback value (0, 0) for invalid values to keep the system functional
    private static DecimalPosition safeDecimalPosition(JsObject posObj) {
        if (posObj == null || JsUtils.isNullOrUndefined((JSObject) posObj)) {
            return null;
        }
        // Debug: Check actual object structure
        double x = posObj.getDouble("x");
        double y = posObj.getDouble("y");
        // Use 0 as fallback for invalid values
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            // Debug: log the object keys to understand structure
            debugLogObjectKeys(posObj);
            x = 0;
        }
        if (Double.isNaN(y) || Double.isInfinite(y)) {
            y = 0;
        }
        return new DecimalPosition(x, y);
    }

    @JSBody(params = {"obj"}, script = "console.log('[DEBUG] Object keys:', Object.keys(obj), 'Object:', JSON.stringify(obj).substring(0, 200));")
    private static native void debugLogObjectKeys(JSObject obj);

    // Safe creation of Vertex with NaN/Infinite validation
    // Uses fallback value (0) for invalid values to keep the system functional
    private static Vertex safeVertex(JsObject posObj) {
        if (posObj == null || JsUtils.isNullOrUndefined((JSObject) posObj)) {
            return null;
        }
        double x = posObj.getDouble("x");
        double y = posObj.getDouble("y");
        double z = posObj.getDouble("z");
        // Use 0 as fallback for invalid values
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            JsConsole.warn("safeVertex: invalid x=" + x + ", using 0");
            x = 0;
        }
        if (Double.isNaN(y) || Double.isInfinite(y)) {
            JsConsole.warn("safeVertex: invalid y=" + y + ", using 0");
            y = 0;
        }
        if (Double.isNaN(z) || Double.isInfinite(z)) {
            JsConsole.warn("safeVertex: invalid z=" + z + ", using 0");
            z = 0;
        }
        return new Vertex(x, y, z);
    }

    // TerrainTile marshalling
    private static JSObject marshallTerrainTile(TerrainTile terrainTile) {
        JsArray<Object> array = JsArray.create();

        // Index
        JsArray<Object> indexArray = JsArray.create();
        indexArray.push(terrainTile.getIndex().getX());
        indexArray.push(terrainTile.getIndex().getY());
        array.push(indexArray);

        // Ground config ID
        array.push(terrainTile.getGroundConfigId());

        // Babylon decals
        array.push(marshallBabylonDecals(terrainTile.getBabylonDecals()));

        // Ground height map
        array.push((JSObject) terrainTile.getGroundHeightMap());

        // Terrain tile object lists
        array.push(marshallTerrainTileObjectList(terrainTile.getTerrainTileObjectLists()));

        // Bot grounds
        array.push(marshallBotGrounds(terrainTile.getBotGrounds()));

        return array;
    }

    private static JSObject marshallBabylonDecals(BabylonDecal[] babylonDecals) {
        JsArray<Object> result = JsArray.create();
        if (babylonDecals != null) {
            for (BabylonDecal decal : babylonDecals) {
                JsObject obj = JsObject.create();
                obj.set("babylonMaterialId", decal.babylonMaterialId);
                obj.set("xPos", decal.xPos);
                obj.set("yPos", decal.yPos);
                obj.set("xSize", decal.xSize);
                obj.set("ySize", decal.ySize);
                result.push(obj);
            }
        }
        return result;
    }

    private static JSObject marshallBotGrounds(BotGround[] botGrounds) {
        JsArray<Object> result = JsArray.create();
        if (botGrounds != null) {
            for (BotGround botGround : botGrounds) {
                if (botGround == null) {
                    continue;
                }
                // Marshal positions and skip bot grounds with no valid positions
                JSObject positionsArray = marshallDecimalPositions(botGround.positions);
                JsArray<Object> posArr = (JsArray<Object>) positionsArray;
                if (posArr.getLength() == 0) {
                    continue;
                }
                JsObject obj = JsObject.create();
                obj.set("model3DId", botGround.model3DId);
                obj.set("height", botGround.height);
                obj.set("positions", positionsArray);
                obj.set("botGroundSlopeBoxes", marshallBotGroundSlopeBoxes(botGround.botGroundSlopeBoxes));
                result.push(obj);
            }
        }
        return result;
    }

    private static JSObject marshallDecimalPositions(DecimalPosition[] positions) {
        JsArray<Object> result = JsArray.create();
        if (positions != null) {
            for (DecimalPosition pos : positions) {
                // Skip null positions and those with invalid values
                if (pos == null) {
                    continue;
                }
                double x = pos.getX();
                double y = pos.getY();
                if (Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y)) {
                    continue;
                }
                JsObject obj = JsObject.create();
                obj.set("x", x);
                obj.set("y", y);
                result.push(obj);
            }
        }
        return result;
    }

    private static JSObject marshallBotGroundSlopeBoxes(BotGroundSlopeBox[] boxes) {
        JsArray<Object> result = JsArray.create();
        if (boxes != null) {
            for (BotGroundSlopeBox box : boxes) {
                JsObject obj = JsObject.create();
                obj.set("xPos", box.xPos);
                obj.set("yPos", box.yPos);
                obj.set("height", box.height);
                obj.set("yRot", box.yRot);
                obj.set("zRot", box.zRot);
                result.push(obj);
            }
        }
        return result;
    }

    private static JSObject marshallTerrainTileObjectList(TerrainTileObjectList[] lists) {
        JsArray<Object> result = JsArray.create();
        if (lists != null) {
            for (TerrainTileObjectList list : lists) {
                TerrainObjectModel[] models = list.getTerrainObjectModels();
                // Skip lists with no models to avoid null issues on client side
                if (models == null || models.length == 0) {
                    continue;
                }
                JsObject obj = JsObject.create();
                obj.set("terrainObjectConfigId", list.getTerrainObjectConfigId());
                obj.set("terrainObjectModels", marshallTerrainObjectModels(models));
                result.push(obj);
            }
        }
        return result;
    }

    private static JSObject marshallTerrainObjectModels(TerrainObjectModel[] models) {
        JsArray<Object> result = JsArray.create();
        if (models != null) {
            for (TerrainObjectModel model : models) {
                if (model == null) {
                    continue;
                }
                // Position is required - skip model if position is null or invalid
                JSObject positionArray = vertexToArray(model.position);
                if (positionArray == null) {
                    continue;
                }
                JsObject obj = JsObject.create();
                obj.set("terrainObjectId", model.terrainObjectId);
                obj.set("position", positionArray);
                obj.set("scale", vertexToArray(model.scale));
                obj.set("rotation", vertexToArray(model.rotation));
                result.push(obj);
            }
        }
        return result;
    }

    private static JSObject vertexToArray(Vertex vertex) {
        if (vertex == null) {
            return null;
        }
        double x = vertex.getX();
        double y = vertex.getY();
        double z = vertex.getZ();
        // Return null if any value is NaN or infinite
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) ||
            Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) {
            return null;
        }
        JsArray<Object> array = JsArray.create();
        array.push(x);
        array.push(y);
        array.push(z);
        return array;
    }

    // TerrainTile demarshalling
    private static TerrainTile demarshallTerrainTile(Object data) {
        JsArray<Object> array = (JsArray<Object>) data;
        TerrainTile terrainTile = new TerrainTile();

        // Index
        JsArray<Object> indexArray = (JsArray<Object>) array.get(0);
        int indexX = JsUtils.asInt((JSObject) indexArray.get(0));
        int indexY = JsUtils.asInt((JSObject) indexArray.get(1));
        terrainTile.setIndex(new Index(indexX, indexY));

        // Ground config ID
        terrainTile.setGroundConfigId(JsUtils.asInt((JSObject) array.get(1)));

        // Babylon decals
        terrainTile.setBabylonDecals(demarshallBabylonDecals(array.get(2)));

        // Ground height map - pass through as is (cast via @JSBody)
        terrainTile.setGroundHeightMap(toUint16ArrayEmu(array.get(3)));

        // Terrain tile object lists
        terrainTile.setTerrainTileObjectLists(demarshallTerrainTileObjectLists(array.get(4)));

        // Bot grounds
        terrainTile.setBotGrounds(demarshallBotGrounds(array.get(5)));

        return terrainTile;
    }

    private static BabylonDecal[] demarshallBabylonDecals(Object data) {
        if (JsUtils.isNullOrUndefined((JSObject) data)) {
            return null;
        }
        JsArray<Object> array = (JsArray<Object>) data;
        int length = array.getLength();
        if (length == 0) {
            return null;
        }
        BabylonDecal[] result = new BabylonDecal[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = (JsObject) array.get(i);
            BabylonDecal decal = new BabylonDecal();
            decal.babylonMaterialId = obj.getInt("babylonMaterialId");
            decal.xPos = obj.getDouble("xPos");
            decal.yPos = obj.getDouble("yPos");
            decal.xSize = obj.getDouble("xSize");
            decal.ySize = obj.getDouble("ySize");
            result[i] = decal;
        }
        return result;
    }

    private static BotGround[] demarshallBotGrounds(Object data) {
        if (JsUtils.isNullOrUndefined((JSObject) data)) {
            return null;
        }
        JsArray<Object> array = (JsArray<Object>) data;
        int length = array.getLength();
        if (length == 0) {
            return null;
        }
        BotGround[] result = new BotGround[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = (JsObject) array.get(i);
            BotGround botGround = new BotGround();
            botGround.model3DId = obj.getInt("model3DId");
            botGround.height = obj.getDouble("height");
            botGround.positions = demarshallDecimalPositions(obj.get("positions"));
            botGround.botGroundSlopeBoxes = demarshallBotGroundSlopeBoxes(obj.get("botGroundSlopeBoxes"));
            result[i] = botGround;
        }
        return result;
    }

    private static DecimalPosition[] demarshallDecimalPositions(JSObject data) {
        if (JsUtils.isNullOrUndefined(data)) {
            return null;
        }
        JsArray<Object> array = (JsArray<Object>) data;
        int length = array.getLength();
        if (length == 0) {
            return null;
        }
        java.util.List<DecimalPosition> validPositions = new java.util.ArrayList<>();
        for (int i = 0; i < length; i++) {
            JsObject obj = (JsObject) array.get(i);
            if (obj == null || JsUtils.isNullOrUndefined((JSObject) obj)) {
                continue;
            }
            double x = obj.getDouble("x");
            double y = obj.getDouble("y");
            // Skip invalid values
            if (Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y)) {
                continue;
            }
            validPositions.add(new DecimalPosition(x, y));
        }
        if (validPositions.isEmpty()) {
            return null;
        }
        return validPositions.toArray(new DecimalPosition[0]);
    }

    private static BotGroundSlopeBox[] demarshallBotGroundSlopeBoxes(JSObject data) {
        if (JsUtils.isNullOrUndefined(data)) {
            return null;
        }
        JsArray<Object> array = (JsArray<Object>) data;
        int length = array.getLength();
        if (length == 0) {
            return null;
        }
        BotGroundSlopeBox[] result = new BotGroundSlopeBox[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = (JsObject) array.get(i);
            BotGroundSlopeBox box = new BotGroundSlopeBox();
            box.xPos = obj.getDouble("xPos");
            box.yPos = obj.getDouble("yPos");
            box.height = obj.getDouble("height");
            box.yRot = obj.getDouble("yRot");
            box.zRot = obj.getDouble("zRot");
            result[i] = box;
        }
        return result;
    }

    private static TerrainTileObjectList[] demarshallTerrainTileObjectLists(Object data) {
        if (JsUtils.isNullOrUndefined((JSObject) data)) {
            return null;
        }
        JsArray<Object> array = (JsArray<Object>) data;
        int length = array.getLength();
        if (length == 0) {
            return null;
        }
        TerrainTileObjectList[] result = new TerrainTileObjectList[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = (JsObject) array.get(i);
            TerrainTileObjectList list = new TerrainTileObjectList();
            list.setTerrainObjectConfigId(obj.getInt("terrainObjectConfigId"));
            list.setTerrainObjectModels(demarshallTerrainObjectModels(obj.get("terrainObjectModels")));
            result[i] = list;
        }
        return result;
    }

    private static TerrainObjectModel[] demarshallTerrainObjectModels(JSObject data) {
        if (JsUtils.isNullOrUndefined(data)) {
            return null;
        }
        JsArray<Object> array = (JsArray<Object>) data;
        int length = array.getLength();
        if (length == 0) {
            return null;
        }
        TerrainObjectModel[] result = new TerrainObjectModel[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = (JsObject) array.get(i);
            TerrainObjectModel model = new TerrainObjectModel();
            model.terrainObjectId = obj.getInt("terrainObjectId");
            model.position = arrayToVertex(obj.get("position"));
            model.scale = arrayToVertex(obj.get("scale"));
            model.rotation = arrayToVertex(obj.get("rotation"));
            result[i] = model;
        }
        return result;
    }

    private static Vertex arrayToVertex(JSObject data) {
        if (JsUtils.isNullOrUndefined(data)) {
            return null;
        }
        JsArray<Object> array = (JsArray<Object>) data;
        double x = JsUtils.asDouble((JSObject) array.get(0));
        double y = JsUtils.asDouble((JSObject) array.get(1));
        double z = JsUtils.asDouble((JSObject) array.get(2));
        // Return null if any value is invalid
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) ||
            Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) {
            return null;
        }
        return new Vertex(x, y, z);
    }

    // Conversion helpers for Java objects to/from JsObject
    // These are simplified implementations - in production, use proper reflection or code generation

    private static JsObject objectToJsObject(Object obj) {
        // Simplified implementation - handle common types
        // In a full implementation, this would use reflection or generated mappers
        JsObject result = JsObject.create();

        if (obj instanceof DecimalPosition) {
            DecimalPosition pos = (DecimalPosition) obj;
            result.set("x", pos.getX());
            result.set("y", pos.getY());
        } else if (obj instanceof Index) {
            Index idx = (Index) obj;
            result.set("x", idx.getX());
            result.set("y", idx.getY());
        } else if (obj instanceof Vertex) {
            Vertex v = (Vertex) obj;
            result.set("x", v.getX());
            result.set("y", v.getY());
            result.set("z", v.getZ());
        }
        // Add more type mappings as needed

        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T jsObjectToJava(JSObject jsObj, Class<T> type) {
        if (JsUtils.isNullOrUndefined(jsObj)) {
            return null;
        }

        JsObject obj = (JsObject) jsObj;

        // Simple types - use safeDecimalPosition/safeVertex for validation with fallback
        if (type == DecimalPosition.class) {
            return (T) safeDecimalPosition(obj);
        }
        if (type == Index.class) {
            return (T) new Index(obj.getInt("x"), obj.getInt("y"));
        }
        if (type == Vertex.class) {
            return (T) safeVertex(obj);
        }

        // Worker DTOs
        if (type == IdsDto.class) {
            return (T) convertIdsDto(obj);
        }
        if (type == IntIntMap.class) {
            return (T) convertIntIntMap(obj);
        }
        if (type == PlayerBaseDto.class) {
            return (T) convertPlayerBaseDto(obj);
        }
        if (type == SyncResourceItemSimpleDto.class) {
            return (T) convertSyncResourceItemSimpleDto(obj);
        }
        if (type == SyncBoxItemSimpleDto.class) {
            return (T) convertSyncBoxItemSimpleDto(obj);
        }
        if (type == BoxContent.class) {
            return (T) convertBoxContent(obj);
        }
        if (type == QuestProgressInfo.class) {
            return (T) convertQuestProgressInfo(obj);
        }
        if (type == UseInventoryItem.class) {
            return (T) convertUseInventoryItem(obj);
        }

        // Packet types for server communication
        if (type == TickInfo.class) {
            return (T) convertTickInfo(obj);
        }
        if (type == InitialSlaveSyncItemInfo.class) {
            return (T) convertInitialSlaveSyncItemInfo(obj);
        }
        if (type == SyncBaseItemInfo.class) {
            return (T) convertSyncBaseItemInfo(obj);
        }
        if (type == PlayerBaseInfo.class) {
            return (T) convertPlayerBaseInfo(obj);
        }
        if (type == SyncResourceItemInfo.class) {
            return (T) convertSyncResourceItemInfo(obj);
        }
        if (type == SyncBoxItemInfo.class) {
            return (T) convertSyncBoxItemInfo(obj);
        }
        if (type == SyncPhysicalAreaInfo.class) {
            return (T) convertSyncPhysicalAreaInfo(obj);
        }
        if (type == SyncItemDeletedInfo.class) {
            return (T) convertSyncItemDeletedInfo(obj);
        }
        if (type == SyncItemSpawnStart.class) {
            return (T) convertSyncItemSpawnStart(obj);
        }

        // Complex config types
        if (type == StaticGameConfig.class) {
            return (T) convertStaticGameConfig(obj);
        }
        if (type == PlanetConfig.class) {
            return (T) convertPlanetConfig(obj);
        }
        if (type == UserContext.class) {
            return (T) convertUserContext(obj);
        }

        // List type - requires special handling based on context
        if (type == List.class) {
            return (T) convertGenericList(obj);
        }

        JsConsole.warn("jsObjectToJava: Unsupported type: " + type.getName());
        return null;
    }

    // ============ Simple DTO converters ============

    private static IdsDto convertIdsDto(JsObject obj) {
        IdsDto dto = new IdsDto();
        JSObject idsArray = obj.get("ids");
        if (!JsUtils.isNullOrUndefined(idsArray)) {
            dto.setIds(convertIntegerList(idsArray));
        }
        return dto;
    }

    private static IntIntMap convertIntIntMap(JsObject obj) {
        IntIntMap dto = new IntIntMap();
        JSObject mapObj = obj.get("map");
        if (!JsUtils.isNullOrUndefined(mapObj)) {
            dto.setMap(convertIntIntMapObject(mapObj));
        }
        return dto;
    }

    private static PlayerBaseDto convertPlayerBaseDto(JsObject obj) {
        PlayerBaseDto dto = new PlayerBaseDto();
        dto.setBaseId(obj.getInt("baseId"));
        dto.setName(obj.getString("name"));
        String characterStr = obj.getString("character");
        if (characterStr != null && !characterStr.isEmpty()) {
            dto.setCharacter(com.btxtech.shared.gameengine.datatypes.Character.valueOf(characterStr));
        }
        dto.setUserId(obj.getString("userId"));
        dto.setBotId(obj.getNullableInt("botId"));
        return dto;
    }

    private static SyncResourceItemSimpleDto convertSyncResourceItemSimpleDto(JsObject obj) {
        SyncResourceItemSimpleDto dto = new SyncResourceItemSimpleDto();
        dto.setId(obj.getInt("id"));
        dto.setItemTypeId(obj.getInt("itemTypeId"));
        JSObject posObj = obj.get("position");
        if (!JsUtils.isNullOrUndefined(posObj)) {
            dto.setPosition(safeVertex((JsObject) posObj));
        }
        return dto;
    }

    private static SyncBoxItemSimpleDto convertSyncBoxItemSimpleDto(JsObject obj) {
        SyncBoxItemSimpleDto dto = new SyncBoxItemSimpleDto();
        dto.setId(obj.getInt("id"));
        dto.setItemTypeId(obj.getInt("itemTypeId"));
        JSObject posObj = obj.get("position");
        if (!JsUtils.isNullOrUndefined(posObj)) {
            dto.setPosition(safeVertex((JsObject) posObj));
        }
        return dto;
    }

    private static BoxContent convertBoxContent(JsObject obj) {
        BoxContent dto = new BoxContent();
        dto.setCrystals(obj.getInt("crystals"));
        JSObject itemsArr = obj.get("inventoryItems");
        if (!JsUtils.isNullOrUndefined(itemsArr)) {
            dto.setInventoryItems(convertInventoryItemList(itemsArr));
        }
        return dto;
    }

    private static QuestProgressInfo convertQuestProgressInfo(JsObject obj) {
        QuestProgressInfo dto = new QuestProgressInfo();
        dto.setCount(obj.getNullableInt("count"));
        dto.setSecondsRemaining(obj.getNullableInt("secondsRemaining"));
        dto.setBotBasesInformation(obj.getString("botBasesInformation"));
        JSObject typeCountObj = obj.get("typeCount");
        if (!JsUtils.isNullOrUndefined(typeCountObj)) {
            dto.setTypeCount(convertIntIntMapObject(typeCountObj));
        }
        return dto;
    }

    private static UseInventoryItem convertUseInventoryItem(JsObject obj) {
        UseInventoryItem dto = new UseInventoryItem();
        dto.setInventoryId(obj.getInt("inventoryId"));
        JSObject posArr = obj.get("positions");
        if (!JsUtils.isNullOrUndefined(posArr)) {
            dto.setPositions(convertDecimalPositionList(posArr));
        }
        return dto;
    }

    private static List<DecimalPosition> convertDecimalPositionList(JSObject arr) {
        List<DecimalPosition> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            JsObject posObj = (JsObject) jsArr.get(i);
            DecimalPosition pos = safeDecimalPosition(posObj);
            if (pos != null) {
                result.add(pos);
            }
        }
        return result;
    }

    // ============ Packet converters for server communication ============

    private static TickInfo convertTickInfo(JsObject obj) {
        TickInfo tickInfo = new TickInfo();
        tickInfo.setTickCount(obj.getDouble("tickCount"));
        JSObject syncBaseItemInfosArr = obj.get("syncBaseItemInfos");
        if (!JsUtils.isNullOrUndefined(syncBaseItemInfosArr)) {
            tickInfo.setSyncBaseItemInfos(convertSyncBaseItemInfoList(syncBaseItemInfosArr));
        }
        return tickInfo;
    }

    private static InitialSlaveSyncItemInfo convertInitialSlaveSyncItemInfo(JsObject obj) {
        InitialSlaveSyncItemInfo info = new InitialSlaveSyncItemInfo();
        info.setTickCount(obj.getDouble("tickCount"));
        info.setActualBaseId(obj.getNullableInt("actualBaseId"));

        JSObject syncBaseItemInfosArr = obj.get("syncBaseItemInfos");
        if (!JsUtils.isNullOrUndefined(syncBaseItemInfosArr)) {
            info.setSyncBaseItemInfos(convertSyncBaseItemInfoList(syncBaseItemInfosArr));
        }

        JSObject playerBaseInfosArr = obj.get("playerBaseInfos");
        if (!JsUtils.isNullOrUndefined(playerBaseInfosArr)) {
            info.setPlayerBaseInfos(convertPlayerBaseInfoList(playerBaseInfosArr));
        }

        JSObject syncResourceItemInfosArr = obj.get("syncResourceItemInfos");
        if (!JsUtils.isNullOrUndefined(syncResourceItemInfosArr)) {
            info.setSyncResourceItemInfos(convertSyncResourceItemInfoList(syncResourceItemInfosArr));
        }

        JSObject syncBoxItemInfosArr = obj.get("syncBoxItemInfos");
        if (!JsUtils.isNullOrUndefined(syncBoxItemInfosArr)) {
            info.setSyncBoxItemInfos(convertSyncBoxItemInfoList(syncBoxItemInfosArr));
        }

        return info;
    }

    private static List<SyncBaseItemInfo> convertSyncBaseItemInfoList(JSObject arr) {
        List<SyncBaseItemInfo> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            try {
                result.add(convertSyncBaseItemInfo((JsObject) jsArr.get(i)));
            } catch (Throwable t) {
                JsConsole.warn("convertSyncBaseItemInfoList: Failed to convert item at index " + i + ": " + t.getMessage());
            }
        }
        return result;
    }

    private static SyncBaseItemInfo convertSyncBaseItemInfo(JsObject obj) {
        SyncBaseItemInfo info = new SyncBaseItemInfo();
        info.setId(obj.getInt("id"));
        info.setItemTypeId(obj.getInt("itemTypeId"));
        info.setBaseId(obj.getInt("baseId"));
        info.setHealth(obj.getDouble("health"));
        info.setBuildup(obj.getDouble("buildup"));
        info.setReloadProgress(obj.getDouble("reloadProgress"));
        info.setSpawnProgress(obj.getDouble("spawnProgress"));

        JSObject syncPhysicalAreaInfoObj = obj.get("syncPhysicalAreaInfo");
        if (!JsUtils.isNullOrUndefined(syncPhysicalAreaInfoObj)) {
            info.setSyncPhysicalAreaInfo(convertSyncPhysicalAreaInfo((JsObject) syncPhysicalAreaInfoObj));
        }
        JSObject toBeBuildPositionObj = obj.get("toBeBuildPosition");
        if (!JsUtils.isNullOrUndefined(toBeBuildPositionObj)) {
            info.setToBeBuildPosition(safeDecimalPosition((JsObject) toBeBuildPositionObj));
        }
        info.setToBeBuiltTypeId(obj.getNullableInt("toBeBuiltTypeId"));
        info.setCurrentBuildup(obj.getNullableInt("currentBuildup"));
        info.setFactoryBuildupProgress(obj.getNullableDouble("factoryBuildupProgress"));
        info.setTarget(obj.getNullableInt("target"));
        info.setFollowTarget(obj.getNullableBoolean("followTarget"));

        JSObject spawnPointObj = obj.get("spawnPoint");
        if (!JsUtils.isNullOrUndefined(spawnPointObj)) {
            info.setSpawnPoint(safeDecimalPosition((JsObject) spawnPointObj));
        }
        JSObject rallyPointObj = obj.get("rallyPoint");
        if (!JsUtils.isNullOrUndefined(rallyPointObj)) {
            info.setRallyPoint(safeDecimalPosition((JsObject) rallyPointObj));
        }
        JSObject containedItemsArr = obj.get("containedItems");
        if (!JsUtils.isNullOrUndefined(containedItemsArr)) {
            info.setContainedItems(convertIntegerList(containedItemsArr));
        }
        info.setTargetContainer(obj.getNullableInt("targetContainer"));
        info.setContainedIn(obj.getNullableInt("containedIn"));

        JSObject unloadPosObj = obj.get("unloadPos");
        if (!JsUtils.isNullOrUndefined(unloadPosObj)) {
            info.setUnloadPos(safeDecimalPosition((JsObject) unloadPosObj));
        }
        info.setSyncBoxItemId(obj.getNullableInt("syncBoxItemId"));
        info.setTurretAngle(obj.getNullableDouble("turretAngle"));

        return info;
    }

    private static SyncPhysicalAreaInfo convertSyncPhysicalAreaInfo(JsObject obj) {
        SyncPhysicalAreaInfo info = new SyncPhysicalAreaInfo();
        JSObject positionObj = obj.get("position");
        if (!JsUtils.isNullOrUndefined(positionObj)) {
            info.setPosition(safeDecimalPosition((JsObject) positionObj));
        }
        info.setAngle(obj.getDouble("angle"));
        JSObject velocityObj = obj.get("velocity");
        if (!JsUtils.isNullOrUndefined(velocityObj)) {
            info.setVelocity(safeDecimalPosition((JsObject) velocityObj));
        }
        JSObject wayPositionsArr = obj.get("wayPositions");
        if (!JsUtils.isNullOrUndefined(wayPositionsArr)) {
            info.setWayPositions(convertDecimalPositionList(wayPositionsArr));
        }
        return info;
    }

    private static List<PlayerBaseInfo> convertPlayerBaseInfoList(JSObject arr) {
        List<PlayerBaseInfo> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            try {
                result.add(convertPlayerBaseInfo((JsObject) jsArr.get(i)));
            } catch (Throwable t) {
                JsConsole.warn("convertPlayerBaseInfoList: Failed to convert item at index " + i + ": " + t.getMessage());
            }
        }
        return result;
    }

    private static PlayerBaseInfo convertPlayerBaseInfo(JsObject obj) {
        PlayerBaseInfo info = new PlayerBaseInfo();
        info.setBaseId(obj.getInt("baseId"));
        info.setName(obj.getString("name"));
        String characterStr = obj.getString("character");
        if (characterStr != null && !characterStr.isEmpty()) {
            info.setCharacter(com.btxtech.shared.gameengine.datatypes.Character.valueOf(characterStr));
        }
        info.setResources(obj.getDouble("resources"));
        info.setUserId(obj.getString("userId"));
        info.setBotId(obj.getNullableInt("botId"));
        return info;
    }

    private static List<SyncResourceItemInfo> convertSyncResourceItemInfoList(JSObject arr) {
        List<SyncResourceItemInfo> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            try {
                result.add(convertSyncResourceItemInfo((JsObject) jsArr.get(i)));
            } catch (Throwable t) {
                JsConsole.warn("convertSyncResourceItemInfoList: Failed to convert item at index " + i + ": " + t.getMessage());
            }
        }
        return result;
    }

    private static SyncResourceItemInfo convertSyncResourceItemInfo(JsObject obj) {
        SyncResourceItemInfo info = new SyncResourceItemInfo();
        info.setId(obj.getInt("id"));
        info.setResourceItemTypeId(obj.getInt("resourceItemTypeId"));
        info.setAmount(obj.getDouble("amount"));
        JSObject syncPhysicalAreaInfoObj = obj.get("syncPhysicalAreaInfo");
        if (!JsUtils.isNullOrUndefined(syncPhysicalAreaInfoObj)) {
            info.setSyncPhysicalAreaInfo(convertSyncPhysicalAreaInfo((JsObject) syncPhysicalAreaInfoObj));
        }
        return info;
    }

    private static List<SyncBoxItemInfo> convertSyncBoxItemInfoList(JSObject arr) {
        List<SyncBoxItemInfo> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            try {
                result.add(convertSyncBoxItemInfo((JsObject) jsArr.get(i)));
            } catch (Throwable t) {
                JsConsole.warn("convertSyncBoxItemInfoList: Failed to convert item at index " + i + ": " + t.getMessage());
            }
        }
        return result;
    }

    private static SyncBoxItemInfo convertSyncBoxItemInfo(JsObject obj) {
        SyncBoxItemInfo info = new SyncBoxItemInfo();
        info.setId(obj.getInt("id"));
        info.setBoxItemTypeId(obj.getInt("boxItemTypeId"));
        JSObject syncPhysicalAreaInfoObj = obj.get("syncPhysicalAreaInfo");
        if (!JsUtils.isNullOrUndefined(syncPhysicalAreaInfoObj)) {
            info.setSyncPhysicalAreaInfo(convertSyncPhysicalAreaInfo((JsObject) syncPhysicalAreaInfoObj));
        }
        return info;
    }

    private static SyncItemDeletedInfo convertSyncItemDeletedInfo(JsObject obj) {
        SyncItemDeletedInfo info = new SyncItemDeletedInfo();
        info.setId(obj.getInt("id"));
        info.setExplode(obj.getBoolean("explode"));
        return info;
    }

    private static SyncItemSpawnStart convertSyncItemSpawnStart(JsObject obj) {
        SyncItemSpawnStart info = new SyncItemSpawnStart();
        info.setBaseItemTypeId(obj.getInt("baseItemTypeId"));
        JSObject positionObj = obj.get("position");
        if (!JsUtils.isNullOrUndefined(positionObj)) {
            info.setPosition(safeDecimalPosition((JsObject) positionObj));
        }
        return info;
    }

    // ============ Complex config converters (simplified) ============

    private static StaticGameConfig convertStaticGameConfig(JsObject obj) {
        StaticGameConfig config = new StaticGameConfig();
        // For TeaVM worker, we pass the config through to InitializeService
        // which only needs specific fields. Convert what's needed.
        JSObject groundConfigsArr = obj.get("groundConfigs");
        if (!JsUtils.isNullOrUndefined(groundConfigsArr)) {
            config.setGroundConfigs(convertGroundConfigList(groundConfigsArr));
        }
        JSObject terrainObjConfigsArr = obj.get("terrainObjectConfigs");
        if (!JsUtils.isNullOrUndefined(terrainObjConfigsArr)) {
            config.setTerrainObjectConfigs(convertTerrainObjectConfigList(terrainObjConfigsArr));
        }
        JSObject baseItemTypesArr = obj.get("baseItemTypes");
        if (!JsUtils.isNullOrUndefined(baseItemTypesArr)) {
            config.setBaseItemTypes(convertBaseItemTypeList(baseItemTypesArr));
        }
        JSObject resourceItemTypesArr = obj.get("resourceItemTypes");
        if (!JsUtils.isNullOrUndefined(resourceItemTypesArr)) {
            config.setResourceItemTypes(convertResourceItemTypeList(resourceItemTypesArr));
        }
        JSObject boxItemTypesArr = obj.get("boxItemTypes");
        if (!JsUtils.isNullOrUndefined(boxItemTypesArr)) {
            config.setBoxItemTypes(convertBoxItemTypeList(boxItemTypesArr));
        }
        JSObject levelConfigsArr = obj.get("levelConfigs");
        if (!JsUtils.isNullOrUndefined(levelConfigsArr)) {
            config.setLevelConfigs(convertLevelConfigList(levelConfigsArr));
        }
        JSObject inventoryItemsArr = obj.get("inventoryItems");
        if (!JsUtils.isNullOrUndefined(inventoryItemsArr)) {
            config.setInventoryItems(convertInventoryItemList(inventoryItemsArr));
        }
        return config;
    }

    private static PlanetConfig convertPlanetConfig(JsObject obj) {
        PlanetConfig config = new PlanetConfig();
        config.setId(obj.getInt("id"));
        config.setInternalName(obj.getString("internalName"));
        JSObject sizeObj = obj.get("size");
        if (!JsUtils.isNullOrUndefined(sizeObj)) {
            config.setSize(safeDecimalPosition((JsObject) sizeObj));
        }
        JSObject itemTypeLimObj = obj.get("itemTypeLimitation");
        if (!JsUtils.isNullOrUndefined(itemTypeLimObj)) {
            config.setItemTypeLimitation(convertIntIntMapObject(itemTypeLimObj));
        }
        config.setHouseSpace(obj.getInt("houseSpace"));
        config.setStartRazarion(obj.getInt("startRazarion"));
        config.setStartBaseItemTypeId(obj.getNullableInt("startBaseItemTypeId"));
        config.setGroundConfigId(obj.getNullableInt("groundConfigId"));
        return config;
    }

    private static UserContext convertUserContext(JsObject obj) {
        UserContext ctx = new UserContext();
        ctx.setUserId(obj.getString("userId"));
        String regStateStr = obj.getString("registerState");
        if (regStateStr != null && !regStateStr.isEmpty()) {
            ctx.setRegisterState(UserContext.RegisterState.valueOf(regStateStr));
        }
        ctx.setName(obj.getString("name"));
        ctx.setLevelId(obj.getNullableInt("levelId"));
        JSObject unlockedObj = obj.get("unlockedItemLimit");
        if (!JsUtils.isNullOrUndefined(unlockedObj)) {
            ctx.setUnlockedItemLimit(convertIntIntMapObject(unlockedObj));
        }
        ctx.setXp(obj.getInt("xp"));
        return ctx;
    }

    // ============ List converters ============

    private static List<Integer> convertIntegerList(JSObject arr) {
        List<Integer> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(JsUtils.asInt((JSObject) jsArr.get(i)));
        }
        return result;
    }

    private static java.util.Map<Integer, Integer> convertIntIntMapObject(JSObject mapObj) {
        java.util.Map<Integer, Integer> result = new java.util.HashMap<>();
        JsArray<String> keys = JsUtils.getObjectKeys(mapObj);
        int len = keys.getLength();
        for (int i = 0; i < len; i++) {
            String key = JsUtils.getArrayString(keys, i);
            int value = JsUtils.getObjectInt(mapObj, key);
            result.put(Integer.parseInt(key), value);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> convertGenericList(JSObject arr) {
        List<Object> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(jsArr.get(i));
        }
        return result;
    }

    // ============ Config list converters ============

    private static List<GroundConfig> convertGroundConfigList(JSObject arr) {
        List<GroundConfig> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(convertGroundConfig((JsObject) jsArr.get(i)));
        }
        return result;
    }

    private static GroundConfig convertGroundConfig(JsObject obj) {
        GroundConfig config = new GroundConfig();
        config.setId(obj.getInt("id"));
        config.setInternalName(obj.getString("internalName"));
        config.setGroundBabylonMaterialId(obj.getNullableInt("groundBabylonMaterialId"));
        config.setWaterBabylonMaterialId(obj.getNullableInt("waterBabylonMaterialId"));
        config.setUnderWaterBabylonMaterialId(obj.getNullableInt("underWaterBabylonMaterialId"));
        config.setBotBabylonMaterialId(obj.getNullableInt("botBabylonMaterialId"));
        config.setBotWallBabylonMaterialId(obj.getNullableInt("botWallBabylonMaterialId"));
        return config;
    }

    private static List<TerrainObjectConfig> convertTerrainObjectConfigList(JSObject arr) {
        List<TerrainObjectConfig> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(convertTerrainObjectConfig((JsObject) jsArr.get(i)));
        }
        return result;
    }

    private static TerrainObjectConfig convertTerrainObjectConfig(JsObject obj) {
        TerrainObjectConfig config = new TerrainObjectConfig();
        config.setId(obj.getInt("id"));
        config.setInternalName(obj.getString("internalName"));
        config.setRadius(obj.getDouble("radius"));
        config.setModel3DId(obj.getNullableInt("model3DId"));
        return config;
    }

    private static List<BaseItemType> convertBaseItemTypeList(JSObject arr) {
        List<BaseItemType> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(convertBaseItemType((JsObject) jsArr.get(i)));
        }
        return result;
    }

    private static BaseItemType convertBaseItemType(JsObject obj) {
        BaseItemType type = new BaseItemType();
        // ItemType fields
        type.setId(obj.getInt("id"));
        type.setInternalName(obj.getString("internalName"));
        type.setName(obj.getString("name"));
        type.setDescription(obj.getString("description"));
        type.setModel3DId(obj.getNullableInt("model3DId"));
        type.setThumbnail(obj.getNullableInt("thumbnail"));
        // BaseItemType specific fields
        type.setHealth(obj.getInt("health"));
        type.setPrice(obj.getInt("price"));
        type.setBuildup(obj.getInt("buildup"));
        type.setXpOnKilling(obj.getInt("xpOnKilling"));
        type.setConsumingHouseSpace(obj.getInt("consumingHouseSpace"));
        type.setDropBoxItemTypeId(obj.getNullableInt("dropBoxItemTypeId"));
        type.setDropBoxPossibility(obj.getDouble("dropBoxPossibility"));
        type.setBoxPickupRange(obj.getDouble("boxPickupRange"));
        type.setUnlockCrystals(obj.getNullableInt("unlockCrystals"));
        type.setSpawnDurationMillis(obj.getInt("spawnDurationMillis"));
        type.setSpawnParticleSystemId(obj.getNullableInt("spawnParticleSystemId"));
        type.setSpawnAudioId(obj.getNullableInt("spawnAudioId"));
        type.setDemolitionImageId(obj.getNullableInt("demolitionImageId"));
        type.setBuildupTextureId(obj.getNullableInt("buildupTextureId"));
        type.setExplosionAudioItemConfigId(obj.getNullableInt("explosionAudioItemConfigId"));
        type.setExplosionParticleId(obj.getNullableInt("explosionParticleId"));

        // PhysicalAreaConfig
        JSObject physObj = obj.get("physicalAreaConfig");
        if (!JsUtils.isNullOrUndefined(physObj)) {
            type.setPhysicalAreaConfig(convertPhysicalAreaConfig((JsObject) physObj));
        }
        // WeaponType
        JSObject weaponObj = obj.get("weaponType");
        if (!JsUtils.isNullOrUndefined(weaponObj)) {
            type.setWeaponType(convertWeaponType((JsObject) weaponObj));
        }
        // FactoryType
        JSObject factoryObj = obj.get("factoryType");
        if (!JsUtils.isNullOrUndefined(factoryObj)) {
            type.setFactoryType(convertFactoryType((JsObject) factoryObj));
        }
        // HarvesterType
        JSObject harvesterObj = obj.get("harvesterType");
        if (!JsUtils.isNullOrUndefined(harvesterObj)) {
            type.setHarvesterType(convertHarvesterType((JsObject) harvesterObj));
        }
        // BuilderType
        JSObject builderObj = obj.get("builderType");
        if (!JsUtils.isNullOrUndefined(builderObj)) {
            type.setBuilderType(convertBuilderType((JsObject) builderObj));
        }
        // GeneratorType
        JSObject generatorObj = obj.get("generatorType");
        if (!JsUtils.isNullOrUndefined(generatorObj)) {
            type.setGeneratorType(convertGeneratorType((JsObject) generatorObj));
        }
        // ConsumerType
        JSObject consumerObj = obj.get("consumerType");
        if (!JsUtils.isNullOrUndefined(consumerObj)) {
            type.setConsumerType(convertConsumerType((JsObject) consumerObj));
        }
        // ItemContainerType
        JSObject containerObj = obj.get("itemContainerType");
        if (!JsUtils.isNullOrUndefined(containerObj)) {
            type.setItemContainerType(convertItemContainerType((JsObject) containerObj));
        }
        // HouseType
        JSObject houseObj = obj.get("houseType");
        if (!JsUtils.isNullOrUndefined(houseObj)) {
            type.setHouseType(convertHouseType((JsObject) houseObj));
        }
        // SpecialType
        JSObject specialObj = obj.get("specialType");
        if (!JsUtils.isNullOrUndefined(specialObj)) {
            type.setSpecialType(convertSpecialType((JsObject) specialObj));
        }
        // DemolitionStepEffects
        JSObject demolitionArr = obj.get("demolitionStepEffects");
        if (!JsUtils.isNullOrUndefined(demolitionArr)) {
            type.setDemolitionStepEffects(convertDemolitionStepEffectList(demolitionArr));
        }
        return type;
    }

    private static PhysicalAreaConfig convertPhysicalAreaConfig(JsObject obj) {
        PhysicalAreaConfig config = new PhysicalAreaConfig();
        config.setRadius(obj.getDouble("radius"));
        config.setSpeed(obj.getDouble("speed"));
        config.setAcceleration(obj.getDouble("acceleration"));
        config.setAngularVelocity(obj.getDouble("angularVelocity"));
        config.setTerrainType(convertTerrainTypeEnum(obj.getString("terrainType")));
        return config;
    }

    private static TerrainType convertTerrainTypeEnum(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return TerrainType.valueOf(value);
    }

    private static WeaponType convertWeaponType(JsObject obj) {
        WeaponType type = new WeaponType();
        type.setRange(obj.getDouble("range"));
        type.setDamage(obj.getInt("damage"));
        type.setDetonationRadius(obj.getDouble("detonationRadius"));
        type.setReloadTime(obj.getDouble("reloadTime"));
        JSObject disallowedArr = obj.get("disallowedItemTypes");
        if (!JsUtils.isNullOrUndefined(disallowedArr)) {
            type.setDisallowedItemTypes(convertIntegerList(disallowedArr));
        }
        JSObject projectileSpeedObj = obj.get("projectileSpeed");
        if (!JsUtils.isNullOrUndefined(projectileSpeedObj)) {
            type.setProjectileSpeed(JsUtils.asDouble(projectileSpeedObj));
        }
        type.setImpactParticleSystemId(obj.getNullableInt("impactParticleSystemId"));
        JSObject turretAngleObj = obj.get("turretAngleVelocity");
        if (!JsUtils.isNullOrUndefined(turretAngleObj)) {
            type.setTurretAngleVelocity(JsUtils.asDouble(turretAngleObj));
        }
        type.setMuzzleFlashAudioItemConfigId(obj.getNullableInt("muzzleFlashAudioItemConfigId"));
        type.setTrailParticleSystemConfigId(obj.getNullableInt("trailParticleSystemConfigId"));
        return type;
    }

    private static FactoryType convertFactoryType(JsObject obj) {
        FactoryType type = new FactoryType();
        type.setProgress(obj.getDouble("progress"));
        JSObject ableToBuildArr = obj.get("ableToBuildIds");
        if (!JsUtils.isNullOrUndefined(ableToBuildArr)) {
            type.setAbleToBuildIds(convertIntegerList(ableToBuildArr));
        }
        return type;
    }

    private static HarvesterType convertHarvesterType(JsObject obj) {
        HarvesterType type = new HarvesterType();
        type.setRange(obj.getInt("range"));
        type.setProgress(obj.getDouble("progress"));
        type.setParticleSystemConfigId(obj.getNullableInt("particleSystemConfigId"));
        return type;
    }

    private static BuilderType convertBuilderType(JsObject obj) {
        BuilderType type = new BuilderType();
        type.setRange(obj.getDouble("range"));
        type.setProgress(obj.getDouble("progress"));
        JSObject ableToBuildArr = obj.get("ableToBuildIds");
        if (!JsUtils.isNullOrUndefined(ableToBuildArr)) {
            type.setAbleToBuildIds(convertIntegerList(ableToBuildArr));
        }
        return type;
    }

    private static GeneratorType convertGeneratorType(JsObject obj) {
        GeneratorType type = new GeneratorType();
        type.setWattage(obj.getInt("wattage"));
        return type;
    }

    private static ConsumerType convertConsumerType(JsObject obj) {
        ConsumerType type = new ConsumerType();
        type.setWattage(obj.getInt("wattage"));
        return type;
    }

    private static ItemContainerType convertItemContainerType(JsObject obj) {
        ItemContainerType type = new ItemContainerType();
        type.setRange(obj.getDouble("range"));
        type.setMaxCount(obj.getInt("maxCount"));
        JSObject ableToContainArr = obj.get("ableToContain");
        if (!JsUtils.isNullOrUndefined(ableToContainArr)) {
            type.setAbleToContain(convertIntegerList(ableToContainArr));
        }
        return type;
    }

    private static HouseType convertHouseType(JsObject obj) {
        HouseType type = new HouseType();
        type.setSpace(obj.getInt("space"));
        return type;
    }

    private static SpecialType convertSpecialType(JsObject obj) {
        SpecialType type = new SpecialType();
        type.setMiniTerrain(JsUtils.asBoolean(obj.get("miniTerrain")));
        return type;
    }

    private static List<DemolitionStepEffect> convertDemolitionStepEffectList(JSObject arr) {
        List<DemolitionStepEffect> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(convertDemolitionStepEffect((JsObject) jsArr.get(i)));
        }
        return result;
    }

    private static DemolitionStepEffect convertDemolitionStepEffect(JsObject obj) {
        // DemolitionStepEffect has no fields
        return new DemolitionStepEffect();
    }

    private static List<ResourceItemType> convertResourceItemTypeList(JSObject arr) {
        List<ResourceItemType> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(convertResourceItemType((JsObject) jsArr.get(i)));
        }
        return result;
    }

    private static ResourceItemType convertResourceItemType(JsObject obj) {
        ResourceItemType type = new ResourceItemType();
        type.setId(obj.getInt("id"));
        type.setInternalName(obj.getString("internalName"));
        type.setName(obj.getString("name"));
        type.setDescription(obj.getString("description"));
        type.setModel3DId(obj.getNullableInt("model3DId"));
        type.setThumbnail(obj.getNullableInt("thumbnail"));
        type.setRadius(obj.getDouble("radius"));
        type.setAmount(obj.getInt("amount"));
        return type;
    }

    private static List<BoxItemType> convertBoxItemTypeList(JSObject arr) {
        List<BoxItemType> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(convertBoxItemType((JsObject) jsArr.get(i)));
        }
        return result;
    }

    private static BoxItemType convertBoxItemType(JsObject obj) {
        BoxItemType type = new BoxItemType();
        type.setId(obj.getInt("id"));
        type.setInternalName(obj.getString("internalName"));
        type.setName(obj.getString("name"));
        type.setDescription(obj.getString("description"));
        type.setModel3DId(obj.getNullableInt("model3DId"));
        type.setThumbnail(obj.getNullableInt("thumbnail"));
        type.setRadius(obj.getDouble("radius"));
        type.setTtl(obj.getNullableInt("ttl"));
        JSObject possibilitiesArr = obj.get("boxItemTypePossibilities");
        if (!JsUtils.isNullOrUndefined(possibilitiesArr)) {
            type.setBoxItemTypePossibilities(convertBoxItemTypePossibilityList(possibilitiesArr));
        }
        return type;
    }

    private static List<BoxItemTypePossibility> convertBoxItemTypePossibilityList(JSObject arr) {
        List<BoxItemTypePossibility> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(convertBoxItemTypePossibility((JsObject) jsArr.get(i)));
        }
        return result;
    }

    private static BoxItemTypePossibility convertBoxItemTypePossibility(JsObject obj) {
        BoxItemTypePossibility poss = new BoxItemTypePossibility();
        poss.setPossibility(obj.getDouble("possibility"));
        poss.setInventoryItemId(obj.getNullableInt("inventoryItemId"));
        poss.setCrystals(obj.getNullableInt("crystals"));
        return poss;
    }

    private static List<LevelConfig> convertLevelConfigList(JSObject arr) {
        List<LevelConfig> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(convertLevelConfig((JsObject) jsArr.get(i)));
        }
        return result;
    }

    private static LevelConfig convertLevelConfig(JsObject obj) {
        LevelConfig config = new LevelConfig();
        config.setId(obj.getInt("id"));
        config.setInternalName(obj.getString("internalName"));
        config.setNumber(obj.getInt("number"));
        config.setXp2LevelUp(obj.getInt("xp2LevelUp"));
        JSObject unlockConfigsArr = obj.get("levelUnlockConfigs");
        if (!JsUtils.isNullOrUndefined(unlockConfigsArr)) {
            config.setLevelUnlockConfigs(convertLevelUnlockConfigList(unlockConfigsArr));
        }
        return config;
    }

    private static List<LevelUnlockConfig> convertLevelUnlockConfigList(JSObject arr) {
        List<LevelUnlockConfig> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(convertLevelUnlockConfig((JsObject) jsArr.get(i)));
        }
        return result;
    }

    private static LevelUnlockConfig convertLevelUnlockConfig(JsObject obj) {
        LevelUnlockConfig config = new LevelUnlockConfig();
        config.setId(obj.getNullableInt("id"));
        config.setInternalName(obj.getString("internalName"));
        config.setThumbnail(obj.getNullableInt("thumbnail"));
        JSObject i18nNameObj = obj.get("i18nName");
        if (!JsUtils.isNullOrUndefined(i18nNameObj)) {
            config.setI18nName(convertI18nString((JsObject) i18nNameObj));
        }
        JSObject i18nDescObj = obj.get("i18nDescription");
        if (!JsUtils.isNullOrUndefined(i18nDescObj)) {
            config.setI18nDescription(convertI18nString((JsObject) i18nDescObj));
        }
        config.setBaseItemType(obj.getNullableInt("baseItemType"));
        config.setBaseItemTypeCount(obj.getInt("baseItemTypeCount"));
        config.setCrystalCost(obj.getInt("crystalCost"));
        return config;
    }

    private static I18nString convertI18nString(JsObject obj) {
        I18nString str = new I18nString();
        str.setString(obj.getString("string"));
        return str;
    }

    private static List<InventoryItem> convertInventoryItemList(JSObject arr) {
        List<InventoryItem> result = new ArrayList<>();
        JsArray<Object> jsArr = (JsArray<Object>) arr;
        int len = jsArr.getLength();
        for (int i = 0; i < len; i++) {
            result.add(convertInventoryItem((JsObject) jsArr.get(i)));
        }
        return result;
    }

    private static InventoryItem convertInventoryItem(JsObject obj) {
        InventoryItem item = new InventoryItem();
        item.setId(obj.getInt("id"));
        item.setInternalName(obj.getString("internalName"));
        item.setRazarion(obj.getNullableInt("razarion"));
        item.setCrystalCost(obj.getNullableInt("crystalCost"));
        item.setBaseItemTypeId(obj.getNullableInt("baseItemTypeId"));
        item.setBaseItemTypeCount(obj.getInt("baseItemTypeCount"));
        item.setBaseItemTypeFreeRange(obj.getDouble("baseItemTypeFreeRange"));
        item.setImageId(obj.getNullableInt("imageId"));
        JSObject i18nNameObj = obj.get("i18nName");
        if (!JsUtils.isNullOrUndefined(i18nNameObj)) {
            item.setI18nName(convertI18nString((JsObject) i18nNameObj));
        }
        return item;
    }

    // ============ Java to JsObject converter ============

    private static JsObject javaToJsObject(Object obj) {
        if (obj == null) {
            return null;
        }
        JsObject result = JsObject.create();

        if (obj instanceof DecimalPosition) {
            DecimalPosition pos = (DecimalPosition) obj;
            result.set("x", pos.getX());
            result.set("y", pos.getY());
        } else if (obj instanceof Index) {
            Index idx = (Index) obj;
            result.set("x", idx.getX());
            result.set("y", idx.getY());
        } else if (obj instanceof Vertex) {
            Vertex v = (Vertex) obj;
            result.set("x", v.getX());
            result.set("y", v.getY());
            result.set("z", v.getZ());
        } else if (obj instanceof IdsDto) {
            IdsDto ids = (IdsDto) obj;
            if (ids.getIds() != null) {
                JsArray<Object> arr = JsArray.create();
                for (Integer id : ids.getIds()) {
                    arr.push(id);
                }
                result.set("ids", arr);
            }
        } else if (obj instanceof PlayerBaseDto) {
            PlayerBaseDto dto = (PlayerBaseDto) obj;
            result.set("baseId", dto.getBaseId());
            result.set("name", dto.getName());
            if (dto.getCharacter() != null) {
                result.set("character", dto.getCharacter().name());
            }
            result.set("userId", dto.getUserId());
            result.setNullableInt("botId", dto.getBotId());
        } else if (obj instanceof SyncResourceItemSimpleDto) {
            SyncResourceItemSimpleDto dto = (SyncResourceItemSimpleDto) obj;
            result.set("id", dto.getId());
            result.set("itemTypeId", dto.getItemTypeId());
            if (dto.getPosition() != null) {
                result.set("position", javaToJsObject(dto.getPosition()));
            }
        } else if (obj instanceof SyncBoxItemSimpleDto) {
            SyncBoxItemSimpleDto dto = (SyncBoxItemSimpleDto) obj;
            result.set("id", dto.getId());
            result.set("itemTypeId", dto.getItemTypeId());
            if (dto.getPosition() != null) {
                result.set("position", javaToJsObject(dto.getPosition()));
            }
        } else if (obj instanceof BoxContent) {
            BoxContent dto = (BoxContent) obj;
            result.set("crystals", dto.getCrystals());
            // inventoryItems is a List - serialize as array
            if (dto.getInventoryItems() != null && !dto.getInventoryItems().isEmpty()) {
                JsArray<Object> arr = JsArray.create();
                for (InventoryItem item : dto.getInventoryItems()) {
                    arr.push(javaToJsObject(item));
                }
                result.set("inventoryItems", arr);
            }
        } else if (obj instanceof QuestProgressInfo) {
            QuestProgressInfo dto = (QuestProgressInfo) obj;
            result.setNullableInt("count", dto.getCount());
            result.setNullableInt("secondsRemaining", dto.getSecondsRemaining());
            result.set("botBasesInformation", dto.getBotBasesInformation());
            // typeCount is a Map - serialize as object
            if (dto.getTypeCount() != null) {
                JsObject mapObj = JsObject.create();
                for (java.util.Map.Entry<Integer, Integer> entry : dto.getTypeCount().entrySet()) {
                    mapObj.set(String.valueOf(entry.getKey()), entry.getValue().intValue());
                }
                result.set("typeCount", mapObj);
            }
        } else if (obj instanceof InventoryItem) {
            InventoryItem item = (InventoryItem) obj;
            result.set("id", item.getId());
            result.set("internalName", item.getInternalName());
            result.setNullableInt("razarion", item.getRazarion());
            result.setNullableInt("crystalCost", item.getCrystalCost());
            result.setNullableInt("baseItemTypeId", item.getBaseItemTypeId());
            result.set("baseItemTypeCount", item.getBaseItemTypeCount());
            result.set("baseItemTypeFreeRange", item.getBaseItemTypeFreeRange());
            result.setNullableInt("imageId", item.getImageId());
        } else {
            JsConsole.warn("javaToJsObject: Unsupported type: " + obj.getClass().getName());
            return null;
        }

        return result;
    }

    // Type cast helper - casts JavaScript object to Uint16ArrayEmu via double-cast
    @SuppressWarnings("unchecked")
    private static Uint16ArrayEmu toUint16ArrayEmu(Object obj) {
        return (Uint16ArrayEmu) obj;
    }

    // ============ NativeTickInfo conversion to JavaScript object ============

    private static JsNativeTickInfo convertNativeTickInfoToJs(NativeTickInfo nativeTickInfo) {
        if (nativeTickInfo == null) {
            return null;
        }

        JsNativeTickInfo jsTickInfo = JsNativeTickInfo.create();
        jsTickInfo.setResources(nativeTickInfo.resources);
        jsTickInfo.setXpFromKills(nativeTickInfo.xpFromKills);
        jsTickInfo.setHouseSpace(nativeTickInfo.houseSpace);

        // Convert updatedNativeSyncBaseItemTickInfos
        if (nativeTickInfo.updatedNativeSyncBaseItemTickInfos != null) {
            JsNativeSyncBaseItemTickInfo[] jsUpdated = new JsNativeSyncBaseItemTickInfo[nativeTickInfo.updatedNativeSyncBaseItemTickInfos.length];
            for (int i = 0; i < nativeTickInfo.updatedNativeSyncBaseItemTickInfos.length; i++) {
                jsUpdated[i] = convertNativeSyncBaseItemTickInfoToJs(nativeTickInfo.updatedNativeSyncBaseItemTickInfos[i]);
            }
            jsTickInfo.setUpdatedNativeSyncBaseItemTickInfos(jsUpdated);
        } else {
            jsTickInfo.setUpdatedNativeSyncBaseItemTickInfos(new JsNativeSyncBaseItemTickInfo[0]);
        }

        // Convert killedSyncBaseItems
        if (nativeTickInfo.killedSyncBaseItems != null) {
            JsNativeSimpleSyncBaseItemTickInfo[] jsKilled = new JsNativeSimpleSyncBaseItemTickInfo[nativeTickInfo.killedSyncBaseItems.length];
            for (int i = 0; i < nativeTickInfo.killedSyncBaseItems.length; i++) {
                jsKilled[i] = convertNativeSimpleSyncBaseItemTickInfoToJs(nativeTickInfo.killedSyncBaseItems[i]);
            }
            jsTickInfo.setKilledSyncBaseItems(jsKilled);
        } else {
            jsTickInfo.setKilledSyncBaseItems(new JsNativeSimpleSyncBaseItemTickInfo[0]);
        }

        // Convert removeSyncBaseItemIds
        if (nativeTickInfo.removeSyncBaseItemIds != null) {
            jsTickInfo.setRemoveSyncBaseItemIds(nativeTickInfo.removeSyncBaseItemIds);
        } else {
            jsTickInfo.setRemoveSyncBaseItemIds(new int[0]);
        }

        return jsTickInfo;
    }

    private static JsNativeSyncBaseItemTickInfo convertNativeSyncBaseItemTickInfoToJs(NativeSyncBaseItemTickInfo info) {
        if (info == null) {
            return null;
        }

        JsNativeSyncBaseItemTickInfo jsInfo = JsNativeSyncBaseItemTickInfo.create();
        jsInfo.setId(info.id);
        jsInfo.setItemTypeId(info.itemTypeId);
        jsInfo.setX(info.x);
        jsInfo.setY(info.y);
        jsInfo.setZ(info.z);
        jsInfo.setAngle(info.angle);
        jsInfo.setBaseId(info.baseId);
        jsInfo.setTurretAngle(info.turretAngle);
        jsInfo.setSpawning(info.spawning);
        jsInfo.setBuildup(info.buildup);
        jsInfo.setHealth(info.health);
        jsInfo.setConstructing(info.constructing);
        jsInfo.setConstructingBaseItemTypeId(info.constructingBaseItemTypeId);

        if (info.harvestingResourcePosition != null) {
            jsInfo.setHarvestingResourcePosition(convertNativeDecimalPositionToJs(info.harvestingResourcePosition));
        }
        if (info.buildingPosition != null) {
            jsInfo.setBuildingPosition(convertNativeDecimalPositionToJs(info.buildingPosition));
        }

        if (info.containingItemTypeIds != null) {
            jsInfo.setContainingItemTypeIds(info.containingItemTypeIds);
        }
        jsInfo.setMaxContainingRadius(info.maxContainingRadius);
        jsInfo.setContained(info.contained);
        jsInfo.setIdle(info.idle);

        return jsInfo;
    }

    private static JsNativeSimpleSyncBaseItemTickInfo convertNativeSimpleSyncBaseItemTickInfoToJs(NativeSimpleSyncBaseItemTickInfo info) {
        if (info == null) {
            return null;
        }

        JsNativeSimpleSyncBaseItemTickInfo jsInfo = JsNativeSimpleSyncBaseItemTickInfo.create();
        jsInfo.setId(info.id);
        jsInfo.setItemTypeId(info.itemTypeId);
        jsInfo.setContained(info.contained);
        jsInfo.setX(info.x);
        jsInfo.setY(info.y);

        return jsInfo;
    }

    private static JsNativeDecimalPosition convertNativeDecimalPositionToJs(NativeDecimalPosition pos) {
        if (pos == null) {
            return null;
        }
        return JsNativeDecimalPosition.create(pos.x, pos.y);
    }
}

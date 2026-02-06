package com.btxtech.client;

import com.btxtech.client.jso.JsArray;
import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsJson;
import com.btxtech.client.jso.JsObject;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
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
import com.btxtech.client.jso.JsUint16ArrayWrapper;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Marshaller for communication between main thread and Web Worker.
 * Uses JsArray-based protocol matching the worker-teavm TeaVMWorkerMarshaller.
 */
public class TeaVMClientMarshaller {

    private static final int COMMAND_OFFSET = 0;
    private static final int DATA_OFFSET_0 = 1;
    private static final int DATA_OFFSET_1 = 2;
    private static final int DATA_OFFSET_2 = 3;
    private static final int DATA_OFFSET_3 = 4;
    private static final int DATA_OFFSET_4 = 5;

    /**
     * Raw JsObject from the ColdGameUiContext REST response.
     * Used for forwarding complex config data to the worker without re-serialization.
     */
    private static JsObject rawColdContext;

    public static void storeRawColdContext(JsObject json) {
        rawColdContext = json;
    }

    // ============ Marshalling (client → worker) ============

    public static JSObject marshall(GameEngineControlPackage controlPackage) {
        JsArray<Object> array = JsArray.create();
        setArrayString(array, COMMAND_OFFSET, controlPackage.getCommand().name());

        switch (controlPackage.getCommand()) {
            // No data
            case STOP_REQUEST:
            case PERFMON_REQUEST:
            case TICK_UPDATE_REQUEST:
                break;

            case INITIALIZE:
                // StaticGameConfig and PlanetConfig: use raw JSON from server to avoid re-serialization
                if (rawColdContext != null) {
                    setArrayString(array, DATA_OFFSET_0, stringify(rawColdContext.get("staticGameConfig")));
                    JSObject warmCtx = rawColdContext.get("warmGameUiContext");
                    setArrayString(array, DATA_OFFSET_1, stringify(JsObject.cast(warmCtx).get("planetConfig")));
                } else {
                    setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                    setArrayString(array, DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                }
                setArrayString(array, DATA_OFFSET_2, toJson(controlPackage.getData(2))); // UserContext
                setArrayString(array, DATA_OFFSET_3, toJson(controlPackage.getData(3))); // GameEngineMode
                setArrayString(array, DATA_OFFSET_4, toJson(controlPackage.getData(4))); // String (gameSessionUuid)
                break;

            case INITIALIZE_WARM:
                setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0))); // PlanetConfig
                setArrayString(array, DATA_OFFSET_1, toJson(controlPackage.getData(1))); // UserContext
                setArrayString(array, DATA_OFFSET_2, toJson(controlPackage.getData(2))); // GameEngineMode
                setArrayString(array, DATA_OFFSET_3, toJson(controlPackage.getData(3))); // String
                break;

            case START:
                setArrayString(array, DATA_OFFSET_0, (String) controlPackage.getData(0)); // raw bearer token
                break;

            // Single JSON data
            case START_BOTS:
            case EXECUTE_BOT_COMMANDS:
            case CREATE_RESOURCES:
            case CREATE_BOXES:
            case ACTIVATE_QUEST:
            case UPDATE_LEVEL:
            case SELL_ITEMS:
            case USE_INVENTORY_ITEM:
            case TERRAIN_TILE_REQUEST:
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
                setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                setArrayString(array, DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                break;

            // Triple JSON data
            case COMMAND_BUILD:
                setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                setArrayString(array, DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                setArrayString(array, DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                break;

            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                setArrayString(array, DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                setArrayString(array, DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                setArrayString(array, DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                setArrayString(array, DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                setArrayString(array, DATA_OFFSET_4, toJson(controlPackage.getData(4)));
                break;

            default:
                JsConsole.warn("TeaVMClientMarshaller.marshall: unhandled command: " + controlPackage.getCommand());
                break;
        }
        return array;
    }

    // ============ Demarshalling (worker → client) ============

    public static GameEngineControlPackage deMarshall(JSObject javaScriptObject) {
        try {
            // Check if the object is an array
            if (!isArray(javaScriptObject)) {
                JsConsole.error("TeaVMClientMarshaller.deMarshall: received non-array object: " + stringify(javaScriptObject));
                throw new IllegalArgumentException("Expected array but got: " + getObjectType(javaScriptObject));
            }
            // Use JSObject directly instead of casting to avoid WASM-GC type checking issues
            String commandName = getArrayStringDirect(javaScriptObject, COMMAND_OFFSET);
            if (commandName == null || commandName.isEmpty()) {
                JsConsole.error("TeaVMClientMarshaller.deMarshall: command name is null or empty. Array: " + stringify(javaScriptObject));
                throw new IllegalArgumentException("Command name is null or empty");
            }

            GameEngineControlPackage.Command command;
            try {
                command = GameEngineControlPackage.Command.valueOf(commandName);
            } catch (Throwable t) {
                JsConsole.error("TeaVMClientMarshaller.deMarshall: unknown command: " + commandName);
                throw t;
            }

            List<Object> data = new ArrayList<>();
            try {
                demarshallData(command, javaScriptObject, data);
            } catch (Throwable t) {
                JsConsole.error("TeaVMClientMarshaller.deMarshall: error processing command " + commandName + ": " + t.getMessage());
                throw t;
            }

            return new GameEngineControlPackage(command, data.toArray());
        } catch (Throwable t) {
            JsConsole.error("TeaVMClientMarshaller.deMarshall: unexpected error: " + t.getMessage());
            try {
                JsConsole.error("  Message data: " + stringify(javaScriptObject));
            } catch (Throwable t2) {
                JsConsole.error("  (Could not stringify message data)");
            }
            throw t;
        }
    }

    private static void demarshallData(GameEngineControlPackage.Command command, JSObject javaScriptObject, List<Object> data) {
        switch (command) {
            // No data
            case LOADED:
            case STOP_RESPONSE:
            case QUEST_PASSED:
            case TICK_UPDATE_RESPONSE_FAIL:
            case CONNECTION_LOST:
            case INITIAL_SLAVE_SYNCHRONIZED_NO_BASE:
            case COMMAND_MOVE_ACK:
            case INITIALIZED:
                break;

            case INITIALISING_FAILED:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), String.class));
                break;

            // Native JS objects - pass through as-is
            case TICK_UPDATE_RESPONSE:
            case SYNC_ITEM_START_SPAWNED:
            case SYNC_ITEM_IDLE:
                data.add(getArrayElementDirect(javaScriptObject, DATA_OFFSET_0));
                break;

            case RESOURCE_CREATED:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), SyncResourceItemSimpleDto.class));
                break;

            case RESOURCE_DELETED:
            case BOX_DELETED:
            case BASE_DELETED:
            case UPDATE_LEVEL:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), Integer.class));
                break;

            case ENERGY_CHANGED:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), Integer.class));
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_1), Integer.class));
                break;

            case BASE_CREATED:
            case BASE_UPDATED:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), PlayerBaseDto.class));
                break;

            case BOX_CREATED:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), SyncBoxItemSimpleDto.class));
                break;

            case BOX_PICKED:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), BoxContent.class));
                break;

            case PROJECTILE_FIRED:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), Integer.class));
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_1), Integer.class));
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_2), DecimalPosition.class));
                break;

            case PROJECTILE_DETONATION:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), Integer.class));
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_1), DecimalPosition.class));
                break;

            case PERFMON_RESPONSE:
                // Pass through as raw data for now
                data.add(new ArrayList<>());
                break;

            case TERRAIN_TILE_RESPONSE:
                data.add(demarshallTerrainTile(getArrayElementDirect(javaScriptObject, DATA_OFFSET_0)));
                break;

            case QUEST_PROGRESS:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), QuestProgressInfo.class));
                break;

            case INITIAL_SLAVE_SYNCHRONIZED:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), DecimalPosition.class));
                break;

            case GET_TERRAIN_TYPE_ANSWER:
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0), Index.class));
                data.add(fromJson(getArrayStringDirect(javaScriptObject, DATA_OFFSET_1), TerrainType.class));
                break;

            case START:
                data.add(getArrayStringDirect(javaScriptObject, DATA_OFFSET_0));
                break;

            default:
                JsConsole.warn("TeaVMClientMarshaller.deMarshall: unhandled command: " + command);
                break;
        }
    }

    // ============ toJson: Java object → JSON string ============

    private static String toJson(Object object) {
        if (object == null) {
            return "null";
        }
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
        // Complex Java objects
        JsObject jsObj = javaToJsObject(object);
        if (jsObj != null) {
            return stringify(jsObj);
        }
        JsConsole.warn("TeaVMClientMarshaller.toJson: unsupported type: " + object.getClass().getName());
        return "null";
    }

    @SuppressWarnings("unchecked")
    private static JsObject javaToJsObject(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof DecimalPosition) {
            DecimalPosition pos = (DecimalPosition) obj;
            JsObject result = JsObject.create();
            result.set("x", pos.getX());
            result.set("y", pos.getY());
            return result;
        }
        if (obj instanceof Index) {
            Index idx = (Index) obj;
            JsObject result = JsObject.create();
            result.set("x", idx.getX());
            result.set("y", idx.getY());
            return result;
        }
        if (obj instanceof UserContext) {
            return userContextToJs((UserContext) obj);
        }
        if (obj instanceof PlanetConfig) {
            return planetConfigToJs((PlanetConfig) obj);
        }
        if (obj instanceof IdsDto) {
            IdsDto ids = (IdsDto) obj;
            JsObject result = JsObject.create();
            if (ids.getIds() != null) {
                JsArray<Object> arr = JsArray.create();
                for (Integer id : ids.getIds()) {
                    pushInt(arr, id);
                }
                result.set("ids", arr);
            }
            return result;
        }
        if (obj instanceof IntIntMap) {
            IntIntMap intIntMap = (IntIntMap) obj;
            JsObject result = JsObject.create();
            if (intIntMap.getMap() != null) {
                JsObject mapObj = JsObject.create();
                for (Map.Entry<Integer, Integer> entry : intIntMap.getMap().entrySet()) {
                    mapObj.set(String.valueOf(entry.getKey()), entry.getValue().intValue());
                }
                result.set("map", mapObj);
            }
            return result;
        }
        if (obj instanceof List) {
            return listToJsArray((List<?>) obj);
        }
        JsConsole.warn("TeaVMClientMarshaller.javaToJsObject: unsupported type: " + obj.getClass().getName());
        return null;
    }

    private static JsObject userContextToJs(UserContext ctx) {
        JsObject result = JsObject.create();
        if (ctx.getUserId() != null) result.set("userId", ctx.getUserId());
        if (ctx.getRegisterState() != null) result.set("registerState", ctx.getRegisterState().name());
        if (ctx.getName() != null) result.set("name", ctx.getName());
        if (ctx.getLevelId() != null) result.set("levelId", ctx.getLevelId().intValue());
        if (ctx.getUnlockedItemLimit() != null) {
            JsObject mapObj = JsObject.create();
            for (Map.Entry<Integer, Integer> entry : ctx.getUnlockedItemLimit().entrySet()) {
                mapObj.set(String.valueOf(entry.getKey()), entry.getValue().intValue());
            }
            result.set("unlockedItemLimit", mapObj);
        }
        result.set("xp", ctx.getXp());
        return result;
    }

    private static JsObject planetConfigToJs(PlanetConfig config) {
        JsObject result = JsObject.create();
        result.set("id", config.getId());
        if (config.getInternalName() != null) result.set("internalName", config.getInternalName());
        if (config.getSize() != null) {
            result.set("size", javaToJsObject(config.getSize()));
        }
        if (config.getItemTypeLimitation() != null) {
            JsObject mapObj = JsObject.create();
            for (Map.Entry<Integer, Integer> entry : config.getItemTypeLimitation().entrySet()) {
                mapObj.set(String.valueOf(entry.getKey()), entry.getValue().intValue());
            }
            result.set("itemTypeLimitation", mapObj);
        }
        result.set("houseSpace", config.getHouseSpace());
        result.set("startRazarion", config.getStartRazarion());
        if (config.getStartBaseItemTypeId() != null) result.set("startBaseItemTypeId", config.getStartBaseItemTypeId().intValue());
        if (config.getGroundConfigId() != null) result.set("groundConfigId", config.getGroundConfigId().intValue());
        return result;
    }

    @SuppressWarnings("unchecked")
    private static JsObject listToJsArray(List<?> list) {
        JsArray<Object> arr = JsArray.create();
        for (Object item : list) {
            if (item == null) {
                continue;
            }
            JsObject jsItem = javaToJsObject(item);
            if (jsItem != null) {
                arr.push(jsItem);
            } else {
                // Primitive or enum - push as string
                arr.push(toJson(item));
            }
        }
        // Return the array cast as JsObject
        return JsObject.cast(arr);
    }

    // ============ fromJson: JSON string → Java object ============

    @SuppressWarnings("unchecked")
    private static <T> T fromJson(String json, Class<T> type) {
        if ("null".equals(json) || json == null || json.isEmpty()) {
            return null;
        }
        if (type == String.class) {
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
            return (T) GameEngineMode.valueOf(json.replace("\"", ""));
        }
        if (type == TerrainType.class) {
            return (T) TerrainType.valueOf(json.replace("\"", ""));
        }

        // Parse JSON and convert
        JSObject jsObj = JsJson.parse(json);
        return jsObjectToJava(jsObj, type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T jsObjectToJava(JSObject jsObj, Class<T> type) {
        if (isNullOrUndefined(jsObj)) {
            return null;
        }
        JsObject obj = JsObject.cast(jsObj);

        if (type == DecimalPosition.class) {
            return (T) new DecimalPosition(obj.getDouble("x"), obj.getDouble("y"));
        }
        if (type == Index.class) {
            return (T) new Index(obj.getInt("x"), obj.getInt("y"));
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
        JsConsole.warn("TeaVMClientMarshaller.jsObjectToJava: unsupported type: " + type.getName());
        return null;
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
        if (!isNullOrUndefined(posObj)) {
            JsObject pos = JsObject.cast(posObj);
            dto.setPosition(new Vertex(pos.getDouble("x"), pos.getDouble("y"), pos.getDouble("z")));
        }
        return dto;
    }

    private static SyncBoxItemSimpleDto convertSyncBoxItemSimpleDto(JsObject obj) {
        SyncBoxItemSimpleDto dto = new SyncBoxItemSimpleDto();
        dto.setId(obj.getInt("id"));
        dto.setItemTypeId(obj.getInt("itemTypeId"));
        JSObject posObj = obj.get("position");
        if (!isNullOrUndefined(posObj)) {
            JsObject pos = JsObject.cast(posObj);
            dto.setPosition(new Vertex(pos.getDouble("x"), pos.getDouble("y"), pos.getDouble("z")));
        }
        return dto;
    }

    private static BoxContent convertBoxContent(JsObject obj) {
        BoxContent dto = new BoxContent();
        dto.setCrystals(obj.getInt("crystals"));
        JSObject itemsArr = obj.get("inventoryItems");
        if (!isNullOrUndefined(itemsArr)) {
            List<InventoryItem> items = new ArrayList<>();
            int length = getArrayLength(itemsArr);
            for (int i = 0; i < length; i++) {
                JsObject itemObj = JsObject.cast(getArrayElementDirect(itemsArr, i));
                InventoryItem item = new InventoryItem();
                item.setId(itemObj.getInt("id"));
                item.setInternalName(itemObj.getString("internalName"));
                item.setRazarion(itemObj.getNullableInt("razarion"));
                item.setCrystalCost(itemObj.getNullableInt("crystalCost"));
                item.setBaseItemTypeId(itemObj.getNullableInt("baseItemTypeId"));
                item.setBaseItemTypeCount(itemObj.getInt("baseItemTypeCount"));
                item.setBaseItemTypeFreeRange(itemObj.getInt("baseItemTypeFreeRange"));
                item.setImageId(itemObj.getNullableInt("imageId"));
                items.add(item);
            }
            dto.setInventoryItems(items);
        }
        return dto;
    }

    private static QuestProgressInfo convertQuestProgressInfo(JsObject obj) {
        QuestProgressInfo dto = new QuestProgressInfo();
        dto.setCount(obj.getNullableInt("count"));
        dto.setSecondsRemaining(obj.getNullableInt("secondsRemaining"));
        dto.setBotBasesInformation(obj.getString("botBasesInformation"));
        JSObject typeCountObj = obj.get("typeCount");
        if (!isNullOrUndefined(typeCountObj)) {
            Map<Integer, Integer> typeCount = new HashMap<>();
            JsArray<JSObject> keys = JsObject.getKeys(typeCountObj);
            for (int i = 0; i < keys.getLength(); i++) {
                String key = JsObject.jsToString(keys.get(i));
                JsObject mapObj = JsObject.cast(typeCountObj);
                typeCount.put(Integer.parseInt(key), mapObj.getInt(key));
            }
            dto.setTypeCount(typeCount);
        }
        return dto;
    }

    // ============ TerrainTile demarshalling ============

    private static TerrainTile demarshallTerrainTile(Object data) {
        JSObject array = (JSObject) data;
        TerrainTile terrainTile = new TerrainTile();

        // [0] = Index array [x, y]
        JSObject indexArray = getArrayElementDirect(array, 0);
        int indexX = jsAsInt(getArrayElementDirect(indexArray, 0));
        int indexY = jsAsInt(getArrayElementDirect(indexArray, 1));
        terrainTile.setIndex(new Index(indexX, indexY));

        // [1] = groundConfigId
        terrainTile.setGroundConfigId(jsAsInt(getArrayElementDirect(array, 1)));

        // [2] = babylonDecals array
        terrainTile.setBabylonDecals(demarshallBabylonDecals(getArrayElementDirect(array, 2)));

        // [3] = groundHeightMap (Uint16Array) - wrap it
        JSObject heightMapObj = getArrayElementDirect(array, 3);
        if (!isNullOrUndefined(heightMapObj)) {
            terrainTile.setGroundHeightMap(JsUint16ArrayWrapper.wrap(heightMapObj));
        }

        // [4] = terrainTileObjectLists array
        terrainTile.setTerrainTileObjectLists(demarshallTerrainTileObjectLists(getArrayElementDirect(array, 4)));

        // [5] = botGrounds array
        terrainTile.setBotGrounds(demarshallBotGrounds(getArrayElementDirect(array, 5)));

        return terrainTile;
    }

    private static BabylonDecal[] demarshallBabylonDecals(Object data) {
        if (isNullOrUndefined((JSObject) data)) {
            return null;
        }
        JSObject array = (JSObject) data;
        int length = getArrayLength(array);
        if (length == 0) {
            return null;
        }
        BabylonDecal[] result = new BabylonDecal[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = JsObject.cast(getArrayElementDirect(array, i));
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

    private static TerrainTileObjectList[] demarshallTerrainTileObjectLists(Object data) {
        if (isNullOrUndefined((JSObject) data)) {
            return null;
        }
        JSObject array = (JSObject) data;
        int length = getArrayLength(array);
        if (length == 0) {
            return null;
        }
        TerrainTileObjectList[] result = new TerrainTileObjectList[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = JsObject.cast(getArrayElementDirect(array, i));
            TerrainTileObjectList list = new TerrainTileObjectList();
            list.setTerrainObjectConfigId(obj.getInt("terrainObjectConfigId"));
            list.setTerrainObjectModels(demarshallTerrainObjectModels(obj.get("terrainObjectModels")));
            result[i] = list;
        }
        return result;
    }

    private static TerrainObjectModel[] demarshallTerrainObjectModels(JSObject data) {
        if (isNullOrUndefined(data)) {
            return null;
        }
        int length = getArrayLength(data);
        if (length == 0) {
            return null;
        }
        TerrainObjectModel[] result = new TerrainObjectModel[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = JsObject.cast(getArrayElementDirect(data, i));
            TerrainObjectModel model = new TerrainObjectModel();
            model.terrainObjectId = obj.getInt("terrainObjectId");
            model.position = demarshallVertexFromArray(obj.get("position"));
            model.scale = demarshallVertexFromArray(obj.get("scale"));
            model.rotation = demarshallVertexFromArray(obj.get("rotation"));
            result[i] = model;
        }
        return result;
    }

    private static Vertex demarshallVertexFromArray(JSObject data) {
        if (isNullOrUndefined(data)) {
            return null;
        }
        double x = jsAsDouble(getArrayElementDirect(data, 0));
        double y = jsAsDouble(getArrayElementDirect(data, 1));
        double z = jsAsDouble(getArrayElementDirect(data, 2));
        return new Vertex(x, y, z);
    }

    private static BotGround[] demarshallBotGrounds(Object data) {
        if (isNullOrUndefined((JSObject) data)) {
            return null;
        }
        JSObject array = (JSObject) data;
        int length = getArrayLength(array);
        if (length == 0) {
            return null;
        }
        BotGround[] result = new BotGround[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = JsObject.cast(getArrayElementDirect(array, i));
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
        if (isNullOrUndefined(data)) {
            return null;
        }
        int length = getArrayLength(data);
        if (length == 0) {
            return null;
        }
        DecimalPosition[] result = new DecimalPosition[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = JsObject.cast(getArrayElementDirect(data, i));
            result[i] = new DecimalPosition(obj.getDouble("x"), obj.getDouble("y"));
        }
        return result;
    }

    private static BotGroundSlopeBox[] demarshallBotGroundSlopeBoxes(JSObject data) {
        if (isNullOrUndefined(data)) {
            return null;
        }
        int length = getArrayLength(data);
        if (length == 0) {
            return null;
        }
        BotGroundSlopeBox[] result = new BotGroundSlopeBox[length];
        for (int i = 0; i < length; i++) {
            JsObject obj = JsObject.cast(getArrayElementDirect(data, i));
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

    // ============ Helper methods ============

    @JSBody(params = {"array", "index", "value"}, script = "array[index] = value;")
    private static native void setArrayString(JsArray<Object> array, int index, String value);

    @JSBody(params = {"array", "index"}, script = "return array[index];")
    private static native String getArrayString(JsArray<Object> array, int index);

    @JSBody(params = {"obj", "index"}, script = "return obj[index];")
    private static native String getArrayStringDirect(JSObject obj, int index);

    @JSBody(params = {"obj", "index"}, script = "return obj[index];")
    private static native JSObject getArrayElementDirect(JSObject obj, int index);

    @JSBody(params = {"obj"}, script = "return obj.length || 0;")
    private static native int getArrayLength(JSObject obj);

    @JSBody(params = {"obj"}, script = "return JSON.stringify(obj);")
    private static native String stringify(JSObject obj);

    @JSBody(params = {"obj"}, script = "return obj === null || obj === undefined;")
    private static native boolean isNullOrUndefined(JSObject obj);

    // Cast to JsArray - the @JSBody annotation prevents WASM-GC type checking
    @JSBody(params = {"obj"}, script = "return obj;")
    private static native JsArray<Object> castToArray(JSObject obj);

    @JSBody(params = {"array", "value"}, script = "array.push(value);")
    private static native void pushInt(JsArray<Object> array, int value);

    @JSBody(params = {"obj"}, script = "return typeof obj === 'number' ? obj : parseInt(obj, 10);")
    private static native int jsAsInt(JSObject obj);

    @JSBody(params = {"obj"}, script = "return typeof obj === 'number' ? obj : parseFloat(obj);")
    private static native double jsAsDouble(JSObject obj);

    @JSBody(params = {"obj"}, script = "return Array.isArray(obj);")
    private static native boolean isArray(JSObject obj);

    @JSBody(params = {"obj"}, script = "return typeof obj + (Array.isArray(obj) ? ' (array)' : '');")
    private static native String getObjectType(JSObject obj);

    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

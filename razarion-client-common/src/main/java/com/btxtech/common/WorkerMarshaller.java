package com.btxtech.common;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.tracking.PlayerBaseTracking;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import elemental2.core.Array;
import elemental2.core.Float32Array;
import elemental2.core.JsObject;
import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import jsinterop.base.JsPropertyMapOfAny;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jsinterop.base.Js.uncheckedCast;

/**
 * Created by Beat
 * 03.01.2017.
 */
public class WorkerMarshaller {
    // private static Logger LOGGER = Logger.getLogger(WorkerMarshaller.class.getName());
    private static final int COMMAND_OFFSET = 0;
    private static final int DATA_OFFSET_0 = 1;
    private static final int DATA_OFFSET_1 = 2;
    private static final int DATA_OFFSET_2 = 3;
    private static final int DATA_OFFSET_3 = 4;
    private static final int DATA_OFFSET_4 = 5;
    private static final int DATA_OFFSET_5 = 6;
    private static final int DATA_OFFSET_6 = 7;

    public static Array<Object> marshall(GameEngineControlPackage controlPackage) {
        Array<Object> array = new Array<>();
        array.setAt(COMMAND_OFFSET, controlPackage.getCommand().name());
        switch (controlPackage.getCommand()) {
            // No data
            case LOADED:
            case START:
            case STOP_REQUEST:
            case STOP_RESPONSE:
            case QUEST_PASSED:
            case PERFMON_REQUEST:
            case TICK_UPDATE_REQUEST:
            case INITIALIZED:
            case TICK_UPDATE_RESPONSE_FAIL:
            case CONNECTION_LOST:
            case INITIAL_SLAVE_SYNCHRONIZED_NO_BASE: // Marshaller can not handle null value
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
            case SINGLE_Z_TERRAIN:
            case SINGLE_Z_TERRAIN_ANSWER_FAIL:
            case TERRAIN_TILE_REQUEST:
            case PLAYBACK_PLAYER_BASE:
            case PLAYBACK_SYNC_ITEM_DELETED:
            case PLAYBACK_SYNC_BASE_ITEM:
            case PLAYBACK_SYNC_RESOURCE_ITEM:
            case PLAYBACK_SYNC_BOX_ITEM:
            case QUEST_PROGRESS:
            case SELL_ITEMS:
            case USE_INVENTORY_ITEM:
            case INITIAL_SLAVE_SYNCHRONIZED:
                array.setAt(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
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
            case SINGLE_Z_TERRAIN_ANSWER:
            case ENERGY_CHANGED:
                array.setAt(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.setAt(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                break;
            // Triple JSON data
            case COMMAND_BUILD:
            case PROJECTILE_FIRED:
                array.setAt(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.setAt(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.setAt(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                break;
            case INITIALIZE_WARM:
                array.setAt(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.setAt(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.setAt(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                array.setAt(DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                break;
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                array.setAt(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.setAt(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.setAt(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                array.setAt(DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                array.setAt(DATA_OFFSET_4, toJson(controlPackage.getData(4)));
                break;
            // Multiple  JSON data
            case INITIALIZE:
                array.setAt(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.setAt(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.setAt(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                array.setAt(DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                array.setAt(DATA_OFFSET_4, toJson(controlPackage.getData(4)));
                array.setAt(DATA_OFFSET_5, toJson(controlPackage.getData(5)));
                break;
            // Native marshal terrain buffers
            case TERRAIN_TILE_RESPONSE:
                array.setAt(DATA_OFFSET_0, marshallTerrainTile((TerrainTile) controlPackage.getData(0)));
                break;
            // Single Structure clone
            case TICK_UPDATE_RESPONSE:
            case SYNC_ITEM_START_SPAWNED:
            case SYNC_ITEM_IDLE:
                array.setAt(DATA_OFFSET_0, (JsObject) controlPackage.getData(0));
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
        return array;
    }

    public static GameEngineControlPackage deMarshall(Object javaScriptObject) {
        Any[] array = Js.castToArray(javaScriptObject);
        GameEngineControlPackage.Command command = GameEngineControlPackage.Command.valueOf(array[COMMAND_OFFSET].asString());

        List<Object> data = new ArrayList<>();
        switch (command) {
            // No data
            case LOADED:
            case START:
            case STOP_REQUEST:
            case STOP_RESPONSE:
            case QUEST_PASSED:
            case PERFMON_REQUEST:
            case TICK_UPDATE_REQUEST:
            case INITIALIZED:
            case TICK_UPDATE_RESPONSE_FAIL:
            case CONNECTION_LOST:
            case INITIAL_SLAVE_SYNCHRONIZED_NO_BASE: // Marshaller can not handle null value
                break;
            case INITIALIZE:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), StaticGameConfig.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), PlanetConfig.class));
                data.add(fromJson(array[DATA_OFFSET_2].asString(), UserContext.class));
                data.add(fromJson(array[DATA_OFFSET_3].asString(), GameEngineMode.class));
                data.add(fromJson(array[DATA_OFFSET_4].asString(), Boolean.class));
                data.add(fromJson(array[DATA_OFFSET_5].asString(), String.class));
                break;
            case INITIALIZE_WARM:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), PlanetConfig.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), UserContext.class));
                data.add(fromJson(array[DATA_OFFSET_2].asString(), GameEngineMode.class));
                data.add(fromJson(array[DATA_OFFSET_3].asString(), String.class));
                break;
            case INITIALISING_FAILED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), String.class));
                break;
            case START_BOTS:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                break;
            case EXECUTE_BOT_COMMANDS:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                break;
            case CREATE_RESOURCES:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                break;
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Map.class));
                data.add(fromJson(array[DATA_OFFSET_2].asString(), HumanPlayerId.class));
                data.add(fromJson(array[DATA_OFFSET_3].asString(), String.class));
                data.add(fromJson(array[DATA_OFFSET_4].asString(), DecimalPosition.class));
                break;
            case TICK_UPDATE_RESPONSE:
                data.add(array[DATA_OFFSET_0]);
//                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
//                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
//                data.add(fromJson(array[DATA_OFFSET_2].asString(), List.class));
//                data.add(fromJson(array[DATA_OFFSET_3].asString(), List.class));
                break;
            case COMMAND_ATTACK:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
                break;
            case COMMAND_FINALIZE_BUILD:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
                break;
            case COMMAND_BUILD:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), DecimalPosition.class));
                data.add(fromJson(array[DATA_OFFSET_2].asString(), Integer.class));
                break;
            case COMMAND_FABRICATE:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
                break;
            case COMMAND_HARVEST:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
                break;
            case COMMAND_MOVE:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), DecimalPosition.class));
                break;
            case COMMAND_PICK_BOX:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
                break;
            case COMMAND_LOAD_CONTAINER:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
                break;
            case COMMAND_UNLOAD_CONTAINER:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), DecimalPosition.class));
                break;
            case RESOURCE_CREATED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), SyncResourceItemSimpleDto.class));
                break;
            case RESOURCE_DELETED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                break;
            case ENERGY_CHANGED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
                break;
            case BASE_CREATED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), PlayerBaseDto.class));
                break;
            case BASE_DELETED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                break;
            case BASE_UPDATED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), PlayerBaseDto.class));
                break;
            case USE_INVENTORY_ITEM:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), UseInventoryItem.class));
                break;
            case SYNC_ITEM_START_SPAWNED:
                data.add(array[DATA_OFFSET_0]);
                break;
            case SYNC_ITEM_IDLE:
                data.add(array[DATA_OFFSET_0]);
                break;
            case CREATE_BOXES:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                break;
            case BOX_CREATED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), SyncBoxItemSimpleDto.class));
                break;
            case BOX_DELETED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                break;
            case BOX_PICKED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), BoxContent.class));
                break;
            case ACTIVATE_QUEST:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                break;
            case UPDATE_LEVEL:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                break;
            case PROJECTILE_FIRED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Vertex.class));
                data.add(fromJson(array[DATA_OFFSET_2].asString(), Vertex.class));
                break;
            case PROJECTILE_DETONATION:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Vertex.class));
                break;
            case PERFMON_RESPONSE:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                break;
            case SINGLE_Z_TERRAIN:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), DecimalPosition.class));
                break;
            case SINGLE_Z_TERRAIN_ANSWER:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), DecimalPosition.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Double.class));
                break;
            case SINGLE_Z_TERRAIN_ANSWER_FAIL:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), DecimalPosition.class));
                break;
            case TERRAIN_TILE_REQUEST:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Index.class));
                break;
            case TERRAIN_TILE_RESPONSE:
                data.add(demarshallTerrainTile(array[DATA_OFFSET_0]));
                break;
            case PLAYBACK_PLAYER_BASE:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), PlayerBaseTracking.class));
                break;
            case PLAYBACK_SYNC_ITEM_DELETED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), SyncItemDeletedInfo.class));
                break;
            case PLAYBACK_SYNC_BASE_ITEM:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), SyncBaseItemInfo.class));
                break;
            case PLAYBACK_SYNC_RESOURCE_ITEM:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), SyncResourceItemInfo.class));
                break;
            case PLAYBACK_SYNC_BOX_ITEM:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), SyncBoxItemInfo.class));
                break;
            case QUEST_PROGRESS:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), QuestProgressInfo.class));
                break;
            case SELL_ITEMS:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                break;
            case INITIAL_SLAVE_SYNCHRONIZED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), DecimalPosition.class));
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + command);
        }

        return new GameEngineControlPackage(command, data.toArray());
    }

    private static String toJson(Object object) {
        RestClient.setJacksonMarshallingActive(false); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
        try {
            if (object == null) {
                return "null";
            } else {
                return MarshallingWrapper.toJSON(object);
            }
        } finally {
            RestClient.setJacksonMarshallingActive(true); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
        }
    }

    private static <T> T fromJson(String json, Class<T> type) {
        RestClient.setJacksonMarshallingActive(false); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
        try {
            if ("null".equals(json)) {
                return null;
            } else {
                return MarshallingWrapper.fromJSON(json, type);
            }
        } finally {
            RestClient.setJacksonMarshallingActive(true); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
        }
    }

    private static Object marshallTerrainTile(TerrainTile terrainTile) {
        Array<Object> array = new Array<>();
        JsArrayInteger indexArray = JavaScriptObject.createArray().cast();
        indexArray.push(terrainTile.getIndex().getX());
        indexArray.push(terrainTile.getIndex().getY());
        array.push(indexArray);
        array.push(marshallFloat32ArrayMap(terrainTile.getGroundPositions()));
        array.push(marshallFloat32ArrayMap(terrainTile.getGroundNorms()));
        array.push(marshallTerrainSlopeTiles(terrainTile.getTerrainSlopeTiles()));
        array.push(marshallTerrainWaterTiles(terrainTile.getTerrainWaterTiles()));
        return array;
    }

    private static elemental2.core.Map<Any, Float32Array> marshallFloat32ArrayMap(Map<Integer, Float32ArrayEmu> float32ArrayMap) {
        elemental2.core.Map<Any, Float32Array> groundSlopePositions = new elemental2.core.Map<>();
        if (float32ArrayMap != null) {
            for (Map.Entry<Integer, Float32ArrayEmu> entry : float32ArrayMap.entrySet()) {
                if (entry.getKey() != null) {
                    groundSlopePositions.set(Any.of((int) entry.getKey()), Js.uncheckedCast(entry.getValue()));
                } else {
                    groundSlopePositions.set(null, Js.uncheckedCast(entry.getValue()));
                }
            }
        }
        return groundSlopePositions;
    }

    private static Array<JsPropertyMapOfAny> marshallTerrainSlopeTiles(List<TerrainSlopeTile> terrainSlopeTiles) {
        Array<JsPropertyMapOfAny> result = new Array<>();
        if (terrainSlopeTiles != null) {
            for (TerrainSlopeTile terrainSlopeTile : terrainSlopeTiles) {
                JsPropertyMapOfAny mapOfAny = JsPropertyMap.of();
                mapOfAny.set("slopeConfigId", terrainSlopeTile.getSlopeConfigId());
                mapOfAny.set("outerSlopeGeometry", marshallSlopeGeometry(terrainSlopeTile.getOuterSlopeGeometry()));
                mapOfAny.set("centerSlopeGeometry", marshallSlopeGeometry(terrainSlopeTile.getCenterSlopeGeometry()));
                mapOfAny.set("innerSlopeGeometry", marshallSlopeGeometry(terrainSlopeTile.getInnerSlopeGeometry()));
                result.push(mapOfAny);
            }
        }
        return result;
    }

    private static Float32Array[] marshallSlopeGeometry(SlopeGeometry slopeGeometry) {
        if (slopeGeometry != null) {
            return new Float32Array[]{
                    Js.uncheckedCast(slopeGeometry.getPositions()),
                    Js.uncheckedCast(slopeGeometry.getNorms()),
                    Js.uncheckedCast(slopeGeometry.getSlopeFactors()),
                    Js.uncheckedCast(slopeGeometry.getUvs())
            };
        } else {
            return new Float32Array[0];
        }
    }

    private static Object marshallTerrainWaterTiles(Collection<TerrainWaterTile> terrainWaterTiles) {
        Array<JsPropertyMapOfAny> result = new Array<>();
        if (terrainWaterTiles != null) {
            for (TerrainWaterTile terrainWaterTile : terrainWaterTiles) {
                JsPropertyMapOfAny mapOfAny = JsPropertyMap.of();
                mapOfAny.set("slopeConfigId", terrainWaterTile.getSlopeConfigId());
                if (terrainWaterTile.isPositionsSet()) {
                    mapOfAny.set("positions", Js.uncheckedCast(terrainWaterTile.getPositions()));
                }
                if (terrainWaterTile.isShallowPositionsSet()) {
                    mapOfAny.set("shallowPositions", Js.uncheckedCast(terrainWaterTile.getShallowPositions()));
                    mapOfAny.set("shallowUvs", Js.uncheckedCast(terrainWaterTile.getShallowUvs()));
                }
                result.push(mapOfAny);
            }
        }
        return result;
    }

    private static TerrainTile demarshallTerrainTile(Object data) {
        Any[] array = Js.castToArray(data);
        TerrainTile terrainTile = new TerrainTile();
        terrainTile.setIndex(new Index(array[0].asArray()[0].asInt(), array[0].asArray()[1].asInt()));
        terrainTile.setGroundPositions(demarshallFloat32ArrayMap(array[1]));
        terrainTile.setGroundNorms(demarshallFloat32ArrayMap(array[2]));
        terrainTile.setTerrainSlopeTiles(demarshallTerrainSlopeTiles(array[3]));
        terrainTile.setTerrainWaterTiles(demarshallTerrainWaterTiles(array[4]));
        return terrainTile;
    }

    private static Map<Integer, Float32ArrayEmu> demarshallFloat32ArrayMap(Any any) {
        elemental2.core.Map<Any, Float32ArrayEmu> jsFloat32ArrayMap = uncheckedCast(any);
        if (jsFloat32ArrayMap.size == 0) {
            return null;
        }
        Map<Integer, Float32ArrayEmu> float32ArrayMap = new HashMap<>();
        jsFloat32ArrayMap.forEach((float32Array, groundId, ignore) -> {
            if (groundId == null) {
                float32ArrayMap.put(null, float32Array);
            } else {
                float32ArrayMap.put(groundId.asInt(), float32Array);
            }
            return null;
        });
        return float32ArrayMap;
    }

    private static List<TerrainSlopeTile> demarshallTerrainSlopeTiles(Any any) {
        JsPropertyMapOfAny[] array = Js.cast(any);
        if (array.length == 0) {
            return null;
        }
        return Arrays.stream(array).map(anyTerrainSlopeTile -> {
            TerrainSlopeTile terrainSlopeTile = new TerrainSlopeTile();
            terrainSlopeTile.setSlopeConfigId(((Any) anyTerrainSlopeTile.get("slopeConfigId")).asInt());
            terrainSlopeTile.setOuterSlopeGeometry(demarshallSlopeGeometry(((Any) anyTerrainSlopeTile.get("outerSlopeGeometry")).asArray()));
            terrainSlopeTile.setCenterSlopeGeometry(demarshallSlopeGeometry(((Any) anyTerrainSlopeTile.get("centerSlopeGeometry")).asArray()));
            terrainSlopeTile.setInnerSlopeGeometry(demarshallSlopeGeometry(((Any) anyTerrainSlopeTile.get("innerSlopeGeometry")).asArray()));
            return terrainSlopeTile;
        }).collect(Collectors.toList());
    }

    private static SlopeGeometry demarshallSlopeGeometry(Any[] slopeGeometryAnyArray) {
        if (slopeGeometryAnyArray == null || slopeGeometryAnyArray.length == 0) {
            return null;
        }
        SlopeGeometry slopeGeometry = new SlopeGeometry();
        slopeGeometry.setPositions(slopeGeometryAnyArray[0].uncheckedCast());
        slopeGeometry.setNorms(slopeGeometryAnyArray[1].uncheckedCast());
        slopeGeometry.setSlopeFactors(slopeGeometryAnyArray[2].uncheckedCast());
        slopeGeometry.setUvs(slopeGeometryAnyArray[3].uncheckedCast());
        return slopeGeometry;
    }

    private static List<TerrainWaterTile> demarshallTerrainWaterTiles(Any any) {
        JsPropertyMapOfAny[] array = Js.cast(any);
        if (array.length == 0) {
            return null;
        }

        return Arrays.stream(array).map(anyTerrainWaterTile -> {
            TerrainWaterTile terrainWaterTile = new TerrainWaterTile();
            terrainWaterTile.setSlopeConfigId(((Any) Js.uncheckedCast(anyTerrainWaterTile.get("slopeConfigId"))).asInt());
            if (anyTerrainWaterTile.has("positions")) {
                terrainWaterTile.setPositions(Js.uncheckedCast(anyTerrainWaterTile.get("positions")));
            }
            if (anyTerrainWaterTile.has("shallowPositions")) {
                terrainWaterTile.setShallowPositions(Js.uncheckedCast((anyTerrainWaterTile.get("shallowPositions"))));
                terrainWaterTile.setShallowUvs(Js.uncheckedCast((anyTerrainWaterTile.get("shallowUvs"))));
            }
            return terrainWaterTile;
        }).collect(Collectors.toList());
    }
}

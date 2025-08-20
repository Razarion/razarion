package com.btxtech.common;

import com.btxtech.shared.RazarionSharedDominokitJsonRegistry;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.IdsDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.IntIntMap;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.BabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.TerrainObjectModel;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;
import elemental2.core.JsArray;
import elemental2.core.JsObject;
import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsArrayLike;
import jsinterop.base.JsPropertyMap;
import org.dominokit.jackson.DefaultJsonDeserializationContext;
import org.dominokit.jackson.DefaultJsonSerializationContext;
import org.dominokit.jackson.JsonDeserializationContext;
import org.dominokit.jackson.JsonSerializationContext;
import org.dominokit.jackson.ObjectReader;
import org.dominokit.jackson.ObjectWriter;
import org.dominokit.jackson.deser.BaseNumberJsonDeserializer;
import org.dominokit.jackson.deser.BooleanJsonDeserializer;
import org.dominokit.jackson.deser.StringJsonDeserializer;
import org.dominokit.jackson.registration.TypeToken;
import org.dominokit.jackson.ser.BaseNumberJsonSerializer;
import org.dominokit.jackson.ser.BooleanJsonSerializer;
import org.dominokit.jackson.ser.EnumJsonSerializer;
import org.dominokit.jackson.ser.StringJsonSerializer;
import org.dominokit.jackson.stream.JsonReader;
import org.dominokit.jackson.stream.JsonWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jsinterop.base.Js.asInt;

/**
 * Created by Beat
 * 03.01.2017.
 */
public class WorkerMarshaller {
    private static final Logger logger = Logger.getLogger(WorkerMarshaller.class.getName());
    private static final int COMMAND_OFFSET = 0;
    private static final int DATA_OFFSET_0 = 1;
    private static final int DATA_OFFSET_1 = 2;
    private static final int DATA_OFFSET_2 = 3;
    private static final int DATA_OFFSET_3 = 4;
    private static final int DATA_OFFSET_4 = 5;
    private static final int DATA_OFFSET_5 = 6;

    public static JsArrayLike<Object> marshall(GameEngineControlPackage controlPackage) {
        JsArray<Object> array = new JsArray<>();
        array.setAt(COMMAND_OFFSET, controlPackage.getCommand().name());
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
            case INITIAL_SLAVE_SYNCHRONIZED_NO_BASE: // Marshaller can not handle null value
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
            case ENERGY_CHANGED:
            case PROJECTILE_FIRED:
            case GET_TERRAIN_TYPE_ANSWER:
                array.setAt(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.setAt(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                break;
            // Triple JSON data
            case COMMAND_BUILD:
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
                break;
            // Native marshal terrain buffers
            case TERRAIN_TILE_RESPONSE:
                array.setAt(DATA_OFFSET_0, marshallTerrainTile((TerrainTile) controlPackage.getData(0)));
                break;
            // Single Structure clone
            case TICK_UPDATE_RESPONSE:
            case SYNC_ITEM_START_SPAWNED:
            case SYNC_ITEM_IDLE:
                array.setAt(DATA_OFFSET_0, controlPackage.getData(0));
                break;
            case START:
                array.setAt(DATA_OFFSET_0, controlPackage.getData(0));
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
        return array;
    }

    public static GameEngineControlPackage deMarshall(Object javaScriptObject) {
        Any[] array = Js.asArray(javaScriptObject);
        GameEngineControlPackage.Command command = GameEngineControlPackage.Command.valueOf(array[COMMAND_OFFSET].asString());

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
            case INITIAL_SLAVE_SYNCHRONIZED_NO_BASE: // Marshaller can not handle null value
            case COMMAND_MOVE_ACK:
                break;
            case INITIALIZE:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), StaticGameConfig.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), PlanetConfig.class));
                data.add(fromJson(array[DATA_OFFSET_2].asString(), UserContext.class));
                data.add(fromJson(array[DATA_OFFSET_3].asString(), GameEngineMode.class));
                data.add(fromJson(array[DATA_OFFSET_4].asString(), String.class));
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
                data.add(fromJson(array[DATA_OFFSET_1].asString(), IntIntMap.class));
                data.add(fromJson(array[DATA_OFFSET_2].asString(), String.class));
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
                data.add(fromJson(array[DATA_OFFSET_0].asString(), IdsDto.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
                break;
            case COMMAND_FINALIZE_BUILD:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), IdsDto.class));
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
                data.add(fromJson(array[DATA_OFFSET_0].asString(), IdsDto.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
                break;
            case COMMAND_MOVE:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), IdsDto.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), DecimalPosition.class));
                break;
            case COMMAND_PICK_BOX:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), IdsDto.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), Integer.class));
                break;
            case COMMAND_LOAD_CONTAINER:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), IdsDto.class));
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
                data.add(fromJson(array[DATA_OFFSET_1].asString(), DecimalPosition.class));
                break;
            case PROJECTILE_DETONATION:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Integer.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), DecimalPosition.class));
                break;
            case PERFMON_RESPONSE:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), List.class));
                break;
            case GET_TERRAIN_TYPE:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Index.class));
                break;
            case GET_TERRAIN_TYPE_ANSWER:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Index.class));
                data.add(fromJson(array[DATA_OFFSET_1].asString(), TerrainType.class));
                break;
            case TERRAIN_TILE_REQUEST:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), Index.class));
                break;
            case TERRAIN_TILE_RESPONSE:
                data.add(demarshallTerrainTile(array[DATA_OFFSET_0]));
                break;
            case QUEST_PROGRESS:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), QuestProgressInfo.class));
                break;
            case SELL_ITEMS:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), IdsDto.class));
                break;
            case INITIAL_SLAVE_SYNCHRONIZED:
                data.add(fromJson(array[DATA_OFFSET_0].asString(), DecimalPosition.class));
                break;
            case START:
                data.add(array[DATA_OFFSET_0].asString());
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + command);
        }

        return new GameEngineControlPackage(command, data.toArray());
    }

    public static String toJson(Object object) {
        if (object == null) {
            return "null";
        } else {
            try {
                ObjectWriter<?> objectWriter = RazarionSharedDominokitJsonRegistry
                        .getInstance()
                        .getWriter(TypeToken.of(object.getClass()));
                if (objectWriter != null) {
                    return objectWriter.write(Js.uncheckedCast(object));
                }
                JsonSerializationContext context = DefaultJsonSerializationContext.builder().build();
                JsonWriter writer = context.newJsonWriter();

                if (object instanceof String) {
                    StringJsonSerializer.getInstance().serialize(writer, (String) object, context);
                } else if (object instanceof Integer) {
                    BaseNumberJsonSerializer.IntegerJsonSerializer.getInstance().serialize(writer, (Integer) object, context);
                } else if (object instanceof Double) {
                    BaseNumberJsonSerializer.DoubleJsonSerializer.getInstance().serialize(writer, (Double) object, context);
                } else if (object instanceof Boolean) {
                    BooleanJsonSerializer.getInstance().serialize(writer, (Boolean) object, context);
                } else if (object instanceof Enum) {
                    EnumJsonSerializer.getInstance().serialize(writer, object, context);
                } else {
                    throw new IllegalArgumentException("Unsupported type: " + object.getClass().getName());
                }
                return writer.getOutput();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "WorkerMarshaller.toJson() can not handle: " + object.getClass(), t);
                return "null";
            }
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        if ("null".equals(json)) {
            return null;
        } else {
            try {
                ObjectReader<?> objectReader = RazarionSharedDominokitJsonRegistry
                        .getInstance()
                        .getReader(TypeToken.of(type));
                if (objectReader != null) {
                    return Js.uncheckedCast(objectReader.read(json));
                }
                JsonDeserializationContext context = DefaultJsonDeserializationContext.builder().build();
                JsonReader reader = context.newJsonReader(json);

                if (type == String.class) {
                    return Js.uncheckedCast(StringJsonDeserializer.getInstance().deserialize(reader, context));
                } else if (type == Integer.class) {
                    return Js.uncheckedCast(BaseNumberJsonDeserializer.IntegerJsonDeserializer.getInstance().deserialize(reader, context));
                } else if (type == Double.class) {
                    return Js.uncheckedCast(BaseNumberJsonDeserializer.DoubleJsonDeserializer.getInstance().deserialize(reader, context));
                } else if (type == Boolean.class) {
                    return Js.uncheckedCast(BooleanJsonDeserializer.getInstance().deserialize(reader, context));
                } else if (type == GameEngineMode.class) {
                    String enumString = json.replace("\"", "");
                    return Js.uncheckedCast(GameEngineMode.valueOf(enumString));
                } else if (type == TerrainType.class) {
                    String enumString = json.replace("\"", "");
                    return Js.uncheckedCast(TerrainType.valueOf(enumString));
                } else {
                    throw new IllegalArgumentException("Unsupported type: " + type);
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "WorkerMarshaller.fromJson() can not handle: " + type + " json: " + json, t);
                return null;
            }
        }
    }

    private static Object marshallTerrainTile(TerrainTile terrainTile) {
        JsArray<Object> array = new JsArray<>();
        JsArrayInteger indexArray = JavaScriptObject.createArray().cast();
        indexArray.push(terrainTile.getIndex().getX());
        indexArray.push(terrainTile.getIndex().getY());
        array.push(indexArray);
        array.push(terrainTile.getGroundConfigId());
        array.push(marshallBabylonDecals(terrainTile.getBabylonDecals()));
        array.push(terrainTile.getGroundHeightMap());
        array.push(marshallTerrainTileObjectList(terrainTile.getTerrainTileObjectLists()));
        return array;
    }

    private static Object marshallBabylonDecals(BabylonDecal[] babylonDecals) {
        JsArray<JsPropertyMap<Object>> result = new JsArray<>();
        if (babylonDecals != null) {
            for (BabylonDecal babylonDecal : babylonDecals) {
                JsPropertyMap<Object> mapOfBabylonDecal = JsPropertyMap.of();
                mapOfBabylonDecal.set("babylonMaterialId", babylonDecal.babylonMaterialId);
                mapOfBabylonDecal.set("xPos", babylonDecal.xPos);
                mapOfBabylonDecal.set("yPos", babylonDecal.yPos);
                mapOfBabylonDecal.set("xSize", babylonDecal.xSize);
                mapOfBabylonDecal.set("ySize", babylonDecal.ySize);
                result.push(mapOfBabylonDecal);
            }
        }
        return result;
    }

    private static Object marshallTerrainTileObjectList(TerrainTileObjectList[] terrainTileObjectLists) {
        JsArray<JsPropertyMap<Object>> result = new JsArray<>();
        if (terrainTileObjectLists != null) {
            for (TerrainTileObjectList terrainTileObjectList : terrainTileObjectLists) {
                JsPropertyMap<Object> mapOfTerrainTileObjectList = JsPropertyMap.of();
                mapOfTerrainTileObjectList.set("terrainObjectConfigId", terrainTileObjectList.getTerrainObjectConfigId());
                mapOfTerrainTileObjectList.set("terrainObjectModels", marshallTerrainObjectModel(terrainTileObjectList.getTerrainObjectModels()));
                result.push(mapOfTerrainTileObjectList);
            }
        }
        return result;
    }

    private static Object marshallTerrainObjectModel(TerrainObjectModel[] terrainObjectModels) {
        JsArray<JsPropertyMap<Object>> result = new JsArray<>();
        if (terrainObjectModels != null) {
            for (TerrainObjectModel terrainObjectModel : terrainObjectModels) {
                JsPropertyMap<Object> mapOfTerrainObjectModel = JsPropertyMap.of();
                mapOfTerrainObjectModel.set("terrainObjectId", terrainObjectModel.terrainObjectId);
                mapOfTerrainObjectModel.set("position", vertexToArray(terrainObjectModel.position));
                mapOfTerrainObjectModel.set("scale", vertexToArray(terrainObjectModel.scale));
                mapOfTerrainObjectModel.set("rotation", vertexToArray(terrainObjectModel.rotation));
                result.push(mapOfTerrainObjectModel);
            }
        }
        return result;
    }

    private static TerrainTile demarshallTerrainTile(Object data) {
        Any[] array = Js.asArray(data);
        TerrainTile terrainTile = new TerrainTile();
        terrainTile.setIndex(new Index(array[0].asArray()[0].asInt(), array[0].asArray()[1].asInt()));
        terrainTile.setGroundConfigId(getIssueNumber(array[1]));
        terrainTile.setBabylonDecals(demarshallBabylonDecals(array[2]));
        terrainTile.setGroundHeightMap(Js.uncheckedCast(array[3].asArrayLike()));
        terrainTile.setTerrainTileObjectLists(demarshallTerrainTileObjectLists(array[4]));
        return terrainTile;
    }

    private static BabylonDecal[] demarshallBabylonDecals(Any any) {
        JsPropertyMap<Object>[] array = Js.cast(any);
        if (array.length == 0) {
            return null;
        }
        return Arrays.stream(array).map(anyBabylonDecal -> {
            BabylonDecal babylonDecal = new BabylonDecal();
            babylonDecal.babylonMaterialId = ((Any) Js.uncheckedCast(anyBabylonDecal.get("babylonMaterialId"))).asInt();
            babylonDecal.xPos = ((Any) Js.uncheckedCast(anyBabylonDecal.get("xPos"))).asDouble();
            babylonDecal.yPos = ((Any) Js.uncheckedCast(anyBabylonDecal.get("yPos"))).asDouble();
            babylonDecal.xSize = ((Any) Js.uncheckedCast(anyBabylonDecal.get("xSize"))).asDouble();
            babylonDecal.ySize = ((Any) Js.uncheckedCast(anyBabylonDecal.get("ySize"))).asDouble();
            return babylonDecal;
        }).toArray(BabylonDecal[]::new);
    }

    private static TerrainTileObjectList[] demarshallTerrainTileObjectLists(Any any) {
        JsPropertyMap<Object>[] array = Js.cast(any);
        if (array.length == 0) {
            return null;
        }
        return Arrays.stream(array).map(anyTerrainTileObjectList -> {
            TerrainTileObjectList terrainTileObjectList = new TerrainTileObjectList();
            terrainTileObjectList.setTerrainObjectConfigId(((Any) Js.uncheckedCast(anyTerrainTileObjectList.get("terrainObjectConfigId"))).asInt());
            terrainTileObjectList.setTerrainObjectModels(demarshallTerrainObjectModels((Any) anyTerrainTileObjectList.get("terrainObjectModels")));
            return terrainTileObjectList;
        }).toArray(TerrainTileObjectList[]::new);
    }

    private static TerrainObjectModel[] demarshallTerrainObjectModels(Any any) {
        JsPropertyMap<Object>[] array = Js.cast(any);
        if (array.length == 0) {
            return null;
        }
        return Arrays.stream(array).map(anyTerrainObjectModels -> {
            TerrainObjectModel terrainTileObjectList = new TerrainObjectModel();
            terrainTileObjectList.terrainObjectId = ((Any) Js.uncheckedCast(anyTerrainObjectModels.get("terrainObjectId"))).asInt();
            terrainTileObjectList.position = arrayToVertex(anyTerrainObjectModels.get("position"));
            terrainTileObjectList.scale = arrayToVertex(anyTerrainObjectModels.get("scale"));
            terrainTileObjectList.rotation = arrayToVertex(anyTerrainObjectModels.get("rotation"));
            return terrainTileObjectList;
        }).toArray(TerrainObjectModel[]::new);
    }


    private static JsArrayNumber vertexToArray(Vertex vertex) {
        if (vertex == null) {
            return null;
        }
        JsArrayNumber xyz = JavaScriptObject.createArray().cast();
        xyz.push(vertex.getX());
        xyz.push(vertex.getY());
        xyz.push(vertex.getZ());
        return xyz;
    }

    private static Vertex arrayToVertex(Object xyz) {
        if (xyz == null) {
            return null;
        }
        Any[] jsArray = ((Any) xyz).asArray();
        return new Vertex(jsArray[0].asDouble(),
                jsArray[1].asDouble(),
                jsArray[2].asDouble());
    }

    private static int getIssueNumber(Any any) {
        if (any instanceof Number) {
            return ((Number) any).intValue();
        }
        return asInt(Js.asPropertyMap(any).get(JsObject.keys(any).getAt(0)));
    }
}

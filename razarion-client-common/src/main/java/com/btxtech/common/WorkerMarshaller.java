package com.btxtech.common;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.tracking.PlayerBaseTracking;
import com.btxtech.shared.dto.SlaveSyncItemInfo;
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
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 03.01.2017.
 */
public class WorkerMarshaller {
    // private static Logger logger = Logger.getLogger(WorkerMarshaller.class.getName());
    private static final int COMMAND_OFFSET = 0;
    private static final int DATA_OFFSET_0 = 1;
    private static final int DATA_OFFSET_1 = 2;
    private static final int DATA_OFFSET_2 = 3;
    private static final int DATA_OFFSET_3 = 4;
    private static final int DATA_OFFSET_4 = 5;
    private static final int DATA_OFFSET_5 = 6;
    private static final int DATA_OFFSET_6 = 7;

    public static JavaScriptObject marshall(GameEngineControlPackage controlPackage) {
        JsArrayMixed array = JavaScriptObject.createArray().cast();
        array.set(COMMAND_OFFSET, controlPackage.getCommand().name());
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
                break;
            // Single JSON data
            case START_BOTS:
            case EXECUTE_BOT_COMMANDS:
            case CREATE_RESOURCES:
            case RESOURCE_CREATED:
            case RESOURCE_DELETED:
            case SYNC_ITEM_START_SPAWNED:
            case SYNC_ITEM_IDLE:
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
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
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
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                break;
            // Triple JSON data
            case COMMAND_BUILD:
            case PROJECTILE_FIRED:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.set(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                break;
            // Quadruple JSON data
            case INITIALIZE_WARM:
            case TICK_UPDATE_RESPONSE:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.set(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                array.set(DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                break;
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.set(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                array.set(DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                array.set(DATA_OFFSET_4, toJson(controlPackage.getData(4)));
                break;
            // Multiple  JSON data
            case INITIALIZE:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.set(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                array.set(DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                array.set(DATA_OFFSET_4, toJson(controlPackage.getData(4)));
                array.set(DATA_OFFSET_5, toJson(controlPackage.getData(5)));
                array.set(DATA_OFFSET_6, toJson(controlPackage.getData(6)));
                break;
            // Native marshal terrain buffers
            case TERRAIN_TILE_RESPONSE:
                array.set(DATA_OFFSET_0, marshallTerrainTile((TerrainTile) controlPackage.getData(0)));
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
        return array;
    }

    public static GameEngineControlPackage deMarshall(Object javaScriptObject) {
        JsArrayMixed array = ((JavaScriptObject) javaScriptObject).cast();
        GameEngineControlPackage.Command command = GameEngineControlPackage.Command.valueOf(array.getString(COMMAND_OFFSET));

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
                break;
            case INITIALIZE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), StaticGameConfig.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), PlanetConfig.class));
                data.add(fromJson(array.getString(DATA_OFFSET_2), SlaveSyncItemInfo.class));
                data.add(fromJson(array.getString(DATA_OFFSET_3), UserContext.class));
                data.add(fromJson(array.getString(DATA_OFFSET_4), GameEngineMode.class));
                data.add(fromJson(array.getString(DATA_OFFSET_5), Boolean.class));
                data.add(fromJson(array.getString(DATA_OFFSET_6), String.class));
                break;
            case INITIALIZE_WARM:
                data.add(fromJson(array.getString(DATA_OFFSET_0), PlanetConfig.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), SlaveSyncItemInfo.class));
                data.add(fromJson(array.getString(DATA_OFFSET_2), UserContext.class));
                data.add(fromJson(array.getString(DATA_OFFSET_3), GameEngineMode.class));
                break;
            case INITIALISING_FAILED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), String.class));
                break;
            case START_BOTS:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                break;
            case EXECUTE_BOT_COMMANDS:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                break;
            case CREATE_RESOURCES:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                break;
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Map.class));
                data.add(fromJson(array.getString(DATA_OFFSET_2), HumanPlayerId.class));
                data.add(fromJson(array.getString(DATA_OFFSET_3), String.class));
                data.add(fromJson(array.getString(DATA_OFFSET_4), DecimalPosition.class));
                break;
            case TICK_UPDATE_RESPONSE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_2), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_3), List.class));
                break;
            case COMMAND_ATTACK:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                break;
            case COMMAND_FINALIZE_BUILD:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                break;
            case COMMAND_BUILD:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), DecimalPosition.class));
                data.add(fromJson(array.getString(DATA_OFFSET_2), Integer.class));
                break;
            case COMMAND_FABRICATE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                break;
            case COMMAND_HARVEST:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                break;
            case COMMAND_MOVE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), DecimalPosition.class));
                break;
            case COMMAND_PICK_BOX:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                break;
            case COMMAND_LOAD_CONTAINER:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                break;
            case COMMAND_UNLOAD_CONTAINER:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), DecimalPosition.class));
                break;
            case RESOURCE_CREATED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), SyncResourceItemSimpleDto.class));
                break;
            case RESOURCE_DELETED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                break;
            case ENERGY_CHANGED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                break;
            case BASE_CREATED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), PlayerBaseDto.class));
                break;
            case BASE_DELETED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                break;
            case BASE_UPDATED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), PlayerBaseDto.class));
                break;
            case USE_INVENTORY_ITEM:
                data.add(fromJson(array.getString(DATA_OFFSET_0), UseInventoryItem.class));
                break;
            case SYNC_ITEM_START_SPAWNED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), SyncBaseItemSimpleDto.class));
                break;
            case SYNC_ITEM_IDLE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), SyncBaseItemSimpleDto.class));
                break;
            case CREATE_BOXES:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                break;
            case BOX_CREATED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), SyncBoxItemSimpleDto.class));
                break;
            case BOX_DELETED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                break;
            case BOX_PICKED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), BoxContent.class));
                break;
            case ACTIVATE_QUEST:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                break;
            case UPDATE_LEVEL:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                break;
            case PROJECTILE_FIRED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Vertex.class));
                data.add(fromJson(array.getString(DATA_OFFSET_2), Vertex.class));
                break;
            case PROJECTILE_DETONATION:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Vertex.class));
                break;
            case PERFMON_RESPONSE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                break;
            case SINGLE_Z_TERRAIN:
                data.add(fromJson(array.getString(DATA_OFFSET_0), DecimalPosition.class));
                break;
            case SINGLE_Z_TERRAIN_ANSWER:
                data.add(fromJson(array.getString(DATA_OFFSET_0), DecimalPosition.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Double.class));
                break;
            case SINGLE_Z_TERRAIN_ANSWER_FAIL:
                data.add(fromJson(array.getString(DATA_OFFSET_0), DecimalPosition.class));
                break;
            case TERRAIN_TILE_REQUEST:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Index.class));
                break;
            case TERRAIN_TILE_RESPONSE:
                data.add(demarshallTerrainTile(array.getObject(DATA_OFFSET_0)));
                break;
            case PLAYBACK_PLAYER_BASE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), PlayerBaseTracking.class));
                break;
            case PLAYBACK_SYNC_ITEM_DELETED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), SyncItemDeletedInfo.class));
                break;
            case PLAYBACK_SYNC_BASE_ITEM:
                data.add(fromJson(array.getString(DATA_OFFSET_0), SyncBaseItemInfo.class));
                break;
            case PLAYBACK_SYNC_RESOURCE_ITEM:
                data.add(fromJson(array.getString(DATA_OFFSET_0), SyncResourceItemInfo.class));
                break;
            case PLAYBACK_SYNC_BOX_ITEM:
                data.add(fromJson(array.getString(DATA_OFFSET_0), SyncBoxItemInfo.class));
                break;
            case QUEST_PROGRESS:
                data.add(fromJson(array.getString(DATA_OFFSET_0), QuestProgressInfo.class));
                break;
            case SELL_ITEMS:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + command);
        }

        return new GameEngineControlPackage(command, data.toArray());
    }

    private static String toJson(Object object) {
        RestClient.setJacksonMarshallingActive(false); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
        try {
            return MarshallingWrapper.toJSON(object);
        } finally {
            RestClient.setJacksonMarshallingActive(true); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
        }
    }

    private static <T> T fromJson(String json, Class<T> type) {
        RestClient.setJacksonMarshallingActive(false); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
        try {
            return MarshallingWrapper.fromJSON(json, type);
        } finally {
            RestClient.setJacksonMarshallingActive(true); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
        }
    }


    private static TerrainTile demarshallTerrainTile(JavaScriptObject data) {
        TerrainTile terrainTile = new TerrainTile() {
        };
        terrainTile.fromArray(data);
        return terrainTile;
    }

    private static JavaScriptObject marshallTerrainTile(TerrainTile terrainTile) {
        return (JavaScriptObject) terrainTile.toArray();
    }

}

package com.btxtech.common;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;

import java.util.ArrayList;
import java.util.List;

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

    public static JavaScriptObject marshall(GameEngineControlPackage controlPackage) {
        JsArrayMixed array = JavaScriptObject.createArray().cast();
        array.set(COMMAND_OFFSET, controlPackage.getCommand().name());
        switch (controlPackage.getCommand()) {
            // No data
            case INITIALIZED:
            case START:
            case STARTED:
            case QUEST_PASSED:
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
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                break;
            // Double JSON data
            case INITIALIZE:
            case COMMAND_ATTACK:
            case COMMAND_FINALIZE_BUILD:
            case COMMAND_FABRICATE:
            case COMMAND_HARVEST:
            case COMMAND_MOVE:
            case COMMAND_PICK_BOX:
            case BASE_CREATED:
            case BASE_DELETED:
            case SPAWN_BASE_ITEMS:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                break;
            // Triple JSON data
            case TICK_UPDATE:
            case COMMAND_BUILD:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.set(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                break;
            // Quadruple JSON data
            // Quintuple  JSON data
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.set(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                array.set(DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                array.set(DATA_OFFSET_4, toJson(controlPackage.getData(4)));
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
            case INITIALIZED:
            case START:
            case STARTED:
            case QUEST_PASSED:
                break;
            case INITIALIZE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), GameEngineConfig.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), UserContext.class));
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
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_2), String.class));
                data.add(fromJson(array.getString(DATA_OFFSET_3), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_4), DecimalPosition.class));
                break;
            case TICK_UPDATE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_2), List.class));
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
            case RESOURCE_CREATED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), SyncResourceItemSimpleDto.class));
                break;
            case RESOURCE_DELETED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                break;
            case BASE_CREATED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), PlayerBaseDto.class));
                break;
            case BASE_DELETED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                break;
            case SPAWN_BASE_ITEMS:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), List.class));
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
}

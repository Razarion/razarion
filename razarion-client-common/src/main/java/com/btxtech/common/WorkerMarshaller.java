package com.btxtech.common;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 03.01.2017.
 */
public class WorkerMarshaller {
    private static Logger logger = Logger.getLogger(WorkerMarshaller.class.getName());
    private static final int COMMAND_OFFSET = 0;
    private static final int DATA_OFFSET_0 = 1;
    private static final int DATA_OFFSET_1 = 2;
    private static final int DATA_OFFSET_2 = 3;

    public static JavaScriptObject marshall(GameEngineControlPackage controlPackage) {
        JsArrayMixed array = JavaScriptObject.createArray().cast();
        array.set(COMMAND_OFFSET, controlPackage.getCommand().name());
        switch (controlPackage.getCommand()) {
            // No data
            case INITIALIZED:
            case START:
            case STARTED:
                break;
            // Single JSON data
            case INITIALIZE:
            case START_BOTS:
            case EXECUTE_BOT_COMMANDS:
            case CREATE_RESOURCES:
            case SYNC_ITEM_UPDATE:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getSingleData()));
                break;
            // Triple JSON data
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.set(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
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
                break;
            case INITIALIZE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), GameEngineConfig.class));
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
                data.add(fromJson(array.getString(DATA_OFFSET_0), UserContext.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_2), DecimalPosition.class));
                break;
            // Javascript structured clone algorithm
            case SYNC_ITEM_UPDATE:
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
}

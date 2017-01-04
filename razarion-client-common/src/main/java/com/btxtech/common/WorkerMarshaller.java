package com.btxtech.common;

import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;

/**
 * Created by Beat
 * 03.01.2017.
 */
public class WorkerMarshaller {
    private static final int COMMAND_OFFSET = 0;
    private static final int DATA_OFFSET = 1;

    public static JavaScriptObject marshall(GameEngineControlPackage controlPackage) {
        JsArrayMixed array = JavaScriptObject.createArray().cast();
        array.set(COMMAND_OFFSET, controlPackage.getCommand().name());
        switch (controlPackage.getCommand()) {
            case INITIALIZE:
                array.set(DATA_OFFSET, toJson(controlPackage.getData()));
                break;
            case INITIALIZED:
            case START:
            case STARTED:
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
        return array;
    }

    public static GameEngineControlPackage deMarshall(Object javaScriptObject) {
        JsArrayMixed array = ((JavaScriptObject) javaScriptObject).cast();
        GameEngineControlPackage.Command command = GameEngineControlPackage.Command.valueOf(array.getString(COMMAND_OFFSET));

        Object data = null;
        switch (command) {
            case INITIALIZE:
                data = fromJson(array.getString(DATA_OFFSET), GameEngineConfig.class);
                break;
            case INITIALIZED:
            case START:
            case STARTED:
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + command);
        }

        return new GameEngineControlPackage(command, data);
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

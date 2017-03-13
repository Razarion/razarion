package com.btxtech.common;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.Float32ArrayEmu;
import com.btxtech.shared.datatypes.shape.SlopeUi;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import elemental.js.util.JsArrayOfNumber;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;

import java.util.ArrayList;
import java.util.Collection;
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
            case LOADED:
            case START:
            case QUEST_PASSED:
            case PERFMON_REQUEST:
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
            case PROJECTILE_DETONATION:
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
            case TICK_UPDATE:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.set(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                array.set(DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                break;
            // Quintuple JSON data
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                array.set(DATA_OFFSET_2, toJson(controlPackage.getData(2)));
                array.set(DATA_OFFSET_3, toJson(controlPackage.getData(3)));
                array.set(DATA_OFFSET_4, toJson(controlPackage.getData(4)));
                break;
            // Native marshal slopes
            case INITIALIZED:
                array.set(DATA_OFFSET_0, marshallSlope(controlPackage.getData(0)));
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
            case QUEST_PASSED:
            case PERFMON_REQUEST:
                break;
            case INITIALIZE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), GameEngineConfig.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), UserContext.class));
                break;
            case INITIALISING_FAILED:
                data.add(fromJson(array.getString(DATA_OFFSET_0), String.class));
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
            // Native marshal slopes
            case INITIALIZED:
                data.add(demarshallNativeSlope(array.getObject(DATA_OFFSET_0)));
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

    public static JavaScriptObject marshallSlope(Object input) {
        JsArrayMixed array = JavaScriptObject.createArray().cast();
        for (Slope slope : (Collection<Slope>) input) {
            array.push(marshallSlope(slope));
        }
        return array;
    }

    private static JavaScriptObject marshallSlope(Slope slope) {
        Mesh mesh = slope.getMesh();
        return marshallNativeSlope(slope.getSlopeSkeletonConfig().getId(),
                mesh.size(),
                vertices2JsArrayOfNumber(mesh.getVertices()),
                vertices2JsArrayOfNumber(mesh.getNorms()),
                vertices2JsArrayOfNumber(mesh.getTangents()),
                floats2JsArrayOfNumber(mesh.getSplatting()),
                floats2JsArrayOfNumber(mesh.getSlopeFactors()));
    }

    private static JsArrayOfNumber vertices2JsArrayOfNumber(List<Vertex> vertices) {
        JsArrayOfNumber numberArray = JsArrayOfNumber.create();
        for (Vertex vertex : vertices) {
            numberArray.push(vertex.getX());
            numberArray.push(vertex.getY());
            numberArray.push(vertex.getZ());
        }
        return numberArray;
    }

    private static JsArrayOfNumber floats2JsArrayOfNumber(List<Float> floats) {
        JsArrayOfNumber numberArray = JsArrayOfNumber.create();
        for (float floatValue : floats) {
            numberArray.push(floatValue);
        }
        return numberArray;
    }

    private static Object demarshallNativeSlope(JavaScriptObject javaScriptObject) {
        Collection<SlopeUi> slopeBuffers = new ArrayList<>();
        JsArrayMixed array = ((JavaScriptObject) javaScriptObject).cast();
        for (int i = 0; i < array.length(); i++) {
            JavaScriptObject slopeJsData = array.getObject(i);
            slopeBuffers.add(new SlopeUi(demarshallNativeSlopeId(slopeJsData), demarshallNativeSlopeElementCount(slopeJsData), demarshallNativeSlopeVertices(slopeJsData), demarshallNativeSlopeNorms(slopeJsData),
                    demarshallNativeSlopeTangents(slopeJsData), demarshallNativeSlopeSplattings(slopeJsData), demarshallNativeSlopeSlopeFactors(slopeJsData)));
        }
        return slopeBuffers;
    }

    // The structured clone algorithm
    private native static JavaScriptObject marshallNativeSlope(int id, int elementCount, JsArrayOfNumber vertices, JsArrayOfNumber norms, JsArrayOfNumber tangents, JsArrayOfNumber splattings, JsArrayOfNumber slopeFactors) /*-{
        return {
            "id": id,
            "elementCount": elementCount,
            "vertices": new Float32Array(vertices),
            "norms": new Float32Array(norms),
            "tangents": new Float32Array(tangents),
            "splattings": new Float32Array(splattings),
            "slopeFactors": new Float32Array(slopeFactors)
        };
    }-*/;

    private native static int demarshallNativeSlopeId(JavaScriptObject slopeJsData) /*-{
        return slopeJsData.id;
    }-*/;

    private native static int demarshallNativeSlopeElementCount(JavaScriptObject slopeJsData) /*-{
        return slopeJsData.elementCount;
    }-*/;

    private native static Float32ArrayEmu demarshallNativeSlopeVertices(JavaScriptObject slopeJsData) /*-{
        return slopeJsData.vertices;
    }-*/;

    private native static Float32ArrayEmu demarshallNativeSlopeNorms(JavaScriptObject slopeJsData) /*-{
        return slopeJsData.norms;
    }-*/;

    private native static Float32ArrayEmu demarshallNativeSlopeTangents(JavaScriptObject slopeJsData) /*-{
        return slopeJsData.tangents;
    }-*/;

    private native static Float32ArrayEmu demarshallNativeSlopeSplattings(JavaScriptObject slopeJsData) /*-{
        return slopeJsData.splattings;
    }-*/;


    private native static Float32ArrayEmu demarshallNativeSlopeSlopeFactors(JavaScriptObject slopeJsData) /*-{
        return slopeJsData.slopeFactors;
    }-*/;

}

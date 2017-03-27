package com.btxtech.common;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.terrain.GroundUi;
import com.btxtech.shared.datatypes.terrain.SlopeUi;
import com.btxtech.shared.datatypes.terrain.WaterUi;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.Water;
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
    private static final String ID_KEY = "id";
    private static final String ELEMENT_COUNT_KEY = "elementCount";
    private static final String VERTICES_KEY = "vertices";
    private static final String NORMS_KEY = "norms";
    private static final String TANGENTS_KEY = "tangents";
    private static final String SPLATTINGS_KEY = "splattings";
    private static final String SLOPE_FACTOR_KEY = "slopeFactor";
    private static final String RECT_X_KEY = "rectX";
    private static final String RECT_Y_KEY = "rectY";
    private static final String RECT_WIDTH_KEY = "rectWidth";
    private static final String RECT_HEIGHT_KEY = "rectHeight";

    public static JavaScriptObject marshall(GameEngineControlPackage controlPackage) {
        JsArrayMixed array = JavaScriptObject.createArray().cast();
        array.set(COMMAND_OFFSET, controlPackage.getCommand().name());
        switch (controlPackage.getCommand()) {
            // No data
            case LOADED:
            case START:
            case QUEST_PASSED:
            case PERFMON_REQUEST:
            case TICK_UPDATE_REQUEST:
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
            case TERRAIN_PICK_RAY:
            case TERRAIN_OVERLAP:
            case SINGLE_Z_TERRAIN_ANSWER_FAIL:
            case TERRAIN_PICK_RAY_ANSWER_FAIL:
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
            case SINGLE_Z_TERRAIN_ANSWER:
            case TERRAIN_PICK_RAY_ANSWER:
            case TERRAIN_OVERLAP_ANSWER:
            case TERRAIN_OVERLAP_TYPE_ANSWER:
                array.set(DATA_OFFSET_0, toJson(controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, toJson(controlPackage.getData(1)));
                break;
            // Triple JSON data
            case COMMAND_BUILD:
            case PROJECTILE_FIRED:
            case TERRAIN_OVERLAP_TYPE:
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
            // Native marshal terrain buffers
            case INITIALIZED:
                array.set(DATA_OFFSET_0, marshallGroundBuffers((VertexList) controlPackage.getData(0)));
                array.set(DATA_OFFSET_1, marshallSlopeBuffers((Collection<Slope>) controlPackage.getData(1)));
                array.set(DATA_OFFSET_2, marshallWaterBuffers((Water) controlPackage.getData(2)));
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
            case TICK_UPDATE_REQUEST:
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
            case TERRAIN_PICK_RAY:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Line3d.class));
                break;
            case TERRAIN_PICK_RAY_ANSWER:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Line.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Vertex.class));
                break;
            case TERRAIN_PICK_RAY_ANSWER_FAIL:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Line.class));
                break;
            case TERRAIN_OVERLAP:
                data.add(fromJson(array.getString(DATA_OFFSET_0), DecimalPosition.class));
                break;
            case TERRAIN_OVERLAP_ANSWER:
                data.add(fromJson(array.getString(DATA_OFFSET_0), DecimalPosition.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Boolean.class));
                break;
            case TERRAIN_OVERLAP_TYPE:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), List.class));
                data.add(fromJson(array.getString(DATA_OFFSET_2), Integer.class));
                break;
            case TERRAIN_OVERLAP_TYPE_ANSWER:
                data.add(fromJson(array.getString(DATA_OFFSET_0), Integer.class));
                data.add(fromJson(array.getString(DATA_OFFSET_1), Boolean.class));
                break;
            // Native demarshal terrain buffers
            case INITIALIZED:
                data.add(demarshallGroundBuffers(array.getObject(DATA_OFFSET_0)));
                data.add(demarshallSlopeBuffers(array.getObject(DATA_OFFSET_1)));
                data.add(demarshallWaterBuffers(array.getObject(DATA_OFFSET_2)));
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

    private static JavaScriptObject marshallGroundBuffers(VertexList groundVertexList) {
        JavaScriptObject javaScriptObject = JavaScriptObject.createObject();
        setIntProperty(javaScriptObject, ELEMENT_COUNT_KEY, groundVertexList.getVerticesCount());
        setFloat32ArrayProperty(javaScriptObject, VERTICES_KEY, vertices2JsArrayOfVertices(groundVertexList.getVertices()));
        setFloat32ArrayProperty(javaScriptObject, NORMS_KEY, vertices2JsArrayOfVertices(groundVertexList.getNormVertices()));
        setFloat32ArrayProperty(javaScriptObject, TANGENTS_KEY, vertices2JsArrayOfVertices(groundVertexList.getTangentVertices()));
        setFloat32ArrayProperty(javaScriptObject, SPLATTINGS_KEY, doubles2JsArrayOfNumber(groundVertexList.getSplattings()));
        return javaScriptObject;
    }

    private static GroundUi demarshallGroundBuffers(JavaScriptObject javaScriptObject) {
        return new GroundUi(getIntProperty(javaScriptObject, ELEMENT_COUNT_KEY),
                getFloat32ArrayEmuProperty(javaScriptObject, VERTICES_KEY), getFloat32ArrayEmuProperty(javaScriptObject, NORMS_KEY),
                getFloat32ArrayEmuProperty(javaScriptObject, TANGENTS_KEY), getFloat32ArrayEmuProperty(javaScriptObject, SPLATTINGS_KEY));
    }


    private static JavaScriptObject marshallSlopeBuffers(Collection<Slope> slopes) {
        JsArrayMixed array = JavaScriptObject.createArray().cast();
        for (Slope slope : slopes) {
            array.push(marshallSlope(slope));
        }
        return array;
    }

    private static JavaScriptObject marshallSlope(Slope slope) {
        Mesh mesh = slope.getMesh();
        JavaScriptObject javaScriptObject = JavaScriptObject.createObject();
        setIntProperty(javaScriptObject, ID_KEY, slope.getSlopeSkeletonConfig().getId());
        setIntProperty(javaScriptObject, ELEMENT_COUNT_KEY, mesh.size());
        setFloat32ArrayProperty(javaScriptObject, VERTICES_KEY, vertices2JsArrayOfVertices(mesh.getVertices()));
        setFloat32ArrayProperty(javaScriptObject, NORMS_KEY, vertices2JsArrayOfVertices(mesh.getNorms()));
        setFloat32ArrayProperty(javaScriptObject, TANGENTS_KEY, vertices2JsArrayOfVertices(mesh.getTangents()));
        setFloat32ArrayProperty(javaScriptObject, SPLATTINGS_KEY, floats2JsArrayOfNumber(mesh.getSplatting()));
        setFloat32ArrayProperty(javaScriptObject, SLOPE_FACTOR_KEY, floats2JsArrayOfNumber(mesh.getSlopeFactors()));
        return javaScriptObject;
    }

    private static Collection<SlopeUi> demarshallSlopeBuffers(JavaScriptObject javaScriptObject) {
        Collection<SlopeUi> slopeBuffers = new ArrayList<>();
        JsArrayMixed array = ((JavaScriptObject) javaScriptObject).cast();
        for (int i = 0; i < array.length(); i++) {
            JavaScriptObject slopeJsData = array.getObject(i);
            slopeBuffers.add(new SlopeUi(getIntProperty(slopeJsData, ID_KEY), getIntProperty(slopeJsData, ELEMENT_COUNT_KEY),
                    getFloat32ArrayEmuProperty(slopeJsData, VERTICES_KEY), getFloat32ArrayEmuProperty(slopeJsData, NORMS_KEY),
                    getFloat32ArrayEmuProperty(slopeJsData, TANGENTS_KEY), getFloat32ArrayEmuProperty(slopeJsData, SPLATTINGS_KEY),
                    getFloat32ArrayEmuProperty(slopeJsData, SLOPE_FACTOR_KEY)));
        }
        return slopeBuffers;
    }

    private static JavaScriptObject marshallWaterBuffers(Water water) {
        JavaScriptObject javaScriptObject = JavaScriptObject.createObject();
        setIntProperty(javaScriptObject, ELEMENT_COUNT_KEY, water.getVertices().size());
        setFloat32ArrayProperty(javaScriptObject, VERTICES_KEY, vertices2JsArrayOfVertices(water.getVertices()));
        setFloat32ArrayProperty(javaScriptObject, NORMS_KEY, vertices2JsArrayOfVertices(water.getNorms()));
        setFloat32ArrayProperty(javaScriptObject, TANGENTS_KEY, vertices2JsArrayOfVertices(water.getTangents()));
        setDoubleProperty(javaScriptObject, RECT_X_KEY, water.calculateAabb().startX());
        setDoubleProperty(javaScriptObject, RECT_Y_KEY, water.calculateAabb().startY());
        setDoubleProperty(javaScriptObject, RECT_WIDTH_KEY, water.calculateAabb().width());
        setDoubleProperty(javaScriptObject, RECT_HEIGHT_KEY, water.calculateAabb().height());
        return javaScriptObject;
    }

    private static WaterUi demarshallWaterBuffers(JavaScriptObject waterJsData) {
        Rectangle2D aabb = new Rectangle2D(getDoubleProperty(waterJsData, RECT_X_KEY), getDoubleProperty(waterJsData, RECT_Y_KEY), getDoubleProperty(waterJsData, RECT_WIDTH_KEY), getDoubleProperty(waterJsData, RECT_HEIGHT_KEY));
        return new WaterUi(getIntProperty(waterJsData, ELEMENT_COUNT_KEY), getFloat32ArrayEmuProperty(waterJsData, VERTICES_KEY), getFloat32ArrayEmuProperty(waterJsData, NORMS_KEY),
                getFloat32ArrayEmuProperty(waterJsData, TANGENTS_KEY), aabb);
    }

    private static JsArrayOfNumber vertices2JsArrayOfVertices(List<Vertex> vertices) {
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

    private static JsArrayOfNumber doubles2JsArrayOfNumber(List<Double> doubles) {
        JsArrayOfNumber numberArray = JsArrayOfNumber.create();
        for (double doubleValue : doubles) {
            numberArray.push(doubleValue);
        }
        return numberArray;
    }

    private native static int getIntProperty(JavaScriptObject javaScriptObject, String propertyName) /*-{
        return javaScriptObject[propertyName];
    }-*/;

    private native static double getDoubleProperty(JavaScriptObject javaScriptObject, String propertyName) /*-{
        return javaScriptObject[propertyName];
    }-*/;

    private native static Float32ArrayEmu getFloat32ArrayEmuProperty(JavaScriptObject javaScriptObject, String propertyName) /*-{
        return javaScriptObject[propertyName];
    }-*/;

    private native static void setIntProperty(JavaScriptObject javaScriptObject, String propertyName, int number) /*-{
        return javaScriptObject[propertyName] = number;
    }-*/;

    private native static void setDoubleProperty(JavaScriptObject javaScriptObject, String propertyName, double number) /*-{
        return javaScriptObject[propertyName] = number;
    }-*/;

    private native static void setFloat32ArrayProperty(JavaScriptObject javaScriptObject, String propertyName, JsArrayOfNumber array) /*-{
        return javaScriptObject[propertyName] = new Float32Array(array);
    }-*/;
}

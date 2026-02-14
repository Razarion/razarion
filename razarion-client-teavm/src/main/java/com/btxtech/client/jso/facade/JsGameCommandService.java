package com.btxtech.client.jso.facade;

import com.btxtech.client.jso.JsObject;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.terrain.InputService;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Creates a JS proxy for GameCommandService that sends commands with raw ID arrays
 * directly to the GameEngineControl (worker).
 */
public class JsGameCommandService {

    public static JSObject createProxy(GameEngineControl gameEngineControl, InputService inputService) {
        JsObject proxy = JsObject.create();

        // moveCmd(itemIds: number[], x: number, y: number)
        setMoveCmd(proxy, "moveCmd", (jsIds, x, y) -> {
            int[] ids = jsArrayToIntArray(jsIds);
            gameEngineControl.moveCmdIds(ids, x, y);
        });

        // attackCmd(itemIds: number[], targetId: number)
        setArrayIntCmd(proxy, "attackCmd", (jsIds, targetId) -> {
            int[] ids = jsArrayToIntArray(jsIds);
            gameEngineControl.attackCmdIds(ids, targetId);
        });

        // harvestCmd(itemIds: number[], resourceId: number)
        setArrayIntCmd(proxy, "harvestCmd", (jsIds, resourceId) -> {
            int[] ids = jsArrayToIntArray(jsIds);
            gameEngineControl.harvestCmdIds(ids, resourceId);
        });

        // pickBoxCmd(itemIds: number[], boxId: number)
        setArrayIntCmd(proxy, "pickBoxCmd", (jsIds, boxId) -> {
            int[] ids = jsArrayToIntArray(jsIds);
            gameEngineControl.pickBoxCmdIds(ids, boxId);
        });

        // loadContainerCmd(itemIds: number[], containerId: number)
        setArrayIntCmd(proxy, "loadContainerCmd", (jsIds, containerId) -> {
            int[] ids = jsArrayToIntArray(jsIds);
            gameEngineControl.loadContainerCmdIds(ids, containerId);
        });

        // finalizeBuildCmd(itemIds: number[], toBeFinalizedId: number)
        setArrayIntCmd(proxy, "finalizeBuildCmd", (jsIds, toBeFinalizedId) -> {
            int[] ids = jsArrayToIntArray(jsIds);
            gameEngineControl.finalizeBuildCmdIds(ids, toBeFinalizedId);
        });

        // setMoveCommandAckCallback(callback: () => void)
        setCallbackSetter(proxy, "setMoveCommandAckCallback", (callback) -> {
            inputService.setMoveCommandAckCallback(() -> callJsFunction(callback));
        });

        return proxy;
    }

    // --- JS array conversion ---

    @JSBody(params = {"jsArray"}, script =
            "var len = jsArray.length; var result = []; for (var i = 0; i < len; i++) { result[i] = jsArray[i] | 0; } return result;")
    private static native int[] jsArrayToIntArray(JSObject jsArray);

    // --- Functor interfaces ---

    @JSFunctor
    interface MoveCmdCallback extends JSObject {
        void call(JSObject itemIds, double x, double y);
    }

    @JSFunctor
    interface ArrayIntCmdCallback extends JSObject {
        void call(JSObject itemIds, int targetId);
    }

    @JSFunctor
    interface CallbackSetterCallback extends JSObject {
        void call(JSObject callback);
    }

    // --- setMethod helpers ---

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMoveCmd(JSObject obj, String name, MoveCmdCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setArrayIntCmd(JSObject obj, String name, ArrayIntCmdCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setCallbackSetter(JSObject obj, String name, CallbackSetterCallback fn);

    @JSBody(params = {"fn"}, script = "fn();")
    private static native void callJsFunction(JSObject fn);
}

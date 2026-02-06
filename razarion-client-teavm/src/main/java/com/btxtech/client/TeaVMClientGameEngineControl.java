package com.btxtech.client;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsWorker;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeDecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.user.UserUiService;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class TeaVMClientGameEngineControl extends GameEngineControl {
    private final Provider<TeaVMLifecycleService> lifecycleService;
    private JsWorker worker;
    private DeferredStartup deferredStartup;

    @Inject
    public TeaVMClientGameEngineControl(Provider<InputService> inputServices,
                                        PerfmonService perfmonService,
                                        Provider<Boot> boot,
                                        TerrainUiService terrainUiService,
                                        InventoryUiService inventoryUiService,
                                        UserUiService userUiService,
                                        SelectionService selectionService,
                                        GameUiControl gameUiControl,
                                        AudioService audioService,
                                        BoxUiService boxUiService,
                                        ResourceUiService resourceUiService,
                                        BaseItemUiService baseItemUiService,
                                        Provider<TeaVMLifecycleService> lifecycleService,
                                        BabylonRendererService babylonRendererService) {
        super(inputServices,
                perfmonService,
                boot,
                terrainUiService,
                inventoryUiService,
                userUiService,
                selectionService,
                gameUiControl,
                audioService,
                boxUiService,
                resourceUiService,
                baseItemUiService,
                babylonRendererService);
        this.lifecycleService = lifecycleService;
    }

    @Override
    public boolean isStarted() {
        return worker != null;
    }

    public void loadWorker(DeferredStartup deferredStartup) {
        this.deferredStartup = deferredStartup;
        try {
            worker = JsWorker.create(CommonUrl.getWorkerScriptUrl());
            worker.setOnMessage(evt -> {
                try {
                    JSObject data = evt.getData();
                    GameEngineControlPackage controlPackage = TeaVMClientMarshaller.deMarshall(data);
                    dispatch(controlPackage);
                } catch (Throwable t) {
                    JsConsole.error("[CLIENT-WASM] Exception during dispatch: " + t.getMessage());
                    JsConsole.error("Stack trace: " + getStackTrace(t));
                }
            });
            worker.setOnError(evt -> {
                JsConsole.error("TeaVMClientGameEngineControl: worker error");
            });
        } catch (Throwable t) {
            this.deferredStartup.failed(t);
            this.deferredStartup = null;
        }
    }

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object... data) {
        try {
            JSObject message = TeaVMClientMarshaller.marshall(new GameEngineControlPackage(command, data));
            postMessage(worker, message);
        } catch (Throwable t) {
            JsConsole.error("worker.postMessage() failed: " + command + " " + t.getMessage());
        }
    }

    @Override
    protected void onLoaded() {
        JsConsole.log("[CLIENT-WASM] ✓ Worker loaded successfully");
        if (deferredStartup != null) {
            deferredStartup.finished();
            deferredStartup = null;
        }
    }

    @Override
    public void enableTracking() {
        // TODO: implement tracking
    }

    @Override
    protected void onConnectionLost() {
        lifecycleService.get().onConnectionLost("ClientServerGameConnection");
    }

    @Override
    protected NativeTickInfo castToNativeTickInfo(Object javaScriptObject) {
        JSObject jsObj = (JSObject) javaScriptObject;
        NativeTickInfo result = new NativeTickInfo();
        result.resources = jsGetInt(jsObj, "resources");
        result.xpFromKills = jsGetInt(jsObj, "xpFromKills");
        result.houseSpace = jsGetInt(jsObj, "houseSpace");

        // updatedNativeSyncBaseItemTickInfos
        JSObject updatedArr = jsGetProp(jsObj, "updatedNativeSyncBaseItemTickInfos");
        if (!jsIsNullOrUndefined(updatedArr)) {
            int len = jsLength(updatedArr);
            result.updatedNativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[len];
            for (int i = 0; i < len; i++) {
                result.updatedNativeSyncBaseItemTickInfos[i] = convertSyncBaseItemTickInfo(jsArrayGet(updatedArr, i));
            }
        }

        // killedSyncBaseItems
        JSObject killedArr = jsGetProp(jsObj, "killedSyncBaseItems");
        if (!jsIsNullOrUndefined(killedArr)) {
            int len = jsLength(killedArr);
            result.killedSyncBaseItems = new NativeSimpleSyncBaseItemTickInfo[len];
            for (int i = 0; i < len; i++) {
                result.killedSyncBaseItems[i] = convertSimpleSyncBaseItemTickInfo(jsArrayGet(killedArr, i));
            }
        }

        // removeSyncBaseItemIds
        JSObject removeArr = jsGetProp(jsObj, "removeSyncBaseItemIds");
        if (!jsIsNullOrUndefined(removeArr)) {
            int len = jsLength(removeArr);
            result.removeSyncBaseItemIds = new int[len];
            for (int i = 0; i < len; i++) {
                result.removeSyncBaseItemIds[i] = jsArrayGetInt(removeArr, i);
            }
        }

        return result;
    }

    @Override
    protected NativeSyncBaseItemTickInfo castToNativeSyncBaseItemTickInfo(Object javaScriptObject) {
        return convertSyncBaseItemTickInfo((JSObject) javaScriptObject);
    }

    private static NativeSyncBaseItemTickInfo convertSyncBaseItemTickInfo(JSObject jsObj) {
        NativeSyncBaseItemTickInfo info = new NativeSyncBaseItemTickInfo();
        info.id = jsGetInt(jsObj, "id");
        info.itemTypeId = jsGetInt(jsObj, "itemTypeId");
        info.x = jsGetDouble(jsObj, "x");
        info.y = jsGetDouble(jsObj, "y");
        info.z = jsGetDouble(jsObj, "z");
        info.angle = jsGetDouble(jsObj, "angle");
        info.baseId = jsGetInt(jsObj, "baseId");
        info.turretAngle = jsGetDouble(jsObj, "turretAngle");
        info.spawning = jsGetDouble(jsObj, "spawning");
        info.buildup = jsGetDouble(jsObj, "buildup");
        info.health = jsGetDouble(jsObj, "health");
        info.constructing = jsGetDouble(jsObj, "constructing");
        info.constructingBaseItemTypeId = jsGetInt(jsObj, "constructingBaseItemTypeId");
        info.contained = jsGetBoolean(jsObj, "contained");
        info.idle = jsGetBoolean(jsObj, "idle");
        info.maxContainingRadius = jsGetDouble(jsObj, "maxContainingRadius");

        // harvestingResourcePosition
        JSObject harvestPos = jsGetProp(jsObj, "harvestingResourcePosition");
        if (!jsIsNullOrUndefined(harvestPos)) {
            info.harvestingResourcePosition = new NativeDecimalPosition();
            info.harvestingResourcePosition.x = jsGetDouble(harvestPos, "x");
            info.harvestingResourcePosition.y = jsGetDouble(harvestPos, "y");
        }

        // buildingPosition
        JSObject buildPos = jsGetProp(jsObj, "buildingPosition");
        if (!jsIsNullOrUndefined(buildPos)) {
            info.buildingPosition = new NativeDecimalPosition();
            info.buildingPosition.x = jsGetDouble(buildPos, "x");
            info.buildingPosition.y = jsGetDouble(buildPos, "y");
        }

        // containingItemTypeIds
        JSObject containArr = jsGetProp(jsObj, "containingItemTypeIds");
        if (!jsIsNullOrUndefined(containArr)) {
            int len = jsLength(containArr);
            info.containingItemTypeIds = new int[len];
            for (int i = 0; i < len; i++) {
                info.containingItemTypeIds[i] = jsArrayGetInt(containArr, i);
            }
        }

        return info;
    }

    private static NativeSimpleSyncBaseItemTickInfo convertSimpleSyncBaseItemTickInfo(JSObject jsObj) {
        NativeSimpleSyncBaseItemTickInfo info = new NativeSimpleSyncBaseItemTickInfo();
        info.id = jsGetInt(jsObj, "id");
        info.itemTypeId = jsGetInt(jsObj, "itemTypeId");
        info.x = jsGetDouble(jsObj, "x");
        info.y = jsGetDouble(jsObj, "y");
        info.contained = jsGetBoolean(jsObj, "contained");
        return info;
    }

    // ============ JS interop helpers ============

    @JSBody(params = {"worker", "message"}, script = "worker.postMessage(message);")
    private static native void postMessage(JsWorker worker, JSObject message);

    @JSBody(params = {"obj", "key"}, script = "return obj[key] || 0;")
    private static native int jsGetInt(JSObject obj, String key);

    @JSBody(params = {"obj", "key"}, script = "var v = obj[key]; return (v === undefined || v === null || Number.isNaN(v) || v !== v) ? 0.0 : v;")
    private static native double jsGetDouble(JSObject obj, String key);

    @JSBody(params = {"obj", "key"}, script = "return !!obj[key];")
    private static native boolean jsGetBoolean(JSObject obj, String key);

    @JSBody(params = {"obj", "key"}, script = "return obj[key];")
    private static native JSObject jsGetProp(JSObject obj, String key);

    @JSBody(params = {"obj"}, script = "return obj === null || obj === undefined;")
    private static native boolean jsIsNullOrUndefined(JSObject obj);

    @JSBody(params = {"arr"}, script = "return arr.length;")
    private static native int jsLength(JSObject arr);

    @JSBody(params = {"arr", "index"}, script = "return arr[index];")
    private static native JSObject jsArrayGet(JSObject arr, int index);

    @JSBody(params = {"arr", "index"}, script = "return arr[index] || 0;")
    private static native int jsArrayGetInt(JSObject arr, int index);

    @JSBody(params = {"obj"}, script = "return typeof obj + (Array.isArray(obj) ? ' (array)' : '');")
    private static native String getJsType(JSObject obj);

    private static String getStackTrace(Throwable t) {
        if (t == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getName()).append(": ").append(t.getMessage());
        StackTraceElement[] elements = t.getStackTrace();
        if (elements != null) {
            for (int i = 0; i < Math.min(5, elements.length); i++) {
                sb.append("\n  at ").append(elements[i].toString());
            }
        }
        return sb.toString();
    }
}

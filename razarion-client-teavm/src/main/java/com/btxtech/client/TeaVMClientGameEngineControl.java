package com.btxtech.client;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsWorker;
import com.btxtech.client.jso.SharedTickBufferReader;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeDecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SharedTickBufferLayout;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.SelectionService;
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
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class TeaVMClientGameEngineControl extends GameEngineControl {
    private final Provider<TeaVMLifecycleService> lifecycleService;
    private JsWorker worker;
    private DeferredStartup deferredStartup;
    private SharedTickBufferReader sharedTickBufferReader;
    private boolean sharedBufferMode;
    private boolean initialSyncComplete;

    @Inject
    public TeaVMClientGameEngineControl(Provider<InputService> inputServices,
                                        PerfmonService perfmonService,
                                        Provider<Boot> boot,
                                        TerrainUiService terrainUiService,
                                        InventoryUiService inventoryUiService,
                                        UserUiService userUiService,
                                        SelectionService selectionService,
                                        GameUiControl gameUiControl,
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
            worker.setOnMessage(evt -> safeWasmCall(() -> {
                JSObject data = evt.getData();
                GameEngineControlPackage controlPackage = TeaVMClientMarshaller.deMarshall(data);
                if (controlPackage.getCommand() == GameEngineControlPackage.Command.INITIAL_SLAVE_SYNCHRONIZED
                        || controlPackage.getCommand() == GameEngineControlPackage.Command.INITIAL_SLAVE_SYNCHRONIZED_NO_BASE) {
                    initialSyncComplete = true;
                }
                dispatch(controlPackage);
            }));
            worker.setOnError(evt -> {
                JsConsole.error("TeaVMClientGameEngineControl: worker error");
            });
        } catch (Throwable t) {
            this.deferredStartup.failed(t);
            this.deferredStartup = null;
        }
    }

    private void initSharedTickBuffer() {
        try {
            if (false) {
                JsConsole.log("[CLIENT-WASM] SharedBuffer disabled for debugging, using postMessage fallback");
                return;
            }
            if (!isCrossOriginIsolated()) {
                JsConsole.log("[CLIENT-WASM] crossOriginIsolated=false, using postMessage for tick data");
                return;
            }
            JSObject sab = createSharedArrayBuffer(SharedTickBufferLayout.TOTAL_BYTES);
            if (sab == null) {
                JsConsole.log("[CLIENT-WASM] SharedArrayBuffer not available, using postMessage fallback");
                return;
            }
            // Send init message to worker
            sendSharedBufferInit(worker, sab);
            sharedTickBufferReader = new SharedTickBufferReader(sab);
            sharedBufferMode = true;
            JsConsole.log("[CLIENT-WASM] SharedArrayBuffer tick transfer initialized (" + SharedTickBufferLayout.TOTAL_BYTES + " bytes)");
        } catch (Throwable t) {
            JsConsole.log("[CLIENT-WASM] SharedArrayBuffer init failed: " + t.getMessage() + ", using postMessage fallback");
            sharedBufferMode = false;
            sharedTickBufferReader = null;
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
    protected boolean isSharedBufferMode() {
        return sharedBufferMode;
    }

    @Override
    public void start(String bearerToken) {
        super.start(bearerToken);
        if (sharedBufferMode) {
            startPollLoop();
        }
    }

    private void startPollLoop() {
        requestAnimationFrame(timestamp -> pollTick());
    }

    private void pollTick() {
        safeWasmCall(() -> {
            if (initialSyncComplete && sharedTickBufferReader != null && sharedTickBufferReader.hasNewData()) {
                NativeTickInfo tickInfo = sharedTickBufferReader.readTickData();
                onTickUpdate(tickInfo);
            }
        });
        if (worker != null) {
            requestAnimationFrame(timestamp -> pollTick());
        }
    }

    @Override
    protected void onLoaded() {
        JsConsole.log("[CLIENT-WASM] Worker loaded, initializing SharedArrayBuffer...");
        // Worker's onmessage handler is now ready — safe to send SAB init
        initSharedTickBuffer();
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

    // ============ TypedArray tick decoding constants ============
    private static final int DOUBLES_PER_ITEM = 14;
    private static final int INTS_PER_ITEM = 4;
    private static final int KILLED_DOUBLES_PER_ITEM = 2;
    private static final int KILLED_INTS_PER_ITEM = 2;

    // Wire-format slot indices (matching worker encoding)
    private static final int TICK_ITEM_COUNT = 1;
    private static final int TICK_RESOURCES = 2;
    private static final int TICK_XP_FROM_KILLS = 3;
    private static final int TICK_HOUSE_SPACE = 4;
    private static final int TICK_DOUBLES = 5;
    private static final int TICK_INTS = 6;
    private static final int TICK_FLAGS = 7;
    private static final int TICK_CONTAINING_IDS = 8;
    private static final int TICK_KILLED_COUNT = 9;
    private static final int TICK_KILLED_DOUBLES = 10;
    private static final int TICK_KILLED_INTS = 11;
    private static final int TICK_KILLED_FLAGS = 12;
    private static final int TICK_REMOVE_IDS = 13;

    @Override
    protected NativeTickInfo castToNativeTickInfo(Object javaScriptObject) {
        JSObject array = (JSObject) javaScriptObject;
        NativeTickInfo result = new NativeTickInfo();

        int itemCount = jsArrayGetInt(array, TICK_ITEM_COUNT);
        result.resources = jsArrayGetInt(array, TICK_RESOURCES);
        result.xpFromKills = jsArrayGetInt(array, TICK_XP_FROM_KILLS);
        result.houseSpace = jsArrayGetInt(array, TICK_HOUSE_SPACE);

        // Decode updated items from TypedArrays
        if (itemCount > 0) {
            JSObject tickDoubles = jsArrayGet(array, TICK_DOUBLES);
            JSObject tickInts = jsArrayGet(array, TICK_INTS);
            JSObject tickFlags = jsArrayGet(array, TICK_FLAGS);
            JSObject containingIds = jsArrayGet(array, TICK_CONTAINING_IDS);

            result.updatedNativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[itemCount];
            int containingOffset = 0;

            for (int i = 0; i < itemCount; i++) {
                NativeSyncBaseItemTickInfo info = new NativeSyncBaseItemTickInfo();
                int dOff = i * DOUBLES_PER_ITEM;
                int iOff = i * INTS_PER_ITEM;

                // Doubles
                info.x = getFloat64(tickDoubles, dOff + 0);
                info.y = getFloat64(tickDoubles, dOff + 1);
                info.z = getFloat64(tickDoubles, dOff + 2);
                info.angle = getFloat64(tickDoubles, dOff + 3);
                info.turretAngle = getFloat64(tickDoubles, dOff + 4);
                info.spawning = getFloat64(tickDoubles, dOff + 5);
                info.buildup = getFloat64(tickDoubles, dOff + 6);
                info.health = getFloat64(tickDoubles, dOff + 7);
                info.constructing = getFloat64(tickDoubles, dOff + 8);
                info.maxContainingRadius = getFloat64(tickDoubles, dOff + 9);

                // Optional positions (NaN = absent)
                double harvestX = getFloat64(tickDoubles, dOff + 10);
                if (!Double.isNaN(harvestX)) {
                    info.harvestingResourcePosition = new NativeDecimalPosition();
                    info.harvestingResourcePosition.x = harvestX;
                    info.harvestingResourcePosition.y = getFloat64(tickDoubles, dOff + 11);
                }
                double buildX = getFloat64(tickDoubles, dOff + 12);
                if (!Double.isNaN(buildX)) {
                    info.buildingPosition = new NativeDecimalPosition();
                    info.buildingPosition.x = buildX;
                    info.buildingPosition.y = getFloat64(tickDoubles, dOff + 13);
                }

                // Ints
                info.id = getInt32(tickInts, iOff + 0);
                info.itemTypeId = getInt32(tickInts, iOff + 1);
                info.baseId = getInt32(tickInts, iOff + 2);
                info.constructingBaseItemTypeId = getInt32(tickInts, iOff + 3);

                // Flags
                int flags = getUint8(tickFlags, i);
                info.contained = (flags & 1) != 0;
                info.idle = (flags & 2) != 0;
                boolean hasContaining = (flags & 4) != 0;

                // ContainingItemTypeIds (prefix-length encoding)
                if (hasContaining && !jsIsNullOrUndefined(containingIds)) {
                    int count = getInt32(containingIds, containingOffset++);
                    info.containingItemTypeIds = new int[count];
                    for (int c = 0; c < count; c++) {
                        info.containingItemTypeIds[c] = getInt32(containingIds, containingOffset++);
                    }
                }

                result.updatedNativeSyncBaseItemTickInfos[i] = info;
            }
        }

        // Decode killed items
        int killedCount = jsArrayGetInt(array, TICK_KILLED_COUNT);
        if (killedCount > 0) {
            JSObject killedDoubles = jsArrayGet(array, TICK_KILLED_DOUBLES);
            JSObject killedInts = jsArrayGet(array, TICK_KILLED_INTS);
            JSObject killedFlags = jsArrayGet(array, TICK_KILLED_FLAGS);

            result.killedSyncBaseItems = new NativeSimpleSyncBaseItemTickInfo[killedCount];
            for (int i = 0; i < killedCount; i++) {
                NativeSimpleSyncBaseItemTickInfo k = new NativeSimpleSyncBaseItemTickInfo();
                k.x = getFloat64(killedDoubles, i * 2);
                k.y = getFloat64(killedDoubles, i * 2 + 1);
                k.id = getInt32(killedInts, i * 2);
                k.itemTypeId = getInt32(killedInts, i * 2 + 1);
                k.contained = getUint8(killedFlags, i) != 0;
                result.killedSyncBaseItems[i] = k;
            }
        }

        // Decode remove IDs
        JSObject removeArr = jsArrayGet(array, TICK_REMOVE_IDS);
        if (!jsIsNullOrUndefined(removeArr)) {
            int len = jsLength(removeArr);
            result.removeSyncBaseItemIds = new int[len];
            for (int i = 0; i < len; i++) {
                result.removeSyncBaseItemIds[i] = getInt32(removeArr, i);
            }
        }

        return result;
    }

    @Override
    protected NativeSyncBaseItemTickInfo castToNativeSyncBaseItemTickInfo(Object javaScriptObject) {
        JSObject array = (JSObject) javaScriptObject;
        int itemCount = jsArrayGetInt(array, TICK_ITEM_COUNT);
        if (itemCount == 0) {
            return new NativeSyncBaseItemTickInfo();
        }

        JSObject tickDoubles = jsArrayGet(array, TICK_DOUBLES);
        JSObject tickInts = jsArrayGet(array, TICK_INTS);
        JSObject tickFlags = jsArrayGet(array, TICK_FLAGS);

        NativeSyncBaseItemTickInfo info = new NativeSyncBaseItemTickInfo();

        // Doubles
        info.x = getFloat64(tickDoubles, 0);
        info.y = getFloat64(tickDoubles, 1);
        info.z = getFloat64(tickDoubles, 2);
        info.angle = getFloat64(tickDoubles, 3);
        info.turretAngle = getFloat64(tickDoubles, 4);
        info.spawning = getFloat64(tickDoubles, 5);
        info.buildup = getFloat64(tickDoubles, 6);
        info.health = getFloat64(tickDoubles, 7);
        info.constructing = getFloat64(tickDoubles, 8);
        info.maxContainingRadius = getFloat64(tickDoubles, 9);

        double harvestX = getFloat64(tickDoubles, 10);
        if (!Double.isNaN(harvestX)) {
            info.harvestingResourcePosition = new NativeDecimalPosition();
            info.harvestingResourcePosition.x = harvestX;
            info.harvestingResourcePosition.y = getFloat64(tickDoubles, 11);
        }
        double buildX = getFloat64(tickDoubles, 12);
        if (!Double.isNaN(buildX)) {
            info.buildingPosition = new NativeDecimalPosition();
            info.buildingPosition.x = buildX;
            info.buildingPosition.y = getFloat64(tickDoubles, 13);
        }

        // Ints
        info.id = getInt32(tickInts, 0);
        info.itemTypeId = getInt32(tickInts, 1);
        info.baseId = getInt32(tickInts, 2);
        info.constructingBaseItemTypeId = getInt32(tickInts, 3);

        // Flags
        int flags = getUint8(tickFlags, 0);
        info.contained = (flags & 1) != 0;
        info.idle = (flags & 2) != 0;
        boolean hasContaining = (flags & 4) != 0;

        if (hasContaining) {
            JSObject containingIds = jsArrayGet(array, TICK_CONTAINING_IDS);
            if (!jsIsNullOrUndefined(containingIds)) {
                int count = getInt32(containingIds, 0);
                info.containingItemTypeIds = new int[count];
                for (int c = 0; c < count; c++) {
                    info.containingItemTypeIds[c] = getInt32(containingIds, c + 1);
                }
            }
        }

        return info;
    }

    // ============ JS interop helpers ============

    @JSBody(params = {"worker", "message"}, script = "worker.postMessage(message);")
    private static native void postMessage(JsWorker worker, JSObject message);

    @JSBody(params = {"obj"}, script = "return obj === null || obj === undefined;")
    private static native boolean jsIsNullOrUndefined(JSObject obj);

    @JSBody(params = {"arr"}, script = "return arr.length;")
    private static native int jsLength(JSObject arr);

    @JSBody(params = {"arr", "index"}, script = "return arr[index];")
    private static native JSObject jsArrayGet(JSObject arr, int index);

    @JSBody(params = {"arr", "index"}, script = "return arr[index] | 0;")
    private static native int jsArrayGetInt(JSObject arr, int index);

    // TypedArray reading helpers
    @JSBody(params = {"arr", "index"}, script = "return arr[index];")
    private static native double getFloat64(JSObject arr, int index);

    @JSBody(params = {"arr", "index"}, script = "return arr[index] | 0;")
    private static native int getInt32(JSObject arr, int index);

    @JSBody(params = {"arr", "index"}, script = "return arr[index] | 0;")
    private static native int getUint8(JSObject arr, int index);

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

    // ============ SharedArrayBuffer JS interop ============

    @JSBody(script = "return typeof crossOriginIsolated !== 'undefined' && crossOriginIsolated === true;")
    private static native boolean isCrossOriginIsolated();

    @JSBody(params = {"size"}, script = "try { return new SharedArrayBuffer(size); } catch(e) { return null; }")
    private static native JSObject createSharedArrayBuffer(int size);

    @JSBody(params = {"worker", "sab"}, script = "worker.postMessage({type: 'shared-tick-buffer', buffer: sab});")
    private static native void sendSharedBufferInit(JsWorker worker, JSObject sab);

    @JSBody(params = {"callback"}, script = "requestAnimationFrame(callback);")
    private static native void requestAnimationFrame(RafCallback callback);

    @JSBody(params = {"fn"}, script = "try { fn(); } catch(e) { console.error('[WASM trap]', e); }")
    private static native void safeWasmCall(SafeCallback fn);

    @JSFunctor
    interface RafCallback extends JSObject {
        void onFrame(double timestamp);
    }

    @JSFunctor
    interface SafeCallback extends JSObject {
        void call();
    }
}

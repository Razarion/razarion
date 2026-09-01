package com.btxtech.client;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsURLSearchParams;
import com.btxtech.client.jso.JsWindow;
import com.btxtech.client.jso.JsWorker;
import com.btxtech.client.jso.SharedTickBufferReader;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeDecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SharedTickBufferLayout;
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
                                        Provider<Boot> boot,
                                        TerrainUiService terrainUiService,
                                        InventoryUiService inventoryUiService,
                                        UserUiService userUiService,
                                        GameUiControl gameUiControl,
                                        BoxUiService boxUiService,
                                        ResourceUiService resourceUiService,
                                        BaseItemUiService baseItemUiService,
                                        Provider<TeaVMLifecycleService> lifecycleService,
                                        BabylonRendererService babylonRendererService) {
        super(inputServices,
                boot,
                terrainUiService,
                inventoryUiService,
                userUiService,
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
                GameEngineControlPackage controlPackage;
                try {
                    controlPackage = TeaVMClientMarshaller.deMarshall(data);
                } catch (Throwable t) {
                    // The session of 2026-08-31 reported two traps, and only the second carried a
                    // command - because this step runs before there is a command to name.
                    reportEngineErrorToServer("deMarshall: " + t.getMessage());
                    throw t;
                }
                if (controlPackage.getCommand() == GameEngineControlPackage.Command.INITIAL_SLAVE_SYNCHRONIZED
                        || controlPackage.getCommand() == GameEngineControlPackage.Command.INITIAL_SLAVE_SYNCHRONIZED_NO_BASE) {
                    initialSyncComplete = true;
                }
                /*
                 * "wasm trap: dereferencing a null pointer" is what the Meta cohort reported on
                 * 2026-08-31, 449ms after RUN_GAME, and a trap here stops the tick pull loop before
                 * it can ask for the next tick - which is why that session rendered terrain, moved
                 * its camera, and never showed a unit.
                 *
                 * The message alone does not say which handler died, and there is no stack. The
                 * command is the one piece of context that is free to carry, so it is carried.
                 * Rethrown so nothing about the existing behaviour changes: the JS catch above
                 * still logs it, this only adds a name to the report.
                 */
                try {
                    dispatch(controlPackage);
                } catch (Throwable t) {
                    reportEngineErrorToServer("dispatch " + controlPackage.getCommand()
                            + ": " + t.getMessage());
                    /*
                     * Without a SharedArrayBuffer the tick arrives as a request/response pull loop,
                     * and the request for the next one is the last thing onTickUpdate does. A throw
                     * on the way there is therefore not one lost tick but the end of the stream:
                     * units, buildings and bots never update again while the game goes on rendering
                     * terrain and moving its camera, which is what the Meta cohort has been looking
                     * at. One tick nobody can apply must not cost all the following ones.
                     *
                     * The reason is reported above either way, so this cannot quietly paper over a
                     * defect - it only stops that defect from being permanent.
                     */
                    if (controlPackage.getCommand() == GameEngineControlPackage.Command.TICK_UPDATE_RESPONSE) {
                        sendToWorker(GameEngineControlPackage.Command.TICK_UPDATE_REQUEST);
                    }
                    throw t;
                }
            }));
            worker.setOnError(evt -> {
                // The ErrorEvent carries message/filename/lineno and often the Error with its stack.
                // Logging only the bare sentence turned every uncaught exception in the game engine
                // into an anonymous one: 111 of these in a single session on 2026-08-01, none of them
                // diagnosable afterwards.
                String described = describeErrorEvent(evt);
                JsConsole.error("TeaVMClientGameEngineControl: worker error: " + described);
                reportEngineErrorToServer("worker error: " + described);
            });
        } catch (Throwable t) {
            this.deferredStartup.failed(t);
            this.deferredStartup = null;
        }
    }

    private void initSharedTickBuffer() {
        try {
            if (isSharedBufferDisabledByUrl()) {
                JsConsole.log("[CLIENT-WASM] ?nosab in the URL, using postMessage for tick data");
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
    private static final int DOUBLES_PER_ITEM = 16;
    private static final int INTS_PER_ITEM = 4;
    private static final int KILLED_DOUBLES_PER_ITEM = 2;
    private static final int KILLED_INTS_PER_ITEM = 2;

    // Wire-format slot indices (matching worker encoding)
    private static final int TICK_ITEM_COUNT = 1;
    private static final int TICK_RESOURCES = 2;
    private static final int TICK_HOUSE_SPACE = 3;
    private static final int TICK_DOUBLES = 4;
    private static final int TICK_INTS = 5;
    private static final int TICK_FLAGS = 6;
    private static final int TICK_CONTAINING_IDS = 7;
    private static final int TICK_KILLED_COUNT = 8;
    private static final int TICK_KILLED_DOUBLES = 9;
    private static final int TICK_KILLED_INTS = 10;
    private static final int TICK_KILLED_FLAGS = 11;
    private static final int TICK_REMOVE_IDS = 12;
    private static final int TICK_FACTORY_QUEUE_IDS = 13;

    @Override
    protected NativeTickInfo castToNativeTickInfo(Object javaScriptObject) {
        JSObject array = (JSObject) javaScriptObject;
        NativeTickInfo result = new NativeTickInfo();

        int itemCount = jsArrayGetInt(array, TICK_ITEM_COUNT);
        result.resources = jsArrayGetInt(array, TICK_RESOURCES);
        result.houseSpace = jsArrayGetInt(array, TICK_HOUSE_SPACE);

        // An empty tick is a tick with no items, not a tick with no array. Leaving the field null
        // here is what emptied the game without a SharedArrayBuffer: onTickUpdate hands it
        // straight to BaseItemUiService.updateSyncBaseItems, which stores it, and every later
        // read walks over null and traps. A player who has not placed a base yet owns nothing, so
        // the very first tick is the empty one - which is why this only ever showed up in the
        // in-app browsers, and there always.
        result.updatedNativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[0];

        // Decode updated items from TypedArrays
        if (itemCount > 0) {
            JSObject tickDoubles = jsArrayGet(array, TICK_DOUBLES);
            JSObject tickInts = jsArrayGet(array, TICK_INTS);
            JSObject tickFlags = jsArrayGet(array, TICK_FLAGS);
            JSObject containingIds = jsArrayGet(array, TICK_CONTAINING_IDS);
            JSObject factoryQueueIds = jsArrayGet(array, TICK_FACTORY_QUEUE_IDS);

            result.updatedNativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[itemCount];
            int containingOffset = 0;
            int factoryQueueOffset = 0;

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
                double rallyX = getFloat64(tickDoubles, dOff + 14);
                if (!Double.isNaN(rallyX)) {
                    info.factoryRallyPoint = new NativeDecimalPosition();
                    info.factoryRallyPoint.x = rallyX;
                    info.factoryRallyPoint.y = getFloat64(tickDoubles, dOff + 15);
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
                boolean hasFactoryQueue = (flags & 8) != 0;

                // ContainingItemTypeIds (prefix-length encoding)
                if (hasContaining && !jsIsNullOrUndefined(containingIds)) {
                    int count = getInt32(containingIds, containingOffset++);
                    info.containingItemTypeIds = new int[count];
                    for (int c = 0; c < count; c++) {
                        info.containingItemTypeIds[c] = getInt32(containingIds, containingOffset++);
                    }
                }

                // FactoryBuildQueue (prefix-length encoding)
                if (hasFactoryQueue && !jsIsNullOrUndefined(factoryQueueIds)) {
                    int count = getInt32(factoryQueueIds, factoryQueueOffset++);
                    info.factoryBuildQueue = new int[count];
                    for (int c = 0; c < count; c++) {
                        info.factoryBuildQueue[c] = getInt32(factoryQueueIds, factoryQueueOffset++);
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
        boolean hasFactoryQueue = (flags & 8) != 0;

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

        if (hasFactoryQueue) {
            JSObject factoryQueueIds = jsArrayGet(array, TICK_FACTORY_QUEUE_IDS);
            if (!jsIsNullOrUndefined(factoryQueueIds)) {
                int count = getInt32(factoryQueueIds, 0);
                info.factoryBuildQueue = new int[count];
                for (int c = 0; c < count; c++) {
                    info.factoryBuildQueue[c] = getInt32(factoryQueueIds, c + 1);
                }
            }
        }

        return info;
    }

    // ============ JS interop helpers ============

    /**
     * Flattens a worker {@code ErrorEvent} into one line. The stack is capped because this string
     * travels to the server through the remote logger, one POST per occurrence.
     */
    @JSBody(params = {"event"}, script =
            "if (!event) { return 'no event'; }\n" +
            "var msg = event.message || (event.error && event.error.message) || event.type || 'unknown';\n" +
            "var where = event.filename ? (' at ' + event.filename + ':' + event.lineno + ':' + event.colno) : '';\n" +
            "var stack = (event.error && event.error.stack) ? (' stack=' + String(event.error.stack).substring(0, 600)) : '';\n" +
            "return msg + where + stack;")
    private static native String describeErrorEvent(JSObject event);

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

    /**
     * Whether ?nosab in the URL asks for the postMessage tick path.
     * <p>
     * Every browser that gets cross-origin isolation takes the SharedArrayBuffer path, so the
     * postMessage fallback is exercised by nobody in production - except the in-app browsers of
     * Facebook and Instagram. Those run on the Android WebView, which withholds SharedArrayBuffer
     * however the server sets its headers: measured on a Pixel 7 on 2026-08-30 as
     * "wasm=1,wasmgc=1,webgl2=1,coi=0,sab=0". They are also the one cohort that has never placed a
     * single base, and a path only they take is a path that cannot be debugged where they are.
     * <p>
     * With this it can be taken on a desktop, with a console and a debugger:
     * <pre>http://localhost:4200/game?nosab=1</pre>
     * locally and
     * <pre>https://www.razarion.com/game?nosab=1</pre>
     * against production. The dev server answers the root with a redirect to /game that keeps
     * the query string, so the shorter form works too.
     * The worker needs no switch of its own. It is in postMessage mode exactly when the client
     * never hands it a shared buffer, which is what returning here does.
     */
    private static boolean isSharedBufferDisabledByUrl() {
        try {
            String search = JsWindow.getLocationSearch();
            if (search == null || search.isEmpty()) {
                return false;
            }
            return JsURLSearchParams.create(search).has("nosab");
        } catch (Throwable t) {
            // A diagnostic switch is never a reason for a client not to start.
            JsConsole.warn("Failed to read ?nosab from the URL: " + t.getMessage());
            return false;
        }
    }

    // ============ SharedArrayBuffer JS interop ============

    @JSBody(script = "return typeof crossOriginIsolated !== 'undefined' && crossOriginIsolated === true;")
    private static native boolean isCrossOriginIsolated();

    @JSBody(params = {"size"}, script = "try { return new SharedArrayBuffer(size); } catch(e) { return null; }")
    private static native JSObject createSharedArrayBuffer(int size);

    @JSBody(params = {"worker", "sab"}, script = "worker.postMessage({type: 'shared-tick-buffer', buffer: sab});")
    private static native void sendSharedBufferInit(JsWorker worker, JSObject sab);

    /**
     * Reports through the same channel {@link #safeWasmCall} uses, for the failures that are caught
     * in Java rather than trapped in JS. Silently does nothing if the renderer has not installed
     * the reporter yet - which is itself honest: nothing was there to hear it.
     */
    @JSBody(params = {"reason"}, script = "try { if (window.RAZ_engineError) { window.RAZ_engineError(reason); } } catch (ignored) {}")
    private static native void reportEngineErrorToServer(String reason);

    @JSBody(params = {"callback"}, script = "requestAnimationFrame(callback);")
    private static native void requestAnimationFrame(RafCallback callback);

    /**
     * Runs a call that may trap in WASM, and says so where somebody can read it.
     *
     * Every message from the worker is dispatched inside this, so this catch is the roof over the
     * whole engine-to-client path - including the cast of the tick and everything applied from it.
     * A trap here stops the tick pull loop before it can ask for the next one, which leaves a game
     * that renders terrain and moves its camera and never shows a unit again.
     *
     * It used to reach the console and nothing else. On a phone in an in-app browser there is no
     * console, and that is where the Meta cohort lives. RAZ_engineError is installed by the
     * renderer and reports to the server; the guards around it are because a diagnostic must never
     * be the reason a trap turns into a crash.
     */
    @JSBody(params = {"fn"}, script = "try { fn(); } catch(e) { console.error('[WASM trap]', e);"
            + " try { if (window.RAZ_engineError) { window.RAZ_engineError('wasm trap: '"
            + " + ((e && e.message) ? e.message : e)); } } catch (ignored) {} }")
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

package com.btxtech.client.jso.facade;

import com.btxtech.client.jso.JsObject;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.SyncBaseItemMonitor;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a JS proxy for ItemCockpitBridge.
 * Handles cockpit commands (build, fabricate, sell, unload) and state callbacks.
 */
public class JsItemCockpitBridge {
    private static final Logger logger = Logger.getLogger(JsItemCockpitBridge.class.getName());

    private static JSObject cockpitStateCallback;
    private static SyncBaseItemMonitor watchedContainerMonitor;
    private static SyncBaseItemMonitor watchedFactoryMonitor;
    private static JSObject watchedFactoryCallback;

    public static JSObject createProxy(GameEngineControl gameEngineControl,
                                       BaseItemPlacerService baseItemPlacerService,
                                       ItemTypeService itemTypeService,
                                       BaseItemUiService baseItemUiService) {
        JsObject proxy = JsObject.create();

        // requestBuild(builderId: number, itemTypeId: number)
        setIntIntVoid(proxy, "requestBuild", (builderId, itemTypeId) -> {
            try {
                BaseItemType toBeBuild = itemTypeService.getBaseItemType(itemTypeId);
                BaseItemPlacerConfig config = new BaseItemPlacerConfig().baseItemCount(1).baseItemTypeId(itemTypeId);
                baseItemPlacerService.activate(config, true, (decimalPositions, rallyPoint) -> {
                    gameEngineControl.buildCmdIds(builderId, CollectionUtils.getFirst(decimalPositions), itemTypeId, rallyPoint);
                });
            } catch (Throwable t) {
                logger.log(Level.WARNING, "requestBuild failed", t);
            }
        });

        // requestFabricate(factoryIds: number[], itemTypeId: number)
        // Factories queue, so instead of skipping busy ones we pick the factory with the fewest
        // pending units (active + waiting) to load-balance the order across the selection.
        setArrayIntVoid(proxy, "requestFabricate", (jsFactoryIds, itemTypeId) -> {
            try {
                int[] factoryIds = jsArrayToIntArray(jsFactoryIds);
                int bestFactoryId = -1;
                int bestPending = Integer.MAX_VALUE;
                for (int factoryId : factoryIds) {
                    try {
                        SyncBaseItemMonitor monitor = baseItemUiService.monitorSyncItem(factoryId);
                        int pending = monitor.getConstructingBaseItemTypeId() != null ? 1 : 0;
                        int[] queue = monitor.getFactoryBuildQueue();
                        if (queue != null) {
                            pending += queue.length;
                        }
                        monitor.release();
                        if (pending < bestPending) {
                            bestPending = pending;
                            bestFactoryId = factoryId;
                        }
                    } catch (Exception e) {
                        // Skip if monitor not found
                    }
                }
                if (bestFactoryId >= 0) {
                    gameEngineControl.fabricateCmdIds(bestFactoryId, itemTypeId);
                } else if (factoryIds.length > 0) {
                    gameEngineControl.fabricateCmdIds(factoryIds[0], itemTypeId);
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING, "requestFabricate failed", t);
            }
        });

        // requestCancelFactoryQueue(factoryId: number, queueIndex: number)
        // queueIndex is into the waiting queue only (the active unit is not cancelable).
        setIntIntVoid(proxy, "requestCancelFactoryQueue", (factoryId, queueIndex) -> {
            try {
                gameEngineControl.cancelFactoryQueueCmdIds(factoryId, queueIndex);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "requestCancelFactoryQueue failed", t);
            }
        });

        // sellItems(itemIds: number[])
        setArrayVoid(proxy, "sellItems", (jsItemIds) -> {
            try {
                int[] itemIds = jsArrayToIntArray(jsItemIds);
                gameEngineControl.sellItemIds(itemIds);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "sellItems failed", t);
            }
        });

        // surrenderBase()
        setVoid(proxy, "surrenderBase", () -> {
            try {
                gameEngineControl.surrenderBase();
            } catch (Throwable t) {
                logger.log(Level.WARNING, "surrenderBase failed", t);
            }
        });

        // requestUnload(containerId: number)
        setIntVoid(proxy, "requestUnload", (containerId) -> {
            try {
                SyncBaseItemMonitor monitor = baseItemUiService.monitorSyncItem(containerId);
                int[] containingIds = monitor.getSyncBaseItemState().getContainingItemTypeIds();
                monitor.release();
                if (containingIds != null && containingIds.length > 0) {
                    int baseItemTypeId = containingIds[0];
                    BaseItemPlacerConfig config = new BaseItemPlacerConfig().baseItemCount(1).baseItemTypeId(baseItemTypeId);
                    baseItemPlacerService.activate(config, true, (decimalPositions, rallyPoint) -> {
                        gameEngineControl.unloadContainerCmd(containerId, CollectionUtils.getFirst(decimalPositions));
                    });
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING, "requestUnload failed", t);
            }
        });

        // setCockpitStateCallback(callback: () => void)
        setCallbackSetter(proxy, "setCockpitStateCallback", (callback) -> {
            cockpitStateCallback = callback;
        });

        // watchContainerCount(containerId: number, callback: (count: number) => void)
        setIntCallbackVoid(proxy, "watchContainerCount", (containerId, callback) -> {
            try {
                unwatchContainerMonitor();
                watchedContainerMonitor = baseItemUiService.monitorSyncItem(containerId);
                // Send initial count immediately
                int[] initialIds = watchedContainerMonitor.getSyncBaseItemState().getContainingItemTypeIds();
                int initialCount = initialIds != null ? initialIds.length : 0;
                callJsFunctionWithInt(callback, initialCount);
                // Listen for changes
                watchedContainerMonitor.setContainingChangeListener(monitor -> {
                    int[] ids = watchedContainerMonitor.getSyncBaseItemState().getContainingItemTypeIds();
                    int count = ids != null ? ids.length : 0;
                    callJsFunctionWithInt(callback, count);
                });
            } catch (Throwable t) {
                logger.log(Level.WARNING, "watchContainerCount failed", t);
            }
        });

        // unwatchContainerCount()
        setVoid(proxy, "unwatchContainerCount", () -> {
            unwatchContainerMonitor();
        });

        // watchFactoryQueue(factoryId: number, callback: (snapshot: number[]) => void)
        // snapshot = [activeTypeId (0 = idle), progressPermille (0..1000), ...waitingTypeIds]
        setIntCallbackVoid(proxy, "watchFactoryQueue", (factoryId, callback) -> {
            try {
                unwatchFactoryQueueMonitor();
                watchedFactoryMonitor = baseItemUiService.monitorSyncItem(factoryId);
                watchedFactoryCallback = callback;
                pushFactoryQueueSnapshot();
                watchedFactoryMonitor.setFactoryQueueChangeListener(monitor -> pushFactoryQueueSnapshot());
                watchedFactoryMonitor.setConstructingChangeListener(monitor -> pushFactoryQueueSnapshot());
            } catch (Throwable t) {
                logger.log(Level.WARNING, "watchFactoryQueue failed", t);
            }
        });

        // unwatchFactoryQueue()
        setVoid(proxy, "unwatchFactoryQueue", () -> {
            unwatchFactoryQueueMonitor();
        });

        return proxy;
    }

    private static void unwatchContainerMonitor() {
        if (watchedContainerMonitor != null) {
            watchedContainerMonitor.setContainingChangeListener(null);
            watchedContainerMonitor.release();
            watchedContainerMonitor = null;
        }
    }

    private static void unwatchFactoryQueueMonitor() {
        if (watchedFactoryMonitor != null) {
            watchedFactoryMonitor.setFactoryQueueChangeListener(null);
            watchedFactoryMonitor.setConstructingChangeListener(null);
            watchedFactoryMonitor.release();
            watchedFactoryMonitor = null;
        }
        watchedFactoryCallback = null;
    }

    private static void pushFactoryQueueSnapshot() {
        if (watchedFactoryMonitor == null || watchedFactoryCallback == null) {
            return;
        }
        JSObject snapshot = newJsArray();
        Integer activeTypeId = watchedFactoryMonitor.getConstructingBaseItemTypeId();
        double constructing = watchedFactoryMonitor.getConstructing();
        jsArrayPush(snapshot, activeTypeId != null ? activeTypeId : 0);
        // Warmup reports the -1 sentinel via getConstructing(); clamp negatives to 0.
        jsArrayPush(snapshot, constructing > 0 ? (int) Math.round(constructing * 1000) : 0);
        int[] queue = watchedFactoryMonitor.getFactoryBuildQueue();
        if (queue != null) {
            for (int typeId : queue) {
                jsArrayPush(snapshot, typeId);
            }
        }
        callJsFunctionWithArray(watchedFactoryCallback, snapshot);
    }

    /**
     * Called from Java when cockpit state changes (item count, house space, resources).
     */
    public static void notifyCockpitStateChanged() {
        if (cockpitStateCallback != null) {
            callJsFunction(cockpitStateCallback);
        }
    }

    // --- JS array conversion ---

    @JSBody(params = {"jsArray"}, script =
            "var len = jsArray.length; var result = []; for (var i = 0; i < len; i++) { result[i] = jsArray[i] | 0; } return result;")
    private static native int[] jsArrayToIntArray(JSObject jsArray);

    // --- Functor interfaces ---

    @JSFunctor
    interface IntIntVoidCallback extends JSObject {
        void call(int a, int b);
    }

    @JSFunctor
    interface ArrayIntVoidCallback extends JSObject {
        void call(JSObject arr, int value);
    }

    @JSFunctor
    interface ArrayVoidCallback extends JSObject {
        void call(JSObject arr);
    }

    @JSFunctor
    interface IntVoidCallback extends JSObject {
        void call(int value);
    }

    @JSFunctor
    interface CallbackSetterCallback extends JSObject {
        void call(JSObject callback);
    }

    @JSFunctor
    interface IntCallbackVoidCallback extends JSObject {
        void call(int value, JSObject callback);
    }

    @JSFunctor
    interface VoidCallback extends JSObject {
        void call();
    }

    // --- setMethod helpers ---

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setIntIntVoid(JSObject obj, String name, IntIntVoidCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setArrayIntVoid(JSObject obj, String name, ArrayIntVoidCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setArrayVoid(JSObject obj, String name, ArrayVoidCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setIntVoid(JSObject obj, String name, IntVoidCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setCallbackSetter(JSObject obj, String name, CallbackSetterCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setIntCallbackVoid(JSObject obj, String name, IntCallbackVoidCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setVoid(JSObject obj, String name, VoidCallback fn);

    @JSBody(params = {"fn"}, script = "fn();")
    private static native void callJsFunction(JSObject fn);

    @JSBody(params = {"fn", "value"}, script = "fn(value);")
    private static native void callJsFunctionWithInt(JSObject fn, int value);

    @JSBody(params = {}, script = "return [];")
    private static native JSObject newJsArray();

    @JSBody(params = {"arr", "value"}, script = "arr.push(value);")
    private static native void jsArrayPush(JSObject arr, int value);

    @JSBody(params = {"fn", "arr"}, script = "fn(arr);")
    private static native void callJsFunctionWithArray(JSObject fn, JSObject arr);
}

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
                baseItemPlacerService.activate(config, true, decimalPositions -> {
                    gameEngineControl.buildCmdIds(builderId, CollectionUtils.getFirst(decimalPositions), itemTypeId);
                });
            } catch (Throwable t) {
                logger.log(Level.WARNING, "requestBuild failed", t);
            }
        });

        // requestFabricate(factoryIds: number[], itemTypeId: number)
        setArrayIntVoid(proxy, "requestFabricate", (jsFactoryIds, itemTypeId) -> {
            try {
                int[] factoryIds = jsArrayToIntArray(jsFactoryIds);
                // Find first idle factory (one that is not currently constructing)
                for (int factoryId : factoryIds) {
                    try {
                        SyncBaseItemMonitor monitor = baseItemUiService.monitorSyncItem(factoryId);
                        if (monitor.getConstructingBaseItemTypeId() == null) {
                            monitor.release();
                            gameEngineControl.fabricateCmdIds(factoryId, itemTypeId);
                            return;
                        }
                        monitor.release();
                    } catch (Exception e) {
                        // Skip if monitor not found
                    }
                }
                // All busy - use the first one
                if (factoryIds.length > 0) {
                    gameEngineControl.fabricateCmdIds(factoryIds[0], itemTypeId);
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING, "requestFabricate failed", t);
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

        // requestUnload(containerId: number)
        setIntVoid(proxy, "requestUnload", (containerId) -> {
            try {
                SyncBaseItemMonitor monitor = baseItemUiService.monitorSyncItem(containerId);
                int[] containingIds = monitor.getSyncBaseItemState().getContainingItemTypeIds();
                monitor.release();
                if (containingIds != null && containingIds.length > 0) {
                    int baseItemTypeId = containingIds[0];
                    BaseItemPlacerConfig config = new BaseItemPlacerConfig().baseItemCount(1).baseItemTypeId(baseItemTypeId);
                    baseItemPlacerService.activate(config, true, decimalPositions -> {
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

        return proxy;
    }

    private static void unwatchContainerMonitor() {
        if (watchedContainerMonitor != null) {
            watchedContainerMonitor.setContainingChangeListener(null);
            watchedContainerMonitor.release();
            watchedContainerMonitor = null;
        }
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
}

package com.btxtech.client.bridge;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsObject;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.uiservice.Diplomacy;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.client.TeaVMStatusProvider;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Creates JS proxy objects that Angular can call.
 * With WASM-GC, Java objects are not directly callable from JS,
 * so we create plain JS objects with methods that call back into Java via @JSFunctor callbacks.
 *
 * IMPORTANT: Each setMethod overload uses a typed @JSFunctor parameter (not generic JSObject)
 * so that TeaVM WASM-GC properly wraps the lambda as a callable JS function with correct
 * type conversions between JS and WASM.
 */
public class AngularProxyFactory {

    // --- Typed setMethod overloads for each functor type ---
    // Using typed parameters ensures TeaVM wraps the functor as a callable JS function.

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodVoid(JSObject obj, String name, VoidCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodBool(JSObject obj, String name, BooleanCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodIntBool(JSObject obj, String name, IntToBooleanCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethod4D(JSObject obj, String name, VoidWith4DoubleCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethod8D(JSObject obj, String name, VoidWith8DoubleCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodInt(JSObject obj, String name, VoidWithIntCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethod2D(JSObject obj, String name, VoidWith2DoubleCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodRetObj(JSObject obj, String name, ReturnJSObjectCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodIntObj(JSObject obj, String name, IntToJSObjectCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethod2DObj(JSObject obj, String name, TwoDoubleToJSObjectCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodBoolVoid(JSObject obj, String name, VoidWithBooleanCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodObjVoid(JSObject obj, String name, VoidWithJSObjectCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethod4DObj(JSObject obj, String name, FourDoubleToJSObjectCallback fn);

    // Unwraps a single DecimalPosition JS object to 2 doubles, void return
    @JSBody(params = {"obj", "name", "fn"}, script =
            "obj[name] = function(pos) { fn(pos.getX(), pos.getY()); };")
    private static native void setMethodPosVoid(JSObject obj, String name, VoidWith2DoubleCallback fn);

    // Unwraps two DecimalPosition JS objects to 4 doubles, returns JSObject
    @JSBody(params = {"obj", "name", "fn"}, script =
            "obj[name] = function(bl, tr) { return fn(bl.getX(), bl.getY(), tr.getX(), tr.getY()); };")
    private static native void setMethod2PosRetObj(JSObject obj, String name, FourDoubleToJSObjectCallback fn);

    // Reads baseId from a NativeSyncBaseItemTickInfo JS object, returns JSObject
    @JSBody(params = {"obj", "name", "fn"}, script =
            "obj[name] = function(info) { return fn(info.baseId); };")
    private static native void setMethodTickInfoRetObj(JSObject obj, String name, IntToJSObjectCallback fn);

    // --- Functor interfaces for callbacks ---

    @JSFunctor
    public interface VoidCallback extends JSObject {
        void call();
    }

    @JSFunctor
    public interface BooleanCallback extends JSObject {
        boolean call();
    }

    @JSFunctor
    public interface IntToBooleanCallback extends JSObject {
        boolean call(int arg);
    }

    @JSFunctor
    public interface VoidWith4DoubleCallback extends JSObject {
        void call(double a, double b, double c, double d);
    }

    @JSFunctor
    public interface VoidWith8DoubleCallback extends JSObject {
        void call(double a, double b, double c, double d, double e, double f, double g, double h);
    }

    @JSFunctor
    public interface VoidWithIntCallback extends JSObject {
        void call(int arg);
    }

    @JSFunctor
    public interface VoidWith2DoubleCallback extends JSObject {
        void call(double x, double y);
    }

    @JSFunctor
    public interface ReturnJSObjectCallback extends JSObject {
        JSObject call();
    }

    @JSFunctor
    public interface IntToJSObjectCallback extends JSObject {
        JSObject call(int arg);
    }

    @JSFunctor
    public interface TwoDoubleToJSObjectCallback extends JSObject {
        JSObject call(double x, double y);
    }

    @JSFunctor
    public interface FourDoubleToBooleanCallback extends JSObject {
        boolean call(double a, double b, int c, boolean d);
    }

    @JSFunctor
    public interface VoidWithBooleanCallback extends JSObject {
        void call(boolean arg);
    }

    @JSFunctor
    public interface VoidWithJSObjectCallback extends JSObject {
        void call(JSObject arg);
    }

    @JSFunctor
    public interface FourDoubleToJSObjectCallback extends JSObject {
        JSObject call(double a, double b, double c, double d);
    }

    // --- Proxy creation methods ---

    public static JSObject createGameUiControlProxy(GameUiControl ctrl) {
        JsObject proxy = JsObject.create();
        setMethodRetObj(proxy, "getPlanetConfig", () ->
                DtoConverter.convertPlanetConfig(ctrl.getPlanetConfig()));
        setMethodRetObj(proxy, "getColdGameUiContext", () ->
                DtoConverter.convertColdGameUiContext(ctrl.getColdGameUiContext()));
        return proxy;
    }

    public static JSObject createInputServiceProxy(InputService inputService) {
        JsObject proxy = JsObject.create();
        setMethod8D(proxy, "onViewFieldChanged", (blX, blY, brX, brY, trX, trY, tlX, tlY) ->
                inputService.onViewFieldChanged(blX, blY, brX, brY, trX, trY, tlX, tlY));
        setMethodPosVoid(proxy, "terrainClicked", (x, y) ->
                inputService.terrainClicked(new com.btxtech.shared.datatypes.DecimalPosition(x, y)));
        setMethodInt(proxy, "ownItemClicked", inputService::ownItemClicked);
        setMethodInt(proxy, "friendItemClicked", inputService::friendItemClicked);
        setMethodInt(proxy, "enemyItemClicked", inputService::enemyItemClicked);
        setMethodInt(proxy, "resourceItemClicked", inputService::resourceItemClicked);
        setMethodInt(proxy, "boxItemClicked", inputService::boxItemClicked);
        return proxy;
    }

    public static JSObject createSelectionServiceProxy(SelectionService selectionService) {
        JsObject proxy = JsObject.create();
        List<Object[]> listenerPairs = new ArrayList<>();
        setMethodBool(proxy, "hasOwnSelection", selectionService::hasOwnSelection);
        setMethodBool(proxy, "hasOwnMovable", selectionService::hasOwnMovable);
        setMethodBool(proxy, "hasAttackers", selectionService::hasAttackers);
        setMethodIntBool(proxy, "canAttack", selectionService::canAttack);
        setMethodBool(proxy, "hasHarvesters", selectionService::hasHarvesters);
        setMethodIntBool(proxy, "canContain", selectionService::canContain);
        setMethodIntBool(proxy, "canBeFinalizeBuild", selectionService::canBeFinalizeBuild);
        setMethod4D(proxy, "selectRectangle", selectionService::selectRectangle);
        setMethodObjVoid(proxy, "addSelectionListener", listener -> {
            SelectionService.SelectionChangeListener javaListener = () -> callJsFunction(listener);
            listenerPairs.add(new Object[]{listener, javaListener});
            selectionService.addSelectionListener(javaListener);
        });
        setMethodObjVoid(proxy, "removeSelectionListener", listener -> {
            Iterator<Object[]> it = listenerPairs.iterator();
            while (it.hasNext()) {
                Object[] pair = it.next();
                if (jsObjectEquals((JSObject) pair[0], listener)) {
                    selectionService.removeSelectionListener((SelectionService.SelectionChangeListener) pair[1]);
                    it.remove();
                    return;
                }
            }
        });
        return proxy;
    }

    public static JSObject createStatusProviderProxy(TeaVMStatusProvider statusProvider) {
        JsObject proxy = JsObject.create();
        setMethodRetObj(proxy, "getClientAlarms", () ->
                DtoConverter.convertAlarms(statusProvider.getClientAlarms()));
        return proxy;
    }

    public static JSObject createInGameQuestVisualizationServiceProxy(InGameQuestVisualizationService service) {
        JsObject proxy = JsObject.create();
        setMethodBoolVoid(proxy, "setVisible", service::setVisible);
        return proxy;
    }

    public static JSObject createTerrainTypeServiceProxy(TerrainTypeService service) {
        JsObject proxy = JsObject.create();
        setMethodIntObj(proxy, "getTerrainObjectConfig", id ->
                DtoConverter.convertTerrainObjectConfig(service.getTerrainObjectConfig(id)));
        setMethodIntObj(proxy, "getGroundConfig", id ->
                DtoConverter.convertGroundConfig(service.getGroundConfig(id)));
        return proxy;
    }

    public static JSObject createItemTypeServiceProxy(ItemTypeService service) {
        JsObject proxy = JsObject.create();
        setMethodIntObj(proxy, "getResourceItemTypeAngular", id ->
                DtoConverter.convertResourceItemType(service.getResourceItemType(id)));
        setMethodIntObj(proxy, "getBaseItemTypeAngular", id ->
                DtoConverter.convertBaseItemType(service.getBaseItemType(id)));
        return proxy;
    }

    public static JSObject createBaseItemUiServiceProxy(BaseItemUiService service) {
        JsObject proxy = JsObject.create();

        // getBases(): PlayerBaseDto[]
        setMethodRetObj(proxy, "getBases", () -> {
            return DtoConverter.convertPlayerBaseDtoArray(service.getBases());
        });

        // getVisibleNativeSyncBaseItemTickInfos(bottomLeft, topRight): NativeSyncBaseItemTickInfo[]
        setMethod2PosRetObj(proxy, "getVisibleNativeSyncBaseItemTickInfos", (blX, blY, trX, trY) -> {
            DecimalPosition bl = new DecimalPosition(blX, blY);
            DecimalPosition tr = new DecimalPosition(trX, trY);
            NativeSyncBaseItemTickInfo[] infos = service.getVisibleNativeSyncBaseItemTickInfos(bl, tr);
            return DtoConverter.convertNativeSyncBaseItemTickInfos(infos);
        });

        // diplomacy4SyncBaseItem(nativeSyncBaseItemTickInfo): Diplomacy
        setMethodTickInfoRetObj(proxy, "diplomacy4SyncBaseItem", (baseId) -> {
            NativeSyncBaseItemTickInfo tempInfo = new NativeSyncBaseItemTickInfo();
            tempInfo.baseId = baseId;
            Diplomacy diplomacy = service.diplomacy4SyncBaseItem(tempInfo);
            return DtoConverter.toJsString(diplomacy.name());
        });

        // getNearestEnemyPosition(fromX, fromY, enemyItemTypeId, enemyItemTypeIdUsed): Vertex | null
        setMethod4DObj(proxy, "getNearestEnemyPosition", (fromX, fromY, enemyItemTypeId, enemyItemTypeIdUsed) -> {
            com.btxtech.shared.datatypes.Vertex vertex = service.getNearestEnemyPosition(
                fromX, fromY, (int)enemyItemTypeId, enemyItemTypeIdUsed != 0
            );
            return DtoConverter.convertVertex(vertex);
        });

        return proxy;
    }

    public static JSObject createResourceUiServiceProxy(ResourceUiService service) {
        JsObject proxy = JsObject.create();
        setMethod2DObj(proxy, "getNearestResourcePosition", (x, y) ->
                DtoConverter.convertVertex(service.getNearestResourcePosition(x, y)));
        return proxy;
    }

    public static JSObject createInventoryTypeServiceProxy(InventoryTypeService service) {
        JsObject proxy = JsObject.create();
        return proxy;
    }

    public static JSObject createInventoryUiServiceProxy(InventoryUiService service) {
        JsObject proxy = JsObject.create();
        return proxy;
    }

    public static JSObject createTerrainUiServiceProxy(TerrainUiService service) {
        JsObject proxy = JsObject.create();
        setMethod2DObj(proxy, "getTerrainType", (x, y) ->
                DtoConverter.convertTerrainType(service.getTerrainType(
                        new com.btxtech.shared.datatypes.DecimalPosition(x, y))));
        return proxy;
    }

    @JSBody(params = {"fn"}, script = "fn();")
    private static native void callJsFunction(JSObject fn);

    @JSBody(params = {"a", "b"}, script = "return a === b;")
    private static native boolean jsObjectEquals(JSObject a, JSObject b);
}

package com.btxtech.client.bridge;

import com.btxtech.client.jso.JsArray;
import com.btxtech.client.jso.JsObject;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.config.TipConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.terrain.BabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.BotGround;
import com.btxtech.shared.gameengine.planet.terrain.BotGroundSlopeBox;
import com.btxtech.shared.gameengine.planet.terrain.TerrainObjectModel;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.uiservice.cockpit.item.BuildupItemCockpit;
import com.btxtech.uiservice.cockpit.item.ItemContainerCockpit;
import com.btxtech.uiservice.cockpit.item.OtherItemCockpit;
import com.btxtech.uiservice.cockpit.item.OwnItemCockpit;
import com.btxtech.uiservice.cockpit.item.OwnMultipleIteCockpit;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.renderer.MarkerConfig;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Converts Java DTOs to JS proxy objects matching the TypeScript interfaces in GwtAngularFacade.ts
 *
 * IMPORTANT: Each setGetter overload uses a typed @JSFunctor parameter (not generic JSObject)
 * so that TeaVM WASM-GC properly wraps the lambda as a callable JS function with correct
 * type conversions between JS and WASM.
 */
public class DtoConverter {

    // --- Typed setGetter overloads ---

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setGetterInt(JSObject obj, String name, ReturnIntCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setGetterDouble(JSObject obj, String name, ReturnDoubleCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setGetterString(JSObject obj, String name, ReturnStringCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setGetterBool(JSObject obj, String name, ReturnBooleanCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setGetterObj(JSObject obj, String name, ReturnJSObjectCallback fn);

    @JSFunctor
    public interface ReturnIntCallback extends JSObject {
        int call();
    }

    @JSFunctor
    public interface ReturnDoubleCallback extends JSObject {
        double call();
    }

    @JSFunctor
    public interface ReturnStringCallback extends JSObject {
        String call();
    }

    @JSFunctor
    public interface ReturnBooleanCallback extends JSObject {
        boolean call();
    }

    @JSFunctor
    public interface ReturnJSObjectCallback extends JSObject {
        JSObject call();
    }

    @JSFunctor
    public interface VoidWith2DoubleCallback extends JSObject {
        void call(double x, double y);
    }

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethod2D(JSObject obj, String name, VoidWith2DoubleCallback fn);

    // --- Object.defineProperty for mutable data (getters that read current Java field values) ---

    @JSBody(params = {"obj", "name", "fn"}, script = "Object.defineProperty(obj, name, { get: fn, enumerable: true, configurable: true });")
    private static native void definePropertyInt(JSObject obj, String name, ReturnIntCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "Object.defineProperty(obj, name, { get: fn, enumerable: true, configurable: true });")
    private static native void definePropertyBool(JSObject obj, String name, ReturnBooleanCallback fn);

    // --- Void method functors for cockpit callbacks ---

    @JSFunctor
    public interface VoidCallback extends JSObject {
        void call();
    }

    @JSFunctor
    public interface VoidWithJSObjectCallback extends JSObject {
        void call(JSObject arg);
    }

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodVoid(JSObject obj, String name, VoidCallback fn);

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodObjVoid(JSObject obj, String name, VoidWithJSObjectCallback fn);

    @JSBody(params = {"jsRunner", "callback"}, script = "jsRunner.runInAngularZone(callback);")
    private static native void callRunInAngularZone(JSObject jsRunner, VoidCallback callback);

    public static JSObject convertBaseItemPlacer(BaseItemPlacer placer) {
        if (placer == null) return null;
        JsObject obj = JsObject.create();

        setGetterDouble(obj, "getEnemyFreeRadius", () -> placer.getEnemyFreeRadius());
        setMethod2D(obj, "onMove", (x, y) -> placer.onMove(x, y));
        setMethod2D(obj, "onPlace", (x, y) -> placer.onPlace(x, y));
        setGetterBool(obj, "isPositionValid", () -> placer.isPositionValid());
        setGetterString(obj, "getErrorText", () -> {
            String errorText = placer.getErrorText();
            return errorText != null ? errorText : "";
        });
        setGetterObj(obj, "getModel3DId", () -> convertNullableInt(placer.getModel3DId()));

        return obj;
    }

    public static JSObject convertPlanetConfig(PlanetConfig config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        setGetterInt(obj, "getId", config::getId);
        setGetterObj(obj, "getSize", () -> convertDecimalPosition(config.getSize()));
        return obj;
    }

    public static JSObject convertQuestDescriptionConfig(QuestDescriptionConfig<?> config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        setGetterInt(obj, "getId", config::getId);
        setGetterString(obj, "getInternalName", config::getInternalName);
        setGetterInt(obj, "getXp", config::getXp);
        setGetterInt(obj, "getRazarion", config::getRazarion);
        setGetterInt(obj, "getCrystal", config::getCrystal);
        setGetterObj(obj, "getTipConfig", () -> convertTipConfig(config.getTipConfig()));
        return obj;
    }

    public static JSObject convertTipConfig(TipConfig config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        setGetterString(obj, "getTipString", config::getTipString);
        setGetterObj(obj, "getActorItemTypeId", () -> convertNullableInt(config.getActorItemTypeId()));
        return obj;
    }

    public static JSObject convertColdGameUiContext(ColdGameUiContext ctx) {
        if (ctx == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getInGameQuestVisualConfig", () -> {
            if (ctx.getInGameQuestVisualConfig() == null) return null;
            JsObject vc = JsObject.create();
            setGetterObj(vc, "getNodesMaterialId", () ->
                    convertNullableInt(ctx.getInGameQuestVisualConfig().getNodesMaterialId()));
            setGetterObj(vc, "getPlaceNodesMaterialId", () ->
                    convertNullableInt(ctx.getInGameQuestVisualConfig().getPlaceNodesMaterialId()));
            setGetterDouble(vc, "getRadius", () ->
                    ctx.getInGameQuestVisualConfig().getRadius());
            setGetterObj(vc, "getOutOfViewNodesMaterialId", () ->
                    convertNullableInt(ctx.getInGameQuestVisualConfig().getOutOfViewNodesMaterialId()));
            setGetterDouble(vc, "getOutOfViewSize", () ->
                    ctx.getInGameQuestVisualConfig().getOutOfViewSize());
            setGetterDouble(vc, "getOutOfViewDistanceFromCamera", () ->
                    ctx.getInGameQuestVisualConfig().getOutOfViewDistanceFromCamera());
            return vc;
        });
        return obj;
    }

    public static JSObject convertDecimalPosition(DecimalPosition pos) {
        if (pos == null) return null;
        JsObject obj = JsObject.create();
        // Capture the values immediately instead of using method references
        // to avoid TeaVM WASM-GC bridging issues that can result in NaN
        double x = pos.getX();
        double y = pos.getY();
        setGetterDouble(obj, "getX", () -> x);
        setGetterDouble(obj, "getY", () -> y);
        return obj;
    }

    public static JSObject convertVertex(Vertex vertex) {
        if (vertex == null) return null;
        JsObject obj = JsObject.create();
        // Capture the values immediately instead of using method references
        // to avoid TeaVM WASM-GC bridging issues that can result in NaN
        double x = vertex.getX();
        double y = vertex.getY();
        double z = vertex.getZ();
        setGetterDouble(obj, "getX", () -> x);
        setGetterDouble(obj, "getY", () -> y);
        setGetterDouble(obj, "getZ", () -> z);
        return obj;
    }

    public static JSObject convertTerrainType(TerrainType terrainType) {
        if (terrainType == null) return null;
        return toJsString(terrainType.name());
    }

    public static JSObject convertAlarms(Alarm[] alarms) {
        if (alarms == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (Alarm alarm : alarms) {
            arr.push(convertAlarm(alarm));
        }
        return arr;
    }

    public static JSObject convertAlarm(Alarm alarm) {
        if (alarm == null) return null;
        JsObject obj = JsObject.create();
        setGetterString(obj, "angularTypeString", alarm::angularTypeString);
        setGetterDouble(obj, "angularDateAsLong", () -> (double) alarm.angularDateAsLong());
        setGetterString(obj, "getText", alarm::getText);
        setGetterInt(obj, "getId", alarm::getId);
        return obj;
    }

    public static JSObject convertTerrainObjectConfig(Object config) {
        if (config == null) return null;
        TerrainObjectConfig toc = (TerrainObjectConfig) config;
        JsObject obj = JsObject.create();
        setGetterInt(obj, "getId", toc::getId);
        setGetterString(obj, "getInternalName", toc::getInternalName);
        setGetterDouble(obj, "getRadius", toc::getRadius);
        setGetterObj(obj, "getModel3DId", () -> convertNullableInt(toc.getModel3DId()));
        setGetterString(obj, "toString", toc::toString);
        return obj;
    }

    public static JSObject convertGroundConfig(Object config) {
        if (config == null) return null;
        GroundConfig gc = (GroundConfig) config;
        JsObject obj = JsObject.create();
        setGetterInt(obj, "getId", gc::getId);
        setGetterString(obj, "getInternalName", gc::getInternalName);
        setGetterObj(obj, "getGroundBabylonMaterialId", () -> convertNullableInt(gc.getGroundBabylonMaterialId()));
        setGetterObj(obj, "getWaterBabylonMaterialId", () -> convertNullableInt(gc.getWaterBabylonMaterialId()));
        setGetterObj(obj, "getUnderWaterBabylonMaterialId", () -> convertNullableInt(gc.getUnderWaterBabylonMaterialId()));
        setGetterObj(obj, "getBotBabylonMaterialId", () -> convertNullableInt(gc.getBotBabylonMaterialId()));
        setGetterObj(obj, "getBotWallBabylonMaterialId", () -> convertNullableInt(gc.getBotWallBabylonMaterialId()));
        return obj;
    }

    public static JSObject convertBaseItemType(BaseItemType type) {
        if (type == null) return null;
        JsObject obj = JsObject.create();
        setGetterInt(obj, "getId", type::getId);
        setGetterString(obj, "getInternalName", type::getInternalName);
        setGetterString(obj, "getName", () -> type.getName() != null ? type.getName() : "");
        setGetterString(obj, "getDescription", () -> type.getDescription() != null ? type.getDescription() : "");
        setGetterObj(obj, "getModel3DId", () -> convertNullableInt(type.getModel3DId()));
        setGetterObj(obj, "getPhysicalAreaConfig", () -> convertPhysicalAreaConfig(type));
        setGetterObj(obj, "getBuilderType", () -> convertBuilderType(type));
        setGetterObj(obj, "getWeaponType", () -> convertWeaponType(type));
        setGetterObj(obj, "getHarvesterType", () -> convertHarvesterType(type));
        setGetterObj(obj, "getExplosionParticleId", () -> convertNullableInt(type.getExplosionParticleId()));
        return obj;
    }

    public static JSObject convertResourceItemType(ResourceItemType type) {
        if (type == null) return null;
        JsObject obj = JsObject.create();
        setGetterInt(obj, "getId", type::getId);
        setGetterString(obj, "getInternalName", type::getInternalName);
        setGetterString(obj, "getName", () -> type.getName() != null ? type.getName() : "");
        setGetterString(obj, "getDescription", () -> type.getDescription() != null ? type.getDescription() : "");
        setGetterObj(obj, "getModel3DId", () -> convertNullableInt(type.getModel3DId()));
        setGetterDouble(obj, "getRadius", type::getRadius);
        return obj;
    }

    public static JSObject convertBoxItemType(BoxItemType type) {
        if (type == null) return null;
        JsObject obj = JsObject.create();
        setGetterInt(obj, "getId", type::getId);
        setGetterString(obj, "getInternalName", type::getInternalName);
        setGetterString(obj, "getName", () -> type.getName() != null ? type.getName() : "");
        setGetterString(obj, "getDescription", () -> type.getDescription() != null ? type.getDescription() : "");
        setGetterObj(obj, "getModel3DId", () -> convertNullableInt(type.getModel3DId()));
        setGetterDouble(obj, "getRadius", type::getRadius);
        setGetterBool(obj, "isFixVerticalNorm", type::isFixVerticalNorm);
        setGetterString(obj, "getTerrainType", () ->
                type.getTerrainType() != null ? type.getTerrainType().name() : null);
        return obj;
    }

    private static JSObject convertPhysicalAreaConfig(BaseItemType type) {
        if (type.getPhysicalAreaConfig() == null) return null;
        JsObject obj = JsObject.create();
        setGetterDouble(obj, "getRadius", () -> type.getPhysicalAreaConfig().getRadius());
        setGetterString(obj, "getTerrainType", () ->
                type.getPhysicalAreaConfig().getTerrainType() != null ? type.getPhysicalAreaConfig().getTerrainType().name() : null);
        setGetterBool(obj, "fulfilledMovable", () -> type.getPhysicalAreaConfig().fulfilledMovable());
        return obj;
    }

    private static JSObject convertBuilderType(BaseItemType type) {
        if (type.getBuilderType() == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getParticleSystemConfigId", () ->
                convertNullableInt(type.getBuilderType().getParticleSystemConfigId()));
        return obj;
    }

    private static JSObject convertWeaponType(BaseItemType type) {
        if (type.getWeaponType() == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getImpactParticleSystemId", () ->
                convertNullableInt(type.getWeaponType().getImpactParticleSystemId()));
        setGetterObj(obj, "getProjectileSpeed", () ->
                convertNullableDouble(type.getWeaponType().getProjectileSpeed()));
        setGetterObj(obj, "getTrailParticleSystemConfigId", () ->
                convertNullableInt(type.getWeaponType().getTrailParticleSystemConfigId()));
        return obj;
    }

    private static JSObject convertHarvesterType(BaseItemType type) {
        if (type.getHarvesterType() == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getParticleSystemConfigId", () ->
                convertNullableInt(type.getHarvesterType().getParticleSystemConfigId()));
        return obj;
    }

    // ============ TerrainTile conversion ============

    public static JSObject convertTerrainTile(TerrainTile tile) {
        if (tile == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getIndex", () -> convertIndex(tile.getIndex()));
        setGetterInt(obj, "getGroundConfigId", tile::getGroundConfigId);
        setGetterObj(obj, "getGroundHeightMap", () -> convertUint16ArrayEmu(tile.getGroundHeightMap()));
        setGetterObj(obj, "getTerrainTileObjectLists", () -> convertTerrainTileObjectLists(tile.getTerrainTileObjectLists()));
        setGetterObj(obj, "getBabylonDecals", () -> convertBabylonDecals(tile.getBabylonDecals()));
        setGetterObj(obj, "getBotGrounds", () -> convertBotGrounds(tile.getBotGrounds()));
        return obj;
    }

    public static JSObject convertIndex(Index idx) {
        if (idx == null) return null;
        JsObject obj = JsObject.create();
        setGetterInt(obj, "getX", idx::getX);
        setGetterInt(obj, "getY", idx::getY);
        // Also provide toString for debug display
        setGetterString(obj, "toString", idx::toString);
        return obj;
    }

    private static JSObject convertUint16ArrayEmu(Uint16ArrayEmu heightMap) {
        // Pass through - it's already a JS object wrapper
        return (JSObject) heightMap;
    }

    private static JSObject convertTerrainTileObjectLists(TerrainTileObjectList[] lists) {
        if (lists == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (TerrainTileObjectList list : lists) {
            arr.push(convertTerrainTileObjectList(list));
        }
        return arr;
    }

    private static JSObject convertTerrainTileObjectList(TerrainTileObjectList list) {
        if (list == null) return null;
        JsObject obj = JsObject.create();
        obj.set("terrainObjectConfigId", list.getTerrainObjectConfigId());
        obj.set("terrainObjectModels", convertTerrainObjectModels(list.getTerrainObjectModels()));
        return obj;
    }

    private static JSObject convertTerrainObjectModels(TerrainObjectModel[] models) {
        if (models == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (TerrainObjectModel model : models) {
            arr.push(convertTerrainObjectModel(model));
        }
        return arr;
    }

    private static JSObject convertTerrainObjectModel(TerrainObjectModel model) {
        if (model == null) return null;
        JsObject obj = JsObject.create();
        obj.set("terrainObjectId", model.terrainObjectId);
        obj.set("position", convertVertex(model.position));
        obj.set("scale", convertVertex(model.scale));
        obj.set("rotation", convertVertex(model.rotation));
        return obj;
    }

    private static JSObject convertBabylonDecals(BabylonDecal[] decals) {
        if (decals == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (BabylonDecal decal : decals) {
            arr.push(convertBabylonDecal(decal));
        }
        return arr;
    }

    private static JSObject convertBabylonDecal(BabylonDecal decal) {
        if (decal == null) return null;
        JsObject obj = JsObject.create();
        obj.set("babylonMaterialId", decal.babylonMaterialId);
        obj.set("xPos", decal.xPos);
        obj.set("yPos", decal.yPos);
        obj.set("xSize", decal.xSize);
        obj.set("ySize", decal.ySize);
        return obj;
    }

    private static JSObject convertBotGrounds(BotGround[] botGrounds) {
        if (botGrounds == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (BotGround bg : botGrounds) {
            arr.push(convertBotGround(bg));
        }
        return arr;
    }

    private static JSObject convertBotGround(BotGround bg) {
        if (bg == null) return null;
        JsObject obj = JsObject.create();
        obj.set("model3DId", bg.model3DId);
        obj.set("height", bg.height);
        obj.set("positions", convertDecimalPositions(bg.positions));
        obj.set("botGroundSlopeBoxes", convertBotGroundSlopeBoxes(bg.botGroundSlopeBoxes));
        return obj;
    }

    private static JSObject convertDecimalPositions(DecimalPosition[] positions) {
        if (positions == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (DecimalPosition pos : positions) {
            arr.push(convertDecimalPosition(pos));
        }
        return arr;
    }

    private static JSObject convertBotGroundSlopeBoxes(BotGroundSlopeBox[] boxes) {
        if (boxes == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (BotGroundSlopeBox box : boxes) {
            arr.push(convertBotGroundSlopeBox(box));
        }
        return arr;
    }

    private static JSObject convertBotGroundSlopeBox(BotGroundSlopeBox box) {
        if (box == null) return null;
        JsObject obj = JsObject.create();
        obj.set("xPos", box.xPos);
        obj.set("yPos", box.yPos);
        obj.set("height", box.height);
        obj.set("yRot", box.yRot);
        obj.set("zRot", box.zRot);
        return obj;
    }

    @JSBody(params = {"str"}, script = "return str;")
    static native JSObject toJsString(String str);

    @JSBody(params = {"value"}, script = "return value;")
    static native JSObject toJsInt(int value);

    @JSBody(params = {"value"}, script = "return value;")
    static native JSObject toJsDouble(double value);

    static JSObject convertNullableInt(Integer value) {
        if (value == null) return null;
        return toJsInt(value);
    }

    static JSObject convertNullableDouble(Double value) {
        if (value == null) return null;
        return toJsDouble(value);
    }

    public static JSObject convertPlayerBaseDtoArray(com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto[] bases) {
        if (bases == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto base : bases) {
            arr.push(convertPlayerBaseDto(base));
        }
        return arr;
    }

    public static JSObject convertPlayerBaseDto(com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto base) {
        if (base == null) return null;
        JsObject obj = JsObject.create();
        setGetterInt(obj, "getBaseId", () -> base.getBaseId());
        setGetterString(obj, "getName", () -> base.getName());
        setGetterObj(obj, "getCharacter", () -> convertCharacter(base.getCharacter()));
        return obj;
    }

    public static JSObject convertCharacter(com.btxtech.shared.gameengine.datatypes.Character character) {
        if (character == null) return null;
        return toJsString(character.name());
    }

    // ============ NativeSyncBaseItemTickInfo converters ============

    public static JSObject convertNativeSyncBaseItemTickInfos(NativeSyncBaseItemTickInfo[] infos) {
        if (infos == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (NativeSyncBaseItemTickInfo info : infos) {
            arr.push(convertNativeSyncBaseItemTickInfo(info));
        }
        return arr;
    }

    public static JSObject convertNativeSyncBaseItemTickInfo(NativeSyncBaseItemTickInfo info) {
        if (info == null) return null;
        JsObject obj = JsObject.create();
        obj.set("x", info.x);
        obj.set("y", info.y);
        obj.set("baseId", info.baseId);
        return obj;
    }

    // ============ Cockpit DTO converters ============

    public static JSObject convertOwnItemCockpit(OwnItemCockpit cockpit) {
        if (cockpit == null) return null;
        JsObject obj = JsObject.create();
        obj.set("imageUrl", cockpit.imageUrl != null ? cockpit.imageUrl : "");
        obj.set("itemTypeName", cockpit.itemTypeName != null ? cockpit.itemTypeName : "");
        obj.set("itemTypeDescr", cockpit.itemTypeDescr != null ? cockpit.itemTypeDescr : "");
        obj.set("buildupItemInfos", convertBuildupItemCockpits(cockpit.buildupItemInfos));
        obj.set("itemContainerInfo", convertItemContainerCockpit(cockpit.itemContainerInfo));
        if (cockpit.sellHandler != null) {
            setMethodVoid(obj, "sellHandler", () -> cockpit.sellHandler.onSell());
        }
        return obj;
    }

    public static JSObject convertBuildupItemCockpits(BuildupItemCockpit[] cockpits) {
        if (cockpits == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (BuildupItemCockpit cockpit : cockpits) {
            arr.push(convertBuildupItemCockpit(cockpit));
        }
        return arr;
    }

    public static JSObject convertBuildupItemCockpit(BuildupItemCockpit cockpit) {
        if (cockpit == null) return null;
        JsObject obj = JsObject.create();
        // Static properties
        obj.set("imageUrl", cockpit.imageUrl != null ? cockpit.imageUrl : "");
        obj.set("itemTypeId", cockpit.itemTypeId);
        obj.set("itemTypeName", cockpit.itemTypeName != null ? cockpit.itemTypeName : "");
        obj.set("price", cockpit.price);
        // Mutable properties (Object.defineProperty getters read current Java field values)
        definePropertyInt(obj, "itemCount", () -> cockpit.itemCount);
        definePropertyInt(obj, "itemLimit", () -> cockpit.itemLimit);
        definePropertyBool(obj, "enabled", () -> cockpit.enabled);
        definePropertyBool(obj, "buildLimitReached", () -> cockpit.buildLimitReached);
        definePropertyBool(obj, "buildHouseSpaceReached", () -> cockpit.buildHouseSpaceReached);
        definePropertyBool(obj, "buildNoMoney", () -> cockpit.buildNoMoney);
        obj.setNull("progress");
        // Callbacks
        setMethodVoid(obj, "onBuild", () -> cockpit.onBuild());
        setMethodObjVoid(obj, "setAngularZoneRunner", jsRunner ->
                cockpit.setAngularZoneRunner(callback ->
                        callRunInAngularZone(jsRunner, () -> callback.callback())
                )
        );
        return obj;
    }

    public static JSObject convertItemContainerCockpit(ItemContainerCockpit cockpit) {
        if (cockpit == null) return null;
        JsObject obj = JsObject.create();
        definePropertyInt(obj, "count", () -> cockpit.count);
        setMethodVoid(obj, "onUnload", () -> cockpit.onUnload());
        setMethodObjVoid(obj, "setAngularZoneRunner", jsRunner ->
                cockpit.setAngularZoneRunner(callback ->
                        callRunInAngularZone(jsRunner, () -> callback.callback())
                )
        );
        return obj;
    }

    public static JSObject convertOwnMultipleIteCockpits(OwnMultipleIteCockpit[] cockpits) {
        if (cockpits == null) return null;
        JsArray<JSObject> arr = JsArray.create();
        for (OwnMultipleIteCockpit cockpit : cockpits) {
            arr.push(convertOwnMultipleIteCockpit(cockpit));
        }
        return arr;
    }

    public static JSObject convertOwnMultipleIteCockpit(OwnMultipleIteCockpit cockpit) {
        if (cockpit == null) return null;
        JsObject obj = JsObject.create();
        obj.set("ownItemCockpit", convertOwnItemCockpit(cockpit.ownItemCockpit));
        obj.set("count", cockpit.count);
        setMethodVoid(obj, "onSelect", () -> cockpit.onSelect());
        return obj;
    }

    public static JSObject convertOtherItemCockpit(OtherItemCockpit cockpit) {
        if (cockpit == null) return null;
        JsObject obj = JsObject.create();
        obj.set("id", cockpit.id);
        obj.set("imageUrl", cockpit.imageUrl != null ? cockpit.imageUrl : "");
        obj.set("itemTypeName", cockpit.itemTypeName != null ? cockpit.itemTypeName : "");
        obj.set("itemTypeDescr", cockpit.itemTypeDescr != null ? cockpit.itemTypeDescr : "");
        if (cockpit.baseId != null) {
            obj.set("baseId", cockpit.baseId);
        }
        obj.set("baseName", cockpit.baseName != null ? cockpit.baseName : "");
        obj.set("friend", cockpit.friend);
        obj.set("bot", cockpit.bot);
        obj.set("resource", cockpit.resource);
        obj.set("box", cockpit.box);
        return obj;
    }
}

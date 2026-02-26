package com.btxtech.client.bridge;

import com.btxtech.client.jso.JsArray;
import com.btxtech.client.jso.JsObject;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.config.TipConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.itemtype.AudioItemConfig;
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

    // --- Method functor: int -> bool (for type-check methods) ---

    @JSFunctor
    public interface IntToBoolCallback extends JSObject {
        boolean call(int value);
    }

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodIntBool(JSObject obj, String name, IntToBoolCallback fn);

    // --- Method functor: int -> int (for query methods returning int) ---

    @JSFunctor
    public interface IntToIntCallback extends JSObject {
        int call(int value);
    }

    @JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
    private static native void setMethodIntReturn(JSObject obj, String name, IntToIntCallback fn);

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
        setGetterObj(obj, "getSpawnAudioId", () -> convertNullableInt(placer.getSpawnAudioId()));
        setGetterBool(obj, "isPlayBuildSound", () -> placer.isPlayBuildSound());
        setGetterBool(obj, "isCanBeCanceled", () -> placer.isCanBeCanceled());
        setMethodVoid(obj, "cancel", placer::cancel);

        return obj;
    }

    public static JSObject convertPlanetConfig(PlanetConfig config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        setGetterInt(obj, "getId", config::getId);
        setGetterObj(obj, "getSize", () -> convertDecimalPosition(config.getSize()));
        setGetterInt(obj, "getHouseSpace", config::getHouseSpace);
        setMethodIntReturn(obj, "imitation4ItemType", config::imitation4ItemType);
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
        if (config instanceof QuestConfig) {
            QuestConfig questConfig = (QuestConfig) config;
            setGetterObj(obj, "getConditionConfig", () -> convertConditionConfig(questConfig.getConditionConfig()));
        }
        return obj;
    }

    public static JSObject convertConditionConfig(ConditionConfig config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        setGetterString(obj, "getConditionTrigger", () ->
                config.getConditionTrigger() != null ? config.getConditionTrigger().name() : null);
        setGetterObj(obj, "getComparisonConfig", () -> convertComparisonConfig(config.getComparisonConfig()));
        return obj;
    }

    public static JSObject convertComparisonConfig(ComparisonConfig config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getCount", () -> convertNullableInt(config.getCount()));
        setGetterObj(obj, "getTimeSeconds", () -> convertNullableInt(config.getTimeSeconds()));
        setGetterObj(obj, "getPlaceConfig", () -> convertPlaceConfig(config.getPlaceConfig()));
        setGetterObj(obj, "toTypeCountAngular", () -> {
            Integer[][] typeCount = config.toTypeCountAngular();
            if (typeCount == null) return null;
            JsArray<JSObject> arr = JsArray.create();
            for (Integer[] entry : typeCount) {
                JsArray<JSObject> pair = JsArray.create();
                pair.push((int) entry[0]);
                pair.push((int) entry[1]);
                arr.push(pair);
            }
            return arr;
        });
        return obj;
    }

    public static JSObject convertQuestProgressInfo(QuestProgressInfo info) {
        if (info == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getCount", () -> convertNullableInt(info.getCount()));
        setGetterObj(obj, "getSecondsRemaining", () -> convertNullableInt(info.getSecondsRemaining()));
        setGetterString(obj, "getBotBasesInformation", info::getBotBasesInformation);
        setGetterObj(obj, "toTypeCountAngular", () -> {
            Integer[][] typeCount = info.toTypeCountAngular();
            if (typeCount == null) return null;
            JsArray<JSObject> arr = JsArray.create();
            for (Integer[] entry : typeCount) {
                JsArray<JSObject> pair = JsArray.create();
                pair.push((int) entry[0]);
                pair.push((int) entry[1]);
                arr.push(pair);
            }
            return arr;
        });
        return obj;
    }

    public static JSObject convertPlaceConfig(PlaceConfig config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getPosition", () -> convertDecimalPosition(config.getPosition()));
        setGetterDouble(obj, "toRadiusAngular", () -> config.toRadiusAngular());
        setGetterObj(obj, "getPolygon2D", () -> {
            if (config.getPolygon2D() == null) return null;
            JsObject polyObj = JsObject.create();
            setGetterObj(polyObj, "toCornersAngular", () -> {
                DecimalPosition[] corners = config.getPolygon2D().toCornersAngular();
                JsArray<JSObject> arr = JsArray.create();
                for (DecimalPosition corner : corners) {
                    arr.push(convertDecimalPosition(corner));
                }
                return arr;
            });
            return polyObj;
        });
        return obj;
    }

    public static JSObject convertTipConfig(TipConfig config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        setGetterString(obj, "getTipString", config::getTipString);
        setGetterObj(obj, "getActorItemTypeId", () -> convertNullableInt(config.getActorItemTypeId()));
        return obj;
    }

    public static JSObject convertMarkerConfig(MarkerConfig config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        obj.set("radius", config.radius);
        obj.setNullableInt("nodesMaterialId", config.nodesMaterialId);
        obj.setNullableInt("placeNodesMaterialId", config.placeNodesMaterialId);
        obj.setNullableInt("outOfViewNodesMaterialId", config.outOfViewNodesMaterialId);
        obj.set("outOfViewSize", config.outOfViewSize);
        obj.set("outOfViewDistanceFromCamera", config.outOfViewDistanceFromCamera);
        return obj;
    }

    public static JSObject convertAudioConfig(AudioConfig config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getTerrainLoopWater", () -> convertNullableInt(config.getTerrainLoopWater()));
        setGetterObj(obj, "getTerrainLoopLand", () -> convertNullableInt(config.getTerrainLoopLand()));
        setGetterObj(obj, "getOnQuestActivated", () -> convertNullableInt(config.getOnQuestActivated()));
        return obj;
    }

    public static JSObject convertColdGameUiContext(ColdGameUiContext ctx) {
        if (ctx == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getAudioConfig", () -> convertAudioConfig(ctx.getAudioConfig()));
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
        setVertexDistanceMethod(obj, x, y, z);
        return obj;
    }

    @JSBody(params = {"obj", "x", "y", "z"}, script =
            "obj.distance = function(v) { return Math.sqrt(Math.pow(x - v.getX(), 2) + Math.pow(y - v.getY(), 2) + Math.pow(z - v.getZ(), 2)); };")
    private static native void setVertexDistanceMethod(JSObject obj, double x, double y, double z);

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
        setGetterObj(obj, "getExplosionAudioItemConfigId", () -> convertNullableInt(type.getExplosionAudioItemConfigId()));
        setGetterObj(obj, "getItemContainerType", () -> convertItemContainerType(type));
        setGetterObj(obj, "getFactoryType", () -> convertFactoryType(type));
        setGetterObj(obj, "getThumbnail", () -> convertNullableInt(type.getThumbnail()));
        setGetterObj(obj, "getSpawnAudioId", () -> convertNullableInt(type.getSpawnAudioId()));
        setGetterInt(obj, "getPrice", type::getPrice);
        setGetterInt(obj, "getConsumingHouseSpace", type::getConsumingHouseSpace);
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
        setGetterObj(obj, "getThumbnail", () -> convertNullableInt(type.getThumbnail()));
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
        setGetterObj(obj, "getThumbnail", () -> convertNullableInt(type.getThumbnail()));
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
        setMethodIntBool(obj, "checkAbleToBuild", (itemTypeId) ->
                type.getBuilderType().checkAbleToBuild(itemTypeId));
        setGetterObj(obj, "getAbleToBuildIds", () ->
                convertIntegerList(type.getBuilderType().getAbleToBuildIds()));
        return obj;
    }

    private static JSObject convertAudioItemConfig(AudioItemConfig config) {
        if (config == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getAudioId", () -> convertNullableInt(config.getAudioId()));
        setGetterInt(obj, "getPitchCentsMin", config::getPitchCentsMin);
        setGetterInt(obj, "getPitchCentsMax", config::getPitchCentsMax);
        setGetterDouble(obj, "getVolumeMin", config::getVolumeMin);
        setGetterDouble(obj, "getVolumeMax", config::getVolumeMax);
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
        setGetterObj(obj, "getMuzzleFlashAudioConfig", () ->
                convertAudioItemConfig(type.getWeaponType().getMuzzleFlashAudioConfig()));
        setGetterObj(obj, "getImpactAudioConfig", () ->
                convertAudioItemConfig(type.getWeaponType().getImpactAudioConfig()));
        setMethodIntBool(obj, "checkItemTypeDisallowed", (targetItemTypeId) ->
                type.getWeaponType().checkItemTypeDisallowed(targetItemTypeId));
        return obj;
    }

    private static JSObject convertHarvesterType(BaseItemType type) {
        if (type.getHarvesterType() == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getParticleSystemConfigId", () ->
                convertNullableInt(type.getHarvesterType().getParticleSystemConfigId()));
        return obj;
    }

    private static JSObject convertItemContainerType(BaseItemType type) {
        if (type.getItemContainerType() == null) return null;
        JsObject obj = JsObject.create();
        setMethodIntBool(obj, "isAbleToContain", (itemTypeId) ->
                type.getItemContainerType().isAbleToContain(itemTypeId));
        return obj;
    }

    private static JSObject convertFactoryType(BaseItemType type) {
        if (type.getFactoryType() == null) return null;
        JsObject obj = JsObject.create();
        setGetterObj(obj, "getAbleToBuildIds", () ->
                convertIntegerList(type.getFactoryType().getAbleToBuildIds()));
        return obj;
    }

    private static JSObject convertIntegerList(java.util.List<Integer> list) {
        if (list == null) return null;
        JsArray arr = JsArray.create();
        for (Integer val : list) {
            arr.push(val.intValue());
        }
        return arr;
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
}

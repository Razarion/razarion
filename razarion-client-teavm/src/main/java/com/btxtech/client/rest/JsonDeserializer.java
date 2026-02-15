package com.btxtech.client.rest;

import com.btxtech.client.jso.JsArray;
import com.btxtech.client.jso.JsObject;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.InGameQuestVisualConfig;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.ScrollUiQuest;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.dto.SlaveQuestInfo;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.config.TipConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.GeneratorType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HouseType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.SpecialType;
import com.btxtech.shared.gameengine.datatypes.itemtype.AudioItemConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.terrain.BotGroundSlopeBox;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class JsonDeserializer {

    private JsonDeserializer() {
    }

    // --- JS interop helpers ---

    @JSBody(params = {"arr", "index"}, script = "return arr[index];")
    private static native int jsArrayGetInt(JSObject arr, int index);

    @JSBody(params = {"arr"}, script = "return arr.length;")
    private static native int jsArrayLength(JSObject arr);

    @JSBody(params = {"arr", "index"}, script = "return arr[index];")
    private static native JSObject jsArrayGet(JSObject arr, int index);

    // --- Helper methods ---

    private static JsObject obj(JsObject parent, String key) {
        if (parent.isNullOrUndefined(key)) {
            return null;
        }
        return JsObject.cast(parent.get(key));
    }

    private static <T> List<T> list(JsObject parent, String key, Function<JsObject, T> deserializer) {
        if (parent.isNullOrUndefined(key)) {
            return null;
        }
        JSObject arr = parent.get(key);
        int len = jsArrayLength(arr);
        List<T> result = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            result.add(deserializer.apply(JsObject.cast(jsArrayGet(arr, i))));
        }
        return result;
    }

    private static List<Integer> intList(JsObject parent, String key) {
        if (parent.isNullOrUndefined(key)) {
            return null;
        }
        JSObject arr = parent.get(key);
        int len = jsArrayLength(arr);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            result.add(jsArrayGetInt(arr, i));
        }
        return result;
    }

    private static List<DecimalPosition> decimalPositionList(JsObject parent, String key) {
        return list(parent, key, JsonDeserializer::deserializeDecimalPosition);
    }

    private static Map<Integer, Integer> intIntMap(JsObject parent, String key) {
        if (parent.isNullOrUndefined(key)) {
            return null;
        }
        JSObject mapObj = parent.get(key);
        JsArray<JSObject> keys = JsObject.getKeys(mapObj);
        Map<Integer, Integer> result = new HashMap<>();
        JsObject mapJsObj = JsObject.cast(mapObj);
        for (int i = 0; i < keys.getLength(); i++) {
            String k = JsObject.jsToString(keys.get(i));
            int value = mapJsObj.getInt(k);
            result.put(Integer.parseInt(k), value);
        }
        return result;
    }

    private static <E extends Enum<E>> E deserializeEnum(JsObject parent, String key, Class<E> enumClass) {
        if (parent.isNullOrUndefined(key)) {
            return null;
        }
        String name = parent.getString(key);
        if (name == null) {
            return null;
        }
        E[] constants = enumClass.getEnumConstants();
        for (E constant : constants) {
            if (constant.name().equals(name)) {
                return constant;
            }
        }
        return null;
    }

    // --- Top-level ---

    public static ColdGameUiContext deserializeColdGameUiContext(JsObject json) {
        if (json == null) return null;
        ColdGameUiContext r = new ColdGameUiContext();
        r.setUserContext(deserializeUserContext(obj(json, "userContext")));
        r.setStaticGameConfig(deserializeStaticGameConfig(obj(json, "staticGameConfig")));
        r.setAudioConfig(deserializeAudioConfig(obj(json, "audioConfig")));
        r.setInGameQuestVisualConfig(deserializeInGameQuestVisualConfig(obj(json, "inGameQuestVisualConfig")));
        r.setWarmGameUiContext(deserializeWarmGameUiContext(obj(json, "warmGameUiContext")));
        return r;
    }

    public static UserContext deserializeUserContext(JsObject json) {
        if (json == null) return null;
        UserContext r = new UserContext();
        r.setUserId(json.isNullOrUndefined("userId") ? null : json.getString("userId"));
        r.setRegisterState(deserializeEnum(json, "registerState", UserContext.RegisterState.class));
        r.setName(json.isNullOrUndefined("name") ? null : json.getString("name"));
        r.setLevelId(json.getNullableInt("levelId"));
        r.setUnlockedItemLimit(intIntMap(json, "unlockedItemLimit"));
        r.setXp(json.getInt("xp"));
        return r;
    }

    public static StaticGameConfig deserializeStaticGameConfig(JsObject json) {
        if (json == null) return null;
        StaticGameConfig r = new StaticGameConfig();
        r.setGroundConfigs(list(json, "groundConfigs", JsonDeserializer::deserializeGroundConfig));
        r.setTerrainObjectConfigs(list(json, "terrainObjectConfigs", JsonDeserializer::deserializeTerrainObjectConfig));
        r.setBaseItemTypes(list(json, "baseItemTypes", JsonDeserializer::deserializeBaseItemType));
        r.setResourceItemTypes(list(json, "resourceItemTypes", JsonDeserializer::deserializeResourceItemType));
        r.setBoxItemTypes(list(json, "boxItemTypes", JsonDeserializer::deserializeBoxItemType));
        r.setLevelConfigs(list(json, "levelConfigs", JsonDeserializer::deserializeLevelConfig));
        r.setInventoryItems(list(json, "inventoryItems", JsonDeserializer::deserializeInventoryItem));
        return r;
    }

    public static AudioConfig deserializeAudioConfig(JsObject json) {
        if (json == null) return null;
        AudioConfig r = new AudioConfig();
        r.setDialogOpened(json.getNullableInt("dialogOpened"));
        r.setDialogClosed(json.getNullableInt("dialogClosed"));
        r.setOnQuestActivated(json.getNullableInt("onQuestActivated"));
        r.setOnQuestPassed(json.getNullableInt("onQuestPassed"));
        r.setOnLevelUp(json.getNullableInt("onLevelUp"));
        r.setOnBoxPicked(json.getNullableInt("onBoxPicked"));
        r.setOnBaseLost(json.getNullableInt("onBaseLost"));
        r.setTerrainLoopWater(json.getNullableInt("terrainLoopWater"));
        r.setTerrainLoopLand(json.getNullableInt("terrainLoopLand"));
        return r;
    }

    public static InGameQuestVisualConfig deserializeInGameQuestVisualConfig(JsObject json) {
        if (json == null) return null;
        InGameQuestVisualConfig r = new InGameQuestVisualConfig();
        r.setNodesMaterialId(json.getNullableInt("nodesMaterialId"));
        r.setPlaceNodesMaterialId(json.getNullableInt("placeNodesMaterialId"));
        r.setRadius(json.getDouble("radius"));
        r.setOutOfViewNodesMaterialId(json.getNullableInt("outOfViewNodesMaterialId"));
        r.setOutOfViewSize(json.getDouble("outOfViewSize"));
        r.setOutOfViewDistanceFromCamera(json.getDouble("outOfViewDistanceFromCamera"));
        r.setHarvestColor(deserializeColor(obj(json, "harvestColor")));
        r.setAttackColor(deserializeColor(obj(json, "attackColor")));
        r.setPickColor(deserializeColor(obj(json, "pickColor")));
        return r;
    }

    public static WarmGameUiContext deserializeWarmGameUiContext(JsObject json) {
        if (json == null) return null;
        WarmGameUiContext r = new WarmGameUiContext();
        r.setGameUiControlConfigId(json.getInt("gameUiControlConfigId"));
        r.setGameEngineMode(deserializeEnum(json, "gameEngineMode", GameEngineMode.class));
        r.setAvailableUnlocks(json.getBoolean("availableUnlocks"));
        r.setSlavePlanetConfig(deserializeSlavePlanetConfig(obj(json, "slavePlanetConfig")));
        r.setSlaveQuestInfo(deserializeSlaveQuestInfo(obj(json, "slaveQuestInfo")));
        r.setPlanetConfig(deserializePlanetConfig(obj(json, "planetConfig")));
        r.setSceneConfigs(list(json, "sceneConfigs", JsonDeserializer::deserializeSceneConfig));
        return r;
    }

    // --- Shared primitives ---

    public static DecimalPosition deserializeDecimalPosition(JsObject json) {
        if (json == null) return null;
        return new DecimalPosition(json.getDouble("x"), json.getDouble("y"));
    }

    public static Color deserializeColor(JsObject json) {
        if (json == null) return null;
        return new Color(json.getDouble("r"), json.getDouble("g"), json.getDouble("b"), json.getDouble("a"));
    }

    public static I18nString deserializeI18nString(JsObject json) {
        if (json == null) return null;
        I18nString r = new I18nString();
        r.setString(json.isNullOrUndefined("string") ? null : json.getString("string"));
        return r;
    }

    public static Polygon2D deserializePolygon2D(JsObject json) {
        if (json == null) return null;
        List<DecimalPosition> corners = decimalPositionList(json, "corners");
        if (corners == null || corners.isEmpty()) {
            return null;
        }
        return new Polygon2D(corners);
    }

    public static Rectangle2D deserializeRectangle2D(JsObject json) {
        if (json == null) return null;
        DecimalPosition start = deserializeDecimalPosition(obj(json, "start"));
        DecimalPosition end = deserializeDecimalPosition(obj(json, "end"));
        if (start == null || end == null) return null;
        return new Rectangle2D(start, end);
    }

    // --- Config types ---

    public static GroundConfig deserializeGroundConfig(JsObject json) {
        if (json == null) return null;
        GroundConfig r = new GroundConfig();
        r.setId(json.getInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setGroundBabylonMaterialId(json.getNullableInt("groundBabylonMaterialId"));
        r.setWaterBabylonMaterialId(json.getNullableInt("waterBabylonMaterialId"));
        r.setUnderWaterBabylonMaterialId(json.getNullableInt("underWaterBabylonMaterialId"));
        r.setBotBabylonMaterialId(json.getNullableInt("botBabylonMaterialId"));
        r.setBotWallBabylonMaterialId(json.getNullableInt("botWallBabylonMaterialId"));
        return r;
    }

    public static TerrainObjectConfig deserializeTerrainObjectConfig(JsObject json) {
        if (json == null) return null;
        TerrainObjectConfig r = new TerrainObjectConfig();
        r.setId(json.getInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setRadius(json.getDouble("radius"));
        r.setModel3DId(json.getNullableInt("model3DId"));
        return r;
    }

    public static PlanetConfig deserializePlanetConfig(JsObject json) {
        if (json == null) return null;
        PlanetConfig r = new PlanetConfig();
        r.setId(json.getInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setSize(deserializeDecimalPosition(obj(json, "size")));
        r.setItemTypeLimitation(intIntMap(json, "itemTypeLimitation"));
        r.setHouseSpace(json.getInt("houseSpace"));
        r.setStartRazarion(json.getInt("startRazarion"));
        r.setStartBaseItemTypeId(json.getNullableInt("startBaseItemTypeId"));
        r.setGroundConfigId(json.getNullableInt("groundConfigId"));
        return r;
    }

    public static LevelConfig deserializeLevelConfig(JsObject json) {
        if (json == null) return null;
        LevelConfig r = new LevelConfig();
        r.setId(json.getInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setNumber(json.getInt("number"));
        r.setItemTypeLimitation(intIntMap(json, "itemTypeLimitation"));
        r.setXp2LevelUp(json.getInt("xp2LevelUp"));
        r.setLevelUnlockConfigs(list(json, "levelUnlockConfigs", JsonDeserializer::deserializeLevelUnlockConfig));
        return r;
    }

    public static LevelUnlockConfig deserializeLevelUnlockConfig(JsObject json) {
        if (json == null) return null;
        LevelUnlockConfig r = new LevelUnlockConfig();
        r.setId(json.getNullableInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setThumbnail(json.getNullableInt("thumbnail"));
        r.setI18nName(deserializeI18nString(obj(json, "i18nName")));
        r.setI18nDescription(deserializeI18nString(obj(json, "i18nDescription")));
        r.setBaseItemType(json.getNullableInt("baseItemType"));
        r.setBaseItemTypeCount(json.getInt("baseItemTypeCount"));
        r.setCrystalCost(json.getInt("crystalCost"));
        return r;
    }

    public static InventoryItem deserializeInventoryItem(JsObject json) {
        if (json == null) return null;
        InventoryItem r = new InventoryItem();
        r.setId(json.getInt("id"));
        r.setI18nName(deserializeI18nString(obj(json, "i18nName")));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setRazarion(json.getNullableInt("razarion"));
        r.setBaseItemTypeId(json.getNullableInt("baseItemTypeId"));
        r.setBaseItemTypeCount(json.getInt("baseItemTypeCount"));
        r.setBaseItemTypeFreeRange(json.getDouble("baseItemTypeFreeRange"));
        r.setImageId(json.getNullableInt("imageId"));
        r.setCrystalCost(json.getNullableInt("crystalCost"));
        return r;
    }

    // --- Item types ---

    private static void deserializeItemTypeFields(JsObject json, BaseItemType r) {
        r.setId(json.getInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setName(json.isNullOrUndefined("name") ? null : json.getString("name"));
        r.setDescription(json.isNullOrUndefined("description") ? null : json.getString("description"));
        r.setModel3DId(json.getNullableInt("model3DId"));
        r.setThumbnail(json.getNullableInt("thumbnail"));
    }

    private static void deserializeItemTypeFieldsResource(JsObject json, ResourceItemType r) {
        r.setId(json.getInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setName(json.isNullOrUndefined("name") ? null : json.getString("name"));
        r.setDescription(json.isNullOrUndefined("description") ? null : json.getString("description"));
        r.setModel3DId(json.getNullableInt("model3DId"));
        r.setThumbnail(json.getNullableInt("thumbnail"));
    }

    private static void deserializeItemTypeFieldsBox(JsObject json, BoxItemType r) {
        r.setId(json.getInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setName(json.isNullOrUndefined("name") ? null : json.getString("name"));
        r.setDescription(json.isNullOrUndefined("description") ? null : json.getString("description"));
        r.setModel3DId(json.getNullableInt("model3DId"));
        r.setThumbnail(json.getNullableInt("thumbnail"));
    }

    public static BaseItemType deserializeBaseItemType(JsObject json) {
        if (json == null) return null;
        BaseItemType r = new BaseItemType();
        deserializeItemTypeFields(json, r);
        r.setPhysicalAreaConfig(deserializePhysicalAreaConfig(obj(json, "physicalAreaConfig")));
        r.setHealth(json.getInt("health"));
        r.setPrice(json.getInt("price"));
        r.setBuildup(json.getInt("buildup"));
        r.setXpOnKilling(json.getInt("xpOnKilling"));
        r.setConsumingHouseSpace(json.getInt("consumingHouseSpace"));
        r.setWeaponType(deserializeWeaponType(obj(json, "weaponType")));
        r.setFactoryType(deserializeFactoryType(obj(json, "factoryType")));
        r.setHarvesterType(deserializeHarvesterType(obj(json, "harvesterType")));
        r.setBuilderType(deserializeBuilderType(obj(json, "builderType")));
        r.setGeneratorType(deserializeGeneratorType(obj(json, "generatorType")));
        r.setConsumerType(deserializeConsumerType(obj(json, "consumerType")));
        r.setItemContainerType(deserializeItemContainerType(obj(json, "itemContainerType")));
        r.setHouseType(deserializeHouseType(obj(json, "houseType")));
        r.setSpecialType(deserializeSpecialType(obj(json, "specialType")));
        r.setDropBoxItemTypeId(json.getNullableInt("dropBoxItemTypeId"));
        r.setDropBoxPossibility(json.getDouble("dropBoxPossibility"));
        r.setBoxPickupRange(json.getDouble("boxPickupRange"));
        r.setUnlockCrystals(json.getNullableInt("unlockCrystals"));
        r.setSpawnDurationMillis(json.getInt("spawnDurationMillis"));
        r.setSpawnParticleSystemId(json.getNullableInt("spawnParticleSystemId"));
        r.setSpawnAudioId(json.getNullableInt("spawnAudioId"));
        r.setDemolitionStepEffects(list(json, "demolitionStepEffects", JsonDeserializer::deserializeDemolitionStepEffect));
        r.setDemolitionImageId(json.getNullableInt("demolitionImageId"));
        r.setBuildupTextureId(json.getNullableInt("buildupTextureId"));
        r.setExplosionAudioItemConfigId(json.getNullableInt("explosionAudioItemConfigId"));
        r.setExplosionParticleId(json.getNullableInt("explosionParticleId"));
        return r;
    }

    public static ResourceItemType deserializeResourceItemType(JsObject json) {
        if (json == null) return null;
        ResourceItemType r = new ResourceItemType();
        deserializeItemTypeFieldsResource(json, r);
        r.setRadius(json.getDouble("radius"));
        r.setFixVerticalNorm(json.getBoolean("fixVerticalNorm"));
        r.setTerrainType(deserializeEnum(json, "terrainType", TerrainType.class));
        r.setAmount(json.getInt("amount"));
        return r;
    }

    public static BoxItemType deserializeBoxItemType(JsObject json) {
        if (json == null) return null;
        BoxItemType r = new BoxItemType();
        deserializeItemTypeFieldsBox(json, r);
        r.setTtl(json.getNullableInt("ttl"));
        r.setRadius(json.getDouble("radius"));
        r.setFixVerticalNorm(json.getBoolean("fixVerticalNorm"));
        r.setTerrainType(deserializeEnum(json, "terrainType", TerrainType.class));
        r.setBoxItemTypePossibilities(list(json, "boxItemTypePossibilities", JsonDeserializer::deserializeBoxItemTypePossibility));
        return r;
    }

    public static PhysicalAreaConfig deserializePhysicalAreaConfig(JsObject json) {
        if (json == null) return null;
        PhysicalAreaConfig r = new PhysicalAreaConfig();
        r.setRadius(json.getDouble("radius"));
        r.setFixVerticalNorm(json.getBoolean("fixVerticalNorm"));
        r.setTerrainType(deserializeEnum(json, "terrainType", TerrainType.class));
        r.setAngularVelocity(json.getNullableDouble("angularVelocity"));
        r.setSpeed(json.getNullableDouble("speed"));
        r.setAcceleration(json.getNullableDouble("acceleration"));

        return r;
    }

    public static AudioItemConfig deserializeAudioItemConfig(JsObject json) {
        if (json == null) return null;
        AudioItemConfig r = new AudioItemConfig();
        r.setAudioId(json.getNullableInt("audioId"));
        r.setPitchCentsMin(json.getInt("pitchCentsMin"));
        r.setPitchCentsMax(json.getInt("pitchCentsMax"));
        r.setVolumeMin(json.getDouble("volumeMin"));
        r.setVolumeMax(json.getDouble("volumeMax"));
        return r;
    }

    public static WeaponType deserializeWeaponType(JsObject json) {
        if (json == null) return null;
        WeaponType r = new WeaponType();
        r.setRange(json.getDouble("range"));
        r.setDamage(json.getInt("damage"));
        r.setDetonationRadius(json.getDouble("detonationRadius"));
        r.setReloadTime(json.getDouble("reloadTime"));
        r.setDisallowedItemTypes(intList(json, "disallowedItemTypes"));
        r.setProjectileSpeed(json.getNullableDouble("projectileSpeed"));
        r.setImpactParticleSystemId(json.getNullableInt("impactParticleSystemId"));
        r.setTurretAngleVelocity(json.getNullableDouble("turretAngleVelocity"));
        r.setMuzzleFlashAudioConfig(deserializeAudioItemConfig(obj(json, "muzzleFlashAudioConfig")));
        r.setImpactAudioConfig(deserializeAudioItemConfig(obj(json, "impactAudioConfig")));
        r.setTrailParticleSystemConfigId(json.getNullableInt("trailParticleSystemConfigId"));
        return r;
    }

    public static FactoryType deserializeFactoryType(JsObject json) {
        if (json == null) return null;
        FactoryType r = new FactoryType();
        r.setProgress(json.getDouble("progress"));
        r.setAbleToBuildIds(intList(json, "ableToBuildIds"));
        return r;
    }

    public static BuilderType deserializeBuilderType(JsObject json) {
        if (json == null) return null;
        BuilderType r = new BuilderType();
        r.setRange(json.getDouble("range"));
        r.setRangeOtherTerrain(json.getDouble("rangeOtherTerrain"));
        r.setProgress(json.getDouble("progress"));
        r.setAbleToBuildIds(intList(json, "ableToBuildIds"));
        r.setParticleSystemConfigId(json.getNullableInt("particleSystemConfigId"));
        return r;
    }

    public static HarvesterType deserializeHarvesterType(JsObject json) {
        if (json == null) return null;
        HarvesterType r = new HarvesterType();
        r.setRange(json.getInt("range"));
        r.setProgress(json.getDouble("progress"));
        r.setParticleSystemConfigId(json.getNullableInt("particleSystemConfigId"));
        return r;
    }

    public static GeneratorType deserializeGeneratorType(JsObject json) {
        if (json == null) return null;
        GeneratorType r = new GeneratorType();
        r.setWattage(json.getInt("wattage"));
        return r;
    }

    public static ConsumerType deserializeConsumerType(JsObject json) {
        if (json == null) return null;
        ConsumerType r = new ConsumerType();
        r.setWattage(json.getInt("wattage"));
        return r;
    }

    public static ItemContainerType deserializeItemContainerType(JsObject json) {
        if (json == null) return null;
        ItemContainerType r = new ItemContainerType();
        r.setAbleToContain(intList(json, "ableToContain"));
        r.setMaxCount(json.getInt("maxCount"));
        r.setRange(json.getDouble("range"));
        return r;
    }

    public static HouseType deserializeHouseType(JsObject json) {
        if (json == null) return null;
        HouseType r = new HouseType();
        r.setSpace(json.getInt("space"));
        return r;
    }

    public static SpecialType deserializeSpecialType(JsObject json) {
        if (json == null) return null;
        SpecialType r = new SpecialType();
        r.setMiniTerrain(json.getBoolean("miniTerrain"));
        return r;
    }

    public static DemolitionStepEffect deserializeDemolitionStepEffect(JsObject json) {
        if (json == null) return null;
        return new DemolitionStepEffect();
    }

    public static BoxItemTypePossibility deserializeBoxItemTypePossibility(JsObject json) {
        if (json == null) return null;
        BoxItemTypePossibility r = new BoxItemTypePossibility();
        r.setPossibility(json.getDouble("possibility"));
        r.setInventoryItemId(json.getNullableInt("inventoryItemId"));
        r.setCrystals(json.getNullableInt("crystals"));
        return r;
    }

    // --- Scene / Quest / Bot ---

    public static SlavePlanetConfig deserializeSlavePlanetConfig(JsObject json) {
        if (json == null) return null;
        SlavePlanetConfig r = new SlavePlanetConfig();
        r.setStartRegion(deserializePlaceConfig(obj(json, "startRegion")));
        r.setNoBaseViewPosition(deserializeDecimalPosition(obj(json, "noBaseViewPosition")));
        r.setFindFreePosition(json.getBoolean("findFreePosition"));
        r.setPositionPath(decimalPositionList(json, "positionPath"));
        r.setPositionRadius(json.getNullableDouble("positionRadius"));
        r.setPositionMaxItems(json.getNullableInt("positionMaxItems"));
        return r;
    }

    public static SlaveQuestInfo deserializeSlaveQuestInfo(JsObject json) {
        if (json == null) return null;
        SlaveQuestInfo r = new SlaveQuestInfo();
        r.setActiveQuest(deserializeQuestConfig(obj(json, "activeQuest")));
        r.setQuestProgressInfo(deserializeQuestProgressInfo(obj(json, "questProgressInfo")));
        return r;
    }

    public static QuestProgressInfo deserializeQuestProgressInfo(JsObject json) {
        if (json == null) return null;
        QuestProgressInfo r = new QuestProgressInfo();
        r.setCount(json.getNullableInt("count"));
        r.setTypeCount(intIntMap(json, "typeCount"));
        r.setSecondsRemaining(json.getNullableInt("secondsRemaining"));
        r.setBotBasesInformation(json.isNullOrUndefined("botBasesInformation") ? null : json.getString("botBasesInformation"));
        return r;
    }

    public static PlaceConfig deserializePlaceConfig(JsObject json) {
        if (json == null) return null;
        PlaceConfig r = new PlaceConfig();
        r.setPolygon2D(deserializePolygon2D(obj(json, "polygon2D")));
        r.setPosition(deserializeDecimalPosition(obj(json, "position")));
        r.setRadius(json.getNullableDouble("radius"));
        return r;
    }

    public static QuestConfig deserializeQuestConfig(JsObject json) {
        if (json == null) return null;
        QuestConfig r = new QuestConfig();
        // QuestDescriptionConfig fields
        r.setId(json.getNullableInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setXp(json.getInt("xp"));
        r.setRazarion(json.getInt("razarion"));
        r.setCrystal(json.getInt("crystal"));
        r.setTipConfig(deserializeTipConfig(obj(json, "tipConfig")));
        // QuestConfig fields
        r.setConditionConfig(deserializeConditionConfig(obj(json, "conditionConfig")));
        return r;
    }

    public static TipConfig deserializeTipConfig(JsObject json) {
        if (json == null) return null;
        TipConfig r = new TipConfig();
        r.setTipString(json.isNullOrUndefined("tipString") ? null : json.getString("tipString"));
        r.setActorItemTypeId(json.getNullableInt("actorItemTypeId"));
        return r;
    }

    public static ConditionConfig deserializeConditionConfig(JsObject json) {
        if (json == null) return null;
        ConditionConfig r = new ConditionConfig();
        r.setConditionTrigger(deserializeEnum(json, "conditionTrigger", ConditionTrigger.class));
        r.setComparisonConfig(deserializeComparisonConfig(obj(json, "comparisonConfig")));
        return r;
    }

    public static ComparisonConfig deserializeComparisonConfig(JsObject json) {
        if (json == null) return null;
        ComparisonConfig r = new ComparisonConfig();
        r.setCount(json.getNullableInt("count"));
        r.setTypeCount(intIntMap(json, "typeCount"));
        r.setIncludeExisting(json.getBoolean("includeExisting"));
        r.setTimeSeconds(json.getNullableInt("timeSeconds"));
        r.setPlaceConfig(deserializePlaceConfig(obj(json, "placeConfig")));
        r.setStartRegionId(json.getNullableInt("startRegionId"));
        r.setBotIds(intList(json, "botIds"));
        return r;
    }

    public static SceneConfig deserializeSceneConfig(JsObject json) {
        if (json == null) return null;
        SceneConfig r = new SceneConfig();
        r.setId(json.getNullableInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setIntroText(json.isNullOrUndefined("introText") ? null : json.getString("introText"));
        r.setQuestConfig(deserializeQuestConfig(obj(json, "questConfig")));
        r.setViewFieldConfig(deserializeViewFieldConfig(obj(json, "viewFieldConfig")));
        r.setSuppressSell(json.getNullableBoolean("suppressSell"));
        r.setBotConfigs(list(json, "botConfigs", JsonDeserializer::deserializeBotConfig));
        r.setBotMoveCommandConfigs(list(json, "botMoveCommandConfigs", JsonDeserializer::deserializeBotMoveCommandConfig));
        r.setBotHarvestCommandConfigs(list(json, "botHarvestCommandConfigs", JsonDeserializer::deserializeBotHarvestCommandConfig));
        r.setBotAttackCommandConfigs(list(json, "botAttackCommandConfigs", JsonDeserializer::deserializeBotAttackCommandConfig));
        r.setBotKillOtherBotCommandConfigs(list(json, "botKillOtherBotCommandConfigs", JsonDeserializer::deserializeBotKillOtherBotCommandConfig));
        r.setBotKillHumanCommandConfigs(list(json, "botKillHumanCommandConfigs", JsonDeserializer::deserializeBotKillHumanCommandConfig));
        r.setBotRemoveOwnItemCommandConfigs(list(json, "botRemoveOwnItemCommandConfigs", JsonDeserializer::deserializeBotRemoveOwnItemCommandConfig));
        r.setKillBotCommandConfigs(list(json, "killBotCommandConfigs", JsonDeserializer::deserializeKillBotCommandConfig));
        r.setStartPointPlacerConfig(deserializeBaseItemPlacerConfig(obj(json, "startPointPlacerConfig")));
        r.setWait4LevelUpDialog(json.getNullableBoolean("wait4LevelUpDialog"));
        r.setWait4QuestPassedDialog(json.getNullableBoolean("wait4QuestPassedDialog"));
        r.setWaitForBaseLostDialog(json.getNullableBoolean("waitForBaseLostDialog"));
        r.setWaitForBaseCreated(json.getNullableBoolean("waitForBaseCreated"));
        r.setProcessServerQuests(json.getNullableBoolean("processServerQuests"));
        r.setResourceItemTypePositions(list(json, "resourceItemTypePositions", JsonDeserializer::deserializeResourceItemPosition));
        r.setDuration(json.getNullableInt("duration"));
        r.setScrollUiQuest(deserializeScrollUiQuest(obj(json, "scrollUiQuest")));
        r.setBoxItemPositions(list(json, "boxItemPositions", JsonDeserializer::deserializeBoxItemPosition));
        return r;
    }

    public static ViewFieldConfig deserializeViewFieldConfig(JsObject json) {
        if (json == null) return null;
        ViewFieldConfig r = new ViewFieldConfig();
        r.setFromPosition(deserializeDecimalPosition(obj(json, "fromPosition")));
        r.setToPosition(deserializeDecimalPosition(obj(json, "toPosition")));
        r.setSpeed(json.getNullableDouble("speed"));
        r.setCameraLocked(json.getBoolean("cameraLocked"));
        r.setBottomWidth(json.getNullableDouble("bottomWidth"));
        return r;
    }

    public static BaseItemPlacerConfig deserializeBaseItemPlacerConfig(JsObject json) {
        if (json == null) return null;
        BaseItemPlacerConfig r = new BaseItemPlacerConfig();
        r.setSuggestedPosition(deserializeDecimalPosition(obj(json, "suggestedPosition")));
        r.setBaseItemTypeId(json.getInt("baseItemTypeId"));
        r.setBaseItemCount(json.getInt("baseItemCount"));
        r.setEnemyFreeRadius(json.getNullableDouble("enemyFreeRadius"));
        r.setAllowedArea(deserializePlaceConfig(obj(json, "allowedArea")));
        return r;
    }

    public static ResourceItemPosition deserializeResourceItemPosition(JsObject json) {
        if (json == null) return null;
        ResourceItemPosition r = new ResourceItemPosition();
        r.setResourceItemTypeId(json.getNullableInt("resourceItemTypeId"));
        r.setPosition(deserializeDecimalPosition(obj(json, "position")));
        r.setRotationZ(json.getDouble("rotationZ"));
        return r;
    }

    public static BoxItemPosition deserializeBoxItemPosition(JsObject json) {
        if (json == null) return null;
        BoxItemPosition r = new BoxItemPosition();
        r.setBoxItemTypeId(json.getNullableInt("boxItemTypeId"));
        r.setPosition(deserializeDecimalPosition(obj(json, "position")));
        r.setRotationZ(json.getDouble("rotationZ"));
        return r;
    }

    public static ScrollUiQuest deserializeScrollUiQuest(JsObject json) {
        if (json == null) return null;
        ScrollUiQuest r = new ScrollUiQuest();
        // QuestDescriptionConfig fields
        r.setId(json.getNullableInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setXp(json.getInt("xp"));
        r.setRazarion(json.getInt("razarion"));
        r.setCrystal(json.getInt("crystal"));
        r.setTipConfig(deserializeTipConfig(obj(json, "tipConfig")));
        // ScrollUiQuest fields
        r.setScrollTargetRectangle(deserializeRectangle2D(obj(json, "scrollTargetRectangle")));
        return r;
    }

    // --- Bot configs ---

    public static BotConfig deserializeBotConfig(JsObject json) {
        if (json == null) return null;
        BotConfig r = new BotConfig();
        r.setId(json.getInt("id"));
        r.setInternalName(json.isNullOrUndefined("internalName") ? null : json.getString("internalName"));
        r.setAuxiliaryId(json.getNullableInt("auxiliaryId"));
        r.setNpc(json.getBoolean("npc"));
        r.setActionDelay(json.getInt("actionDelay"));
        r.setRealm(deserializePlaceConfig(obj(json, "realm")));
        r.setName(json.isNullOrUndefined("name") ? null : json.getString("name"));
        r.setAutoAttack(json.getBoolean("autoAttack"));
        r.setMinInactiveMs(json.getNullableInt("minInactiveMs"));
        r.setMaxInactiveMs(json.getNullableInt("maxInactiveMs"));
        r.setMinActiveMs(json.getNullableInt("minActiveMs"));
        r.setMaxActiveMs(json.getNullableInt("maxActiveMs"));
        r.setBotEnragementStateConfigs(list(json, "botEnragementStateConfigs", JsonDeserializer::deserializeBotEnragementStateConfig));
        r.setGroundBoxModel3DEntityId(json.getNullableInt("groundBoxModel3DEntityId"));
        r.setGroundBoxHeight(json.getNullableDouble("groundBoxHeight"));
        r.setGroundBoxPositions(decimalPositionList(json, "groundBoxPositions"));
        r.setBotGroundSlopeBoxes(list(json, "botGroundSlopeBoxes", JsonDeserializer::deserializeBotGroundSlopeBox));
        return r;
    }

    public static BotEnragementStateConfig deserializeBotEnragementStateConfig(JsObject json) {
        if (json == null) return null;
        BotEnragementStateConfig r = new BotEnragementStateConfig();
        r.setName(json.isNullOrUndefined("name") ? null : json.getString("name"));
        r.setBotItems(list(json, "botItems", JsonDeserializer::deserializeBotItemConfig));
        r.setEnrageUpKills(json.getNullableInt("enrageUpKills"));
        return r;
    }

    public static BotItemConfig deserializeBotItemConfig(JsObject json) {
        if (json == null) return null;
        BotItemConfig r = new BotItemConfig();
        r.setBaseItemTypeId(json.getNullableInt("baseItemTypeId"));
        r.setCount(json.getInt("count"));
        r.setCreateDirectly(json.getBoolean("createDirectly"));
        r.setNoSpawn(json.getBoolean("noSpawn"));
        r.setPlace(deserializePlaceConfig(obj(json, "place")));
        r.setAngle(json.getDouble("angle"));
        r.setMoveRealmIfIdle(json.getBoolean("moveRealmIfIdle"));
        r.setIdleTtl(json.getNullableInt("idleTtl"));
        r.setNoRebuild(json.getBoolean("noRebuild"));
        r.setRePopTime(json.getNullableInt("rePopTime"));
        return r;
    }

    public static BotGroundSlopeBox deserializeBotGroundSlopeBox(JsObject json) {
        if (json == null) return null;
        BotGroundSlopeBox r = new BotGroundSlopeBox();
        r.xPos = json.getDouble("xPos");
        r.yPos = json.getDouble("yPos");
        r.height = json.getDouble("height");
        r.yRot = json.getDouble("yRot");
        r.zRot = json.getDouble("zRot");
        return r;
    }

    // --- Bot command configs ---

    public static BotMoveCommandConfig deserializeBotMoveCommandConfig(JsObject json) {
        if (json == null) return null;
        BotMoveCommandConfig r = new BotMoveCommandConfig();
        r.setBotAuxiliaryId(json.getNullableInt("botAuxiliaryId"));
        r.setBaseItemTypeId(json.getNullableInt("baseItemTypeId"));
        r.setTargetPosition(deserializeDecimalPosition(obj(json, "targetPosition")));
        return r;
    }

    public static BotHarvestCommandConfig deserializeBotHarvestCommandConfig(JsObject json) {
        if (json == null) return null;
        BotHarvestCommandConfig r = new BotHarvestCommandConfig();
        r.setBotAuxiliaryId(json.getNullableInt("botAuxiliaryId"));
        r.setHarvesterItemTypeId(json.getNullableInt("harvesterItemTypeId"));
        r.setResourceItemTypeId(json.getNullableInt("resourceItemTypeId"));
        r.setResourceSelection(deserializePlaceConfig(obj(json, "resourceSelection")));
        return r;
    }

    public static BotAttackCommandConfig deserializeBotAttackCommandConfig(JsObject json) {
        if (json == null) return null;
        BotAttackCommandConfig r = new BotAttackCommandConfig();
        r.setBotAuxiliaryId(json.getNullableInt("botAuxiliaryId"));
        r.setTargetItemTypeId(json.getNullableInt("targetItemTypeId"));
        r.setTargetSelection(deserializePlaceConfig(obj(json, "targetSelection")));
        r.setActorItemTypeId(json.getNullableInt("actorItemTypeId"));
        return r;
    }

    public static BotKillOtherBotCommandConfig deserializeBotKillOtherBotCommandConfig(JsObject json) {
        if (json == null) return null;
        BotKillOtherBotCommandConfig r = new BotKillOtherBotCommandConfig();
        // AbstractBotCommandConfig
        r.setBotAuxiliaryId(json.getNullableInt("botAuxiliaryId"));
        // BotKillBaseCommandConfig
        r.setAttackerBaseItemTypeId(json.isNullOrUndefined("attackerBaseItemTypeId") ? 0 : json.getInt("attackerBaseItemTypeId"));
        r.setDominanceFactor(json.getInt("dominanceFactor"));
        r.setSpawnPoint(deserializePlaceConfig(obj(json, "spawnPoint")));
        // BotKillOtherBotCommandConfig
        r.setTargetBotAuxiliaryId(json.getNullableInt("targetBotAuxiliaryId"));
        return r;
    }

    public static BotKillHumanCommandConfig deserializeBotKillHumanCommandConfig(JsObject json) {
        if (json == null) return null;
        BotKillHumanCommandConfig r = new BotKillHumanCommandConfig();
        // AbstractBotCommandConfig
        r.setBotAuxiliaryId(json.getNullableInt("botAuxiliaryId"));
        // BotKillBaseCommandConfig
        r.setAttackerBaseItemTypeId(json.isNullOrUndefined("attackerBaseItemTypeId") ? 0 : json.getInt("attackerBaseItemTypeId"));
        r.setDominanceFactor(json.getInt("dominanceFactor"));
        r.setSpawnPoint(deserializePlaceConfig(obj(json, "spawnPoint")));
        return r;
    }

    public static BotRemoveOwnItemCommandConfig deserializeBotRemoveOwnItemCommandConfig(JsObject json) {
        if (json == null) return null;
        BotRemoveOwnItemCommandConfig r = new BotRemoveOwnItemCommandConfig();
        r.setBotAuxiliaryId(json.getNullableInt("botAuxiliaryId"));
        r.setBaseItemType2RemoveId(json.getNullableInt("baseItemType2RemoveId"));
        return r;
    }

    public static KillBotCommandConfig deserializeKillBotCommandConfig(JsObject json) {
        if (json == null) return null;
        KillBotCommandConfig r = new KillBotCommandConfig();
        r.setBotAuxiliaryId(json.getNullableInt("botAuxiliaryId"));
        return r;
    }

    public static LifecyclePacket deserializeLifecyclePacket(JsObject json) {
        if (json == null) return null;
        LifecyclePacket r = new LifecyclePacket();
        r.setType(deserializeEnum(json, "type", LifecyclePacket.Type.class));
        r.setDialog(deserializeEnum(json, "dialog", LifecyclePacket.Dialog.class));
        return r;
    }

    public static LevelUpPacket deserializeLevelUpPacket(JsObject json) {
        if (json == null) return null;
        LevelUpPacket r = new LevelUpPacket();
        r.setUserContext(deserializeUserContext(obj(json, "userContext")));
        r.setAvailableUnlocks(json.getBoolean("availableUnlocks"));
        return r;
    }

    public static BoxContent deserializeBoxContent(JsObject json) {
        if (json == null) return null;
        BoxContent r = new BoxContent();
        r.setCrystals(json.getInt("crystals"));
        r.setInventoryItems(list(json, "inventoryItems", JsonDeserializer::deserializeInventoryItem));
        return r;
    }

    public static UnlockedItemPacket deserializeUnlockedItemPacket(JsObject json) {
        if (json == null) return null;
        UnlockedItemPacket r = new UnlockedItemPacket();
        r.setUnlockedItemLimit(intIntMap(json, "unlockedItemLimit"));
        r.setAvailableUnlocks(json.getBoolean("availableUnlocks"));
        return r;
    }

    public static ChatMessage deserializeChatMessage(JsObject json) {
        if (json == null) return null;
        ChatMessage r = new ChatMessage();
        r.setUserName(json.isNullOrUndefined("userName") ? null : json.getString("userName"));
        r.setMessage(json.isNullOrUndefined("message") ? null : json.getString("message"));
        return r;
    }
}

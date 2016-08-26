package com.btxtech.server.emulation;

import com.btxtech.servercommon.StoryboardPersistence;
import com.btxtech.servercommon.collada.Emulation;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.PlaceConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 03.08.2016.
 */
@Emulation
@Singleton
public class StoryboardEmulationImpl implements StoryboardPersistence {
    @Inject
    private ItemTypeEmulation itemTypeEmulation;
    @Inject
    private JsonPersistence jsonPersistence;
    @Inject
    private Shape3DEmulator shape3DEmulator;

    @Override
    public StoryboardConfig load() {
        return jsonPersistence.readJson("StoryboardConfig.json", StoryboardConfig.class);

//        StoryboardConfig storyboardConfig = new StoryboardConfig();
//        // Setup game engine
//        GameEngineConfig gameEngineConfig = new GameEngineConfig().setItemTypes(itemTypeEmulation.createItemTypes());
//        // TODO gameEngineConfig.setGroundSkeletonConfig(loadedStoryBoard.getGameEngineConfig().getGroundSkeletonConfig());
//        // TODO gameEngineConfig.setSlopeSkeletonConfigs(loadedStoryBoard.getGameEngineConfig().getSlopeSkeletonConfigs());
//        // TODO gameEngineConfig.setTerrainObjectConfigs(loadedStoryBoard.getGameEngineConfig().getTerrainObjectConfigs());
//        PlanetConfig planetConfig = new PlanetConfig();
//        planetConfig.setWaterLevel(-7);
//        // TODO planetConfig.setTerrainSlopePositions(loadedStoryBoard.getGameEngineConfig().getPlanetConfig().getTerrainSlopePositions());
//        // TODO planetConfig.setTerrainObjectPositions(loadedStoryBoard.getGameEngineConfig().getPlanetConfig().getTerrainObjectPositions());
//        gameEngineConfig.setPlanetConfig(planetConfig);
//        storyboardConfig.setGameEngineConfig(gameEngineConfig);
//        // Setup scenes
//        List<SceneConfig> sceneConfigs = new ArrayList<>();
//        CameraConfig cameraConfig = new CameraConfig().setToPosition(new Index(200, 200));
//        List<BotConfig> botConfigs = new ArrayList<>();
//        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
//        Collection<BotItemConfig> botItems = new ArrayList<>();
//        botItems.add(new BotItemConfig().setBaseItemTypeId(ItemTypeEmulation.Id.SIMPLE_MOVABLE.ordinal()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new Index(200, 500))));
//        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
//        botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
//        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotConfigs(botConfigs));
//        storyboardConfig.setSceneConfigs(sceneConfigs);
//        // Setup VisualConfig
//        VisualConfig visualConfig = new VisualConfig();
//        visualConfig.setShadowAlpha(0.2).setShadowRotationX(Math.toRadians(25)).setShadowRotationZ(Math.toRadians(250));
//        visualConfig.setShape3DLightRotateX(Math.toRadians(25)).setShadowRotationZ(Math.toRadians(290));
//        visualConfig.setWaterGroundLevel(-20).setWaterBmDepth(10).setWaterTransparency(0.65);
//        LightConfig lightConfig = new LightConfig();
//        lightConfig.setDiffuse(new Color(1, 1, 1)).setAmbient(new Color(1, 1, 1)).setXRotation(Math.toRadians(-20));
//        lightConfig.setYRotation(Math.toRadians(-20)).setSpecularIntensity(1.0).setSpecularHardness(0.5);
//        visualConfig.setWaterLightConfig(lightConfig);
//        visualConfig.setShape3Ds(shape3DEmulator.getShape3Ds()).setShape3DGeneralScale(10);
//        storyboardConfig.setVisualConfig(visualConfig);
//        return storyboardConfig;
    }

    private List<SceneConfig> setSceneConfig(List<SceneConfig> originalSceneConfig) {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        sceneConfigs.add(setupAnimationSceneConfig());
        // sceneConfigs.addAll(originalSceneConfig);
        return sceneConfigs;
    }

    private SceneConfig setupAnimationSceneConfigOLD() {
        SceneConfig sceneConfig = new SceneConfig();
//        CameraConfig cameraConfig = new CameraConfig();
//        cameraConfig.setCameraLocked(true);
//        cameraConfig.setSmooth(true);
//        cameraConfig.setFromPosition(new Index(2000, 2000));
//        cameraConfig.setToPosition(new Index(200, 200));
//        sceneConfig.setCameraConfig(cameraConfig);
//
//        sceneConfig.setIntroText("Kenny wird dich dabei unterstützen");
//        AnimatedMeshConfig animatedMeshConfig = new AnimatedMeshConfig();
//        try {
//            ColladaConverterInput input = new ColladaConverterInput();
//            input.setColladaString(IOUtils.toString(new FileInputStream("C:\\dev\\projects\\razarion\\code\\tmp\\ArrivelBall01.dae"))).setId(1).setTextureMapper(new DevToolColladaConverterTextureMapper());
//            TerrainObjectConfig terrainObject = ColladaConverter.convertToTerrainObject(input);
//            animatedMeshConfig.setVertexContainer(CollectionUtils.getFirst(terrainObject.getVertexContainers()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        animatedMeshConfig.setPosition(new Vertex(1000, 1000, 10));
//        animatedMeshConfig.setDuration(10000);
//        animatedMeshConfig.setScaleFrom(1);
//        animatedMeshConfig.setScaleTo(10);
//        sceneConfig.setAnimatedMeshConfig(animatedMeshConfig);
//
//        return sceneConfig;
        return null;
    }

    private SceneConfig setupAnimationSceneConfig() {
        SceneConfig sceneConfig = new SceneConfig();
        CameraConfig cameraConfig = new CameraConfig();
        cameraConfig.setToPosition(new Index(200, 200));
        sceneConfig.setCameraConfig(cameraConfig);

        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        Collection<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig());
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        List<BotConfig> botConfigs = new ArrayList<>();
        botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny"));
        sceneConfig.setBotConfigs(botConfigs);
        return sceneConfig;
    }
}

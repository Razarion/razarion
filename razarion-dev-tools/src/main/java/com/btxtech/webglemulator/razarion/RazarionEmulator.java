package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.PlaceConfig;
import com.btxtech.uiservice.storyboard.StoryboardService;
import com.btxtech.webglemulator.WebGlEmulatorSceneController;
import com.google.gson.Gson;
import javafx.application.Platform;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 22.05.2016.
 */
@ApplicationScoped
public class RazarionEmulator {
    private static final long RENDER_DELAY = 100;
    @Inject
    private WebGlEmulatorSceneController sceneController;
    @Inject
    private StoryboardService storyboardService;
    @Inject
    private DevToolsRenderServiceImpl renderService;
    @Inject
    private ItemTypeEmulation itemTypeEmulation;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean showRenderTime;

    public void run() {
        setupStoryboard();

        renderService.setup();

        start();
    }

    public boolean isShowRenderTime() {
        return showRenderTime;
    }

    public void setShowRenderTime(boolean showRenderTime) {
        this.showRenderTime = showRenderTime;
    }

    private void start() {
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long time = System.currentTimeMillis();
                            renderService.render();
                            sceneController.update();
                            if (showRenderTime) {
                                System.out.println("Time for render: " + (System.currentTimeMillis() - time));
                            }
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                });
            }
        }, RENDER_DELAY, RENDER_DELAY, TimeUnit.MILLISECONDS);
        storyboardService.start();
    }

    private void setupStoryboard() {
        // Gson gson = new Gson();
        // StoryboardConfig storyboardConfig = gson.fromJson(new InputStreamReader(RazarionEmulator.class.getResourceAsStream("/StoryboardConfig.json")), StoryboardConfig.class);
        // storyboardConfig.setSceneConfigs(setSceneConfig(storyboardConfig.getSceneConfigs()));
        StoryboardConfig storyboardConfig = new StoryboardConfig();
        // Setup game engine
        Gson gson = new Gson();
        GameEngineConfig gameEngineConfig = new GameEngineConfig().setItemTypes(itemTypeEmulation.createItemTypes());
        gameEngineConfig.setGroundSkeletonConfig(gson.fromJson(new InputStreamReader(getClass().getResourceAsStream("/GroundSkeleton.json")), GroundSkeletonConfig.class));
        gameEngineConfig.setPlanetConfig(new PlanetConfig());
        storyboardConfig.setGameEngineConfig(gameEngineConfig);
        // Setup scenes
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new Index(200, 200));
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        Collection<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(ItemTypeEmulation.Id.SIMPLE_MOVABLE.ordinal()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new Index(200, 500))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotConfigs(botConfigs));
        storyboardConfig.setSceneConfigs(sceneConfigs);
        // Init
        storyboardService.init(storyboardConfig);
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
//            TerrainObject terrainObject = ColladaConverter.convertToTerrainObject(input);
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

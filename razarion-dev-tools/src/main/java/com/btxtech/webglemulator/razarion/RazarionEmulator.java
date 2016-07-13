package com.btxtech.webglemulator.razarion;

import com.btxtech.servercommon.collada.ColladaConverter;
import com.btxtech.servercommon.collada.ColladaConverterInput;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.AnimatedMeshConfig;
import com.btxtech.shared.dto.ItemType;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.storyboard.StoryboardService;
import com.btxtech.uiservice.units.ItemService;
import com.btxtech.webglemulator.WebGlEmulatorSceneController;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 22.05.2016.
 */
@Singleton
public class RazarionEmulator {
    private static final long RENDER_DELAY = 800;
    @Inject
    private ItemService itemService;
    @Inject
    private WebGlEmulatorSceneController sceneController;
    @Inject
    private StoryboardService storyboardService;
    @Inject
    private DevToolsRenderServiceImpl renderService;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void run() {
        setupStoryboard();
        storyboardService.setup();
        setupItems();

        renderService.setupRenderers();

        start();
    }

    private void start() {
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // long time = System.currentTimeMillis();
                            renderService.render();
                            sceneController.update();
                            // System.out.println("Time for render: " + (System.currentTimeMillis() - time));
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
        Gson gson = new Gson();
        StoryboardConfig storyboardConfig = gson.fromJson(new InputStreamReader(RazarionEmulator.class.getResourceAsStream("/StoryboardConfig.json")), StoryboardConfig.class);
        storyboardConfig.setSceneConfigs(setSceneConfig(storyboardConfig.getSceneConfigs()));
        storyboardService.init(storyboardConfig);
    }

    private List<SceneConfig> setSceneConfig(List<SceneConfig> originalSceneConfig) {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        sceneConfigs.add(setupAnimationSceneConfig());
        // sceneConfigs.addAll(originalSceneConfig);
        return sceneConfigs;
    }

    private SceneConfig setupAnimationSceneConfig() {
        SceneConfig sceneConfig = new SceneConfig();
//        CameraConfig cameraConfig = new CameraConfig();
//        cameraConfig.setCameraLocked(true);
//        cameraConfig.setSmooth(true);
//        cameraConfig.setFromPosition(new Index(2000, 2000));
//        cameraConfig.setToPosition(new Index(200, 200));
//        sceneConfig.setCameraConfig(cameraConfig);

        sceneConfig.setIntroText("Kenny wird dich dabei unterstützen");
        AnimatedMeshConfig animatedMeshConfig = new AnimatedMeshConfig();
        try {
            ColladaConverterInput input = new ColladaConverterInput();
            input.setColladaString(IOUtils.toString(new FileInputStream("C:\\dev\\projects\\razarion\\code\\tmp\\ArrivelBall01.dae"))).setId(1).setTextureMapper(new DummyColladaConverterTextureMapper());
            TerrainObject terrainObject = ColladaConverter.convertToTerrainObject(input);
            animatedMeshConfig.setVertexContainer(CollectionUtils.getFirst(terrainObject.getVertexContainers()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        animatedMeshConfig.setPosition(new Vertex(1000, 1000, 10));
        animatedMeshConfig.setDuration(10000);
        animatedMeshConfig.setScaleFrom(1);
        animatedMeshConfig.setScaleTo(10);
        sceneConfig.setAnimatedMeshConfig(animatedMeshConfig);

        return sceneConfig;
    }

    @Deprecated
    private void setupItems() {
        Gson gson = new Gson();
        List<ItemType> itemTypes = gson.fromJson(new InputStreamReader(RazarionEmulator.class.getResourceAsStream("/ItemType.json")), new TypeToken<List<ItemType>>() {
        }.getType());
        try {
            itemService.setItemTypes(itemTypes);
            itemService.init();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

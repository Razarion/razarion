package com.btxtech.client.system.boot;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.GameTipVisualConfig;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.ViewPositionConfig;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class ExperimentalTask extends AbstractStartupTask {
    // private Logger logger = Logger.getLogger(ExperimentalTask.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        gameUiControl.setGameUiControlConfig(setupGameUiControlConfig());
        gameUiControl.init();
        gameCanvas.init();
        gameCanvas.startRenderLoop();
        gameUiControl.start();
    }

    private GameUiControlConfig setupGameUiControlConfig() {
        GameEngineConfig gameEngineConfig = new GameEngineConfig();
        gameEngineConfig.setGroundSkeletonConfig(defaultGroundSkeletonConfig());
        gameEngineConfig.setLevelConfigs(Collections.emptyList());
        gameEngineConfig.setPlanetConfig(defaultPlanetConfig());
        GameUiControlConfig gameUiControlConfig = new GameUiControlConfig();
        gameUiControlConfig.setUserContext(new UserContext().setUserId(1).setName("Emulator Name").setLevelId(1).setInventoryItemIds(Collections.emptyList()));
        gameUiControlConfig.setVisualConfig(defaultVisualConfig());
        gameUiControlConfig.setGameEngineConfig(gameEngineConfig);
        gameUiControlConfig.setSceneConfigs(defaultSceneConfigs());
        gameUiControlConfig.setGameTipVisualConfig(defaultGameTipVisualConfig());
        return gameUiControlConfig;
    }

    private GroundSkeletonConfig defaultGroundSkeletonConfig() {
        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        groundSkeletonConfig.setSplattingXCount(1);
        groundSkeletonConfig.setSplattingYCount(1);
        groundSkeletonConfig.setSplattings(new double[][]{{1, 1}, {1, 1}});
        groundSkeletonConfig.setHeightXCount(1);
        groundSkeletonConfig.setHeightYCount(1);
        groundSkeletonConfig.setHeights(new double[][]{{1, 1}, {1, 1}});
        return groundSkeletonConfig;
    }

    private GameTipVisualConfig defaultGameTipVisualConfig() {
        GameTipVisualConfig gameTipVisualConfig = new GameTipVisualConfig();
        gameTipVisualConfig.setCornerMoveDuration(1500);
        gameTipVisualConfig.setCornerMoveDistance(15);
        gameTipVisualConfig.setCornerLength(1);
        gameTipVisualConfig.setDefaultCommandShape3DId(272501);
        gameTipVisualConfig.setSelectCornerColor(new Color(0, 1, 0));
        gameTipVisualConfig.setSelectShape3DId(272499);
        gameTipVisualConfig.setOutOfViewShape3DId(272503);
        gameTipVisualConfig.setAttackCommandCornerColor(new Color(1, 0, 0));
        gameTipVisualConfig.setBaseItemPlacerCornerColor(new Color(1, 1, 0));
        gameTipVisualConfig.setBaseItemPlacerShape3DId(272499);
        gameTipVisualConfig.setGrabCommandCornerColor(new Color(0, 0, 1));
        gameTipVisualConfig.setMoveCommandCornerColor(new Color(0, 1, 0));
        gameTipVisualConfig.setToBeFinalizedCornerColor(new Color(1, 1, 0));
        gameTipVisualConfig.setWestLeftMouseGuiImageId(272506);
        gameTipVisualConfig.setSouthLeftMouseGuiImageId(272507);
        gameTipVisualConfig.setDirectionShape3DId(272503);
        gameTipVisualConfig.setSplashImageId(272508);
        return gameTipVisualConfig;
    }

    private VisualConfig defaultVisualConfig() {
        VisualConfig visualConfig = new VisualConfig();
        visualConfig.setShadowAlpha(0.2).setShadowRotationX(Math.toRadians(-27)).setShadowRotationY(Math.toRadians(0));
        visualConfig.setShape3DLightRotateX(Math.toRadians(25)).setShape3DLightRotateZ(Math.toRadians(290));
        visualConfig.setWaterGroundLevel(-2).setWaterBmDepth(10).setWaterTransparency(0.65).setWaterBmId(272480).setWaterBmDepth(20).setWaterBmScale(0.01);
        LightConfig lightConfig = new LightConfig();
        lightConfig.setDiffuse(new Color(1, 1, 1)).setAmbient(new Color(1, 1, 1)).setRotationX(Math.toRadians(-20));
        lightConfig.setRotationY(Math.toRadians(-20)).setSpecularIntensity(1.0).setSpecularHardness(0.5);
        visualConfig.setWaterLightConfig(lightConfig);
        visualConfig.setBaseItemDemolitionImageId(180848);
        return visualConfig;
    }

    private PlanetConfig defaultPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setHouseSpace(10);
        planetConfig.setGroundMeshDimension(new Rectangle(0, 0, 64, 64));
        planetConfig.setPlayGround(new Rectangle2D(50, 40, 310, 320));
        planetConfig.setWaterLevel(-0.7);
        planetConfig.setStartRazarion(550);
        return planetConfig;
    }

    private List<SceneConfig> defaultSceneConfigs() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        sceneConfigs.add(new SceneConfig().setRemoveLoadingCover(true).setViewPositionConfig(new ViewPositionConfig().setToPosition(new DecimalPosition(200, 200))));
        return sceneConfigs;
    }
}

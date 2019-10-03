package com.btxtech.client.system.boot;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.GameTipVisualConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SpecularLightConfig;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.dto.WarmGameUiControlConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
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
        gameUiControl.setColdGameUiControlConfig(setupGameUiControlConfig());
        gameUiControl.init();
        gameCanvas.init();
        renderService.setup();
        gameCanvas.startRenderLoop();
        gameUiControl.start();
    }

    private ColdGameUiControlConfig setupGameUiControlConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setGroundSkeletonConfig(defaultGroundSkeletonConfig());
        staticGameConfig.setLevelConfigs(Collections.emptyList());
        staticGameConfig.setWaterConfig(defaultWaterConfig());
        ColdGameUiControlConfig coldGameUiControlConfig = new ColdGameUiControlConfig();
        coldGameUiControlConfig.setUserContext(new UserContext().setHumanPlayerId(new HumanPlayerId().setPlayerId(1)).setName("Emulator Name").setLevelId(1));
        coldGameUiControlConfig.setAudioConfig(new AudioConfig());
        coldGameUiControlConfig.setStaticGameConfig(staticGameConfig);
        coldGameUiControlConfig.setGameTipVisualConfig(defaultGameTipVisualConfig());
        coldGameUiControlConfig.setWarmGameUiControlConfig(new WarmGameUiControlConfig().setGameUiControlConfigId(-1).setGameEngineMode(GameEngineMode.MASTER).setSceneConfigs(defaultSceneConfigs()).setPlanetConfig(defaultPlanetConfig()).setPlanetVisualConfig(defaultPlanetVisualConfig()));
        return coldGameUiControlConfig;
    }

    private GroundSkeletonConfig defaultGroundSkeletonConfig() {
        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        groundSkeletonConfig.setSplattingXCount(1);
        groundSkeletonConfig.setSplattingYCount(1);
        groundSkeletonConfig.setSplattings(new double[][]{{1, 1}, {1, 1}});
        groundSkeletonConfig.setHeightXCount(1);
        groundSkeletonConfig.setHeightYCount(1);
        groundSkeletonConfig.setHeights(new double[][]{{1, 1}, {1, 1}});
        // TODO groundSkeletonConfig.setTopTextureId(180844);
        // TODO groundSkeletonConfig.setTopTextureScale(0.05);
        // TODO groundSkeletonConfig.setBottomTextureId(180847);
        // TODO groundSkeletonConfig.setBottomTextureScale(0.05);
        // TODO groundSkeletonConfig.setBottomBmId(180848);
        // TODO groundSkeletonConfig.setBottomBmDepth(5.04);
        // TODO groundSkeletonConfig.setBottomBmScale(0.025);
        groundSkeletonConfig.setSplattingId(180846);
        groundSkeletonConfig.setSplattingScale(0.01);
        groundSkeletonConfig.setSpecularLightConfig(new SpecularLightConfig());
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
        return gameTipVisualConfig;
    }

    private PlanetVisualConfig defaultPlanetVisualConfig() {
        PlanetVisualConfig planetVisualConfig = new PlanetVisualConfig();
        planetVisualConfig.setShadowAlpha(0.2);
        planetVisualConfig.setDiffuse(new Color(0.5, 0.5, 0.5));
        planetVisualConfig.setAmbient(new Color(0.5, 0.5, 0.5));
        planetVisualConfig.setLightDirection(new Vertex(0, 0, -1));
        return planetVisualConfig;
    }

    private WaterConfig defaultWaterConfig() {
        WaterConfig waterConfig = new WaterConfig();
        waterConfig.setGroundLevel(-2).setTransparency(0.65).setNormMapId(272480);
        SpecularLightConfig specularLightConfig = new SpecularLightConfig();
        specularLightConfig.setSpecularIntensity(1.0).setSpecularHardness(0.5);
        return waterConfig.setSpecularLightConfig(specularLightConfig);
    }

    private PlanetConfig defaultPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setHouseSpace(10);
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 64, 64));
        planetConfig.setPlayGround(new Rectangle2D(50, 40, 310, 320));
        planetConfig.setStartRazarion(550);
        return planetConfig;
    }

    private List<SceneConfig> defaultSceneConfigs() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        sceneConfigs.add(new SceneConfig().setInternalName("_experimental default scene").setRemoveLoadingCover(true).setViewFieldConfig(new ViewFieldConfig().setToPosition(new DecimalPosition(200, 200))));
        return sceneConfigs;
    }
}

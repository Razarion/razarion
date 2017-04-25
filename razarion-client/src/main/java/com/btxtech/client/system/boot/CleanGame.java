package com.btxtech.client.system.boot;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.effects.TrailService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ItemMarkerService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.mouse.CursorService;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.projectile.ProjectileUiService;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Inject;

/**
 * Created by Beat
 * 25.04.2017.
 */
public class CleanGame extends AbstractStartupTask {
    @Inject
    private RenderService renderService;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ClientPerformanceTrackerService clientPerformanceTrackerService;
    @Inject
    private PerfmonService perfmonService;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private BoxUiService boxUiService;
    @Inject
    private ItemMarkerService itemMarkerService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;
    @Inject
    private CursorService cursorService;
    @Inject
    private ParticleService particleService;
    @Inject
    private ProjectileUiService projectileUiService;
    @Inject
    private TrailService trailService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private AudioService audioService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        gameEngineControl.stop(deferredStartup);
        gameCanvas.stopRenderLoop();
        clientPerformanceTrackerService.stop();
        perfmonService.stop();
        baseItemUiService.clear();
        boxUiService.clear();
        itemMarkerService.clear();
        resourceUiService.clear();
        terrainMouseHandler.clear();
        cursorService.clear();
        particleService.clear();
        projectileUiService.clear();
        trailService.clear();
        terrainUiService.clear();
        audioService.muteTerrainLoopAudio();
    }
}

package com.btxtech.client.system.boot;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.system.LifecycleService;
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
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Dependent
public class CleanGameTask extends AbstractStartupTask {
    @Inject
    private LifecycleService lifecycleService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        lifecycleService.clearAndHold(deferredStartup);
    }
}

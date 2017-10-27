package com.btxtech.client.system;

import com.btxtech.client.ClientTrackerService;
import com.btxtech.client.cockpit.ClientScreenCoverImpl;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.sidebar.LeftSideBarManager;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.system.boot.GameStartupSeq;
import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.effects.TrailService;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ItemMarkerService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.mouse.CursorService;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.projectile.ProjectileUiService;
import com.btxtech.uiservice.system.boot.ClientRunner;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.client.Browser;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 10.09.2017.
 */
@Singleton
public class LifecycleService {
    @Inject
    private ClientRunner clientRunner;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ClientTrackerService clientTrackerService;
    @Inject
    private ClientScreenCoverImpl clientScreenCover;
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
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private ScreenCover screenCover;
    @Inject
    private LeftSideBarManager leftSideBarManager;

    @PostConstruct
    public void postConstruct() {
        clientRunner.addStartupProgressListener(clientTrackerService);
        clientRunner.addStartupProgressListener(clientScreenCover);
    }

    public void startCold() {
        try {
            clientRunner.start(GameStartupSeq.COLD);
            // clientRunner.start(GameStartupSeq.COLD_EXPERIMENTAL);
        } catch (Throwable throwable) {
            exceptionHandler.handleException("Start failed", throwable);
        }
    }

    public void startWarm() {
        // GameUiControl does not use LifecycleService. It uses the ClientRunner for startWarm() directly. This is wrong.
        try {
            clientRunner.start(GameStartupSeq.WARM);
        } catch (Throwable throwable) {
            exceptionHandler.handleException("Start failed", throwable);
        }
    }

    public void onLifecyclePacket(LifecyclePacket lifecyclePacket) {
        switch (lifecyclePacket.getType()) {
            case HOLD:
                clearAndHold(null);
                if (lifecyclePacket.getDialog() == LifecyclePacket.Dialog.PLANET_RESTART) {
                    modalDialogManager.showSingleNoClosableDialog(I18nHelper.getConstants().planetRestartTitle(), I18nHelper.getConstants().planetRestartMessage());
                }
                break;
            case SHUTDOWN:
                // clearAndHold(null);
                // gameUiControl.closeConnection();
                // break;
                throw new UnsupportedOperationException("LifecycleService.onLifecyclePacket() SHUTDOWN not implemented");
            case RESTART_WARM:
                screenCover.fadeInLoadingCover();
                startWarm();
                break;
            case RESTART_COLD:
                Browser.getWindow().getLocation().reload();
                break;
            default:
                throw new IllegalArgumentException("LifecycleService.onLifecyclePacket() Unknown type: " + lifecyclePacket.getType());
        }
    }

    public void clearAndHold(DeferredStartup deferredStartup) {
        gameEngineControl.stop(deferredStartup);
        modalDialogManager.closeAll();
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
        terrainScrollHandler.cleanup();
        leftSideBarManager.close();
    }
}

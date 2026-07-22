package com.btxtech.client;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsDocument;
import com.btxtech.client.jso.JsWindow;
import com.btxtech.client.system.boot.AngularStartupListener;
import com.btxtech.client.system.boot.GameStartupSeq;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.effects.TrailService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;
import com.btxtech.uiservice.terrain.TerrainUiService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class TeaVMLifecycleService {
    private final Boot boot;
    private final TeaVMClientTrackerService clientTrackerService;
    private final PerfmonService perfmonService;
    private final Provider<GameEngineControl> gameEngineControl;
    private final BaseItemUiService baseItemUiService;
    private final BoxUiService boxUiService;
    private final ResourceUiService resourceUiService;
    private final TrailService trailService;
    private final TerrainUiService terrainUiService;
    private final Provider<ScreenCover> screenCover;
    private final GameUiControl gameUiControl;
    private final SimpleExecutorService simpleExecutorService;
    private final TeaVMGwtAngularService gwtAngularService;
    private boolean beforeUnload;
    // True once the server announced a restart, so a following disconnect can be explained as
    // "server is restarting" rather than a generic connection loss.
    private boolean serverRestartAnnounced;

    @Inject
    public TeaVMLifecycleService(TeaVMGwtAngularService gwtAngularService,
                                 SimpleExecutorService simpleExecutorService,
                                 GameUiControl gameUiControl,
                                 Provider<ScreenCover> screenCover,
                                 TerrainUiService terrainUiService,
                                 TrailService trailService,
                                 ResourceUiService resourceUiService,
                                 BoxUiService boxUiService,
                                 BaseItemUiService baseItemUiService,
                                 Provider<GameEngineControl> gameEngineControl,
                                 PerfmonService perfmonService,
                                 TeaVMClientTrackerService clientTrackerService,
                                 Boot boot) {
        this.gwtAngularService = gwtAngularService;
        this.simpleExecutorService = simpleExecutorService;
        this.gameUiControl = gameUiControl;
        this.screenCover = screenCover;
        this.terrainUiService = terrainUiService;
        this.trailService = trailService;
        this.resourceUiService = resourceUiService;
        this.boxUiService = boxUiService;
        this.baseItemUiService = baseItemUiService;
        this.gameEngineControl = gameEngineControl;
        this.perfmonService = perfmonService;
        this.clientTrackerService = clientTrackerService;
        this.boot = boot;

        boot.addStartupProgressListener(clientTrackerService);
        boot.addStartupProgressListener(new StartupProgressListener() {
            @Override
            public void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
                gwtAngularService.onCrash();
                screenCover.get().removeLoadingCover();
            }
        });
        JsDocument.addBeforeUnloadListener(() -> beforeUnload = true);
    }

    public void startCold() {
        try {
            boot.addStartupProgressListener(new AngularStartupListener(GameStartupSeq.COLD));
            boot.start(GameStartupSeq.COLD);
        } catch (Throwable throwable) {
            JsConsole.error("Start cold failed: " + throwable.getMessage());
        }
    }

    public void startWarm() {
        try {
            boot.addStartupProgressListener(new AngularStartupListener(GameStartupSeq.WARM));
            boot.start(GameStartupSeq.WARM);
        } catch (Throwable throwable) {
            JsConsole.error("Start warm failed: " + throwable.getMessage());
        }
    }

    public void onLifecyclePacket(LifecyclePacket lifecyclePacket) {
        switch (lifecyclePacket.getType()) {
            case HOLD:
                clearAndHold(null);
                if (lifecyclePacket.getDialog() == LifecyclePacket.Dialog.PLANET_RESTART) {
                    // The planet restarts within seconds; tell the player instead of just freezing.
                    gwtAngularService.onServerUnavailable(true);
                }
                break;
            case RESTART:
                handleServerRestart();
                break;
            case PLANET_RESTART_WARM:
                screenCover.get().fadeInLoadingCover();
                startWarm();
                break;
            case PLANET_RESTART_COLD:
                JsWindow.reload();
                break;
            case SERVER_RESTART_ANNOUNCEMENT:
                onServerRestartAnnouncement(lifecyclePacket.getRestartInSeconds());
                break;
            default:
                throw new IllegalArgumentException("TeaVMLifecycleService.onLifecyclePacket() Unknown type: " + lifecyclePacket.getType());
        }
    }

    /**
     * The server is going down in the given number of seconds. Nothing is stopped here on purpose:
     * the player keeps playing until the connection actually drops.
     */
    private void onServerRestartAnnouncement(Integer restartInSeconds) {
        if (restartInSeconds == null) {
            serverRestartAnnounced = false;
            gwtAngularService.onServerRestartCancelled();
        } else {
            serverRestartAnnounced = true;
            gwtAngularService.onServerRestartAnnounced(restartInSeconds);
        }
    }

    public void handleServerRestart() {
        if (beforeUnload) {
            return;
        }
        clearAndHold(null);
        gameUiControl.closeConnection();
        gwtAngularService.onServerUnavailable(true);
    }

    public void clearAndHold(DeferredStartup deferredStartup) {
        gameEngineControl.get().stop(() -> {
            baseItemUiService.clear();
            boxUiService.clear();
            resourceUiService.clear();
            if (deferredStartup != null) {
                deferredStartup.finished();
            }
        });
        perfmonService.stop();
        trailService.clear();
        terrainUiService.clear();
    }

    /**
     * Reached after the web socket wrapper gave up reconnecting. The game cannot continue without
     * the server, so everything is stopped and Angular takes over: it shows a blocking overlay and
     * reloads the page as soon as the server answers again. A crash and an unannounced restart look
     * identical from here, which is fine — the recovery is the same.
     */
    public void onConnectionLost(String websocketName) {
        if (beforeUnload) {
            return;
        }
        JsConsole.error("Connection lost on websocket: '" + websocketName + "'");
        clearAndHold(null);
        gwtAngularService.onServerUnavailable(serverRestartAnnounced);
    }
}

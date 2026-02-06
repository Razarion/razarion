package com.btxtech.client;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsDocument;
import com.btxtech.client.jso.JsWindow;
import com.btxtech.client.system.boot.AngularStartupListener;
import com.btxtech.client.system.boot.GameStartupSeq;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.audio.AudioService;
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
    private final AudioService audioService;
    private final Provider<ScreenCover> screenCover;
    private final GameUiControl gameUiControl;
    private final SelectionService selectionService;
    private final SimpleExecutorService simpleExecutorService;
    private final TeaVMGwtAngularService gwtAngularService;
    private boolean beforeUnload;

    @Inject
    public TeaVMLifecycleService(TeaVMGwtAngularService gwtAngularService,
                                 SimpleExecutorService simpleExecutorService,
                                 SelectionService selectionService,
                                 GameUiControl gameUiControl,
                                 Provider<ScreenCover> screenCover,
                                 AudioService audioService,
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
        this.selectionService = selectionService;
        this.gameUiControl = gameUiControl;
        this.screenCover = screenCover;
        this.audioService = audioService;
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
            default:
                throw new IllegalArgumentException("TeaVMLifecycleService.onLifecyclePacket() Unknown type: " + lifecyclePacket.getType());
        }
    }

    public void handleServerRestart() {
        if (beforeUnload) {
            return;
        }
        clearAndHold(null);
        gameUiControl.closeConnection();
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
        selectionService.clearSelection(true);
        trailService.clear();
        terrainUiService.clear();
        audioService.muteTerrainLoopAudio();
    }

    public void onConnectionLost(String websocketName) {
        JsConsole.error("Connection lost on websocket: '" + websocketName + "'. Restarting browser");
        // TODO: implement proper reconnection/restart logic
    }
}

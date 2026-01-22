package com.btxtech.client.system;

import com.btxtech.client.ClientTrackerService;
import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.system.boot.GameStartupSeq;
import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.ServerState;
import com.btxtech.shared.deprecated.Caller;
import com.btxtech.shared.rest.ServerMgmtController;
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
import com.btxtech.uiservice.user.UserUiService;
import elemental2.dom.DomGlobal;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 10.09.2017.
 */
@Singleton
public class LifecycleService {
    private static final int WATCHDOG_DELAY = 5000;
    private static final int RESTART_DELAY = 3000;
    private final Logger logger = Logger.getLogger(LifecycleService.class.getName());
    private final Boot boot;
    private final ClientTrackerService clientTrackerService;
    private final ClientPerformanceTrackerService clientPerformanceTrackerService;
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
    private final Caller<ServerMgmtController> serverMgmt;
    private final UserUiService userUiService;
    private final GwtAngularService gwtAngularService;
    private Consumer<ServerState> serverRestartCallback;
    // private SimpleScheduledFuture simpleScheduledFuture;
    private boolean beforeUnload;

    @Inject
    public LifecycleService(GwtAngularService gwtAngularService,
                            UserUiService userUiService,
                            Caller<ServerMgmtController> serverMgmt,
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
                            ClientPerformanceTrackerService clientPerformanceTrackerService,
                            ClientTrackerService clientTrackerService,
                            Boot boot) {
        this.gwtAngularService = gwtAngularService;
        this.userUiService = userUiService;
        this.serverMgmt = serverMgmt;
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
        this.clientPerformanceTrackerService = clientPerformanceTrackerService;
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
        DomGlobal.document.addEventListener("beforeunload", evt -> beforeUnload = true);
    }

    public void startCold() {
        try {
            boot.addStartupProgressListener(new AngularStartupListener(GameStartupSeq.COLD));
            boot.start(GameStartupSeq.COLD);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "Start cols failed", throwable);
        }
    }

    public void startWarm() {
        // GameUiControl does not use LifecycleService. It uses the Boot for startWarm() directly. This is wrong.
        try {
            boot.addStartupProgressListener(new AngularStartupListener(GameStartupSeq.WARM));
            boot.start(GameStartupSeq.WARM);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "Start warm failed", throwable);
        }
    }

    public void onLifecyclePacket(LifecyclePacket lifecyclePacket) {
        switch (lifecyclePacket.getType()) {
            case HOLD:
                clearAndHold(null);
                if (lifecyclePacket.getDialog() == LifecyclePacket.Dialog.PLANET_RESTART) {
                    // TODO modalDialogManager.showSingleNoClosableDialog(I18nHelper.getConstants().planetRestartTitle(), I18nHelper.getConstants().planetRestartMessage());
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
                DomGlobal.window.location.reload();
                break;
            default:
                throw new IllegalArgumentException("LifecycleService.onLifecyclePacket() Unknown type: " + lifecyclePacket.getType());
        }
    }

    public void handleServerRestart() {
        if (beforeUnload) {
            return;
        }
        clearAndHold(null);
        gameUiControl.closeConnection();
        // TODO modalDialogManager.showSingleNoClosableServerRestartDialog();
        // TODO startRestartWatchdog();
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
        // TODO modalDialogManager.closeAll();
        // TODO gameCanvas.stopRenderLoop();
        clientPerformanceTrackerService.stop();
        perfmonService.stop();
        selectionService.clearSelection(true);
        trailService.clear();
        terrainUiService.clear();
        audioService.muteTerrainLoopAudio();
        // TODO terrainScrollHandler.cleanup();
        // TODO ??? leftSideBarManager.close();
    }

    private void startRestartWatchdog() {
//        simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(WATCHDOG_DELAY, true, serverMgmt.call((RemoteCallback<String>) serverStateString -> {
//            try {
//                ServerState serverState = ServerState.valueOf(serverStateString);
//                if (serverState == ServerState.RUNNING) {
//                    if (simpleScheduledFuture != null) {
//                        simpleScheduledFuture.cancel();
//                        simpleScheduledFuture = null;
//                    }
//                    if (serverRestartCallback != null) {
//                        serverRestartCallback.accept(ServerState.RUNNING);
//                    }
//                    Window.Location.replace("/");
//                } else {
//                    if (serverRestartCallback != null) {
//                        serverRestartCallback.accept(serverState);
//                    }
//                }
//            } catch (Throwable t) {
//                logger.log(Level.WARNING, t.getMessage(), t);
//            }
//        }, (message, throwable) -> {
//            if (serverRestartCallback != null) {
////                if (throwable instanceof ResponseException) {
////                    ResponseException responseException = (ResponseException) throwable;
////                    if (responseException.getResponse().getStatusCode() == Response.SC_NOT_FOUND) {
////                        serverRestartCallback.accept(ServerState.STARTING);
////                        return false;
////                    }
////                }
//                serverRestartCallback.accept(ServerState.SHUTTING_DOWN);
//            }
//            return false;
//        })::getServerStatus, SimpleExecutorService.Type.SERVER_RESTART_WATCHDOG);
    }

    public void setServerRestartCallback(Consumer<ServerState> serverRestartCallback) {
        this.serverRestartCallback = serverRestartCallback;
    }

    public void onConnectionLost(String websocketName) {
        logger.severe("Connection lost on websocket: '" + websocketName + "'. Restarting browser");
//   TODO     serverMgmt.call((RemoteCallback<String>) serverStateString -> {
//            try {
//                ServerState serverState = ServerState.valueOf(serverStateString);
//                if (serverState == ServerState.RUNNING) {
//                    // TODO modalDialogManager.showSingleNoClosableDialog(I18nHelper.getConstants().connectionFailed(), I18nHelper.getConstants().connectionLost());
//                    simpleExecutorService.schedule(RESTART_DELAY, () -> Window.Location.replace("/"), SimpleExecutorService.Type.WAIT_RESTART);
//                } else {
//                    handleServerRestart();
//                }
//            } catch (Throwable t) {
//                exceptionHandler.handleException(t);
//            }
//        }, (message, throwable) -> {
//            handleServerRestart();
//            return false;
//        }).getServerStatus();
    }
}

package com.btxtech.client.system;

import com.btxtech.client.ClientTrackerService;
import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.system.boot.GameStartupSeq;
import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.ServerState;
import com.btxtech.shared.rest.ServerMgmtProvider;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.SelectionHandler;
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
import com.google.gwt.user.client.Window;
import elemental2.dom.DomGlobal;
import com.btxtech.client.Caller;
import com.btxtech.client.RemoteCallback;

import javax.annotation.PostConstruct;
import javax.inject.Provider;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;
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

    private Boot boot;

    private ExceptionHandler exceptionHandler;

    private ClientTrackerService clientTrackerService;

    private ClientPerformanceTrackerService clientPerformanceTrackerService;

    private PerfmonService perfmonService;

    private GameEngineControl gameEngineControl;

    private BaseItemUiService baseItemUiService;

    private BoxUiService boxUiService;

    private ResourceUiService resourceUiService;

    private TrailService trailService;

    private TerrainUiService terrainUiService;

    private AudioService audioService;

    private Provider<ScreenCover> screenCover;

    private GameUiControl gameUiControl;

    private SelectionHandler selectionHandler;

    private SimpleExecutorService simpleExecutorService;

    private Caller<ServerMgmtProvider> serverMgmt;

    private UserUiService userUiService;

    private GwtAngularService gwtAngularService;
    private Consumer<ServerState> serverRestartCallback;
    private SimpleScheduledFuture simpleScheduledFuture;
    private boolean beforeUnload;

    @Inject
    public LifecycleService(GwtAngularService gwtAngularService, UserUiService userUiService, Caller<com.btxtech.shared.rest.ServerMgmtProvider> serverMgmt, SimpleExecutorService simpleExecutorService, SelectionHandler selectionHandler, GameUiControl gameUiControl, Provider<com.btxtech.uiservice.cockpit.ScreenCover> screenCover, AudioService audioService, TerrainUiService terrainUiService, TrailService trailService, ResourceUiService resourceUiService, BoxUiService boxUiService, BaseItemUiService baseItemUiService, GameEngineControl gameEngineControl, PerfmonService perfmonService, ClientPerformanceTrackerService clientPerformanceTrackerService, ClientTrackerService clientTrackerService, ExceptionHandler exceptionHandler, Boot boot) {
        this.gwtAngularService = gwtAngularService;
        this.userUiService = userUiService;
        this.serverMgmt = serverMgmt;
        this.simpleExecutorService = simpleExecutorService;
        this.selectionHandler = selectionHandler;
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
        this.exceptionHandler = exceptionHandler;
        this.boot = boot;
    }

    @PostConstruct
    public void postConstruct() {
        boot.addStartupProgressListener(clientTrackerService);
        boot.addStartupProgressListener(new StartupProgressListener() {
            @Override
            public void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
                if (userUiService.isAdmin()) {
                    gwtAngularService.onCrash();
                    screenCover.get().removeLoadingCover();
                }
            }
        });
        DomGlobal.document.addEventListener("beforeunload", evt -> beforeUnload = true);
    }

    public void startCold() {
        try {
            boot.start(GameStartupSeq.COLD);
        } catch (Throwable throwable) {
            exceptionHandler.handleException("Start failed", throwable);
        }
    }

    public void startWarm() {
        // GameUiControl does not use LifecycleService. It uses the Boot for startWarm() directly. This is wrong.
        try {
            boot.start(GameStartupSeq.WARM);
        } catch (Throwable throwable) {
            exceptionHandler.handleException("Start failed", throwable);
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
        startRestartWatchdog();
    }

    public void clearAndHold(DeferredStartup deferredStartup) {
        gameEngineControl.stop(() -> {
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
        selectionHandler.clearSelection(true);
        trailService.clear();
        terrainUiService.clear();
        audioService.muteTerrainLoopAudio();
        // TODO terrainScrollHandler.cleanup();
        // TODO ??? leftSideBarManager.close();
    }

    private void startRestartWatchdog() {
        simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(WATCHDOG_DELAY, true, serverMgmt.call((RemoteCallback<String>) serverStateString -> {
            try {
                ServerState serverState = ServerState.valueOf(serverStateString);
                if (serverState == ServerState.RUNNING) {
                    if (simpleScheduledFuture != null) {
                        simpleScheduledFuture.cancel();
                        simpleScheduledFuture = null;
                    }
                    if (serverRestartCallback != null) {
                        serverRestartCallback.accept(ServerState.RUNNING);
                    }
                    Window.Location.replace("/");
                } else {
                    if (serverRestartCallback != null) {
                        serverRestartCallback.accept(serverState);
                    }
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }, (message, throwable) -> {
            if (serverRestartCallback != null) {
//                if (throwable instanceof ResponseException) {
//                    ResponseException responseException = (ResponseException) throwable;
//                    if (responseException.getResponse().getStatusCode() == Response.SC_NOT_FOUND) {
//                        serverRestartCallback.accept(ServerState.STARTING);
//                        return false;
//                    }
//                }
                serverRestartCallback.accept(ServerState.SHUTTING_DOWN);
            }
            return false;
        })::getServerStatus, SimpleExecutorService.Type.SERVER_RESTART_WATCHDOG);
    }

    public void setServerRestartCallback(Consumer<ServerState> serverRestartCallback) {
        this.serverRestartCallback = serverRestartCallback;
    }

    public void onConnectionLost(String websocketName) {
        logger.severe("Connection lost on websocket: '" + websocketName + "'. Restarting browser");
        serverMgmt.call((RemoteCallback<String>) serverStateString -> {
            try {
                ServerState serverState = ServerState.valueOf(serverStateString);
                if (serverState == ServerState.RUNNING) {
                    // TODO modalDialogManager.showSingleNoClosableDialog(I18nHelper.getConstants().connectionFailed(), I18nHelper.getConstants().connectionLost());
                    simpleExecutorService.schedule(RESTART_DELAY, () -> Window.Location.replace("/"), SimpleExecutorService.Type.WAIT_RESTART);
                } else {
                    handleServerRestart();
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }, (message, throwable) -> {
            handleServerRestart();
            return false;
        }).getServerStatus();
    }
}

package com.btxtech.client.system;

import com.btxtech.client.ClientTrackerService;
import com.btxtech.client.cockpit.ClientScreenCoverImpl;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.renderer.GameCanvas;
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
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ItemMarkerService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.mouse.CursorService;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.projectile.ProjectileUiService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import elemental.client.Browser;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;

import javax.annotation.PostConstruct;
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
    private Logger logger = Logger.getLogger(LifecycleService.class.getName());
    @Inject
    private Boot boot;
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
    private GameUiControl gameUiControl;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private Caller<ServerMgmtProvider> serverMgmt;
    @Inject
    private UserUiService userUiService;
    @Inject
    private GwtAngularService gwtAngularService;
    private Consumer<ServerState> serverRestartCallback;
    private SimpleScheduledFuture simpleScheduledFuture;
    private boolean beforeUnload;

    @PostConstruct
    public void postConstruct() {
        boot.addStartupProgressListener(clientTrackerService);
        boot.addStartupProgressListener(clientScreenCover);
        boot.addStartupProgressListener(new StartupProgressListener() {
            @Override
            public void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
                if(userUiService.isAdmin()) {
                    gwtAngularService.onCrash();
                }
            }
        });
        Browser.getDocument().addEventListener("beforeunload", evt -> beforeUnload = true);
    }

    public void startCold() {
        try {
            boot.start(GameStartupSeq.COLD);
            // boot.start(GameStartupSeq.COLD_EXPERIMENTAL);
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
                    modalDialogManager.showSingleNoClosableDialog(I18nHelper.getConstants().planetRestartTitle(), I18nHelper.getConstants().planetRestartMessage());
                }
                break;
            case RESTART:
                handleServerRestart();
                break;
            case PLANET_RESTART_WARM:
                screenCover.fadeInLoadingCover();
                startWarm();
                break;
            case PLANET_RESTART_COLD:
                Browser.getWindow().getLocation().reload();
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
        modalDialogManager.showSingleNoClosableServerRestartDialog();
        startRestartWatchdog();
    }

    public void clearAndHold(DeferredStartup deferredStartup) {
        gameEngineControl.stop(() -> {
            baseItemUiService.clear();
            boxUiService.clear();
            resourceUiService.clear();
            deferredStartup.finished();
        });
        modalDialogManager.closeAll();
        gameCanvas.stopRenderLoop();
        clientPerformanceTrackerService.stop();
        perfmonService.stop();
        selectionHandler.clearSelection(true);
        itemMarkerService.clear();
        terrainMouseHandler.clear();
        cursorService.clear();
        particleService.clear();
        projectileUiService.clear();
        trailService.clear();
        terrainUiService.clear();
        audioService.muteTerrainLoopAudio();
        terrainScrollHandler.cleanup();
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
                if (throwable instanceof ResponseException) {
                    ResponseException responseException = (ResponseException) throwable;
                    if (responseException.getResponse().getStatusCode() == Response.SC_NOT_FOUND) {
                        serverRestartCallback.accept(ServerState.STARTING);
                        return false;
                    }
                }
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
                    modalDialogManager.showSingleNoClosableDialog(I18nHelper.getConstants().connectionFailed(), I18nHelper.getConstants().connectionLost());
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

package com.btxtech.client;

import com.btxtech.client.system.LifecycleService;
import com.btxtech.common.WorkerMarshaller;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.user.UserUiService;
import elemental2.dom.ErrorEvent;
import elemental2.dom.Worker;

import javax.inject.Singleton;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.01.2017.
 */
@Singleton
public class ClientGameEngineControl extends GameEngineControl {
    private final Logger logger = Logger.getLogger(ClientGameEngineControl.class.getName());

    private ExceptionHandler exceptionHandler;

    private Provider<LifecycleService> lifecycleService;
    private Worker worker;
    private DeferredStartup deferredStartup;
    private QueueStatistics queueStatistics;

    @Inject
    public ClientGameEngineControl(Provider<InputService> inputServices, PerfmonService perfmonService, Boot boot, TerrainUiService terrainUiService, InventoryUiService inventoryUiService, UserUiService userUiService, SelectionHandler selectionHandler, GameUiControl gameUiControl, AudioService audioService, BoxUiService boxUiService, ResourceUiService resourceUiService, BaseItemUiService baseItemUiService, Provider<com.btxtech.client.system.LifecycleService> lifecycleService, ExceptionHandler exceptionHandler) {
        super(inputServices, perfmonService, exceptionHandler, boot, terrainUiService, inventoryUiService, userUiService, selectionHandler, gameUiControl, audioService, boxUiService, resourceUiService, baseItemUiService);
        this.lifecycleService = lifecycleService;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public boolean isStarted() {
        return worker != null;
    }

    public void loadWorker(DeferredStartup deferredStartup) {
        this.deferredStartup = deferredStartup;
        try {
            worker = new Worker(CommonUrl.getWorkerScriptUrl());
            worker.onmessage = messageEvent -> {
                Object data = null;
                try {
                    data = messageEvent.data;
                    GameEngineControlPackage controlPackage = WorkerMarshaller.deMarshall(data);
                    dispatch(controlPackage);
                    if (queueStatistics != null) {
                        queueStatistics.received(controlPackage.getCommand());
                    }
                } catch (Throwable t) {
                    exceptionHandler.handleException("ClientGameEngineControl: exception processing package on client. Data: " + data, t);
                }
            };
            worker.onerror = this::handleErrors;
        } catch (Throwable t) {
            this.deferredStartup.failed(t);
            this.deferredStartup = null;
        }
    }

    private void handleErrors(ErrorEvent errorEvent) {
        logger.severe("ClientGameEngineControl handleErrors. Message: \"" + errorEvent.message + "\". FileName: " + errorEvent.filename + ". LineNo: " + errorEvent.lineno);
    }

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object... data) {
        if (queueStatistics != null) {
            queueStatistics.send(command);
        }
        try {
            worker.postMessage(WorkerMarshaller.marshall(new GameEngineControlPackage(command, data)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    protected void onLoaded() {
        deferredStartup.finished();
        deferredStartup = null;
    }

    @Override
    public void enableTracking() {
        queueStatistics = new QueueStatistics();
    }

    @Override
    protected void onConnectionLost() {
        lifecycleService.get().onConnectionLost("ClientServerGameConnection");
    }

    @Override
    protected native NativeTickInfo castToNativeTickInfo(Object javaScriptObject) /*-{
        return javaScriptObject;
    }-*/;

    @Override
    protected native NativeSyncBaseItemTickInfo castToNativeSyncBaseItemTickInfo(Object javaScriptObject) /*-{
        return javaScriptObject;
    }-*/;
}

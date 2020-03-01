package com.btxtech.client;

import com.btxtech.client.system.LifecycleService;
import com.btxtech.common.WorkerMarshaller;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import elemental2.dom.ErrorEvent;
import elemental2.dom.Worker;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.01.2017.
 */
@ApplicationScoped
public class ClientGameEngineControl extends GameEngineControl {
    private Logger logger = Logger.getLogger(ClientGameEngineControl.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    @Inject
    private Instance<LifecycleService> lifecycleService;
    private Worker worker;
    private DeferredStartup deferredStartup;
    private QueueStatistics queueStatistics;

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
                    GameEngineControlPackage controlPackage = WorkerMarshaller.deMarshall(data, nativeMatrixFactory);
                    dispatch(controlPackage);
                    if (queueStatistics != null) {
                        queueStatistics.received(controlPackage.getCommand());
                    }
                } catch (Throwable t) {
                    exceptionHandler.handleException("ClientGameEngineControl: exception processing package on client. Data: " + data, t);
                }
                return null;
            };
            worker.onerror = event -> {
                handleErrors((ErrorEvent) event);
                return null;
            };
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

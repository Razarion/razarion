package com.btxtech.client;

import com.btxtech.common.WorkerMarshaller;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import elemental.client.Browser;
import elemental.events.ErrorEvent;
import elemental.events.MessageEvent;
import elemental.html.Worker;

import javax.enterprise.context.ApplicationScoped;
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
            worker = Browser.getWindow().newWorker(CommonUrl.getWorkerScriptUrl());
            worker.setOnmessage(event -> {
                try {
                    MessageEvent messageEvent = (MessageEvent) event;
                    GameEngineControlPackage controlPackage = WorkerMarshaller.deMarshall(messageEvent.getData(), nativeMatrixFactory);
                    dispatch(controlPackage);
                    if (queueStatistics != null) {
                        queueStatistics.received(controlPackage.getCommand());
                    }
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            });
            worker.setOnerror(event -> handleErrors((ErrorEvent) event));
        } catch (Throwable t) {
            this.deferredStartup.failed(t);
            this.deferredStartup = null;
        }
    }

    private void handleErrors(ErrorEvent errorEvent) {
        logger.severe("ClientGameEngineControl handleErrors. Message: " + errorEvent.getMessage() + " FileName: " + errorEvent.getFilename() + " LineNo: " + errorEvent.getLineno());
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
    protected native NativeTickInfo castToNativeTickInfo(Object javaScriptObject) /*-{
        return javaScriptObject;
    }-*/;

    @Override
    protected native NativeSyncBaseItemTickInfo castToNativeSyncBaseItemTickInfo(Object javaScriptObject) /*-{
        return javaScriptObject;
    }-*/;
}

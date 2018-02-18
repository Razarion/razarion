package com.btxtech.worker;


import com.btxtech.common.WorkerMarshaller;
import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.ExceptionHandler;
import elemental.events.MessageEvent;
import elemental.js.html.JsDedicatedWorkerGlobalScope;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 30.12.2016.
 */
@EntryPoint
public class ClientGameEngineWorker extends GameEngineWorker {
    // private Logger logger = Logger.getLogger(ClientGameEngineWorker.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ClientPerformanceTrackerService clientPerformanceTrackerService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;

    @PostConstruct
    public void onModuleLoad() {
        RestClient.setApplicationRoot(CommonUrl.getWorkerApplicationRoot());
        getDedicatedWorkerGlobalScope().setOnmessage(evt -> {
            Object data = null;
            try {
                MessageEvent messageEvent = (MessageEvent) evt;
                data = messageEvent.getData();
                GameEngineControlPackage controlPackage = WorkerMarshaller.deMarshall(messageEvent.getData(), nativeMatrixFactory);
                dispatch(controlPackage);
            } catch (Throwable t) {
                exceptionHandler.handleException("ClientGameEngineWorker: exception processing package on worker. Data: " + data, t);
            }
        });
        sendToClient(GameEngineControlPackage.Command.LOADED);
    }

    @Override
    public void start() {
        super.start();
        clientPerformanceTrackerService.start();
    }

    @Override
    public void stop() {
        clientPerformanceTrackerService.stop();
        super.stop();
    }

    @Override
    protected void sendToClient(GameEngineControlPackage.Command command, Object... object) {
        getDedicatedWorkerGlobalScope().postMessage(WorkerMarshaller.marshall(new GameEngineControlPackage(command, object)));
    }

    public static native JsDedicatedWorkerGlobalScope getDedicatedWorkerGlobalScope() /*-{
        return self;
    }-*/;
}

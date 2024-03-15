package com.btxtech.worker;


import com.btxtech.common.WorkerMarshaller;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.GameEngineWorker;
import elemental2.dom.DedicatedWorkerGlobalScope;
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
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private ClientPerformanceTrackerService clientPerformanceTrackerService;

    @PostConstruct
    public void onModuleLoad() {
        exceptionHandler.registerWindowCloseHandler();
        RestClient.setApplicationRoot(CommonUrl.getWorkerApplicationRoot());
        getDedicatedWorkerGlobalScope().setOnmessage(evt -> {
            try {
                GameEngineControlPackage controlPackage = WorkerMarshaller.deMarshall(evt.data);
                dispatch(controlPackage);
            } catch (Throwable t) {
                exceptionHandler.handleException("ClientGameEngineWorker: exception processing package on worker. Data: " + evt.data, t);
            }
            return null;
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

    public static native DedicatedWorkerGlobalScope getDedicatedWorkerGlobalScope() /*-{
        return self;
    }-*/;
}

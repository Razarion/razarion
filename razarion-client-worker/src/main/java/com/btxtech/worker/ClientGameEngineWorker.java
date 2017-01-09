package com.btxtech.worker;


import com.btxtech.common.WorkerMarshaller;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.system.ExceptionHandler;
import elemental.events.MessageEvent;
import elemental.js.html.JsDedicatedWorkerGlobalScope;
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
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    @PostConstruct
    public void onModuleLoad() {
        getDedicatedWorkerGlobalScope().setOnmessage(evt -> {
            Object data = null;
            try {
                MessageEvent messageEvent = (MessageEvent) evt;
                data = messageEvent.getData();
                GameEngineControlPackage controlPackage = WorkerMarshaller.deMarshall(messageEvent.getData());
                dispatch(controlPackage);
            } catch (Throwable t) {
                exceptionHandler.handleException("data: " + data, t);
            }
        });
    }

    @Override
    protected void sendToClient(GameEngineControlPackage.Command command, Object... object) {
        getDedicatedWorkerGlobalScope().postMessage(WorkerMarshaller.marshall(new GameEngineControlPackage(command, object)));
    }

    public static native JsDedicatedWorkerGlobalScope getDedicatedWorkerGlobalScope() /*-{
        return self;
    }-*/;
}

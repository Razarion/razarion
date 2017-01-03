package com.btxtech.worker;


import com.btxtech.shared.gameengine.GameEngine;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.system.ExceptionHandler;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MessageEvent;
import elemental.html.DedicatedWorkerGlobalScope;
import elemental.js.html.JsDedicatedWorkerGlobalScope;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 30.12.2016.
 */
@EntryPoint
public class MainWorker {
    private Logger logger = Logger.getLogger(MainWorker.class.getName());
    @Inject
    private GameEngine gameEngine;
    @Inject
    private ExceptionHandler exceptionHandler;

    @PostConstruct
    public void onModuleLoad() {
        DedicatedWorkerGlobalScope globalScope = getDedicatedWorkerGlobalScope();


        globalScope.setOnmessage(evt -> {
            try {
                MessageEvent messageEvent = (MessageEvent) evt;
                GameEngineConfig gameEngineConfig;
                RestClient.setJacksonMarshallingActive(false); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
                try {
                    gameEngineConfig = MarshallingWrapper.fromJSON((String) messageEvent.getData(), GameEngineConfig.class);
                } finally {
                    RestClient.setJacksonMarshallingActive(true); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
                }
                gameEngine.initialise(gameEngineConfig);
                getDedicatedWorkerGlobalScope().postMessage("!!!!! RUNNING !!!!!");
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        });

    }

    public static native JsDedicatedWorkerGlobalScope getDedicatedWorkerGlobalScope() /*-{
        return self;
    }-*/;
}

package com.btxtech.client;

import com.btxtech.common.ClientUrls;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.storyboard.GameEngineControl;
import elemental.client.Browser;
import elemental.events.ErrorEvent;
import elemental.events.MessageEvent;
import elemental.html.Worker;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.01.2017.
 */
@ApplicationScoped
public class ClientGameEngineControl implements GameEngineControl {
    private Logger logger = Logger.getLogger(ClientGameEngineControl.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private Worker worker;

    @PostConstruct
    public void postConstruct() {
        try {
            worker = Browser.getWindow().newWorker(ClientUrls.CLIENT_WORKER_SCRIPT);
            worker.setOnmessage(event -> handleMessages((MessageEvent) event));
            worker.setOnerror(event -> handleErrors((ErrorEvent) event));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void handleErrors(ErrorEvent errorEvent) {
        logger.severe("ClientGameEngineControl handleErrors. Message: " + errorEvent.getMessage() + " FileName: " + errorEvent.getFilename() + " LineNo: " + errorEvent.getLineno());
    }

    private void handleMessages(MessageEvent messageEvent) {
        logger.severe("ClientGameEngineControl handleMessages: " + messageEvent.getData());
    }

    @Override
    public void initialise(GameEngineConfig gameEngineConfig) {
        try {
            RestClient.setJacksonMarshallingActive(false); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
            String json;
            try {
                json = MarshallingWrapper.toJSON(gameEngineConfig);
            } finally {
                RestClient.setJacksonMarshallingActive(true); // Bug in Errai Jackson marshaller -> Map<Integer, Integer> sometimes has still "^NumVal" in the Jackson string
            }
            worker.postMessage(json);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public void start() {

    }
}

package com.btxtech.client;

import com.btxtech.common.ClientUrls;
import com.btxtech.common.WorkerMarshaller;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.control.GameEngineControl;
import elemental.client.Browser;
import elemental.events.ErrorEvent;
import elemental.events.MessageEvent;
import elemental.html.Worker;

import javax.annotation.PostConstruct;
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
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private Worker worker;

    @PostConstruct
    public void postConstruct() {
        try {
            worker = Browser.getWindow().newWorker(ClientUrls.CLIENT_WORKER_SCRIPT);
            worker.setOnmessage(event -> {
                try {
                    MessageEvent messageEvent = (MessageEvent) event;
                    logger.severe("ClientGameEngineControl dispatch: " + messageEvent.getData());
                    GameEngineControlPackage controlPackage = WorkerMarshaller.deMarshall(messageEvent.getData());
                    dispatch(controlPackage);
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            });
            worker.setOnerror(event -> handleErrors((ErrorEvent) event));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void handleErrors(ErrorEvent errorEvent) {
        logger.severe("ClientGameEngineControl handleErrors. Message: " + errorEvent.getMessage() + " FileName: " + errorEvent.getFilename() + " LineNo: " + errorEvent.getLineno());
    }

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object data) {
        try {
            worker.postMessage(WorkerMarshaller.marshall(new GameEngineControlPackage(command, data)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}

package com.btxtech.client;

import com.btxtech.common.ClientUrls;
import com.btxtech.common.WorkerMarshaller;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.storyboard.GameEngineControl;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import elemental.client.Browser;
import elemental.events.ErrorEvent;
import elemental.events.MessageEvent;
import elemental.html.Worker;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
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
        try {
            logger.severe("ClientGameEngineControl handleMessages: " + messageEvent.getData());
            GameEngineControlPackage controlPackage = WorkerMarshaller.deMarshall(messageEvent.getData());
            switch (controlPackage.getCommand()) {
                case INITIALIZED:
                    logger.severe("!!!Initialized!!!!"); // TODO
                    break;
                case STARTED:
                    logger.severe("!!!Started!!!!");
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public void initialise(GameEngineConfig gameEngineConfig) {
        dispatchPackage(GameEngineControlPackage.Command.INITIALIZE, gameEngineConfig);
    }

    @Override
    public void start() {
        dispatchPackage(GameEngineControlPackage.Command.START, null);
    }

    @Override
    public void startBots(List<BotConfig> botConfigs) {
        dispatchPackage(GameEngineControlPackage.Command.START_BOTS, botConfigs);
    }

    @Override
    public void executeBotCommands(List<? extends AbstractBotCommandConfig> botCommandConfigs) {
        dispatchPackage(GameEngineControlPackage.Command.EXECUTE_BOT_COMMANDS, botCommandConfigs);
    }

    private void dispatchPackage(GameEngineControlPackage.Command command, Object data) {
        try {
            worker.postMessage(WorkerMarshaller.marshall(new GameEngineControlPackage(command, data)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}

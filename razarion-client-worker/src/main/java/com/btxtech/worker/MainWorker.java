package com.btxtech.worker;


import com.btxtech.common.WorkerMarshaller;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.gameengine.GameEngine;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.system.ExceptionHandler;
import elemental.events.MessageEvent;
import elemental.html.DedicatedWorkerGlobalScope;
import elemental.js.html.JsDedicatedWorkerGlobalScope;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 30.12.2016.
 */
@EntryPoint
public class MainWorker {
    // private Logger logger = Logger.getLogger(MainWorker.class.getName());
    @Inject
    private GameEngine gameEngine;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private BotService botService;

    @PostConstruct
    public void onModuleLoad() {
        DedicatedWorkerGlobalScope globalScope = getDedicatedWorkerGlobalScope();

        globalScope.setOnmessage(evt -> {
            try {
                MessageEvent messageEvent = (MessageEvent) evt;
                GameEngineControlPackage controlPackage = WorkerMarshaller.deMarshall(messageEvent.getData());
                switch (controlPackage.getCommand()) {
                    case INITIALIZE:
                        gameEngine.initialise((GameEngineConfig) controlPackage.getData());
                        dispatchPackage(GameEngineControlPackage.Command.INITIALIZED);
                        break;
                    case START:
                        gameEngine.start();
                        dispatchPackage(GameEngineControlPackage.Command.STARTED);
                        break;
                    case START_BOTS:
                        botService.startBots((Collection<BotConfig>) controlPackage.getData());
                        break;
                    case EXECUTE_BOT_COMMANDS:
                        botService.executeCommands((List<? extends AbstractBotCommandConfig>) controlPackage.getData());
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        });

    }

    private void dispatchPackage(GameEngineControlPackage.Command command) {
        getDedicatedWorkerGlobalScope().postMessage(WorkerMarshaller.marshall(new GameEngineControlPackage(command, null)));
    }

    public static native JsDedicatedWorkerGlobalScope getDedicatedWorkerGlobalScope() /*-{
        return self;
    }-*/;
}

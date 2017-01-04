package com.btxtech.uiservice.control;

import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.01.2017.
 */
public abstract class GameEngineControl {
    private Logger logger = Logger.getLogger(GameEngineControl.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    protected abstract void sendToWorker(GameEngineControlPackage.Command command, Object data);

    public void start() {
        sendToWorker(GameEngineControlPackage.Command.START, null);
    }

    void initialise(GameEngineConfig gameEngineConfig) {
        sendToWorker(GameEngineControlPackage.Command.INITIALIZE, gameEngineConfig);
    }

    void startBots(List<BotConfig> botConfigs) {
        sendToWorker(GameEngineControlPackage.Command.START_BOTS, botConfigs);
    }

    void executeBotCommands(List<? extends AbstractBotCommandConfig> botCommandConfigs) {
        sendToWorker(GameEngineControlPackage.Command.EXECUTE_BOT_COMMANDS, botCommandConfigs);
    }

    protected void dispatch(GameEngineControlPackage controlPackage) {
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
    }

}

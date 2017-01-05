package com.btxtech.shared.gameengine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.bot.BotService;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 18.07.2016.
 */
public abstract class GameEngineWorker {
    @Inject
    private PlanetService planetService;
    @Inject
    private Event<GameEngineInitEvent> gameEngineInitEvent;
    @Inject
    private BotService botService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private BaseItemService baseItemService;

    protected abstract void dispatchPackage(GameEngineControlPackage.Command command);

    public void initialise(GameEngineConfig gameEngineConfig) {
        gameEngineInitEvent.fire(new GameEngineInitEvent(gameEngineConfig));
        planetService.initialise(gameEngineConfig.getPlanetConfig());
    }

    public void start() {
        planetService.start();
    }

    public void stop() {
        planetService.stop();
    }

    protected void dispatch(GameEngineControlPackage controlPackage) {
        switch (controlPackage.getCommand()) {
            case INITIALIZE:
                initialise((GameEngineConfig) controlPackage.getSingleData());
                dispatchPackage(GameEngineControlPackage.Command.INITIALIZED);
                break;
            case INITIALIZED:
                break;
            case START:
                start();
                dispatchPackage(GameEngineControlPackage.Command.STARTED);
                break;
            case STARTED:
                break;
            case START_BOTS:
                botService.startBots((Collection<BotConfig>) controlPackage.getSingleData());
                break;
            case EXECUTE_BOT_COMMANDS:
                botService.executeCommands((List<? extends AbstractBotCommandConfig>) controlPackage.getSingleData());
                break;
            case CREATE_RESOURCES:
                resourceService.createResources((Collection<ResourceItemPosition>) controlPackage.getSingleData());
                break;
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                baseItemService.createHumanBaseWithBaseItem((UserContext) controlPackage.getData(0), (Integer) controlPackage.getData(1), (DecimalPosition) controlPackage.getData(2));
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
    }

}

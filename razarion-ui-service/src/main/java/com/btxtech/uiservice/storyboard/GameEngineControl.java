package com.btxtech.uiservice.storyboard;

import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import java.util.List;

/**
 * Created by Beat
 * 02.01.2017.
 */
public interface GameEngineControl {
    void initialise(GameEngineConfig gameEngineConfig);

    void start();

    void startBots(List<BotConfig> botConfigs);

    void executeBotCommands(List<? extends AbstractBotCommandConfig> botCommandConfigs);
}

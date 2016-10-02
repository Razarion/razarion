package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
public class SceneConfig {
    private String introText;
    private QuestConfig questConfig;
    private CameraConfig cameraConfig;
    private List<BotConfig> botConfigs;
    private List<BotMoveCommandConfig> botMoveCommandConfigs;
    private StartPointConfig startPointConfig;
    private Boolean wait4LevelUp;

    public String getIntroText() {
        return introText;
    }

    public SceneConfig setIntroText(String introText) {
        this.introText = introText;
        return this;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public SceneConfig setCameraConfig(CameraConfig cameraConfig) {
        this.cameraConfig = cameraConfig;
        return this;
    }

    public QuestConfig getQuestConfig() {
        return questConfig;
    }

    public SceneConfig setQuestConfig(QuestConfig questConfig) {
        this.questConfig = questConfig;
        return this;
    }

    public List<BotConfig> getBotConfigs() {
        return botConfigs;
    }

    public SceneConfig setBotConfigs(List<BotConfig> botConfigs) {
        this.botConfigs = botConfigs;
        return this;
    }

    public List<BotMoveCommandConfig> getBotMoveCommandConfigs() {
        return botMoveCommandConfigs;
    }

    public SceneConfig setBotMoveCommandConfigs(List<BotMoveCommandConfig> botMoveCommandConfigs) {
        this.botMoveCommandConfigs = botMoveCommandConfigs;
        return this;
    }

    public StartPointConfig getStartPointConfig() {
        return startPointConfig;
    }

    public SceneConfig setStartPointConfig(StartPointConfig startPointConfig) {
        this.startPointConfig = startPointConfig;
        return this;
    }

    public Boolean isWait4LevelUp() {
        return wait4LevelUp;
    }

    public SceneConfig setWait4LevelUp(Boolean wait4LevelUp) {
        this.wait4LevelUp = wait4LevelUp;
        return this;
    }
}

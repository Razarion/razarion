package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
public class SceneConfig {
    private String introText;
    private boolean showQuestSideBar;
    private CameraConfig cameraConfig;
    private List<BotConfig> botConfigs;
    private List<BotMoveCommandConfig> botMoveCommandConfigs;
    private StartPointConfig startPointConfig;

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

    public boolean isShowQuestSideBar() {
        return showQuestSideBar;
    }

    public SceneConfig setShowQuestSideBar(boolean showQuestSideBar) {
        this.showQuestSideBar = showQuestSideBar;
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
}

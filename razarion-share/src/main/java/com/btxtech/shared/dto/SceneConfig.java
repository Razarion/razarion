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
    private List<BotHarvestCommandConfig> botHarvestCommandConfigs;
    private List<BotAttackCommandConfig> botAttackCommandConfigs;
    private StartPointConfig startPointConfig;
    private Boolean wait4LevelUp;
    private List<ResourceItemPosition> resourceItemTypePositions;
    private Integer duration;
    private ScrollUiQuest scrollUiQuest;
    private List<BoxItemPosition> boxItemPositions;

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

    public List<BotHarvestCommandConfig> getBotHarvestCommandConfigs() {
        return botHarvestCommandConfigs;
    }

    public SceneConfig setBotHarvestCommandConfigs(List<BotHarvestCommandConfig> botHarvestCommandConfigs) {
        this.botHarvestCommandConfigs = botHarvestCommandConfigs;
        return this;
    }

    public List<BotAttackCommandConfig> getBotAttackCommandConfigs() {
        return botAttackCommandConfigs;
    }

    public SceneConfig setBotAttackCommandConfigs(List<BotAttackCommandConfig> botAttackCommandConfigs) {
        this.botAttackCommandConfigs = botAttackCommandConfigs;
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

    public List<ResourceItemPosition> getResourceItemTypePositions() {
        return resourceItemTypePositions;
    }

    public SceneConfig setResourceItemTypePositions(List<ResourceItemPosition> resourceItemTypePositions) {
        this.resourceItemTypePositions = resourceItemTypePositions;
        return this;
    }

    public Integer getDuration() {
        return duration;
    }

    public SceneConfig setDuration(Integer duration) {
        this.duration = duration;
        return this;
    }

    public ScrollUiQuest getScrollUiQuest() {
        return scrollUiQuest;
    }

    public SceneConfig setScrollUiQuest(ScrollUiQuest scrollUiQuest) {
        this.scrollUiQuest = scrollUiQuest;
        return this;
    }

    public List<BoxItemPosition> getBoxItemPositions() {
        return boxItemPositions;
    }

    public SceneConfig setBoxItemPositions(List<BoxItemPosition> boxItemPositions) {
        this.boxItemPositions = boxItemPositions;
        return this;
    }
}

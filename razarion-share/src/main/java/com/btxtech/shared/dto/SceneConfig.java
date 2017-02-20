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
    private ViewFieldConfig viewFieldConfig;
    private List<BotConfig> botConfigs;
    private List<BotMoveCommandConfig> botMoveCommandConfigs;
    private List<BotHarvestCommandConfig> botHarvestCommandConfigs;
    private List<BotAttackCommandConfig> botAttackCommandConfigs;
    private List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs;
    private List<BotKillHumanCommandConfig> botKillHumanCommandConfigs;
    private List<BotRemoveOwnItemCommandConfig> botRemoveOwnItemCommandConfigs;
    private List<KillBotCommandConfig> killBotCommandConfigs;
    private BaseItemPlacerConfig startPointPlacerConfig;
    private Boolean wait4LevelUpDialog;
    private Boolean wait4QuestPassedDialog;
    private Boolean waitForBaseLostDialog;
    private List<ResourceItemPosition> resourceItemTypePositions;
    private Integer duration;
    private ScrollUiQuest scrollUiQuest;
    private List<BoxItemPosition> boxItemPositions;
    private GameTipConfig gameTipConfig;
    private boolean removeLoadingCover;

    public String getIntroText() {
        return introText;
    }

    public SceneConfig setIntroText(String introText) {
        this.introText = introText;
        return this;
    }

    public ViewFieldConfig getViewFieldConfig() {
        return viewFieldConfig;
    }

    public SceneConfig setViewFieldConfig(ViewFieldConfig viewFieldConfig) {
        this.viewFieldConfig = viewFieldConfig;
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

    public List<BotKillOtherBotCommandConfig> getBotKillOtherBotCommandConfigs() {
        return botKillOtherBotCommandConfigs;
    }

    public SceneConfig setBotKillOtherBotCommandConfigs(List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs) {
        this.botKillOtherBotCommandConfigs = botKillOtherBotCommandConfigs;
        return this;
    }

    public List<BotKillHumanCommandConfig> getBotKillHumanCommandConfigs() {
        return botKillHumanCommandConfigs;
    }

    public SceneConfig setBotKillHumanCommandConfigs(List<BotKillHumanCommandConfig> botKillHumanCommandConfigs) {
        this.botKillHumanCommandConfigs = botKillHumanCommandConfigs;
        return this;
    }

    public List<BotRemoveOwnItemCommandConfig> getBotRemoveOwnItemCommandConfigs() {
        return botRemoveOwnItemCommandConfigs;
    }

    public SceneConfig setBotRemoveOwnItemCommandConfigs(List<BotRemoveOwnItemCommandConfig> botRemoveOwnItemCommandConfigs) {
        this.botRemoveOwnItemCommandConfigs = botRemoveOwnItemCommandConfigs;
        return this;
    }

    public List<KillBotCommandConfig> getKillBotCommandConfigs() {
        return killBotCommandConfigs;
    }

    public SceneConfig setKillBotCommandConfigs(List<KillBotCommandConfig> killBotCommandConfigs) {
        this.killBotCommandConfigs = killBotCommandConfigs;
        return this;
    }

    public BaseItemPlacerConfig getStartPointPlacerConfig() {
        return startPointPlacerConfig;
    }

    public SceneConfig setStartPointPlacerConfig(BaseItemPlacerConfig startPointPlacerConfig) {
        this.startPointPlacerConfig = startPointPlacerConfig;
        return this;
    }

    public Boolean isWait4QuestPassedDialog() {
        return wait4QuestPassedDialog;
    }

    public SceneConfig setWait4QuestPassedDialog(Boolean wait4QuestPassedDialog) {
        this.wait4QuestPassedDialog = wait4QuestPassedDialog;
        return this;
    }

    public Boolean isWait4LevelUpDialog() {
        return wait4LevelUpDialog;
    }

    public SceneConfig setWait4LevelUpDialog(Boolean wait4LevelUpDialog) {
        this.wait4LevelUpDialog = wait4LevelUpDialog;
        return this;
    }

    public Boolean isWaitForBaseLostDialog() {
        return waitForBaseLostDialog;
    }

    public SceneConfig setWaitForBaseLostDialog(Boolean waitForBaseLostDialog) {
        this.waitForBaseLostDialog = waitForBaseLostDialog;
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

    public GameTipConfig getGameTipConfig() {
        return gameTipConfig;
    }

    public SceneConfig setGameTipConfig(GameTipConfig gameTipConfig) {
        this.gameTipConfig = gameTipConfig;
        return this;
    }

    public boolean isRemoveLoadingCover() {
        return removeLoadingCover;
    }

    public SceneConfig setRemoveLoadingCover(boolean removeLoadingCover) {
        this.removeLoadingCover = removeLoadingCover;
        return this;
    }
}

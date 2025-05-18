package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
public class SceneConfig {
    private Integer id;
    private String internalName;
    private String introText;
    private QuestConfig questConfig;
    private ViewFieldConfig viewFieldConfig;
    private Boolean suppressSell;
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
    private Boolean waitForBaseCreated;
    private Boolean processServerQuests;
    private List<ResourceItemPosition> resourceItemTypePositions;
    private Integer duration;
    private ScrollUiQuest scrollUiQuest;
    private List<BoxItemPosition> boxItemPositions;
    private GameTipConfig gameTipConfig;

    public Integer id() {
        return id;
    }

    public SceneConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public SceneConfig internalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public String getIntroText() {
        return introText;
    }

    public SceneConfig introText(String introText) {
        this.introText = introText;
        return this;
    }

    public ViewFieldConfig getViewFieldConfig() {
        return viewFieldConfig;
    }

    public SceneConfig viewFieldConfig(ViewFieldConfig viewFieldConfig) {
        this.viewFieldConfig = viewFieldConfig;
        return this;
    }

    public Boolean isSuppressSell() {
        return suppressSell;
    }

    public SceneConfig suppressSell(Boolean suppressSell) {
        this.suppressSell = suppressSell;
        return this;
    }

    public QuestConfig getQuestConfig() {
        return questConfig;
    }

    public SceneConfig questConfig(QuestConfig questConfig) {
        this.questConfig = questConfig;
        return this;
    }

    public List<BotConfig> getBotConfigs() {
        return botConfigs;
    }

    public SceneConfig botConfigs(List<BotConfig> botConfigs) {
        this.botConfigs = botConfigs;
        return this;
    }

    public List<BotMoveCommandConfig> getBotMoveCommandConfigs() {
        return botMoveCommandConfigs;
    }

    public SceneConfig botMoveCommandConfigs(List<BotMoveCommandConfig> botMoveCommandConfigs) {
        this.botMoveCommandConfigs = botMoveCommandConfigs;
        return this;
    }

    public List<BotHarvestCommandConfig> getBotHarvestCommandConfigs() {
        return botHarvestCommandConfigs;
    }

    public SceneConfig botHarvestCommandConfigs(List<BotHarvestCommandConfig> botHarvestCommandConfigs) {
        this.botHarvestCommandConfigs = botHarvestCommandConfigs;
        return this;
    }

    public List<BotAttackCommandConfig> getBotAttackCommandConfigs() {
        return botAttackCommandConfigs;
    }

    public SceneConfig botAttackCommandConfigs(List<BotAttackCommandConfig> botAttackCommandConfigs) {
        this.botAttackCommandConfigs = botAttackCommandConfigs;
        return this;
    }

    public List<BotKillOtherBotCommandConfig> getBotKillOtherBotCommandConfigs() {
        return botKillOtherBotCommandConfigs;
    }

    public SceneConfig botKillOtherBotCommandConfigs(List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs) {
        this.botKillOtherBotCommandConfigs = botKillOtherBotCommandConfigs;
        return this;
    }

    public List<BotKillHumanCommandConfig> getBotKillHumanCommandConfigs() {
        return botKillHumanCommandConfigs;
    }

    public SceneConfig botKillHumanCommandConfigs(List<BotKillHumanCommandConfig> botKillHumanCommandConfigs) {
        this.botKillHumanCommandConfigs = botKillHumanCommandConfigs;
        return this;
    }

    public List<BotRemoveOwnItemCommandConfig> getBotRemoveOwnItemCommandConfigs() {
        return botRemoveOwnItemCommandConfigs;
    }

    public SceneConfig botRemoveOwnItemCommandConfigs(List<BotRemoveOwnItemCommandConfig> botRemoveOwnItemCommandConfigs) {
        this.botRemoveOwnItemCommandConfigs = botRemoveOwnItemCommandConfigs;
        return this;
    }

    public List<KillBotCommandConfig> getKillBotCommandConfigs() {
        return killBotCommandConfigs;
    }

    public SceneConfig killBotCommandConfigs(List<KillBotCommandConfig> killBotCommandConfigs) {
        this.killBotCommandConfigs = killBotCommandConfigs;
        return this;
    }

    public BaseItemPlacerConfig getStartPointPlacerConfig() {
        return startPointPlacerConfig;
    }

    public SceneConfig startPointPlacerConfig(BaseItemPlacerConfig startPointPlacerConfig) {
        this.startPointPlacerConfig = startPointPlacerConfig;
        return this;
    }

    public Boolean isWait4QuestPassedDialog() {
        return wait4QuestPassedDialog;
    }

    public SceneConfig wait4QuestPassedDialog(Boolean wait4QuestPassedDialog) {
        this.wait4QuestPassedDialog = wait4QuestPassedDialog;
        return this;
    }

    public Boolean isWait4LevelUpDialog() {
        return wait4LevelUpDialog;
    }

    public SceneConfig wait4LevelUpDialog(Boolean wait4LevelUpDialog) {
        this.wait4LevelUpDialog = wait4LevelUpDialog;
        return this;
    }

    public Boolean isWaitForBaseLostDialog() {
        return waitForBaseLostDialog;
    }

    public SceneConfig waitForBaseLostDialog(Boolean waitForBaseLostDialog) {
        this.waitForBaseLostDialog = waitForBaseLostDialog;
        return this;
    }

    public Boolean isWaitForBaseCreated() {
        return waitForBaseCreated;
    }

    public SceneConfig waitForBaseCreated(Boolean waitForBaseCreated) {
        this.waitForBaseCreated = waitForBaseCreated;
        return this;
    }

    public Boolean isProcessServerQuests() {
        return processServerQuests;
    }

    public SceneConfig processServerQuests(Boolean processServerQuests) {
        this.processServerQuests = processServerQuests;
        return this;
    }

    public List<ResourceItemPosition> getResourceItemTypePositions() {
        return resourceItemTypePositions;
    }

    public SceneConfig resourceItemTypePositions(List<ResourceItemPosition> resourceItemTypePositions) {
        this.resourceItemTypePositions = resourceItemTypePositions;
        return this;
    }

    public Integer getDuration() {
        return duration;
    }

    public SceneConfig duration(Integer duration) {
        this.duration = duration;
        return this;
    }

    public ScrollUiQuest getScrollUiQuest() {
        return scrollUiQuest;
    }

    public SceneConfig scrollUiQuest(ScrollUiQuest scrollUiQuest) {
        this.scrollUiQuest = scrollUiQuest;
        return this;
    }

    public List<BoxItemPosition> getBoxItemPositions() {
        return boxItemPositions;
    }

    public SceneConfig boxItemPositions(List<BoxItemPosition> boxItemPositions) {
        this.boxItemPositions = boxItemPositions;
        return this;
    }

    public GameTipConfig getGameTipConfig() {
        return gameTipConfig;
    }

    public SceneConfig gameTipConfig(GameTipConfig gameTipConfig) {
        this.gameTipConfig = gameTipConfig;
        return this;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setIntroText(String introText) {
        this.introText = introText;
    }

    public void setQuestConfig(QuestConfig questConfig) {
        this.questConfig = questConfig;
    }

    public void setViewFieldConfig(ViewFieldConfig viewFieldConfig) {
        this.viewFieldConfig = viewFieldConfig;
    }

    public void setSuppressSell(Boolean suppressSell) {
        this.suppressSell = suppressSell;
    }

    public void setBotConfigs(List<BotConfig> botConfigs) {
        this.botConfigs = botConfigs;
    }

    public void setBotMoveCommandConfigs(List<BotMoveCommandConfig> botMoveCommandConfigs) {
        this.botMoveCommandConfigs = botMoveCommandConfigs;
    }

    public void setBotHarvestCommandConfigs(List<BotHarvestCommandConfig> botHarvestCommandConfigs) {
        this.botHarvestCommandConfigs = botHarvestCommandConfigs;
    }

    public void setBotAttackCommandConfigs(List<BotAttackCommandConfig> botAttackCommandConfigs) {
        this.botAttackCommandConfigs = botAttackCommandConfigs;
    }

    public void setBotKillOtherBotCommandConfigs(List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs) {
        this.botKillOtherBotCommandConfigs = botKillOtherBotCommandConfigs;
    }

    public void setBotKillHumanCommandConfigs(List<BotKillHumanCommandConfig> botKillHumanCommandConfigs) {
        this.botKillHumanCommandConfigs = botKillHumanCommandConfigs;
    }

    public void setBotRemoveOwnItemCommandConfigs(List<BotRemoveOwnItemCommandConfig> botRemoveOwnItemCommandConfigs) {
        this.botRemoveOwnItemCommandConfigs = botRemoveOwnItemCommandConfigs;
    }

    public void setKillBotCommandConfigs(List<KillBotCommandConfig> killBotCommandConfigs) {
        this.killBotCommandConfigs = killBotCommandConfigs;
    }

    public void setStartPointPlacerConfig(BaseItemPlacerConfig startPointPlacerConfig) {
        this.startPointPlacerConfig = startPointPlacerConfig;
    }

    public void setWait4LevelUpDialog(Boolean wait4LevelUpDialog) {
        this.wait4LevelUpDialog = wait4LevelUpDialog;
    }

    public void setWait4QuestPassedDialog(Boolean wait4QuestPassedDialog) {
        this.wait4QuestPassedDialog = wait4QuestPassedDialog;
    }

    public void setWaitForBaseLostDialog(Boolean waitForBaseLostDialog) {
        this.waitForBaseLostDialog = waitForBaseLostDialog;
    }

    public void setWaitForBaseCreated(Boolean waitForBaseCreated) {
        this.waitForBaseCreated = waitForBaseCreated;
    }

    public void setProcessServerQuests(Boolean processServerQuests) {
        this.processServerQuests = processServerQuests;
    }

    public void setResourceItemTypePositions(List<ResourceItemPosition> resourceItemTypePositions) {
        this.resourceItemTypePositions = resourceItemTypePositions;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setScrollUiQuest(ScrollUiQuest scrollUiQuest) {
        this.scrollUiQuest = scrollUiQuest;
    }

    public void setBoxItemPositions(List<BoxItemPosition> boxItemPositions) {
        this.boxItemPositions = boxItemPositions;
    }

    public void setGameTipConfig(GameTipConfig gameTipConfig) {
        this.gameTipConfig = gameTipConfig;
    }
}

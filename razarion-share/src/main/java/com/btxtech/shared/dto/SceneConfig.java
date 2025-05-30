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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getIntroText() {
        return introText;
    }

    public void setIntroText(String introText) {
        this.introText = introText;
    }

    public QuestConfig getQuestConfig() {
        return questConfig;
    }

    public void setQuestConfig(QuestConfig questConfig) {
        this.questConfig = questConfig;
    }

    public ViewFieldConfig getViewFieldConfig() {
        return viewFieldConfig;
    }

    public void setViewFieldConfig(ViewFieldConfig viewFieldConfig) {
        this.viewFieldConfig = viewFieldConfig;
    }

    public Boolean getSuppressSell() {
        return suppressSell;
    }

    public void setSuppressSell(Boolean suppressSell) {
        this.suppressSell = suppressSell;
    }

    public List<BotConfig> getBotConfigs() {
        return botConfigs;
    }

    public void setBotConfigs(List<BotConfig> botConfigs) {
        this.botConfigs = botConfigs;
    }

    public List<BotMoveCommandConfig> getBotMoveCommandConfigs() {
        return botMoveCommandConfigs;
    }

    public void setBotMoveCommandConfigs(List<BotMoveCommandConfig> botMoveCommandConfigs) {
        this.botMoveCommandConfigs = botMoveCommandConfigs;
    }

    public List<BotHarvestCommandConfig> getBotHarvestCommandConfigs() {
        return botHarvestCommandConfigs;
    }

    public void setBotHarvestCommandConfigs(List<BotHarvestCommandConfig> botHarvestCommandConfigs) {
        this.botHarvestCommandConfigs = botHarvestCommandConfigs;
    }

    public List<BotAttackCommandConfig> getBotAttackCommandConfigs() {
        return botAttackCommandConfigs;
    }

    public void setBotAttackCommandConfigs(List<BotAttackCommandConfig> botAttackCommandConfigs) {
        this.botAttackCommandConfigs = botAttackCommandConfigs;
    }

    public List<BotKillOtherBotCommandConfig> getBotKillOtherBotCommandConfigs() {
        return botKillOtherBotCommandConfigs;
    }

    public void setBotKillOtherBotCommandConfigs(List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs) {
        this.botKillOtherBotCommandConfigs = botKillOtherBotCommandConfigs;
    }

    public List<BotKillHumanCommandConfig> getBotKillHumanCommandConfigs() {
        return botKillHumanCommandConfigs;
    }

    public void setBotKillHumanCommandConfigs(List<BotKillHumanCommandConfig> botKillHumanCommandConfigs) {
        this.botKillHumanCommandConfigs = botKillHumanCommandConfigs;
    }

    public List<BotRemoveOwnItemCommandConfig> getBotRemoveOwnItemCommandConfigs() {
        return botRemoveOwnItemCommandConfigs;
    }

    public void setBotRemoveOwnItemCommandConfigs(List<BotRemoveOwnItemCommandConfig> botRemoveOwnItemCommandConfigs) {
        this.botRemoveOwnItemCommandConfigs = botRemoveOwnItemCommandConfigs;
    }

    public List<KillBotCommandConfig> getKillBotCommandConfigs() {
        return killBotCommandConfigs;
    }

    public void setKillBotCommandConfigs(List<KillBotCommandConfig> killBotCommandConfigs) {
        this.killBotCommandConfigs = killBotCommandConfigs;
    }

    public BaseItemPlacerConfig getStartPointPlacerConfig() {
        return startPointPlacerConfig;
    }

    public void setStartPointPlacerConfig(BaseItemPlacerConfig startPointPlacerConfig) {
        this.startPointPlacerConfig = startPointPlacerConfig;
    }

    public Boolean getWait4LevelUpDialog() {
        return wait4LevelUpDialog;
    }

    public void setWait4LevelUpDialog(Boolean wait4LevelUpDialog) {
        this.wait4LevelUpDialog = wait4LevelUpDialog;
    }

    public Boolean getWait4QuestPassedDialog() {
        return wait4QuestPassedDialog;
    }

    public void setWait4QuestPassedDialog(Boolean wait4QuestPassedDialog) {
        this.wait4QuestPassedDialog = wait4QuestPassedDialog;
    }

    public Boolean getWaitForBaseLostDialog() {
        return waitForBaseLostDialog;
    }

    public void setWaitForBaseLostDialog(Boolean waitForBaseLostDialog) {
        this.waitForBaseLostDialog = waitForBaseLostDialog;
    }

    public Boolean getWaitForBaseCreated() {
        return waitForBaseCreated;
    }

    public void setWaitForBaseCreated(Boolean waitForBaseCreated) {
        this.waitForBaseCreated = waitForBaseCreated;
    }

    public Boolean getProcessServerQuests() {
        return processServerQuests;
    }

    public void setProcessServerQuests(Boolean processServerQuests) {
        this.processServerQuests = processServerQuests;
    }

    public List<ResourceItemPosition> getResourceItemTypePositions() {
        return resourceItemTypePositions;
    }

    public void setResourceItemTypePositions(List<ResourceItemPosition> resourceItemTypePositions) {
        this.resourceItemTypePositions = resourceItemTypePositions;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public ScrollUiQuest getScrollUiQuest() {
        return scrollUiQuest;
    }

    public void setScrollUiQuest(ScrollUiQuest scrollUiQuest) {
        this.scrollUiQuest = scrollUiQuest;
    }

    public List<BoxItemPosition> getBoxItemPositions() {
        return boxItemPositions;
    }

    public void setBoxItemPositions(List<BoxItemPosition> boxItemPositions) {
        this.boxItemPositions = boxItemPositions;
    }

    public GameTipConfig getGameTipConfig() {
        return gameTipConfig;
    }

    public void setGameTipConfig(GameTipConfig gameTipConfig) {
        this.gameTipConfig = gameTipConfig;
    }

    public SceneConfig id(int id) {
        this.id = id;
        return this;
    }

    public SceneConfig internalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public SceneConfig introText(String introText) {
        this.introText = introText;
        return this;
    }

    public SceneConfig questConfig(QuestConfig questConfig) {
        this.questConfig = questConfig;
        return this;
    }

    public SceneConfig viewFieldConfig(ViewFieldConfig viewFieldConfig) {
        this.viewFieldConfig = viewFieldConfig;
        return this;
    }

    public SceneConfig suppressSell(boolean suppressSell) {
        this.suppressSell = suppressSell;
        return this;
    }

    public SceneConfig botConfigs(List<BotConfig> botConfigs) {
        this.botConfigs = botConfigs;
        return this;
    }

    public SceneConfig botMoveCommandConfigs(List<BotMoveCommandConfig> botMoveCommandConfigs) {
        this.botMoveCommandConfigs = botMoveCommandConfigs;
        return this;
    }

    public SceneConfig botHarvestCommandConfigs(List<BotHarvestCommandConfig> botHarvestCommandConfigs) {
        this.botHarvestCommandConfigs = botHarvestCommandConfigs;
        return this;
    }

    public SceneConfig botAttackCommandConfigs(List<BotAttackCommandConfig> botAttackCommandConfigs) {
        this.botAttackCommandConfigs = botAttackCommandConfigs;
        return this;
    }

    public SceneConfig botKillOtherBotCommandConfigs(List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs) {
        this.botKillOtherBotCommandConfigs = botKillOtherBotCommandConfigs;
        return this;
    }

    public SceneConfig botKillHumanCommandConfigs(List<BotKillHumanCommandConfig> botKillHumanCommandConfigs) {
        this.botKillHumanCommandConfigs = botKillHumanCommandConfigs;
        return this;
    }

    public SceneConfig botRemoveOwnItemCommandConfigs(List<BotRemoveOwnItemCommandConfig> botRemoveOwnItemCommandConfigs) {
        this.botRemoveOwnItemCommandConfigs = botRemoveOwnItemCommandConfigs;
        return this;
    }

    public SceneConfig killBotCommandConfigs(List<KillBotCommandConfig> killBotCommandConfigs) {
        this.killBotCommandConfigs = killBotCommandConfigs;
        return this;
    }

    public SceneConfig startPointPlacerConfig(BaseItemPlacerConfig startPointPlacerConfig) {
        this.startPointPlacerConfig = startPointPlacerConfig;
        return this;
    }

    public SceneConfig wait4LevelUpDialog(boolean wait4LevelUpDialog) {
        this.wait4LevelUpDialog = wait4LevelUpDialog;
        return this;
    }

    public SceneConfig wait4QuestPassedDialog(boolean wait4QuestPassedDialog) {
        this.wait4QuestPassedDialog = wait4QuestPassedDialog;
        return this;
    }

    public SceneConfig waitForBaseLostDialog(boolean waitForBaseLostDialog) {
        this.waitForBaseLostDialog = waitForBaseLostDialog;
        return this;
    }

    public SceneConfig waitForBaseCreated(boolean waitForBaseCreated) {
        this.waitForBaseCreated = waitForBaseCreated;
        return this;
    }

    public SceneConfig processServerQuests(boolean processServerQuests) {
        this.processServerQuests = processServerQuests;
        return this;
    }

    public SceneConfig resourceItemTypePositions(List<ResourceItemPosition> resourceItemTypePositions) {
        this.resourceItemTypePositions = resourceItemTypePositions;
        return this;
    }

    public SceneConfig duration(int duration) {
        this.duration = duration;
        return this;
    }

    public SceneConfig scrollUiQuest(ScrollUiQuest scrollUiQuest) {
        this.scrollUiQuest = scrollUiQuest;
        return this;
    }

    public SceneConfig boxItemPositions(List<BoxItemPosition> boxItemPositions) {
        this.boxItemPositions = boxItemPositions;
        return this;
    }

    public SceneConfig gameTipConfig(GameTipConfig gameTipConfig) {
        this.gameTipConfig = gameTipConfig;
        return this;
    }

}

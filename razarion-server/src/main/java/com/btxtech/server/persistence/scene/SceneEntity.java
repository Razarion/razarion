package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.tracker.I18nBundleEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.ScrollUiQuest;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Entity
@Table(name = "SCENE")
public class SceneEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nIntroText;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private QuestConfigEntity questConfig;
    @AttributeOverrides({
            @AttributeOverride(name = "fromPosition.x", column = @Column(name = "viewFieldFromPositionX")),
            @AttributeOverride(name = "fromPosition.y", column = @Column(name = "viewFieldFromPositionY")),
            @AttributeOverride(name = "toPosition.x", column = @Column(name = "viewFieldToPositionX")),
            @AttributeOverride(name = "toPosition.y", column = @Column(name = "viewFieldToPositionY")),
            @AttributeOverride(name = "speed", column = @Column(name = "viewFieldSpeed")),
            @AttributeOverride(name = "cameraLocked", column = @Column(name = "viewFieldCameraLocked")),
            @AttributeOverride(name = "bottomWidth", column = @Column(name = "viewFieldBottomWidth")),
    })
    private ViewFieldConfig viewFieldConfig;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinTable(name = "SCENE_BOT",
            joinColumns = @JoinColumn(name = "sceneId"),
            inverseJoinColumns = @JoinColumn(name = "botd"))
    private List<BotConfigEntity> botConfigEntities;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn
    private List<BotMoveCommandEntity> botMoveCommandEntities;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn
    private List<BotHarvestCommandEntity> botHarvestCommandEntities;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn
    private List<BotAttackCommandEntity> botAttackCommandEntities;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn
    private List<BotKillOtherBotCommandEntity> botKillOtherBotCommandEntities;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn
    private List<BotKillHumanCommandEntity> botKillHumanCommandEntities;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn
    private List<BotRemoveOwnItemCommandEntity> botRemoveOwnItemCommandEntities;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn
    private List<BotKillBotCommandEntity> killBotCommandEntities;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private StartPointPlacerEntity startPointPlacerEntity;
    private Boolean wait4LevelUpDialog;
    private Boolean wait4QuestPassedDialog;
    private Boolean waitForBaseLostDialog;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "sceneId")
    private List<ResourceItemPositionEntity> resourceItemPositionEntities;
    private Integer duration;
    // ScrollUiQuest
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity scrollUiQuestI18nTitle;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity scrollUiQuestI18nDescription;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity scrollUiQuestI18nPassedMessage;
    private Boolean scrollUiQuestI18nHidePassedDialog;
    @AttributeOverrides({
            @AttributeOverride(name = "start.x", column = @Column(name = "scrollUiQuestTargetRectangleStartX")),
            @AttributeOverride(name = "start.y", column = @Column(name = "scrollUiQuestTargetRectangleStartY")),
            @AttributeOverride(name = "end.x", column = @Column(name = "scrollUiQuestTargetRectangleEndX")),
            @AttributeOverride(name = "end.y", column = @Column(name = "scrollUiQuestTargetRectangleEndY")),
    })
    private Rectangle2D scrollUiQuestTargetRectangle;
    private Integer scrollUiQuestXp;
    private Integer scrollUiQuestMoney;
    private Integer scrollUiQuestCristal;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "sceneId")
    private List<BoxItemPositionEntity> boxItemPositionEntities;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private GameTipConfigEntity gameTipConfigEntity;
    private Boolean removeLoadingCover;

    public SceneConfig toSceneConfig(Locale locale) {
        SceneConfig sceneConfig = new SceneConfig().setInternalName(internalName);
        if (i18nIntroText != null) {
            sceneConfig.setIntroText(i18nIntroText.getString(locale));
        }
        if (questConfig != null) {
            sceneConfig.setQuestConfig(questConfig.toQuestConfig(locale));
        }
        sceneConfig.setViewFieldConfig(viewFieldConfig);
        if (botConfigEntities != null && !botConfigEntities.isEmpty()) {
            List<BotConfig> botConfigs = new ArrayList<>();
            for (BotConfigEntity botConfigEntity : botConfigEntities) {
                botConfigs.add(botConfigEntity.toBotConfig());
            }
            sceneConfig.setBotConfigs(botConfigs);
        }
        if (botMoveCommandEntities != null && !botMoveCommandEntities.isEmpty()) {
            List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
            for (BotMoveCommandEntity botMoveCommandEntity : botMoveCommandEntities) {
                botMoveCommandConfigs.add(botMoveCommandEntity.toBotMoveCommandConfig());
            }
            sceneConfig.setBotMoveCommandConfigs(botMoveCommandConfigs);
        }
        if (botHarvestCommandEntities != null && !botHarvestCommandEntities.isEmpty()) {
            List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
            for (BotHarvestCommandEntity botHarvestCommandEntity : botHarvestCommandEntities) {
                botHarvestCommandConfigs.add(botHarvestCommandEntity.toBotHarvestCommandConfig());
            }
            sceneConfig.setBotHarvestCommandConfigs(botHarvestCommandConfigs);
        }
        if (botAttackCommandEntities != null && !botAttackCommandEntities.isEmpty()) {
            List<BotAttackCommandConfig> botAttackCommandConfigs = new ArrayList<>();
            for (BotAttackCommandEntity botAttackCommandEntity : botAttackCommandEntities) {
                botAttackCommandConfigs.add(botAttackCommandEntity.toBotAttackCommandConfig());
            }
            sceneConfig.setBotAttackCommandConfigs(botAttackCommandConfigs);
        }
        if (botKillOtherBotCommandEntities != null && !botKillOtherBotCommandEntities.isEmpty()) {
            List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs = new ArrayList<>();
            for (BotKillOtherBotCommandEntity botKillOtherBotCommandEntity : botKillOtherBotCommandEntities) {
                botKillOtherBotCommandConfigs.add(botKillOtherBotCommandEntity.toBotKillOtherBotCommandConfig());
            }
            sceneConfig.setBotKillOtherBotCommandConfigs(botKillOtherBotCommandConfigs);
        }
        if (botKillHumanCommandEntities != null && !botKillHumanCommandEntities.isEmpty()) {
            List<BotKillHumanCommandConfig> botKillHumanCommandConfigs = new ArrayList<>();
            for (BotKillHumanCommandEntity botKillHumanCommandEntity : botKillHumanCommandEntities) {
                botKillHumanCommandConfigs.add(botKillHumanCommandEntity.toBotKillHumanCommandConfig());
            }
            sceneConfig.setBotKillHumanCommandConfigs(botKillHumanCommandConfigs);
        }
        if (botRemoveOwnItemCommandEntities != null && !botRemoveOwnItemCommandEntities.isEmpty()) {
            List<BotRemoveOwnItemCommandConfig> botRemoveOwnItemCommandConfigs = new ArrayList<>();
            for (BotRemoveOwnItemCommandEntity botRemoveOwnItemCommandEntity : botRemoveOwnItemCommandEntities) {
                botRemoveOwnItemCommandConfigs.add(botRemoveOwnItemCommandEntity.toBotRemoveOwnItemCommandConfig());
            }
            sceneConfig.setBotRemoveOwnItemCommandConfigs(botRemoveOwnItemCommandConfigs);
        }
        if (killBotCommandEntities != null && !killBotCommandEntities.isEmpty()) {
            List<KillBotCommandConfig> killBotCommandConfigs = new ArrayList<>();
            for (BotKillBotCommandEntity botKillBotCommandEntity : killBotCommandEntities) {
                killBotCommandConfigs.add(botKillBotCommandEntity.toKillBotCommandConfig());
            }
            sceneConfig.setKillBotCommandConfigs(killBotCommandConfigs);
        }
        if (startPointPlacerEntity != null) {
            sceneConfig.setStartPointPlacerConfig(startPointPlacerEntity.toStartPointPlacerConfig());
        }
        if (wait4LevelUpDialog != null) {
            sceneConfig.setWait4LevelUpDialog(wait4LevelUpDialog);
        }
        if (wait4QuestPassedDialog != null) {
            sceneConfig.setWait4QuestPassedDialog(wait4QuestPassedDialog);
        }
        if (waitForBaseLostDialog != null) {
            sceneConfig.setWaitForBaseLostDialog(waitForBaseLostDialog);
        }
        if (resourceItemPositionEntities != null && !resourceItemPositionEntities.isEmpty()) {
            List<ResourceItemPosition> resourceItemTypePositions = new ArrayList<>();
            for (ResourceItemPositionEntity resourceItemPositionEntity : resourceItemPositionEntities) {
                resourceItemTypePositions.add(resourceItemPositionEntity.toResourceItemPosition());
            }
            sceneConfig.setResourceItemTypePositions(resourceItemTypePositions);
        }
        sceneConfig.setDuration(duration);

        if (scrollUiQuestTargetRectangle != null && scrollUiQuestI18nTitle != null) {
            ScrollUiQuest scrollUiQuest = new ScrollUiQuest().setScrollTargetRectangle(scrollUiQuestTargetRectangle).setTitle(scrollUiQuestI18nTitle.getString(locale));
            if (scrollUiQuestI18nDescription != null) {
                scrollUiQuest.setDescription(scrollUiQuestI18nDescription.getString(locale));
            }
            if (scrollUiQuestI18nPassedMessage != null) {
                scrollUiQuest.setPassedMessage(scrollUiQuestI18nPassedMessage.getString(locale));
            }
            if (scrollUiQuestI18nHidePassedDialog != null) {
                scrollUiQuest.setHidePassedDialog(scrollUiQuestI18nHidePassedDialog);
            }
            scrollUiQuest.setXp(scrollUiQuestXp);
            scrollUiQuest.setMoney(scrollUiQuestMoney);
            scrollUiQuest.setCristal(scrollUiQuestCristal);
            sceneConfig.setScrollUiQuest(scrollUiQuest);
        }
        if (boxItemPositionEntities != null && !boxItemPositionEntities.isEmpty()) {
            List<BoxItemPosition> boxItemPositions = new ArrayList<>();
            for (BoxItemPositionEntity boxItemPositionEntity : boxItemPositionEntities) {
                boxItemPositions.add(boxItemPositionEntity.toBoxItemPosition());
            }
            sceneConfig.setBoxItemPositions(boxItemPositions);
        }
        if (gameTipConfigEntity != null) {
            sceneConfig.setGameTipConfig(gameTipConfigEntity.toGameTipConfig());
        }
        if (removeLoadingCover != null) {
            sceneConfig.setRemoveLoadingCover(removeLoadingCover);
        }
        return sceneConfig;
    }

    public void fromSceneConfig(ItemTypePersistence itemTypePersistence, SceneConfig sceneConfig, Locale locale) {
        internalName = sceneConfig.getInternalName();
        if (sceneConfig.getIntroText() != null) {
            i18nIntroText = new I18nBundleEntity();
            i18nIntroText.putString(locale, sceneConfig.getIntroText());
        }
        if (sceneConfig.getQuestConfig() != null) {
            questConfig = new QuestConfigEntity();
            questConfig.fromQuestConfig(itemTypePersistence, sceneConfig.getQuestConfig(), locale);
        }
        viewFieldConfig = sceneConfig.getViewFieldConfig();
        if (sceneConfig.getStartPointPlacerConfig() != null) {
            if (startPointPlacerEntity == null) {
                startPointPlacerEntity = new StartPointPlacerEntity();
            }
            startPointPlacerEntity.fromStartPointPlacerConfig(sceneConfig.getStartPointPlacerConfig());
        } else {
            startPointPlacerEntity = null;
        }
        wait4LevelUpDialog = sceneConfig.isWait4LevelUpDialog();
        wait4QuestPassedDialog = sceneConfig.isWait4QuestPassedDialog();
        waitForBaseLostDialog = sceneConfig.isWaitForBaseLostDialog();
        duration = sceneConfig.getDuration();

        if (sceneConfig.getScrollUiQuest() != null) {
            if (sceneConfig.getScrollUiQuest().getTitle() != null) {
                scrollUiQuestI18nTitle = new I18nBundleEntity();
                scrollUiQuestI18nTitle.putString(locale, sceneConfig.getScrollUiQuest().getTitle());
            }
            if (sceneConfig.getScrollUiQuest().getDescription() != null) {
                scrollUiQuestI18nDescription = new I18nBundleEntity();
                scrollUiQuestI18nDescription.putString(locale, sceneConfig.getScrollUiQuest().getDescription());
            }
            if (sceneConfig.getScrollUiQuest().getPassedMessage() != null) {
                scrollUiQuestI18nPassedMessage = new I18nBundleEntity();
                scrollUiQuestI18nPassedMessage.putString(locale, sceneConfig.getScrollUiQuest().getPassedMessage());
            }
            scrollUiQuestI18nHidePassedDialog = sceneConfig.getScrollUiQuest().isHidePassedDialog();
            scrollUiQuestTargetRectangle = sceneConfig.getScrollUiQuest().getScrollTargetRectangle();
            scrollUiQuestXp = sceneConfig.getScrollUiQuest().getXp();
            scrollUiQuestMoney = sceneConfig.getScrollUiQuest().getMoney();
            scrollUiQuestCristal = sceneConfig.getScrollUiQuest().getCristal();
        }
        removeLoadingCover = sceneConfig.isRemoveLoadingCover();
    }

    public void setBotConfigEntities(List<BotConfigEntity> botConfigEntities) {
        this.botConfigEntities = botConfigEntities;
    }

    public void setBotMoveCommandEntities(List<BotMoveCommandEntity> botMoveCommandEntities) {
        this.botMoveCommandEntities = botMoveCommandEntities;
    }

    public void setBotHarvestCommandEntities(List<BotHarvestCommandEntity> botHarvestCommandEntities) {
        this.botHarvestCommandEntities = botHarvestCommandEntities;
    }

    public void setBotAttackCommandEntities(List<BotAttackCommandEntity> botAttackCommandEntities) {
        this.botAttackCommandEntities = botAttackCommandEntities;
    }

    public void setBotKillOtherBotCommandEntities(List<BotKillOtherBotCommandEntity> botKillOtherBotCommandEntities) {
        this.botKillOtherBotCommandEntities = botKillOtherBotCommandEntities;
    }

    public void setBotKillHumanCommandEntities(List<BotKillHumanCommandEntity> botKillHumanCommandEntities) {
        this.botKillHumanCommandEntities = botKillHumanCommandEntities;
    }

    public void setBotRemoveOwnItemCommandEntities(List<BotRemoveOwnItemCommandEntity> botRemoveOwnItemCommandEntities) {
        this.botRemoveOwnItemCommandEntities = botRemoveOwnItemCommandEntities;
    }

    public void setKillBotCommandEntities(List<BotKillBotCommandEntity> killBotCommandEntities) {
        this.killBotCommandEntities = killBotCommandEntities;
    }

    public void setResourceItemPositionEntities(List<ResourceItemPositionEntity> resourceItemPositionEntities) {
        this.resourceItemPositionEntities = resourceItemPositionEntities;
    }

    public void setBoxItemPositionEntities(List<BoxItemPositionEntity> boxItemPositionEntities) {
        this.boxItemPositionEntities = boxItemPositionEntities;
    }

    public void setGameTipConfigEntity(GameTipConfigEntity gameTipConfigEntity) {
        this.gameTipConfigEntity = gameTipConfigEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SceneEntity that = (SceneEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
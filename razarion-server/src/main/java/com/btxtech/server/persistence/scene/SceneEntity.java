package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.tracker.I18nBundleEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
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
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
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
    private List<KillBotCommandEntity> killBotCommandEntities;
    // BaseItemPlacerConfig
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "startPlacerSuggestedPositionX")),
            @AttributeOverride(name = "y", column = @Column(name = "startPlacerSuggestedPositionY")),
    })
    private DecimalPosition startPlacerSuggestedPosition;
    private Double startPlacerEnemyFreeRadius;
    @ElementCollection
    @CollectionTable(name = "SCENE_START_PLACE_ALLOWED_AREA", joinColumns = @JoinColumn(name = "sceneId"))
    @OrderColumn(name = "orderColumn")
    private List<DecimalPosition> startPlacerEnemyAllowedArea;
    private Boolean wait4LevelUpDialog;
    private Boolean wait4QuestPassedDialog;
    private Boolean waitForBaseLostDialog;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn
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
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn
    private List<BoxItemPositionEntity> boxItemPositionEntities;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private GameTipConfigEntity gameTipConfigEntity;
    private Boolean removeLoadingCover;

    public SceneConfig toSceneConfig(Locale locale) {
        SceneConfig sceneConfig = new SceneConfig().setId(id).setInternalName(internalName);
        if (i18nIntroText != null) {
            sceneConfig.setIntroText(i18nIntroText.getString(locale));
        }
        if (questConfig != null) {
            sceneConfig.setQuestConfig(questConfig.toQuestConfig(locale));
        }
        sceneConfig.setViewFieldConfig(viewFieldConfig);
        List<BotConfig> botConfigs = new ArrayList<>();
        if (botConfigEntities != null) {
            for (BotConfigEntity botConfigEntity : botConfigEntities) {
                botConfigs.add(botConfigEntity.toBotConfig());
            }
        }
        sceneConfig.setBotConfigs(botConfigs);
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        if (botMoveCommandEntities != null) {
            for (BotMoveCommandEntity botMoveCommandEntity : botMoveCommandEntities) {
                botMoveCommandConfigs.add(botMoveCommandEntity.toBotMoveCommandConfig());
            }
        }
        sceneConfig.setBotMoveCommandConfigs(botMoveCommandConfigs);
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        if (botHarvestCommandEntities != null) {
            for (BotHarvestCommandEntity botHarvestCommandEntity : botHarvestCommandEntities) {
                botHarvestCommandConfigs.add(botHarvestCommandEntity.toBotHarvestCommandConfig());
            }
        }
        sceneConfig.setBotHarvestCommandConfigs(botHarvestCommandConfigs);
        List<BotAttackCommandConfig> botAttackCommandConfigs = new ArrayList<>();
        if (botAttackCommandEntities != null) {
            for (BotAttackCommandEntity botAttackCommandEntity : botAttackCommandEntities) {
                botAttackCommandConfigs.add(botAttackCommandEntity.toBotAttackCommandConfig());
            }
        }
        sceneConfig.setBotAttackCommandConfigs(botAttackCommandConfigs);
        List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs = new ArrayList<>();
        if (botKillOtherBotCommandEntities != null) {
            for (BotKillOtherBotCommandEntity botKillOtherBotCommandEntity : botKillOtherBotCommandEntities) {
                botKillOtherBotCommandConfigs.add(botKillOtherBotCommandEntity.toBotKillOtherBotCommandConfig());
            }
        }
        sceneConfig.setBotKillOtherBotCommandConfigs(botKillOtherBotCommandConfigs);
        List<BotKillHumanCommandConfig> botKillHumanCommandConfigs = new ArrayList<>();
        if (botKillHumanCommandEntities != null) {
            for (BotKillHumanCommandEntity botKillHumanCommandEntity : botKillHumanCommandEntities) {
                botKillHumanCommandConfigs.add(botKillHumanCommandEntity.toBotKillHumanCommandConfig());
            }
        }
        sceneConfig.setBotKillHumanCommandConfigs(botKillHumanCommandConfigs);
        List<BotRemoveOwnItemCommandConfig> botRemoveOwnItemCommandConfigs = new ArrayList<>();
        if (botRemoveOwnItemCommandEntities != null) {
            for (BotRemoveOwnItemCommandEntity botRemoveOwnItemCommandEntity : botRemoveOwnItemCommandEntities) {
                botRemoveOwnItemCommandConfigs.add(botRemoveOwnItemCommandEntity.toBotRemoveOwnItemCommandConfig());
            }
        }
        sceneConfig.setBotRemoveOwnItemCommandConfigs(botRemoveOwnItemCommandConfigs);
        List<KillBotCommandConfig> killBotCommandConfigs = new ArrayList<>();
        if (killBotCommandEntities != null) {
            for (KillBotCommandEntity killBotCommandEntity : killBotCommandEntities) {
                killBotCommandConfigs.add(killBotCommandEntity.toKillBotCommandConfig());
            }
        }
        sceneConfig.setKillBotCommandConfigs(killBotCommandConfigs);
        if (startPlacerSuggestedPosition != null && startPlacerEnemyFreeRadius != null && startPlacerEnemyAllowedArea != null && !startPlacerEnemyAllowedArea.isEmpty()) {
            sceneConfig.setStartPointPlacerConfig(new BaseItemPlacerConfig().setSuggestedPosition(startPlacerSuggestedPosition).setEnemyFreeRadius(startPlacerEnemyFreeRadius).setAllowedArea(new Polygon2D(startPlacerEnemyAllowedArea)));
        }
        if(wait4LevelUpDialog != null) {
            sceneConfig.setWait4LevelUpDialog(wait4LevelUpDialog);
        }
        if(wait4QuestPassedDialog != null) {
            sceneConfig.setWait4QuestPassedDialog(wait4QuestPassedDialog);
        }
        if(waitForBaseLostDialog != null) {
            sceneConfig.setWaitForBaseLostDialog(waitForBaseLostDialog);
        }
        if (resourceItemPositionEntities != null) {
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
            sceneConfig.setScrollUiQuest(scrollUiQuest);
        }
        if (boxItemPositionEntities != null) {
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

    public void fromSceneConfig(SceneConfig sceneConfig) {
        removeLoadingCover = sceneConfig.isRemoveLoadingCover();
        viewFieldConfig = sceneConfig.getViewFieldConfig();
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

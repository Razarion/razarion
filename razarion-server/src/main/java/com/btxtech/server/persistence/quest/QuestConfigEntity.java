package com.btxtech.server.persistence.quest;

import com.btxtech.server.mgmt.QuestBackendInfo;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;
import com.btxtech.server.persistence.I18nBundleEntity;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;
import java.util.Locale;

/**
 * Created by Beat
 * 06.05.2017.
 */
@Entity
@Table(name = "QUEST")
public class QuestConfigEntity implements ObjectNameIdProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity title;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity description;
    private int xp;
    private int razarion;
    private int crystal;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity passedMessage;
    private boolean hidePassedDialog;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ConditionConfigEntity conditionConfigEntity;
    @ManyToMany(mappedBy="questConfigs")
    private List<ServerLevelQuestEntity> serverLevelQuestEntities;

    public Integer getId() {
        return id;
    }

    public QuestConfig toQuestConfig(Locale locale) {
        QuestConfig questConfig = new QuestConfig().id(id).internalName(internalName).xp(xp).razarion(razarion).crystal(crystal);
        if (title != null) {
            questConfig.title(title.getString(locale));
        }
        if (description != null) {
            questConfig.description(description.getString(locale));
        }
        if (passedMessage != null) {
            questConfig.passedMessage(passedMessage.getString(locale));
        }
        if (conditionConfigEntity != null) {
            questConfig.setConditionConfig(conditionConfigEntity.toQuestConfig()).hidePassedDialog(hidePassedDialog);
        }
        return questConfig;
    }

    public void fromQuestConfig(ItemTypePersistence itemTypePersistence, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, QuestConfig questConfig, Locale locale) {
        internalName = questConfig.getInternalName();
        if (questConfig.getTitle() != null) {
            if (title == null) {
                title = new I18nBundleEntity();
            }
            title.putString(locale, questConfig.getTitle());
        } else {
            title = null;
        }
        if (questConfig.getDescription() != null) {
            if (description == null) {
                description = new I18nBundleEntity();
            }
            description.putString(locale, questConfig.getDescription());
        } else {
            description = null;
        }
        xp = questConfig.getXp();
        razarion = questConfig.getRazarion();
        crystal = questConfig.getCrystal();
        if (questConfig.getPassedMessage() != null) {
            if (passedMessage == null) {
                passedMessage = new I18nBundleEntity();
            }
            passedMessage.putString(locale, questConfig.getPassedMessage());
        } else {
            passedMessage = null;
        }
        hidePassedDialog = questConfig.isHidePassedDialog();
        if (questConfig.getConditionConfig() != null) {
            if (conditionConfigEntity == null) {
                conditionConfigEntity = new ConditionConfigEntity();
            }
            conditionConfigEntity.fromConditionConfig(itemTypePersistence, baseItemTypeCrudPersistence, questConfig.getConditionConfig());
        } else {
            conditionConfigEntity = null;
        }
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }

    public String getInternalName() {
        return internalName;
    }

    public QuestBackendInfo toQuestBackendInfo() {
        LevelEntity levelEntity = null;
        if (serverLevelQuestEntities != null && serverLevelQuestEntities.size() > 0) {
            levelEntity = serverLevelQuestEntities.get(0).getMinimalLevel();
        }
        QuestBackendInfo questBackendInfo = new QuestBackendInfo().setId(id).setInternalName(internalName);
        if (levelEntity != null) {
            questBackendInfo.setLevelId(levelEntity.getId()).setLevelNumber(levelEntity.getNumber());
        }
        return questBackendInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QuestConfigEntity that = (QuestConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}

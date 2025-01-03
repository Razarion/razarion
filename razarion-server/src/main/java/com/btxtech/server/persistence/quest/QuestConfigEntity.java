package com.btxtech.server.persistence.quest;

import com.btxtech.server.persistence.I18nBundleEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BotConfigEntityPersistence;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

    public Integer getId() {
        return id;
    }

    public QuestConfig toQuestConfig() {
        QuestConfig questConfig = new QuestConfig().id(id).internalName(internalName).xp(xp).razarion(razarion).crystal(crystal);
        if (title != null) {
            questConfig.title(title.getString());
        }
        if (description != null) {
            questConfig.description(description.getString());
        }
        if (passedMessage != null) {
            questConfig.passedMessage(passedMessage.getString());
        }
        if (conditionConfigEntity != null) {
            questConfig.conditionConfig(conditionConfigEntity.toQuestConfig()).hidePassedDialog(hidePassedDialog);
        }
        return questConfig;
    }

    public void fromQuestConfig(BotConfigEntityPersistence botConfigEntityPersistence, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, QuestConfig questConfig) {
        internalName = questConfig.getInternalName();
        if (questConfig.getTitle() != null) {
            if (title == null) {
                title = new I18nBundleEntity();
            }
            title.putString(questConfig.getTitle());
        } else {
            title = null;
        }
        if (questConfig.getDescription() != null) {
            if (description == null) {
                description = new I18nBundleEntity();
            }
            description.putString(questConfig.getDescription());
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
            passedMessage.putString(questConfig.getPassedMessage());
        } else {
            passedMessage = null;
        }
        hidePassedDialog = questConfig.isHidePassedDialog();
        if (questConfig.getConditionConfig() != null) {
            if (conditionConfigEntity == null) {
                conditionConfigEntity = new ConditionConfigEntity();
            }
            conditionConfigEntity.fromConditionConfig(botConfigEntityPersistence, baseItemTypeCrudPersistence, questConfig.getConditionConfig());
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

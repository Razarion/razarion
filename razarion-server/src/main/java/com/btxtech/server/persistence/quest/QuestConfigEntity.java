package com.btxtech.server.persistence.quest;

import com.btxtech.server.persistence.tracker.I18nBundleEntity;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Locale;

/**
 * Created by Beat
 * 06.05.2017.
 */
@Entity
@Table(name = "QUEST")
public class QuestConfigEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity title;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity description;
    private int xp;
    private int money;
    private int cristal;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity passedMessage;
    private boolean hidePassedDialog;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ConditionConfigEntity conditionConfigEntity;

    public QuestConfig toQuestConfig(Locale locale) {
        QuestConfig questConfig = new QuestConfig().setId(id).setTitle(title.getString(locale)).setDescription(description.getString(locale)).setXp(xp).setMoney(money).setCristal(cristal);
        return questConfig.setConditionConfig(conditionConfigEntity.toQuestConfig()).setPassedMessage(passedMessage.getString(locale)).setHidePassedDialog(hidePassedDialog);
    }

    public void fromQuestConfig(QuestConfig questConfig, Locale locale) {
        title.putString(locale, questConfig.getTitle());
        description.putString(locale, questConfig.getDescription());
        xp = questConfig.getXp();
        money = questConfig.getMoney();
        cristal = questConfig.getCristal();
        passedMessage.putString(locale, questConfig.getPassedMessage());
        hidePassedDialog = questConfig.isHidePassedDialog();
        conditionConfigEntity.fromConditionConfig(questConfig.getConditionConfig());
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

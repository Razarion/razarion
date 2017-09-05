package com.btxtech.server.persistence;

import com.btxtech.server.mgmt.QuestBackendInfo;
import com.btxtech.server.persistence.quest.QuestConfigEntity;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * Created by Beat
 * on 06.09.2017.
 */
@Singleton
public class QuestPersistence {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public QuestBackendInfo findQuestBackendInfo(int questId) {
        QuestConfigEntity questConfigEntity = entityManager.find(QuestConfigEntity.class, questId);
        return new QuestBackendInfo().setId(questConfigEntity.getId()).setInternalName(questConfigEntity.getInternalName());
    }
}

package com.btxtech.server.persistence;

import com.btxtech.server.mgmt.QuestBackendInfo;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

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
        return entityManager.find(QuestConfigEntity.class, questId).toQuestBackendInfo();
    }
}

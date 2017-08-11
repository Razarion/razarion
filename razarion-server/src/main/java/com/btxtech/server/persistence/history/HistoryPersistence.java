package com.btxtech.server.persistence.history;

import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;

/**
 * Created by Beat
 * 22.05.2017.
 */
@Singleton
public class HistoryPersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private UserService userService;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Transactional
    public void onLevelUp(HumanPlayerId humanPlayerId, LevelEntity newLevel) {
        try {
            LevelHistoryEntity levelHistoryEntity = new LevelHistoryEntity();
            levelHistoryEntity.setTimeStamp(new Date());
            levelHistoryEntity.setHumanPlayerIdEntityId(userService.getHumanPlayerId(humanPlayerId.getPlayerId()).getId());
            levelHistoryEntity.setLevelId(newLevel.getId());
            levelHistoryEntity.setLevelNumber(newLevel.getNumber());
            entityManager.persist(levelHistoryEntity);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional
    public void onQuest(HumanPlayerId humanPlayerId, QuestConfig questConfig, QuestHistoryEntity.Type type) {
        try {
            QuestHistoryEntity questHistoryEntity = new QuestHistoryEntity();
            questHistoryEntity.setTimeStamp(new Date());
            questHistoryEntity.setHumanPlayerIdEntityId(userService.getHumanPlayerId(humanPlayerId.getPlayerId()).getId());
            questHistoryEntity.setQuestId(questConfig.getId());
            questHistoryEntity.setQuestInternalName(questConfig.getInternalName());
            questHistoryEntity.setType(type);
            entityManager.persist(questHistoryEntity);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

}

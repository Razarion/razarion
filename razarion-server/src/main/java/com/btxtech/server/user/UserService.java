package com.btxtech.server.user;

import com.btxtech.server.model.UserEntity;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.LevelUnlockEntity;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import com.btxtech.server.repository.UserRepository;
import com.btxtech.server.service.engine.LevelCrudPersistence;
import com.btxtech.server.service.engine.QuestConfigService;
import com.btxtech.server.service.engine.ServerGameEngineCrudPersistence;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final LevelCrudPersistence levelCrudPersistence;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;
    private final QuestConfigService questConfigService;

    public UserService(LevelCrudPersistence levelCrudPersistence,
                       SessionService sessionService,
                       UserRepository userRepository,
                       ServerGameEngineCrudPersistence serverGameEngineCrudPersistence, QuestConfigService questConfigService) {
        this.levelCrudPersistence = levelCrudPersistence;
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
        this.questConfigService = questConfigService;
    }

    @Transactional
    public UserContext getUserContext(String httpSessionId) {
        var session = sessionService.getSession(httpSessionId);
        if (session.getUserContext() == null) {
            session.setUserContext(createUserContext());
        }
        return session.getUserContext();
    }


    private UserContext createUserContext() {
        var userEntity = new UserEntity();
        userEntity.setLevel(levelCrudPersistence.getStarterLevel());
        return userRepository.save(userEntity).toUserContext();
    }

    public UserContext getUserContext(int userId) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            return playerSession.getUserContext();
        } else {
            // TODO return getUserEntity(userId).toUserContext();
            throw new UnsupportedOperationException("... TODO ...");
        }
    }

    @Transactional
    public UserContext getUserContextTransactional(int userId) {
        return getUserContext(userId);
    }

    @Transactional
    public void persistLevel(int userId, LevelEntity newLevel) {
        UserEntity userEntity = userRepository.getReferenceById(userId);
        userEntity.setLevel(newLevel);
        userRepository.save(userEntity);
    }

    @Transactional
    public QuestConfig getAndSaveNewQuest(Integer userId) {
        UserEntity userEntity = userRepository.getReferenceById(userId);
        if (userEntity.getActiveQuest() == null) {
            QuestConfigEntity newQuest = serverGameEngineCrudPersistence.getQuest4LevelAndCompleted(userEntity.getLevel(), userEntity.getCompletedQuestIds());
            userEntity.setActiveQuest(newQuest);
            userRepository.save(userEntity);
            if (newQuest != null) {
                return newQuest.toQuestConfig();
            }
        }
        return null;
    }

    @Transactional
    public void setActiveQuest(int userId, int questId) {
        UserEntity userEntity = userRepository.getReferenceById(userId);
        userEntity.setActiveQuest(questConfigService.getEntity(questId));
        userRepository.save(userEntity);
    }

    @Transactional
    public QuestConfig findActiveQuestConfig4CurrentUser(int userId) {
        return getActiveQuest(userId);
    }

    @Transactional
    public void persistXp(int userId, int xp) {
        UserEntity userEntity = userRepository.getReferenceById(userId);
        userEntity.setXp(xp);
        userRepository.save(userEntity);
    }

    @Transactional
    public void addCompletedServerQuest(Integer userId, QuestConfig questConfig) {
        UserEntity userEntity = userRepository.getReferenceById(userId);
        userEntity.addCompletedQuest(questConfigService.getEntity(questConfig.getId()));
        userEntity.setActiveQuest(null);
        userRepository.save(userEntity);
    }

    @Transactional
    public List<Integer> findActivePassedQuestId(int userId) {
        List<Integer> ids = new ArrayList<>();
        UserEntity userEntity = userRepository.getReferenceById(userId);
        if (userEntity.getActiveQuest() != null) {
            ids.add(userEntity.getActiveQuest().getId());
        }
        List<Integer> completedIds = userEntity.getCompletedQuestIds();
        if (completedIds != null) {
            ids.addAll(completedIds);
        }
        return ids;
    }

    @Transactional
    public QuestConfig getActiveQuest(int userId) {
        QuestConfigEntity questConfigEntity = userRepository.getReferenceById(userId).getActiveQuest();
        if (questConfigEntity != null) {
            return questConfigEntity.toQuestConfig();
        }
        return null;
    }

    @Transactional
    public void clearActiveQuest(int userId) {
        UserEntity userEntity = userRepository.getReferenceById(userId);
        userEntity.setActiveQuest(null);
        userRepository.save(userEntity);
    }

    @Transactional
    public Collection<Integer> unlockedEntityIds(int userId) {
        var unlocks = userRepository.getReferenceById(userId).getLevelUnlockEntities();
        if (unlocks != null) {
            return unlocks.stream()
                    .map(LevelUnlockEntity::getId)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

}

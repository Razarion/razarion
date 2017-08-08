package com.btxtech.server.user;

import com.btxtech.server.persistence.GameUiControlConfigPersistence;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity_;
import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.server.system.FilePropertiesService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.quest.QuestService;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 21.02.2017.
 */
@Singleton
public class UserService {
    private static final int DEBUG_LEVEL_ID = 5;
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Logger logger;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private FilePropertiesService filePropertiesService;
    @Inject
    private LevelPersistence levelPersistence;
    @Inject
    private Instance<GameUiControlConfigPersistence> gameUiControlConfigPersistence;
    @Inject
    private Instance<HistoryPersistence> historyPersistence;
    @Inject
    private SessionService sessionService;
    @Inject
    private QuestService questService;
    @Inject
    private ServerGameEnginePersistence serverGameEnginePersistence;
    private Map<String, UserContext> loggedInUserContext = new HashMap<>();

    @Transactional
    public UserContext getUserContext() {
        UserContext userContext = sessionHolder.getPlayerSession().getUserContext();
        if (userContext == null) {
            userContext = createUnregisteredUserContext();
            loginUserContext(userContext, new UnregisteredUser());
        }
        return userContext;
    }

    @Transactional
    public UserContext handleFacebookUserLogin(String facebookUserId) {
        // TODO verify facebook signedRequest

        UserEntity userEntity = getUserForFacebookId(facebookUserId);
        if (userEntity == null) {
            userEntity = createUser(facebookUserId);
        }
        UserContext userContext = userEntity.createUser();
        HumanPlayerId alreadyLoggerIn = null;
        if (sessionHolder.getPlayerSession().getUserContext() != null) {
            alreadyLoggerIn = sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId();
        }
        if (alreadyLoggerIn != null && alreadyLoggerIn.getUserId() != null && alreadyLoggerIn.getUserId().equals(userContext.getHumanPlayerId().getUserId())) {
            return sessionHolder.getPlayerSession().getUserContext();
        }
        return loginUserContext(userContext, null);
    }

    @Transactional
    public void handleUnregisteredLogin() {
        logger.warning("handleUnregisteredLogin: " + sessionHolder.getPlayerSession().getHttpSessionId());
        UserContext userContext = createUnregisteredUserContext();
        loginUserContext(userContext, new UnregisteredUser());
    }

    private UserContext createUnregisteredUserContext() {
        UserContext userContext = new UserContext();
        userContext.setHumanPlayerId(new HumanPlayerId().setPlayerId(createHumanPlayerId().getId()));
        if (filePropertiesService.isDeveloperMode()) {
            userContext.setLevelId(DEBUG_LEVEL_ID);
        } else {
            userContext.setLevelId(levelPersistence.getStarterLevel().getId());
        }
        userContext.setName("Unregistered User");
        return userContext;
    }

    private UserContext loginUserContext(UserContext userContext, UnregisteredUser unregisteredUser) {
        PlayerSession playerSession = sessionHolder.getPlayerSession();
        playerSession.setUserContext(userContext);
        playerSession.setUnregisteredUser(unregisteredUser);
        loggedInUserContext.put(playerSession.getHttpSessionId(), userContext);
        return userContext;
    }

    private UserEntity createUser(String facebookUserId) {
        UserEntity userEntity = new UserEntity();
        userEntity.fromFacebookUserLoginInfo(facebookUserId, createHumanPlayerId(), sessionHolder.getPlayerSession().getLocale());
        userEntity.setLevel(levelPersistence.getStarterLevel());
        entityManager.persist(userEntity);
        return userEntity;
    }

    @Transactional
    public UserEntity getUserForFacebookId(String facebookUseId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = userQuery.from(UserEntity.class);
        CriteriaQuery<UserEntity> userSelect = userQuery.select(from);
        userSelect.where(criteriaBuilder.equal(from.get(UserEntity_.facebookUserId), facebookUseId));
        List<UserEntity> list = entityManager.createQuery(userQuery).getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            throw new IllegalStateException("More then one user for facebook id: " + facebookUseId);
        } else {
            return list.get(0);
        }
    }

    @Transactional
    public HumanPlayerIdEntity createHumanPlayerId() {
        HumanPlayerIdEntity humanPlayerIdEntity = new HumanPlayerIdEntity();
        humanPlayerIdEntity.setTimeStamp(new Date());
        humanPlayerIdEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        entityManager.persist(humanPlayerIdEntity);
        return humanPlayerIdEntity;
    }

    public HumanPlayerIdEntity getHumanPlayerId(Integer id) {
        if (id == null) {
            return null;
        }
        HumanPlayerIdEntity humanPlayerIdEntity = entityManager.find(HumanPlayerIdEntity.class, id);
        if (humanPlayerIdEntity == null) {
            throw new IllegalArgumentException("No HumanPlayerIdEntity for id: " + id);
        }
        return humanPlayerIdEntity;
    }

    public void logoutUserUser(String sessionId) {
        loggedInUserContext.remove(sessionId);
    }

    public UserContext getLoggedInUser(String sessionId) {
        UserContext userContext = loggedInUserContext.get(sessionId);
        if (userContext == null) {
            throw new IllegalArgumentException("No User for sessionId: " + sessionId);
        }
        return userContext;
    }

    @Transactional
    public void onLevelUpdate(String sessionId, int newLevelId) {
        LevelEntity newLevel = levelPersistence.getLevel4Id(newLevelId);
        PlayerSession playerSession = sessionService.getSession(sessionId);
        UserContext userContext = playerSession.getUserContext();
        historyPersistence.get().onLevelUp(userContext.getHumanPlayerId(), newLevel);

        // Temporary: Only save the level if on multiplayer planet. Main reason, tutorial state und units are not saved.
        if (gameUiControlConfigPersistence.get().load4Level(newLevelId).getGameEngineMode() == GameEngineMode.SLAVE) {
            userContext.setLevelId(newLevelId);
            QuestConfigEntity newQuest = null;
            if (userContext.getHumanPlayerId().getUserId() != null) {
                UserEntity userEntity = getUserEntity(userContext.getHumanPlayerId().getUserId());
                userEntity.setLevel(newLevel);
                if (userEntity.getActiveQuest() == null) {
                    newQuest = serverGameEnginePersistence.getQuestConfigEntity2(userEntity.getLevel(), userEntity.getCompletedQuest());
                    userEntity.setActiveQuest(newQuest);
                }
                entityManager.merge(userEntity);
            } else {
                newQuest = serverGameEnginePersistence.getQuestConfigEntity(newLevel, sessionService.getSession(sessionId).getUnregisteredUser().getCompletedQuestIds());
            }
            if (newQuest != null) {
                questService.activateCondition(userContext.getHumanPlayerId(), newQuest.toQuestConfig(playerSession.getLocale()));
            }
        }
    }

    public UserContext getLoggedInUserContext(String sessionId) {
        UserContext userContext = loggedInUserContext.get(sessionId);
        if (userContext == null) {
            throw new IllegalArgumentException("No userContext for sessionId: " + sessionId);
        }
        return userContext;
    }

    private UserEntity getUserEntity(int userId) {
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        if (userEntity == null) {
            throw new IllegalArgumentException("No UserEntity for userId: " + userId);
        }
        return userEntity;
    }

    @Transactional
    public Map<HumanPlayerId, QuestConfig> findUserQuestForPlanet(Collection<Integer> questIds) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = userQuery.from(UserEntity.class);
        userQuery.select(from);
        userQuery.where(from.join(UserEntity_.activeQuest).get(QuestConfigEntity_.id).in(questIds));

        return entityManager.createQuery(userQuery).getResultList().stream().collect(Collectors.toMap(UserEntity::createHumanPlayerId, user -> user.getActiveQuest().toQuestConfig(user.getLocale()), (a, b) -> b));
    }
}

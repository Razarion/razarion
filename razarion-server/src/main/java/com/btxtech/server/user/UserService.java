package com.btxtech.server.user;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.mgmt.QuestBackendInfo;
import com.btxtech.server.mgmt.UnlockedBackendInfo;
import com.btxtech.server.mgmt.UserBackendInfo;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity_;
import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.server.system.FilePropertiesService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.ErrorResult;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.SetNameResult;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    private ServerGameEnginePersistence serverGameEnginePersistence;
    @Inject
    private SessionService sessionService;
    @Inject
    private Instance<ServerGameEngineControl> serverGameEngine;
    @Inject
    private Instance<HistoryPersistence> historyPersistence;

    @Transactional
    public UserContext getUserContextFromSession() {
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
        historyPersistence.get().onUserLoggedIn(userEntity, sessionHolder.getPlayerSession().getHttpSessionId());
        UserContext userContext = userEntity.toUserContext();
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
        // if (filePropertiesService.isDeveloperMode()) {
        //    userContext.setLevelId(DEBUG_LEVEL_ID);
        //} else {
        userContext.setLevelId(levelPersistence.getStarterLevel().getId());
        userContext.setUnlockedItemLimit(ServerUnlockService.convertUnlockedItemLimit(levelPersistence.getStartUnlockedItemLimit()));
        //}
        return userContext;
    }

    private UserContext loginUserContext(UserContext userContext, UnregisteredUser unregisteredUser) {
        PlayerSession playerSession = sessionHolder.getPlayerSession();
        playerSession.setUserContext(userContext);
        playerSession.setUnregisteredUser(unregisteredUser);
        return userContext;
    }

    private UserEntity createUser(String facebookUserId) {
        UserEntity userEntity = new UserEntity();
        userEntity.fromFacebookUserLoginInfo(facebookUserId, createHumanPlayerId(), sessionHolder.getPlayerSession().getLocale());
        userEntity.setLevel(levelPersistence.getStarterLevel());
        userEntity.setLevelUnlockEntities(levelPersistence.getStartUnlockedItemLimit());
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

    @Transactional
    public HumanPlayerId findHumanPlayerId(int playerId) {
        UserEntity userEntity = getUserEntity4PlayerId(playerId);
        if (userEntity != null) {
            return userEntity.createHumanPlayerId();
        } else {
            return new HumanPlayerId().setPlayerId(playerId);
        }
    }

    @Transactional
    public void persistLevel(int userId, LevelEntity newLevel) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.setLevel(newLevel);
        entityManager.merge(userEntity);
    }

    @Transactional
    public void persistXp(int userId, int xp) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.setXp(xp);
        entityManager.merge(userEntity);
    }

    @Transactional
    public void persistAddInventoryItem(int userId, InventoryItemEntity inventoryItemEntity) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.addInventoryItem(inventoryItemEntity);
        entityManager.merge(userEntity);
    }

    @Transactional
    public void persistRemoveInventoryItem(int userId, InventoryItemEntity inventoryItemEntity) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.removeInventoryItem(inventoryItemEntity);
        entityManager.merge(userEntity);
    }

    @Transactional
    public void persistAddCrystals(int userId, int crystals) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.addCrystals(crystals);
        entityManager.merge(userEntity);
    }

    @Transactional
    @SecurityCheck
    public void persistCrystals(int userId, int crystals) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.setCrystals(crystals);
        entityManager.merge(userEntity);
    }

    @Transactional
    public void persistUnlockViaCrystals(int userId, int levelUnlockEntityId) {
        UserEntity userEntity = getUserEntity(userId);
        LevelUnlockEntity levelUnlockEntity = levelPersistence.readLevelUnlockEntity(levelUnlockEntityId);
        if (levelUnlockEntity.getCrystalCost() > userEntity.getCrystals()) {
            throw new IllegalArgumentException("User does not have enough crystals to unlock LevelUnlockEntity. User id: " + userEntity.getId() + " LevelUnlockEntity id: " + levelUnlockEntity.getId());
        }
        userEntity.addLevelUnlockEntity(levelUnlockEntity);
        userEntity.removeCrystals(levelUnlockEntity.getCrystalCost());
    }

    @Transactional
    @SecurityCheck
    public void persistRemoveUnlocked(int userId, int levelUnlockEntityId) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.getLevelUnlockEntities().removeIf(levelUnlockEntity -> levelUnlockEntity.getId() == levelUnlockEntityId);
        entityManager.merge(userEntity);
    }

    @Transactional
    public QuestConfig getAndSaveNewQuest(Integer userId) {
        UserEntity userEntity = getUserEntity(userId);
        if (userEntity.getActiveQuest() == null) {
            QuestConfigEntity newQuest = serverGameEnginePersistence.getQuest4LevelAndCompleted(userEntity.getLevel(), userEntity.getCompletedQuestIds());
            userEntity.setActiveQuest(newQuest);
            entityManager.merge(userEntity);
            if (newQuest != null) {
                return newQuest.toQuestConfig(userEntity.getLocale());
            }
        }
        return null;
    }

    @Transactional
    public void setActiveQuest(int userId, int questId) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.setActiveQuest(entityManager.find(QuestConfigEntity.class, questId));
        entityManager.merge(userEntity);
    }

    @Transactional
    public QuestConfig getActiveQuest(int userId, Locale locale) {
        QuestConfigEntity questConfigEntity = getUserEntity(userId).getActiveQuest();
        if (questConfigEntity != null) {
            return questConfigEntity.toQuestConfig(locale);
        }
        return null;
    }

    @Transactional
    public void clearActiveQuest(int userId) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.setActiveQuest(null);
        entityManager.merge(userEntity);
    }

    @Transactional
    public List<Integer> findActivePassedQuestId(int userId) {
        List<Integer> ids = new ArrayList<>();
        UserEntity userEntity = getUserEntity(userId);
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
    public void addCompletedServerQuest(Integer userId, QuestConfig questConfig) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.addCompletedQuest(entityManager.find(QuestConfigEntity.class, questConfig.getId()));
        userEntity.setActiveQuest(null);
        entityManager.merge(userEntity);
    }

    @Transactional
    public Map<HumanPlayerId, QuestConfig> findActiveQuests4Users(Collection<Integer> questIds) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = userQuery.from(UserEntity.class);
        userQuery.select(from);
        if (questIds != null && !questIds.isEmpty()) {
            userQuery.where(from.join(UserEntity_.activeQuest).get(QuestConfigEntity_.id).in(questIds));
        }

        return entityManager.createQuery(userQuery).getResultList().stream().collect(Collectors.toMap(UserEntity::createHumanPlayerId, user -> user.getActiveQuest().toQuestConfig(user.getLocale()), (a, b) -> b));
    }

    @Transactional
    public QuestConfig findActiveQuestConfig4CurrentUser(Locale locale) {
        Integer userId = sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId().getUserId();
        if (userId != null) {
            return getActiveQuest(userId, locale);
        } else {
            return sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest();
        }
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

    public UserEntity getUserEntity(int userId) {
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        if (userEntity == null) {
            throw new IllegalArgumentException("No UserEntity for userId: " + userId);
        }
        return userEntity;
    }

    @Transactional
    public UserBackendInfo findUserBackendInfo(int playerId) {
        UserEntity userEntity = getUserEntity4PlayerId(playerId);
        if (userEntity == null) {
            return null;
        }
        UserBackendInfo userBackendInfo = new UserBackendInfo().setName(userEntity.getName()).setRegisterDate(userEntity.getRegisterDate()).setFacebookId(userEntity.getFacebookUserId());
        userBackendInfo.setHumanPlayerId(userEntity.createHumanPlayerId()).setLevelNumber(userEntity.getLevel().getNumber()).setXp(userEntity.getXp()).setCrystals(userEntity.getCrystals());
        if (userEntity.getActiveQuest() != null) {
            userBackendInfo.setActiveQuest(new QuestBackendInfo().setId(userEntity.getActiveQuest().getId()).setInternalName(userEntity.getActiveQuest().getInternalName()));
        }
        if (userEntity.getCompletedQuestIds() != null && !userEntity.getCompletedQuestIds().isEmpty()) {
            userBackendInfo.setCompletedQuests(userEntity.getCompletedQuest().stream().map(questConfigEntity -> new QuestBackendInfo().setId(questConfigEntity.getId()).setInternalName(questConfigEntity.getInternalName())).collect(Collectors.toList()));
        }
        if (userEntity.getLevelUnlockEntities() != null && !userEntity.getLevelUnlockEntities().isEmpty()) {
            userBackendInfo.setUnlockedBackendInfos(userEntity.getLevelUnlockEntities().stream().map(levelUnlockEntity -> new UnlockedBackendInfo().setId(levelUnlockEntity.getId()).setInternalName(levelUnlockEntity.getInternalName())).collect(Collectors.toList()));
        }
        return userBackendInfo;
    }

    @Transactional
    public UserBackendInfo removeCompletedQuest(int playerId, int questId) {
        UserEntity userEntity = getUserEntity4PlayerId(playerId);
        if (userEntity == null) {
            return null;
        }
        userEntity.removeCompletedQuest(entityManager.find(QuestConfigEntity.class, questId));
        entityManager.merge(userEntity);
        return findUserBackendInfo(playerId);
    }

    private UserEntity getUserEntity4PlayerId(int playerId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = userQuery.from(UserEntity.class);
        CriteriaQuery<UserEntity> userSelect = userQuery.select(from);
        userSelect.where(criteriaBuilder.equal(from.join(UserEntity_.humanPlayerIdEntity).get(HumanPlayerIdEntity_.id), playerId));
        List<UserEntity> list = entityManager.createQuery(userQuery).getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            throw new IllegalStateException("More then one user for playerId id: " + playerId);
        }
        return list.get(0);
    }

    public UserContext getUserContext(HumanPlayerId humanPlayerId) {
        boolean registered = humanPlayerId.getUserId() != null;
        if (registered) {
            PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
            if (playerSession != null) {
                return playerSession.getUserContext();
            } else {
                return getUserEntity(humanPlayerId.getUserId()).toUserContext();
            }
        } else {
            PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
            if (playerSession == null) {
                throw new IllegalStateException("Unregistered user is no longer online: " + humanPlayerId);
            }
            return playerSession.getUserContext();
        }
    }

    @Transactional
    public UserContext getUserContextTransactional(HumanPlayerId humanPlayerId) {
        return getUserContext(humanPlayerId);
    }

    @Transactional
    public InventoryInfo readInventoryInfo(int userId) {
        return getUserEntity(userId).toInventoryInfo();
    }

    @Transactional
    public int readCrystals(int userId) {
        return getUserEntity(userId).getCrystals();
    }

    @Transactional
    public UserContext readUserContext(int userId) {
        return getUserEntity(userId).toUserContext();
    }

    @Transactional
    public Collection<Integer> unlockedEntityIds(int userId) {
        return getUserEntity(userId).getLevelUnlockEntities().stream().map(LevelUnlockEntity::getId).collect(Collectors.toList());
    }

    @Transactional
    public SetNameResult setName(String name) {
        ErrorResult errorResult = verifySetName(name);
        if (errorResult != null) {
            return new SetNameResult().setErrorResult(errorResult);
        }
        UserContext userContext = getUserContextFromSession();
        UserEntity userEntity = entityManager.find(UserEntity.class, userContext.getHumanPlayerId().getUserId());
        userEntity.setName(name);
        entityManager.merge(userEntity);
        userContext.setName(name);
        serverGameEngine.get().updateUserName(userContext, name);
        return new SetNameResult().setUserContext(userContext);
    }

    @Transactional
    public ErrorResult verifySetName(String name) {
        if (name == null || name.isEmpty()) {
            return ErrorResult.TO_SHORT;
        }

        if (name.length() < 3) {
            return ErrorResult.TO_SHORT;
        }
        UserContext userContext = getUserContextFromSession();
        if (!userContext.checkRegistered()) {
            throw new IllegalStateException("Only registered user chan set a name: " + userContext);
        }
        if (userContext.checkName()) {
            throw new IllegalStateException("The name has already been set: " + userContext);
        }

        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        final Root<UserEntity> from = criteriaQuery.from(UserEntity.class);
        criteriaQuery.select(criteriaBuilder.count(from));
        criteriaQuery.where(criteriaBuilder.equal(from.get(UserEntity_.name), name));
        TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);
        if (typedQuery.getSingleResult() > 0) {
            return ErrorResult.ALREADY_USED;
        }
        return null;
    }

    @Transactional
    @SecurityCheck
    public List<NewUser> findNewUsers() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = userQuery.from(UserEntity.class);
        userQuery.orderBy(criteriaBuilder.desc(from.get(UserEntity_.registerDate)));
        return entityManager.createQuery(userQuery).setMaxResults(20).getResultList().stream().map(userEntity -> new NewUser().setId(userEntity.getId()).setName(userEntity.getName()).setDate(userEntity.getRegisterDate()).setPlayerId(userEntity.getHumanPlayerIdEntity().getId()).setSessionId(entityManager.find(HumanPlayerIdEntity.class, userEntity.getHumanPlayerIdEntity().getId()).getSessionId())).collect(Collectors.toList());
    }
}

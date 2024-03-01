package com.btxtech.server.user;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity_;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.AdditionUserInfo;
import com.btxtech.shared.datatypes.ErrorResult;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.datatypes.RegisterInfo;
import com.btxtech.shared.datatypes.SetNameResult;
import com.btxtech.shared.datatypes.UserAccountInfo;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.dto.LoginResult;
import com.btxtech.shared.dto.RegisterResult;
import com.btxtech.shared.dto.UserBackendInfo;
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
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 21.02.2017.
 */
@Singleton
public class UserService {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private HttpSession httpSession;
    @Inject
    private Logger logger;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private LevelCrudPersistence levelCrudPersistence;
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;
    @Inject
    private SessionService sessionService;
    @Inject
    private Instance<ServerGameEngineControl> serverGameEngine;
    @Inject
    private Instance<HistoryPersistence> historyPersistence;
    @Inject
    private RegisterService registerService;

    @Transactional
    public UserContext getUserContextFromSession() {
        UserContext userContext = sessionHolder.getPlayerSession().getUserContext();
        if (userContext == null) {
            userContext = createAnonymousUser();
            loginUserContext(userContext, new UnregisteredUser());
        }
        return userContext;
    }

    @Transactional
    public LoginResult loginUser(String email, String password) {
        if (sessionHolder.isLoggedIn()) {
            throw new IllegalStateException("User is already logged in: " + sessionHolder.getPlayerSession().getUserContext());
        }

        UserEntity userEntity = getUserEntity4Email(email);
        if (userEntity == null) {
            return LoginResult.WRONG_EMAIL;
        }

        if (!registerService.verifySHA512SecurePassword(userEntity.getPasswordHash(), password)) {
            return LoginResult.WRONG_PASSWORD;
        }
        historyPersistence.get().onUserLoggedIn(userEntity, sessionHolder.getPlayerSession().getHttpSessionId());
        loginUserContext(userEntity.toUserContext(), null);
        return LoginResult.OK;
    }

    @Transactional
    public void autoLoginUser(String email) {
        if (sessionHolder.isLoggedIn()) {
            throw new IllegalStateException("User is already logged in: " + sessionHolder.getPlayerSession().getUserContext());
        }

        UserEntity userEntity = getUserEntity4Email(email);
        if (userEntity == null) {
            throw new IllegalArgumentException("No user for email: " + email);
        }

        historyPersistence.get().onUserLoggedIn(userEntity, sessionHolder.getPlayerSession().getHttpSessionId());
        loginUserContext(userEntity.toUserContext(), null);
    }

    public UserEntity getUserEntity4Email(String email) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> criteriaQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = criteriaQuery.from(UserEntity.class);
        criteriaQuery.select(from);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(from.get(UserEntity_.email), email),
                criteriaBuilder.isNull(from.get(UserEntity_.verificationTimedOutDate))
        ));
        List<UserEntity> users = entityManager.createQuery(criteriaQuery).getResultList();
        if (users == null || users.isEmpty()) {
            return null;
        }
        if (users.size() != 1) {
            logger.warning("UserService: more then one user found for email: " + email + ". SessionId: " + sessionHolder.getPlayerSession().getHttpSessionId());
        }
        return users.get(0);
    }

    public void logout() {
        httpSession.invalidate();
    }

    @Transactional
    public RegisterResult createUnverifiedUserAndLogin(String email, String password) {
        if (email == null || email.isEmpty()) {
            return RegisterResult.INVALID_EMAIL;
        }
        if (password == null || password.isEmpty()) {
            return RegisterResult.INVALID_PASSWORD;
        }
        if (sessionHolder.isLoggedIn()) {
            logger.warning("User is already logged in: " + sessionHolder.getPlayerSession().getUserContext() + ". SessionId: " + sessionHolder.getPlayerSession().getHttpSessionId());
            return RegisterResult.USER_ALREADY_LOGGED_IN;
        }
        ErrorResult errorResult = verifyEmail(email);
        if (errorResult != null) {
            switch (errorResult) {
                case TO_SHORT:
                    return RegisterResult.INVALID_EMAIL;
                case ALREADY_USED:
                    return RegisterResult.EMAIL_ALREADY_USED;
                case UNKNOWN_ERROR:
                    return RegisterResult.UNKNOWN_ERROR;
                default:
                    logger.warning("verifyEmail(email): " + email + ". Unknown result: " + errorResult);
            }
        }
        UserEntity userEntity = createUser(email, password);
        historyPersistence.get().onUserLoggedIn(userEntity, sessionHolder.getPlayerSession().getHttpSessionId());
        registerService.startEmailVerifyingProcess(userEntity);
        UserContext userContext = userEntity.toUserContext();
        loginUserContext(userContext, null);
        serverGameEngine.get().updateHumanPlayerId(userContext);
        return RegisterResult.OK;
    }

    @Transactional
    public UserContext handleFacebookUserLogin(FbAuthResponse fbAuthResponse) {
        if (sessionHolder.isLoggedIn()) {
            String loggedInFacebookUserId = getUserEntity(sessionHolder.getPlayerSession().getUserContext().getUserId()).getFacebookUserId();
            if (fbAuthResponse.getUserID().equals(loggedInFacebookUserId)) {
                return sessionHolder.getPlayerSession().getUserContext();
            } else {
                logger.warning("UserService: loggedInFacebookUserId != facebookUserId. loggedInFacebookUserId: " + loggedInFacebookUserId + ". facebookUserId: " + fbAuthResponse.getUserID() + ". SessionId: " + sessionHolder.getPlayerSession().getHttpSessionId());
            }
        }

        // TODO verify facebook signedRequest
        UserEntity userEntity = getUserForFacebookId(fbAuthResponse.getUserID());
        if (userEntity == null) {
            userEntity = createFacebookUser(fbAuthResponse.getUserID());
        }
        historyPersistence.get().onUserLoggedIn(userEntity, sessionHolder.getPlayerSession().getHttpSessionId());
        UserContext userContext = userEntity.toUserContext();

        return loginUserContext(userContext, null);
    }

    @Transactional
    public RegisterInfo handleInGameFacebookUserLogin(FbAuthResponse fbAuthResponse) {
//   TODO     if (sessionHolder.getPlayerSession().getUserContext() == null) {
//            throw new IllegalStateException("sessionHolder.getPlayerSession().getUserContext() == null");
//        }
//        if (sessionHolder.isLoggedIn()) {
//            throw new IllegalStateException("User is already logged in: " + sessionHolder.getPlayerSession().getUserContext());
//        }
//
//        // TODO verify facebook signedRequest
//  TODO      RegisterInfo registerInfo = new RegisterInfo().setUserAlreadyExits(true);
//        UserEntity userEntity = getUserForFacebookId(fbAuthResponse.getUserID());
//        if (userEntity == null) {
//            userEntity = createFacebookUserFromUnregistered(fbAuthResponse.getUserID());
//            registerInfo.setUserAlreadyExits(false);
//        }
//        historyPersistence.get().onUserLoggedIn(useEntity, sessionHolder.getPlayerSession().getHttpSessionId());
//        UserContext userContext = userEntity.toUsrerContext();
//        loginUserContext(userContext, null);
//        if (!registerInfo.isUserAlreadyExits()) {
//            registerInfo.setHumanPlayerId(userContext.getUserId());
//            serverGameEngine.get().updateHumanPlayerId(userContext);
//        }
//        return registerInfo;
        throw new UnsupportedOperationException("...TODO...");
    }

    private UserContext loginUserContext(UserContext userContext, UnregisteredUser unregisteredUser) {
        PlayerSession playerSession = sessionHolder.getPlayerSession();
        playerSession.setUserContext(userContext);
        playerSession.setUnregisteredUser(unregisteredUser);
        return userContext;
    }

    private UserContext createAnonymousUser() {
        return userEntityFactory(userEntity -> {
            userEntity.fromAnonymus(this.sessionHolder.getPlayerSession().getLocale());
        }).toUserContext();
    }

    private UserEntity createUser(String email, String password) {
        return userEntityFactory(userEntity -> userEntity.fromEmailPasswordHash(
                email,
                registerService.generateSHA512SecurePassword(password),
                this.sessionHolder.getPlayerSession().getLocale()));
    }

    private UserEntity createFacebookUser(String facebookUserId) {
        return userEntityFactory(userEntity -> {
            userEntity.fromFacebookUserLoginInfo(facebookUserId, sessionHolder.getPlayerSession().getLocale());
        });
    }

    private UserEntity userEntityFactory(Consumer<UserEntity> decorator) {
        UserEntity userEntity = new UserEntity();
        userEntity.setLevel(levelCrudPersistence.getStarterLevel());
        userEntity.setLevelUnlockEntities(levelCrudPersistence.getStartUnlockedItemLimit());
        userEntity.setCreationDate(new Date());
        decorator.accept(userEntity);
        entityManager.persist(userEntity);
        return userEntity;

    }

    private UserEntity createFacebookUserFromUnregistered(String facebookUserId) {
        UserEntity userEntity = new UserEntity();
        userEntity.fromFacebookUserLoginInfo(facebookUserId, sessionHolder.getPlayerSession().getLocale());
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
        LevelUnlockEntity levelUnlockEntity = levelCrudPersistence.readLevelUnlockEntity(levelUnlockEntityId);
        if (levelUnlockEntity.getCrystalCost() > userEntity.getCrystals()) {
            throw new IllegalArgumentException("User does not have enough crystals to unlock LevelUnlockEntity. User id: " + userEntity.getId() + " LevelUnlockEntity id: " + levelUnlockEntity.getId());
        }
        userEntity.addLevelUnlockEntity(levelUnlockEntity);
        userEntity.removeCrystals(levelUnlockEntity.getCrystalCost());
    }

    @Transactional
    public QuestConfig getAndSaveNewQuest(Integer userId) {
        UserEntity userEntity = getUserEntity(userId);
        if (userEntity.getActiveQuest() == null) {
            QuestConfigEntity newQuest = serverGameEngineCrudPersistence.getQuest4LevelAndCompleted(userEntity.getLevel(), userEntity.getCompletedQuestIds());
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
    public Map<Integer, QuestConfig> findActiveQuests4Users(Collection<Integer> questIds) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = userQuery.from(UserEntity.class);
        userQuery.select(from);
        if (questIds != null && !questIds.isEmpty()) {
            userQuery.where(from.join(UserEntity_.activeQuest).get(QuestConfigEntity_.id).in(questIds));
        }

        return entityManager.createQuery(userQuery).getResultList().stream().collect(Collectors.toMap(UserEntity::getId, user -> user.getActiveQuest().toQuestConfig(user.getLocale()), (a, b) -> b));
    }

    @Transactional
    public QuestConfig findActiveQuestConfig4CurrentUser(Locale locale) {
        return getActiveQuest(sessionHolder.getPlayerSession().getUserContext().getUserId(),
                locale);
    }

    public UserEntity getUserEntity(int userId) {
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        if (userEntity == null) {
            throw new IllegalArgumentException("No UserEntity for userId: " + userId);
        }
        return userEntity;
    }

    @Transactional
    public List<UserBackendInfo> getUserBackendInfos() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> root = userQuery.from(UserEntity.class);
        CriteriaQuery<UserEntity> userSelect = userQuery.select(root);
        return entityManager.createQuery(userSelect)
                .getResultList()
                .stream()
                .map(UserService::userEntity2UserBackendInfo)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserBackendInfo findUserBackendInfo(int playerId) {
        UserEntity userEntity = getUserEntity4PlayerId(playerId);
        if (userEntity == null) {
            return null;
        }
        return userEntity2UserBackendInfo(userEntity);
    }

    private static UserBackendInfo userEntity2UserBackendInfo(UserEntity userEntity) {
        UserBackendInfo userBackendInfo = new UserBackendInfo()
                .name(userEntity.getName())
                .creationDate(userEntity.getCreationDate())
                .registerDate(userEntity.getRegisterDate())
                .verificationDoneDate(userEntity.getVerificationDoneDate())
                .facebookId(userEntity.getFacebookUserId())
                .email(userEntity.getEmail())
                .userId(userEntity.getId())
                .levelId(extractId(userEntity.getLevel(), LevelEntity::getId))
                .xp(userEntity.getXp())
                .crystals(userEntity.getCrystals())
                .activeQuest(extractId(userEntity.getActiveQuest(), QuestConfigEntity::getId));
        if (userEntity.getCompletedQuestIds() != null && !userEntity.getCompletedQuestIds().isEmpty()) {
            userBackendInfo.completedQuestIds(userEntity.getCompletedQuest().stream().map(QuestConfigEntity::getId).collect(Collectors.toList()));
        }
        if (userEntity.getLevelUnlockEntities() != null && !userEntity.getLevelUnlockEntities().isEmpty()) {
            userBackendInfo.unlockedIds(userEntity.getLevelUnlockEntities().stream().map(LevelUnlockEntity::getId).collect(Collectors.toList()));
        }
        return userBackendInfo;
    }

    @Transactional
    public void setCompletedQuest(int userId, List<Integer> completedQuestIds) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.setCompletedQuest(completedQuestIds.stream().map(questId -> {
            QuestConfigEntity questConfigEntity = entityManager.find(QuestConfigEntity.class, questId);
            if (questConfigEntity == null) {
                throw new IllegalArgumentException("No QuestConfigEntity for id: " + questId);
            }
            return questConfigEntity;
        }).collect(Collectors.toList()));
        entityManager.merge(userEntity);
    }

    @Transactional
    public void setUnlocked(int userId, List<Integer> unlockedIds) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.setLevelUnlockEntities(unlockedIds.stream().map(questId -> {
            LevelUnlockEntity levelUnlockEntity = entityManager.find(LevelUnlockEntity.class, questId);
            if (levelUnlockEntity == null) {
                throw new IllegalArgumentException("No LevelUnlockEntity for id: " + questId);
            }
            return levelUnlockEntity;
        }).collect(Collectors.toList()));
        entityManager.merge(userEntity);
    }


    private UserEntity getUserEntity4PlayerId(int playerId) {
        throw new UnsupportedOperationException("...TODO...");
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
//        Root<UserEntity> from = userQuery.from(UserEntity.class);
//        CriteriaQuery<UserEntity> userSelect = userQuery.select(from);
//        userSelect.where(criteriaBuilder.equal(from.join(UserEntity_.humanPlayerIdEntity).get(HumanPlayerIdEntity_.id), playerId));
//        List<UserEntity> list = entityManager.createQuery(userQuery).getResultList();
//        if (list == null || list.isEmpty()) {
//            return null;
//        } else if (list.size() > 1) {
//            throw new IllegalStateException("More then one user for playerId id: " + playerId);
//        }
//        return list.get(0);
    }

    public UserContext getUserContext(int userId) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            return playerSession.getUserContext();
        } else {
            return getUserEntity(userId).toUserContext();
        }
    }

    @Transactional
    public UserContext getUserContextTransactional(int userId) {
        return getUserContext(userId);
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
        UserEntity userEntity = entityManager.find(UserEntity.class, userContext.getUserId());
        if (!userEntity.isVerified()) {
            throw new IllegalStateException("User is not verified. Id: " + userEntity.getId());
        }
        userEntity.setName(name);
        entityManager.merge(userEntity);
        userContext.name(name);
        serverGameEngine.get().updateUserName(userContext, name);
        return new SetNameResult().setUserName(name);
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
        if (!userContext.registered()) {
            throw new IllegalStateException("Only registered user chan set a name: " + userContext);
        }
        if (userContext.emailNotVerified()) {
            throw new IllegalStateException("Only email verified user can set a name: " + userContext);
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
    public ErrorResult verifyEmail(String email) {
        if (email == null || email.isEmpty()) {
            return ErrorResult.TO_SHORT;
        }
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        final Root<UserEntity> from = criteriaQuery.from(UserEntity.class);
        criteriaQuery.select(criteriaBuilder.count(from));
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(from.get(UserEntity_.email), email),
                criteriaBuilder.isNull(from.get(UserEntity_.verificationTimedOutDate))
        ));
        if (entityManager.createQuery(criteriaQuery).getSingleResult() > 0) {
            return ErrorResult.ALREADY_USED;
        }
        return null;
    }

    @Transactional
    @SecurityCheck
    public List<NewUser> findNewUsers() {
// TODO       CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
// TODO       CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
// TODO       Root<UserEntity> from = userQuery.from(UserEntity.class);
// TODO       userQuery.orderBy(criteriaBuilder.desc(from.get(UserEntity_.registerDate)));
// TODO       return entityManager.createQuery(userQuery).setMaxResults(20).getResultList().stream().map(userEntity -> new NewUser().setId(userEntity.getId()).setName(userEntity.getName()).setDate(userEntity.getRegisterDate()).setPlayerId(userEntity.getHumanPlayerIdEntity().getId()).setSessionId(entityManager.find(HumanPlayerIdEntity.class, userEntity.getHumanPlayerIdEntity().getId()).getSessionId())).collect(Collectors.toList());
        throw new UnsupportedOperationException("...TODO...");
    }

    @Transactional
    public UserAccountInfo getUserAccountInfo() {
        UserAccountInfo userAccountInfo = new UserAccountInfo();
        if (!sessionHolder.isLoggedIn()) {
            throw new IllegalArgumentException("User is not logged in: " + sessionHolder.getPlayerSession().getHttpSessionId());
        }
        UserEntity userEntity = getUserEntity(sessionHolder.getPlayerSession().getUserContext().getUserId());
        userAccountInfo.setEmail(userEntity.getEmail());
        return userAccountInfo;
    }

    @SecurityCheck
    @Transactional
    public List<AdditionUserInfo> additionUserInfo() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
        userQuery.from(UserEntity.class);
        List<AdditionUserInfo> additionUserInfos = new ArrayList<>();
        entityManager.createQuery(userQuery).getResultList().forEach(userEntity -> {
            AdditionUserInfo additionUserInfo = new AdditionUserInfo();
            additionUserInfo.setUserId(userEntity.getId());
            additionUserInfo.setLastLoggedIn(historyPersistence.get().readLastLoginDate(userEntity));
            additionUserInfos.add(additionUserInfo);
        });
        return additionUserInfos;
    }

    @SecurityCheck
    @Transactional
    public Map<Integer, String> getAllHumanPlayerId2RegisteredUserName() {
// TODO       CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
// TODO       CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
// TODO       Root<UserEntity> root = cq.from(UserEntity.class);
// TODO       cq.multiselect(root.get(UserEntity_.humanPlayerIdEntity).get(HumanPlayerIdEntity_.id), root.get(UserEntity_.name));
// TODO       Map<Integer, String> humanPlayerId2RegisteredUserName = new HashMap<>();
// TODO       entityManager.createQuery(cq).getResultList().forEach(tuple -> {
// TODO           String name = tuple.get(1) != null ? tuple.get(1).toString() : null;
// TODO           if (name != null) {
// TODO               humanPlayerId2RegisteredUserName.put((int) tuple.get(0), name);
// TODO           }
// TODO       });
// TODO       return humanPlayerId2RegisteredUserName;
        throw new UnsupportedOperationException("...TODO...");
    }

}

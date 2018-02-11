package com.btxtech.server.persistence.history;

import com.btxtech.server.mgmt.GameHistoryEntry;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.persistence.tracker.ConnectionTrackerEntity;
import com.btxtech.server.persistence.tracker.ConnectionTrackerEntity_;
import com.btxtech.server.user.ForgotPasswordEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 22.05.2017.
 */
@Singleton
public class HistoryPersistence {
    private Logger logger = Logger.getLogger(HistoryPersistence.class.getName());
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private UserService userService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private InventoryPersistence inventoryPersistence;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
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

    @Transactional(Transactional.TxType.REQUIRES_NEW)
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

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onBoxPicked(HumanPlayerId humanPlayerId, BoxContent boxContent) {
        try {
            Date date = new Date();
            if (boxContent.getCrystals() > 0) {
                InventoryHistoryEntry inventoryHistoryEntry = new InventoryHistoryEntry();
                inventoryHistoryEntry.setHumanPlayerIdEntityId(humanPlayerId.getPlayerId());
                inventoryHistoryEntry.setTimeStamp(date);
                inventoryHistoryEntry.setType(InventoryHistoryEntry.Type.BOX_PICKED);
                inventoryHistoryEntry.setCrystals(boxContent.getCrystals());
                entityManager.persist(inventoryHistoryEntry);
            }
            if (boxContent.getInventoryItems() != null) {
                boxContent.getInventoryItems().forEach(inventoryItem -> {
                    InventoryHistoryEntry inventoryHistoryEntry = new InventoryHistoryEntry();
                    inventoryHistoryEntry.setHumanPlayerIdEntityId(humanPlayerId.getPlayerId());
                    inventoryHistoryEntry.setTimeStamp(date);
                    inventoryHistoryEntry.setType(InventoryHistoryEntry.Type.BOX_PICKED);
                    inventoryHistoryEntry.setInventoryItemId(inventoryItem.getId());
                    inventoryHistoryEntry.setInventoryItemName(inventoryItem.getInternalName());
                    entityManager.persist(inventoryHistoryEntry);
                });
            }
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onInventoryItemUsed(HumanPlayerId humanPlayerId, int inventoryItemId) {
        try {
            Date date = new Date();
            InventoryHistoryEntry inventoryHistoryEntry = new InventoryHistoryEntry();
            inventoryHistoryEntry.setHumanPlayerIdEntityId(humanPlayerId.getPlayerId());
            inventoryHistoryEntry.setTimeStamp(date);
            inventoryHistoryEntry.setType(InventoryHistoryEntry.Type.INVENTORY_ITEM_USED);
            InventoryItemEntity inventoryItemEntity = inventoryPersistence.readInventoryItemEntity(inventoryItemId);
            inventoryHistoryEntry.setInventoryItemId(inventoryItemId);
            inventoryHistoryEntry.setInventoryItemName(inventoryItemEntity.getInternalName());
            entityManager.persist(inventoryHistoryEntry);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onLevelUnlockEntityUsedViaCrystals(HumanPlayerId humanPlayerId, int levelUnlockEntityId) {
        try {
            Date date = new Date();
            LevelUnlockHistoryEntry inventoryHistoryEntry = new LevelUnlockHistoryEntry();
            inventoryHistoryEntry.setHumanPlayerIdEntityId(humanPlayerId.getPlayerId());
            inventoryHistoryEntry.setTimeStamp(date);
            LevelUnlockEntity levelUnlockEntity = entityManager.find(LevelUnlockEntity.class, levelUnlockEntityId);
            inventoryHistoryEntry.setUnlockEntityId(levelUnlockEntityId);
            inventoryHistoryEntry.setUnlockEntityName(levelUnlockEntity.getInternalName());
            inventoryHistoryEntry.setCrystals(levelUnlockEntity.getCrystalCost());
            entityManager.persist(inventoryHistoryEntry);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onUserLoggedIn(UserEntity userEntity, String httpSessionId) {
        try {
            UserHistoryEntity userHistoryEntity = new UserHistoryEntity();
            userHistoryEntity.setUserId(userEntity.getId());
            userHistoryEntity.setLoggedIn(new Date());
            userHistoryEntity.setSessionId(httpSessionId);
            entityManager.persist(userHistoryEntity);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onUserLoggedOut(int userId, String httpSessionId) {
        try {
            UserHistoryEntity userHistoryEntity = new UserHistoryEntity();
            userHistoryEntity.setUserId(userId);
            userHistoryEntity.setLoggedOut(new Date());
            userHistoryEntity.setSessionId(httpSessionId);
            entityManager.persist(userHistoryEntity);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onForgotPassword(UserEntity userEntity, ForgotPasswordEntity forgotPasswordEntity, ForgotPasswordHistoryEntity.Type type) {
        try {
            ForgotPasswordHistoryEntity historyEntity = new ForgotPasswordHistoryEntity();
            historyEntity.setUserId(userEntity.getId());
            historyEntity.setHumanPlayerId(userEntity.getHumanPlayerIdEntity().getId());
            historyEntity.setTimeStamp(new Date());
            historyEntity.setForgotPasswordEntityId(forgotPasswordEntity.getId());
            historyEntity.setType(type);
            entityManager.persist(historyEntity);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional
    @SecurityCheck
    public List<UserHistoryEntry> readLoginHistory() {
        List<UserHistoryEntity> userHistoryEntities = entityManager.createNativeQuery("select * from HISTORY_USER order by (IFNULL(UNIX_TIMESTAMP(loggedIn), 0) + IFNULL(UNIX_TIMESTAMP(loggedOut), 0)) desc", UserHistoryEntity.class).setMaxResults(100).getResultList();
        List<UserHistoryEntry> userHistoryEntries = new ArrayList<>();
        for (UserHistoryEntity historyEntity : userHistoryEntities) {
            UserHistoryEntry userHistoryEntry = new UserHistoryEntry().setId(historyEntity.getUserId()).setLogin(historyEntity.getLoggedIn()).setLogout(historyEntity.getLoggedOut()).setSessionId(historyEntity.getSessionId());
            UserEntity userEntity = entityManager.find(UserEntity.class, historyEntity.getUserId());
            userHistoryEntry.setName(userEntity.getName()).setPlayerId(userEntity.getHumanPlayerIdEntity().getId());
            userHistoryEntries.add(userHistoryEntry);
        }
        return userHistoryEntries;
    }

    @Transactional
    @SecurityCheck
    public SimpleUserBackend readUserFromHistory(String sessionId) {
        List<UserEntity> userEntities = entityManager.createQuery("select u from UserEntity u where u.id in (select h.userId from UserHistoryEntity h where h.sessionId=:sessionId and h.loggedIn is not null)", UserEntity.class).setParameter("sessionId", sessionId).getResultList();
        if (userEntities.isEmpty()) {
            return null;
        }
        if (userEntities.size() > 1) {
            logger.warning("More the one entry for UserHistoryEntity found for session id: " + sessionId);
        }
        UserEntity userEntity = userEntities.get(0);
        return new SimpleUserBackend().setName(userEntity.getName()).setHumanPlayerId(userEntity.createHumanPlayerId());
    }

    @Transactional
    @SecurityCheck
    public List<GameHistoryEntry> readUserHistory(int playerId) {
        List<GameHistoryEntry> history = new ArrayList<>();
        readAllHistory(entityManager, LevelHistoryEntity.class, playerId, LevelHistoryEntity_.humanPlayerIdEntityId, LevelHistoryEntity_.timeStamp).forEach(levelHistoryEntity -> history.add(new GameHistoryEntry().setDate(levelHistoryEntity.getTimeStamp()).setDescription("Level up: " + levelHistoryEntity.getLevelNumber() + " (" + levelHistoryEntity.getLevelId() + ")")));
        readAllHistory(entityManager, QuestHistoryEntity.class, playerId, QuestHistoryEntity_.humanPlayerIdEntityId, QuestHistoryEntity_.timeStamp).forEach(questHistoryEntity -> {
            GameHistoryEntry gameHistoryEntry = new GameHistoryEntry().setDate(questHistoryEntity.getTimeStamp());
            switch (questHistoryEntity.getType()) {
                case QUEST_ACTIVATED:
                    gameHistoryEntry.setDescription("Quest activated: " + questHistoryEntity.getQuestInternalName() + " (" + questHistoryEntity.getQuestId() + ")");
                    break;
                case QUEST_DEACTIVATED:
                    gameHistoryEntry.setDescription("Quest deactivated: " + questHistoryEntity.getQuestInternalName() + " (" + questHistoryEntity.getQuestId() + ")");
                    break;
                case QUEST_PASSED:
                    gameHistoryEntry.setDescription("Quest passed: " + questHistoryEntity.getQuestInternalName() + " (" + questHistoryEntity.getQuestId() + ")");
                    break;
                default:
                    gameHistoryEntry.setDescription(questHistoryEntity.getType() + " ??? : " + questHistoryEntity.getQuestInternalName() + " (" + questHistoryEntity.getQuestId() + ")");
            }
            history.add(gameHistoryEntry);
        });
        readAllHistory(entityManager, InventoryHistoryEntry.class, playerId, InventoryHistoryEntry_.humanPlayerIdEntityId, InventoryHistoryEntry_.timeStamp).forEach(inventoryHistoryEntry -> {
            GameHistoryEntry gameHistoryEntry = new GameHistoryEntry().setDate(inventoryHistoryEntry.getTimeStamp());
            switch (inventoryHistoryEntry.getType()) {
                case BOX_PICKED:
                    gameHistoryEntry.setDescription("Box picked. Crystals: " + inventoryHistoryEntry.getCrystals() + ". Inventory item " + inventoryHistoryEntry.getInventoryItemName() + " (" + inventoryHistoryEntry.getInventoryItemId() + ")");
                    break;
                case INVENTORY_ITEM_USED:
                    gameHistoryEntry.setDescription("Inventory item used. Inventory item " + inventoryHistoryEntry.getInventoryItemName() + " (" + inventoryHistoryEntry.getInventoryItemId() + ")");
                    break;
                default:
                    gameHistoryEntry.setDescription("Box or Inventory unknown type: " + inventoryHistoryEntry.getType() + " ???");
            }
            history.add(gameHistoryEntry);
        });
        readAllHistory(entityManager, LevelUnlockHistoryEntry.class, playerId, LevelUnlockHistoryEntry_.humanPlayerIdEntityId, LevelUnlockHistoryEntry_.timeStamp).forEach(levelUnlockHistoryEntry -> history.add(new GameHistoryEntry().setDate(levelUnlockHistoryEntry.getTimeStamp()).setDescription("Unlocked. Crystals: " + levelUnlockHistoryEntry.getCrystals() + ". Unlock item " + levelUnlockHistoryEntry.getUnlockEntityName() + " (" + levelUnlockHistoryEntry.getUnlockEntityId() + ")")));
        readAllHistory(entityManager, ConnectionTrackerEntity.class, playerId, ConnectionTrackerEntity_.humanPlayerId, ConnectionTrackerEntity_.timeStamp).forEach(connectionTrackerEntity -> {
            GameHistoryEntry gameHistoryEntry = new GameHistoryEntry().setDate(connectionTrackerEntity.getTimeStamp());
            switch (connectionTrackerEntity.getType()) {
                case SYSTEM_OPEN:
                    gameHistoryEntry.setDescription("System connection open");
                    break;
                case SYSTEM_CLOSE:
                    gameHistoryEntry.setDescription("System connection close");
                    break;
                case GAME_OPEN:
                    gameHistoryEntry.setDescription("Game connection open");
                    break;
                case GAME_CLOSE:
                    gameHistoryEntry.setDescription("Game connection close");
                    break;
                default:
                    gameHistoryEntry.setDescription("Connection unknown type: " + connectionTrackerEntity.getType() + " ???");
            }
            history.add(gameHistoryEntry);
        });
        readAllHistory(entityManager, ForgotPasswordHistoryEntity.class, playerId, ForgotPasswordHistoryEntity_.humanPlayerId, ForgotPasswordHistoryEntity_.timeStamp).forEach(forgotPasswordHistoryEntity -> {
            GameHistoryEntry gameHistoryEntry = new GameHistoryEntry().setDate(forgotPasswordHistoryEntity.getTimeStamp());
            switch (forgotPasswordHistoryEntity.getType()) {
                case INITIATED:
                    gameHistoryEntry.setDescription("Password reset initiated");
                    break;
                case TIMED_OUT:
                    gameHistoryEntry.setDescription("Password reset timed out");
                    break;
                case OVERRIDDEN:
                    gameHistoryEntry.setDescription("Password reset overridden by new password reset");
                    break;
                case CHANGED:
                    gameHistoryEntry.setDescription("Password changed");
                    break;
                default:
                    gameHistoryEntry.setDescription("Password reset unknown type: " + forgotPasswordHistoryEntity.getType() + " ???");
            }
            history.add(gameHistoryEntry);
        });
        history.sort(Comparator.comparing(GameHistoryEntry::getDate));
        return history;
    }

    private <T> List<T> readAllHistory(EntityManager entityManager, Class<T> theClass, int playerId, SingularAttribute<T, Integer> humanPlayerIdAttr, SingularAttribute<T, Date> orderByAttribute) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> userQuery = criteriaBuilder.createQuery(theClass);
        Root<T> from = userQuery.from(theClass);
        CriteriaQuery<T> userSelect = userQuery.select(from);
        userSelect.where(criteriaBuilder.equal(from.get(humanPlayerIdAttr), playerId));
        userQuery.orderBy(criteriaBuilder.desc(from.get(orderByAttribute)));
        return entityManager.createQuery(userSelect).getResultList();
    }
}

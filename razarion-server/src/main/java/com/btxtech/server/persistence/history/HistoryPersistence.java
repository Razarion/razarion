package com.btxtech.server.persistence.history;

import com.btxtech.server.persistence.inventory.InventoryItemCrudPersistence;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.user.ForgotPasswordEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserEntity;
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
import javax.transaction.Transactional;
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
    private ExceptionHandler exceptionHandler;
    @Inject
    private InventoryItemCrudPersistence inventoryPersistence;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onLevelUp(int userId, LevelEntity newLevel) {
        try {
            LevelHistoryEntity levelHistoryEntity = new LevelHistoryEntity();
            levelHistoryEntity.setTimeStamp(new Date());
            // TODO levelHistoryEntity.setHumanPlayerIdEntityId(userService.getHumanPlayerId(userId.getPlayerId()).getId());
            levelHistoryEntity.setLevelId(newLevel.getId());
            levelHistoryEntity.setLevelNumber(newLevel.getNumber());
            // entityManager.persist(levelHistoryEntity);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onQuest(int userId, QuestConfig questConfig, QuestHistoryEntity.Type type) {
        try {
            QuestHistoryEntity questHistoryEntity = new QuestHistoryEntity();
            questHistoryEntity.setTimeStamp(new Date());
            // TODO questHistoryEntity.setHumanPlayerIdEntityId(userService.getHumanPlayerId(humanPlayerId.getPlayerId()).getId());
            questHistoryEntity.setQuestId(questConfig.getId());
            questHistoryEntity.setQuestInternalName(questConfig.getInternalName());
            questHistoryEntity.setType(type);
            // entityManager.persist(questHistoryEntity);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onBoxPicked(int userId, BoxContent boxContent) {
        try {
            Date date = new Date();
            if (boxContent.getCrystals() > 0) {
                InventoryHistoryEntry inventoryHistoryEntry = new InventoryHistoryEntry();
                // TODO inventoryHistoryEntry.setHumanPlayerIdEntityId(userId.getPlayerId());
                inventoryHistoryEntry.setTimeStamp(date);
                inventoryHistoryEntry.setType(InventoryHistoryEntry.Type.BOX_PICKED);
                inventoryHistoryEntry.setCrystals(boxContent.getCrystals());
                // entityManager.persist(inventoryHistoryEntry);
            }
            if (boxContent.getInventoryItems() != null) {
                boxContent.getInventoryItems().forEach(inventoryItem -> {
                    InventoryHistoryEntry inventoryHistoryEntry = new InventoryHistoryEntry();
                    // TODO inventoryHistoryEntry.setHumanPlayerIdEntityId(userId.getPlayerId());
                    inventoryHistoryEntry.setTimeStamp(date);
                    inventoryHistoryEntry.setType(InventoryHistoryEntry.Type.BOX_PICKED);
                    inventoryHistoryEntry.setInventoryItemId(inventoryItem.getId());
                    inventoryHistoryEntry.setInventoryItemName(inventoryItem.getInternalName());
                    // entityManager.persist(inventoryHistoryEntry);
                });
            }
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onInventoryItemUsed(int userId, int inventoryItemId) {
        try {
            Date date = new Date();
            InventoryHistoryEntry inventoryHistoryEntry = new InventoryHistoryEntry();
            // TODO inventoryHistoryEntry.setHumanPlayerIdEntityId(userId.getPlayerId());
            inventoryHistoryEntry.setTimeStamp(date);
            inventoryHistoryEntry.setType(InventoryHistoryEntry.Type.INVENTORY_ITEM_USED);
            InventoryItemEntity inventoryItemEntity = inventoryPersistence.getEntity(inventoryItemId);
            inventoryHistoryEntry.setInventoryItemId(inventoryItemId);
            inventoryHistoryEntry.setInventoryItemName(inventoryItemEntity.getInternalName());
            // entityManager.persist(inventoryHistoryEntry);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onLevelUnlockEntityUsedViaCrystals(int userId, int levelUnlockEntityId) {
        try {
            Date date = new Date();
            LevelUnlockHistoryEntry inventoryHistoryEntry = new LevelUnlockHistoryEntry();
            // TODO inventoryHistoryEntry.setHumanPlayerIdEntityId(userId.getPlayerId());
            inventoryHistoryEntry.setTimeStamp(date);
            LevelUnlockEntity levelUnlockEntity = entityManager.find(LevelUnlockEntity.class, levelUnlockEntityId);
            inventoryHistoryEntry.setUnlockEntityId(levelUnlockEntityId);
            inventoryHistoryEntry.setUnlockEntityName(levelUnlockEntity.getInternalName());
            inventoryHistoryEntry.setCrystals(levelUnlockEntity.getCrystalCost());
            // entityManager.persist(inventoryHistoryEntry);
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
            // entityManager.persist(userHistoryEntity);
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
            // entityManager.persist(userHistoryEntity);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onForgotPassword(UserEntity userEntity, ForgotPasswordEntity forgotPasswordEntity, ForgotPasswordHistoryEntity.Type type) {
        try {
            ForgotPasswordHistoryEntity historyEntity = new ForgotPasswordHistoryEntity();
            historyEntity.setUserId(userEntity.getId());
            // TODO historyEntity.setHumanPlayerId(userEntity.getHumanPlayerIdEntity().getId());
            historyEntity.setTimeStamp(new Date());
            historyEntity.setForgotPasswordEntityId(forgotPasswordEntity.getId());
            historyEntity.setType(type);
            // entityManager.persist(historyEntity);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Transactional
    @SecurityCheck
    public Date readLastLoginDate(UserEntity userEntity) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserHistoryEntity> userQuery = criteriaBuilder.createQuery(UserHistoryEntity.class);
        Root<UserHistoryEntity> from = userQuery.from(UserHistoryEntity.class);
        // TODO userQuery.orderBy(criteriaBuilder.desc(from.get(UserHistoryEntity_.loggedIn)));
        CriteriaQuery<UserHistoryEntity> userSelect = userQuery.select(from);
        // TODO userQuery.where(criteriaBuilder.equal(from.get(UserHistoryEntity_.userId), userEntity.getId()));
        List<UserHistoryEntity> userHistoryEntities = entityManager.createQuery(userSelect).setMaxResults(1).getResultList();
        if (!userHistoryEntities.isEmpty()) {
            return userHistoryEntities.get(0).getLoggedIn();
        }
        return null;
    }

    @Transactional
    @SecurityCheck
    public SimpleUserBackend readUserFromHistory(String sessionId) {
        List<UserEntity> userEntities = entityManager.createQuery("select u from UserEntity u where u.id in (select h.userId from UserHistoryEntity h where h.sessionId=:sessionId and h.loggedIn is not null)", UserEntity.class).setParameter("sessionId", sessionId).getResultList();
        if (userEntities.isEmpty()) {
            return null;
        }
        if (userEntities.size() > 1) {
            logger.warning("More the one entry for UserHistoryEntity found for session id: " + sessionId + " userEntities: " + userEntities);
        }
        UserEntity userEntity = userEntities.get(0);
        return new SimpleUserBackend().setName(userEntity.getName()).setUserId(userEntity.getId());
    }
}

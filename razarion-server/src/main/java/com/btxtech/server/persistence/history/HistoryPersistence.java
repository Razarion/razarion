package com.btxtech.server.persistence.history;

import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
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
    @Inject
    private InventoryPersistence inventoryPersistence;

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

    @Transactional
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

    @Transactional
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

    @Transactional
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

    @Transactional
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

    @Transactional
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
}

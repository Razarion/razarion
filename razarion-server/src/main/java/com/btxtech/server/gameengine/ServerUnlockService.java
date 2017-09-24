package com.btxtech.server.gameengine;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UnregisteredUser;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * on 20.09.2017.
 */
@Singleton
public class ServerUnlockService {
    @Inject
    private UserService userService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ClientSystemConnectionService systemConnectionService;
    @Inject
    private SessionService sessionService;
    @Inject
    private LevelPersistence levelPersistence;
    @Inject
    private HistoryPersistence historyPersistence;

    public void unlockViaCrystals(HumanPlayerId humanPlayerId, int levelUnlockEntityId) {
        UserContext userContext;
        if (humanPlayerId.getUserId() != null) {
            userService.persistUnlockViaCrystals(humanPlayerId.getUserId(), levelUnlockEntityId);
            userContext = userService.readUserContext(humanPlayerId.getUserId());
            sessionService.updateUserContext(humanPlayerId, userContext);
        } else {
            PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
            if (playerSession != null && playerSession.getUnregisteredUser() != null) {
                UnregisteredUser unregisteredUser = playerSession.getUnregisteredUser();
                int crystals = levelPersistence.readLevelUnlockEntityCrystals(playerSession.getUserContext().getLevelId(), levelUnlockEntityId);
                if (crystals > unregisteredUser.getCrystals()) {
                    throw new IllegalArgumentException("Unregistered user does not have enough crystals to unlock LevelUnlockEntity. LevelUnlockEntity id: " + levelUnlockEntityId);
                }
                unregisteredUser.addLevelUnlockEntityId(levelUnlockEntityId);
                unregisteredUser.removeCrystals(crystals);
                userContext = playerSession.getUserContext();
                userContext.setUnlockedItemLimit(levelPersistence.setupUnlockedItemLimit(unregisteredUser.getLevelUnlockEntityIds()));
            } else {
                return;
            }
        }
        baseItemService.updateUnlockedItemLimit(humanPlayerId, userContext.getUnlockedItemLimit());
        systemConnectionService.onUnlockedItemLimit(humanPlayerId, userContext.getUnlockedItemLimit());
        historyPersistence.onLevelUnlockEntityUsedViaCrystals(humanPlayerId, levelUnlockEntityId);
    }

    public static Map<Integer, Integer> convertUnlockedItemLimit(Collection<LevelUnlockEntity> levelUnlockEntities) {
        Map<Integer, Integer> unlockedItemLimit = new HashMap<>();
        if (levelUnlockEntities == null) {
            return unlockedItemLimit;
        }
        for (LevelUnlockEntity levelUnlockEntity : levelUnlockEntities) {
            if (levelUnlockEntity.getBaseItemType() != null) {
                int count = unlockedItemLimit.getOrDefault(levelUnlockEntity.getBaseItemType().getId(), 0);
                count += levelUnlockEntity.getBaseItemTypeCount();
                unlockedItemLimit.put(levelUnlockEntity.getBaseItemType().getId(), count);
            }
        }
        return unlockedItemLimit;
    }

    public List<LevelUnlockConfig> gatherAvailableUnlocks(HumanPlayerId humanPlayerId, int levelId) {
        Collection<Integer> unlockedEntityIds;
        if (humanPlayerId.getUserId() != null) {
            unlockedEntityIds = userService.unlockedEntityIds(humanPlayerId.getUserId());
        } else {
            PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
            if (playerSession != null && playerSession.getUnregisteredUser() != null) {
                unlockedEntityIds = playerSession.getUnregisteredUser().getLevelUnlockEntityIds();
            } else {
                return Collections.emptyList();
            }
        }
        return levelPersistence.readUnlocks(levelId, unlockedEntityIds);
    }

    @SecurityCheck
    public void removeUnlocked(HumanPlayerId humanPlayerId, int levelUnlockEntityId) {
        UserContext userContext;
        if (humanPlayerId.getUserId() != null) {
            userService.persistRemoveUnlocked(humanPlayerId.getUserId(), levelUnlockEntityId);
            userContext = userService.readUserContext(humanPlayerId.getUserId());
            sessionService.updateUserContext(humanPlayerId, userContext);
        } else {
            PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
            if (playerSession != null && playerSession.getUnregisteredUser() != null) {
                UnregisteredUser unregisteredUser = playerSession.getUnregisteredUser();
                unregisteredUser.getLevelUnlockEntityIds().remove(levelUnlockEntityId);
                userContext = playerSession.getUserContext();
                userContext.setUnlockedItemLimit(levelPersistence.setupUnlockedItemLimit(unregisteredUser.getLevelUnlockEntityIds()));
            } else {
                return;
            }
        }
        baseItemService.updateUnlockedItemLimit(humanPlayerId, userContext.getUnlockedItemLimit());
        systemConnectionService.onUnlockedItemLimit(humanPlayerId, userContext.getUnlockedItemLimit());
    }
}

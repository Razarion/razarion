package com.btxtech.server.gameengine;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
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
    private LevelCrudPersistence levelCrudPersistence;
    @Inject
    private HistoryPersistence historyPersistence;

    public void unlockViaCrystals(int userId, int levelUnlockEntityId) {
        userService.persistUnlockViaCrystals(userId, levelUnlockEntityId);
        UserContext userContext = userService.readUserContext(userId);
        sessionService.updateUserContext(userId, userContext);
        baseItemService.updateUnlockedItemLimit(userId, userContext.getUnlockedItemLimit());
        systemConnectionService.onUnlockedItemLimit(userId, userContext.getUnlockedItemLimit());
        historyPersistence.onLevelUnlockEntityUsedViaCrystals(userId, levelUnlockEntityId);
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

    public List<LevelUnlockConfig> gatherAvailableUnlocks(UserContext userContext, int levelId) {
        return levelCrudPersistence.readUnlocks(levelId, userService.unlockedEntityIds(userContext.getUserId()));
    }

    @SecurityCheck
    public void removeUnlocked(int userId, int levelUnlockEntityId) {
        userService.persistRemoveUnlocked(userId, levelUnlockEntityId);
        UserContext userContext = userService.readUserContext(userId);
        sessionService.updateUserContext(userId, userContext);
        baseItemService.updateUnlockedItemLimit(userId, userContext.getUnlockedItemLimit());
        systemConnectionService.onUnlockedItemLimit(userId, userContext.getUnlockedItemLimit());
    }
}

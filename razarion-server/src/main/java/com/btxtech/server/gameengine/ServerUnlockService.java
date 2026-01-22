package com.btxtech.server.gameengine;

import com.btxtech.server.model.engine.LevelUnlockEntity;
import com.btxtech.server.service.engine.LevelCrudService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServerUnlockService {
    @Inject
    private UserService userService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ClientSystemConnectionService systemConnectionService;
    @Inject
    private LevelCrudService levelCrudService;
    @Inject
    private QuestService questService;

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

    public void unlockViaCrystals(String userId, int levelUnlockEntityId) {
        userService.persistUnlockViaCrystals(userId, levelUnlockEntityId);
        UserContext userContext = userService.getUserContext(userId);
        baseItemService.updateUnlockedItemLimit(userId, userContext.getUnlockedItemLimit());
        systemConnectionService.onUnlockedItemLimit(userId, userContext.getUnlockedItemLimit(), hasAvailableUnlocks(userContext));
        questService.onUnlock(userId);
        // TODO historyPersistence.onLevelUnlockEntityUsedViaCrystals(userId, levelUnlockEntityId);
    }

    public boolean hasAvailableUnlocks(UserContext userContext) {
        return levelCrudService.hasAvailableUnlocks(userContext.getLevelId(), userService.unlockedEntityIds(userContext.getUserId()));
    }

    public List<LevelUnlockConfig> getAvailableLevelUnlockConfigs(String userId, int levelId) {
        return levelCrudService.readAvailableLevelUnlockConfigs(levelId, userService.unlockedEntityIds(userId));
    }

    public void updateUnlocked(String userId, List<Integer> unlockedIds) {
        throw new UnsupportedOperationException("... TODO ...");
//   TODO     userService.setUnlocked(userId, unlockedIds);
//        UserContext userContext = userService.readUserContext(userId);
//        sessionService.updateUserContext(userId, userContext);
//        baseItemService.updateUnlockedItemLimit(userId, userContext.getUnlockedItemLimit());
//        systemConnectionService.onUnlockedItemLimit(userId, userContext.getUnlockedItemLimit(), hasAvailableUnlocks(userContext));
    }
}

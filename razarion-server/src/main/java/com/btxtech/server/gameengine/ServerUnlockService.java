package com.btxtech.server.gameengine;

import com.btxtech.server.model.engine.LevelUnlockEntity;
import com.btxtech.server.service.engine.LevelCrudPersistence;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServerUnlockService {
    @Inject
    private UserService userService;
    @Inject
    private BaseItemService baseItemService;
    // @Inject
    // TODO private ClientSystemConnectionService systemConnectionService;
    @Inject
    private SessionService sessionService;
    @Inject
    private LevelCrudPersistence levelCrudPersistence;
    @Inject
    private QuestService questService;

//  TODO  public void unlockViaCrystals(int userId, int levelUnlockEntityId) {
//        userService.persistUnlockViaCrystals(userId, levelUnlockEntityId);
//        UserContext userContext = userService.readUserContext(userId);
//        sessionService.updateUserContext(userId, userContext);
//        baseItemService.updateUnlockedItemLimit(userId, userContext.getUnlockedItemLimit());
//        systemConnectionService.onUnlockedItemLimit(userId, userContext.getUnlockedItemLimit(), hasAvailableUnlocks(userContext));
//        questService.onUnlock(userId);
//        historyPersistence.onLevelUnlockEntityUsedViaCrystals(userId, levelUnlockEntityId);
//    }

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

   public boolean hasAvailableUnlocks(UserContext userContext) {
        return levelCrudPersistence.hasAvailableUnlocks(userContext.getLevelId(), userService.unlockedEntityIds(userContext.getUserId()));
    }

// TODO   public List<LevelUnlockConfig> getAvailableLevelUnlockConfigs(UserContext userContext, int levelId) {
//        return levelCrudPersistence.readAvailableLevelUnlockConfigs(levelId, userService.unlockedEntityIds(userContext.getUserId()));
//    }

//  TODO  public void updateUnlocked(int userId, List<Integer> unlockedIds) {
//        userService.setUnlocked(userId, unlockedIds);
//        UserContext userContext = userService.readUserContext(userId);
//        sessionService.updateUserContext(userId, userContext);
//        baseItemService.updateUnlockedItemLimit(userId, userContext.getUnlockedItemLimit());
//        systemConnectionService.onUnlockedItemLimit(userId, userContext.getUnlockedItemLimit(), hasAvailableUnlocks(userContext));
//    }
}

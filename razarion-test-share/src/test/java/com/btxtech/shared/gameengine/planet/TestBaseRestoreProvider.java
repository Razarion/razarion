package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Beat
 * on 07.03.2018.
 */
public class TestBaseRestoreProvider implements BaseRestoreProvider {
    private Collection<UserContext> userContexts = new ArrayList<>();

    public void addUserContext(UserContext userContext) {
        userContexts.add(userContext);
    }

    @Override
    public Integer getLevel(PlayerBaseInfo playerBaseInfo) {
        return getUserContext(playerBaseInfo).getLevelId();
    }

    @Override
    public Map<Integer, Integer> getUnlockedItemLimit(PlayerBaseInfo playerBaseInfo) {
        return getUserContext(playerBaseInfo).getUnlockedItemLimit();
    }

    @Override
    public String getName(PlayerBaseInfo playerBaseInfo) {
        return getUserContext(playerBaseInfo).getName();
    }

    private UserContext getUserContext(PlayerBaseInfo playerBaseInfo) {
        if (playerBaseInfo.getUserId() == null) {
            throw new IllegalStateException("Can not restore base with id: " + playerBaseInfo.getBaseId() + " name: " + playerBaseInfo.getName() + " may be this is a bot");
        }
        return userContexts.stream().filter(userContext -> userContext.getUserId() == playerBaseInfo.getUserId()).findFirst().orElseThrow(() -> new IllegalArgumentException("No Context for: " + playerBaseInfo.getUserId()));
    }
}

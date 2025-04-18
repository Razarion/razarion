package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;

import java.util.Map;

/**
 * Created by Beat
 * on 09.01.2018.
 */
public interface BaseRestoreProvider {
    Integer getLevel(PlayerBaseInfo playerBaseInfo);

    Map<Integer, Integer> getUnlockedItemLimit(PlayerBaseInfo playerBaseInfo);

    String getName(PlayerBaseInfo playerBaseInfo);
}

/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;

import java.io.Serializable;
import java.util.Map;

/**
 * User: beat
 * Date: 17.05.2010
 * Time: 18:48:18
 */
public class LevelConfig implements Serializable, ObjectNameIdProvider {
    private int levelId;
    private int number;
    private Map<Integer, Integer> itemTypeLimitation;
    private int xp2LevelUp;

    public int getLevelId() {
        return levelId;
    }

    public LevelConfig setLevelId(int levelId) {
        this.levelId = levelId;
        return this;
    }

    public int getNumber() {
        return number;
    }

    public LevelConfig setNumber(int number) {
        this.number = number;
        return this;
    }

    public Map<Integer, Integer> getItemTypeLimitation() {
        return itemTypeLimitation;
    }

    public LevelConfig setItemTypeLimitation(Map<Integer, Integer> itemTypeLimitation) {
        this.itemTypeLimitation = itemTypeLimitation;
        return this;
    }

    public int getXp2LevelUp() {
        return xp2LevelUp;
    }

    public LevelConfig setXp2LevelUp(int xp2LevelUp) {
        this.xp2LevelUp = xp2LevelUp;
        return this;
    }

    public int limitation4ItemType(int itemTypeId) {
        Integer limitation = itemTypeLimitation.get(itemTypeId);
        if (limitation != null) {
            return limitation;
        } else {
            return 0;
        }
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(levelId, Integer.toString(number));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LevelConfig that = (LevelConfig) o;
        return levelId == that.levelId;
    }

    @Override
    public int hashCode() {
        return levelId;
    }
}

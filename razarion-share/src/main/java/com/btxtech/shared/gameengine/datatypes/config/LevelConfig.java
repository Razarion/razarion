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

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;

import java.util.Map;

/**
 * User: beat
 * Date: 17.05.2010
 * Time: 18:48:18
 */
public class LevelConfig implements Config {
    private int id;
    private String internalName;
    private int number;
    private Map<Integer, Integer> itemTypeLimitation;
    private int xp2LevelUp;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Map<Integer, Integer> getItemTypeLimitation() {
        return itemTypeLimitation;
    }

    public void setItemTypeLimitation(Map<Integer, Integer> itemTypeLimitation) {
        this.itemTypeLimitation = itemTypeLimitation;
    }

    public int getXp2LevelUp() {
        return xp2LevelUp;
    }

    public void setXp2LevelUp(int xp2LevelUp) {
        this.xp2LevelUp = xp2LevelUp;
    }

    public LevelConfig id(int id) {
        this.id = id;
        return this;
    }

    public LevelConfig internalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public LevelConfig number(int number) {
        setNumber(number);
        return this;
    }

    public LevelConfig itemTypeLimitation(Map<Integer, Integer> itemTypeLimitation) {
        setItemTypeLimitation(itemTypeLimitation);
        return this;
    }

    public LevelConfig xp2LevelUp(int xp2LevelUp) {
        setXp2LevelUp(xp2LevelUp);
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
        return new ObjectNameId(id, Integer.toString(number));
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
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}

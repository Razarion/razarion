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

package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * User: beat
 * Date: Aug 5, 2009
 * Time: 2:03:06 PM
 */
public class PlayerBase {
    private int baseId;
    private double resources;
    private String name;
    private Character character;
    private Integer userId;
    private boolean abandoned;

    public PlayerBase(int baseId, String name, Character character, double resources, Integer userId) {
        this.baseId = baseId;
        this.name = name;
        this.character = character;
        this.resources = resources;
        this.userId = userId;
    }

    public int getBaseId() {
        return baseId;
    }

    public String getName() {
        return name;
    }

    public Character getCharacter() {
        return character;
    }

    public boolean isAbandoned() {
        return abandoned;
    }

    public boolean isEnemy(PlayerBase playerBase) {
        return !equals(playerBase) && character.isEnemy(playerBase.character);
    }

    public Integer getUserId() {
        return userId;
    }

    public double getResources() {
        return resources;
    }

    public void addResource(double resources) {
        this.resources += resources;
    }

    public boolean withdrawalResource(double amount) {
        if (amount > resources) {
            return false;
        }
        resources -= amount;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlayerBase that = (PlayerBase) o;

        return baseId == that.baseId;
    }

    @Override
    public int hashCode() {
        return baseId;
    }

    @Override
    public String toString() {
        return "PlayerBase{" +
                "baseId=" + baseId +
                ", name='" + name + '\'' +
                ", character=" + character +
                ", abandoned=" + abandoned +
                '}';
    }
}

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

import com.btxtech.shared.datatypes.UserContext;
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
    private UserContext userContext;
    private boolean abandoned;
    private final Collection<SyncBaseItem> items = new ArrayList<>();
    private int usedHouseSpace = 0;

    public PlayerBase(int baseId, String name, Character character, UserContext userContext) {
        this.baseId = baseId;
        this.name = name;
        this.character = character;
        this.userContext = userContext;
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

    public void addItem(SyncBaseItem syncBaseItem) {
        items.add(syncBaseItem);
        usedHouseSpace += syncBaseItem.getBaseItemType().getConsumingHouseSpace();
    }

    public void removeItem(SyncBaseItem syncBaseItem) {
        items.remove(syncBaseItem);
        usedHouseSpace -= syncBaseItem.getBaseItemType().getConsumingHouseSpace();
    }

    public int getItemCount() {
        return items.size();
    }

    public Collection<SyncBaseItem> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    public Collection<SyncBaseItem> getItemsInPlace(PlaceConfig placeConfig) {
        return items.stream().filter(placeConfig::checkInside).collect(Collectors.toCollection(ArrayList::new));
    }

    public int getUsedHouseSpace() {
        return usedHouseSpace;
    }

    public int getHouseSpace() {
        // TODO
        return 0;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public double getResources() {
        return resources;
    }

    public void setResources(double resources) {
        this.resources = resources;
    }

    public void addResource(double resources) {
        this.resources += resources;
    }

    public boolean withdrawalResource(double amount) {
        if(amount > resources) {
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

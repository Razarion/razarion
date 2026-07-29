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

import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;

/**
 * User: beat
 * Date: Aug 5, 2009
 * Time: 2:03:06 PM
 */
public class PlayerBase {
    private final int baseId;
    private final Character character;
    private final Integer botId;
    /**
     * When this base was first built, as epoch millis. Set once on the master and then carried
     * around unchanged - through the planet backup, through the restore and out to every slave -
     * so that the age of a base survives a server restart. Taking a fresh timestamp anywhere but
     * at creation would make every base look new again after the next restore.
     */
    private final long createdMillis;
    private double resources;
    private double maxRazarion;
    private String name;
    private String userId;
    private boolean abandoned;

    public PlayerBase(int baseId, String name, Character character, double resources, double maxRazarion, String userId, Integer botId, long createdMillis) {
        this.baseId = baseId;
        this.name = name;
        this.character = character;
        this.resources = resources;
        this.maxRazarion = maxRazarion;
        this.userId = userId;
        this.botId = botId;
        this.createdMillis = createdMillis;
    }

    public int getBaseId() {
        return baseId;
    }

    public long getCreatedMillis() {
        return createdMillis;
    }

    public String getName() {
        return name;
    }

    public void updateUserId(String userId) {
        this.userId = userId;
    }

    public void updateName(String name) {
        this.name = name;
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

    public String getUserId() {
        return userId;
    }

    public Integer getBotId() {
        return botId;
    }

    public double getResources() {
        return resources;
    }

    public void setResources(double resources) {
        this.resources = resources;
    }

    public double getMaxRazarion() {
        return maxRazarion;
    }

    public void setMaxRazarion(double maxRazarion) {
        this.maxRazarion = maxRazarion;
    }

    public void addResource(double resources) {
        this.resources += resources;
        // maxRazarion <= 0 means "unlimited"
        if (maxRazarion > 0 && this.resources > maxRazarion) {
            this.resources = maxRazarion;
        }
    }

    public boolean withdrawalResource(double amount) {
        if (character.isBot()) {
            return true;
        }
        if (amount > resources) {
            return false;
        }
        resources -= amount;
        return true;
    }

    public PlayerBaseInfo getPlayerBaseInfo() {
        return new PlayerBaseInfo()
                .baseId(baseId)
                .character(character)
                .name(name)
                .userId(userId)
                .botId(botId)
                .resources(resources)
                .createdMillis(createdMillis);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof PlayerBase)) {
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
                ", userId=" + userId +
                ", botId=" + botId +
                ", createdMillis=" + createdMillis +
                '}';
    }
}

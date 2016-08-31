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

/**
 * User: beat
 * Date: Aug 5, 2009
 * Time: 2:03:06 PM
 */
public class PlayerBase {
    private int baseId;
    private String name;
    private Character character;
    private boolean abandoned;

    public PlayerBase(int baseId, String name, Character character) {
        this.baseId = baseId;
        this.name = name;
        this.character = character;
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
        return !equals(playerBase)
                && !(character == Character.BOT && playerBase.character == Character.BOT)
                && !(character == Character.BOT_NCP || playerBase.character == Character.BOT_NCP)
                && !(character == Character.HUMAN && playerBase.character == Character.HUMAN);

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

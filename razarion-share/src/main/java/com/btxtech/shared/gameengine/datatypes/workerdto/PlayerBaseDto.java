package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.Character;

/**
 * Created by Beat
 * 07.01.2017.
 */
public class PlayerBaseDto {
    private int baseId;
    private String name;
    private Character character;
    private HumanPlayerId humanPlayerId;

    public int getBaseId() {
        return baseId;
    }

    public void setBaseId(int baseId) {
        this.baseId = baseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public void setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
    }
}

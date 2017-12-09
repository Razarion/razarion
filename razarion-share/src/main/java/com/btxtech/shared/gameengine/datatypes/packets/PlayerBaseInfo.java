package com.btxtech.shared.gameengine.datatypes.packets;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.Character;

/**
 * Created by Beat
 * 16.04.2017.
 */
public class PlayerBaseInfo {
    private int baseId;
    private String name;
    private Character character;
    private double resources;
    private HumanPlayerId humanPlayerId;
    private Integer botId;

    public int getBaseId() {
        return baseId;
    }

    public PlayerBaseInfo setBaseId(int baseId) {
        this.baseId = baseId;
        return this;
    }

    public String getName() {
        return name;
    }

    public PlayerBaseInfo setName(String name) {
        this.name = name;
        return this;
    }

    public Character getCharacter() {
        return character;
    }

    public PlayerBaseInfo setCharacter(Character character) {
        this.character = character;
        return this;
    }

    public double getResources() {
        return resources;
    }

    public PlayerBaseInfo setResources(double resources) {
        this.resources = resources;
        return this;
    }

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public PlayerBaseInfo setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
        return this;
    }

    public Integer getBotId() {
        return botId;
    }

    public PlayerBaseInfo setBotId(Integer botId) {
        this.botId = botId;
        return this;
    }
}

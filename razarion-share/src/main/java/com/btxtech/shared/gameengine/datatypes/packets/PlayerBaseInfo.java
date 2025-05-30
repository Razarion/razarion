package com.btxtech.shared.gameengine.datatypes.packets;

import com.btxtech.shared.gameengine.datatypes.Character;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * Created by Beat
 * 16.04.2017.
 */
@JSONMapper
public class PlayerBaseInfo {
    private int baseId;
    private String name;
    private Character character;
    private double resources;
    private String userId;
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

    public String getUserId() {
        return userId;
    }

    public PlayerBaseInfo setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Integer getBotId() {
        return botId;
    }

    public PlayerBaseInfo setBotId(Integer botId) {
        this.botId = botId;
        return this;
    }

    @Override
    public String toString() {
        return "PlayerBaseInfo{" +
                "baseId=" + baseId +
                ", name='" + name + '\'' +
                ", character=" + character +
                ", resources=" + resources +
                ", userId=" + userId +
                ", botId=" + botId +
                '}';
    }
}

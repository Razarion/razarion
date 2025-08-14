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

    public double getResources() {
        return resources;
    }

    public void setResources(double resources) {
        this.resources = resources;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getBotId() {
        return botId;
    }

    public void setBotId(Integer botId) {
        this.botId = botId;
    }

    public PlayerBaseInfo baseId(int baseId) {
        setBaseId(baseId);
        return this;
    }

    public PlayerBaseInfo name(String name) {
        setName(name);
        return this;
    }

    public PlayerBaseInfo character(Character character) {
        setCharacter(character);
        return this;
    }

    public PlayerBaseInfo resources(double resources) {
        setResources(resources);
        return this;
    }

    public PlayerBaseInfo userId(String userId) {
        setUserId(userId);
        return this;
    }

    public PlayerBaseInfo botId(Integer botId) {
        setBotId(botId);
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

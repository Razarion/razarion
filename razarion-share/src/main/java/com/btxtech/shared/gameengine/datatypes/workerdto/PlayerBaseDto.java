package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.gameengine.datatypes.Character;

/**
 * Created by Beat
 * 07.01.2017.
 */
public class PlayerBaseDto {
    private int baseId;
    private String name;
    private Character character;
    private Integer userId;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBotId() {
        return botId;
    }

    public void setBotId(Integer botId) {
        this.botId = botId;
    }


    public PlayerBaseDto baseId(int baseId) {
        setBaseId(baseId);
        return this;
    }

    public PlayerBaseDto name(String name) {
        setName(name);
        return this;
    }

    public PlayerBaseDto character(Character character) {
        setCharacter(character);
        return this;
    }

    public PlayerBaseDto userId(Integer userId) {
        setUserId(userId);
        return this;
    }

    public PlayerBaseDto botId(Integer botId) {
        setBotId(botId);
        return this;
    }
}

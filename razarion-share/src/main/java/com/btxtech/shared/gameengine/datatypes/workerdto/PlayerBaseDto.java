package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.gameengine.datatypes.Character;
import jsinterop.annotations.JsType;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * Created by Beat
 * 07.01.2017.
 */
@JsType
@JSONMapper
public class PlayerBaseDto {
    private int baseId;
    private String name;
    private Character character;
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

    public PlayerBaseDto userId(String userId) {
        setUserId(userId);
        return this;
    }

    public PlayerBaseDto botId(Integer botId) {
        setBotId(botId);
        return this;
    }
}

package com.btxtech.server.mgmt;

import com.btxtech.shared.datatypes.HumanPlayerId;

import java.util.Date;

/**
 * Created by Beat
 * on 05.09.2017.
 */
public class OnlineInfo {
    public enum Type {
        NORMAL,
        ORPHAN,
        EXCEPTION,
        UNKNOWN
    }
    private Date time;
    private Integer duration;
    private HumanPlayerId humanPlayerId;
    private String name;
    private String sessionId;
    private String multiplayerPlanet;
    private Date multiplayerDate;
    private Integer multiplayerDuration;
    private Type type;

    public Date getTime() {
        return time;
    }

    public OnlineInfo setTime(Date time) {
        this.time = time;
        return this;
    }

    public Integer getDuration() {
        return duration;
    }

    public OnlineInfo setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public OnlineInfo setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
        return this;
    }

    public String getName() {
        return name;
    }

    public OnlineInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public OnlineInfo setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getMultiplayerPlanet() {
        return multiplayerPlanet;
    }

    public OnlineInfo setMultiplayerPlanet(String multiplayerPlanet) {
        this.multiplayerPlanet = multiplayerPlanet;
        return this;
    }

    public Date getMultiplayerDate() {
        return multiplayerDate;
    }

    public OnlineInfo setMultiplayerDate(Date multiplayerDate) {
        this.multiplayerDate = multiplayerDate;
        return this;
    }

    public Integer getMultiplayerDuration() {
        return multiplayerDuration;
    }

    public OnlineInfo setMultiplayerDuration(Integer multiplayerDuration) {
        this.multiplayerDuration = multiplayerDuration;
        return this;
    }

    public Type getType() {
        return type;
    }

    public OnlineInfo setType(Type type) {
        this.type = type;
        return this;
    }
}

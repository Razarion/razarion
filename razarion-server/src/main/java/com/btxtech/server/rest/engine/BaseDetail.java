package com.btxtech.server.rest.engine;

/**
 * The part of a base's state that only the master knows. A client knows its own balance, its own
 * house space and its own level, and nothing at all about anybody else's - so the base management
 * has to ask the server for everything in here.
 * <p>
 * Written for the question "why is this player stuck": a base at zero Razarion cannot build, and a
 * base at maxRazarion cannot earn, and both look like "nothing happens" from the outside.
 */
public class BaseDetail {
    private Integer levelNumber;
    private int resources;
    /** 0 means unlimited, which is how PlayerBase.addResource reads it. */
    private int maxRazarion;
    private int usedHouseSpace;
    private int houseSpace;
    private int itemCount;
    /** Human bases only: the owner has an open system connection right now. */
    private Boolean online;
    /**
     * Human bases only, epoch millis: when the owner's last connection ended, or when it opened if
     * no end was ever recorded. Null for bots and for owners the database does not know.
     */
    private Long lastOnlineMillis;
    /** Human bases only: the owner has an e-mail account. Unregistered bases are cleaned up automatically. */
    private Boolean registered;

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public BaseDetail levelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
        return this;
    }

    public int getResources() {
        return resources;
    }

    public BaseDetail resources(int resources) {
        this.resources = resources;
        return this;
    }

    public int getMaxRazarion() {
        return maxRazarion;
    }

    public BaseDetail maxRazarion(int maxRazarion) {
        this.maxRazarion = maxRazarion;
        return this;
    }

    public int getUsedHouseSpace() {
        return usedHouseSpace;
    }

    public BaseDetail usedHouseSpace(int usedHouseSpace) {
        this.usedHouseSpace = usedHouseSpace;
        return this;
    }

    public int getHouseSpace() {
        return houseSpace;
    }

    public BaseDetail houseSpace(int houseSpace) {
        this.houseSpace = houseSpace;
        return this;
    }

    public int getItemCount() {
        return itemCount;
    }

    public BaseDetail itemCount(int itemCount) {
        this.itemCount = itemCount;
        return this;
    }

    public Boolean getOnline() {
        return online;
    }

    public BaseDetail online(Boolean online) {
        this.online = online;
        return this;
    }

    public Long getLastOnlineMillis() {
        return lastOnlineMillis;
    }

    public BaseDetail lastOnlineMillis(Long lastOnlineMillis) {
        this.lastOnlineMillis = lastOnlineMillis;
        return this;
    }

    public Boolean getRegistered() {
        return registered;
    }

    public BaseDetail registered(Boolean registered) {
        this.registered = registered;
        return this;
    }
}

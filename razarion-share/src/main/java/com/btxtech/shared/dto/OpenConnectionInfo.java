package com.btxtech.shared.dto;

import java.util.Date;

/**
 * One connected client, as the server currently sees it.
 * <p>
 * A player holds two web sockets: the system connection (quests, levels, chat, lifecycle) and the
 * game connection (the simulation). They are opened at different moments and can outlive each
 * other, so a row exists as soon as either one is open and says which. A row with a system
 * connection but no game connection is a client that is still starting up - or one that never got
 * that far.
 */
public class OpenConnectionInfo {
    private String userId;
    /** Null for players who never picked one, which is most of them. */
    private String name;
    private Date systemConnectionOpened;
    private Date systemLastMessageSent;
    private Date systemLastMessageReceived;
    private Date gameConnectionOpened;
    private Date gameLastMessageSent;
    private Date gameLastMessageReceived;
    /**
     * The base this player owns, or null if there is none - which is normal for a client that is
     * still starting up, and telling for one that has been connected for a while.
     */
    private Integer baseId;
    private String baseName;
    /** Items the base owns, buildings included. */
    private int itemCount;
    /** Level number, not its id - that is what the game calls it everywhere else. */
    private Integer levelNumber;
    /** Where to send the camera. Null when the base has no items left to look at. */
    private Double baseX;
    private Double baseY;

    public String getUserId() {
        return userId;
    }

    public OpenConnectionInfo userId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public OpenConnectionInfo name(String name) {
        this.name = name;
        return this;
    }

    public Date getSystemConnectionOpened() {
        return systemConnectionOpened;
    }

    public OpenConnectionInfo systemConnectionOpened(Date systemConnectionOpened) {
        this.systemConnectionOpened = systemConnectionOpened;
        return this;
    }

    public Date getSystemLastMessageSent() {
        return systemLastMessageSent;
    }

    public OpenConnectionInfo systemLastMessageSent(Date systemLastMessageSent) {
        this.systemLastMessageSent = systemLastMessageSent;
        return this;
    }

    public Date getSystemLastMessageReceived() {
        return systemLastMessageReceived;
    }

    public OpenConnectionInfo systemLastMessageReceived(Date systemLastMessageReceived) {
        this.systemLastMessageReceived = systemLastMessageReceived;
        return this;
    }

    public Date getGameConnectionOpened() {
        return gameConnectionOpened;
    }

    public OpenConnectionInfo gameConnectionOpened(Date gameConnectionOpened) {
        this.gameConnectionOpened = gameConnectionOpened;
        return this;
    }

    public Date getGameLastMessageSent() {
        return gameLastMessageSent;
    }

    public OpenConnectionInfo gameLastMessageSent(Date gameLastMessageSent) {
        this.gameLastMessageSent = gameLastMessageSent;
        return this;
    }

    public Date getGameLastMessageReceived() {
        return gameLastMessageReceived;
    }

    public OpenConnectionInfo gameLastMessageReceived(Date gameLastMessageReceived) {
        this.gameLastMessageReceived = gameLastMessageReceived;
        return this;
    }

    public Integer getBaseId() {
        return baseId;
    }

    public OpenConnectionInfo baseId(Integer baseId) {
        this.baseId = baseId;
        return this;
    }

    public String getBaseName() {
        return baseName;
    }

    public OpenConnectionInfo baseName(String baseName) {
        this.baseName = baseName;
        return this;
    }

    public int getItemCount() {
        return itemCount;
    }

    public OpenConnectionInfo itemCount(int itemCount) {
        this.itemCount = itemCount;
        return this;
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public OpenConnectionInfo levelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
        return this;
    }

    public Double getBaseX() {
        return baseX;
    }

    public OpenConnectionInfo baseX(Double baseX) {
        this.baseX = baseX;
        return this;
    }

    public Double getBaseY() {
        return baseY;
    }

    public OpenConnectionInfo baseY(Double baseY) {
        this.baseY = baseY;
        return this;
    }

    @Override
    public String toString() {
        return "OpenConnectionInfo{userId='" + userId + "', name='" + name + "'}";
    }
}

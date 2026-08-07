package com.btxtech.shared.dto;

import java.util.Date;

/**
 * The first time in a game session that the player did a particular thing: moved the camera, picked
 * a unit, gave an order.
 * <p>
 * Written to answer a question the funnel cannot: whether the controls are found at all. Mobile
 * players reach the game and leave again without passing a quest, and until this existed the only
 * evidence about why was indirect - tips complaining that their target was off screen. Absence is
 * the signal here, so it is reported once per session and kind and the denominator is the set of
 * sessions that started successfully.
 * <p>
 * Carries {@link #gameSessionUuid} rather than only a user: the device is known solely from the
 * userAgent on the PAGE_LOADED startup task, and that is keyed by the game session. A record with
 * just a user id cannot be told apart by phone and desktop without bridging through two other
 * collections.
 */
public class FirstInteractionJson {
    /**
     * The startup session, and the join to the device - see the class comment.
     */
    private String gameSessionUuid;
    /**
     * What the player did, e.g. CAMERA_PAN_TOUCH or SELECT. A string, not an enum: the interactions
     * live in the Angular client and the set is expected to grow, the same reason
     * {@link TipStallJson#getTipTaskName()} is one.
     * <p>
     * The camera kinds name the input, not the effect. "The camera moved" cannot answer whether the
     * touch gesture was discovered, because on a desktop it is also true and always has been.
     */
    private String kind;
    /**
     * Milliseconds from the page load until this first happened. Deliberately measured against the
     * same origin as {@link TabHiddenJson#getMillisSincePageLoad()}, so the moment a player starts
     * playing and the moment they leave can be put on one axis.
     */
    private Integer millisSincePageLoad;
    private String userId;
    private Date serverTime;

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public void setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Integer getMillisSincePageLoad() {
        return millisSincePageLoad;
    }

    public void setMillisSincePageLoad(Integer millisSincePageLoad) {
        this.millisSincePageLoad = millisSincePageLoad;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public FirstInteractionJson gameSessionUuid(String gameSessionUuid) {
        setGameSessionUuid(gameSessionUuid);
        return this;
    }

    public FirstInteractionJson kind(String kind) {
        setKind(kind);
        return this;
    }

    public FirstInteractionJson millisSincePageLoad(Integer millisSincePageLoad) {
        setMillisSincePageLoad(millisSincePageLoad);
        return this;
    }

    public FirstInteractionJson userId(String userId) {
        setUserId(userId);
        return this;
    }

    public FirstInteractionJson serverTime(Date serverTime) {
        setServerTime(serverTime);
        return this;
    }

    @Override
    public String toString() {
        return "FirstInteractionJson{" +
                "gameSessionUuid='" + gameSessionUuid + '\'' +
                ", kind='" + kind + '\'' +
                ", millisSincePageLoad=" + millisSincePageLoad +
                ", userId='" + userId + '\'' +
                '}';
    }
}

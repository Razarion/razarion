package com.btxtech.shared.dto;

import java.util.Date;

/**
 * A quest tip that stopped making progress: the player has been looking at the same prompt for
 * longer than the tip system's patience, or a tip task is stuck in one of its retry loops.
 * <p>
 * Reported twice for the same {@link #stallUuid}: once when the stall is detected, and again with
 * {@link #resolved} once the player got past it. A stall without a resolution is a dead end -
 * quests with a tip switch the regular quest visualisation off, so a stuck tip leaves the player
 * without any guidance at all.
 */
public class TipStallJson {
    /**
     * Ties the resolution to its stall. Generated on the client - the stall report and the
     * resolution are two separate requests.
     */
    private String stallUuid;
    /**
     * The startup session this game belongs to, so a stall can be traced back to the page load.
     */
    private String gameSessionUuid;
    private Integer questId;
    /**
     * Tip task the player is stuck in, e.g. SELECT or START_BUILD_PLACER. A string, not an enum:
     * the tasks live in the Angular client and the set is expected to grow.
     */
    private String tipTaskName;
    /**
     * What the task was waiting for when the watchdog fired, e.g. ACTOR_NOT_FOUND or
     * BUTTON_DISABLED. This is the part that says whether the tip is broken or the player is.
     */
    private String reason;
    /**
     * Milliseconds the task had been running when the stall was reported.
     */
    private Integer waitMillis;
    /**
     * True on the second report of a stall: the player did get past it after all.
     */
    private boolean resolved;
    /**
     * Milliseconds from the start of the task until it succeeded. Only set on the resolution.
     */
    private Integer resolvedMillis;
    private String userId;
    private Date serverTime;

    public String getStallUuid() {
        return stallUuid;
    }

    public void setStallUuid(String stallUuid) {
        this.stallUuid = stallUuid;
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public void setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
    }

    public Integer getQuestId() {
        return questId;
    }

    public void setQuestId(Integer questId) {
        this.questId = questId;
    }

    public String getTipTaskName() {
        return tipTaskName;
    }

    public void setTipTaskName(String tipTaskName) {
        this.tipTaskName = tipTaskName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getWaitMillis() {
        return waitMillis;
    }

    public void setWaitMillis(Integer waitMillis) {
        this.waitMillis = waitMillis;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Integer getResolvedMillis() {
        return resolvedMillis;
    }

    public void setResolvedMillis(Integer resolvedMillis) {
        this.resolvedMillis = resolvedMillis;
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

    public TipStallJson stallUuid(String stallUuid) {
        setStallUuid(stallUuid);
        return this;
    }

    public TipStallJson gameSessionUuid(String gameSessionUuid) {
        setGameSessionUuid(gameSessionUuid);
        return this;
    }

    public TipStallJson questId(Integer questId) {
        setQuestId(questId);
        return this;
    }

    public TipStallJson tipTaskName(String tipTaskName) {
        setTipTaskName(tipTaskName);
        return this;
    }

    public TipStallJson reason(String reason) {
        setReason(reason);
        return this;
    }

    public TipStallJson waitMillis(Integer waitMillis) {
        setWaitMillis(waitMillis);
        return this;
    }

    public TipStallJson resolved(boolean resolved) {
        setResolved(resolved);
        return this;
    }

    public TipStallJson resolvedMillis(Integer resolvedMillis) {
        setResolvedMillis(resolvedMillis);
        return this;
    }

    public TipStallJson userId(String userId) {
        setUserId(userId);
        return this;
    }

    public TipStallJson serverTime(Date serverTime) {
        setServerTime(serverTime);
        return this;
    }

    @Override
    public String toString() {
        return "TipStallJson{" +
                "stallUuid='" + stallUuid + '\'' +
                ", gameSessionUuid='" + gameSessionUuid + '\'' +
                ", questId=" + questId +
                ", tipTaskName='" + tipTaskName + '\'' +
                ", reason='" + reason + '\'' +
                ", waitMillis=" + waitMillis +
                ", resolved=" + resolved +
                ", resolvedMillis=" + resolvedMillis +
                ", userId='" + userId + '\'' +
                '}';
    }
}

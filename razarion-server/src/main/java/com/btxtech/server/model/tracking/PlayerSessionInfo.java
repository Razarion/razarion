package com.btxtech.server.model.tracking;

import java.util.Date;
import java.util.List;

/**
 * One row of the history view: a single attempt to start the game, with what became of it and who
 * was behind it.
 * <p>
 * The attempt is the unit rather than the player, because an attempt has exactly one point in time
 * - and a 24 hour overview is a timeline. A player who started three times is three rows, which is
 * also the only way to see that the second attempt followed a crash. The player is a column;
 * {@link #attemptNumber} carries the grouping.
 * <p>
 * Landing page visits are deliberately not rows. That traffic belongs in the funnel view - here it
 * would bury the players under crawlers and ad verification. The one exception is
 * {@link PlayerSessionOutcome#NO_STARTUP}: somebody who pressed Play and never sent a single
 * startup task. Those are invisible everywhere else, because the startup tracking only begins to
 * report once the first task runs.
 */
public class PlayerSessionInfo {
    /**
     * Identifies the row for the table. The game session uuid where there is one, the browser
     * session otherwise - a NO_STARTUP row has no game session, and two attempts can share a
     * timestamp, so neither of those alone would keep the expanded rows apart.
     */
    private String rowId;
    /** Null on a NO_STARTUP row - there never was a game session. */
    private String gameSessionUuid;
    /** First startup task of the attempt, or the game page request for a NO_STARTUP row. */
    private Date startTime;
    private String userId;
    /**
     * Display name, only for users that still exist. Unregistered players are deleted an hour
     * after they disconnect, so most rows older than that have none - the whole row is built from
     * the tracking trail rather than from the user table for exactly that reason.
     */
    private String name;
    /** Null means organic: no click id anywhere on this attempt. */
    private TrackingPlatform source;
    private String userAgent;
    private PlayerSessionOutcome outcome;
    /** Where an aborted startup got stuck. Null for anything that ran to its end. */
    private String lastTaskEnum;
    /** Startup duration in milliseconds; null unless the startup reported its own end. */
    private Integer totalTime;
    /** The player left the tab while the game was still loading, rather than leaving the page. */
    private boolean tabbedAway;
    /** Highest level seen in the window; null when the player never levelled in it. */
    private Integer levelNumber;
    /** A base was created during this window. */
    private boolean baseCreated;
    /** The player owns a base right now, asked of the running planet. */
    private boolean baseAlive;
    private int questsPassed;
    /** How many attempts this player had made in the window, this one included. Starts at 1. */
    private int attemptNumber;
    /** The startup task timeline, for the expanded row. */
    private List<PlayerSessionTask> tasks;
    /** What the player did in this window: base, levels, quests. For the expanded row. */
    private List<PlayerSessionEvent> events;

    public String getRowId() {
        return rowId;
    }

    public PlayerSessionInfo rowId(String rowId) {
        this.rowId = rowId;
        return this;
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public PlayerSessionInfo gameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public PlayerSessionInfo startTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public PlayerSessionInfo userId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public PlayerSessionInfo name(String name) {
        this.name = name;
        return this;
    }

    public TrackingPlatform getSource() {
        return source;
    }

    public PlayerSessionInfo source(TrackingPlatform source) {
        this.source = source;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public PlayerSessionInfo userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public PlayerSessionOutcome getOutcome() {
        return outcome;
    }

    public PlayerSessionInfo outcome(PlayerSessionOutcome outcome) {
        this.outcome = outcome;
        return this;
    }

    public String getLastTaskEnum() {
        return lastTaskEnum;
    }

    public PlayerSessionInfo lastTaskEnum(String lastTaskEnum) {
        this.lastTaskEnum = lastTaskEnum;
        return this;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public PlayerSessionInfo totalTime(Integer totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    public boolean isTabbedAway() {
        return tabbedAway;
    }

    public PlayerSessionInfo tabbedAway(boolean tabbedAway) {
        this.tabbedAway = tabbedAway;
        return this;
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public PlayerSessionInfo levelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
        return this;
    }

    public boolean isBaseCreated() {
        return baseCreated;
    }

    public PlayerSessionInfo baseCreated(boolean baseCreated) {
        this.baseCreated = baseCreated;
        return this;
    }

    public boolean isBaseAlive() {
        return baseAlive;
    }

    public PlayerSessionInfo baseAlive(boolean baseAlive) {
        this.baseAlive = baseAlive;
        return this;
    }

    public int getQuestsPassed() {
        return questsPassed;
    }

    public PlayerSessionInfo questsPassed(int questsPassed) {
        this.questsPassed = questsPassed;
        return this;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public PlayerSessionInfo attemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
        return this;
    }

    public List<PlayerSessionTask> getTasks() {
        return tasks;
    }

    public PlayerSessionInfo tasks(List<PlayerSessionTask> tasks) {
        this.tasks = tasks;
        return this;
    }

    public List<PlayerSessionEvent> getEvents() {
        return events;
    }

    public PlayerSessionInfo events(List<PlayerSessionEvent> events) {
        this.events = events;
        return this;
    }
}

package com.btxtech.server.model.tracking;

import java.util.Map;

/**
 * One row of the daily funnel table in the backend statistics: how far the players of a single
 * day got. Read a row horizontally for that day's funnel, read a column downwards to compare
 * the days with each other.
 */
public class DailyProgress {
    /**
     * Day this row covers, as yyyy-MM-dd in the server time zone.
     */
    private String day;
    /**
     * Distinct http sessions that hit the home page that day.
     */
    private int home;
    /**
     * Distinct http sessions that hit the game page that day.
     */
    private int game;
    /**
     * Users whose very first base (over the whole history, not just the shown window) was
     * created that day.
     */
    private int initialBaseCreated;
    /**
     * Level number to the number of distinct users that reached it that day.
     */
    private Map<Integer, Integer> levelUps;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getHome() {
        return home;
    }

    public void setHome(int home) {
        this.home = home;
    }

    public int getGame() {
        return game;
    }

    public void setGame(int game) {
        this.game = game;
    }

    public int getInitialBaseCreated() {
        return initialBaseCreated;
    }

    public void setInitialBaseCreated(int initialBaseCreated) {
        this.initialBaseCreated = initialBaseCreated;
    }

    public Map<Integer, Integer> getLevelUps() {
        return levelUps;
    }

    public void setLevelUps(Map<Integer, Integer> levelUps) {
        this.levelUps = levelUps;
    }

    public DailyProgress day(String day) {
        this.day = day;
        return this;
    }

    public DailyProgress home(int home) {
        this.home = home;
        return this;
    }

    public DailyProgress game(int game) {
        this.game = game;
        return this;
    }

    public DailyProgress initialBaseCreated(int initialBaseCreated) {
        this.initialBaseCreated = initialBaseCreated;
        return this;
    }

    public DailyProgress levelUps(Map<Integer, Integer> levelUps) {
        this.levelUps = levelUps;
        return this;
    }
}

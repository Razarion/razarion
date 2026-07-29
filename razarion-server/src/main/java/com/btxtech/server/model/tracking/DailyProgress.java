package com.btxtech.server.model.tracking;

import java.util.Map;

/**
 * One row of the funnel table in the backend's <em>Daily</em> tab: how far the players of a single
 * day got. Read a row horizontally for that day's funnel, read a column downwards to compare
 * the days with each other.
 * <p>
 * Not to be confused with the tab named <em>Funnel</em>, which reports the whole selected period
 * at once rather than day by day.
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
     * Distinct http sessions that pressed "Play Now" on the home page that day. Sits between home
     * and game on purpose: the gap above it is people the page did not convince, the gap below it
     * is people who wanted to play and did not get there.
     */
    private int playClicked;
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

    public int getPlayClicked() {
        return playClicked;
    }

    public void setPlayClicked(int playClicked) {
        this.playClicked = playClicked;
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

    public DailyProgress playClicked(int playClicked) {
        setPlayClicked(playClicked);
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

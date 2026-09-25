package com.btxtech.server.model.history;

/**
 * One group of the game history over a time window: how many events of one kind happened, and how
 * many different users caused them. Counts only - no user, base or bot name ever leaves the server
 * through this, which is what lets the social pipeline turn it into a public post.
 * <p>
 * {@code itemTypeName} is set for item events only. {@code targetHuman} says whether the victim of a
 * {@code ITEM_DESTROYED} or {@code BASE_DEFEATED} belonged to a player rather than a bot.
 */
public class GameHistorySummaryRow {
    private GameHistoryType type;
    private GameHistorySource source;
    private boolean targetHuman;
    private String itemTypeName;
    private long count;
    private int users;
    private Integer maxLevel;

    public GameHistoryType getType() { return type; }
    public void setType(GameHistoryType type) { this.type = type; }
    public GameHistorySource getSource() { return source; }
    public void setSource(GameHistorySource source) { this.source = source; }
    public boolean isTargetHuman() { return targetHuman; }
    public void setTargetHuman(boolean targetHuman) { this.targetHuman = targetHuman; }
    public String getItemTypeName() { return itemTypeName; }
    public void setItemTypeName(String itemTypeName) { this.itemTypeName = itemTypeName; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public int getUsers() { return users; }
    public void setUsers(int users) { this.users = users; }
    public Integer getMaxLevel() { return maxLevel; }
    public void setMaxLevel(Integer maxLevel) { this.maxLevel = maxLevel; }
}

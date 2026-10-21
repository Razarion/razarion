package com.btxtech.shared.dto;

/**
 * One line of the community ticker: who did what. The text is put together on the client.
 */
public class CommunityTickerEntry {
    /** Increasing per server run; the client asks for everything after the last one it has. */
    private long seq;
    private long timeMillis;
    /** The player's name if registered, a fixed call sign otherwise. */
    private String name;
    private CommunityTickerKind kind;
    /** LEVEL_UP: the level reached. */
    private Integer level;
    /** DESTROYED and FIRST_UNIT: the item type, DEFEATED_BOT_BASE: the bot base. */
    private String what;

    public long getSeq() {
        return seq;
    }

    public CommunityTickerEntry setSeq(long seq) {
        this.seq = seq;
        return this;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public CommunityTickerEntry setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
        return this;
    }

    public String getName() {
        return name;
    }

    public CommunityTickerEntry setName(String name) {
        this.name = name;
        return this;
    }

    public CommunityTickerKind getKind() {
        return kind;
    }

    public CommunityTickerEntry setKind(CommunityTickerKind kind) {
        this.kind = kind;
        return this;
    }

    public Integer getLevel() {
        return level;
    }

    public CommunityTickerEntry setLevel(Integer level) {
        this.level = level;
        return this;
    }

    public String getWhat() {
        return what;
    }

    public CommunityTickerEntry setWhat(String what) {
        this.what = what;
        return this;
    }
}

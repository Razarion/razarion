package com.btxtech.shared.datatypes;

/**
 * A unit shows a sign over its head - a wave, a thumb, a laugh - to everybody who has it in view.
 * Sent by the server to every open game page (SystemConnectionPacket.EMOTE); the player picks it
 * with the emote button, or answers a wave with one tap.
 * <p>
 * Introduced 2026-11-25 so that new players notice the people around them: in the first minutes
 * of most new players somebody else is playing nearby, and nothing showed it.
 */
public class EmotePacket {
    /** WAVE, THUMBS_UP, LAUGH, GG, QUESTION - see EmoteService on the server. */
    private String emote;
    private int syncItemId;
    private int baseId;
    /** The name the others see over the unit: a player's name or call sign. */
    private String name;
    private double x;
    private double y;
    /** True when this answers somebody else's emote. */
    private boolean reply;

    public String getEmote() {
        return emote;
    }

    public EmotePacket setEmote(String emote) {
        this.emote = emote;
        return this;
    }

    public int getSyncItemId() {
        return syncItemId;
    }

    public EmotePacket setSyncItemId(int syncItemId) {
        this.syncItemId = syncItemId;
        return this;
    }

    public int getBaseId() {
        return baseId;
    }

    public EmotePacket setBaseId(int baseId) {
        this.baseId = baseId;
        return this;
    }

    public String getName() {
        return name;
    }

    public EmotePacket setName(String name) {
        this.name = name;
        return this;
    }

    public double getX() {
        return x;
    }

    public EmotePacket setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public EmotePacket setY(double y) {
        this.y = y;
        return this;
    }

    public boolean isReply() {
        return reply;
    }

    public EmotePacket setReply(boolean reply) {
        this.reply = reply;
        return this;
    }
}

package com.btxtech.shared.gameengine.datatypes.config.bot;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * User: beat
 * Date: 10.10.2011
 * Time: 13:37:24
 */
@Portable
public class BotItemConfig {
    private int baseItemTypeId;
    private int count;
    private boolean createDirectly;
    private PlaceConfig place;
    private boolean moveRealmIfIdle;
    private Integer idleTtl;
    private boolean noRebuild;
    private Long rePopTime;

    public BotItemConfig setBaseItemTypeId(int baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
        return this;
    }

    public BotItemConfig setCount(int count) {
        this.count = count;
        return this;
    }

    public BotItemConfig setCreateDirectly(boolean createDirectly) {
        this.createDirectly = createDirectly;
        return this;
    }

    public BotItemConfig setPlace(PlaceConfig place) {
        this.place = place;
        return this;
    }

    public BotItemConfig setMoveRealmIfIdle(boolean moveRealmIfIdle) {
        this.moveRealmIfIdle = moveRealmIfIdle;
        return this;
    }

    public BotItemConfig setIdleTtl(Integer idleTtl) {
        this.idleTtl = idleTtl;
        return this;
    }

    public BotItemConfig setNoRebuild(boolean noRebuild) {
        this.noRebuild = noRebuild;
        return this;
    }

    public BotItemConfig setRePopTime(Long rePopTime) {
        this.rePopTime = rePopTime;
        return this;
    }

    public int getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public int getCount() {
        return count;
    }

    public boolean isCreateDirectly() {
        return createDirectly;
    }

    public PlaceConfig getPlace() {
        return place;
    }

    public boolean isMoveRealmIfIdle() {
        return moveRealmIfIdle;
    }

    public Integer getIdleTtl() {
        return idleTtl;
    }

    public boolean isNoRebuild() {
        return noRebuild;
    }

    public boolean hasRePopTime() {
        return rePopTime != null;
    }

    public long getRePopTime() {
        return rePopTime;
    }
}

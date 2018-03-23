package com.btxtech.shared.gameengine.datatypes.config.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * User: beat
 * Date: 10.10.2011
 * Time: 13:37:24
 */
public class BotItemConfig {
    private Integer baseItemTypeId;
    private int count;
    private boolean createDirectly;
    private boolean noSpawn;
    private PlaceConfig place;
    private double angle;
    private boolean moveRealmIfIdle;
    private Integer idleTtl;
    private boolean noRebuild;
    private Integer rePopTime;

    public BotItemConfig setBaseItemTypeId(Integer baseItemTypeId) {
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

    public BotItemConfig setNoSpawn(boolean noSpawn) {
        this.noSpawn = noSpawn;
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

    public double getAngle() {
        return angle;
    }

    public BotItemConfig setAngle(double angle) {
        this.angle = angle;
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

    public BotItemConfig setRePopTime(Integer rePopTime) {
        this.rePopTime = rePopTime;
        return this;
    }

    public Integer getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public int getCount() {
        return count;
    }

    public boolean isCreateDirectly() {
        return createDirectly;
    }

    public boolean isNoSpawn() {
        return noSpawn;
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

    public Integer getRePopTime() {
        return rePopTime;
    }

    public BotItemConfig cloneWithAbsolutePosition(DecimalPosition absoluteCenter) {
        BotItemConfig botItemConfig = new BotItemConfig();
        botItemConfig.baseItemTypeId = baseItemTypeId;
        botItemConfig.count = count;
        botItemConfig.createDirectly = createDirectly;
        botItemConfig.noSpawn = noSpawn;
        botItemConfig.place = PlaceConfig.cloneWithAbsolutePosition(place, absoluteCenter);
        botItemConfig.angle = angle;
        botItemConfig.moveRealmIfIdle = moveRealmIfIdle;
        botItemConfig.idleTtl = idleTtl;
        botItemConfig.noRebuild = noRebuild;
        botItemConfig.rePopTime = rePopTime;
        return botItemConfig;
    }
}

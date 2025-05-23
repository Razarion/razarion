package com.btxtech.shared.gameengine.datatypes.config.bot;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.system.Nullable;

/**
 * User: beat
 * Date: 10.10.2011
 * Time: 13:37:24
 */
public class BotItemConfig {
    @CollectionReference(CollectionReferenceType.BASE_ITEM)
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

    public @Nullable Integer getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public void setBaseItemTypeId(@Nullable Integer baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isCreateDirectly() {
        return createDirectly;
    }

    public void setCreateDirectly(boolean createDirectly) {
        this.createDirectly = createDirectly;
    }

    public boolean isNoSpawn() {
        return noSpawn;
    }

    public void setNoSpawn(boolean noSpawn) {
        this.noSpawn = noSpawn;
    }

    public @Nullable PlaceConfig getPlace() {
        return place;
    }

    public void setPlace(@Nullable PlaceConfig place) {
        this.place = place;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public boolean isMoveRealmIfIdle() {
        return moveRealmIfIdle;
    }

    public void setMoveRealmIfIdle(boolean moveRealmIfIdle) {
        this.moveRealmIfIdle = moveRealmIfIdle;
    }

    public @Nullable Integer getIdleTtl() {
        return idleTtl;
    }

    public void setIdleTtl(@Nullable Integer idleTtl) {
        this.idleTtl = idleTtl;
    }

    public boolean isNoRebuild() {
        return noRebuild;
    }

    public void setNoRebuild(boolean noRebuild) {
        this.noRebuild = noRebuild;
    }

    public Integer getRePopTime() {
        return rePopTime;
    }

    public void setRePopTime(Integer rePopTime) {
        this.rePopTime = rePopTime;
    }

    public BotItemConfig baseItemTypeId(Integer baseItemTypeId) {
        setBaseItemTypeId(baseItemTypeId);
        return this;
    }

    public BotItemConfig count(int count) {
        setCount(count);
        return this;
    }

    public BotItemConfig createDirectly(boolean createDirectly) {
        setCreateDirectly(createDirectly);
        return this;
    }

    public BotItemConfig noSpawn(boolean noSpawn) {
        setNoSpawn(noSpawn);
        return this;
    }

    public BotItemConfig place(PlaceConfig place) {
        setPlace(place);
        return this;
    }

    public BotItemConfig angle(double angle) {
        setAngle(angle);
        return this;
    }

    public BotItemConfig moveRealmIfIdle(boolean moveRealmIfIdle) {
        setMoveRealmIfIdle(moveRealmIfIdle);
        return this;
    }

    public BotItemConfig idleTtl(Integer idleTtl) {
        setIdleTtl(idleTtl);
        return this;
    }

    public BotItemConfig noRebuild(boolean noRebuild) {
        setNoRebuild(noRebuild);
        return this;
    }

    public BotItemConfig rePopTime(Integer rePopTime) {
        setRePopTime(rePopTime);
        return this;
    }
}
